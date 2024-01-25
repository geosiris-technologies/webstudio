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

import com.geosiris.energyml.utils.ExportVersion;
import com.geosiris.webstudio.model.WorkspaceContent;
import com.geosiris.webstudio.servlet.editing.FileReciever;
import com.geosiris.webstudio.utils.HttpSender;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servlet implementation class ExportEPCFile
 */
@WebServlet("/ExportEPCFile")
public class ExportEPCFile extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(ExportEPCFile.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ExportEPCFile() {
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

        String filePath = request.getParameter("epcFilePath");
        if (filePath != null) {
            if (!filePath.toLowerCase().endsWith(".epc")) {
                filePath += ".epc";
            }
            for (String param : request.getParameterMap().keySet()) {
                logger.info(" > " + param);
            }

            ExportVersion exportVersion = ExportVersion.CLASSIC;
            String versionParam = request.getParameter("exportVersion");
            if (versionParam != null) {
                if (versionParam.toLowerCase().contains("expand")) {
                    exportVersion = ExportVersion.EXPANDED;
                }
            }
            logger.info("export version " + versionParam + " -- " + exportVersion);

            // Debut filtrage des fichiers a exporter
            String[] partialExportUUIDs = request.getParameterValues("partialExportUUID");

            List<String> partialExportUUIDs_list = new ArrayList<>();
            if (partialExportUUIDs != null) {
                partialExportUUIDs_list.addAll(Arrays.asList(partialExportUUIDs));
            }

            WorkspaceContent workspace = new WorkspaceContent();
            workspace.putAll(SessionUtility.getWorkspaceContent(session));

            Map<String, Object> epcFiles = SessionUtility.getResqmlObjects(session);
            if (partialExportUUIDs_list.size() > 0) {
                epcFiles = epcFiles.entrySet().stream()
                        .filter(x -> partialExportUUIDs_list.contains(x.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                workspace.setReadObjects(epcFiles);
            }
            // FIN filtrage fichiers a exporter

            request.setAttribute("response", "File exported");

            // obtains ServletContext
            ServletContext context = getServletContext();

            // gets MIME type of the file
            String mimeType = context.getMimeType(filePath);
            HttpSender.writeEpcAsRequestResponse(response, workspace, filePath, exportVersion, mimeType);

            String closeEpc = request.getParameter("close");
            if (closeEpc != null && closeEpc.toLowerCase().compareTo("true") == 0) {
                FileReciever.closeEPC(session);
            } else {
                logger.debug("not closing epc : " + closeEpc);
            }
        } else {
            logger.error("No epcFilePath found");
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
