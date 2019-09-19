docker build --no-cache -t build-jar -f docker/build-jar .
docker container create --name extract build-jar
docker container cp extract:/src/build/libs/scraper-service.jar ./scraper-service.jar
docker container rm -f extract
docker image rm build-jar
docker image rm -f bombascter/http-service
docker build --no-cache -t bombascter/http-service -f docker/build-service .
docker login -u bombascter -p '!Prisoner31!'
docker push bombascter/http-service
docker image rm -f bombascter/http-service