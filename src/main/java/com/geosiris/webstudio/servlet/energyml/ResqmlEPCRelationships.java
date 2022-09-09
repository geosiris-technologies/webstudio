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

import com.geosiris.energyml.utils.EPCGenericManager;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.energyml.utils.Pair;
import com.geosiris.energyml.utils.Utils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet implementation class FirstServlet
 */
@WebServlet("/ResqmlEPCRelationship")
public class ResqmlEPCRelationships extends HttpServlet {
    private static final long serialVersionUID = -8922813586872223343L;
    public static Logger logger = LogManager.getLogger(ResqmlEPCRelationships.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResqmlEPCRelationships() {
        super();
    }


    public static String relationToJSON_withParamName(HashMap<String, Pair<List<Pair<String, String>>, List<Pair<String, String>>>> relations,
                                                      Map<String, Object> epcObjects) {
        StringBuilder jsonRelations = new StringBuilder();
        jsonRelations.append("{");
        int cpt = 0;
        for (String uuid : relations.keySet()) {
            try {
                Object resqmlObj = epcObjects.get(uuid);

                jsonRelations.append("\"" + uuid + "\" : ");
                jsonRelations.append("{");
                jsonRelations.append("\"uuid\" : \"" + uuid + "\", ");
                jsonRelations.append("\"num\" : \"" + cpt + "\", ");
                jsonRelations.append("\"firp\" : \"" + Utils.getFIRPObjectType(resqmlObj.getClass()) + "\", ");
                jsonRelations.append("\"title\" : \"" + ObjectController.getObjectAttributeValue(resqmlObj, ".Citation.Title") + "\", ");
                jsonRelations.append("\"type\" : \"" + resqmlObj.getClass().getSimpleName() + "\", ");
                jsonRelations.append("\"schemaVersion\" : \"" + ObjectController.getObjectAttributeValue(resqmlObj, ".SchemaVersion") + "\", ");


                // ========= Les relations descendantes :
                jsonRelations.append("\"relationUp\" : [");
                for (Pair<String, String> relUp : relations.get(uuid).l()) {
//					jsonRelations.append("\"" + r + "\",");
                    jsonRelations.append("{ \"uuid\" : \"" + relUp.r() + "\", \"name\" : \"" + transformParamName(relUp.l()) + "\"},");
                }
                // On enleve la virgule de fin
                if (jsonRelations.toString().endsWith(",")) {
                    jsonRelations.replace(jsonRelations.length() - 1, jsonRelations.length(), "");
                }
                jsonRelations.append("],\n");


                // ========= Les relations montantes
                jsonRelations.append("\"relationDown\" : [");
                for (Pair<String, String> relDown : relations.get(uuid).r()) {
                    jsonRelations.append("{ \"uuid\" : \"" + relDown.r() + "\", \"name\" : \"" + transformParamName(relDown.l()) + "\"},");
                }
                // On enleve la virgule de fin
                if (jsonRelations.toString().endsWith(",")) {
                    jsonRelations.replace(jsonRelations.length() - 1, jsonRelations.length(), "");
                }
                jsonRelations.append("]");


                // FIN Objet courrant
                jsonRelations.append("},");

            } catch (Exception e) {
                // exception si la clef n'existe pas, ce qui ne devrait pas arriver
                logger.error(e.getMessage(), e);
            }
            cpt++;
        }

        // On enleve la virgule de fin
        if (jsonRelations.toString().endsWith(",")) {
            jsonRelations.replace(jsonRelations.length() - 1, jsonRelations.length(), "");
        } else {
            if (jsonRelations.length() > 2) {
                logger.error(">> '" + jsonRelations.toString().substring(jsonRelations.toString().length() - 2) + "'");
            } else {
                logger.error(">> Empty relationships");
            }
        }

        jsonRelations.append("}");

        return jsonRelations.toString();
    }

    public static String transformParamName(String paramName) {
        String result = paramName;

        String[] endTo_s_list = {"edProperty", "edRepresentation", "edInterpretation", "edFeature"};
        for (String endTo_s : endTo_s_list) {
            if (result.endsWith(endTo_s)) {
                result = result.substring(0, result.length() - endTo_s.length()) + "s";
            }
        }
        return result;
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

        //			HashMap<String, Pair<List<String>, List<String>>> relations = EPCGenericManager.getEpcRelationships(map);
        //			request.setAttribute("response", relationToJSON(relations, map));

        HashMap<String, Pair<List<Pair<String, String>>, List<Pair<String, String>>>> relations = EPCGenericManager.getEpcRelationshipsWithParamName(map);

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(relationToJSON_withParamName(relations, map));
        out.flush();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
