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

import Energistics.Etp.v12.Datatypes.DataValue;
import Energistics.Etp.v12.Datatypes.Object.*;
import Energistics.Etp.v12.Datatypes.ServerCapabilities;
import Energistics.Etp.v12.Protocol.Discovery.GetResources;
import Energistics.Etp.v12.Protocol.Discovery.GetResourcesResponse;
import Energistics.Etp.v12.Protocol.Store.PutDataObjects;
import com.geosiris.energyml.data.*;
import com.geosiris.energyml.exception.NotImplementedException;
import com.geosiris.energyml.exception.ObjectNotFoundNotError;
import com.geosiris.energyml.pkg.EPCFile;
import com.geosiris.energyml.utils.EPCGenericManager;
import com.geosiris.energyml.utils.EnergymlWorkspace;
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
import com.geosiris.webstudio.exeptions.NotValidInputException;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.model.ETP3DObject;
import com.geosiris.webstudio.servlet.Editor;
import com.google.gson.Gson;
import energyml.resqml2_0_1.ObjPointSetRepresentation;
import energyml.resqml2_2.PointSetRepresentation;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.bind.JAXBException;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpURI;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.geosiris.energyml.utils.EPCGenericManager.getObjectTypeForFilePath_fromClassName;
import static com.geosiris.energyml.utils.EnergymlWorkspaceHelper.getCrsObj;
import static com.geosiris.energyml.utils.EnergymlWorkspaceHelper.readArray;
import static com.geosiris.energyml.utils.ObjectController.searchAttributeMatchingNameWithPath;

public class ETPUtils {
    public static Logger logger = LogManager.getLogger(ETPUtils.class);
    public static final int waitingForResponseTime = 20000;


    public static HttpURI getHttpUriETP(String serverUrl){
        if(serverUrl.toLowerCase(Locale.ROOT).startsWith("http:")) {
            serverUrl = "ws" + serverUrl.substring(4);
        }else if(!serverUrl.toLowerCase(Locale.ROOT).startsWith("ws") && !serverUrl.toLowerCase(Locale.ROOT).startsWith("wss") ){
            serverUrl = "ws://" + serverUrl;
        }
        return new HttpURI(serverUrl);
    }

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
                        "ETP <== Received acknowledge for message '" + msg.getClass().getSimpleName() + "'",
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
            logger.info(msgId + ") Answers : " + dataResp.size());
            for(Message m: dataResp){
                logger.info("\t" + m.getHeader() + " " + m.getBody());
            }
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

    public static ETPClient establishConnexion(
            HttpSession session,
            HttpURI host,
            String userName,
            String password,
            String token,
            String headers,
            Boolean askConnection
    ) {
        Map<String, String> parsedHeaders = new HashMap<>();
		try{
			Gson gson = new Gson();
			parsedHeaders = gson.fromJson(headers, HashMap.class);
		}catch (Exception ignore){}
        if(parsedHeaders == null){
            parsedHeaders = new HashMap<>();
        }
		for(Map.Entry<String, String> he: parsedHeaders.entrySet()){
			logger.info("Headers " + he.getKey() + " " + he.getValue());
		}
        return establishConnexion(session, host, userName, password, token, parsedHeaders, askConnection);
    }

