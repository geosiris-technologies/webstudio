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

import com.geosiris.storage.cloud.api.request.GetFileRequest;
import com.geosiris.storage.cloud.api.request.UploadFileRequest;
import com.geosiris.webstudio.property.UserPreferencesProperties;
import com.geosiris.webstudio.servlet.db.workspace.LoadWorkspace;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@WebServlet("/Preferences")
public class Preferences extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(Preferences.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtility.tryConnectServlet(request, response)) {
            return;
        }
        String jsonResponse = "{}";
        HttpSession session = request.getSession(false);
        String userName = (String) session.getAttribute(SessionUtility.SESSION_USER_NAME);
        try {
            if(SessionUtility.wsProperties.getEnableUserDB() && SessionUtility.wsProperties.getEnableWorkspace()) {
                String fileContent = new String(LoadWorkspace.storageService.getFile(new GetFileRequest(getUserBucketName(userName) + ".ini", SessionUtility.PREFERENCE_FOLDER_NAME)).getContent(), StandardCharsets.UTF_8);
                UserPreferencesProperties properties = new UserPreferencesProperties(fileContent);
                jsonResponse = properties.toJson();
            }
        }catch (Exception e){
            logger.error(e.getMessage());
            logger.debug(e.getMessage(), e);
        }
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(jsonResponse);
        out.flush();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtility.tryConnectServlet(request, response)) {
            return;
        }
        StringBuilder jb = new StringBuilder();
        String line;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) { /*report an error*/ }

        logger.info("Preferences post input " + jb);

        String jsonResponse;
        HttpSession session = request.getSession(false);
        String userName = (String) session.getAttribute(SessionUtility.SESSION_USER_NAME);
        try {
            UserPreferencesProperties properties = UserPreferencesProperties.parseJson(jb.toString(), UserPreferencesProperties.class);
            LoadWorkspace.storageService.uploadFile(new UploadFileRequest(new ByteArrayInputStream(properties.toString().getBytes()),
                    "", getUserBucketName(userName)+".ini", "text/xml", SessionUtility.PREFERENCE_FOLDER_NAME));
            jsonResponse = "true";
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            jsonResponse = "false";
        }
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(jsonResponse);
        out.flush();
    }


    public static String getUserBucketName(String userName){
        return "personal-bucket-" + userName.hashCode();
    }
}
