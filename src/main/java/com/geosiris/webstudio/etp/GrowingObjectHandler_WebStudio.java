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
import Energistics.Etp.v12.Protocol.GrowingObject.*;
import com.geosiris.etp.communication.ClientInfo;
import com.geosiris.etp.communication.Message;
import com.geosiris.etp.protocols.handlers.GrowingObjectHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;



public class GrowingObjectHandler_WebStudio extends GrowingObjectHandler{
    public static Logger logger = LogManager.getLogger(GrowingObjectHandler_WebStudio.class);

    @Override
    public Collection<Message> on_DeleteParts(DeleteParts msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_DeletePartsResponse(DeletePartsResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetChangeAnnotations(GetChangeAnnotations msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetChangeAnnotationsResponse(GetChangeAnnotationsResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetGrowingDataObjectsHeader(GetGrowingDataObjectsHeader msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetGrowingDataObjectsHeaderResponse(GetGrowingDataObjectsHeaderResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetParts(GetParts msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetPartsByRange(GetPartsByRange msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetPartsByRangeResponse(GetPartsByRangeResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetPartsMetadata(GetPartsMetadata msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetPartsMetadataResponse(GetPartsMetadataResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetPartsResponse(GetPartsResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PutGrowingDataObjectsHeader(PutGrowingDataObjectsHeader msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PutGrowingDataObjectsHeaderResponse(PutGrowingDataObjectsHeaderResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PutParts(PutParts msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PutPartsResponse(PutPartsResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_ReplacePartsByRange(ReplacePartsByRange msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_ReplacePartsByRangeResponse(ReplacePartsByRangeResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[GrowingObjectHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }


}