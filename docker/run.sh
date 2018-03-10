#!/usr/bin/env bash

# stopping previous containers
docker rm -f http-service mongo rabbitmq

# mongo without login/password
docker run -d --net net1 --ip 172.18.0.11 --hostname mongo -p 27017:27017 --name mongo mongo
docker exec -it mongo mongorestore --db scraper-service --drop /tmp/backup/

# rabbitmq with admin http://localhost:15673/
docker run -d --net net1 --ip 172.18.0.13 --hostname rabbitmq -p 15673:15672 -p 5672:5672 --name rabbitmq rabbitmq

# http-service http://localhost:8080/
docker run -d --net net1 --ip 172.18.0.12 --hostname http-service --add-host rabbitmq:172.18.0.13 \
    --add-host mongo:172.18.0.11 -p 8080:8080 --name http-service http-service