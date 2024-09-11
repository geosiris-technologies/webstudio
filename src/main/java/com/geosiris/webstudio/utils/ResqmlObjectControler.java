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
import com.geosiris.energyml.utils.EPCGenericManager;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.energyml.utils.Pair;
import com.geosiris.energyml.utils.Utils;
import com.geosiris.etp.utils.ETPUri;
import com.geosiris.webstudio.model.WorkspaceContent;
import com.geosiris.webstudio.property.ConfigurationType;
import com.geosiris.webstudio.servlet.Editor;
import com.geosiris.webstudio.servlet.global.ResqmlAccessibleTypes;
import energyml.common2_1.Activity;
import energyml.common2_3.ActivityTemplate;
import energyml.witsml2_1.Tubular;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;


public class ResqmlObjectControler {
    public static Logger logger = LogManager.getLogger(ResqmlObjectControler.class);


    public static void modifyResqmlObjectFromParameter(HttpSession session, Object resqmlObj, String paramPath,
                                                       ModficationType modifType, Object value,
                                                       Map<String, Object> epcObjectsSource
    ) throws Exception {
        //        TODO: le code suivant ne marche pas car l'additional object est un propertyDict et donc l'uuid des porpsKind n'apparait pas directement dans la map
        //        Map<String, Object> epcObjects = new HashMap<>();
        //        epcObjects.putAll(epcObjectsSource);
        //        epcObjects.putAll(GetAdditionalObjects.ADDITIONAL_ENERGYML_OBJECTS);
        modifyResqmlObjectFromParameter(session, resqmlObj, paramPath, modifType, value, epcObjectsSource, paramPath, resqmlObj);
    }

