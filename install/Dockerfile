FROM gradle:4.10.3-jdk8 as build
COPY --chown=gradle . /src
WORKDIR /src
RUN gradle build --stacktrace --continue

FROM openjdk:8-jre-alpine as final
WORKDIR /src
COPY --from=build /src/build/libs/scraper-service-0.0.1-SNAPSHOT.jar scraper-service.jar
ENTRYPOINT exec java -jar scraper-service.jar
EXPOSE 8080