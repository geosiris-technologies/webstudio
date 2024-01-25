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
package com.geosiris.webstudio.servlet.etp;

import Energistics.Etp.v12.Datatypes.Object.ActiveStatusKind;
import Energistics.Etp.v12.Datatypes.Object.ContextScopeKind;
import Energistics.Etp.v12.Datatypes.Object.DataObject;
import Energistics.Etp.v12.Datatypes.Object.Resource;
import Energistics.Etp.v12.Protocol.Discovery.GetResources;
import Energistics.Etp.v12.Protocol.Discovery.GetResourcesResponse;
import Energistics.Etp.v12.Protocol.Store.*;
import com.geosiris.energyml.utils.EPCGenericManager;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.etp.communication.Message;
import com.geosiris.etp.protocols.handlers.StoreHandler;
import com.geosiris.etp.utils.ETPDefaultProtocolBuilder;
import com.geosiris.etp.utils.ETPUri;
import com.geosiris.etp.utils.Pair;
import com.geosiris.etp.websocket.ETPClient;
import com.geosiris.webstudio.etp.StoreHandler_WebStudio;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.logs.ServerLogMessage.MessageType;
import com.geosiris.webstudio.property.ConfigurationType;
import com.geosiris.webstudio.servlet.Editor;
import com.geosiris.webstudio.servlet.editing.FileReciever;
import com.geosiris.webstudio.utils.ETPRequestUtils;
import com.geosiris.webstudio.utils.ETPUtils;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servlet implementation class ETPRequest
 */
