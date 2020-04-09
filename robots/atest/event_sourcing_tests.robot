*** Settings ***
Library    Collections
Library    RequestsLibrary

*** Variables ***
${SERVER URL} =    http://%{RESOURCESTORE_HOST}:3000
${ARTIST UUID} =    621bd465-0ada-4a59-bc13-97a5d2449a06
${ALBUM UUID} =    8458841a-9adc-4bca-82a3-2393dc28a2e4
${LOCAL DATABASE} =    []
${LOCAL CHANGES} =    []

*** Test Cases ***
Get Current Events
    Create Session    resourcestore    ${SERVER URL}    max_retries=5    backoff_factor=1.0
    ${RESPONSE} =    Get Request    resourcestore    /api/events
    Should Be Equal As Strings    ${RESPONSE.status_code}    200
    ${LOCAL DATABASE}    Set Variable    ${RESPONSE.json()}
    Should Be Equal As Strings    ${RESPONSE.json()[0]["event-id"]}    ${LOCAL DATABASE[0]["event-id"]}
    Should Be Equal As Integers    ${LOCAL DATABASE[0]["event-id"]}    1
    Should Be Equal As Integers    ${LOCAL DATABASE[1]["event-id"]}    2
    Set Global Variable    ${LOCAL DATABASE}

Getting Events With Latest Event Id Should Return Empty List Of Events
    Create Session    resourcestore    ${SERVER URL}    max_retries=5    backoff_factor=1.0
    ${LATEST EVENT ID} =    Convert To String    ${LOCAL DATABASE[1]["event-id"]}
    ${RESPONSE} =    Get Request    resourcestore    /api/events/${LATEST EVENT ID}
    Should Be Equal As Strings    ${RESPONSE.status_code}    200
    ${LOCAL DATABASE}    Set Variable    ${RESPONSE.json()}
    Should Be Equal As Strings    ${RESPONSE.json()}    []
    Set Global Variable    ${LOCAL DATABASE}