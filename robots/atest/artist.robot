*** Settings ***
Library    Collections
Library    RequestsLibrary

*** Variables ***
${url} =    http://%{RESOURCESTORE_HOST}:3000

*** Test Cases ***
Get Non-existing User
    Create Session    resourcestore    ${url}
    ${response} =    Get Request    resourcestore    /api/artists/621bd465-0ada-4a59-bc13-97a5d2449a06
    Should Be Equal As Strings    ${response.status_code}    200
    Should Be Equal As Strings    ${response.json()}    []

Post New User
    &{jsondict} =    Create Dictionary    id    621bd465-0ada-4a59-bc13-97a5d2449a06    name    test
    ${data} =    Set Variable    {"id": "621bd465-0ada-4a59-bc13-97a5d2449a06", "name": "test"}
    ${headers} =    Create Dictionary    content-type=application/json
    Create Session    resourcestore    ${url}    headers=${headers}
    ${response} =    Post Request    resourcestore    /api/artists    data=${data}
    log to console    ${response.json()}
    Should Be Equal As Strings    ${response.status_code}    200

Get Artist List
    &{jsondict} =    Create Dictionary    id    621bd465-0ada-4a59-bc13-97a5d2449a06    name    test
    Create Session    resourcestore    ${url}
    ${response} =    Get Request    resourcestore    /api/artists
    Should Be Equal As Strings    ${response.status_code}    200
    Should Be Equal As Strings    ${response.json()[0]["id"]}    ${jsondict.id}

Get Existing User
    Create Session    resourcestore    ${url}
    ${response} =    Get Request    resourcestore    /api/artists/621bd465-0ada-4a59-bc13-97a5d2449a06
    Should Be Equal As Strings    ${response.status_code}    200
    Should Be Equal As Strings    ${response.json()}    []