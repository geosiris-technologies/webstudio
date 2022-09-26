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

import com.geosiris.energyml.utils.EPCGenericManager;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.webstudio.logs.LogMessage;
import com.geosiris.webstudio.logs.LogResqmlVerification;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.servlet.Editor;
import com.geosiris.webstudio.servlet.global.GetAdditionalObjects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ResqmlVerification {
    public static Logger logger = LogManager.getLogger(ResqmlVerification.class);

    public static boolean attributeIsMendatory(Field f) {
        for (Annotation a : f.getAnnotations()) {
            // System.out.prinC3D-Exporttln("#verifyMendatoryAttributes " + a.toString());
            if (a.toString().toLowerCase().contains("required=true")) {
                return true;

            }
        }
        return false;
    }

    public static boolean attributeIsMendatory(Class<?> objClass, String fieldName) {
        if (objClass != null) {
            for (Field f : ObjectController.getAllFields(objClass)) {
                if (f.getName().compareTo(fieldName) == 0) {
                    return attributeIsMendatory(f);
                }
            }
        }
        return false;
    }

    private static List<LogMessage> verifyMendatoryAttributes(ObjectTree tree,
                                                              String rootUUID,
                                                              String rootCitationTitle,
                                                              String rootType) {
        // int coutError = 0;
        List<LogMessage> messages = new ArrayList<>();

        if (tree.getDataClass() != null
                // et si on est pas sur des fils null (qui ont soit une valeur soit des
                // sous-element (attributs ou properties)
                && (tree.getAttributes().size() > 0 || tree.getProperties().size() > 0 || tree.getData() != null)) {
            // logger.info("#verifyMendatoryAttributes -- Obj Class : " +
            // tree.getDataClass().getName());
            List<Field> classFields = ObjectController.getAllFields(tree.getDataClass());
            for (Field f : classFields) {
                if (attributeIsMendatory(f)) {
                    Object fieldValue = ObjectController.getObjectAttributeValue(tree.getData(), f.getName());
                    if (fieldValue == null) {
                        // logger.info("Required field '" + f.getName()
                        // + "' for object '" + rootUUID + "' with path " + tree.getName() + " data was
                        // " + tree.getData());
                        messages.add(new LogResqmlVerification("Missing Field",
                                "Required field '" + f.getName() + "' at path " + tree.getName(),
                                rootUUID,
                                rootCitationTitle,
                                rootType,
                                ServerLogMessage.MessageType.ERROR));

                    }
                }
            }
            for (ObjectTree subTree : tree.getAttributes()) {
                messages.addAll(verifyMendatoryAttributes(subTree, rootUUID, rootCitationTitle, rootType));
            }
        }

        // logger.info("Nb error found for " + rootUUID + " : " + coutError);
        return messages;
    }

    private static List<LogMessage> verifyActivityParameters(ObjectTree tree,
                                                             String rootUUID,
                                                             String rootCitationTitle,
                                                             String rootType,
                                                             Map<String, Object> resqmlObjects) {
        List<LogMessage> messages = new ArrayList<>();
        String dataClassNameLower = rootType.toLowerCase();
        if (dataClassNameLower.compareTo("activity") == 0
                || dataClassNameLower.compareTo("objactivity") == 0) {
            // si c'est une activity
            String parentTemplateUUID = null;
            try {
                parentTemplateUUID = tree.getAttribute("activitydescriptor", false).getProperty("uuid", false).getData()
                        + "";
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                logger.error(tree.getAttribute("activitydescriptor", false));
                tree.getAttributes().forEach(ttt -> logger.error("ttt " + ttt.getName()));
                tree.getAttribute("activitydescriptor", false).getProperties()
                        .forEach(ttt -> logger.error("ppp " + ttt.getName()));
            }

            if (parentTemplateUUID != null && resqmlObjects.containsKey(parentTemplateUUID)) {
                Object activityTemplate = resqmlObjects.get(parentTemplateUUID);
                ObjectTree actTemplateTree = ObjectTree.createTree(activityTemplate);

                List<ObjectTree> templateParams = new ArrayList<>();
                try {
                    templateParams.addAll(actTemplateTree.getAttribute("parameter", false).getAttributes());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                List<ObjectTree> activityParms = new ArrayList<>();
                try {
                    activityParms.addAll(tree.getAttribute("parameter", false).getAttributes());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                List<String> templatedParamNames = new ArrayList<>();
                for (ObjectTree templParam : templateParams) {
                    try {
                        String templateParamTitle = templParam.getProperty("title", false).getData() + "";
                        templatedParamNames.add(templateParamTitle);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                String templatedParamNamesString = String.join(",", templatedParamNames);

                for (ObjectTree actParam : activityParms) {
                    // Test du title si existe dans les template :
                    String paramTitle = null;
                    try {
                        paramTitle = actParam.getProperty("title", false).getData() + "";
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }

                    if (paramTitle != null) {
                        boolean foundTemplatedParameter = false;
                        for (String templParamTitle : templatedParamNames) {
                            if (paramTitle.compareTo(templParamTitle) == 0) {
                                foundTemplatedParameter = true;
                                break;
                            }
                        }
                        if (!foundTemplatedParameter) {
                            messages.add(new LogResqmlVerification("Wrong field value for '" + actParam.getName() + "'",
                                    "No templated parameter is called '" + paramTitle + "' "
                                            + ", it should be one of [" + templatedParamNamesString + "]",
                                    rootUUID,
                                    rootCitationTitle,
                                    rootType,
                                    ServerLogMessage.MessageType.WARNING));
                        }
                    }

                    /*
                     * // Finalement les index ne sont pas ceux dans les template mais l'index du
                     * parametre courant si
                     * // la cardinalitÃÂ© du parameterTemplate est > 1
                     * Integer paramIdx = null;
                     * try {
                     * paramIdx = Integer.parseInt(actParam.getProperty("index",
                     * false).getData()+"");
                     * }catch (Exception e) {logger.error(e.getMessage(), e);}
                     *
                     * if(paramIdx==null || paramIdx<0 || templateParams.size()<=paramIdx) {
                     * messages.add(new LogResqmlVerification("Missing Field",
                     * "Wrong index, '" + actParam.getName() + "' "
                     * + " ActivityTemplate parameters count found : '" + templateParams.size()+"'",
                     * rootUUID,
                     * rootCitationTitle,
                     * rootType));
                     * }else{
                     * // Test du title
                     * String templateParamTitle = "";
                     * try {
                     * templateParamTitle = templateParams.get(paramIdx).getProperty("title",
                     * false).getData()+"";
                     * }catch (Exception e) {logger.error(e.getMessage(), e);}
                     *
                     *
                     * String actParamTitle = "";
                     * try {
                     * actParamTitle = actParam.getProperty("title", false).getData()+"";
                     * }catch (Exception e) {logger.error(e.getMessage(), e);}
                     *
                     * if(actParamTitle.compareTo(templateParamTitle)!=0) {
                     * messages.add(new LogResqmlVerification("Wrong field value",
                     * "Wrong title for activity parameter'" + actParam.getName() + "' "
                     * + ", it should be '" + templateParamTitle + "'",
                     * rootUUID,
                     * rootCitationTitle,
                     * rootType));
                     * }
                     * }
                     */
                }
            }
        } else if (dataClassNameLower.compareTo("activitytemplate") == 0
                || dataClassNameLower.compareTo("objactivitytemplate") == 0) {

            List<ObjectTree> templateParams = new ArrayList<>();
            try {
                templateParams.addAll(tree.getAttribute("parameter", false).getAttributes());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            for (ObjectTree actParam : templateParams) {
                // Test du minOccur et maxOccur
                Long minOc = null;
                try {
                    minOc = Long.parseLong(actParam.getProperty("MinOccurs", false).getData() + "");
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                Long maxOc = null;
                try {
                    maxOc = Long.parseLong(actParam.getProperty("MaxOccurs", false).getData() + "");
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                if (minOc < 0) {
                    messages.add(new LogResqmlVerification("Wrong field value for '" + actParam.getName() + "'",
                            "MinOccur should be greater than 0 but value is set to '" + minOc + "' ",
                            rootUUID,
                            rootCitationTitle,
                            rootType,
                            ServerLogMessage.MessageType.WARNING));
                }
                if (maxOc < -1) {
                    messages.add(new LogResqmlVerification("Wrong field value for '" + actParam.getName() + "'",
                            "MaxOccur should be greater than 0 (or -1 for infinite) but value is set to '" + minOc
                                    + "' ",
                            rootUUID,
                            rootCitationTitle,
                            rootType,
                            ServerLogMessage.MessageType.WARNING));
                }
                if (maxOc >= 0 && minOc > maxOc) {
                    messages.add(new LogResqmlVerification("Wrong field value for '" + actParam.getName() + "'",
                            "MaxOccur should be greater than MinOccur '" + minOc + "' ",
                            rootUUID,
                            rootCitationTitle,
                            rootType,
                            ServerLogMessage.MessageType.WARNING));
                }
            }
        }
        return messages;
    }

    private static List<LogMessage> verifyReferencedDOR(
            ObjectTree objTree,
            String rootUUID,
            String rootCitationTitle,
            String rootType,
            Map<String, Object> resqmlObjects) {


        final List<ObjectTree> referencedDOR = objTree.filterListByObjectType("DataObjectReference", false, true);

        return referencedDOR.parallelStream().map( (dor) -> {
            List<LogMessage> messages = new ArrayList<>();
            Object refUuid = dor.getData("uuid");
            if (refUuid != null) {
                String rootTitle = objTree.getData("Citation.Title") + "";

                if (!resqmlObjects.containsKey(refUuid)) {
                    // On a pas trouve dans les objects du workspace, on cherche dans les objets
                    // pre-charges:
                    boolean foundInAdditional = false;
                    if (GetAdditionalObjects.ADDITIONAL_ENERGYML_OBJECTS != null) {
                        for (String uuid : GetAdditionalObjects.ADDITIONAL_ENERGYML_OBJECTS.keySet()) {
                            if (foundInAdditional || uuid.compareTo(refUuid + "") == 0) {
                                foundInAdditional = true;
                                break;
                            } else {
                                // On cherche dans l'objet si c'est un dictionary
                                Object additionalObj = GetAdditionalObjects.ADDITIONAL_ENERGYML_OBJECTS.get(uuid);
                                if (additionalObj.getClass().getSimpleName().toLowerCase().endsWith("dictionary")) {
                                    for (Method method : additionalObj.getClass().getMethods()) {
                                        // logger.info("[ADDITIONAL Object] method " + method.getName()
                                        // + " returns : " + method.getReturnType());
                                        if (Collection.class.isAssignableFrom(method.getReturnType())) {
                                            // C'est une collection
                                            try {
                                                Collection<?> collect = (Collection<?>) method.invoke(additionalObj);
                                                for (Object c_obj : collect) {
                                                    // .ObjectController.getObjectAttributeValue(c_obj, "Uuid"));
                                                    if (EPCGenericManager.isRootClass(c_obj.getClass())) {
                                                        String c_obj_uuid = (String) ObjectController.getObjectAttributeValue(c_obj, "Uuid");
                                                        if (c_obj_uuid.compareTo(refUuid + "") == 0) {
                                                            foundInAdditional = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            } catch (IllegalAccessException | IllegalArgumentException
                                                     | InvocationTargetException e) {
                                                logger.error(e.getMessage(), e);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!foundInAdditional) {
                        messages.add(new LogResqmlVerification("DOR reference missing",
                                "Object is referencing an unkown uuid with value " + refUuid
                                        + " sub object is at path : " + dor.getName() + " and supposed title is '"
                                        + dor.getData("title") + "'",
                                rootUUID,
                                rootTitle,
                                rootType,
                                ServerLogMessage.MessageType.INFO));
                    }
                } else {
                    Object referencedObj = resqmlObjects.get(refUuid);
                    String refObjTitle = (String) ObjectController.getObjectAttributeValue(referencedObj, "Citation.Title");
                    String refObjContentType = EPCGenericManager.getObjectContentType(referencedObj);
                    String refObjQualifiedType = EPCGenericManager.getObjectQualifiedType(referencedObj);

                    String title = (String) dor.getData("title");
                    try {
                        String contentType = (String) dor.getData("contenttype");
                        String qualifiedType = (String) dor.getData("qualifiedType");
                        if (contentType == null && qualifiedType == null) {
                            messages.add(new LogResqmlVerification("DOR reference has wrong information",
                                    "Empty contentType/qualifiedType for DOR at path : " + dor.getName(),
                                    rootUUID,
                                    rootTitle,
                                    rootType,
                                    ServerLogMessage.MessageType.WARNING));
                        } else {
                            if (ObjectController.hasAttribute (dor.getData(), "contentType") && (contentType != null && contentType.compareTo(refObjContentType) != 0)) {
                                messages.add(new LogResqmlVerification("DOR reference has wrong information",
                                        "Referenced object content type should be '" + refObjContentType + "' and not '"
                                                + contentType + "' at path : " + dor.getName(),
                                        rootUUID,
                                        rootTitle,
                                        rootType,
                                        ServerLogMessage.MessageType.WARNING));
                            }else if (ObjectController.hasAttribute (dor.getData(), "qualifiedType") && (qualifiedType != null && qualifiedType.compareTo(refObjQualifiedType) != 0)) {
                                messages.add(new LogResqmlVerification("DOR reference has wrong information",
                                        "Referenced object qualified type should be '" + refObjQualifiedType + "' and not '"
                                                + qualifiedType + "' at path : " + dor.getName(),
                                        rootUUID,
                                        rootTitle,
                                        rootType,
                                        ServerLogMessage.MessageType.WARNING));
                            }
                        }
                        if (refObjTitle.compareTo(title) != 0) {
                            messages.add(new LogResqmlVerification("DOR reference has wrong information",
                                    "Referenced object title is '" + refObjTitle + "' and not '" + title
                                            + "' at path : " + dor.getName(),
                                    rootUUID,
                                    rootTitle,
                                    rootType,
                                    ServerLogMessage.MessageType.WARNING));
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            return messages;
        }).flatMap(List::stream).collect(Collectors.toList());
    }

    private static List<LogMessage> verifySingleCollectionAssociationHomogeneity(
            Object resqmlObject,
            ObjectTree objectTree,
            String rootUUID,
            String rootTitle,
            String rootType) {
        List<LogMessage> messages = new ArrayList<>();

        if (resqmlObject.getClass().getSimpleName()
                .compareToIgnoreCase("CollectionsToDataobjectsAssociationSet") == 0) {
//            List<Object> singleList = (List<Object>) ObjectController.getObjectAttributeValue(resqmlObject,
//                    ".SingleCollectionAssociation");

            logger.error("Reprendre vérification homogeneousDatatype");
            messages.add(new LogResqmlVerification("### CODE ### ",
                    "Reprendre vérification homogeneousDatatype",
                    rootUUID,
                    rootTitle,
                    rootType,
                    ServerLogMessage.MessageType.WARNING));

            // int singleIdx = 0;
            // for(Object single: singleList) {
            // String homogeneousDatatype = (String)
            // ObjectController.getObjectAttributeValue(single, "homogeneousDatatype");
            // if(homogeneousDatatype!=null && homogeneousDatatype) {
            // List<Object> dorList = (List<Object>)
            // ObjectController.getObjectAttributeValue(single, ".Dataobject");
            // String lastType = null;
            //
            // for(Object dor: dorList) {
            // String contentType = (String) ObjectController.getObjectAttributeValue(dor,
            // "ContentType");
            // Matcher m = pattern_contentType_type.matcher(contentType);
            // if(m.find()) {
            // String dorType = m.group(1);
            // if(dorType.startsWith("obj_")) {
            // dorType = dorType.substring(4);
            // }
            // if(lastType == null) {
            // lastType = dorType;
            // }else if(lastType.compareToIgnoreCase(dorType)!=0){
            // // Pas le meme type que precedement donc on leve une erreur
            // messages.add(new LogResqmlVerification( "Non homogeneous
            // SingleCollectionAssociation (number "+ singleIdx+ ") dataobjects ",
            // "Found type is '" + dorType + "' but should be '" + lastType + "'",
            // rootUUID,
            // rootTitle,
            // rootType));
            // }
            // }
            //
            // }
            // }
            // singleIdx++;
            // }
        }

        return messages;
    }
    private static List<LogMessage> verifyWithXSDSchema(Object resqmlObject){
        String objUuid = (String) ObjectController.getObjectAttributeValue(resqmlObject, "uuid");
        String objTitle = (String) ObjectController.getObjectAttributeValue(resqmlObject, "Citation.Title");
        String objType = resqmlObject.getClass().getSimpleName();

        List<LogMessage> messages = new ArrayList<>();
        try{
            Editor.pkgManager.validate(resqmlObject);
        }catch (Exception e){
            messages.add(new LogResqmlVerification("XSD validation fail",
                    "[" + objUuid + "] xsd validation failed : '" + e.getCause() + "'\n" + e.getMessage(),
                    objUuid, objTitle, objType,
                    ServerLogMessage.MessageType.INFO));
        }
        return messages;
    }

    private static List<LogResqmlVerification> correctNullEnumValues(Object resqmlObject, Map<String, Object> resqmlObjects) {
        List<LogResqmlVerification> messages = new ArrayList<>();
        // TODO: fill this function
        return messages;
    }

    private static List<LogResqmlVerification> correctDORInformation(final Object resqmlObject, final Map<String, Object> resqmlObjects) {

        final String objUuid = (String) ObjectController.getObjectAttributeValue(resqmlObject, "uuid");
        final String objTitle = (String) ObjectController.getObjectAttributeValue(resqmlObject, "Citation.Title");
        final String objType = resqmlObject.getClass().getSimpleName();

        final List<Object> dors = ObjectController.findSubObjects(resqmlObject, "DataObjectReference", true);

        final Map<String, Object> additionalProperties = GetAdditionalObjects.getExternalProperties();

        return dors.parallelStream().map( (dor) -> {
            List<LogResqmlVerification> messages = new ArrayList<>();
            String dorUuid = (String) ObjectController.getObjectAttributeValue(dor, "uuid");

            if(dorUuid != null && (resqmlObjects.containsKey(dorUuid) || additionalProperties.containsKey(dorUuid))){
                if(additionalProperties.containsKey(dorUuid)){
                    messages.add(new LogResqmlVerification("DOR reference to property dictionary",
                            "Object is referencing a property of the common dictionary " + dorUuid,
                            objUuid, objTitle, objType,
                            ServerLogMessage.MessageType.LOG));
                }


                // DOR infos
                String dorTitle = (String) ObjectController.getObjectAttributeValue(dor, "Title");
                String dorVersion = (String) ObjectController.getObjectAttributeValue(dor, "ObjectVersion");

                // Target object infos
                Object dorTarget;
                if(resqmlObjects.containsKey(dorUuid)){
                    dorTarget = resqmlObjects.get(dorUuid);
                }else{
                    dorTarget = additionalProperties.get(dorUuid);
                }
                boolean modificationOccured = false;

                String targetTitle = (String) ObjectController.getObjectAttributeValue(dorTarget, "Citation.Title");
                String targetVersion = (String) ObjectController.getObjectAttributeValue(dorTarget, "ObjectVersion");

                if(ObjectController.hasAttribute(dor, "ContentType")){
                    String dorType = (String) ObjectController.getObjectAttributeValue(dor, "ContentType");
                    String targetType = EPCGenericManager.getObjectContentType(dorTarget);
                    if(dorType == null || dorType.compareTo(targetType) != 0){
                        modificationOccured = true;
                        try {
                            ObjectController.editObjectAttribute(dor, "ContentType", EPCGenericManager.getObjectContentType(dorTarget));
                        } catch (Exception e) {logger.debug(e.getMessage(), e);}
                    }
                }
                if(ObjectController.hasAttribute(dor, "QualifiedType")){
                    String dorType = (String) ObjectController.getObjectAttributeValue(dor, "QualifiedType");
                    String targetType = EPCGenericManager.getObjectQualifiedType(dorTarget);
                    if(dorType == null || dorType.compareTo(targetType) != 0){
                        modificationOccured = true;
                        try {
                            ObjectController.editObjectAttribute(dor, "QualifiedType", EPCGenericManager.getObjectQualifiedType(dorTarget));
                        } catch (Exception e) {logger.debug(e.getMessage(), e);}
                    }
                }
                if(dorTitle == null || dorTitle.compareTo(targetTitle) != 0){
                    modificationOccured = true;
                    try {
                        ObjectController.editObjectAttribute(dor, "Title", targetTitle);
                    } catch (Exception e) {logger.debug(e.getMessage(), e);}
                }
                if(targetVersion != null && (dorVersion == null || dorVersion.compareTo(targetVersion) != 0)){
                    try {
                        ObjectController.editObjectAttribute(dor, "ObjectVersion", targetVersion);
                        modificationOccured = true;
                    } catch (Exception ignore){}
                }

                if(modificationOccured){
                    messages.add(new LogResqmlVerification("DOR updated",
                            "DOR targeting " + dorUuid + "[" + targetTitle + "] updated",
                            objUuid, objTitle, objType,
                            ServerLogMessage.MessageType.INFO));
                }

            }else{
                messages.add(new LogResqmlVerification("DOR reference missing",
                        "Object is referencing an unknown uuid (not in the workspace) with value " + dorUuid,
                        objUuid, objTitle, objType,
                        ServerLogMessage.MessageType.INFO));
            }
            return messages;
        }).flatMap(List::stream).collect(Collectors.toList());
    }

    public static List<LogMessage> doVerification(final Map<String, Object> in_resqmlObjects) {
        List<LogMessage> messages = new ArrayList<>();
        Map<String, Object> resqmlObjects = new HashMap<>(in_resqmlObjects);

        for (String rootUUID : resqmlObjects.keySet()) {
            messages.addAll(doVerification(rootUUID, resqmlObjects));
        }
        return messages;
    }

    public static List<LogMessage> doVerification(String objUUID, final Map<String, Object> in_resqmlObjects) {
        List<LogMessage> messages = new ArrayList<>();

        Map<String, Object> resqmlObjects = new HashMap<>(in_resqmlObjects);

        Object resqmlObj = resqmlObjects.get(objUUID);

        ObjectTree objTree = ObjectTree.createTree(resqmlObj);

        String objTitle = "";
        try {
            objTitle = objTree.subTree("Citation.Title").getData() + "";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        String objType = resqmlObj.getClass().getSimpleName();

        // Verifications des DOR
        messages.addAll(verifyWithXSDSchema(resqmlObj));

        // Verifications des DOR
        messages.addAll(verifyReferencedDOR(objTree, objUUID, objTitle, objType, resqmlObjects));

        // Verification des Mandatories
        messages.addAll(verifyMendatoryAttributes(objTree, objUUID, objTitle, objType));

        // Verifications des parametres d'activity
        messages.addAll(verifyActivityParameters(objTree, objUUID, objTitle, objType, resqmlObjects));

        // Verification de l'homogeneite des object dans la collectionAssociation
        messages.addAll(verifySingleCollectionAssociationHomogeneity(resqmlObj, objTree, objUUID, objTitle, objType));

        return messages;
    }

    public static List<LogResqmlVerification> doCorrection(Map<String, Object> resqmlObjects) {
        List<LogResqmlVerification> messages = new ArrayList<>();

        for (String rootUUID : resqmlObjects.keySet()) {
            messages.addAll(doCorrection(rootUUID, resqmlObjects));
        }
        return messages;
    }

    public static List<LogResqmlVerification> doCorrection(String objUUID, Map<String, Object> resqmlObjects) {
        List<LogResqmlVerification> messages = new ArrayList<>();
        if(resqmlObjects.containsKey(objUUID)){
            return correctDORInformation(resqmlObjects.get(objUUID), resqmlObjects);
        }
        return messages;
    }

    public static List<LogResqmlVerification> doRemoveVersionString(Object resqmlObj, Map<String, Object> resqmlObjects) {
        List<LogResqmlVerification> messages = new ArrayList<>();

        String objTitle = "";
        try {
            objTitle = ObjectController.getObjectAttributeValue(resqmlObj, ".Citation.Title") + "";
        } catch (Exception ignore){}

        String objUUID = "";
        try {
            objUUID = ObjectController.getObjectAttributeValue(resqmlObj, ".Uuid") + "";
        } catch (Exception ignore){}

        try {
            Object citation = ObjectController.getObjectAttributeValue(resqmlObj, ".Citation");
            if (ObjectController.getObjectAttributeValue(citation, ".VersionString") != null) {
                messages.add(new LogResqmlVerification("Removing VersionString", "", objUUID, objTitle, resqmlObj.getClass().getSimpleName(),
                        ServerLogMessage.MessageType.INFO));
                try {
                    ObjectController.getAttributeEditMethod(citation, "VersionString").invoke(citation, (Object) null);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (NullPointerException e) {
            logger.debug(e.getMessage(), e);
        }

        ObjectTree objTree = ObjectTree.createTree(resqmlObj);
        List<ObjectTree> referencedDOR = objTree.filterListByObjectType("DataObjectReference", false, true);
        for (ObjectTree dor : referencedDOR) {
            if (dor.getProperty("versionString", false).getData() != null) {
                try {
                    messages.add(new LogResqmlVerification("Removing VersionString", "", objUUID, objTitle, resqmlObj.getClass().getSimpleName(),
                            ServerLogMessage.MessageType.INFO));
                    ObjectController.editObjectAttribute(dor.getData(), "VersionString", null);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return messages;
    }

    public static List<LogResqmlVerification> doRemoveVersionString(String objUUID, Map<String, Object> resqmlObjects) {
        return doRemoveVersionString(resqmlObjects.get(objUUID), resqmlObjects);
    }

    public static List<LogResqmlVerification> doRemoveVersionString(Map<String, Object> resqmlObjects) {
        List<LogResqmlVerification> messages = new ArrayList<>();

        for (String rootUUID : resqmlObjects.keySet()) {
            messages.addAll(doRemoveVersionString(rootUUID, resqmlObjects));
        }
        return messages;
    }


    public static List<LogResqmlVerification> doCorrectSchemaVersion(Object resqmlObj, Map<String, Object> resqmlObjects) {
        List<LogResqmlVerification> messages = new ArrayList<>();

        String objTitle = "";
        try {
            objTitle = ObjectController.getObjectAttributeValue(resqmlObj, ".Citation.Title") + "";
        } catch (Exception ignore){}

        String objUUID = "";
        try {
            objUUID = ObjectController.getObjectAttributeValue(resqmlObj, ".Uuid") + "";
        } catch (Exception ignore){}

        String schemaVersion = EPCGenericManager.getSchemaVersion(resqmlObj, true);

        try {
            String currentSchemaVersion = (String) ObjectController.getObjectAttributeValue(resqmlObj, ".SchemaVersion");
            if (currentSchemaVersion == null || currentSchemaVersion.compareToIgnoreCase(schemaVersion) != 0) {
                messages.add(new LogResqmlVerification("Correcting SchemaVersion", "Changing '" + currentSchemaVersion + "' to '" + schemaVersion + "'", objUUID, objTitle, resqmlObj.getClass().getSimpleName(),
                        ServerLogMessage.MessageType.INFO));
                ObjectController.getAttributeEditMethod(resqmlObj, "SchemaVersion").invoke(resqmlObj, schemaVersion);
            }
        } catch (Exception ignore){}

        return messages;
    }


    public static List<LogResqmlVerification> doCorrectSchemaVersion(String objUUID, Map<String, Object> resqmlObjects) {
        return doCorrectSchemaVersion(resqmlObjects.get(objUUID), resqmlObjects);
    }

    public static List<LogResqmlVerification> doCorrectSchemaVersion(Map<String, Object> resqmlObjects) {
        List<LogResqmlVerification> messages = new ArrayList<>();

        for (String rootUUID : resqmlObjects.keySet()) {
            messages.addAll(doCorrectSchemaVersion(rootUUID, resqmlObjects));
        }
        return messages;
    }
}
