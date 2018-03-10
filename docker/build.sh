#!/usr/bin/env bash

# run this script from parent directory
docker build -t http-service -f docker/http-service .
docker build -t mongo -f docker/mongo .
docker build -t rabbitmq -f docker/rabbitmq .

docker network create --subnet=172.18.0.0/16 net1