    public static ETPClient establishConnexion(
            HttpSession session,
            HttpURI host,
            String userName,
            String password,
            String token,
            Map<String, String> headers,
            Boolean askConnection
    ) {
        if(session != null) {
            SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.TOAST,
                    "(ETP) trying to establish connexion with '" + host + "'",
                    SessionUtility.EDITOR_NAME));
        }
        if (askConnection) {
            ETPClient etpClient = establishConnexionForClient(session, host, userName, password, token, headers);
            if (etpClient != null) {
                if(session != null) {
                    session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_ID, etpClient);
                    session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_URL, host);
                    session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_USERNAME, userName);
                    session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_PASSWORD, password);
                    SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.LOG,
                            "ETP ==> Connexion established with '" + host,
                            SessionUtility.EDITOR_NAME));
                }
                return etpClient;
            } else {
                if(session != null) {
                    logger.debug("ETP ==> Connexion NOT established with '" + host);
                    SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.ERROR,
                            "ETP ==> Connexion NOT established with '" + host,
                            SessionUtility.EDITOR_NAME));
                    SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.TOAST,
                            "ETP ==> Connexion NOT established with '" + host,
                            SessionUtility.EDITOR_NAME));
                }
            }
        } else {
            ETPClient etpClient = (ETPClient) session.getAttribute(SessionUtility.SESSION_ETP_CLIENT_ID);
            etpClient.closeClient();
            if(session != null) {
                session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_ID, null);
            }
        }
        if(session != null) {
            session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_URL, "");
            session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_USERNAME, "");
            session.setAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_PASSWORD, "");
        }
        return null;
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

    public static PutDataObjects createPutDataObjects(List<Object> enerygmlObjects, String dataspace){
        Map<CharSequence, DataObject> mapResult = new HashMap<>();

        for(Object o: enerygmlObjects){
            DataObject data_o = getDataObjectFromEnergyml(o, dataspace);
            mapResult.put(data_o.getResource().getUri(), data_o);
        }
        return ETPDefaultProtocolBuilder.buildPutDataObjects(mapResult, false);
    }

    public static DataObject getDataObjectFromEnergyml(Object enerygmlObject, String dataspace){
        String title = ObjectController.getObjectAttributeValue(enerygmlObject, "citation.title") + "";
        String uuid = (String) ObjectController.getObjectAttributeValue(enerygmlObject, "uuid");
        XMLGregorianCalendar lastUpdate_cal = ((XMLGregorianCalendar) ObjectController
                .getObjectAttributeValue(enerygmlObject, "citation.lastUpdate"));
        long lastUpdate = 0L;
        try {
            lastUpdate = lastUpdate_cal.toGregorianCalendar().getTimeInMillis();
        } catch (Exception ignore){}

        String uri = new ETPUri(dataspace, EPCGenericManager.getPackageDomain_fromClassName(enerygmlObject.getClass().getName()),
                EPCGenericManager.getSchemaVersion(enerygmlObject, false).replace(".", ""), EPCGenericManager.getObjectTypeForFilePath(enerygmlObject), uuid, null ).toString();

        logger.info("lastUpdate : " + lastUpdate);
        logger.info("uri : " + uri);
        String marshalledObj = Editor.pkgManager.marshal(enerygmlObject);
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
        return ETPDefaultProtocolBuilder.buildDataObjectFromResqmlXMl(marshalledObj, uuid, fResource);
    }



    public static String getDataObjectFromUuid(HttpSession session, String dataspace, String uuid){
        String result = null;
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
                        for(Resource r: objResp.getResources()){
                            ETPUri uri = ETPUri.parse(r.getUri() + "");
                            if(!uri.hasDataspace()) {
                                uri.setDataspace(dataspace);
                            }
                            if(uri.getUuid().compareTo(uuid) == 0){
                                result = ETPHelper.sendGetDataObjects_pretty(etpClient, Arrays.asList(new String[]{uri + ""}), "xml", 50000).get(0);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }


    private static ETPClient establishConnexionForClient(
            HttpSession session,
            HttpURI host,
            String userName,
            String password,
            String token,
            Map<String, String> headers,
            boolean useDefaultHandler
    ) {
        ClientInfo clientInfo = new ClientInfo(host);
        Map<CharSequence, DataValue> mapCaps = new HashMap<>();
//        mapCaps.put("MaxWebSocketMessagePayloadSize", DataValue.newBuilder().setItem(40000).build());
//        mapCaps.put("MaxWebSocketMessagePayloadSize", DataValue.newBuilder().setItem(40000).build());
        ServerCapabilities caps = new ServerCapabilities();
        caps.setEndpointCapabilities(mapCaps);
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
        ETPConnection etpConnection = new ETPConnection(ConnectionType.CLIENT, caps, clientInfo, protocolHandlers);

        if(token != null && !token.trim().isEmpty()){
            return ETPClient.getInstanceWithAuth_Token(host, etpConnection, 5000, token, headers);
        }else {
            return ETPClient.getInstanceWithAuth_Basic(host, etpConnection, 5000, userName, password, headers);
        }
    }

    private static ETPClient establishConnexionForClient(
            HttpSession session,
            HttpURI host,
            String userName,
            String password,
            String token,
            Map<String, String> headers
    ) {
        return establishConnexionForClient(session, host, userName, password, token, headers, false);
    }

    public static Map<String, Object> getGraphical(HttpSession session, ETPUri representationUri){
        Map<String, Object> graphicalElements = new HashMap<>();

        ETPClient etpClient = (ETPClient) session.getAttribute(SessionUtility.SESSION_ETP_CLIENT_ID);
        boolean isConnected = etpClient != null && etpClient.isConnected();
        if (isConnected) {
            GetResources getResources = GetResources.newBuilder()
                    .setContext(ContextInfo.newBuilder()
                            .setDepth(1)
                            .setUri(representationUri.toString())
                            .setDataObjectTypes(new ArrayList<>())
                            .setIncludeSecondarySources(false)
                            .setIncludeSecondaryTargets(false)
                            .setNavigableEdges(RelationshipKind.Primary)
                            .build()
                    ).setScope(ContextScopeKind.sources)
                    .setStoreLastWriteFilter(null)
                    .setActiveStatusFilter(null)
                    .setCountObjects(true)
                    .setIncludeEdges(false)
                    .build();

            logger.debug("\tgraphical_info : sending " );
            List<Message> msgs = ETPUtils.sendETPRequest(session, etpClient, getResources, false, 5000);
            logger.debug("\tgraphical_info : msgs " + msgs.size());
            for(Message msg: msgs){
                if(msg.getBody() instanceof GetResourcesResponse){
                    GetResourcesResponse resp = (GetResourcesResponse) msg.getBody();
                    resp.getResources().stream().forEach(resource ->{
                        ETPUri uri = ETPUri.parse(resource.getUri() + "");
                        if(!uri.hasDataspace()){
                            uri.setDataspace(representationUri.getDataspace());
                        }
                        logger.debug("\tgraphical_info : uri " + uri + " -- " + uri.getObjectType());
                        if(uri.getObjectType().toLowerCase().contains("graphical")
                                || uri.getObjectType().toLowerCase().contains("colormap")){
                            try{
                                String fileContent = ETPHelper.sendGetDataObjects_pretty(etpClient, Arrays.asList(new String[]{uri + ""}), "xml", 50000).get(0);
                                Object graphicalObject = Editor.pkgManager.unmarshal(fileContent).getValue();
                                graphicalElements.put(ObjectController.getObjectAttributeValue(graphicalObject, "uuid") + "", graphicalObject);
                            }catch (Exception e){
                                logger.error(e);
                            }
                        }
                    });
                }else{
                    logger.debug(msg);
                }
            }
        }
        return graphicalElements;
    }

    public static Tuple3<String, String, String> get_PLF_Colors(HttpSession session, ETPUri etpUri){
        logger.debug("graphical_info start");
        Map<String, Object> graphicals = new HashMap<>();

        String defaultValue = "#000000";

        String pointColor=null, lineColor=null, faceColor=null;
        if(session != null) {
//        try{
            graphicals = getGraphical(session, etpUri);
            logger.debug("graphical size " + graphicals.size());
//        }catch (Exception e){
//            logger.error(e);
//        }

            Iterator<Object> graphicalValuesIt = graphicals.values().iterator();
            while(graphicalValuesIt.hasNext()
                    && ( pointColor == null
                    || lineColor == null
                    || faceColor == null)
            ){
                Object g = graphicalValuesIt.next();

                List<?> g_info = (List<?>) ObjectController.getObjectAttributeValue(g, "GraphicalInformation");
//            logger.debug("g_info " + g_info.size());
                for (Object info: g_info) {
                    if ( pointColor == null
                            || lineColor == null
                            || faceColor == null) {
                        String colorMapUuid = (String) ObjectController.getObjectAttributeValue(info, "ColorMap.uuid");

                        String colorMapXml = getDataObjectFromUuid(session, etpUri.getDataspace(), colorMapUuid);
//                    logger.debug("colorMapXml " + colorMapXml);
                        if (colorMapUuid != null && colorMapXml != null) {
                            Object colorMap = Editor.pkgManager.unmarshal(colorMapXml).getValue();
//                        logger.debug("colorMap " + colorMap);
                            List<Object> targets = ObjectController.findSubObjects(info, "DataObjectReference", true);
//                        logger.debug("targets " + targets.size());
                            for (Object dor : targets) {
                                if (etpUri.getUuid().compareTo((String) ObjectController.getObjectAttributeValue(dor, "uuid")) == 0) {
                                    try {
                                        int valueVectorIndex = Integer.parseInt(ObjectController.getObjectAttributeValue(info, "ValueVectorIndex") + "");
                                        Object hsvColorEntry = ObjectController.getObjectAttributeValue(colorMap, "Entry." + valueVectorIndex + ".hsv");
                                        String rgbHexColor = hsvToRgb(
                                                Float.parseFloat("" + ObjectController.getObjectAttributeValue(hsvColorEntry, "Hue")),
                                                Float.parseFloat("" + ObjectController.getObjectAttributeValue(hsvColorEntry, "Saturation")),
                                                Float.parseFloat("" + ObjectController.getObjectAttributeValue(hsvColorEntry, "Value"))
                                        );
                                        pointColor = rgbHexColor;
                                        lineColor = rgbHexColor;
                                        faceColor = rgbHexColor;
                                        break;
                                    } catch (Exception e) {
                                        logger.error(e);
                                    }
                                }
                            }
                        }
                    }else{
                        break;
                    }
                }
            }

            logger.debug("Colors found : " + pointColor + " " + lineColor + " " + faceColor);
        }
        return Tuples.of(
                pointColor == null ? (etpUri.toString().toLowerCase().contains("pointset") ? randomHexColor() : defaultValue) : pointColor,
                lineColor  == null ? (etpUri.toString().toLowerCase().contains("polyline") ? randomHexColor() : defaultValue) : lineColor,
                faceColor  == null ? randomHexColor() : faceColor);
    }

    public static String randomHexColor(){
        Random random = new Random();
//        return "#" + String.format("%02X", Math.max(20, random.nextInt(8)*4)) + String.format("%02X", Math.max(20, random.nextInt(8)*4)) + String.format("%02X", Math.max(20, random.nextInt(8)*4));
        return String.format("#%06x", random.nextInt(0xffffff + 1));
    }


    public static ETP3DObject get3DFileFromETP(ETPClient etpClient, HttpSession session, String uri, File3DType file3DType) throws JAXBException, NotValidInputException, IOException {
        uri = uri.trim();
        ETPUri etpUri = ETPUri.parse(uri);

        ETPWorkspace workspace = new ETPWorkspace(etpUri.getDataspace(), etpClient);

        Object obj = workspace.getObjectByIdentifier(uri);
        String title = (String) ObjectController.getObjectAttributeValue(obj, "citation.Title");
        String uuid = (String) ObjectController.getObjectAttributeValue(obj, "uuid");
        String epsgCode = null;
        logger.info("URI to load " + uri + " ==> " + obj);

        String objClassNameLC = obj.getClass().getName();

        List<AbstractMesh> meshes = null;
        try {
//            if(objClassNameLC.contains("polyline")){
//                meshes = readPolylineRepresentation(obj, workspace).stream().map(p -> (AbstractMesh)p).collect(Collectors.toList());
//            }else if(objClassNameLC.contains("pointset")){
//                logger.info("reading pointSet");
//                meshes = readPointRepresentation(obj, workspace).stream().map(p -> (AbstractMesh)p).collect(Collectors.toList());
//            }else {
            meshes = Mesh.readMeshObject(obj, workspace);
//            }
        } catch (Exception e){
            logger.error("Not supported resqml type or error to read  " + uri);
            e.printStackTrace();
            return null;
        }
        Tuple3<String, String, String> graphicals = get_PLF_Colors(session, etpUri);
//        logger.info("CRs : ");
//        Gson gson = new Gson();
//        for(AbstractMesh m: meshes){
//            logger.info(m.getCrsObject());
//            logger.info(isZReversed(m.getCrsObject()));
//            logger.info(gson.toJson(m.getCrsObject()));
//            if(isZReversed(m.getCrsObject())){
//                ((List<List<Double>>)m.getPointList()).forEach(l->l.set(2, -l.get(2)));
//            }
//        }

        String pointColor = graphicals.getT1();
        String lineColor = graphicals.getT2();
        String faceColor = graphicals.getT3();

        OutputStream os = new ByteArrayOutputStream();
        if(file3DType == File3DType.OBJ) {
            SurfaceMesh.exportObj(meshes, os, title, !objClassNameLC.contains("triangulatedsetrepresentation"));
        }else if (file3DType == File3DType.OFF) {
            SurfaceMesh.exportOff(meshes, os, title, !objClassNameLC.contains("triangulatedsetrepresentation"));
        }

        return new ETP3DObject(os.toString(), file3DType.toString().toLowerCase(), obj.getClass().getSimpleName(), uuid, title, pointColor, lineColor, faceColor, epsgCode);
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
            logger.info("createDORFromUri : {}\n{}", uri, objList);
            Object dor = Editor.pkgManager.createInstance(dorClassName, objList, uri.getUuid());
            ObjectController.editObjectAttribute(dor, "EnergisticsUri", uri.toString());
            return dor;
        }catch (Exception e){
            logger.error(e);
        }
        return null;
    }

    public static String hsvToRgb(float hue, float saturation, float value) {
        // Hue must be [0;360]
        if(saturation > 1) // [0;1] or [0;100]
            saturation = saturation * 0.01f;
        if(value > 1) // [0;1] or [0;100]
            value = value * 0.01f;

        float h = (hue / 60);
        float C = value * saturation;
        float X = C * (1.f - Math.abs((h % 2) - 1.f));

        float m = value - C;

        if(h<1)
            return rgbToString(C + m, X + m, 0 + m);
        else if(h<2)
            return rgbToString(X + m, C + m, 0 + m);
        else if(h<3)
            return rgbToString(0 + m, C + m, X + m);
        else if(h<4)
            return rgbToString(0 + m, X + m, C + m);
        else if(h<5)
            return rgbToString(X + m, 0 + m, C + m);
        else
            return rgbToString(C + m, 0 + m, X + m);
    }

    public static String rgbToString(float r, float g, float b) {
        String rs = Integer.toHexString((int)(r * 255));
        String gs = Integer.toHexString((int)(g * 255));
        String bs = Integer.toHexString((int)(b * 255));

        if(rs.length() < 2){
            rs = "0" + rs;
        }
        if(gs.length() < 2){
            gs = "0" + gs;
        }
        if(bs.length() < 2){
            bs = "0" + bs;
        }
        return "#" + rs + gs + bs;
    }

    // TODO: remove after energyml-utils fix
    public static List<PolylineSetMesh> readPolylineRepresentation(Object energymlObject, EnergymlWorkspace workspace) throws NotImplementedException, InvocationTargetException, IllegalAccessException {
        List<PolylineSetMesh> meshes = new ArrayList<>();
        try {
            long patchIdx = 0;
            var patchPathInObjMap = searchAttributeMatchingNameWithPath(energymlObject, "[Node|Line]Patch");
            for (Map.Entry<String, Object> e : patchPathInObjMap.entrySet()) {
                String patchPathInObj = e.getKey();
                Object patch = e.getValue();
                Map.Entry<String, Object> entry = searchAttributeMatchingNameWithPath(patch, "Geometry.Points").entrySet().iterator().next();
                String pointsPath = entry.getKey();
                Object pointsObj = entry.getValue();

                List<List<Double>> points = new ArrayList<>();
                List<?> pl = readArray(pointsObj, energymlObject, patchPathInObj + pointsPath, workspace);
                if(pl.size() > 0) {
                    if (pl.get(0) instanceof Collection) {
                        points.addAll(((List<List<?>>) pl).stream()
                                .map(l -> l.stream().map(v -> ((Number) v).doubleValue()).collect(Collectors.toList())).collect(Collectors.toList()));
                    } else { // pl given flat
                        for (int i = 0; i < pl.size() - 2; i+=3) {
                            points.add(new ArrayList<>(List.of(
                                    ((Number) pl.get(i)).doubleValue(),
                                    ((Number) pl.get(i + 1)).doubleValue(),
                                    ((Number) pl.get(i + 2)).doubleValue()
                            )));
                        }
                    }
                }else{
                    logger.info("Size is 0 for {}", patch);
                }


                Object crs = null;
                try {
                    crs = getCrsObj(pointsObj, patchPathInObj + pointsPath, energymlObject, workspace);
                } catch (ObjectNotFoundNotError ignore) {
                }

                Map.Entry<String, Object> closedPolyEntry = searchAttributeMatchingNameWithPath(patch, "ClosedPolylines").entrySet().iterator().next();
                String closePolyPath = closedPolyEntry.getKey();
                Object closePolyObj = closedPolyEntry.getValue();
                var closePoly = readArray(closePolyObj, energymlObject, patchPathInObj + closePolyPath, workspace);

                List<List<Long>> pointIndices = null;
                try {
                    Map.Entry<String, Object> nodeCountPerPolyPathInObjEntry = searchAttributeMatchingNameWithPath(patch, "NodeCountPerPolyline").entrySet().iterator().next();
                    String nodeCountPerPolyPathInObj = nodeCountPerPolyPathInObjEntry.getKey();
                    Object nodeCountPerPoly = nodeCountPerPolyPathInObjEntry.getValue();
                    List<Long> nodeCountsList = readArray(nodeCountPerPoly, energymlObject, patchPathInObj + nodeCountPerPolyPathInObj, workspace).stream()
                            .map(v -> ((Number)v).longValue()).collect(Collectors.toList());
                    long idx = 0;
                    int polyIdx = 0;
                    pointIndices = new ArrayList<>();
                    for (Long nbNode : nodeCountsList) {
                        pointIndices.add(IntStream.range((int) idx, (int) idx + nbNode.intValue())
                                .boxed().map(Long::valueOf).collect(Collectors.toList()));
                        if (closePoly != null && closePoly.size() > polyIdx && closePoly.get(polyIdx) != null) {
                            // pointIndices.get(pointIndices.size() - 1).add(idx);
                        }
                        idx += nbNode;
                        polyIdx++;
                    }
                } catch (IndexOutOfBoundsException ignore) {
                }

                if (pointIndices == null || pointIndices.isEmpty()) {
                    pointIndices = new ArrayList<List<Long>>(Collections.singleton(new ArrayList<Long>(IntStream.range(0, points.size()).mapToObj(Long::valueOf).collect(Collectors.toList()))));
                }

                if (!points.isEmpty()) {
                    meshes.add(new PolylineSetMesh(
                            energymlObject,
                            crs,
                            points,
                            String.format("%s_patch%d", EPCFile.getIdentifier(energymlObject), patchIdx),
                            pointIndices
                    ));
                }

                patchIdx++;
            }
        }catch (Exception e){
            logger.error(e);
            throw e;
        }
        return meshes;
    }

    public static List<PointSetMesh> readPointRepresentation(Object energymlObject, EnergymlWorkspace workspace) throws NotImplementedException, InvocationTargetException, IllegalAccessException {
        List<PointSetMesh> meshes = new ArrayList<>();

        long patchIdx = 0;
        Map<String, Object> pointsPathInObjMap = searchAttributeMatchingNameWithPath(energymlObject, "NodePatch.[\\d]+.Geometry.Points");
        for(Map.Entry<String, Object> e: pointsPathInObjMap.entrySet()) {
            String pointsPathInObj = e.getKey();
            Object pointsObj = e.getValue();

            List<List<Double>> points = new ArrayList<>();
            List<?> pl = readArray(pointsObj, energymlObject, pointsPathInObj, workspace);
            if(pl.size() > 0) {
                if (pl.get(0) instanceof Collection) {
                    points.addAll(((List<List<?>>) pl).stream()
                            .map(l -> l.stream().map(v -> ((Number) v).doubleValue()).collect(Collectors.toList())).collect(Collectors.toList()));
                } else { // pl given flat
                    for (int i = 0; i < pl.size() - 2; i+=3) {
                        points.add(new ArrayList<>(List.of(
                                ((Number) pl.get(i)).doubleValue(),
                                ((Number) pl.get(i + 1)).doubleValue(),
                                ((Number) pl.get(i + 2)).doubleValue()
                        )));
                    }
                }
            }else{
                logger.info("Size is 0 for {}", pointsPathInObj);
            }

            Object crs = null;
            try {
                crs = getCrsObj(pointsObj, pointsPathInObj, energymlObject, workspace);
            } catch (ObjectNotFoundNotError ignore) {}
            if (points != null) {
                meshes.add(new PointSetMesh(
                        energymlObject,
                        crs,
                        points,
                        String.format("NodePatch num %d", patchIdx)
                ));
            }

            patchIdx++;
        }

        patchIdx = 0;
        pointsPathInObjMap = searchAttributeMatchingNameWithPath(energymlObject, "NodePatchGeometry.[\\d]+.Points");
        for(Map.Entry<String, Object> e: pointsPathInObjMap.entrySet()) {
            String pointsPathInObj = e.getKey();
            Object pointsObj = e.getValue();
            List<List<Double>> points = new ArrayList<>();
            List<?> pl = readArray(pointsObj, energymlObject, pointsPathInObj, workspace);
            if(pl.size() > 0) {
                if (pl.get(0) instanceof Collection) {
                    points.addAll(((List<List<?>>) pl).stream()
                            .map(l -> l.stream().map(v -> ((Number) v).doubleValue()).collect(Collectors.toList())).collect(Collectors.toList()));
                } else { // pl given flat
                    for (int i = 0; i < pl.size() - 2; i+=3) {
                        points.add(new ArrayList<>(List.of(
                                ((Number) pl.get(i)).doubleValue(),
                                ((Number) pl.get(i + 1)).doubleValue(),
                                ((Number) pl.get(i + 2)).doubleValue()
                        )));
                    }
                }
            }else{
                logger.info("Size is 0 for {}", pointsPathInObj);
            }

            Object crs = null;
            try {
                crs = getCrsObj(pointsObj, pointsPathInObj, energymlObject, workspace);
            } catch (ObjectNotFoundNotError ignore) {}
            if (points != null) {
                meshes.add(new PointSetMesh(
                        energymlObject,
                        crs,
                        points,
                        String.format("NodePatchGeometry num %d", patchIdx)
                ));
            }

            patchIdx++;
        }

        return meshes;
    }

    public static void main(String[] argv){
        System.out.println(hsvToRgb(180.0f, 33.0f, 100.0f));
        System.out.println(getObjectTypeForFilePath_fromClassName(ObjPointSetRepresentation.class.getName()));
        System.out.println(getObjectTypeForFilePath_fromClassName(PointSetRepresentation.class.getSimpleName()));
    }
}
