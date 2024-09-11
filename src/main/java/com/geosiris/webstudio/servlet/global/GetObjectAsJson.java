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
package com.geosiris.webstudio.servlet.global;

import com.geosiris.energyml.utils.EPCGenericManager;
import com.geosiris.energyml.utils.ExportVersion;
import com.geosiris.webstudio.utils.HttpSender;
import com.geosiris.webstudio.utils.SessionUtility;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Servlet implementation class GetObjectAsJson
 */
@WebServlet("/GetObjectAsJson")
@MultipartConfig
public class GetObjectAsJson extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(GetObjectAsJson.class);

    public static Gson gson = new Gson();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetObjectAsJson() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionUtility.tryConnectServlet(request, response)) {
            return;
        }
        HttpSession session = request.getSession(false);

        Map<String, Object> map = SessionUtility.getResqmlObjects(session);
        String uuid = request.getParameter("uuid");
        String answer = "";


        if(map.containsKey(uuid)) {
            Gson gson = new Gson();
            Object obj = map.get(uuid);
            try {
                answer = gson.toJson(obj);
//                StringWriter s = new StringWriter();
//                Utility.objectToJson();
            }catch (Exception e){
                logger.debug(e.getMessage(), e);
            }

            if("true".equals(request.getParameter("download"))){
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bos.write(answer.getBytes());
                HttpSender.writeFileAsRequestResponse(response, EPCGenericManager.genPathInEPC(obj, ExportVersion.CLASSIC).replace(".xml", ".json"), "application/json", bos);
            }else {
                PrintWriter out = response.getWriter();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                out.write(answer);
                out.flush();
            }
        }else{
            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.write("Unknown uuid : " + uuid);
            out.flush();
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionUtility.tryConnectServlet(request, response)) {
            return;
        }

        doGet(request, response);
    }



}
