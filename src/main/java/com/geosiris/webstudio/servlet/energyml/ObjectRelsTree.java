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

import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.webstudio.model.WorkspaceContent;
import com.geosiris.webstudio.utils.ObjectTree;
import com.geosiris.webstudio.utils.SessionUtility;
import com.geosiris.webstudio.utils.Utility;
import energyml.relationships.Relationships;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servlet implementation class ResqmlObjectTree
 */
@WebServlet("/ObjectRelsTree")
public class ObjectRelsTree extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(ObjectRelsTree.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObjectRelsTree() {
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

        WorkspaceContent wc = SessionUtility.getWorkspaceContent(session);
        String objectJSONTree = "";

        String uuid = request.getParameter("uuid");
        if (uuid != null) {
            if (!wc.getParsedRels().containsKey(uuid)) {
                wc.getParsedRels().put(uuid, new Relationships());
            }
            ObjectTree objTree = ObjectTree.createTree(wc.getParsedRels().get(uuid));
            if (objTree != null)
                objectJSONTree = objTree.toJSON();
            else {
                logger.error("Null json " + ObjectTree.createTree(wc.getParsedRels().get(uuid)).toJSON());
            }
        }
        if(objectJSONTree.length() == 0){
            objectJSONTree = "null";
        }

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(objectJSONTree);
        out.flush();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }


}
