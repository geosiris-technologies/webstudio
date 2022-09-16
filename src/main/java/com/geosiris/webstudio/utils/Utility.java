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

import com.geosiris.energyml.pkg.EPCPackage;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.servlet.Editor;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {
    public static Logger logger = LogManager.getLogger(Utility.class);

    public static String getEPCContentAsJSON(List<Object> resqmlObjectList) {
        StringBuilder json = new StringBuilder();
        json.append("[ ");
        int cpt = 0;

        for (Object obj : resqmlObjectList) {
            String uuid = ObjectController.getObjectAttributeValue(obj, "uuid") + "";
            String title = ObjectController.getObjectAttributeValue(obj, "citation.title") + "";
            String type = obj.getClass().getSimpleName();
            String schemaVersion = ObjectController.getObjectAttributeValue(obj, "schemaVersion") + "";

            Pattern patDevPkg = Pattern.compile(".*(dev[0-9_]+)x_[.-9_]+");
            try {
                Matcher matcher = patDevPkg.matcher(Editor.pkgManager.getMatchingPackage(obj.getClass()).getClass().getName());

                if (matcher.find()) {
                    schemaVersion += " (" + matcher.group(1) + ")";
                }
            }catch (Exception e){
                logger.error(e.getMessage(), e);

                logger.debug(obj);
                logger.debug(obj.getClass());
                for(EPCPackage p: Editor.pkgManager.PKG_LIST){
                    logger.debug(p.getName());
                }
            }

            json.append("{ ");
            json.append("\"num\" : \"").append(cpt).append("\", ");
            json.append("\"title\" : ").append(transformStringForJsonCompatibility(title)).append(", ");
            json.append("\"type\" : ").append(transformStringForJsonCompatibility(type)).append(", ");
            json.append("\"uuid\" : \"").append(uuid).append("\", ");
            json.append("\"schemaVersion\" : ").append(transformStringForJsonCompatibility(schemaVersion)).append(", ");
            json.append("\"package\" : ");
            try {
                json.append(transformStringForJsonCompatibility(Editor.pkgManager.getMatchingPackage(obj.getClass()).getName()));
            }catch (Exception e){
                logger.error("Not found package for obj class " + obj.getClass());
                json.append("\"UNKNOWN\"");
            }
            json.append("}, ");
            cpt++;
        }
        if (json.lastIndexOf(",") > 0) {
            json.replace(json.lastIndexOf(","), json.lastIndexOf(",") + 1, ""); // on enlÃ¨ve la derniÃ¨re virgule
        }
        json.append(" ]");

        return json.toString();
    }


    public static <K, V> String toJson(Map<K, List<V>> map, Method keyToS, Method valToS) {
        String jsonMap = "{\n";
        for (Object key : map.keySet()) {
            jsonMap += "\t";
            if (keyToS != null) {
                try {
                    jsonMap += transformStringForJsonCompatibility(((String) keyToS.invoke(key)).toLowerCase());
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    logger.error(e.getMessage(), e);
                    jsonMap += transformStringForJsonCompatibility((key + "").toLowerCase());
                }
            } else {
                jsonMap += (key + "").toLowerCase();
            }
            jsonMap += " : \n";
            jsonMap += toJson(map.get(key), valToS) + ",";
        }
        if (jsonMap.contains(",")) {
            jsonMap = jsonMap.substring(0, jsonMap.lastIndexOf(","));
        }
        jsonMap += "}";
        return jsonMap;
    }

    public static <K, V> String toJson(Map<K, V> map) {
        String jsonMap = "{\n";
        for (Object key : map.keySet()) {
            jsonMap += "\t\"";
            jsonMap += (key + "").toLowerCase();
            jsonMap += "\" : \n";
            jsonMap += toJson(map.get(key), null);
            jsonMap += ",";
        }
        if (jsonMap.contains(",")) {
            jsonMap = jsonMap.substring(0, jsonMap.lastIndexOf(","));
        }
        jsonMap += "}";
        return jsonMap;
    }

    public static <K> String toJson(List<K> list, Method valToS) {
        String jsonMap = "[";
        int cptSub = 0;
        for (Object instanciable : list) {
            jsonMap += toJson(instanciable, valToS);
            jsonMap += ", ";
            cptSub++;
        }
        if (cptSub > 0) {
            jsonMap = jsonMap.substring(0, jsonMap.lastIndexOf(","));
        }
        jsonMap += "]";
        return jsonMap;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static String toJson(Object obj, Method valToS) {
        String jsonResult = "";
        if (obj instanceof Pair) {
            jsonResult += "[";
            jsonResult += transformStringForJsonCompatibility(((Pair) obj).l() + "");
            jsonResult += ", ";
            jsonResult += transformStringForJsonCompatibility(((Pair) obj).r() + "");
            jsonResult += "]";
        } else if (obj instanceof List) {
            return toJson((List) obj, valToS);
        }else{
            if (valToS != null) {
                try {
                    jsonResult +=  transformStringForJsonCompatibility((String) valToS.invoke(obj));
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    logger.error(e.getMessage(), e);
                    jsonResult += transformStringForJsonCompatibility("" + obj);
                }
            } else {
                jsonResult += transformStringForJsonCompatibility("" + obj);
            }
        }
        return jsonResult;
    }

    public static String transformStringForJsonCompatibility(String input){
        Gson gson =  new Gson();
        return gson.toJson(input);
    }

}
