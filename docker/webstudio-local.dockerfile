FROM tomcat:10.1.0-M15-jdk11-openjdk-slim-buster

LABEL maintainer="valentin.gauthier@geosiris.com"

ARG CI_JOB_TOKEN
ENV CI_JOB_TOKEN $CI_JOB_TOKEN

RUN apt-get -y update
RUN apt-get -y install maven 

# to modify config file
RUN apt-get -y install nano 


RUN rm -rf /usr/local/tomcat/webapps/ROOT

#################################### (1) DOCKER COMPILE ########
## Building with maven
# RUN mkdir ws_build
# COPY ./src ws_build/src
# COPY ./settings.xml ws_build/settings.xml
# COPY ./pom.xml ws_build/pom.xml

# RUN mvn -f ws_build/pom.xml clean 
# RUN mvn package -s ws_build/settings.xml -f ws_build/pom.xml

# # #### Tomcat configuration ####

# RUN cp ws_build/target/WebStudio*.war /usr/local/tomcat/webapps/ROOT.war

#################################### (2) LOCAL WAR #############
# Use after local compiling with : mvn -U -s settings.xml clean package
COPY ./target/WebStudio*.war /usr/local/tomcat/webapps/ROOT.war

############################################################

# RUN apt-get install libjhdf5-java

COPY docker/tomcat/server-production.xml /usr/local/tomcat/conf/server.xml

COPY docker/tomcat/ssl/sample-key.crt /usr/certificates/sample-key.crt
RUN chmod 644 /usr/certificates/sample-key.crt

# Configuration
ENV WS_CONFIG_INI_FILE_PATH /config/sample-ws-config.ini
COPY ./docker/config/sample-ws-config-local.ini /config/sample-ws-config.ini

COPY docker/data/ /config/data

# full version port
EXPOSE 80
EXPOSE 443