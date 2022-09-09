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
package com.geosiris.webstudio.utils;

import Energistics.Etp.v12.Datatypes.ServerCapabilities;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.etp.communication.ClientInfo;
import com.geosiris.etp.communication.ConnectionType;
import com.geosiris.etp.communication.ETPConnection;
import com.geosiris.etp.communication.Message;
import com.geosiris.etp.protocols.CommunicationProtocol;
import com.geosiris.etp.protocols.ProtocolHandler;
import com.geosiris.etp.protocols.handlers.DataspaceHandler;
import com.geosiris.etp.protocols.handlers.DiscoveryHandler;
import com.geosiris.etp.protocols.handlers.StoreHandler;
import com.geosiris.etp.utils.ETPUri;
import com.geosiris.etp.websocket.ETPClient;
import com.geosiris.webstudio.etp.CoreHandler_WebStudio;
import com.geosiris.webstudio.etp.DataspaceHandler_WebStudio;
import com.geosiris.webstudio.etp.DiscoveryHandler_WebStudio;
import com.geosiris.webstudio.etp.StoreHandler_WebStudio;
import com.geosiris.webstudio.logs.ServerLogMessage;
import jakarta.servlet.http.HttpSession;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpURI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ETPUtils {
    public static Logger logger = LogManager.getLogger(ETPUtils.class);
    public static final int waitingForResponseTime = 20000;

    public static List<Message> sendETPRequest(HttpSession session, ETPClient etpClient, SpecificRecordBase msg,
                                               boolean ask_acknowledge, int timeout) {
        long msgId = etpClient.send(msg);
        logger.info("Sending message with id : " + msgId);
        SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.LOG,
                "ETP ==> Send ETP message " + msg.getClass().getSimpleName() + " : " + msg,
                SessionUtility.EDITOR_NAME));
        if (ask_acknowledge) {
            Object ack = etpClient.getEtpClientSession().waitForAknowledge(msgId, waitingForResponseTime);
            if (ack != null) {
                SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.TOAST,
                        "ETP <== Recieved acknoledge for message '" + msg.getClass().getSimpleName() + "'",
                        SessionUtility.EDITOR_NAME));
            }
        }

        logger.info(msgId + ") Waiting for " + msg.getClass().getSimpleName() + " answer");
        Object dataResp = null;
        if (timeout <= 0) {
            dataResp = etpClient.getEtpClientSession().waitForResponse(msgId, 10000000);
        } else {
            dataResp = etpClient.getEtpClientSession().waitForResponse(msgId, timeout);
        }
        if(dataResp != null) {
            logger.info(msgId + ") Answer : " + dataResp);
            SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.LOG,
                    "ETP <== Recieve ETP message " + dataResp.getClass().getSimpleName() + " : " + dataResp,
                    SessionUtility.EDITOR_NAME));
        }else{
            SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.LOG,
                    "No response received for message " + msgId,
                    SessionUtility.EDITOR_NAME));
        }
        return (List<Message>) dataResp;
    }

    public static Boolean establishConnexion(HttpSession session, HttpURI host, String userName, String password,
                                             Boolean askConnection) {
        if (askConnection) {
            ETPClient etpClient = establishConnexionForClient(session, host, userName, password);
            if (etpClient != null) {
                session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_ID, etpClient);
                session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_URL, host);
                session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_USERNAME, userName);
                session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_PASSWORD, password);
                SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.LOG,
                        "ETP ==> Connexion established with '" + host,
                        SessionUtility.EDITOR_NAME));
                return true;
            } else {
                SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.ERROR,
                        "ETP ==> Connexion NOT established with '" + host,
                        SessionUtility.EDITOR_NAME));
                SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.TOAST,
                        "ETP ==> Connexion NOT established with '" + host,
                        SessionUtility.EDITOR_NAME));
            }
        } else {
            ETPClient etpClient = (ETPClient) session.getAttribute(SessionUtility.SESSION_ETP_CLIENT_ID);
            etpClient.closeClient();
            session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_ID, null);
        }
                session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_URL, "");
                session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_USERNAME, "");
                session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_PASSWORD, "");
        return false;
    }


    public static ETPClient establishConnexionForClient(HttpSession session, HttpURI host, String userName, String password, boolean useDefaultHandler) {
        ClientInfo clientInfo = new ClientInfo(host, 4000, 4000);
        Map<CommunicationProtocol, ProtocolHandler> protocolHandlers = new HashMap<>();
        if(useDefaultHandler){
            protocolHandlers.put(CoreHandler_WebStudio.protocol, new CoreHandler_WebStudio());
            protocolHandlers.put(StoreHandler.protocol, new StoreHandler());
            protocolHandlers.put(DataspaceHandler.protocol, new DataspaceHandler());
            protocolHandlers.put(DiscoveryHandler.protocol, new DiscoveryHandler());

        }else{
            protocolHandlers.put(CoreHandler_WebStudio.protocol, new CoreHandler_WebStudio());
            protocolHandlers.put(StoreHandler_WebStudio.protocol, new StoreHandler_WebStudio(session));
            protocolHandlers.put(DataspaceHandler_WebStudio.protocol, new DataspaceHandler_WebStudio());
            protocolHandlers.put(DiscoveryHandler_WebStudio.protocol, new DiscoveryHandler_WebStudio());
        }
        ETPConnection etpConnection = new ETPConnection(ConnectionType.CLIENT, new ServerCapabilities(), clientInfo, protocolHandlers);

        return ETPClient.getInstanceWithAuth_Basic(host, etpConnection, 5000, userName, password);
    }

    public static ETPClient establishConnexionForClient(HttpSession session, HttpURI host, String userName, String password) {
        return establishConnexionForClient(session, host, userName, password, false);
    }

    private static Pattern pat_contentType = Pattern.compile("application/x-(?<domain>[\\w]+)\\+xml;version=(?<domainVersion>[\\d\\.]+);type=(?<type>[\\w\\d_]+)");
    private static Pattern pat_qualifiedType = Pattern.compile("(?<domain>[a-zA-Z]+)(?<domainVersion>[\\d]+)\\.(?<type>[\\w_]+)");

    public static ETPUri getUriFromDOR(Object dor, String dataspace){
        String cq_type = "";
        try{
            cq_type = (String) ObjectController.getObjectAttributeValue(dor, "ContentType");
        }catch (Exception ignore){}
        if (cq_type == null){
            try {
                cq_type = (String) ObjectController.getObjectAttributeValue(dor, "QualifiedType");
            } catch (Exception ignore){}
        }
        Matcher m = pat_contentType.matcher(cq_type);

        if(m.find()){
            // found
        }else{
            m = pat_qualifiedType.matcher(cq_type);
            if(m.find()){
                // found
            }
        }
        return new ETPUri(dataspace,
                m.group("domain"),
                m.group("domainVersion").replaceAll("\\.", ""),
                m.group("type"),
                (String) ObjectController.getObjectAttributeValue(dor, "uuid"),
                "");
    }
}
