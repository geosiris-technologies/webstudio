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

import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.logs.ServerLogMessage.MessageType;
import com.geosiris.webstudio.model.WorkspaceContent;
import com.geosiris.webstudio.property.ConfigurationType;
import com.geosiris.webstudio.property.WebStudioProperties;
import com.geosiris.webstudio.servlet.db.user.UserDBInfos;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SessionUtility {
    public static Logger logger = LogManager.getLogger(SessionUtility.class);

    public static final String EDITOR_NAME = "ResqmlEditor";

    public static final String PREFERENCE_FOLDER_NAME = "preference";
    public static final String SESSION_WORKSPACE_DATA_ID = "workspaceData";

    public static final String SESSION_ETP_CLIENT_ID = "etpClient";
    public static final String SESSION_ETP_CLIENT_LAST_URL = "etpUrl";
    public static final String SESSION_ETP_CLIENT_LAST_USERNAME = "etpLogin";
    public static final String SESSION_ETP_CLIENT_LAST_PASSWORD = "etpPassword";

    public static final String SESSION_LOGS = "editorLogs";

    public static final String SESSION_USER_NAME = "userName";
    public static final String SESSION_USER_GRP = "userGroup";

    public static final WebStudioProperties wsProperties = new WebStudioProperties();

    public static boolean tryConnectServlet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("GEOSIRIS_ENV_PRODUCTION_TYPE", wsProperties.getDeploymentVersion() + "");
        request.setAttribute("GEOSIRIS_ENV_CONFIG_TYPE", wsProperties.getConfigurationType() + "");
        request.setAttribute("GEOSIRIS_ENV_WEBSTUDIO_VERSION", SessionUtility.class.getPackage().getImplementationVersion() + "");

        if (!SessionUtility.isConnectedUser(request)) {
            response.sendRedirect("connexion");
            return false;
        }
        HttpSession session = request.getSession(false);

        String userName = (String) session.getAttribute(SESSION_USER_NAME);
        if(userName == null){
            userName = "DefaultUser";
        }

        // User is connected, now let's give the response some variables
        request.setAttribute("login", userName);
        request.setAttribute("user_grp", session.getAttribute(SESSION_USER_GRP) + "");

        return true;
    }

    public static ConcurrentLinkedQueue<ServerLogMessage> getLogs(HttpSession session) {
        try {
            return (ConcurrentLinkedQueue<ServerLogMessage>) session.getAttribute(SESSION_LOGS);
        }catch (Exception e){
            return new ConcurrentLinkedQueue<>();
        }
    }

    public static WorkspaceContent getWorkspaceContent(HttpSession session){
        WorkspaceContent content = (WorkspaceContent) session.getAttribute(SESSION_WORKSPACE_DATA_ID);
        if(content == null){
            content = new WorkspaceContent();
            session.setAttribute(SESSION_WORKSPACE_DATA_ID, content);
        }
        return content;
    }

    public static Map<String, Object> getResqmlObjects(HttpSession session) {
        WorkspaceContent wc = getWorkspaceContent(session);
        return wc.getReadObjects();
    }
    public static List<Pair<String, byte[]>> getNotResqmlObjects(HttpSession session) {
        WorkspaceContent wc = getWorkspaceContent(session);
        return wc.getNotReadObjects();
    }
    public static Map<String, Object> getAdditionalInformation(HttpSession session) {
        WorkspaceContent wc = getWorkspaceContent(session);
        return wc.getAdditionalInformation();
    }

    public static void log(HttpSession session, ServerLogMessage msg) {
        // Print debug message only if not in production
        if (session!=null && (msg.getSeverity() != MessageType.DEBUG || SessionUtility.configIsMoreVerborseThan(ConfigurationType.debug))) {
            logger.error("#LOGS# " + msg.toJSON());
            if (getLogs(session) == null) {
                session.setAttribute(SESSION_LOGS, new ConcurrentLinkedQueue<ServerLogMessage>());
            }
            getLogs(session).add(msg);
        }
    }

    public static boolean isConnectedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        // logger.info(wsProperties);
        String userName = "";
        if (session != null) {
            userName = (String) session.getAttribute(SESSION_USER_NAME);
        } else if (!wsProperties.getEnableUserDB()) {
            request.getSession(true);
            return true;
        }
        return !wsProperties.getEnableUserDB() || (userName != null && userName.length() > 0);
    }

    public static boolean isAdminUser(HttpServletRequest request) {
        if (isConnectedUser(request)) {
            String userName = (String) request.getSession(false).getAttribute(SESSION_USER_NAME);
            String usergrp = UserDBInfos.getUserAttribute(userName, UserDBInfos.DB_GRP);

            return isAdminUser(usergrp);
        }
        return false;
    }

    public static boolean isAdminUser(String usergrp) {
        return "geosiris".compareTo(usergrp) == 0;
    }

    public static boolean configIsMoreVerborseThan(ConfigurationType config){
        return wsProperties.getConfigurationType().value >= config.value;
    }

    public static HashMap<String, List<String>> getParameterMap(HttpServletRequest request){
        HashMap<String, List<String>> parameterMap = new HashMap<>();

        if (ServletFileUpload.isMultipartContent(request)) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            try {
                FileItemIterator iterator = upload.getItemIterator(request);
                while (iterator.hasNext()) {
                    FileItemStream item = iterator.next();
                    InputStream stream = item.openStream();

                    if (item.isFormField()) {
                        String value = Streams.asString(stream);
                        if (!parameterMap.containsKey(item.getFieldName()))
                            parameterMap.put(item.getFieldName(), new ArrayList<>());
                        parameterMap.get(item.getFieldName()).add(value);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            for (String k : request.getParameterMap().keySet()) {
                parameterMap.put(k, Arrays.asList(request.getParameterMap().get(k)));
            }
        }

        return parameterMap;
    }
}
