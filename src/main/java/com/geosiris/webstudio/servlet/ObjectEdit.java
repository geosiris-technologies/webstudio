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

import com.geosiris.energyml.exception.NoSuchAccessibleParameterFound;
import com.geosiris.energyml.exception.NoSuchEditableParameterFound;
import com.geosiris.energyml.pkg.EPCPackage;
import com.geosiris.energyml.pkg.EPCPackageManager;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.energyml.utils.Utils;
import com.geosiris.webstudio.property.ConfigurationType;
import com.geosiris.webstudio.servlet.db.workspace.LoadWorkspace;
import com.geosiris.webstudio.utils.ResQMLConverter;
import com.geosiris.webstudio.utils.ResqmlObjectControler;
import com.geosiris.webstudio.utils.ResqmlObjectControler.ModficationType;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet implementation class ObjectEdit
 */
@WebServlet("/ObjectEdit")
@MultipartConfig
public class ObjectEdit extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(ObjectEdit.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ObjectEdit() {
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
        // SessionUtility.log(session, new ServerLogMessage(MessageType.TOAST, "Coucou", "Server"));
        String userName = (String) session.getAttribute(SessionUtility.SESSION_USER_NAME);

        String command = request.getParameter("command");
        if (command == null)
            command = "update";
        String uuid = request.getParameter("Root_UUID");

        String objectType = request.getParameter("type");
        String subObjectPath = request.getParameter("path");

        String answer = editObject(session, uuid, command, objectType, subObjectPath, request.getParameterMap(),
                userName);

        PrintWriter out = response.getWriter();
        response.setContentType("application/text");
        response.setCharacterEncoding("UTF-8");
        out.write(answer);
        out.flush();
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

        if (SessionUtility.configIsMoreVerborseThan(ConfigurationType.debug)) {
            logger.debug("##=== REQ property ");
            Enumeration<String> enReqHeader = request.getHeaderNames();
            while(enReqHeader.hasMoreElements()){
                String hn = enReqHeader.nextElement();
                logger.debug(hn +  " ==> " + request.getHeader(hn));
            }
            logger.debug("##================");
        }

        HttpSession session = request.getSession(false);
        String userName = (String) session.getAttribute(SessionUtility.SESSION_USER_NAME);

        logger.debug("Encode : " + request.getCharacterEncoding());
        String command = request.getParameter("command");
        if (command == null)
            command = "update";
        String uuid = request.getParameter("Root_UUID");

        logger.debug("Rootuuid : " + uuid);

        String objectType = request.getParameter("type");
        String subObjectPath = request.getParameter("path");

        // logger.info(request.getParameterMap());

        String answer = editObject(session, uuid, command, objectType, subObjectPath, request.getParameterMap(),
                userName);

        logger.debug("objectType '" + objectType + "' subObjectPath '" + subObjectPath + "' map '"
                + request.getParameterMap().keySet() + "'");

        PrintWriter out = response.getWriter();
        response.setContentType("application/text");
        response.setCharacterEncoding("UTF-8");
        out.write(answer);
        out.flush();
    }

    public String editObject(HttpSession session, String rootUUID, String command, String type, String subPath,
                             Map<String, String[]> parameterMap, String userName) {
        if (userName == null || userName.length() <= 0) {
            userName = EPCPackageManager.DEFAULT_CITATION_ORIGINATOR;
        }

        Map<String, Object> map = SessionUtility.getResqmlObjects(session);
        if (map == null) {
            map = new HashMap<>();
        }
        if (SessionUtility.configIsMoreVerborseThan(ConfigurationType.debug))
            logger.debug("commande d'objectEdit : " + command + " root uuid : " + rootUUID);

        Object resqmlObj = null;
        if (rootUUID != null)
            resqmlObj = map.get(rootUUID);

        String response = "Object edit failed '" + rootUUID + "' " + resqmlObj;

        if (command.toLowerCase().compareTo("update") == 0) { // edition
            XMLGregorianCalendar now = Utils.getCalendarForNow();
            if (resqmlObj != null) {
                for (String key : parameterMap.keySet()) {
                    // logger.info("P : " + key + " -> " + parameterMap.get(key));

                    if (key.compareTo("Root_UUID") != 0 && key.compareTo("command") != 0) {
                        if (SessionUtility.configIsMoreVerborseThan(ConfigurationType.debug))
                            logger.debug("#param : " + rootUUID + "/" + key + " = " + parameterMap.get(key)[0]);
                        for (int p_k_i = 0; p_k_i < parameterMap.get(key).length; p_k_i++) {
                            try {
                                if (SessionUtility.configIsMoreVerborseThan(ConfigurationType.debug))
                                    logger.debug(p_k_i + ") parameter " + key);
                                ResqmlObjectControler.modifyResqmlObjectFromParameter(resqmlObj, key,
                                        ModficationType.EDITION, parameterMap.get(key)[p_k_i], map);
                                if (SessionUtility.configIsMoreVerborseThan(ConfigurationType.debug))
                                    logger.debug(resqmlObj.getClass().getName() + " - "
                                            + resqmlObj.getClass().getName().toLowerCase().endsWith("activity") + " - "
                                            + key.toLowerCase().endsWith("activitydescriptor"));

                                // If an activity is linked to an activityTemplate, we try to create attribute
                                // in the activity
                                if (resqmlObj.getClass().getName().toLowerCase().endsWith("activity")
                                        && key.toLowerCase().endsWith("activitydescriptor")) {
                                    Object activityTemplate = map.get(parameterMap.get(key)[p_k_i]);
                                    ResqmlObjectControler.prefillActivityFromTemplate(activityTemplate, resqmlObj);
                                }
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    } else {
                        if (SessionUtility.configIsMoreVerborseThan(ConfigurationType.debug))
                            logger.debug("#param : " + rootUUID + "/" + key);
                    }
                }
                response = "Object edited : '" + rootUUID + "'";
                try {
                    ResqmlObjectControler.modifyResqmlObjectFromParameter(resqmlObj, ".Citation.LastUpdate",
                            ModficationType.EDITION, now.toString(), map);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                SessionUtility.getWorkspaceContent(session).setReadObjects(map);
                LoadWorkspace.updateWorkspace(session, rootUUID);
            }
        } else if (command.toLowerCase().compareTo("create") == 0) {

            if (subPath == null) { // On crée un nouvel objet racine
                logger.debug("#creation of root object : " + type);

                EPCPackage pkg = Editor.pkgManager.getMatchingPackage(type);
                if (pkg != null) {
                    try {
                        Class<?> objClass = Class.forName(type);
                        logger.debug("creating object '" + objClass + "'");
                        if (pkg.isRootClass(objClass)) {
                            Object newObj = Editor.pkgManager.createInstance(type, map, null, null, userName, false);
                            String objUuid = ObjectController.getObjectAttributeValue(newObj, "Uuid") + "";
                            map.put(objUuid, newObj);
                            SessionUtility.getWorkspaceContent(session).setReadObjects(map);
                            response = "Object created with uuid " + objUuid;
                            LoadWorkspace.updateWorkspace(session, objUuid);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                } else {
                    logger.error("#ERR no matching package for '" + type + "'");
                }
            } else if (type != null) { // On cree des sous-elements de type objectType
                Class<?> objClassToCreate = null;
                try {
                    objClassToCreate = Class.forName(type);
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage(), e);
                }

                try {
                    ObjectController.createObjectAttribute(resqmlObj, subPath, objClassToCreate);
                } catch (NoSuchAccessibleParameterFound | NoSuchEditableParameterFound | InvocationTargetException | IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                }
                logger.debug("creation of sub elt : " + subPath + "[" + objClassToCreate + "] for object " + rootUUID);
            }
            // TODO : application/x-hdf5 par defaut dans mimetype de ExternalPartRef

        } else if (command.toLowerCase().compareTo("copy") == 0) {
            String copyVersion = ObjectController.getObjectAttributeValue(resqmlObj, "SchemaVersion") + "";
            if (parameterMap.containsKey("version")) {
                String versionParam = parameterMap.get("version")[0];
                if (versionParam != null) {
                    copyVersion = versionParam;
                }
            }
            logger.debug("trying copy with schemaVersion '" + copyVersion + "'");
            if (resqmlObj != null) {
                try {
                    Object copyObj = ResQMLConverter.getCopy(resqmlObj, copyVersion, map);
                    String copyUuid = ObjectController.getObjectAttributeValue(copyObj, "Uuid") + "";
                    if (copyObj != null) {
                        map.put(copyUuid, copyObj);
                        SessionUtility.getWorkspaceContent(session).setReadObjects(map);
                        response = "Object created with uuid " + copyUuid;
                        LoadWorkspace.updateWorkspace(session, copyUuid);
                        logger.debug("Copy of " + rootUUID + " has uuid " + copyUuid);
                    } else {
                        logger.error("La copie de " + rootUUID + " n'a pas fonctionnée");
                    }

                } catch (Exception exceptCopy) {
                    exceptCopy.printStackTrace();
                }
            }
        } else if (command.toLowerCase().compareTo("delete") == 0) {
            if (SessionUtility.configIsMoreVerborseThan(ConfigurationType.debug))
                logger.debug("#suppression : " + rootUUID);
            if (subPath == null) { // si pas de subpath on supprime l'objet
                map.remove(rootUUID);
                LoadWorkspace.removeFileFromWorkspace(session, rootUUID);
                response = "Object removed : '" + rootUUID + "'";
            } else { // on supprime le sous element
                try {
                    if (SessionUtility.configIsMoreVerborseThan(ConfigurationType.debug))
                        logger.debug("#suppression subpath : " + subPath);
                    ResqmlObjectControler.modifyResqmlObjectFromParameter(resqmlObj, subPath, ModficationType.EDITION,
                            null, map);
                    // logger.debug("#new object json : " + ObjectTree.createTree(resqmlObj).toJSON());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } else if (command.toLowerCase().compareTo("movelistelement") == 0) {
            int newIdx = -1;
            if (parameterMap.containsKey("index")) {
                String indexParam = parameterMap.get("index")[0];
                if (indexParam != null) {
                    try {
                        newIdx = Integer.parseInt(indexParam);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (newIdx >= 0) {
                    if (ObjectController.displaceEltInList(resqmlObj, subPath, newIdx)) {
                        response = "List content object " + subPath + " has been moved to index " + newIdx;
                        SessionUtility.getWorkspaceContent(session).setReadObjects(map);
                        LoadWorkspace.updateWorkspace(session, rootUUID);
                    }
                }
            }
        } else {
            response = "Command not found for ObjectEdit " + command;
        }

        logger.debug("Answer to @editObject call : " + response);

        return response;
    }

}