@WebServlet("/ETPRequest")
public class ETPRequest extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static Logger logger = LogManager.getLogger(ETPRequest.class);


    /**
     * @see HttpServlet#HttpServlet()
     */
    public ETPRequest() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionUtility.tryConnectServlet(request, response)) {
            return;
        }

		PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(ETPRequestUtils.getAllBuildableMessages());
        out.flush();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionUtility.tryConnectServlet(request, response)) {
            return;
        }

        HashMap<String, List<String>> etpRequestParameterMap = SessionUtility.getParameterMap(request);

        String jsonContent = manageETPRequest(etpRequestParameterMap, request.getSession(false));

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(Objects.requireNonNullElse(jsonContent, "{}"));
        out.flush();
    }


    private String manageETPRequest(Map<String, List<String>> parameterMap, HttpSession session) {
        for (String k : parameterMap.keySet()) {
            logger.info(k + " > " + parameterMap.get(k));
        }
        try {
            String request = parameterMap.get("request").get(0);
            String dataspace = null;
            if(parameterMap.containsKey("dataspace")){
                dataspace = parameterMap.get("dataspace").get(0);
            }

            boolean ask_aknowledge = parameterMap.containsKey("ask_aknowledge");

            logger.info("Asking aknowledge : " + ask_aknowledge);

            ETPClient etpClient = (ETPClient) session.getAttribute(SessionUtility.SESSION_ETP_CLIENT_ID);

            StoreHandler_WebStudio storeHandler = (StoreHandler_WebStudio) etpClient.getEtpConnection().getProtocolHandlers().get(StoreHandler.protocol);

            boolean isConnected = etpClient != null && etpClient.isConnected();
            if (isConnected) {
                try {
                    StringBuilder req_result = new StringBuilder();
                    if (request.toLowerCase().startsWith("getresource")) {

                        GetResources getRess = ETPDefaultProtocolBuilder.buildGetResources(new ETPUri(dataspace).toString(),
                                ContextScopeKind.self, new ArrayList<>());

                        List<Message> ressResp_l = ETPUtils.sendETPRequest(session, etpClient, getRess, ask_aknowledge,
                                ETPUtils.waitingForResponseTime);

                        for(Message ressResp : ressResp_l) {
                            if (ressResp != null) {
                                GetResourcesResponse objResp = (GetResourcesResponse) ressResp.getBody();
                                if (objResp.getResources() != null) {
                                    req_result = new StringBuilder(objResp.toString());
////                                    logger.info("Found objects uri : ");
//                                    logger.info("Nb ressources found : " + objResp.getResources().size());
//                                    SessionUtility.log(session, new ServerLogMessage(MessageType.LOG,
//                                            "ETP <== Recieve ETP message " + objResp.getClass().getSimpleName() + " : "
//                                                    + objResp,
//                                            SessionUtility.EDITOR_NAME));
//
//                                    req_result = "[";
//                                    for (Resource r : objResp.getResources()) {
//                                        // logger.info("\t> " + r.getUri());
//                                        req_result += "\"" + r.getUri() + "\",";
//                                    }
//                                    if (objResp.getResources().size() > 0) {
//                                        req_result = req_result.substring(0, req_result.length() - 1);
//                                    }
//                                    req_result += "]";
                                } else {
                                    logger.info("no resources found in GetResourcesResponse " + objResp);
                                }
                            } else {
                                logger.info("null GetResourcesResponse ");
                            }
                        }
                    } else if (request.toLowerCase().startsWith("deletedataobject")) {
                        Map<CharSequence, CharSequence> mapUri = new HashMap<>();
                        for (String uri : parameterMap.get("etp_uri")) {
                            ETPUri etpuri = ETPUri.parse(uri);
                            if(!etpuri.hasDataspace()){
                                etpuri.setDataspace(dataspace);
                            }
                            mapUri.put(uri, etpuri.toString());
                        }

                        DeleteDataObjects deleteDO = ETPDefaultProtocolBuilder.buildDeleteDataObjects(mapUri);
                        List<Message> deleteDO_resp_m_l = ETPUtils.sendETPRequest(session, etpClient, deleteDO, ask_aknowledge,
                                ETPUtils.waitingForResponseTime);

                        req_result = new StringBuilder("[");
                        for(Message deleteDO_resp_m : deleteDO_resp_m_l) {
                            Object deleteDO_resp = deleteDO_resp_m.getBody();

                            if (deleteDO_resp instanceof DeleteDataObjectsResponse) {
                                DeleteDataObjectsResponse del_DO_resp = (DeleteDataObjectsResponse) deleteDO_resp;
                                List<String> filesContent = new ArrayList<>();
                                for (CharSequence del : del_DO_resp.getDeletedUris().keySet()) {
                                    req_result.append("\"").append(del).append("\",");

                                }
                                FileReciever.loadFiles_Unnamed(session, filesContent, false, true, true);
                            }
                        }
                        if (req_result.length() > 1) {
                            req_result = new StringBuilder(req_result.substring(0, req_result.length() - 1));
                        }
                        req_result.append("]");
                    } else if (request.toLowerCase().compareTo("import") == 0) {
                        Map<CharSequence, CharSequence> mapUri = new HashMap<>();
                        for (String uri : parameterMap.get("etp_uri")) {
                            ETPUri etpuri = ETPUri.parse(uri);
                            if(!etpuri.hasDataspace()){
                                etpuri.setDataspace(dataspace);
                            }
                            SessionUtility.log(session, new ServerLogMessage(MessageType.LOG,
                                        "ETP request import on " + etpuri + " == " + etpuri.hasDataspace() + " --- " + etpuri.getDataspace(),
                                        SessionUtility.EDITOR_NAME));
                            mapUri.put(mapUri.size()+"", etpuri.toString());
                        }

                        storeHandler.importableUUID.addAll(mapUri.values().stream().map(CharSequence::toString).map(ETPUri::parse).map(ETPUri::getUuid).collect(Collectors.toList()));

                        GetDataObjects getDataO = ETPDefaultProtocolBuilder.buildGetDataObjects(mapUri, "xml");
                        List<Message> dataResp_m_l = ETPUtils.sendETPRequest(session, etpClient, getDataO, ask_aknowledge, -1);
                        for(Message dataResp_m : dataResp_m_l) {
                            Object dataResp = dataResp_m.getBody();

                            if (dataResp instanceof GetDataObjectsResponse) {
                                SessionUtility.log(session, new ServerLogMessage(MessageType.LOG,
                                        "ETP <== Recieve ETP message " + dataResp.getClass().getSimpleName() + " : "
                                                + dataResp,
                                        SessionUtility.EDITOR_NAME));
                            } else {
                                logger.info("GetDataObject Response : " + dataResp);
                            }
                        }
                    } else if (request.toLowerCase().compareTo("putdataobjects") == 0) {
                        String logs = "";

                        Map<String, Object> epcFiles = SessionUtility.getResqmlObjects(session);

                        Pair<Map<CharSequence, DataObject>, String> mapAndLog = getDataObjectMaptoURI(
                                parameterMap.get("exportToETP_UUID"), epcFiles, dataspace);

                        logs += mapAndLog.r();

                        logger.info("#manageETPRequest LOGS : " + logs);

                        PutDataObjects putDataO = ETPDefaultProtocolBuilder.buildPutDataObjects(mapAndLog.l(), false);
                        ETPUtils.sendETPRequest(session, etpClient, putDataO, ask_aknowledge, -1);

                    } else if (request.toLowerCase().compareTo("getrelated") == 0) {
                        StringBuilder logs = new StringBuilder();

                        Map<String, Object> epcFiles = SessionUtility.getResqmlObjects(session);

                        Pair<Map<CharSequence, DataObject>, String> mapAndLog = getDataObjectMaptoURI(
                                parameterMap.get("getrelated_UUID"), epcFiles, dataspace);

                        logs.append(mapAndLog.r());

                        logger.info("#manageETPRequest LOGS : " + logs);

                        String scope_str = "sources";
                        try {
                            scope_str = parameterMap.get("scope").get(0);
                        } catch (Exception ignore){}
                        ContextScopeKind scope = ContextScopeKind.valueOf(scope_str);

                        String depth_str = "1";
                        try {
                            depth_str = parameterMap.get("depth").get(0);
                        } catch (Exception ignore){}
                        int depth = 1;
                        try {
                            depth = Integer.parseInt(depth_str);
                        } catch (Exception ignore){}

                        List<Pair<String, Long>> msgIds = new ArrayList<>();
                        for (CharSequence uri : mapAndLog.l().keySet()) {
                            GetResources getResRelated = ETPDefaultProtocolBuilder.buildGetResources(uri + "", scope,
                                    new ArrayList<>(), depth);
                            msgIds.add(new Pair<>(uri + "", etpClient.send(getResRelated)));
                            SessionUtility.log(session, new ServerLogMessage(MessageType.LOG,
                                    "ETP ==> Send ETP message " + getResRelated.getClass().getSimpleName() + " : "
                                            + getResRelated,
                                    SessionUtility.EDITOR_NAME));
                        }

                        logger.info("... GetResources related to : Waiting for answers");

                        req_result = new StringBuilder("[");
                        int cptAnswer = 0;
                        for (Pair<String, Long> msgid : msgIds) {
                            logger.info("waiting for message (" + msgid.r() + ")");
                            try {
                                GetResourcesResponse getResResp = (GetResourcesResponse) etpClient.getEtpClientSession()
                                        .waitForResponse(msgid.r(), ETPUtils.waitingForResponseTime).get(0).getBody();
                                SessionUtility.log(session, new ServerLogMessage(MessageType.LOG,
                                        "ETP <== Recieve ETP message " + getResResp.getClass().getSimpleName() + " : "
                                                + getResResp,
                                        SessionUtility.EDITOR_NAME));

                                req_result.append("{ \"uri\": \"").append(msgid.l()).append("\",");
                                req_result.append("\"related\" : [");
                                for (Resource r : getResResp.getResources()) {
                                    // logger.info("\t> " + r.getUri());
                                    req_result.append("\"").append(r.getUri()).append("\",");
                                }
                                if (getResResp.getResources().size() > 0) {
                                    req_result = new StringBuilder(req_result.substring(0, req_result.length() - 1));
                                }
                                req_result.append("]},");
                                cptAnswer++;
                            } catch (Exception e) {
                                logs.append(e.getMessage());
                                logger.error(e.getMessage(), e);
                            }
                        }
                        if (cptAnswer > 0) {
                            req_result = new StringBuilder(req_result.substring(0, req_result.length() - 1));
                        }
                        req_result.append("]");
                        logger.info("<== GetResources related to : answers recieved");

                    } else {
                        logger.error("ETP request not supported");
                    }

                    logger.info("Final ETP Result : ");
                    logger.info(req_result.toString());
                    return req_result.toString();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    if(SessionUtility.wsProperties.getConfigurationType() == ConfigurationType.debug) {
                        SessionUtility.log(session, new ServerLogMessage(MessageType.ERROR,
                                "ETP /!\\\\ error for request : " + e.getCause() + " " + e.getMessage(),
                                SessionUtility.EDITOR_NAME));
                    }
                }
            } else {
                SessionUtility.log(session, new ServerLogMessage(MessageType.ERROR,
                        "ETP /!\\\\ Please establish a connection before sending a request",
                        SessionUtility.EDITOR_NAME));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private Pair<Map<CharSequence, DataObject>, String> getDataObjectMaptoURI(List<String> uuidList,
                                                                              Map<String, Object> epcFiles,
                                                                              String dataspace) {
        Map<CharSequence, DataObject> mapResult = new HashMap<>();
        StringBuilder logs = new StringBuilder();

        for (String uuid : uuidList) {
            logger.info("uuid to export : " + uuid);
            if (epcFiles.containsKey(uuid)) {
                Object epc_obj = epcFiles.get(uuid);
                // ObjectTree obj_tree = ObjectTree.createTree(epc_obj);
                // String title = obj_tree.getAttribute("Citation.Title", false).getData()+"";
                // String lastUpdate = obj_tree.getAttribute("Citation.LastUpdate",
                // false).getData()+"";

                String title = ObjectController.getObjectAttributeValue(epc_obj, "citation.title") + "";
                XMLGregorianCalendar lastUpdate_cal = ((XMLGregorianCalendar) ObjectController
                        .getObjectAttributeValue(epc_obj, "citation.lastUpdate"));
                long lastUpdate = 0L;
                try {
                    lastUpdate = lastUpdate_cal.toGregorianCalendar().getTimeInMillis();
                } catch (Exception ignore){}

                String uri = new ETPUri(dataspace, EPCGenericManager.getPackageDomain_fromClassName(epc_obj.getClass().getName()),
                                        EPCGenericManager.getSchemaVersion(epc_obj, false).replace(".", ""), EPCGenericManager.getObjectTypeForFilePath(epc_obj), uuid, null ).toString();

                logger.error("lastUpdate : " + lastUpdate);
                logger.error("uri : " + uri);
                String marshalledObj = Editor.pkgManager.marshal(epc_obj);
                // Date d = new Date();
                Resource fResource = Resource.newBuilder()
                        .setActiveStatus(ActiveStatusKind.Active)
                        .setAlternateUris(new ArrayList<>())
                        .setCustomData(new HashMap<>())
                        .setLastChanged(lastUpdate)
                        .setName(title)
                        .setSourceCount(null)
                        .setTargetCount(null)
                        .setStoreCreated(0)
                        .setStoreLastWrite(lastUpdate)
                        .setUri(uri)
                        .build();
                mapResult.put(uri,
                        ETPDefaultProtocolBuilder.buildDataObjectFromResqmlXMl(marshalledObj, uuid, fResource));
            } else {
                logs.append("ETP send request > error for file with uuid '").append(uuid).append("' ==> file not found\n");
            }
        }
        return new Pair<>(mapResult, logs.toString());
    }
}
