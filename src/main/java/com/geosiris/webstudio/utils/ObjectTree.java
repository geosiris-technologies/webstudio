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

import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.property.ConfigurationType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ObjectTree {
    public static Logger logger = LogManager.getLogger(ObjectTree.class);
    private final String name;
    // private List<Object> typeValues;
    private final Class<?> dataClass;
    private final List<Class<?>> dataClassTemplatedList; // liste des types template de la classe #dataClass
    private final ObjectTree parent;
    private final List<ObjectTree> attributes;
    private final List<ObjectTree> properties;
    private Object data;
    private Boolean isMandatory;

    public ObjectTree() {
        name = "";
        data = null;
        isMandatory = false;
        dataClass = null;
        // typeValues= null;
        isMandatory = false;
        dataClassTemplatedList = new ArrayList<>();
        this.parent = null;
        attributes = new ArrayList<>();
        properties = new ArrayList<>();
    }

    public ObjectTree(String _name, Object _data, Class<?> _dataClass, ObjectTree parent, Boolean isMandatory) {
        name = _name;
        data = _data;

        this.isMandatory = isMandatory;
        dataClass = (data != null) ? data.getClass() : _dataClass;

        // Malheureusement on ne peut pas determiner les template directement il faut
        // regarder la declaration
        // dans la classe mere : cf @appendChildren et @appendProperty
        dataClassTemplatedList = new ArrayList<>();
        this.parent = parent;
        attributes = new ArrayList<>();
        properties = new ArrayList<>();
    }

    public static ObjectTree createTree(Object obj) {
        return createTree("", obj, obj != null ? obj.getClass() : null, null, false);
    }

    // TODO : a revoir ce n'est pas optimal de tout construire pour reparcourir
    public static ObjectTree createTree(Object obj, String subPath) {
        ObjectTree completeTree = createTree("", obj, obj != null ? obj.getClass() : null, null, false);
        return completeTree.subTree(subPath);
    }

    public static ObjectTree createTree(String name, Object obj, Class<?> objClass, ObjectTree parent,
                                        boolean bypassNullObj) {
        boolean isMandatory = false;
        if (parent != null) {
            String simpleName = name;
            if (simpleName.contains(".")) {
                simpleName = simpleName.substring(simpleName.lastIndexOf(".") + 1);
            }
            isMandatory = ResqmlVerification.attributeIsMendatory(parent.dataClass,
                    simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1));
        }
        ObjectTree tree = new ObjectTree(name, obj, objClass, parent, isMandatory);

        if (!bypassNullObj && obj != null) {
            List<Pair<Class<?>, String>> attributes = ObjectController.getClassAttributes(objClass);
            if (List.class.isAssignableFrom(objClass)) {
                // Si c'est une liste on va chercher les elements
                int cpt = 0;
                for (Object elt : (List) obj) {
                    // logger.error("LIST CONTENT ("+cpt+") " + elt);
                    // Non on ne test pas si c'est une propriete car c'est le contenu de la liste
                    // donc peut importe le type,
                    Class<?> eltClass = Object.class;
                    if (elt != null) {
                        eltClass = elt.getClass();
                        tree.appendChild(createTree(name + "." + cpt, elt, eltClass, tree, bypassNullObj));
                        cpt++;
                    } else {
                        try {
                            assert parent != null;
                            eltClass = (Class<?>) ObjectController.getClassTemplates(parent.getDataClass(),
                                    name.substring(name.lastIndexOf(".") + 1)).getActualTypeArguments()[0];

                            logger.error("Trying to create with class : " + eltClass);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                        tree.appendChild(createTree(name + "." + cpt, elt, eltClass, tree, bypassNullObj));
                        cpt++;
                    }
                }
                // On met la valeur a null car on met la valeur comme des enfants de l'arbre
                tree.data = null;
            } else {
                // Si on est sur une propriete on ne cherche pas les sous-elements
                if (!ObjectController.isPropertyClass(objClass)) {
                    // On parcours les attributs pour savoir si c'est des proprietes terminales
                    // (feuille d'arbre)
                    // ou des enfant a mettre dans children (branche de l'arbre)
                    for (Pair<Class<?>, String> attrib : attributes) {
                        String paramName = attrib.r();
                        Class<?> paramClass = attrib.l();
                        Object paramVal = ObjectController.getObjectAttributeValue(obj, paramName);
                        if (paramVal != null) {
                            // logger.info("changing " + paramClass.getSimpleName() + " to " +
                            // paramVal.getClass().getSimpleName());
                            paramClass = paramVal.getClass();
                        }
                        if (!ObjectController.isPropertyClass(paramClass)) {
                            // attribut
                            tree.appendChild(
                                    createTree(name + "." + paramName, paramVal, paramClass, tree, bypassNullObj));
                        } else {
                            // propriete
                            tree.appendProperty(
                                    createTree(name + "." + paramName, paramVal, paramClass, tree, bypassNullObj));
                        }
                    }
                }
            }
        }
        return tree;
    }
    public Object getData(String subPath) {
        ObjectTree subTree = subTree(subPath);
        if (subTree != null) {
            return subTree.data;
        }
        return null;
    }

    public Class<?> getDataClass() {
        return dataClass;
    }

    public Object getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public List<ObjectTree> getAttributes() {
        return attributes;
    }

    public ObjectTree getAttribute(String name, boolean caseSensitiv) {
        if (caseSensitiv)
            for (ObjectTree att : attributes) {
                String attName = att.name;
                if (attName.contains(".")) {
                    attName = attName.substring(attName.lastIndexOf(".") + 1);
                }
                if (attName.compareTo(name) == 0)
                    return att;
            }
        else
            for (ObjectTree att : attributes) {
                String attName = att.name;
                if (attName.contains(".")) {
                    attName = attName.substring(attName.lastIndexOf(".") + 1);
                }
                if (attName.compareToIgnoreCase(name) == 0)
                    return att;
            }
        return null;
    }

    public List<ObjectTree> getProperties() {
        return properties;
    }

    public ObjectTree getProperty(String name, boolean caseSensitiv) {
        if (caseSensitiv) {
            for (ObjectTree prop : properties) {
                String propName = prop.name;
                if (propName.contains(".")) {
                    propName = propName.substring(propName.lastIndexOf(".") + 1);
                }
                if (propName.compareTo(name) == 0)
                    return prop;
            }
        }else {
            for (ObjectTree prop : properties) {
                String propName = prop.name;
                if (propName.contains(".")) {
                    propName = propName.substring(propName.lastIndexOf(".") + 1);
                }
                if (propName.compareToIgnoreCase(name) == 0)
                    return prop;
            }
        }
        return null;
    }

    public List<Class<?>> getDataClassTemplatedList() {
        return dataClassTemplatedList;
    }

    public List<ObjectTree> filterListByObjectType(String typeName, boolean caseSensitiv,
                                                   boolean searchInSuperClasses) {
        List<ObjectTree> dorList = new ArrayList<>();
        if (!caseSensitiv) {
            typeName = typeName.toLowerCase();
        }

        for (ObjectTree att : attributes) {
            if ((att.data != null
                    && ObjectController.inherits(att.data.getClass(), typeName, caseSensitiv, searchInSuperClasses))) {
                dorList.add(att);
            }
            dorList.addAll(att.filterListByObjectType(typeName, caseSensitiv, searchInSuperClasses));
        }

        for (ObjectTree prop : properties) {
            if (prop.data != null
                    && ObjectController.inherits(prop.data.getClass(), typeName, caseSensitiv, searchInSuperClasses)) {
                dorList.add(prop);
            }
            dorList.addAll(prop.filterListByObjectType(typeName, caseSensitiv, searchInSuperClasses));
        }

        return dorList;
    }

    public void appendChild(ObjectTree child) {
        String paramName = child.name;
        if (paramName.contains(".")) {
            paramName = paramName.substring(paramName.lastIndexOf(".") + 1);
        }
        paramName = paramName.replaceAll("\\*", "");

        ParameterizedType tempType = ObjectController.getClassTemplates(dataClass, paramName);
        if (tempType != null) {
            for (Type contentClass : tempType.getActualTypeArguments()) {
                try {
                    child.dataClassTemplatedList.add((Class<?>) contentClass);
                } catch (Exception e) {
                    child.dataClassTemplatedList.add(Object.class);
                    logger.error(e.getMessage(), e);
                    logger.debug(
                            "Template class problem ===> " + contentClass + " : " + contentClass.getTypeName());
                    logger.debug("## => " + tempType + " (" + this.dataClass.getName() + "." + paramName + ")");
                }
            }
        } else {
            if (List.class.isAssignableFrom(dataClass)) {
                if (SessionUtility.configIsMoreVerborseThan(ConfigurationType.debug))
                    logger.debug("#ERR /!\\ attribute " + paramName + " inside " + child.getParent().getName() + "[" + child.getParent().getDataClass() + "] has no parametrized type found by should have !");
            }
        }
//        logger.info(dataClass + "> tempType " + tempType +  " -- " + paramName);

        attributes.add(child);
    }

    public void appendProperty(ObjectTree child) {
        String paramName = child.name;
        if (paramName.contains(".")) {
            paramName = paramName.substring(paramName.lastIndexOf(".") + 1);
        }
        paramName = paramName.replaceAll("\\*", "");

        ParameterizedType tempType = ObjectController.getClassTemplates(dataClass, paramName);
        if (tempType != null) {
            for (Type contentClass : tempType.getActualTypeArguments()) {
                child.dataClassTemplatedList.add((Class<?>) contentClass);
            }
        }

        properties.add(child);
    }

    public ObjectTree subTree(String subPath) {
        if (subPath.length() > 0) {
            String currentPath = subPath;
            while (currentPath.startsWith(".")) {
                currentPath = currentPath.substring(1);
            }
            if (currentPath.contains(".")) {
                currentPath = currentPath.substring(0, currentPath.indexOf("."));
            }
            // logger.error("CurrentPath " + currentPath + " subpath " + subPath);
            for (ObjectTree attribTree : attributes) {
                // logger.error(" > " + attribTree.name + " -- " +
                // attribTree.name.substring(name.length()+1));
                if (attribTree.name.substring(name.length() + 1).toLowerCase().startsWith(currentPath.toLowerCase())) {
                    if (currentPath.compareToIgnoreCase(subPath) == 0) {
                        return attribTree;
                    } else {
                        return attribTree.subTree(subPath.substring(currentPath.length() + 1));
                    }
                }
            }

            for (ObjectTree propTree : properties) {
                // logger.error(" prop name " + propTree.name.substring(name.length()+1));
                if (propTree.name.substring(name.length() + 1).toLowerCase().startsWith(currentPath.toLowerCase())) {
                    if (currentPath.compareToIgnoreCase(subPath) == 0) {
                        return propTree;
                    } else {
                        return propTree.subTree(subPath.substring(currentPath.length() + 1));
                    }
                }
            }
        } else {
            return this;
        }
        return null;
    }

    public String toJSON() {

        StringBuilder jsonValue = new StringBuilder();
        jsonValue.append("  { \"name\" : \"").append(name).append("\",\n");
        jsonValue.append("   \"type\" : \"").append(dataClass.getCanonicalName()).append("\",\n");
        jsonValue.append("   \"mandatory\" : \"").append(isMandatory).append("\",\n");

        if (dataClassTemplatedList != null && dataClassTemplatedList.size() > 0) {
            jsonValue.append("   \"templatedClass\" : [ ");
            for (Class<?> templateClass : dataClassTemplatedList) {
                jsonValue.append("\"").append(templateClass.getName()).append("\",");
            }
            jsonValue = new StringBuilder(jsonValue.substring(0, jsonValue.length() - 1));
            jsonValue.append(" ],\n");
        }

        // TODO : faire les filtre de "typevalue" pour les classes finissant par "Ext"
        // on cherche la classe de mÃªme nom sans "Ext" et on contraint en type enum

        // Pour cela regarder le root elet pout avoir la version puis chercher si le
        // package
        // contient un type ext

        // boolean hasChildOrProperty = false;
        // attributes
        if (attributes != null && attributes.size() > 0) { // A des attributs donc pas de valeur
            jsonValue.append("   \"attributes\" :\n  [\n");
            jsonValue.append("  ").append(attributes.get(0).toJSON());
            for (int childId = 1; childId < attributes.size(); childId++) {
                jsonValue.append(",\n  ").append(attributes.get(childId).toJSON());
            }
            jsonValue.append("  ] , \n");
            // hasChildOrProperty = true;
        }

        if (properties != null && properties.size() > 0) { // A des attributs donc pas de valeur
            jsonValue.append("   \"properties\" :\n  [\n");
            jsonValue.append("  ").append(properties.get(0).toJSON());
            for (int propId = 1; propId < properties.size(); propId++) {
                jsonValue.append(",\n  ").append(properties.get(propId).toJSON());
            }
            jsonValue.append("  ], \n");
            // hasChildOrProperty = true;
        }

        // On met une valeur si ce n'est pas un array et si c'est un type modifiable
        // directement (comme les proprietes)
        if (data != null && !dataClass.getName().toLowerCase().endsWith("array")
                && ObjectController.isPropertyClass(data.getClass())) {
            String dataAsString = data + "";
            if (data.getClass().isEnum()) {
                try {
                    Method m_enulValue = data.getClass().getMethod("value");
                    dataAsString = (String) m_enulValue.invoke(data);
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                         | InvocationTargetException ignore) {
                }
            }
            dataAsString = Utility.transformStringForJsonCompatibility(dataAsString);
            jsonValue.append("  \"value\" : ").append(dataAsString).append("\n");
        } else if (data == null && ObjectController.isPrimitiveClass(dataClass)) {
            jsonValue.append("  \"value\" : \"\"\n");
        } else {
            jsonValue = new StringBuilder(jsonValue.substring(0, jsonValue.lastIndexOf(",")) + " ");
        }
        jsonValue.append("}");
        return jsonValue.toString().replaceAll("\t", "    ");
    }

    public ObjectTree map(Consumer<ObjectTree> c, Boolean consumeAttributes, Boolean consumeProperties) {
        c.accept(this);
        if (consumeAttributes) {
            attributes.sort((a, b) -> a.getName().compareTo(b.getName()));
            for (ObjectTree at : attributes) {
                if (at != null) {
                    at.map(c, consumeAttributes, consumeProperties);
                }
            }
        }
        if (consumeProperties) {
            properties.sort((a, b) -> a.getName().compareTo(b.getName()));
            for (ObjectTree prop : properties) {
                if (prop != null) {
                    prop.map(c, consumeAttributes, consumeProperties);
                }
            }
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder dataString = new StringBuilder(data + "");
        if (data instanceof Object[]) {
            dataString = new StringBuilder("(");
            for (Object o : (Object[]) data) {
                dataString.append(o).append(", ");
            }
            dataString.append(")");
        }
        return "{ [ '" + dataString + "' ] => \n"
                + attributes.stream().map(e -> "\t" + e.toString().replaceAll("\n", "\n\t")).reduce("", String::concat)
                + "\n}";
    }

    public ObjectTree getParent() {
        return parent;
    }
}
