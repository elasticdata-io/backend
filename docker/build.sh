#!/usr/bin/env bash

docker image rm -f bombascter/http-service
docker build --no-cache -t bombascter/http-service -f docker/http-service .

docker image rm -f bombascter/mongo
docker build --no-cache -t bombascter/mongo -f docker/mongo .