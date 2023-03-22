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

import Energistics.Etp.v12.Protocol.Store.GetDataObjects;
import Energistics.Etp.v12.Protocol.Store.GetDataObjectsResponse;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.etp.communication.Message;
import com.geosiris.etp.utils.ETPDefaultProtocolBuilder;
import com.geosiris.etp.utils.ETPUri;
import com.geosiris.etp.websocket.ETPClient;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.property.ActivityLauncherProperties;
import com.geosiris.webstudio.servlet.Editor;
import com.geosiris.webstudio.utils.ETPUtils;
import com.geosiris.webstudio.utils.SessionUtility;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpURI;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@WebServlet("/LaunchActivityWorkflow")
public class LaunchActivityWorkflow extends HttpServlet {
    public static Logger logger = LogManager.getLogger(LaunchActivityWorkflow.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(!SessionUtility.tryConnectServlet(request, response)) {
            return;
        }

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write("[]");
        out.flush();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtility.tryConnectServlet(request, response)) {
            return;
        }
        StringBuilder answer = new StringBuilder();

        HttpSession session = request.getSession(false);
        HttpURI host = new HttpURI("ws://rdms.geosiris.com/etp/");

        String message =  IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);

        logger.debug("---------------- Message is ");
        logger.debug(message);
        logger.debug("----------------");

        Gson gson = new Gson();
        JsonObject jsonObject = new Gson().fromJson(message, JsonObject.class);

        JsonArray uris = jsonObject.getAsJsonArray("uris");
        String ce_type = jsonObject.getAsJsonPrimitive("ce-type").getAsString();

        ETPClient etpClient = (ETPClient) session.getAttribute(SessionUtility.SESSION_ETP_CLIENT_ID);

        Map<CharSequence, CharSequence> mapUri = new HashMap<>();
        for(JsonElement uriElt: uris){
            String uri = uriElt.getAsString();
            if(uri.contains("Activity"))
                mapUri.put(mapUri.size() + "", uri);
            else{
                logger.debug("Not an activity : " + uri);
            }
        }

        if(mapUri.size()>0) {
            try {
                GetDataObjects getDataO = ETPDefaultProtocolBuilder.buildGetDataObjects(mapUri, "xml");
                List<Message> dataResp_m_l = ETPUtils.sendETPRequest(session, etpClient, getDataO, false, -1);
                logger.debug("dataResp_m_l > " + dataResp_m_l.size() + " messages");
                for (Message dataResp_m : dataResp_m_l) {
                    Object dataResp = dataResp_m.getBody();

                    logger.debug("\t-> " + dataResp.getClass().getSimpleName() + " messages");
                    if (dataResp instanceof GetDataObjectsResponse) {
                        logger.debug("ETP <== Recieve ETP message " + dataResp.getClass().getSimpleName());
//                        logger.debug("ETP <== Recieve ETP message " + dataResp.getClass().getSimpleName() + " : " + dataResp);
                        GetDataObjectsResponse gdor = (GetDataObjectsResponse) dataResp;
                        for (CharSequence respId : gdor.getDataObjects().keySet()) {
                            String currentDataspace = ETPUri.parse(mapUri.get(respId + "") + "").getDataspace(); // On lit l'uri de l'objet qui a donne cette reponse
                            logger.debug(respId + "- " + mapUri.size() + ") CURRENT DATASPACE : " + currentDataspace + " of " + mapUri.get(respId));
                            try {
                                Object parsedObject = Editor.pkgManager.unmarshal(new String(gdor.getDataObjects().get(respId).getData().array(), StandardCharsets.UTF_8)).getValue();
//                                logger.debug("OBJClass : " + parsedObject.getClass().getName());
                                List<Object> parameters = (List<Object>) ObjectController.getObjectAttributeValue(parsedObject, "Parameter");

                                Map<CharSequence, CharSequence> mapActivity = new HashMap<>();
                                for(Object paramActivity : parameters){
                                    try {
                                        mapActivity.put(mapActivity.size() + "", ETPUtils.getUriFromDOR(ObjectController.getObjectAttributeValue(paramActivity, "dataObject"), currentDataspace).toString());
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                                String reqContent = "{\"data_objects\": " + gson.toJson(ETPDefaultProtocolBuilder.buildGetDataObjects(mapActivity, "xml")).replace("\u0027", "'") + "}";
                                logger.debug( reqContent );

                                answer.append(sendLaunchWorkflow(session, reqContent, ce_type)).append("\n");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        logger.debug("GetDataObject Response : " + dataResp);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        PrintWriter out = response.getWriter();
        response.setContentType("application/text");
        response.setCharacterEncoding("UTF-8");
        out.write(answer.toString());
        out.flush();
    }

    public static String sendLaunchWorkflow(HttpSession session,  String jsonContentToSend, String ce_type){
        ActivityLauncherProperties properties = new ActivityLauncherProperties();

        String result = "";
        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.DEBUG, jsonContentToSend, "ActivityLauncher"));
            HttpPost send_req = new HttpPost(properties.getWorkflowUrl());
            send_req.addHeader("content-type", "application/json");
            send_req.addHeader("ce-specversion", properties.getSpecversion());
            send_req.addHeader("ce-id", "ws-" + UUID.randomUUID());
            send_req.addHeader("ce-type", ce_type); //properties.getType()); // MULTIPLE_MBA.started ou  Process_0hv5t1z.started
            send_req.addHeader("ce-source", properties.getSource());
            StringEntity params = new StringEntity(jsonContentToSend);
            send_req.setEntity(params);
            HttpResponse answer = httpClient.execute(send_req);
            result = IOUtils.toString(answer.getEntity().getContent());
            logger.debug("@sendLaunchWorkflow \n" + Arrays.stream(answer.getAllHeaders()).map(h -> "\t" + h.getName() + " => " + h.getValue() + "\n").reduce("", (a,b) -> a+b ));

            SessionUtility.log(session, new ServerLogMessage( ServerLogMessage.MessageType.DEBUG,
                    "@sendLaunchWorkflow \n" + Arrays.stream(answer.getAllHeaders()).map(h -> "\t" + h.getName() + " => " + h.getValue() + "\n").reduce("", (a,b) -> a+b),
                    "ActivityLauncher"));
            logger.debug("@sendLaunchWorkflow " + result);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return result;
    }
}
