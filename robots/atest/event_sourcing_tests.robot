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
    ${RESPONSE} =    Get On Session    resourcestore    /api/events
    Should Be Equal As Strings    ${RESPONSE.status_code}    200
    ${LOCAL DATABASE}    Set Variable    ${RESPONSE.json()}
    Should Be Equal As Strings    ${RESPONSE.json()[0]["event-id"]}    ${LOCAL DATABASE[0]["event-id"]}
    Should Be Equal As Integers    ${LOCAL DATABASE[0]["event-id"]}    1
    Should Be Equal As Integers    ${LOCAL DATABASE[1]["event-id"]}    2
    Set Global Variable    ${LOCAL DATABASE}

Getting Events With Latest Event Id Should Return Empty List Of Events
    Create Session    resourcestore    ${SERVER URL}    max_retries=5    backoff_factor=1.0
    ${LATEST EVENT ID} =    Convert To String    ${LOCAL DATABASE[3]["event-id"]}
    ${RESPONSE} =    Get On Session    resourcestore    /api/events/${LATEST EVENT ID}
    Should Be Equal As Strings    ${RESPONSE.status_code}    200
    Should Be Equal As Strings    ${RESPONSE.json()}    []

Put Request With Existing Album Id Should Update Existing Album With Given Data
    ${ALBUM DATA} =    Set Variable    {"id": "${ALBUM UUID}", "name": "testalbum2", "artist": "test", "artist-id": "${ARTIST UUID}", "formats": ["cd"]}
    ${HEADERS} =    Create Dictionary    content-type=application/json
    Create Session    resourcestore    ${SERVER URL}    headers=${HEADERS}
    ${RESPONSE} =    Put On Session    resourcestore    /api/albums/${ALBUM UUID}    data=${ALBUM DATA}
    Should Be Equal As Strings    ${RESPONSE.status_code}    200
    Should Be Equal As Strings    ${RESPONSE.json()["name"]}    testalbum2

Get Events After Updating Existing Album
    Create Session    resourcestore    ${SERVER URL}    max_retries=5    backoff_factor=1.0
    ${LATEST EVENT ID} =    Convert To String    ${LOCAL DATABASE[1]["event-id"]}
    ${RESPONSE} =    Get On Session    resourcestore    /api/events/${LATEST EVENT ID}
    Should Be Equal As Strings    ${RESPONSE.status_code}    200
    Should Be Equal As Strings    ${RESPONSE.json()[0]["event-id"]}    3

Getting Updated Album Should Return Album With Updated Data
    ${ALBUM} =    Create Dictionary    id=${ALBUM UUID}    name=testalbum2    artist=test    artistId=${ARTIST UUID}    formats=cd
    Create Session    resourcestore    ${SERVER URL}
    ${RESPONSE} =    Get On Session    resourcestore    /api/albums/${ALBUM UUID}
    Should Be Equal As Strings    ${RESPONSE.status_code}    200
    Should Be Equal As Strings    ${RESPONSE.json()["id"]}    ${ALBUM.id}
    Should Be Equal As Strings    ${RESPONSE.json()["name"]}    ${ALBUM.name}
    Should Be Equal As Strings    ${RESPONSE.json()["artist"]}    ${ALBUM.artist}
    Should Be Equal As Strings    ${RESPONSE.json()["artist-id"]}    ${ALBUM.artistId}
    Should Be Equal As Strings    ${RESPONSE.json()["formats"][0]}    ${ALBUM.formats}