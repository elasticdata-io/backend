sh docker/build.sh
sh docker/run.sh

http://localhost:8080 - app started 

http://localhost:15673 - rabbitmq admin plugin started 

docker container stop http-service mongo rabbitmq

docker rm http-service mongo rabbitmq