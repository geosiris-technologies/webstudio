FROM tomcat:10.1.0-M15-jdk11-openjdk-slim-buster as base

LABEL maintainer="valentin.gauthier@geosiris.com"

ARG WEBSTUDIO_WAR_PATH

RUN apt-get -y update
RUN apt-get -y install maven

# to modify config file
RUN apt-get -y install nano wget p7zip-full
# RUN apt-get install libjhdf5-java


#    _  _______ ____         __                    __                ___
#   | |/ / ___// __ \   ____/ /___ _      ______  / /___  ____ _____/ (_)___  ____ _
#   |   /\__ \/ / / /  / __  / __ \ | /| / / __ \/ / __ \/ __ `/ __  / / __ \/ __ `/
#  /   |___/ / /_/ /  / /_/ / /_/ / |/ |/ / / / / / /_/ / /_/ / /_/ / / / / / /_/ /
# /_/|_/____/_____/   \__,_/\____/|__/|__/_/ /_/_/\____/\__,_/\__,_/_/_/ /_/\__, /
#                                                                          /____/

RUN mkdir -p /config/data/xsd/energyml/common
RUN mkdir -p /config/data/xsd/energyml/resqml
RUN mkdir -p /config/data/xsd/energyml/witsml
RUN mkdir -p /config/data/xsd/energyml/prodml

RUN mkdir tmp_ZIP

# COMMON
    # v2.0
RUN wget http://geosiris.com/wp-content/uploads/2022/09/common_v2.0.zip -P tmp_ZIP/
RUN 7z x tmp_ZIP/common_v2.0.zip -o/config/data/xsd/energyml/common/
    # v2.1
RUN wget http://geosiris.com/wp-content/uploads/2022/09/common_v2.1.zip -P tmp_ZIP/
RUN 7z x tmp_ZIP/common_v2.1.zip -o/config/data/xsd/energyml/common/
    # v2.2
RUN wget http://geosiris.com/wp-content/uploads/2022/09/common_v2.2.zip -P tmp_ZIP/
RUN 7z x tmp_ZIP/common_v2.2.zip -o/config/data/xsd/energyml/common/
    # v2.3
RUN wget http://geosiris.com/wp-content/uploads/2022/09/common_v2.3.zip -P tmp_ZIP/
RUN 7z x tmp_ZIP/common_v2.3.zip -o/config/data/xsd/energyml/common/

# RESQML
    # v2.0.1
RUN wget http://geosiris.com/wp-content/uploads/2022/09/resqml_v2.0.1.zip -P tmp_ZIP/
RUN 7z x tmp_ZIP/resqml_v2.0.1.zip -o/config/data/xsd/energyml/resqml/
    # v2.2
RUN wget http://geosiris.com/wp-content/uploads/2022/09/resqml_v2.2.zip -P tmp_ZIP/
RUN 7z x tmp_ZIP/resqml_v2.2.zip -o/config/data/xsd/energyml/resqml/
    # v2.2_dev3
RUN wget http://geosiris.com/wp-content/uploads/2022/09/resqml_v2.2_dev3.zip -P tmp_ZIP/
RUN 7z x tmp_ZIP/resqml_v2.2_dev3.zip -o/config/data/xsd/energyml/resqml/

# Witsml
    # v2.0
RUN wget http://geosiris.com/wp-content/uploads/2022/09/witsml_v2.0.zip -P tmp_ZIP/
RUN 7z x tmp_ZIP/witsml_v2.0.zip -o/config/data/xsd/energyml/witsml/
    # v2.1
RUN wget http://geosiris.com/wp-content/uploads/2022/09/witsml_v2.1.zip -P tmp_ZIP/
RUN 7z x tmp_ZIP/witsml_v2.1.zip -o/config/data/xsd/energyml/witsml/

# Prodml
    # v2.0
RUN wget http://geosiris.com/wp-content/uploads/2022/09/prodml_v2.0.zip -P tmp_ZIP/
RUN 7z x tmp_ZIP/prodml_v2.0.zip -o/config/data/xsd/energyml/prodml/
    # v2.2
RUN wget http://geosiris.com/wp-content/uploads/2022/09/prodml_v2.2.zip -P tmp_ZIP/
RUN 7z x tmp_ZIP/prodml_v2.2.zip -o/config/data/xsd/energyml/prodml/

