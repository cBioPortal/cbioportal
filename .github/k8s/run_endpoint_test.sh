#!/bin/bash
POD_NAME=$1
MAX_WAIT_TIME_IN_SECONDS=$2
URL=$3
SEARCH_STR=$4
POST_BODY=$5
EXPECT_EMPTY_LIST_RESPONSE=$6
EXPECT_EMPTY_RESPONSE=$7

OK_RESPONSE_CODE=200
EMPTY_LIST_RESPONSE='[]'

kubectl exec -it --namespace=performance-test $POD_NAME -- bash -c "curl --max-time $MAX_WAIT_TIME_IN_SECONDS -s -w '\n\n%{http_code}' -X POST '$URL' -H 'accept: application/json' -H 'Content-Type: application/json' -d '$POST_BODY'" > curl.out
RESPONSE_CODE=`tail -1 curl.out`
RESPONSE_BODY=`head -1 curl.out`

# did we get the response code we expect?
if [ "$RESPONSE_CODE" != "$OK_RESPONSE_CODE" ]; then
    echo "Request failed with response code '$RESPONSE_CODE', expected '$OK_RESPONSE_CODE'"
    exit 1
else
    # check if we expect [] and did not get it
    if [ "$EXPECT_EMPTY_LIST_RESPONSE" -eq 1 ]; then
        if [ "$RESPONSE_BODY" != "[]" ]; then
            echo "Request returned a non-empty response when we expect an empty list."
            exit 1
        fi
        # else expected empty list and got it
        exit 0
    fi
    # check if we expect an empty response and did not get it
    if [ "$EXPECT_EMPTY_RESPONSE" -eq 1 ]; then
        if [ ! -z "$RESPONSE_BODY" ]; then
            echo "Request returned a non-empty response when we expect an empty response."
            exit 1
        fi
        # else expected empty response and got it
        exit 0
    fi
    # check if we got an emtpy response or empty list
    if [ "$RESPONSE_BODY" == "$EMPTY_LIST_RESPONSE" ] || [ -z "$RESPONSE_BODY" ]; then
        echo "Request failed with empty response."
        exit 1
    else # check for search string in response
        if [[ "$RESPONSE_BODY" != *"$SEARCH_STR"* ]]; then
           echo "Response did not contain search string '$SEARCH_STR'."
            exit 1
        fi
    fi
    # else we are OK
    exit 0
fi
