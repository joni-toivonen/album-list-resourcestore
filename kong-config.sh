#!/bin/sh

CONTAINER_ID=$(docker ps -a | grep "album-list-resourcestore_kong_1" | grep -o '^[^ ]*')

if [ "$OS" = "Windows_NT" ] || [ "$OS" = "WindowsNT" ]; then
    DOCKER="winpty docker";
else
    DOCKER="docker";
fi;

$DOCKER exec -it $CONTAINER_ID curl -i -X POST --url http://localhost:8001/services/ --data 'name=album-list' --data 'url=http://resourcestore:3000'
$DOCKER exec -it $CONTAINER_ID curl -i -X POST --url http://localhost:8001/services/album-list/routes --data 'methods[]=GET&methods[]=POST'

$DOCKER exec -it $CONTAINER_ID curl -i -X POST --url http://localhost:8001/services/album-list/plugins/ --data 'name=key-auth'

$DOCKER exec -it $CONTAINER_ID curl -i -X POST --url http://localhost:8001/consumers/ --data "username=AlbumLister"
$DOCKER exec -it $CONTAINER_ID curl -i -X POST --url http://localhost:8001/consumers/AlbumLister/key-auth/ --data ''
