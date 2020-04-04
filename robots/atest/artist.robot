*** Settings ***
Library    Collections
Library    RequestsLibrary

*** Variables ***
${SERVER URL} =    http://%{RESOURCESTORE_HOST}:3000
${ARTIST UUID} =    621bd465-0ada-4a59-bc13-97a5d2449a06
${ALBUM UUID} =    8458841a-9adc-4bca-82a3-2393dc28a2e4

*** Test Cases ***
Get Non-existing Artist
    Create Session    resourcestore    ${SERVER URL}
    ${RESPONSE} =    Get Request    resourcestore    /api/artists/${ARTIST UUID}
    Should Be Equal As Strings    ${RESPONSE.status_code}    200
    Should Be Equal As Strings    ${RESPONSE.json()}    []

Post New Artist
    &{ARTIST} =    Create Dictionary    id=${ARTIST UUID}    name=test
    ${ARTIST DATA} =    Set Variable    {"id": "${ARTIST UUID}", "name": "test"}
    ${HEADERS} =    Create Dictionary    content-type=application/json
    Create Session    resourcestore    ${SERVER URL}    headers=${HEADERS}
    ${RESPONSE} =    Post Request    resourcestore    /api/artists    data=${ARTIST DATA}
    Should Be Equal As Strings    ${RESPONSE.status_code}    200
    Should Be Equal As Strings    ${RESPONSE.json()["id"]}    ${ARTIST.id}

Get Artist List
    &{ARTIST} =    Create Dictionary    id=${ARTIST UUID}    name=test
    Create Session    resourcestore    ${SERVER URL}
    ${RESPONSE} =    Get Request    resourcestore    /api/artists
    Should Be Equal As Strings    ${RESPONSE.status_code}    200
    Should Be Equal As Strings    ${RESPONSE.json()[0]["id"]}    ${ARTIST.id}
    Should Be Equal As Strings    ${RESPONSE.json()[0]["name"]}    ${ARTIST.name}

Get Existing Artist That Has No Albums
    Create Session    resourcestore    ${SERVER URL}
    ${RESPONSE} =    Get Request    resourcestore    /api/artists/${ARTIST UUID}
    Should Be Equal As Strings    ${RESPONSE.status_code}    200
    Should Be Equal As Strings    ${RESPONSE.json()}    []

Post New Album For Artist
    ${ALBUM DATA} =    Set Variable    {"id": "${ALBUM UUID}", "name": "testalbum", "artist": "test", "artist-id": "${ARTIST UUID}", "formats": ["cd"]}
    ${HEADERS} =    Create Dictionary    content-type=application/json
    Create Session    resourcestore    ${SERVER URL}    headers=${HEADERS}
    ${RESPONSE} =    Post Request    resourcestore    /api/albums    data=${ALBUM DATA}
    Should Be Equal As Strings    ${RESPONSE.status_code}    200

Get Existing Artist That Has Albums
    &{ALBUM} =    Create Dictionary    id=${ALBUM UUID}    name=testalbum
    Create Session    resourcestore    ${SERVER URL}
    ${RESPONSE} =    Get Request    resourcestore    /api/artists/${ARTIST UUID}
    Should Be Equal As Strings    ${RESPONSE.status_code}    200
    Should Be Equal As Strings    ${RESPONSE.json()[0]["id"]}    ${ALBUM.id}
    Should Be Equal As Strings    ${RESPONSE.json()[0]["name"]}    ${ALBUM.name}

Get Album From Artist
    ${ALBUM} =    Create Dictionary    id=${ALBUM UUID}    name=testalbum    artist=test    artistId=${ARTIST UUID}    formats=cd
    Create Session    resourcestore    ${SERVER URL}
    ${RESPONSE} =    Get Request    resourcestore    /api/albums/${ALBUM UUID}
    Should Be Equal As Strings    ${RESPONSE.status_code}    200
    Should Be Equal As Strings    ${RESPONSE.json()["id"]}    ${ALBUM.id}
    Should Be Equal As Strings    ${RESPONSE.json()["name"]}    ${ALBUM.name}
    Should Be Equal As Strings    ${RESPONSE.json()["artist"]}    ${ALBUM.artist}
    Should Be Equal As Strings    ${RESPONSE.json()["artist-id"]}    ${ALBUM.artistId}
    Should Be Equal As Strings    ${RESPONSE.json()["formats"][0]}    ${ALBUM.formats}