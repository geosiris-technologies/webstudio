/*
Copyright 2019 GEOSIRIS

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.geosiris.webstudio.etp;

import Energistics.Etp.v12.Datatypes.MessageHeader;
import Energistics.Etp.v12.Protocol.Discovery.*;
import com.geosiris.etp.communication.ClientInfo;
import com.geosiris.etp.communication.Message;
import com.geosiris.etp.protocols.handlers.DiscoveryHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;



public class DiscoveryHandler_WebStudio extends DiscoveryHandler{
    public static Logger logger = LogManager.getLogger(DiscoveryHandler_WebStudio.class);

    @Override
    public Collection<Message> on_GetDeletedResources(GetDeletedResources msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[DiscoveryHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetDeletedResourcesResponse(GetDeletedResourcesResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[DiscoveryHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetResources(GetResources msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[DiscoveryHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetResourcesEdgesResponse(GetResourcesEdgesResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[DiscoveryHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetResourcesResponse(GetResourcesResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[DiscoveryHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }


}