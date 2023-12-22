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
package com.geosiris.webstudio.servlet.energyml;

import com.geosiris.webstudio.logs.LogMessage;
import com.geosiris.webstudio.logs.LogResqmlVerification;
import com.geosiris.webstudio.servlet.db.workspace.LoadWorkspace;
import com.geosiris.webstudio.utils.ResqmlVerification;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servlet implementation class ResqmlVerification
 */
@WebServlet("/ResqmlCorrection")
public class ResqmlCorrection extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(ResqmlCorrection.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResqmlCorrection() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtility.tryConnectServlet(request, response)) {
            return;
        }
        HttpSession session = request.getSession(false);
        String answer = "";

        Map<String, Object> map = SessionUtility.getResqmlObjects(session);
        String uuid = request.getParameter("uuid");
        String correctionType = request.getParameter("correctionType");

        if (correctionType == null || correctionType.compareToIgnoreCase("dor") == 0) {
            List<LogResqmlVerification> logs;
            if (uuid != null) {
                logs = ResqmlVerification.doCorrection(uuid, ResqmlVerification.getAllEnergymlAbstractObjects(map));
            } else {
                logs = ResqmlVerification.doCorrection(map);
            }
            answer = LogMessage.toJSON(logs);
            if (answer.length() > 0) {
                LoadWorkspace.updateWorkspace(session, logs.stream().map(LogResqmlVerification::getRootUUID).collect(Collectors.toList()));
            }
        } else if (correctionType.compareToIgnoreCase("versionString") == 0) {
            logger.info("Removing versionString request");
            List<LogResqmlVerification> logs;
            if (uuid != null) {
                logs = ResqmlVerification.doRemoveVersionString(uuid, map);
            } else {
                logs = ResqmlVerification.doRemoveVersionString(map);
            }
            answer = LogResqmlVerification.toJSON(logs);
            if (answer.length() > 0) {
                LoadWorkspace.updateWorkspace(session, logs.stream().map(LogResqmlVerification::getRootUUID).collect(Collectors.toList()));
            }
        } else if (correctionType.compareToIgnoreCase("SchemaVersion") == 0) {
            logger.info("Correct SchemaVersion request");
            List<LogResqmlVerification> logs;
            if (uuid != null) {
                logs = ResqmlVerification.doCorrectSchemaVersion(uuid, map);
            } else {
                logs = ResqmlVerification.doCorrectSchemaVersion(map);
            }
            answer = LogResqmlVerification.toJSON(logs);
            if (answer.length() > 0) {
                LoadWorkspace.updateWorkspace(session, logs.stream().map(LogResqmlVerification::getRootUUID).collect(Collectors.toList()));
            }
        } else {
            answer = "Unkown correction";
        }
        //SessionUtility.log(session, new ServerLogMessage(MessageType.TOAST, "Corrected", "Server"));
        PrintWriter out = response.getWriter();
        response.setContentType("application/text");
        response.setCharacterEncoding("UTF-8");
        out.write(answer);
        out.flush();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
