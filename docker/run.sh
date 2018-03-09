#!/usr/bin/env bash

docker run -d --net net1 --ip 172.18.0.11 --hostname mongo  --name mongo mongo
docker run -d --net net1 --ip 172.18.0.12 --hostname http-service --add-host rabbitmq:172.18.0.13 --add-host mongo:172.18.0.11 -p 8080:8080 --name http-service http-service
docker run -d --net net1 --ip 172.18.0.13 --hostname rabbitmq -p 15673:15672 --name rabbitmq rabbitmq:3-management