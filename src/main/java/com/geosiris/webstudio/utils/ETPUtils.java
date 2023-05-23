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

import Energistics.Etp.v12.Datatypes.Object.ContextScopeKind;
import Energistics.Etp.v12.Datatypes.Object.Resource;
import Energistics.Etp.v12.Datatypes.ServerCapabilities;
import Energistics.Etp.v12.Protocol.Discovery.GetResources;
import Energistics.Etp.v12.Protocol.Discovery.GetResourcesResponse;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.etp.communication.ClientInfo;
import com.geosiris.etp.communication.ConnectionType;
import com.geosiris.etp.communication.ETPConnection;
import com.geosiris.etp.communication.Message;
import com.geosiris.etp.protocols.CommunicationProtocol;
import com.geosiris.etp.protocols.ProtocolHandler;
import com.geosiris.etp.protocols.handlers.DataArrayHandler;
import com.geosiris.etp.protocols.handlers.DataspaceHandler;
import com.geosiris.etp.protocols.handlers.DiscoveryHandler;
import com.geosiris.etp.protocols.handlers.StoreHandler;
import com.geosiris.etp.utils.ETPDefaultProtocolBuilder;
import com.geosiris.etp.utils.ETPHelper;
import com.geosiris.etp.utils.ETPUri;
import com.geosiris.etp.websocket.ETPClient;
import com.geosiris.webstudio.etp.*;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.model.ETP3DObject;
import com.geosiris.webstudio.servlet.Editor;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.bind.JAXBException;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpURI;