# Config file mapping path
RUN echo "{ \
    \"energyml.common2_0\" : \"/config/data/xsd/energyml/common/v2.0/xsd_schemas/AllCommonObjects.xsd\", \
    \"energyml.common2_1\" : \"/config/data/xsd/energyml/common/v2.1/xsd_schemas/EmlAllObjects.xsd\", \
    \"energyml.common2_2\" : \"/config/data/xsd/energyml/common/v2.2/xsd_schemas/EmlAllObjects.xsd\", \
    \"energyml.common2_3\" : \"/config/data/xsd/energyml/common/v2.3/xsd_schemas/EmlAllObjects.xsd\", \
 \
    \"energyml.resqml2_0_1\"    : \"/config/data/xsd/energyml/resqml/v2.0.1/xsd_schemas/ResqmlAllObjects.xsd\", \
    \"energyml.resqml2_2\"      : \"/config/data/xsd/energyml/resqml/v2.2/xsd_schemas/ResqmlAllObjects.xsd\", \
    \"energyml.resqml_dev3x_2_2\" : \"/config/data/xsd/energyml/resqml/v2.2_dev3/xsd_schemas/ResqmlAllObjects.xsd\", \
 \
    \"energyml.witsml2_0\" : \"/config/data/xsd/energyml/witsml/v2.0/xsd_schemas/WitsmlAllObjects.xsd\", \
    \"energyml.witsml2_1\" : \"/config/data/xsd/energyml/witsml/v2.1/xsd_schemas/WitsmlAllObjects.xsd\", \
 \
    \"energyml.prodml2_0\" : \"/config/data/xsd/energyml/prodml/v2.0/xsd_schemas/ProdmlAllObjects.xsd\", \
    \"energyml.prodml2_2\" : \"/config/data/xsd/energyml/prodml/v2.2/xsd_schemas/ProdmlAllObjects.xsd\" \
}" > /config/data/xsd/xsd_mapping.json

ENV webstudio_fpathToXSDMapping /config/data/xsd/xsd_mapping.json

# Cleaning zips
RUN rm -rf tmp_ZIP

# Additional datas (property kinds)
RUN mkdir -p /config/data/rc/
RUN wget http://geosiris.com/wp-content/uploads/2022/09/PropertyKindDictionary_v2.3.xml -P /config/data/rc/

ENV webstudio_pathToAdditionalObjectsDir /config/data/rc/


#    ______            _____                        __  _
#   / ____/___  ____  / __(_)___ ___  ___________ _/ /_(_)___  ____
#  / /   / __ \/ __ \/ /_/ / __ `/ / / / ___/ __ `/ __/ / __ \/ __ \
# / /___/ /_/ / / / / __/ / /_/ / /_/ / /  / /_/ / /_/ / /_/ / / / /
# \____/\____/_/ /_/_/ /_/\__, /\__,_/_/   \__,_/\__/_/\____/_/ /_/
#                        /____/

ENV WS_CONFIG_INI_FILE_PATH /config/sample-ws-config.ini

COPY ./docker/config/sample-ws-config.ini /config/sample-ws-config.ini

ADD docker/data/ /config/data

ENV webstudio_fpathToEPCPkgGroup /config/data/epcPackagesGroups.json
ENV webstudio_fpathToAccessibleDORMapping /config/data/resqmlAccessibleDORMapping.json
ENV webstudio_dirPathToComments /config/data/comments/
ENV webstudio_dirPathToExtTypes /config/data/extTypesAttributes/


#   ______                           __
#  /_  __/___  ____ ___  _________ _/ /_
#   / / / __ \/ __ `__ \/ ___/ __ `/ __/
#  / / / /_/ / / / / / / /__/ /_/ / /_
# /_/  \____/_/ /_/ /_/\___/\__,_/\__/

# 2 following lines to have the server at the root path and not at /WebStudio
RUN rm -rf /usr/local/tomcat/webapps/ROOT
ADD $WEBSTUDIO_WAR_PATH /usr/local/tomcat/webapps/ROOT.war


#     ______
#    / ____/  ______  ____  ________
#   / __/ | |/_/ __ \/ __ \/ ___/ _ \
#  / /____>  </ /_/ / /_/ (__  )  __/
# /_____/_/|_/ .___/\____/____/\___/
#           /_/

EXPOSE 80
EXPOSE 443



#  _       ___ __  __       __________ __
# | |     / (_) /_/ /_     / ___/ ___// /
# | | /| / / / __/ __ \    \__ \\__ \/ /
# | |/ |/ / / /_/ / / /   ___/ /__/ / /___
# |__/|__/_/\__/_/ /_/   /____/____/_____/


FROM base AS image-with-ssl

COPY docker/tomcat/ssl/sample-key.crt /usr/certificates/sample-key.crt
RUN chmod 644 /usr/certificates/sample-key.crt

COPY docker/tomcat/server-production.xml /usr/local/tomcat/conf/server.xml


#  _       ___ __  __                __     __________ __
# | |     / (_) /_/ /_  ____  __  __/ /_   / ___/ ___// /
# | | /| / / / __/ __ \/ __ \/ / / / __/   \__ \\__ \/ /
# | |/ |/ / / /_/ / / / /_/ / /_/ / /_    ___/ /__/ / /___
# |__/|__/_/\__/_/ /_/\____/\__,_/\__/   /____/____/_____/

FROM base AS image-no-ssl

# WARNING : to use this no-ssl image, use docker/tomcat/web-no-ssl.xml instead of 
#           src/main/webapp/WEB-INF/web.xml during maven build

COPY docker/tomcat/server-production-no-ssl.xml /usr/local/tomcat/conf/server.xml

