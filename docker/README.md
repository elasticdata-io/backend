sh docker/build.sh
sh docker/run.sh

http://localhost:8080 - app started 

http://localhost:15673 - rabbitmq admin plugin started 

docker container stop http-service mongo rabbitmq

docker rm http-service mongo rabbitmq


docker run --rm --name mongo -d mongo

docker exec -it mongo bash

mongorestore --db scraper-service --drop /tmp/backup/

docker container stop mongo

docker run --rm --name mongo -d mongo --auth

docker exec -it mongo mongo admin

db.createUser({ user: 'bombascter', pwd: '!Prisoner31!', roles: [ { role: "userAdminAnyDatabase", db: "admin" } ] });

docker run -it --rm --link mongo:mongo mongo mongo -u bombascter -p '!Prisoner31!' --authenticationDatabase admin mongo/scraper-service


    