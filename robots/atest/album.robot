*** Settings ***
Library    Collections
Library    RequestsLibrary

*** Variables ***
${SERVER URL} =    http://%{RESOURCESTORE_HOST}:3000
${NON EXISTING ALBUM ID} =    12345678-1234-4321-9876-123456789abc

*** Test Cases ***
Trying To Get Non-existing Album Should Return 404
    Create Session    resourcestore    ${SERVER URL}    max_retries=5    backoff_factor=1.0
    ${RESPONSE} =    Get Request    resourcestore    /api/albums/${NON EXISTING ALBUM ID}
    Should Be Equal As Strings    ${RESPONSE.status_code}    404

Trying To Update Non-existing Album Should Return 404
    ${ALBUM DATA} =    Set Variable    {"id": "${NON EXISTING ALBUM ID}", "name": "testalbum2", "artist": "test", "artist-id": "${NON EXISTING ALBUM ID}", "formats": ["cd"]}
    ${HEADERS} =    Create Dictionary    content-type=application/json
    Create Session    resourcestore    ${SERVER URL}    headers=${HEADERS}    max_retries=5    backoff_factor=1.0
    ${RESPONSE} =    Put Request    resourcestore    /api/albums/${NON EXISTING ALBUM ID}    data=${ALBUM DATA}
    Should Be Equal As Strings    ${RESPONSE.status_code}    404