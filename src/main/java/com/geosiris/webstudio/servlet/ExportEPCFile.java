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
package com.geosiris.webstudio.servlet;

import com.geosiris.energyml.utils.EPCGenericManager;
import com.geosiris.energyml.utils.ExportVersion;
import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.model.WorkspaceContent;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                exportEPCFile(bos,
                        workspace,
                        exportVersion);
                byte[] bos_bytes = bos.toByteArray();

                request.setAttribute("response", "File exported");

                // obtains ServletContext
                ServletContext context = getServletContext();

                // gets MIME type of the file
                String mimeType = context.getMimeType(filePath);
                if (mimeType == null) {
                    // set to binary type if MIME mapping not found
                    mimeType = "application/octet-stream";
                }

                response.setContentType(mimeType);
//                response.setContentLength((int) downloadFile.length());
                response.setContentLength(bos_bytes.length);

                // forces download
                String headerKey = "Content-Disposition";
                String headerValue = String.format("attachment; filename=\"%s\"", filePath);
                response.setHeader(headerKey, headerValue);

                // obtains response's output stream
                OutputStream outStream = response.getOutputStream();

                for(int chunk=0; chunk<bos_bytes.length; chunk+= 4096){
                    outStream.write(bos_bytes, chunk, Math.min(4096, bos_bytes.length-chunk));
                }

                outStream.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                request.setAttribute("response", "Error occured while exporting file : \n" + e.getMessage() + " \n " + Arrays.toString(e.getStackTrace()));
            }

            String closeEpc = request.getParameter("close");
            if (closeEpc != null && closeEpc.toLowerCase().compareTo("true") == 0) {
                FileReciever.closeEPC(session);
            } else {
                logger.error("not closing epc : " + closeEpc);
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

    public static void exportEPCFile(OutputStream out,
                                     WorkspaceContent workspace,
                                     ExportVersion exportVersion){
        try {
            try(ZipOutputStream epc = new ZipOutputStream(out)) {
                for (Map.Entry<String, Object> kv : workspace.getReadObjects().entrySet()) {
                    ZipEntry ze_resqml = new ZipEntry(EPCGenericManager.genPathInEPC(kv.getValue(), exportVersion));
                    epc.putNextEntry(ze_resqml);
                    Editor.pkgManager.marshal(kv.getValue(), epc);
                    epc.closeEntry();
                }
                EPCGenericManager.exportRels(workspace.getReadObjects(), workspace.getParsedRels(), exportVersion, epc, "Geosiris Resqml WebStudio");

                if (workspace.getNotReadObjects() != null) {
                    /// non resqml obj
                    for (Pair<String, byte[]> nonResqmlObj : workspace.getNotReadObjects()) {
                        try {
                            ZipEntry ze_resqml = new ZipEntry(nonResqmlObj.l());
                            epc.putNextEntry(ze_resqml);
                            byte[] fileContent = nonResqmlObj.r();
                            for(int chunk=0; chunk<fileContent.length; chunk += 4096){
                                epc.write(fileContent, chunk, Math.min(4096, fileContent.length-chunk));
                            }
                            epc.closeEntry();
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            logger.error("nonResqmlObj " + nonResqmlObj.l());
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
