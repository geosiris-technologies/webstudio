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
import com.geosiris.webstudio.utils.ObjectTree;
import com.geosiris.webstudio.utils.SessionUtility;
import com.geosiris.webstudio.utils.Utility;
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
@WebServlet("/ResqmlObjectTree")
public class ResqmlObjectTree extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(ResqmlObjectTree.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResqmlObjectTree() {
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

        Map<String, Object> map = SessionUtility.getResqmlObjects(session);
        String objectJSONTree = "";

        String uuid = request.getParameter("uuid");
        if (uuid != null) {
            if (map.containsKey(uuid)) {
                String subPath = request.getParameter("path");
                if (subPath == null)
                    subPath = "";
                ObjectTree objTree = ObjectTree.createTree(map.get(uuid), subPath);
                if (objTree != null)
                    objectJSONTree = objTree.toJSON();
                else {
                    logger.error("Null json " + ObjectTree.createTree(map.get(uuid)).toJSON());
                }
                logger.debug("request on : /ResqmlObjectTree?uuid=" + uuid);
                logger.debug(objTree.toJSON());
            }
        } else {
            // si pas d'uuid on envoie la liste des objets de l'epc
            Integer pageNum = null;
            try{
                pageNum = Integer.parseInt(request.getParameter("page"));
            }catch (Exception ignore){}
            Integer pageSize = null;
            try{
                pageSize = Integer.parseInt(request.getParameter("pageSize"));
            }catch (Exception ignore){}

            String attributeSort = request.getParameter("attributeSort");

            logger.info("Only page " + pageNum + "/" + pageSize + " sorted by " + attributeSort);
            logger.info("map.values() size " + map.size());

            List<Object> objList = new ArrayList<>(map.values());
            if(attributeSort != null){
                objList.sort((a, b) -> {
                    String va = (String) ObjectController.getObjectAttributeValue(a, attributeSort);
                    String vb = (String) ObjectController.getObjectAttributeValue(b, attributeSort);
                    if(va==null){va = "";}
                    if(vb==null){vb = "";}
                    return va.compareTo(vb);
                });
            }

            if(pageNum !=null && pageSize != null){
                objList = objList.subList(pageNum*pageSize, Math.min(pageNum*pageSize + pageSize, objList.size()));
            }

            objectJSONTree = Utility.getEPCContentAsJSON(new ArrayList<>(objList));
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
