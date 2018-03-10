sh docker/build.sh
sh docker/run.sh

http://localhost:8080 - app started 

http://localhost:15673 - rabbitmq admin plugin started 

```bash
# stop and remove all containers
docker container stop http-service mongo rabbitmq && docker rm http-service mongo rabbitmq
```

```bash
# restore database from backup
# docker run --rm --name mongo -d mongo
# after run 'docker run -d --net net1 --ip 172.18.0.11 --hostname mongo -p 27017:27017 --name mongo mongo'
docker exec -it mongo mongorestore --db scraper-service --drop /tmp/backup/
# docker container stop mongo
```

```bash
# !!! Optional 
# add admin user auth 
docker run --rm --name mongo -d mongo --auth
docker exec -it mongo mongo admin
```

```
db.createUser({ user: 'bombascter', pwd: '!Prisoner31!', roles: [ { role: "userAdminAnyDatabase", db: "admin" } ] });
exit
```

```bash
# run this for testing connection 
docker run -it --rm --link mongo:mongo mongo mongo -u bombascter -p '!Prisoner31!' --authenticationDatabase admin mongo/scraper-service
```

    