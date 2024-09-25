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
import Energistics.Etp.v12.Protocol.Dataspace.*;
import com.geosiris.etp.communication.ClientInfo;
import com.geosiris.etp.communication.Message;
import com.geosiris.etp.protocols.handlers.DataspaceHandler;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;



public class DataspaceHandler_WebStudio extends DataspaceHandler{
    public static Logger logger = LogManager.getLogger(DataspaceHandler_WebStudio.class);

    private HttpSession session;

    public DataspaceHandler_WebStudio(HttpSession session){
        this.session = session;
    }

    @Override
    public Collection<Message> on_DeleteDataspaces(DeleteDataspaces msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[DataspaceHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_DeleteDataspacesResponse(DeleteDataspacesResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[DataspaceHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetDataspaces(GetDataspaces msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[DataspaceHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetDataspacesResponse(GetDataspacesResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[DataspaceHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PutDataspaces(PutDataspaces msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[DataspaceHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PutDataspacesResponse(PutDataspacesResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[DataspaceHandler_WebStudio] received message" + msg);

        SessionUtility.logToast(session, "Dataspace created");
        SessionUtility.logAction(session, "updatedataspace");
        return new ArrayList<>();
    }


}