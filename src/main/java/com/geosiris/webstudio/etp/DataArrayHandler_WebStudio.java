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
import Energistics.Etp.v12.Protocol.DataArray.*;
import com.geosiris.etp.communication.ClientInfo;
import com.geosiris.etp.communication.Message;
import com.geosiris.etp.protocols.handlers.DataArrayHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;



public class DataArrayHandler_WebStudio extends DataArrayHandler{
    public static Logger logger = LogManager.getLogger(DataArrayHandler_WebStudio.class);

    @Override
    public Collection<Message> on_GetDataArrayMetadata(GetDataArrayMetadata msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        // logger.info("[DataArrayHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetDataArrayMetadataResponse(GetDataArrayMetadataResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        // logger.info("[DataArrayHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetDataArrays(GetDataArrays msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        // logger.info("[DataArrayHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetDataArraysResponse(GetDataArraysResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        // logger.info("[DataArrayHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetDataSubarrays(GetDataSubarrays msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        // logger.info("[DataArrayHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetDataSubarraysResponse(GetDataSubarraysResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        // logger.info("[DataArrayHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PutDataArrays(PutDataArrays msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        // logger.info("[DataArrayHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PutDataArraysResponse(PutDataArraysResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        // logger.info("[DataArrayHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PutDataSubarrays(PutDataSubarrays msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        // logger.info("[DataArrayHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PutDataSubarraysResponse(PutDataSubarraysResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        // logger.info("[DataArrayHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PutUninitializedDataArrays(PutUninitializedDataArrays msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        // logger.info("[DataArrayHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PutUninitializedDataArraysResponse(PutUninitializedDataArraysResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        // logger.info("[DataArrayHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }


}