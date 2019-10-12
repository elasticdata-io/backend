FROM gradle:4.10.2-jdk8 as build
COPY --chown=gradle . /src
WORKDIR /src
RUN gradle asciidoctor --stacktrace --continue

FROM nginx:1.12.2 AS final
WORKDIR /usr/share/nginx/html
COPY install/nginx.conf /tmp/nginx.conf
RUN rm /etc/nginx/conf.d/default.conf
RUN cp /tmp/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /src/build/docs/html5 .
EXPOSE 80