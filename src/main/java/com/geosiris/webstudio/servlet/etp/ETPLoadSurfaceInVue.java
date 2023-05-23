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

import com.geosiris.etp.utils.ETPUri;
import com.geosiris.etp.websocket.ETPClient;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.model.ETP3DObject;
import com.geosiris.webstudio.utils.ETPUtils;
import com.geosiris.webstudio.utils.SessionUtility;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.bind.JAXBException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Servlet implementation class ETPLoadSurfaceInVue
 */
@WebServlet("/ETPLoadSurfaceInVue")
public class ETPLoadSurfaceInVue extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static Logger logger = LogManager.getLogger(ETPLoadSurfaceInVue.class);


    /**
     * @see HttpServlet#HttpServlet()
     */
    public ETPLoadSurfaceInVue() {
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
        out.write("Please use POST");
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
        HttpSession session = request.getSession(false);
        StringBuilder jb = new StringBuilder();
        String line;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) { /*report an error*/ }

        JsonObject parameters = new Gson().fromJson(jb.toString(), JsonObject.class);

        Map<String, ETPUri> mapUri = new HashMap<>();
        logger.debug("#importObjectIn3DView uris :");
        for (JsonElement uri : parameters.getAsJsonArray("uris")) {
            logger.debug(uri);
            ETPUri etpuri = ETPUri.parse(uri.getAsString());
            SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.LOG,
                    "ETP request import on " + etpuri + " == " + etpuri.hasDataspace() + " --- " + etpuri.getDataspace(),
                    SessionUtility.EDITOR_NAME));
            mapUri.put(mapUri.size()+"", etpuri);
        }

        ETPClient etpClient = (ETPClient) session.getAttribute(SessionUtility.SESSION_ETP_CLIENT_ID);

        List<ETP3DObject> surfaces = new ArrayList<>();
        for(ETPUri etpUri: mapUri.values()) {
            try {
                surfaces.add(ETPUtils.get3DFileFromETP(etpClient, etpUri.toString(), false));
            } catch (JAXBException e) {
                logger.error(e.getMessage(), e);
            }
        }
        PrintWriter out = response.getWriter();
        response.setContentType("application/text");
        response.setCharacterEncoding("UTF-8");
        out.write(Objects.requireNonNullElse(new Gson().toJson(surfaces), ""));
        out.flush();
    }

}
