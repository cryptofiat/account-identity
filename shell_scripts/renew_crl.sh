#!/bin/sh

###################################################
# Usage: renew.sh [ cron_interval_sec ]
#	Default: cron_interval_sec = 43200
#
# Command-line script for updating CRLs
#       - Checks the next update time of the CRLs
#       - Takes into account the next time it will be launched.
#
# Variables that need definition:
#	CRL_PATH - full path to the CRL directory
#	ERROR_TO - email address to send email to
#	
# Variables that might need redefenition (default value in brackets)
#	BEFORE (1800) - if the script's next expeted run isn't happening at least that
#                       many seconds before the crl's expiration time, the crl
#                       is updated now.
#	DEBUG (yes) -   log progress messages to stdout
#	MAIL_SUBJECT ('ESTEID CRL Update Error') - 
#                       subject of the email that is sent when crl update
#                       fails
#	CRON_INTERVAL (43200) - the script is expected to be run again after
#                               that many seconds (this parameter can be given
#                               on the command line.
#
# You also need to revise all the paths to commands listed at
# the beginning of the script. The current values were only tested on
# Ubuntu 14.04.
#
# Examine the code of the "run" function to make sure that
# all the required CRLs are being downloaded.
#
# You need to have the mailutils package installed for the error emails to work.
#
# Author: Konstantin Tretyakov <kt@ut.ee>
# License: GPL v3.
#
# This script is a minor modification of an original script by:
#
# Autor: Reigo Küngas <reigo@cvotech.com>
# (C) Copyright 2003 Reigo Küngas; Cvo Technologies
#
# The original script was licensed under GPLv2.
# it is accessible at http://id.ee/public/renew.sh
#
####################################################


### Utilities and scripts ####
OPENSSL="/usr/bin/openssl";
CUT="/usr/bin/cut";
CAT="/bin/cat";
DATE="/bin/date";
WGET="/usr/bin/wget";
LN="/bin/ln";
MV="/bin/mv";
RM="/bin/rm";
SLEEP="/bin/sleep";
MAIL="/bin/mail";
PIDOF="/sbin/pidof";
KILLALL="/usr/bin/killall";
EXPR="/usr/bin/expr";
IS_RUNNING_CMD="/sbin/pidof /usr/sbin/httpd";
RELOAD_CMD="/usr/sbin/apachectl restart";
NEXTUPDATE="${OPENSSL} crl -nextupdate -noout";
FILE_OWNER="www-data:www-data"
EID_DIR="/etc/apache2/certs/eid"
SLACK_NOTIFIER="${EID_DIR}/slack_notify.sh"

#### Directory with CRLs to be updated #####
CRL_PATH="${EID_DIR}/crl"

### Subject of the email
MAIL_SUBJECT="ESTEID CRL Update Error";

#### Will update CRL if the next run doesn't happen at least 30 minutes before the expiration
BEFORE=1800;

### Assume the script is set up to run twice a day
CRON_INTERVAL=43200;

### DEBUG
DEBUG="yes";

### If a command line given, this is assumed to be CRON_INTERVAL
if test $# -gt 0
then
	CRON_INTERVAL=$1;
fi

### When the script started
START=`${DATE} +"%s"`;

### When it will run next time
NEXT_RUN=`${EXPR} $START + $CRON_INTERVAL`;


### Debug output
output()
{
	if test -n "${DEBUG}"
	then
		echo `${DATE} +"%T"` "$*";
	fi
}

### Send error notification
error()
{
	./slack_notify.sh alert "$*"
}

### Send 'ok' notification
notify()
{
	./slack_notify.sh ok "$*"
}

### Send 'warn' notification
warn()
{
	./slack_notify.sh warn "$*"
}

