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
import com.geosiris.webstudio.servlet.editing.FileReciever;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


public class StoreHandler_WebStudio extends StoreHandler{
    public static Logger logger = LogManager.getLogger(StoreHandler_WebStudio.class);
    private HttpSession session;
    public final BlockingQueue<String> importableUUID = new LinkedBlockingDeque<>();


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
        List<String> filesToImport = new ArrayList<>();
        for (CharSequence dr : msg.getDataObjects().keySet()) {
            if(canBeImported(msg.getDataObjects().get(dr).getResource().getUri().toString(), true)) {
                filesToImport.add(new String(msg.getDataObjects().get(dr).getData().array(), StandardCharsets.UTF_8));
            }
        }
        if(filesToImport.size() > 0) {
            String res = FileReciever.loadFiles_Unnamed(session, filesToImport, false, true, true);

            SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.LOG, res, SessionUtility.EDITOR_NAME));
            SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.TOAST, "ETP : " + filesToImport.size()
                    + " imported objects. If they not appear in the table view, refresh the page. ", SessionUtility.EDITOR_NAME));
            SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.ACTION, "reload", SessionUtility.EDITOR_NAME));
        }
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

    private boolean canBeImported(String inputUri, boolean removeIfFound){
        boolean found = false;
        logger.info("[StoreHandler_WebStudio] searching " + inputUri);
        List<String> urisToImport = new ArrayList<>(importableUUID);
        for(String uriToImport : urisToImport){
            logger.info("\t[StoreHandler_WebStudio] <==" + uriToImport);
            if(inputUri.contains(uriToImport)){
                found = true;
                if(removeIfFound){
                    importableUUID.remove(inputUri);
                }
            }
        }
        return found;
    }

}