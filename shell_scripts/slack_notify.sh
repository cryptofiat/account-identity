#!/bin/sh

HOSTNAME=$(hostname -f)
LEVEL="warn" # one of ok / warn / alert
MESSAGE="Hello from ${HOSTNAME}"
SLACK_HOOK="https://hooks.slack.com/services/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"
EMOJI=":grey_exclamation:"

if test $# -eq 1
then
	MESSAGE=$1;
fi
if test $# -gt 1
then
	LEVEL=$1;
	MESSAGE=$2;
fi

run()
{
  set_emoji
	/usr/bin/curl -X POST --data-urlencode 'payload={"channel": "#monitoring", "username": "account-id-police", "text": "*'"$LEVEL"'* '"$MESSAGE"'", "icon_emoji": "'$EMOJI'"}' $SLACK_HOOK
}

set_emoji()
{
  if [ "$LEVEL" == "ok" ]; then
  	EMOJI=":white_check_mark:"
  elif [ "$LEVEL" == "warn" ]; then
  	EMOJI=":grey_exclamation:"
  else
  	EMOJI=":exclamation:"
  fi
}

run;
exit;