### Download a given CRL
get()
{
	url=$1
	file=`echo $url|awk -F / '{print $(NF)}'`

	if test -z "$file"
	then
		error "Error: empty file (url=$url)";
		file="index.crl"
	fi

	### Backup
	if test -s "$file"
	then
		${MV} -f "${file}" "${file}.bu"
	fi

	### Download
	if ! test -s "$file"
	then
		output "Getting $url -> $file"
		${WGET} -q "$url" -O "$file"
	fi

	${OPENSSL} crl -in "${file}" -out "${file}" -inform DER

	# Assign ownership
	chown ${FILE_OWNER} ${file}

	# Make symlink
	symlink="`${OPENSSL} crl -hash -noout -in ${file}`.r0";
	if ! test -e "${symlink}"
	then
		output "Making symlink ${symlink}"
		${LN} -s "${file}" "${symlink}"
	fi
}


### Check whether the CRL needs to be downloaded and do it if so
check()
{
	url=$1
	file=`echo $url|awk -F / '{print $(NF)}'`

    output "Checking file: $file";

	if test -z "$file"
	then
		error "Error: empty file (url=$url)";
		return;
	fi

	if test -s "$file"
	then
		nexttime=`${NEXTUPDATE} -in $file|${CUT} -f2 -d=`;
		next_sec=`${DATE} +"%s" -d"${nexttime}"`;
		need_update=`${EXPR} $next_sec - ${BEFORE}`;

		now=`${DATE} +"%s"`;

		if test ${NEXT_RUN} -lt $need_update
		then
			return;
		fi

		if test $need_update -le $now
		then
			if test $next_sec -lt $now
			then
				output "NB Expired: $file";
			fi
			get "$url";
		else
			MY_SLEEP=`${EXPR} $need_update - $now`;
			if test -z "$DO_SLEEP" || test $DO_SLEEP -gt $MY_SLEEP
			then
				DO_SLEEP=$MY_SLEEP;
				return;
			fi
		fi
	else
		output "NB! File does not exists: $file";
		get "$url";
	fi

	
	if ! test -s "$file"
	then
		error "NB! File still does not exists: $file";
		get "$url";
	else
		nexttime=`${NEXTUPDATE} -in $file|${CUT} -f2 -d=`;
		next_sec=`${DATE} +"%s" -d"${nexttime}"`;
		need_update=`${EXPR} $next_sec - ${BEFORE}`;
		
		##
		# Check the next cron time just in case
		#
		if test ${NEXT_RUN} -gt $need_update
		then
			warn "NB! CRL expires before next run. Re-scheduling next run.";
			now=`${DATE} +"%s"`;
			MY_SLEEP=`${EXPR} $need_update - $now`;
			if test -z "$DO_SLEEP" || test $DO_SLEEP -gt $MY_SLEEP
			then
				DO_SLEEP=$MY_SLEEP;
				return;
			fi
		fi
		
	fi
}

### Main function
run()
{
	cd ${CRL_PATH};
	DO="yes";
	while test -n "$DO"
	do
		output "Checking certs";

		#expired_26.08.2016 check "https://sk.ee/crls/juur/crl.crl"
		check "https://sk.ee/crls/eeccrca/eeccrca.crl"
		#expired_26.08.2016 check "https://sk.ee/crls/esteid/esteid2007.crl"
		check "https://sk.ee/repository/crls/esteid2011.crl"
		check "https://sk.ee/crls/esteid/esteid2015.crl"

		notify "Checked and hopefully updated all CRLs"

		# Reload apache
		$RELOAD_CMD || error "Failed to reload Apache";

		if test -n "$DO_SLEEP"
		then
			if test $DO_SLEEP -lt 1
			then
				output "Overriding negative DO_SLEEP: ${DO_SLEEP}, probably the downloaded CRL file was already expired!!!";
                error "Overriding negative DO_SLEEP: ${DO_SLEEP}, probably the downloaded CRL file was already expired!!!";
				DO_SLEEP=900;
			fi
			output "Sleep ${DO_SLEEP}";
			${SLEEP} "${DO_SLEEP}"
			DO_SLEEP="";
		else
			DO="";
		fi
	done
	cd -;
}


run;
exit;

