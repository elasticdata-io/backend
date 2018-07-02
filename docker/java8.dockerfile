FROM oraclelinux:7-slim

ENV JAVA_PKG=docker/server-jre-8u*-linux-x64.tar.gz \
    JAVA_HOME=/usr/java/default

ADD $JAVA_PKG /usr/java/

RUN export JAVA_DIR=$(ls -1 -d /usr/java/*) && \
    ln -s $JAVA_DIR /usr/java/latest && \
    ln -s $JAVA_DIR /usr/java/default && \
    alternatives --install /usr/bin/java java $JAVA_DIR/bin/java 20000 && \
    alternatives --install /usr/bin/javac javac $JAVA_DIR/bin/javac 20000 && \
    alternatives --install /usr/bin/jar jar $JAVA_DIR/bin/jar 20000

WORKDIR /opt

RUN yum -y install unzip
RUN yum -y install wget

RUN wget https://bintray.com/artifact/download/groovy/maven/apache-groovy-binary-2.4.13.zip
RUN unzip apache-groovy-binary-2.4.13.zip

RUN wget https://services.gradle.org/distributions/gradle-4.6-bin.zip
RUN unzip gradle-4.6-bin.zip

RUN ln /opt/groovy-2.4.13/bin/groovy /bin/groovy
