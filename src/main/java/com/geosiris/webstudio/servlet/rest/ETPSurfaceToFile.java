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
package com.geosiris.webstudio.servlet.rest;

import com.geosiris.etp.utils.ETPUri;
import com.geosiris.etp.websocket.ETPClient;
import com.geosiris.webstudio.model.ETP3DObject;
import com.geosiris.webstudio.utils.ETPUtils;
import com.geosiris.webstudio.utils.File3DType;
import com.geosiris.webstudio.utils.SessionUtility;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.JAXBException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Servlet implementation class ETPSurfaceToOff
 */
@WebServlet(urlPatterns = {"/ETPSurfaceToFile", "/ETPSurfaceToObj", "/ETPSurfaceToOff"})
public class ETPSurfaceToFile extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static Logger logger = LogManager.getLogger(ETPSurfaceToFile.class);


    /**
     * @see HttpServlet#HttpServlet()
     */
    public ETPSurfaceToFile() {
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
        response.setContentType("application/text");
        response.setCharacterEncoding("UTF-8");
        out.write("Please use POST with parameters : uri, serverUrl, [serverLogin], [serverPassword], [format]");
        out.flush();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StringBuilder jb = new StringBuilder();
        String line;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) { /*report an error*/ }
        String requestUri = request.getRequestURI().toLowerCase();
        JsonObject parameters = new Gson().fromJson(jb.toString(), JsonObject.class);

        String serverUrl = parameters.get("serverUrl").getAsString();
        String username = parameters.get("username").getAsString();
        String password = parameters.get("password").getAsString();
//        String serverToken = parameters.get("serverToken").getAsString();

        String fileFormat_param = parameters.get("format").getAsString();
        File3DType fileFormat = File3DType.OFF;
        if(fileFormat_param != null){
            if(fileFormat_param.equalsIgnoreCase("obj")){
                fileFormat = File3DType.OBJ;
            }
        } else if (requestUri.contains("obj")) {
            fileFormat = File3DType.OBJ;
        }else if (requestUri.contains("off")) {
            fileFormat = File3DType.OFF;
        }


        System.out.println(serverUrl  + "- " + username  + "- " + password);
        ETPClient etpClient = ETPUtils.establishConnexion(null, ETPUtils.getHttpUriETP(serverUrl), username, password, true);

        Map<String, ETPUri> mapUri = new HashMap<>();
        ETPUri etpuri = ETPUri.parse(parameters.get("uri").getAsString());
        mapUri.put("0", etpuri);

        List<ETP3DObject> surfaces = new ArrayList<>();
        for(ETPUri etpUri: mapUri.values()) {
            try {
                surfaces.add(ETPUtils.get3DFileFromETP(etpClient, null, etpUri.toString(), fileFormat, false));
            } catch (JAXBException e) {
                logger.error(e.getMessage(), e);
            }
        }
        PrintWriter out = response.getWriter();
        response.setContentType("application/text");
        response.setCharacterEncoding("UTF-8");
        out.write(Objects.requireNonNullElse(surfaces.get(0).getData(), ""));
        out.flush();
    }

}