    private static void modifyResqmlObjectFromParameter(HttpSession session, Object resqmlObj, String paramPath,
                                                       ModficationType modifType, Object value,
                                                       Map<String, Object> epcObjects,
                                                       String completePath,
                                                       Object rootObject
    ) throws Exception {

        if (resqmlObj != null) {
            Class<?> objClass = resqmlObj.getClass();
            if (paramPath.startsWith(".")) {
                paramPath = paramPath.substring(1);
            }
            //logger.info("Path : '" + paramPath  + "' value " + value);

            String simplePath = paramPath;
            if (simplePath.contains("."))
                simplePath = simplePath.substring(0, simplePath.indexOf("."));

            if (simplePath.replaceAll("[0-9]+", "").length() == 0) {    // Si on est sur un chiffre (dans l'acces d'une liste)
                int simplePathVal = Integer.parseInt(simplePath);
                if (!paramPath.contains(".")) {
                    // On est au bout du chemin
                    if (value != null) {
                        // Si la valeur n'est pas nulle, on essaie de remplacer l'element de la liste par un nouveau du mÃªme type
                        Class<?> listObjectClass = null;
                        if (value instanceof String) { // si un String en parametre, ce n'est peut etre pas le type
                            // qu'il convient de mettre dans la liste, on cherche donc le type d'objet que la liste contient

                            try {
                                Object oldObj = objClass.getMethod("get", int.class).invoke(resqmlObj, simplePathVal);
                                if (oldObj != null) {
                                    listObjectClass = oldObj.getClass();
                                } else { // si objet null on check le type dans la liste
                                    String listPath = completePath.substring(0, completePath.lastIndexOf("."));
                                    // On recupere l'objet parent de la liste, afin d'obtenir sa classe
                                    logger.info("listPath '" + listPath + "' ==> '" + listPath.substring(0, listPath.lastIndexOf(".")) + "'");
//                                    Object listParentVal = ObjectController.getObjectAttributeValue(rootObject, listPath);
                                    Object listParentVal = ObjectController.getObjectAttributeValue(rootObject, listPath.substring(0, listPath.lastIndexOf(".")));
                                    logger.info("listParentVal : '" + listParentVal + "' listPath '" + listPath + "'");
                                    listObjectClass = (Class<?>) ObjectController.getClassTemplates(listParentVal.getClass(), listPath.substring(listPath.lastIndexOf(".") + 1)).getActualTypeArguments()[0];
                                }
                                logger.info("Object class found inside list : " + listObjectClass);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                                logger.error("ERR Modification in list : objClass '" + objClass + "' "
                                        + "simplePathVal '" + simplePathVal + "'");
                            }
                        } else {    // Si on a autre chose qu'un String en paramÃ¨tre on essaie de creer un objet de meme classe
                            //							ObjectController.getClassTemplates(parentElementClass, paramName)
                            //							logger.error("cp : " + completePath + " -- " + paramPath);
                            String parentPath = completePath.substring(0, completePath.length() - paramPath.length() - 1);
                            //							logger.error("PARENT PATH : " + parentPath); // -1 pour le point
                            try {
                                // On essaie de recuperer la classe qui template la liste
                                //								logger.error(parentPath.substring(0, parentPath.lastIndexOf("."))
                                //													+" --- " + parentPath.substring(parentPath.lastIndexOf(".") + 1));
                                ParameterizedType listContentType = ObjectController.getClassTemplates(ObjectController.getObjectAttributeValue(rootObject, parentPath.substring(0, parentPath.lastIndexOf("."))).getClass(),
                                        parentPath.substring(parentPath.lastIndexOf(".") + 1));
                                //								logger.error("Type found : " + listContentType + " -- " + listContentType.getActualTypeArguments()[0]);
                                Class<?> listContentClass = (Class<?>) listContentType.getActualTypeArguments()[0];
                                listObjectClass = Editor.pkgManager.getClassFromSuperAndName(listContentClass, value.getClass());
                                //schemaVersion = EPCGenericManager.getSchemaVersionFromClassName(listContentClass.getName());
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                            if (listObjectClass == null)
                                listObjectClass = value.getClass();
                        }

                        assert listObjectClass != null;
                        Object newObject = Editor.pkgManager.createInstance(listObjectClass.getName(), epcObjects, value);
                        if (newObject != null) {
                            Method setMethod = null;
                            Class<?> objClassForSet = newObject.getClass();
                            while (setMethod == null && objClassForSet != null) {
                                try {
                                    setMethod = objClass.getMethod("set", int.class, objClassForSet);
                                } catch (Exception exceptSetMethod) {
                                    logger.debug(exceptSetMethod.getMessage(), exceptSetMethod);
                                }
                                List<Class<?>> superClassList = ObjectController.getSuperClasses(objClassForSet);
                                if (superClassList.size() > 0) {
                                    objClassForSet = superClassList.get(0);
                                } else {
                                    objClassForSet = null;
                                }
                            }
                            if (setMethod != null) {
                                while (((List) resqmlObj).size() <= simplePathVal) {
                                    ((List) resqmlObj).add(null);
                                }
                                logger.error("invoking method " + setMethod);
                                setMethod.invoke(resqmlObj, simplePathVal, newObject);
                            } else {
                                logger.error("Error while setting list element '" + newObject + "' at path " + paramPath);
                            }
                        } else {
                            logger.error("null object created for " + paramPath);
                        }
                    } else {
                        // Si on est a la fin du chemin, on supprime l'element de la liste si la valeur est nulle
                        logger.error("Removing object list : " + paramPath + " - " + simplePath + " - " + resqmlObj);
                        objClass.getMethod("remove", int.class).invoke(resqmlObj, simplePathVal);
                    }
                } else { // On est pas au bout du chemin, on get l'element de la liste et on continue la recursion
                    Object listObjectContent = null;
                    try {
                        listObjectContent = objClass.getMethod("get", int.class).invoke(resqmlObj, simplePathVal);
                    } catch (Exception e) {
                        //logger.error(" ==> " + objClass.getName());
                        if (List.class.isAssignableFrom(objClass)) {
                            String subPath = completePath.substring(0, completePath.length() - paramPath.length() - 1);
                            ObjectTree treeObj = ObjectTree.createTree(rootObject, subPath);
                            while ((int) objClass.getMethod("size").invoke(resqmlObj) <= simplePathVal) {
                                ObjectController.createObjectAttribute(rootObject, subPath,
                                        treeObj.getDataClassTemplatedList().get(0));
                            }
                            //logger.error("Getting object : at idx : " + simplePath +" in list size : " +objClass.getMethod("size").invoke(resqmlObj));
                            listObjectContent = objClass.getMethod("get", int.class).invoke(resqmlObj, simplePathVal);
                        } else {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    //logger.error("new assignable : " + listObjectContent);
                    modifyResqmlObjectFromParameter(session, listObjectContent,
                            paramPath.substring(paramPath.indexOf(".")), modifType, value,
                            epcObjects, completePath, rootObject);
                }

            } else {    // On est pas sur un chiffre donc pas dans l'acces dans une liste

                Method paramAccessMethod = ObjectController.getAttributeAccessMethod(resqmlObj, simplePath);
                Object paramVal = paramAccessMethod.invoke(resqmlObj); // ancienne valeur
                //logger.info("param Val : " + paramVal + " for simple path " + simplePath);

                Class<?> paramClass = paramAccessMethod.getReturnType();
                Object valueToAssign;

                if (!paramPath.contains(".")) {// Si c'est un attribut final on fait le "set"
                    // Si c'est une liste : on ajoute a la fin
                    if (List.class.isAssignableFrom(paramClass)) {
                        if (ObjectController.getObjectAttributeValue(resqmlObj, simplePath) == null) {
                            ObjectController.getAttributeEditMethod(resqmlObj, simplePath).invoke(resqmlObj, new ArrayList<>());
                        }
                        List<Class<?>> templateClass = ObjectController.getClassTemplatedTypeofSubParameter(objClass, simplePath);
                        if (templateClass.size() > 0) {
                            // On cree l'objet avec le premier template

                            // TODO :  Attention ! j'ai enleve le calcul du schemaVersion ici pour le laisse en deporte dans l'appel de createInstance, a voir si ca ne fait pas de bug!

                            Object newObject = Editor.pkgManager.createInstance(templateClass.get(0).getName(), epcObjects, value);
                            //Object newObject = Editor.pkgManager.createInstance(templateClass.get(0).getName(), epcObjects, EPCGenericManager.getSchemaVersion(rootObject), (String) value);

                            List.class.getMethod("add", Object.class).invoke(ObjectController.getObjectAttributeValue(resqmlObj, simplePath), newObject);
                        } else {
                            logger.error("ERROR : no template for list at " + simplePath + " for object " + resqmlObj);
                        }


                    } else if (value != null && (!(value instanceof String) || ((String) value).replaceAll("\\s+", "").length() > 0)) { // si la value est null on ne fait rien
                        // Si ce n'est pas une liste on fait un "set"

                        Method editMethod = null;
                        try {
                            editMethod = ObjectController.getAttributeEditMethod(resqmlObj, simplePath);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }

                        if (paramClass == String.class) {
                            valueToAssign = value + "";    // On cree une copie de string pour ne pas avoir un objet partage
                            //						logger.error("string value : " + valueToAssign);
                        } else if (ObjectController.getClassName(editMethod.getParameters()[0].getType().getName()).compareTo(ObjectController.getClassName(value.getClass().getName())) == 0) {
                            // Si la value n'est pas un string mais directement le bon type a mettre dans l'objet (e.g. lors d'une copie vers une autre version)
                            valueToAssign = value;
                        } else { // la value n'est pas nulle (teste avant)
                            if (value.getClass() != String.class) {    // Si on tombe sur une valeur non string
                                try {
                                    valueToAssign = Editor.pkgManager.createInstance(value.getClass().getName(), epcObjects, value);
                                } catch (Exception exceptEditMethodParamFail) {
                                    logger.error("Err in @modifyResqmlObjectFromParameter could not get edit method parameter class for object " + objClass
                                            + " and method is " + editMethod);
                                    valueToAssign = Editor.pkgManager.createInstance(value.getClass().getName(), epcObjects, value);
                                }
                            } else {    // si on tombe sur un String alors que ce n'est pas ce qui est attendu par 'paramClass' c'est que
                                // l'objet final va etre cree en parsant le string (e.g. DataObjectReference)
                                try {
                                    ETPUri uri = ETPUri.parse(value + "");
                                    if (SessionUtility.configIsMoreVerborseThan(ConfigurationType.debug))
                                        logger.debug("Creating elt " + paramClass.getName() + " from string : '" + value + "'");
                                    if(paramClass.getName().toLowerCase().endsWith("reference") && uri.getUuid() != null){
                                        // If we want to create a dor from an ETP uri
                                        valueToAssign = ETPUtils.createDORFromUri(session, paramClass.getName(), uri);
                                    }else {
                                        valueToAssign = Editor.pkgManager.createInstance(paramClass.getName(), epcObjects, value);
                                    }
                                } catch (Exception exceptEditMethodParamFail) {
                                    logger.error("Err in @modifyResqmlObjectFromParameter could not get edit method parameter class for object " + objClass
                                            + " and method is " + editMethod);
                                    valueToAssign = Editor.pkgManager.createInstance(paramClass.getName(), epcObjects, value);
                                }
                            }
                        }
                        try {
                            assert editMethod != null;
                            editMethod.invoke(resqmlObj, valueToAssign);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            logger.error("ERR > '" + resqmlObj + "' -- '" + simplePath + "' value to assign : '" + valueToAssign
                                    + "' rootVersion " + EPCGenericManager.getSchemaVersionFromClassName(rootObject.getClass().getName(), true)
                                    + " edit method : '" + editMethod + "' "
                                    + " param class : '" + editMethod.getParameters()[0].getType().getName() + "'");
                        }
                    } else {
                        Object arg = null;    // On est obliger de declarer dans une variable sinon
                        // le passage d'argument a 'invoke' ne marche pas
                        ObjectController.getAttributeEditMethod(resqmlObj, simplePath).invoke(resqmlObj, arg);
                        logger.info("setting to null " + ObjectController.getAttributeEditMethod(resqmlObj, simplePath));
                    }

                } else { // On recurse, on est pas a la fin

                    if (paramVal == null) { // si n'existe pas on cree l'objet a la volee et on l'assigne a "resqmlObj"
                        Class<?> newObjectClass = null;
                        try {
                            if (value == null || value instanceof String) { // si un String en parametre, ce n'est peut etre pas le type
                                // qu'il convient de mettre, on cherche donc le type d'objet qui est sense Ãªtre present (en esperant ne pas
                                // tomber sur une classe abstraite)
                                newObjectClass = paramAccessMethod.getReturnType();
                            } else {    // Si on a autre chose qu'un String en paramÃ¨tre on essaie de creer un objet de meme classe
                                newObjectClass = value.getClass();
                            }
                            paramVal = Editor.pkgManager.createInstance(newObjectClass.getName(), epcObjects, null);
                            ObjectController.getAttributeEditMethod(resqmlObj, simplePath).invoke(resqmlObj, paramVal);
                        } catch (Exception e) {
                            logger.error("Error creating Instance of '" + newObjectClass + "'");
                            logger.error(ObjectController.getAttributeEditMethod(resqmlObj, simplePath));
                            logger.error(" paramVal " + paramVal);
                            logger.error(e.getMessage(), e);
                        }
                    } else if (paramVal.getClass().getName().toLowerCase().endsWith("dataobjectreference")
                            && paramPath.toLowerCase().endsWith("uuid")
                            && epcObjects.containsKey(value)) {
                        // Si on tombe sur un DOR et que l'on veux changer l'uuid, on le supprime pour que lors de la modification, on cree tout les
                        // elements qu'il faut
//                        logger.info("DOR uuid compare ["+simplePath+"]" + ObjectController.getObjectAttributeValue(resqmlObj, simplePath + ".uuid") + " =?= " + value);
                        if(("" + ObjectController.getObjectAttributeValue(resqmlObj, simplePath + ".uuid")).compareTo(""+value) != 0){
                            ResqmlObjectControler.modifyResqmlObjectFromParameter(session, resqmlObj, simplePath,
                                    ModficationType.EDITION,
                                    null,
                                    epcObjects);
                            logger.info("removing DOR to replace it '" + simplePath + "' " + paramVal);
                            modifyResqmlObjectFromParameter(session, resqmlObj, paramPath.substring(0, paramPath.indexOf(".")), modifType, value, epcObjects, completePath, rootObject);
                            return;// On sort apres avoir modifie le DOR car on ne fait pas de modification, il sera
                            // cree par l'uuid
                        }
                    }else{
                        logger.debug("Modification not specific for : " + paramPath + " - " + value);
                    }
                    modifyResqmlObjectFromParameter(session, paramVal, paramPath.substring(paramPath.indexOf(".")), modifType, value, epcObjects, completePath, rootObject);
                }
            }
        }
    }


    public static void prefillActivityFromTemplate(HttpSession session, Object activityTemplate, Object activity) {
        List<Object> paramListA = (List<Object>) ObjectController.getObjectAttributeValue(activity, ".Parameter");
        if (paramListA.size() <= 0) {
            List<Object> paramListTempl = (List<Object>) ObjectController.getObjectAttributeValue(activityTemplate, ".Parameter");
            List<Class<?>> possibleParamType = new ArrayList<>();
            try {
                Class<?> paramClass = ObjectController.getClassTemplatedTypeofSubParameter(activity.getClass(), ".Parameter").get(0);
                //				logger.info("param class " + paramClass);
                for (Class<?> subC : ResqmlAccessibleTypes.CLASS_INSTANCIABLE_BY.get(paramClass)) {
                    logger.info("param type is instanciable by " + subC.getName());
                }
                possibleParamType.addAll(ResqmlAccessibleTypes.CLASS_INSTANCIABLE_BY.get(paramClass));
                logger.info("Activity template has " + paramListTempl.size() + " parameters");


                // For each parameterTemplate, we create all real parameter in the activity
                for (Object paramTempl : paramListTempl) {
                    try {
                        boolean isInput = Boolean.parseBoolean("" + ObjectController.getObjectAttributeValue(paramTempl, "isInput"));

                        if (isInput) {
                            long minOccurs = Long.parseLong("" + ObjectController.getObjectAttributeValue(paramTempl, "MinOccurs"));
                            String title = (String) ObjectController.getObjectAttributeValue(paramTempl, "Title");
                            List<String> paramKind = ((List<?>) ObjectController.getObjectAttributeValue(paramTempl, "AllowedKind"))
                                    .stream().map(x -> "" + x).collect(Collectors.toList());

                            List<Object> defaultValues = (List<Object>) ObjectController.getObjectAttributeValue(paramTempl, "DefaultValue");

                            for (String allowedK : paramKind) {
                                logger.info("Allowed Kind : " + allowedK);
                            }
                            logger.info("ParamTempl : " + title + " minOc " + minOccurs);
                            for (int cpt = 0; cpt < Math.max(minOccurs, 1); cpt++) {
                                Class<?> activityParamClass = getClassMatchFromPrefix(paramKind.get(cpt % paramKind.size()), possibleParamType);

                                Object defaultValue = null;
                                for (Object defVal : defaultValues) {
                                    assert activityParamClass != null;
                                    logger.info("Compare default value class '" + activityParamClass.getName()
                                            + "' with def val class : '" + defVal.getClass().getName() + "'");
                                    if (defVal.getClass().getName().compareTo(activityParamClass.getName()) == 0) {
                                        defaultValue = defVal;
                                        break;
                                    }
                                }

                                // If there are no default value
                                if (defaultValue == null) {
                                    assert activityParamClass != null;
                                    defaultValue = Editor.pkgManager.createInstance(activityParamClass.getName(), new HashMap<>(), null);
                                } else {
                                    logger.info("taking default value " + defaultValue);
                                    // If there is a default value, we copy it and add it to the activity parameter list
                                    defaultValue = ResQMLConverter.getCopy(session, defaultValue, new HashMap<>());
                                }
                                ResqmlObjectControler.modifyResqmlObjectFromParameter(session, defaultValue, "Title",
                                        ModficationType.EDITION,
                                        title, new HashMap<>());
                                ResqmlObjectControler.modifyResqmlObjectFromParameter(session, defaultValue, "Index",
                                        ModficationType.EDITION,
                                        "" + cpt, new HashMap<>());

                                ((List<Object>) ObjectController.getObjectAttributeValue(activity, ".Parameter")).add(defaultValue);
                            }
                        }
                    }catch (Exception e){
                        logger.error(e);
                    }
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.error("Activity '" + ObjectController.getObjectAttributeValue(activity, ".uuid") + "' already has params");
        }
    }

    private static Class<?> getClassMatchFromPrefix(String prefix, List<Class<?>> possibleClasses) {
        for (Class<?> cl : possibleClasses) {
            if (cl.getSimpleName().toLowerCase().startsWith(prefix.toLowerCase())) {
                return cl;
            }
        }
        // If not found, try to remove '_' character
        for (Class<?> cl : possibleClasses) {
            if (cl.getSimpleName().replace("_", "").toLowerCase().startsWith(prefix.toLowerCase())) {
                return cl;
            }
        }
        // If not found, try to check only with prefix start
        int minPrefixSize = 4;
        for (Class<?> cl : possibleClasses) {
            if (cl.getSimpleName().toLowerCase().startsWith(prefix.substring(0, Math.min(minPrefixSize, prefix.length())).toLowerCase())) {
                return cl;
            }
        }

        return null;
    }

    public enum ModficationType {ADD_LIST_ELT, REMOVE_LIST_ELT, CREATE_ELT, EDITION}

    public static Object randomizecontent(Class<?> objClass, WorkspaceContent wc){
        return createInstance(objClass, wc, null, null);
    }

    private static final Random RANDOMIZER = new Random();
    private static Map<String, String> extTypes = Editor.pkgManager.getExtTypesAsJson(SessionUtility.wsProperties.getDirPathToExtTypes());

    public static Object createInstance(Class<?> objClass, WorkspaceContent wc, Object parent, String thisAttributeName){
        if(objClass.getName().compareToIgnoreCase("java.lang.Object") == 0) return null;
        if(objClass.isEnum()){
            return objClass.getEnumConstants()[RANDOMIZER.nextInt(objClass.getEnumConstants().length)];
        }else{
            if(Number.class.isAssignableFrom(objClass)){
                Integer rand = RANDOMIZER.nextInt(100);
                try {
                    return objClass.getMethod("valueOf", String.class).invoke(null, String.valueOf(rand));
                } catch (Exception e) {e.printStackTrace();}
            }else if(Boolean.class.isAssignableFrom(objClass)){
                int rand = RANDOMIZER.nextInt(2);
                return rand != 0;
            }else if(objClass.isPrimitive()){
                Integer rand = RANDOMIZER.nextInt(100);
                try {
                    return createInstance(
                            Class.forName("java.lang." + objClass.getSimpleName().substring(0,1).toUpperCase() + objClass.getSimpleName().substring(1)),
                            wc, parent, thisAttributeName
                    );
                } catch (ClassNotFoundException e) {e.printStackTrace();}
            }else if(String.class.isAssignableFrom(objClass)){
                if(thisAttributeName != null){
                    String refClass = (parent!=null?parent.getClass().getName():objClass.getName()).toLowerCase();
                    if(refClass.endsWith("ext")) refClass = refClass.substring(0,refClass.length()-3);
                    String attribPath = ( refClass + "." + thisAttributeName).toLowerCase();

                    if(thisAttributeName.compareToIgnoreCase("uuid") == 0
                            || thisAttributeName.compareToIgnoreCase("uid") == 0
                    ){
                        return UUID.randomUUID().toString();
                    }else if(extTypes.containsKey(attribPath)){
                        try {
                            String enumClassName = extTypes.get(attribPath);
                            if(enumClassName.toLowerCase().endsWith("ext")) enumClassName = enumClassName.substring(0,enumClassName.length()-3);
                            return String.valueOf(
                                    createInstance(
                                            Class.forName(enumClassName),
                                            wc, parent, thisAttributeName
                                    )
                            );

                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                            return "Randomization of " + thisAttributeName + "(" + RANDOMIZER.nextInt() + ") ExtEnum value failed to generate";
                        }
                    }else{
                        return "Randomization of " + thisAttributeName + "(" + RANDOMIZER.nextInt() + ") ";// + attribPath;
                    }
                }else{
                    return "A random value (" + RANDOMIZER.nextInt() + ")";
                }
            }else if(objClass.getSimpleName().compareToIgnoreCase("DataObjectReference") == 0){
                // TODO
            }else if(XMLGregorianCalendar.class.isAssignableFrom(objClass)){
                return Utils.getCalendarForNow();
            }else if(Collection.class.isAssignableFrom(objClass)){
                try {
                    Collection objInstance = (Collection) (!Modifier.isAbstract(objClass.getModifiers()) ? objClass.getDeclaredConstructor().newInstance(): new ArrayList<>());

                    for(int i=0; i<RANDOMIZER.nextInt(2) + 2; i++){
                        objInstance.add(createInstance(ObjectController.getClassTemplatedTypeofSubParameter(parent.getClass(), thisAttributeName).get(0), wc, objInstance, thisAttributeName));
                    }
                    return objInstance;
                } catch (Exception e) {e.printStackTrace();}
            }else{
                if(Modifier.isAbstract(objClass.getModifiers())) {
                    EPCPackage energymlPkg = Editor.pkgManager.getMatchingPackage(objClass);
                    List<Class<?>> possibleClasses = ObjectController.getResqmlInheritorClasses(objClass.getName(), energymlPkg.getPkgClasses());
                    return createInstance(
                            possibleClasses.get(RANDOMIZER.nextInt(possibleClasses.size())),
                            wc, parent, thisAttributeName
                    );
                }else {
                    try {
                        Object objInstance = objClass.getDeclaredConstructor().newInstance();
                        for (Pair<Class<?>, String> param : ObjectController.getClassAttributes(objClass)) {
                            Object paramInstance = createInstance(param.l(), wc, objInstance, param.r());
                            if(Collection.class.isAssignableFrom(param.l())) {
                                ((Collection<?>)ObjectController.getObjectAttributeValue(objInstance, param.r())).addAll((Collection)paramInstance);
                            }else{
                                ObjectController.editObjectAttribute(objInstance, param.r(), paramInstance);
                            }
                        }
                        return objInstance;
                    } catch (Exception e) {
                        System.err.println("Failed to instanciate class " + objClass + " for attribute " + thisAttributeName);
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static void main(String[] argv){
//        System.out.println(createInstance(WellboreType.class, null, null, null));
//        System.out.println(createInstance(Double.class, null, null, null));
//        System.out.println(createInstance(double.class, null, null, null));
//        System.out.println(createInstance(Integer.class, null, null, null));
//        System.out.println(createInstance(String.class, null, null, null));

//        for(String pkg: extTypes.keySet()){
//            System.out.println(pkg);
//        }
//        System.out.println(Editor.pkgManager.marshal(randomizecontent(Tubular.class, null)));

        ActivityTemplate activityTemplate = (ActivityTemplate) Editor.pkgManager.unmarshal(Utils.readFileOrRessource("C:/Users/Cryptaro/Downloads/ActivityTemplate_c5d4277b-266d-4b78-bae4-af9d642f513b.xml")).getValue();
        Object activity = new Activity();
        System.out.println(activityTemplate);
        prefillActivityFromTemplate(null, activityTemplate, activity);
        System.out.println(Editor.pkgManager.marshal(activity));
    }
}
