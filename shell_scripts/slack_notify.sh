#!/bin/sh

MESSAGE=$1
SLACK_HOOK="https://hooks.slack.com/services/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"
/usr/bin/curl -X POST --data-urlencode 'payload={"channel": "#monitoring", "username": "account-id-police", "text": "'"$MESSAGE"'", "icon_emoji": ":cop:"}' $SLACK_HOOK
