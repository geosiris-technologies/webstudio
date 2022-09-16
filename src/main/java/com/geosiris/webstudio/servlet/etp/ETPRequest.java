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
import com.geosiris.etp.utils.ETPDefaultProtocolBuilder;
import com.geosiris.etp.utils.ETPUri;
import com.geosiris.etp.utils.Pair;
import com.geosiris.etp.websocket.ETPClient;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.logs.ServerLogMessage.MessageType;
import com.geosiris.webstudio.property.ConfigurationType;
import com.geosiris.webstudio.servlet.Editor;
import com.geosiris.webstudio.servlet.FileReciever;
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
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

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

        HashMap<String, List<String>> etpRequestParameterMap = new HashMap<>();

        if (ServletFileUpload.isMultipartContent(request)) {
            DiskFileItemFactory factory = new DiskFileItemFactory();

            ServletFileUpload upload = new ServletFileUpload(factory);

            try {
                FileItemIterator iterator = upload.getItemIterator(request);

                while (iterator.hasNext()) {
                    FileItemStream item = iterator.next();
                    InputStream stream = item.openStream();

                    if (item.isFormField()) {
                        String value = Streams.asString(stream);
                        logger.info("$> " + item.getFieldName() + " => " + value);
                        if (!etpRequestParameterMap.containsKey(item.getFieldName()))
                            etpRequestParameterMap.put(item.getFieldName(), new ArrayList<>());
                        etpRequestParameterMap.get(item.getFieldName()).add(value);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            for (String k : request.getParameterMap().keySet()) {
                etpRequestParameterMap.put(k, Arrays.asList(request.getParameterMap().get(k)));
            }
        }
        String jsonContent = manageETPRequest(etpRequestParameterMap, request.getSession(false));

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(jsonContent);
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

            boolean ask_aknowledge = false;
            if (parameterMap.containsKey("ask_aknowledge")) {
                // logger.info("AKNOWLEDGE : " +
                // parameterMap.get("ask_aknowledge").get(0));
                ask_aknowledge = true;
            }
            logger.info("Asking aknowledge : " + ask_aknowledge);

            ETPClient etpClient = (ETPClient) session.getAttribute(SessionUtility.SESSION_ETP_CLIENT_ID);
            Boolean isConnected = etpClient != null && etpClient.isConnected();
            if (isConnected) {
                try {
                    String req_result = "";
                    if (request.toLowerCase().startsWith("getresource")) {

                        GetResources getRess = ETPDefaultProtocolBuilder.buildGetResources(new ETPUri(dataspace).toString(),
                                ContextScopeKind.self, new ArrayList<>());

                        List<Message> ressResp_l = ETPUtils.sendETPRequest(session, etpClient, getRess, ask_aknowledge,
                                ETPUtils.waitingForResponseTime);

                        for(Message ressResp : ressResp_l) {
                            if (ressResp != null) {
                                GetResourcesResponse objResp = (GetResourcesResponse) ressResp.getBody();
                                if (objResp.getResources() != null) {
                                    req_result = objResp.toString();
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

                        req_result = "[";
                        for(Message deleteDO_resp_m : deleteDO_resp_m_l) {
                            Object deleteDO_resp = deleteDO_resp_m.getBody();

                            if (deleteDO_resp instanceof DeleteDataObjectsResponse) {
                                DeleteDataObjectsResponse del_DO_resp = (DeleteDataObjectsResponse) deleteDO_resp;
                                List<String> filesContent = new ArrayList<String>();
                                for (CharSequence del : del_DO_resp.getDeletedUris().keySet()) {
                                    req_result += "\"" + del + "\",";

                                }
                                FileReciever.loadFiles_Unnamed(session, filesContent, false, true, true);
                            }
                        }
                        if (req_result.length() > 1) {
                            req_result = req_result.substring(0, req_result.length() - 1);
                        }
                        req_result += "]";
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
                        String logs = "";

                        Map<String, Object> epcFiles = SessionUtility.getResqmlObjects(session);

                        Pair<Map<CharSequence, DataObject>, String> mapAndLog = getDataObjectMaptoURI(
                                parameterMap.get("getrelated_UUID"), epcFiles, dataspace);

                        logs += mapAndLog.r();

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
                            msgIds.add(new Pair<String, Long>(uri + "", etpClient.send(getResRelated)));
                            SessionUtility.log(session, new ServerLogMessage(MessageType.LOG,
                                    "ETP ==> Send ETP message " + getResRelated.getClass().getSimpleName() + " : "
                                            + getResRelated,
                                    SessionUtility.EDITOR_NAME));
                        }

                        logger.info("... GetResources related to : Waiting for answers");

                        req_result = "[";
                        int cptAnswer = 0;
                        for (Pair<String, Long> msgid : msgIds) {
                            try {
                                GetResourcesResponse getResResp = (GetResourcesResponse) etpClient.getEtpClientSession()
                                        .waitForResponse(msgid.r(), ETPUtils.waitingForResponseTime);
                                SessionUtility.log(session, new ServerLogMessage(MessageType.LOG,
                                        "ETP <== Recieve ETP message " + getResResp.getClass().getSimpleName() + " : "
                                                + getResResp,
                                        SessionUtility.EDITOR_NAME));

                                req_result += "{ \"uri\": \"" + msgid.l() + "\",";
                                req_result += "\"related\" : [";
                                for (Resource r : getResResp.getResources()) {
                                    // logger.info("\t> " + r.getUri());
                                    req_result += "\"" + r.getUri() + "\",";
                                }
                                if (getResResp.getResources().size() > 0) {
                                    req_result = req_result.substring(0, req_result.length() - 1);
                                }
                                req_result += "]},";
                                cptAnswer++;
                            } catch (Exception e) {
                                logs += e.getMessage();
                            }
                        }
                        if (cptAnswer > 0) {
                            req_result = req_result.substring(0, req_result.length() - 1);
                        }
                        req_result += "]";
                        logger.info("<== GetResources related to : answers recieved");

                    } else {
                        logger.error("ETP request not supported");
                    }

                    logger.info("Final ETP Result : ");
                    logger.info(req_result);
                    return req_result;
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
        String logs = "";

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
                Long lastUpdate = 0L;
                try {
                    lastUpdate = lastUpdate_cal.toGregorianCalendar().getTimeInMillis();
                } catch (Exception ignore){}

                String dataObjectType = EPCGenericManager.getPackageIdentifier_withVersionForETP(epc_obj, 2, 2) + "." + EPCGenericManager.getObjectTypeForFilePath(epc_obj);
                String uri = new ETPUri(dataspace, EPCGenericManager.getPackageIdentifierFromClassName(epc_obj.getClass().getName()),
                                        EPCGenericManager.getSchemaVersion(epc_obj).replace(".", ""), dataObjectType, uuid, null ).toString();

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
                logs += "ETP send request > error for file with uuid '" + uuid + "' ==> file not found\n";
            }
        }
        return new Pair<>(mapResult, logs);
    }
}
