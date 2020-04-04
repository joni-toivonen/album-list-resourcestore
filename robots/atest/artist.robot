*** Settings ***
Library    Collections
Library    RequestsLibrary

*** Variables ***
${url} =    http://%{RESOURCESTORE_HOST}:3000
${artistuuid} =    621bd465-0ada-4a59-bc13-97a5d2449a06
${albumuuid} =    8458841a-9adc-4bca-82a3-2393dc28a2e4

*** Test Cases ***
Get Non-existing Artist
    Create Session    resourcestore    ${url}
    ${response} =    Get Request    resourcestore    /api/artists/${artistuuid}
    Should Be Equal As Strings    ${response.status_code}    200
    Should Be Equal As Strings    ${response.json()}    []

Post New Artist
    &{jsondict} =    Create Dictionary    id=${artistuuid}    name=test
    ${artistdata} =    Set Variable    {"id": "${artistuuid}", "name": "test"}
    ${headers} =    Create Dictionary    content-type=application/json
    Create Session    resourcestore    ${url}    headers=${headers}
    ${response} =    Post Request    resourcestore    /api/artists    data=${artistdata}
    Should Be Equal As Strings    ${response.status_code}    200

Get Artist List
    &{jsondict} =    Create Dictionary    id=${artistuuid}    name=test
    Create Session    resourcestore    ${url}
    ${response} =    Get Request    resourcestore    /api/artists
    Should Be Equal As Strings    ${response.status_code}    200
    Should Be Equal As Strings    ${response.json()[0]["id"]}    ${jsondict.id}
    Should Be Equal As Strings    ${response.json()[0]["name"]}    ${jsondict.name}

Get Existing Artist That Has No Albums
    Create Session    resourcestore    ${url}
    ${response} =    Get Request    resourcestore    /api/artists/${artistuuid}
    Should Be Equal As Strings    ${response.status_code}    200
    Should Be Equal As Strings    ${response.json()}    []

Post New Album For Artist
    ${albumdata} =    Set Variable    {"id": "${albumuuid}", "name": "testalbum", "artist": "test", "artist-id": "${artistuuid}", "formats": ["cd"]}
    ${headers} =    Create Dictionary    content-type=application/json
    Create Session    resourcestore    ${url}    headers=${headers}
    ${response} =    Post Request    resourcestore    /api/albums    data=${albumdata}
    Should Be Equal As Strings    ${response.status_code}    200

Get Existing Artist That Has Albums
    &{albumdict} =    Create Dictionary    id=${albumuuid}    name=testalbum
    Create Session    resourcestore    ${url}
    ${response} =    Get Request    resourcestore    /api/artists/${artistuuid}
    Should Be Equal As Strings    ${response.status_code}    200
    Should Be Equal As Strings    ${response.json()[0]["id"]}    ${albumdict.id}
    Should Be Equal As Strings    ${response.json()[0]["name"]}    ${albumdict.name}

Get Album From Artist
    ${albumdict} =    Create Dictionary    id=${albumuuid}    name=testalbum    artist=test    artistId=${artistuuid}    formats=cd
    Create Session    resourcestore    ${url}
    ${response} =    Get Request    resourcestore    /api/albums/${albumuuid}
    Should Be Equal As Strings    ${response.status_code}    200
    Should Be Equal As Strings    ${response.json()["id"]}    ${albumdict.id}
    Should Be Equal As Strings    ${response.json()["name"]}    ${albumdict.name}
    Should Be Equal As Strings    ${response.json()["artist"]}    ${albumdict.artist}
    Should Be Equal As Strings    ${response.json()["artist-id"]}    ${albumdict.artistId}
    Should Be Equal As Strings    ${response.json()["formats"][0]}    ${albumdict.formats}