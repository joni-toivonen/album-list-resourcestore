*** Settings ***
Library    Collections
Library	   RequestsLibrary

*** Variables ***
${url}    http://%{RESOURCESTORE_HOST}:3000
${json}    {"id": "621bd465-0ada-4a59-bc13-97a5d2449a06", "name": "test"}

*** Test Cases ***
Get Non-existing User
    Create Session    resourcestore    ${url}
    ${response}=    Get Request    resourcestore    /api/artists/621bd465-0ada-4a59-bc13-97a5d2449a06
    Should Be Equal As Strings    ${response.status_code}    200
    Should Be Equal As Strings    ${response.json()}    []

Post New User
    ${headers}=    Create Dictionary    content-type=application/json
    Create Session    resourcestore    ${url}    headers=${headers}
    ${response}=    Post Request    resourcestore    /api/artists    data=${json}
    log to console    ${response.json()}
    Should Be Equal As Strings    ${response.status_code}    200

Get Artist List
    Create Session    resourcestore    ${url}
    ${response}=    Get Request    resourcestore    /api/artists
    Should Be Equal As Strings    ${response.status_code}    200
    Should Be Equal As Strings    ${response.json()}    [${json}]

Get Existing User
    Create Session    resourcestore    ${url}
    ${response}=    Get Request    resourcestore    /api/artists/621bd465-0ada-4a59-bc13-97a5d2449a06
    Should Be Equal As Strings    ${response.status_code}    200
    Should Be Equal As Strings    ${response.json()}    []