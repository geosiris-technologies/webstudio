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

import com.geosiris.energyml.data.Mesh;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.energyml.utils.Utils;
import com.geosiris.etp.utils.ETPUri;
import com.geosiris.etp.websocket.ETPClient;
import com.geosiris.webstudio.etp.ETPWorkspace;
import com.geosiris.webstudio.logs.ServerLogMessage;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.geosiris.energyml.data.SurfaceMesh.exportObj;

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
        ETPClient etpClient = ETPUtils.establishConnexion(null, ETPUtils.getHttpUriETP(serverUrl), username, password, null, new HashMap<>(), true);

        Map<String, ETPUri> mapUri = new HashMap<>();
        ETPUri etpuri = ETPUri.parse(parameters.get("uri").getAsString());
        mapUri.put("0", etpuri);

        List<ETP3DObject> surfaces = new ArrayList<>();
        for(ETPUri etpUri: mapUri.values()) {
            String err_msg = "Failed to load 3D surface : " + etpUri;
            try {
                ETP3DObject o = ETPUtils.get3DFileFromETP(etpClient, null, etpUri.toString(), fileFormat);
                if(o == null){
                    SessionUtility.log(request.getSession(), new ServerLogMessage(ServerLogMessage.MessageType.TOAST, err_msg, "Surface loader"));
                }else {
                    surfaces.add(o);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                SessionUtility.log(request.getSession(), new ServerLogMessage(ServerLogMessage.MessageType.TOAST, err_msg, "Surface loader"));
            }
        }
        PrintWriter out = response.getWriter();
        response.setContentType("application/text");
        response.setCharacterEncoding("UTF-8");
        out.write(Objects.requireNonNullElse(surfaces.get(0).getData(), ""));
        out.flush();
    }

    public static void main(String[] argv) throws IOException, InvocationTargetException, IllegalAccessException {
        String serverUrl = "https://rdms.geosiris.com:443/etp";
        String username = "FAKE";
        String password = "FAKE";
        ETPClient etpClient = ETPUtils.establishConnexion(null, ETPUtils.getHttpUriETP(serverUrl), username, password, null, new HashMap<>(), true);

        ETPUri etpuri = ETPUri.parse("eml:///dataspace('brgm')/resqml22.TriangulatedSetRepresentation(ffbf9912-c2d2-489f-a924-7d14e2627134)");
//        ETPUri etpuri = ETPUri.parse("eml:///dataspace('brgm')/resqml22.PointSetRepresentation(c26dfedf-c354-4263-9219-97797638beef)");
//        ETPUri etpuri = ETPUri.parse("eml:///dataspace('brgm')/resqml22.TriangulatedSetRepresentation(e648b1d0-70a3-46c6-9be8-b12625661c2b)");
//        ETPUri etpuri = ETPUri.parse("eml:///dataspace('brgm')/resqml22.TriangulatedSetRepresentation(16ca92d0-912b-4c3d-a8d5-86378c9e8be5)");
//        ETPUri etpuri = ETPUri.parse("eml:///dataspace('brgm')/resqml22.PolylineSetRepresentation(2ab8076e-a9cd-4e4b-b9af-a32f8f434f43)");
//        ETPUri etpuri = ETPUri.parse("eml:///dataspace('brgm')/resqml22.PolylineSetRepresentation(8ba85389-0ab9-4a79-a6ac-a4de0d7b1cb2)");
//        ETPUri etpuri = ETPUri.parse("eml:///dataspace('brgm')/resqml22.PolylineSetRepresentation(8236dd72-0944-4df1-abfc-28e86b2e446e)");
//        ETPUri etpuri = ETPUri.parse("eml:///dataspace('brgm')/resqml22.PolylineSetRepresentation(950e490a-f12c-4920-bc66-70ef4a5aaba0)");


        ETPWorkspace workspace = new ETPWorkspace(etpuri.getDataspace(), etpClient);
        Object obj = workspace.getEnergisticsObject(etpuri.toString());
        System.out.println(obj);
        exportObj(Mesh.readMeshObject(obj, workspace),
                new FileOutputStream("D:/Geosiris/Cloud/Geo-Workflow/BRGM/BRGM_RESQML_PROJECT/AVRE/results/" + etpuri.getObjectType() + "_" + etpuri.getUuid()
                        + ObjectController.getObjectAttributeValue(obj, "citation.Title") + ".obj"), "test", false);

        etpClient.closeClient();

    }

}
