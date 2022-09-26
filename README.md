# WebStudio

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-orange.svg)](https://sonarcloud.io/summary/new_code?id=geosiris-technologies_webstudio)

## Introduction

The WebStudio is a web application that allows to manipulate energyml file (such as EPC or individual xml files)

## Features

- Edit/create energyml (resqml/witsml/prodml) files.
- Working with [ETP](https://www.energistics.org/energistics-transfer-protocol/) servers
- Generate EPC files
- Validation of EPC file content
- Graph and tree visualisation of energyml data

## Requirements

- Java 11
- Maven
- Tomcat 10
- Docker
- Docker-compose

## Version History

- 1.0.1: 
    - Initial working Release

## License

This project is licensed under the Apache 2.0 License - see the `LICENSE` file for details

## Support

Please enter an issue in the repo for any questions or problems.

## Compile the project:

```bash
mvn -U -s settings.xml clean package
```

If you do not have a **settings.xml** file, please copy the **ci_settings.xml** file and replace the **${env.CI_JOB_TOKEN}** variable by your own token, and save this new file as **settings.xml**

## Related projects :

 - https://github.com/geosiris-technologies/cloud-storage-api
 - https://github.com/geosiris-technologies/energyml-java-generator
 - https://github.com/geosiris-technologies/energyml-utils
 - https://github.com/geosiris-technologies/etpproto-java
 - https://github.com/geosiris-technologies/etptypes-java

## Run the code :

### For local tests (and local code changes):

1. If you want to use your 'hand-compiled' **.war** file, please first refer to [Compile the project](#compile-the-project) section.
2. If you want to compile inside the docker, comment the **LOCAL WAR** part of the [docker/webstudio-local.dockerfile](file:///./docker/webstudio-local.dockerfile), and uncomment the **DOCKER COMPILE** part.

Run the following commands in the **docker/compose** folder.
```bash
docker-compose -f docker-compose-local.yml -p webstudio-public-local pull
docker-compose -f docker-compose-local.yml -p webstudio-public-local build
docker-compose -f docker-compose-local.yml -p webstudio-public-local up -d
```

**Note :** Remove the *-d* option for *up* command if you want to follow directly the main logs (not debug logs).

### For local tests (need to compile the maven project yourself before):

Run the following commands in the **docker/compose** folder.
```bash
docker-compose -f docker-compose.yml -p webstudio-official pull
docker-compose -f docker-compose.yml -p webstudio-official build
docker-compose -f docker-compose.yml -p webstudio-official up -d
```

**Note :** Remove the *-d* option for *up* command if you want to follow directly the main logs (not debug logs).

### Reset all storage volumes (use carefully):

If you have trouble with your local docker volumes (user database or workspace save).

```bash
docker-compose -f docker-compose-local.yml -p webstudio-public-local down --volumes
docker-compose -f docker-compose.yml -p webstudio-official down --volumes
```


### Run standalone instance :

```console
docker run -d \
  -p 80:80 -p 443:443 \
  --env webstudio_enableUserDB=false \
  --env webstudio_enableWorkspace=false \
  geomods/geomods-webstudio:1.0.0 
```


## Setting up project :

### With ini file :

The WebStudio configuration can be done with a ".ini" file. Examples are given in the "docker/" folder of this projet.
To be found by the WebStudio, you must set the environment variable **WS_CONFIG_INI_FILE_PATH** to the path of this file. 
Example in a dockerFile : 
```dockerfile
ENV WS_CONFIG_INI_FILE_PATH /config/sample-ws-config.ini
```

**User Database**

Your can parametrize the WebStudio to use or not a user database with postgresSQL.
To enable this feature, set the property **enableUserDB** to **true** in the *webstudio* section.
```ini
[webstudio]
...
enableUserDB=true
...
```


To configure the database connection you can modify the ini file (with your own values) by creating a *userdb* section : 
```ini
[userdb]
databaseType=postgres 
host=psql_demo
port=5432
login=postgres
password=pwd_ws_DEMO
hashSalt=mqgztZ1VbL4cOwFjkwbARY
```

**User Workspace persitence**

The WebStudio also allows to save the work in progress for each user. This feature allows to recover data even if the user has been disconnected.+

*Warning :* Thuis feature is only enabled if the user-database is also enabled.

To enable this feature, set the property **enableWorkspace** to **true** in the *webstudio* section.
```ini
[webstudio]
...
enableWorkspace=true
...
```

To configure the storage of the xml files, you can use an S3 bucket, Azure Blob Storage, or Google Cloud Storage.

Example for bucket s3 :
```ini
[s3]
localstackEnabled=true
; must not have underscore in the url !
localstackEndpoint=http://workspace-minio:9000 
localstackRegion=us-east-1
accessKey=minioadmin
secretKey=minioadmin
```

Example for Azure :
```ini
[azureblobstorage]
connectionString=YOUR_VALUE 
containerName=CONTAINER_NAME
```

Example for Google :
```ini
[googlecloudstorage]
keyfile=PATH_TO_YOUR_FILE 
```

### With environment variables:

All variables set in the ini file can be **overwritten** with environment variable. To set variables you must follow the naming rule : 
```
[INI_SECTION_LOWERCASE]_[VARIABLE_WITH_CASE]
```

Example for S3 Property in a docker-compose : 
```dockerfile
environment:
  webstudio_enableUserDB: true
  webstudio_enableWorkspace: true
  workspace_databaseType: s3
  s3_localstackEndpoint: http://my-workspace:9000 
  s3_accessKey: myAdminLogin
  s3_secretKey: myAdminPassword
```

## Change the https certs:

Generate the key with your password
```bash
keytool -genkey -alias tomcat -keyalg RSA -keystore sample-key.crt -storepass DemoCRT_password
```

Change the **docker/server-production.xml** to have a connector like this:

```java 
<Connector
    protocol="org.apache.coyote.http11.Http11NioProtocol"
    port="8443"
    maxThreads="150"
    SSLEnabled="true">
  <SSLHostConfig>
    <Certificate
      certificateKeystoreFile="/usr/certificates/sample-key.crt"
      certificateKeystorePassword="DemoCRT_password"
      type="RSA"
      />
    </SSLHostConfig>
</Connector>
```


## Check logs if run fails :

If a error like "org.apache.jasper.servlet.TldScanner.scanJars At least one JAR was scanned for TLDs yet contained no TLDs.", check the tomcat 
logs to see which servlet failed to start and why:

```bash
cat /usr/local/tomcat/logs/localhost.***.log
cat /logs/webstudio.log
```