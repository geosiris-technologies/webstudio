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

import com.geosiris.energyml.utils.ExportVersion;
import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.logs.LogMessage;
import com.geosiris.webstudio.logs.LogResqmlVerification;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.model.WorkspaceContent;
import com.geosiris.webstudio.utils.HttpSender;
import com.geosiris.webstudio.utils.ResqmlVerification;
import com.geosiris.webstudio.utils.SessionUtility;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servlet implementation class ResqmlVerification
 */
@WebServlet(urlPatterns = {"/ResqmlVerification", "/EnergymlValidation", "/EnergymlCorrection", "/EnergymlFix"})
public class ResqmlVerificationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResqmlVerificationServlet() {
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

        StringBuilder answer = new StringBuilder();
        Map<String, Object> map = SessionUtility.getResqmlObjects(session);
        String uuid = request.getParameter("uuid");

        List<LogResqmlVerification> logs;

        if (uuid != null) {
            logs = ResqmlVerification.doVerification(uuid, map, ResqmlVerification.getAllEnergymlAbstractObjects(map));
        } else {
            logs = ResqmlVerification.doVerification(map);
        }

        answer.append("[");
        for (LogMessage lm : logs) {
            answer.append((ServerLogMessage.parseLogMessage(lm)).toJSON()).append(",");
        }
        if (answer.toString().endsWith(",")) {
            answer = new StringBuilder(answer.substring(0, answer.length() - 1));
        }
        answer.append("]");

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(answer.toString());
        out.flush();
    }

    /**
     * A REST endpoint that takes in input files as EPC or xml and returns a json representations of verification logs
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestUri = request.getRequestURI().toLowerCase();

        HttpSession session = request.getSession(false);
        WorkspaceContent tmpWorkspace = new WorkspaceContent();

        boolean isCorrection = requestUri.contains("correct") || requestUri.contains("fix");

        ExportVersion exportVersion = null;

        if (ServletFileUpload.isMultipartContent(request)) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            FileItemIterator iterator = upload.getItemIterator(request);
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                InputStream stream = item.openStream();
                if (item.getFieldName().compareTo("version") == 0) {
                    String fieldValue = Streams.asString(stream);
                    try {
                        exportVersion = ExportVersion.valueOf(fieldValue);
                    }catch (Exception ignore){}
                } else {
                    tmpWorkspace.putAll(HttpSender.readFile(session, stream, item.getName()));
                }
            }
        }
        Map<String, Object> additionalInformation = new HashMap<>();
        if(isCorrection) {
            if(exportVersion == null){
                exportVersion = ExportVersion.CLASSIC;
            }
            additionalInformation.put("notReadObjects", tmpWorkspace.getNotReadObjects().stream().map(Pair::l).collect(Collectors.toList()));

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String logs = logVerificationListToJsonResult(ResqmlVerification.doCorrection(tmpWorkspace.getReadObjects()), tmpWorkspace, additionalInformation);
            tmpWorkspace.getNotReadObjects().add(new Pair<>("correctionLogs_" + dtf.format(now) + ".json", logs.getBytes(StandardCharsets.UTF_8)));

            HttpSender.writeEpcAsRequestResponse(response, tmpWorkspace, "correctedFile.epc", exportVersion, null);
        }else{ // verification
            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.write(logVerificationListToJsonResult(ResqmlVerification.doVerification(tmpWorkspace.getReadObjects()), tmpWorkspace, additionalInformation));
            out.flush();
        }
    }


    public static String logVerificationListToJsonResult(List<LogResqmlVerification> verifsLogs, WorkspaceContent workspaceContent, Map<String, Object> additionalInformation){
        HashMap<String, Object> jsonResult = new HashMap<>();
        // < Severity, < Uuid, < MsgTitle, [ Messages ] > > >
        Map<String, Map<String, Map<String, List<String>>>> mapped = new HashMap<>();

        for (LogResqmlVerification log : verifsLogs) {
            Map<String, Map<String, List<String>>> mapUuid = mapped.getOrDefault(log.getSeverity().toString(), new HashMap<>());
            Map<String, List<String>> msgsForUuid = mapUuid.getOrDefault(log.getRootUUID(), new HashMap<>());
            List<String> msgsForTitle = msgsForUuid.getOrDefault(log.getTitle(), new ArrayList<>());
            msgsForTitle.add(log.getRootType() + ") \"" + log.getRootTitle() + "\": " + log.getMsg());
            msgsForUuid.put(log.getTitle(), msgsForTitle);
            mapUuid.put(log.getRootUUID(), msgsForUuid);
            mapped.put(log.getSeverity().toString(), mapUuid);
        }

        jsonResult.put("errors", verifsLogs.size());
        jsonResult.put("readObjects", mapped);

        /* ** OTHERS ** */
        HashMap<String, Object> other = new HashMap<>();
        other.put("nbReadedFile", workspaceContent.getReadObjects().size());
        other.put("notReadObjects", workspaceContent.getNotReadObjects().stream().map(Pair::l).collect(Collectors.toList()));
        other.put("additionalInformation", workspaceContent.getAdditionalInformation());

        other.putAll(additionalInformation);

        jsonResult.put("other", other);
        /* ** ****** ** */

        Gson gson = new Gson();
        return gson.toJson(jsonResult);
    }

}
