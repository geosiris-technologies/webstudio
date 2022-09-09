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
import Energistics.Etp.v12.Protocol.Store.*;
import com.geosiris.etp.communication.ClientInfo;
import com.geosiris.etp.communication.Message;
import com.geosiris.etp.protocols.handlers.StoreHandler;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.servlet.FileReciever;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class StoreHandler_WebStudio extends StoreHandler{
    public static Logger logger = LogManager.getLogger(StoreHandler_WebStudio.class);
    private HttpSession session;


    public StoreHandler_WebStudio(HttpSession session){
        this.session = session;
    }

    @Override
    public Collection<Message> on_Chunk(Chunk msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[StoreHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_DeleteDataObjects(DeleteDataObjects msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[StoreHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_DeleteDataObjectsResponse(DeleteDataObjectsResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[StoreHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetDataObjects(GetDataObjects msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[StoreHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_GetDataObjectsResponse(GetDataObjectsResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[StoreHandler_WebStudio] received message" + msg);
        List<String> filesContent = new ArrayList<>();
        for (CharSequence dr : msg.getDataObjects().keySet()) {
//            logger.info("\t => " + dr);
            try {
                filesContent.add(new String(msg.getDataObjects().get(dr).getData().array(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }

        }
        String res = FileReciever.loadFiles_Unnamed(session, filesContent, false, true, true);

        SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.LOG, res, SessionUtility.EDITOR_NAME));
        SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.TOAST,"ETP : " + filesContent.size()
                + " imported objects. If they not appear in the table view, refresh the page. ",SessionUtility.EDITOR_NAME));
        SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.ACTION, "reload", SessionUtility.EDITOR_NAME));
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PutDataObjects(PutDataObjects msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[StoreHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }

    @Override
    public Collection<Message> on_PutDataObjectsResponse(PutDataObjectsResponse msg, MessageHeader msgHeader, ClientInfo clientInfo) {
        logger.info("[StoreHandler_WebStudio] received message" + msg);
        return new ArrayList<>();
    }


}