import java.util.*;
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
        List<Message> dataResp;
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
        return dataResp;
    }

    public static Boolean establishConnexion(HttpSession session, HttpURI host, String userName, String password,
                                             Boolean askConnection) {

        SessionUtility.log(session, new ServerLogMessage( ServerLogMessage.MessageType.TOAST,
                "(ETP) trying to establish connexion with '" + host + "'",
                SessionUtility.EDITOR_NAME));
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

    public static List<Resource> getResources(HttpSession session, String dataspace){
        List<Resource> result = new ArrayList<>();
        ETPClient etpClient = (ETPClient) session.getAttribute(SessionUtility.SESSION_ETP_CLIENT_ID);

        boolean isConnected = etpClient != null && etpClient.isConnected();
        if (isConnected) {
            GetResources getRess = ETPDefaultProtocolBuilder.buildGetResources(new ETPUri(dataspace).toString(),
                    ContextScopeKind.self, new ArrayList<>());

            List<Message> ressResp_l = ETPUtils.sendETPRequest(session, etpClient, getRess, false,
                    ETPUtils.waitingForResponseTime);

            for(Message ressResp : ressResp_l) {
                if (ressResp != null) {
                    GetResourcesResponse objResp = (GetResourcesResponse) ressResp.getBody();
                    if (objResp.getResources() != null) {
                        result.addAll(objResp.getResources());
                    }
                }
            }
        }
        return result;
    }


    private static ETPClient establishConnexionForClient(HttpSession session, HttpURI host, String userName, String password, boolean useDefaultHandler) {
        ClientInfo clientInfo = new ClientInfo(host, 4000, 4000);
        Map<CommunicationProtocol, ProtocolHandler> protocolHandlers = new HashMap<>();
        if(useDefaultHandler){
            protocolHandlers.put(CoreHandler_WebStudio.protocol, new CoreHandler_WebStudio());
            protocolHandlers.put(StoreHandler.protocol, new StoreHandler());
            protocolHandlers.put(DataspaceHandler.protocol, new DataspaceHandler());
            protocolHandlers.put(DiscoveryHandler.protocol, new DiscoveryHandler());
            protocolHandlers.put(DataArrayHandler.protocol, new DataArrayHandler_WebStudio());

        }else{
            protocolHandlers.put(CoreHandler_WebStudio.protocol, new CoreHandler_WebStudio());
            protocolHandlers.put(StoreHandler_WebStudio.protocol, new StoreHandler_WebStudio(session));
            protocolHandlers.put(DataspaceHandler_WebStudio.protocol, new DataspaceHandler_WebStudio());
            protocolHandlers.put(DiscoveryHandler_WebStudio.protocol, new DiscoveryHandler_WebStudio());
            protocolHandlers.put(DataArrayHandler.protocol, new DataArrayHandler_WebStudio());
        }
        ETPConnection etpConnection = new ETPConnection(ConnectionType.CLIENT, new ServerCapabilities(), clientInfo, protocolHandlers);

        return ETPClient.getInstanceWithAuth_Basic(host, etpConnection, 5000, userName, password);
    }

    private static ETPClient establishConnexionForClient(HttpSession session, HttpURI host, String userName, String password) {
        return establishConnexionForClient(session, host, userName, password, false);
    }

    public static ETP3DObject get3DFileFromETP(ETPClient etpClient, String uri, Boolean normalizePosition) throws JAXBException {
        ETP3DObject obj3D = null;

        String fileContent = ETPHelper.sendGetDataObjects_pretty(etpClient, Arrays.asList(new String[]{uri}), "xml", 50000).get(0);
        Object resqmlObj = Editor.pkgManager.unmarshal(fileContent).getValue();

        String title = (String) ObjectController.getObjectAttributeValue(resqmlObj, "citation.Title");
        String uuid = (String) ObjectController.getObjectAttributeValue(resqmlObj, "uuid");

        System.out.println("Title : " + ObjectController.getObjectAttributeValue(resqmlObj, "citation.Title") + "\n" + resqmlObj);

        long offsetPoints = 0;

        StringBuilder off_points = new StringBuilder();
        StringBuilder off_triangles = new StringBuilder();

        long nbPoints = 0;
        long nbfaces = 0;

        if(resqmlObj.getClass().getSimpleName().compareToIgnoreCase("TriangulatedSetRepresentation") == 0) {
            List<Object> trianglePatch = (List<Object>) ObjectController.getObjectAttributeValue(resqmlObj, "trianglePatch");
            System.out.println("patchs : " + trianglePatch.size() + "\n\t" + (trianglePatch.size() > 0 ? trianglePatch.get(0) : "NONE"));
            for (Object patch : trianglePatch) {
                List<Object> pointsExtArray = ObjectController.findSubObjects(ObjectController.getObjectAttributeValue(patch, "geometry"), "ExternalDataArrayPart", true);
                List<Object> trianglesExtArray = ObjectController.findSubObjects(ObjectController.getObjectAttributeValue(patch, "triangles"), "ExternalDataArrayPart", true);

                assert pointsExtArray.size() > 0;
                assert pointsExtArray.size() == trianglesExtArray.size(); // Should have as many pointExtArray as triangleExtArray

                for (int patchPart_idx = 0; patchPart_idx < pointsExtArray.size(); patchPart_idx++) {
                    String pathInExternalFile_point = (String) ObjectController.getObjectAttributeValue(pointsExtArray.get(patchPart_idx), "PathInExternalFile");
                    if(pathInExternalFile_point == null){
                        pathInExternalFile_point = (String) ObjectController.getObjectAttributeValue(trianglesExtArray.get(patchPart_idx), "PathInHdfFile");
                    }
                    String pathInExternalFile_triangles = (String) ObjectController.getObjectAttributeValue(trianglesExtArray.get(patchPart_idx), "PathInExternalFile");
                    if(pathInExternalFile_triangles == null){
                        pathInExternalFile_triangles = (String) ObjectController.getObjectAttributeValue(trianglesExtArray.get(patchPart_idx), "PathInHdfFile");
                    }

                    List<Number> allpoints = ETPHelper.sendGetDataArray_prettier(etpClient, uri, pathInExternalFile_point, 5000, true);
                    List<Number> alltriangles = ETPHelper.sendGetDataArray_prettier(etpClient, uri, pathInExternalFile_triangles, 5000, true);

                    for (int p_idx = 0; p_idx < allpoints.size() - 2; p_idx += 3) {
                        off_points.append(allpoints.get(p_idx).floatValue()).append(" ");
                        off_points.append(allpoints.get(p_idx + 1).floatValue()).append(" ");
                        off_points.append(allpoints.get(p_idx + 2).floatValue()).append("\n");
                        nbPoints++;
                    }
                    for (int f_idx = 0; f_idx < alltriangles.size() - 2; f_idx += 3) {
                        off_triangles.append("3 ");
                        off_triangles.append(offsetPoints + alltriangles.get(f_idx).longValue()).append(" ");
                        off_triangles.append(offsetPoints + alltriangles.get(f_idx + 1).longValue()).append(" ");
                        off_triangles.append(offsetPoints + alltriangles.get(f_idx + 2).longValue()).append("\n");
                        nbfaces++;
                    }

                    offsetPoints += allpoints.size() / 3;
                }
            }

            StringBuilder result = new StringBuilder();
            result.append("OFF\n\n");
            result.append(nbPoints).append(" ").append(nbfaces).append(" ").append("0\n");
            result.append(off_points).append("\n");
            result.append(off_triangles);

            obj3D = new ETP3DObject(result.toString(), "off", resqmlObj.getClass().getSimpleName(), uuid, title, null, null, null);
        } else if(resqmlObj.getClass().getSimpleName().compareToIgnoreCase("PolylineSetRepresentation") == 0)  {
            StringBuilder result = new StringBuilder();

            List<Object> patchs = (List<Object>) ObjectController.getObjectAttributeValue(resqmlObj, "linePatch");
            for (Object patch : patchs) {
                List<Object> pointsExtArray = ObjectController.findSubObjects(ObjectController.getObjectAttributeValue(patch, "geometry"), "ExternalDataArrayPart", true);
                List<Object> nodeCountExtArray = ObjectController.findSubObjects(ObjectController.getObjectAttributeValue(patch, "NodeCountPerPolyline"), "ExternalDataArrayPart", true);
                assert pointsExtArray.size() > 0;

                List<List<Number>> pointPerElt = new ArrayList<>();
                for (Object patchPart : pointsExtArray) {
                    String pathInExternalFile_point = (String) ObjectController.getObjectAttributeValue(patchPart, "PathInExternalFile");
                    if(pathInExternalFile_point == null){
                        pathInExternalFile_point = (String) ObjectController.getObjectAttributeValue(patchPart, "PathInHdfFile");
                    }
                    pointPerElt.add(ETPHelper.sendGetDataArray_prettier(etpClient, uri, pathInExternalFile_point, 5000, true));
//                    for(Number n: pointPerElt.get(pointPerElt.size()-1)){
//                        logger.debug(n);
//                    }
                    logger.debug("pointPerElt size" + pointPerElt.get(0).size());
                }

                for (int nc_i=0; nc_i<nodeCountExtArray.size(); nc_i++) {
                    Object nodeCountExt = nodeCountExtArray.get(nc_i);
                    String pathInExternalFile_point = (String) ObjectController.getObjectAttributeValue(nodeCountExt, "PathInExternalFile");
                    if(pathInExternalFile_point == null){
                        pathInExternalFile_point = (String) ObjectController.getObjectAttributeValue(nodeCountExt, "PathInHdfFile");
                    }
                    List<Number> nodeCounts = ETPHelper.sendGetDataArray_prettier(etpClient, uri, pathInExternalFile_point, 5000, true);
                    for(Number n: nodeCounts){
                        logger.debug(n);
                    }
                    int idx = 0;
                    for(Number size: nodeCounts){
                        try {
                            for (int i=0; i < size.intValue(); i++) {
                                result.append(pointPerElt.get(nc_i).get(idx * 3).floatValue()).append(" ");
                                result.append(pointPerElt.get(nc_i).get(idx * 3 + 1).floatValue()).append(" ");
                                result.append(pointPerElt.get(nc_i).get(idx * 3 + 2).floatValue()).append(" ");
                                idx++;
                            }
                            result.append("\n");
                        }catch (Exception e){logger.error(e);}
                    }
                }
            }

            obj3D = new ETP3DObject(result.toString(), "polyline", resqmlObj.getClass().getSimpleName(), uuid, title, null, null, null);
        } else if(resqmlObj.getClass().getSimpleName().compareToIgnoreCase("PointSetRepresentation") == 0)  {
            List<Object> patchs = (List<Object>) ObjectController.getObjectAttributeValue(resqmlObj, "NodePatchGeometry");
            for (Object patch : patchs) {
                List<Object> pointsExtArray = ObjectController.findSubObjects(ObjectController.getObjectAttributeValue(patch, "Points"), "ExternalDataArrayPart", true);
                assert pointsExtArray.size() > 0;
                for (Object patchPart : pointsExtArray) {
                    String pathInExternalFile_point = (String) ObjectController.getObjectAttributeValue(patchPart, "PathInExternalFile");
                    if(pathInExternalFile_point == null){
                        pathInExternalFile_point = (String) ObjectController.getObjectAttributeValue(patchPart, "PathInHdfFile");
                    }
                    List<Number> allpoints = ETPHelper.sendGetDataArray_prettier(etpClient, uri, pathInExternalFile_point, 5000, true);
                    for (int p_idx = 0; p_idx < allpoints.size() - 2; p_idx += 3) {
                        off_points.append(allpoints.get(p_idx).floatValue()).append(" ");
                        off_points.append(allpoints.get(p_idx + 1).floatValue()).append(" ");
                        off_points.append(allpoints.get(p_idx + 2).floatValue()).append("\n");
                        nbPoints++;
                    }
                }
            }

            StringBuilder result = new StringBuilder();
            result.append("OFF\n\n");
            result.append(nbPoints).append(" ").append(nbfaces).append(" ").append("0\n");
            result.append(off_points).append("\n");
            result.append(off_triangles);

            obj3D = new ETP3DObject(result.toString(), "off", resqmlObj.getClass().getSimpleName(), uuid, title, null, null, null);
        }
        return obj3D;
    }

    private static final Pattern pat_contentType = Pattern.compile("application/x-(?<domain>[\\w]+)\\+xml;version=(?<domainVersion>[\\d\\.]+);type=(?<type>[\\w\\d_]+)");
    private static final Pattern pat_qualifiedType = Pattern.compile("(?<domain>[a-zA-Z]+)(?<domainVersion>[\\d]+)\\.(?<type>[\\w_]+)");

    public static ETPUri getUriFromDOR(Object dor, String dataspace){
        String cq_type = "";
        try{
            cq_type = (String) ObjectController.getObjectAttributeValue(dor, "ContentType");
        }catch (Exception ignore) {}
        if (cq_type == null){
            try {
                cq_type = (String) ObjectController.getObjectAttributeValue(dor, "QualifiedType");
            } catch (Exception ignore2) {}
        }
        Matcher m = pat_contentType.matcher(cq_type);

        String domain = null;
        String domainVersion = null;
        String type = null;

        if(m.find()){
            // found
            domain = m.group("domain");
            domainVersion = m.group("domainVersion").replaceAll("\\.", "");
            type = m.group("type");
        }else{
            m = pat_qualifiedType.matcher(cq_type);
            if(m.find()){
                // found
                domain = m.group("domain");
                domainVersion = m.group("domainVersion").replaceAll("\\.", "");
                type = m.group("type");
            }
        }
        return new ETPUri(dataspace, domain, domainVersion, type,
                (String) ObjectController.getObjectAttributeValue(dor, "uuid"),
                "");
    }

    public static Object createDORFromUri(HttpSession session, String dorClassName, ETPUri uri){
        try {
            ETPClient etpClient = (ETPClient) session.getAttribute(SessionUtility.SESSION_ETP_CLIENT_ID);
            String fileContent = ETPHelper.sendGetDataObjects_pretty(etpClient, Arrays.asList(new String[]{uri.toString()}), "xml", 50000).get(0);
            Object resqmlObj = Editor.pkgManager.unmarshal(fileContent).getValue();
            Map<String, Object> objList = new HashMap<>();
            objList.put((String) ObjectController.getObjectAttributeValue(resqmlObj, "uuid"), resqmlObj);

            Object dor = Editor.pkgManager.createInstance(dorClassName, objList, uri.getUuid());
            ObjectController.editObjectAttribute(dor, "EnergisticsUri", uri.toString());
            return dor;
        }catch (Exception e){
            logger.error(e);
        }
        return null;
    }
}
