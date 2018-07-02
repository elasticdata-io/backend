#!/usr/bin/env bash

# run this script from parent directory
# docker build --no-cache -t bombascter/java8 -f java8.dockerfile .
docker build --no-cache -t http-service -f docker/http-service .
docker build --no-cache -t mongo -f docker/mongo .
docker build --no-cache -t rabbitmq -f docker/rabbitmq .