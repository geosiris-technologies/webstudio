FROM tomcat:10.1.0-M15-jdk11-openjdk-slim-buster

LABEL maintainer="valentin.gauthier@geosiris.com"

ARG CI_JOB_TOKEN
ENV CI_JOB_TOKEN $CI_JOB_TOKEN

RUN apt-get -y update
RUN apt-get -y install maven 

# to modify config file
RUN apt-get install nano 

COPY ./target/WebStudio*.war /usr/local/tomcat/webapps/ROOT.war

#### Tomcat configuration ####

# 2 following lines to have the server at the root path and not at /WebStudio
RUN rm -rf /usr/local/tomcat/webapps/ROOT
ADD /target/WebStudio*.war /usr/local/tomcat/webapps/ROOT.war

# RUN apt-get install libjhdf5-java

COPY docker/tomcat/server-production.xml /usr/local/tomcat/conf/server.xml

COPY docker/tomcat/ssl/sample-key.crt /usr/certificates/sample-key.crt
RUN chmod 644 /usr/certificates/sample-key.crt

# Configuration
ENV WS_CONFIG_INI_FILE_PATH /config/sample-ws-config.ini
COPY ./docker/config/sample-ws-config.ini /config/sample-ws-config.ini

COPY docker/data/ /config/data


EXPOSE 80
EXPOSE 443

