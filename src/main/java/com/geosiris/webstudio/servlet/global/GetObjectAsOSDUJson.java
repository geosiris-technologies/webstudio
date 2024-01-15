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

import com.geosiris.energyml.utils.ObjectController;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Servlet implementation class GetObjectAsOSDUJson
 */
@WebServlet("/GetObjectAsOSDUJson")
@MultipartConfig
public class GetObjectAsOSDUJson extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(GetObjectAsOSDUJson.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetObjectAsOSDUJson() {
        super();
    }

    public static String strValue(Object value) {
        String res = value.toString();
        if (value.getClass().isEnum()) {
            res = res.substring(0, 1).toUpperCase() + res.substring(1).toLowerCase();
        }
        return res;
    }

    private static String toOSDUJson(Object obj, Map<String, Object> objMap) {
        Map<String, BiFunction<Object, Map<String, Object>, Object>> attribMapper = new HashMap<>();

        StringBuilder result = new StringBuilder("{");

        // AbstractInterpretation
        if (ObjectController.inherits(obj.getClass(), "AbstractFeatureInterpretation", false, true)) {
            attribMapper.put("OlderPossibleAge", (_obj, _ws_obj_map) -> ObjectController.getObjectAttributeValue(_obj, ".OlderPossibleAge"));
            attribMapper.put("YoungerPossibleAge", (_obj, _ws_obj_map) -> ObjectController.getObjectAttributeValue(_obj, ".YoungerPossibleAge"));
            attribMapper.put("DomainTypeID", (_obj, _ws_obj_map) -> "namespace:reference-data--DomainType:" + strValue(ObjectController.getObjectAttributeValue(_obj, ".Domain")));
            attribMapper.put("FeatureID", (_obj, _ws_obj_map) -> {
                Object feature = _ws_obj_map.get("" + ObjectController.getObjectAttributeValue(_obj, ".InterpretedFeature.Uuid"));
                String featTypeOSDU = "Local" + feature.getClass().getSimpleName();
                return "namespace:work-product-component--" + featTypeOSDU + ":" + featTypeOSDU + "-"
                        + ObjectController.getObjectAttributeValue(_obj, ".InterpretedFeature.Uuid")
                        + ":";
            });
            attribMapper.put("FeatureName", (_obj, _ws_obj_map) -> ObjectController.getObjectAttributeValue(_obj, ".InterpretedFeature.Title"));
        }

        // HorizonInterpretation
        if (ObjectController.inherits(obj.getClass(), "HorizonInterpretation", false, true)) {
            attribMapper.put("isConformableAbove", (_obj, _ws_obj_map) -> ObjectController.getObjectAttributeValue(_obj, ".isConformableAbove"));
            attribMapper.put("isConformableBelow", (_obj, _ws_obj_map) -> ObjectController.getObjectAttributeValue(_obj, ".isConformableBelow"));
            /*attribMapper.put("ChronoTop", (_obj, _ws_obj_map) -> {
                return (Boolean) ObjectController.getObjectAttributeValue(_obj, ".isConformableAbove")
                        && (Boolean) ObjectController.getObjectAttributeValue(_obj, ".isConformableBelow");
                // return true; // SI above et below sont vrais
            });
            attribMapper.put("ChronoBottom", (_obj, _ws_obj_map) -> {
                return ObjectController.getObjectAttributeValue(_obj, ".isConformableAbove");
                // return true; // Si above = True
            });*/
            attribMapper.put("SequenceStratigraphySurfaceTypeID", (_obj, _ws_obj_map) -> {
                Object objValue = ObjectController.getObjectAttributeValue(_obj, ".SequenceStratigraphySurface");
                return "namespace:reference-data--" + objValue.getClass().getSimpleName() + ":" + strValue(objValue) + ":";
            });
            attribMapper.put("HorizonStratigraphicRoleTypeID", (_obj, _ws_obj_map) -> ((Collection<?>) ObjectController.getObjectAttributeValue(_obj, ".HorizonStratigraphicRole"))
                    .stream()
                    .map((v) -> "namespace:reference-data--SequenceStratigraphySurfaceType:" + strValue(v) + ":")
                    .collect(Collectors.toList()));
        }

        if (ObjectController.inherits(obj.getClass(), "HorizonInterpretation", false, true)
                || ObjectController.inherits(obj.getClass(), "FeatureInterpretation", false, true)
                || ObjectController.inherits(obj.getClass(), "RockFluidUnitInterpretation", false, true)
        ) {
            // REF TO FEATURE
            attribMapper.put("FeatureTypeID", (_obj, _ws_obj_map) -> {
                try {
                    if (_obj.getClass().getSimpleName().contains("Horizon"))
                        return "namespace:reference-data--FeatureType:Top:";
                    else if (_obj.getClass().getSimpleName().contains("Fault"))
                        return "namespace:reference-data--FeatureType:Fault:";
                    else if (_obj.getClass().getSimpleName().contains("RockFluidUnit")) {
//                        RockFluidUnitInterpretation ru = new RockFluidUnitInterpretation();
//                        switch (ru.getPhase()) {
//                            case AQUIFER:
//                                break;
//                            case SEAL:
//                                break;
//                            case GAS_CAP:
//                                break;
//                            case OIL_COLUMN:
//                                break;
//                        }
                    } else
                        return "";
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                return "";
            });
        }
        if (ObjectController.inherits(obj.getClass(), "FaultInterpretation", false, true)) {
            attribMapper.put("MaximumFaultThrowValue", (_obj, _ws_obj_map) -> ObjectController.getObjectAttributeValue(_obj, ".MaximumThrow.value"));
            attribMapper.put("RepresentativeDipDirection", (_obj, _ws_obj_map) -> ObjectController.getObjectAttributeValue(_obj, ".MeanAzimuth.value"));
            attribMapper.put("RepresentativeDipAngle", (_obj, _ws_obj_map) -> ObjectController.getObjectAttributeValue(_obj, ".MeanDip.value"));
            attribMapper.put("IsSealed", (_obj, _ws_obj_map) -> ObjectController.getObjectAttributeValue(_obj, ".isSealed"));
        }
        /////////

        List<String> keys = new ArrayList<>(attribMapper.keySet());
        Collections.sort(keys);
        for (String attName : keys) {
            result.append("\n\t\"").append(attName).append("\": ");
            try {
                Object value = attribMapper.get(attName).apply(obj, objMap);
                if (value == null) {
                    throw new Exception("null value");
                } else {
                    String valueStr = value + "";
                    if (value instanceof String)
                        valueStr = "\"" + valueStr + "\"";

                    // TODO : tester les listes!
                    if (value instanceof Collection) {
                        result.append("[");
                        for (Object itx : ((Collection<?>) value)) {
                            result.append("\n\t\t\"").append(itx).append("\",");
                        }
                        if (result.toString().endsWith(",")) {
                            result = new StringBuilder(result.substring(0, result.length() - 1));
                        }
                        result.append("\n\t],");
                    } else {
                        result.append(valueStr).append(",");
                    }
                }
            } catch (Exception e) {
                result.append("\"\",");
            }

        }
        if (result.toString().endsWith(",")) {
            result = new StringBuilder(result.substring(0, result.length() - 1));
        }

        result.append("\n}");
        return result.toString();
    }

    public static void main(String[] argv) {
        // Boundary Feature
      /*  BoundaryFeature bf = new BoundaryFeature();

        Citation bf_cit = new Citation();
        bf_cit.setTitle("[FEAT] Test Horizon for OSDU");

        bf.setCitation(bf_cit);
        bf.setUuid(UUID.randomUUID().toString());
        bf.setIsWellKnown(false);

        // DOR
        DataObjectReference dor = new DataObjectReference();
        dor.setTitle(bf_cit.getTitle());
        dor.setUuid(bf.getUuid());

        // HorizonInterpretation
        HorizonInterpretation hi = new HorizonInterpretation();

        Citation hi_cit = new Citation();
        hi_cit.setTitle("[HOR_INT] Test Horizon for OSDU");

        hi.setCitation(hi_cit);
        hi.setUuid(UUID.randomUUID().toString());
        hi.setInterpretedFeature(dor);
//		hi.setOlderPossibleAge(17L);
//		hi.setYoungerPossibleAge(10L);
        hi.setDomain(Domain.DEPTH);
//		hi.setIsConformableAbove(true);
        hi.setIsConformableBelow(false);
        hi.setSequenceStratigraphySurface("Flooding");

        hi.getHorizonStratigraphicRole().add(HorizonStratigraphicRole.CHRONOSTRATIGRAPHIC);

        // Map D'objets
        Map<String, Object> mapObj = new HashMap<>();
        mapObj.put(bf.getUuid(), bf);
        mapObj.put(hi.getUuid(), hi);

        // Calcul final
        logger.info(toOSDUJson(hi, mapObj));*/
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

        Map<String, Object> map = SessionUtility.getResqmlObjects(session);
        String uuid = request.getParameter("uuid");
        String answer = "";

        if (uuid == null || !map.containsKey(uuid)) {
            answer = "{}";
        } else {
            Object energymlObj = map.get(uuid);
            answer = toOSDUJson(energymlObj, map);
        }
        // SessionUtility.log(session, new ServerLogMessage(MessageType.LOG, answer, SessionUtility.EDITOR_NAME));
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
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

        doGet(request, response);
    }

}
