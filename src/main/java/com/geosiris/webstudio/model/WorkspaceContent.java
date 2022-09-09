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
package com.geosiris.webstudio.model;

import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.energyml.utils.Pair;
import com.google.gson.Gson;
import energyml.relationships.Relationships;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkspaceContent {

    private List<Pair<String, byte[]>> notReadObjects;
    private Map<String, Object> readObjects;
    private Map<String, Object> additionalInformation;
    private Map<String, Relationships> parsedRels;

    public WorkspaceContent() {
        this.notReadObjects = new ArrayList<>();
        this.readObjects = new HashMap<>();
        this.additionalInformation = new HashMap<>();
        this.parsedRels = new HashMap<>();
    }

    public List<Pair<String, byte[]>> getNotReadObjects() {
        return notReadObjects;
    }

    public void setNotReadObjects(List<Pair<String, byte[]>> notReadObjects) {
        this.notReadObjects = notReadObjects;
    }

    public Map<String, Object> getReadObjects() {
        return readObjects;
    }

    public void setReadObjects(Map<String, Object> readedObjects) {
        this.readObjects = readedObjects;
    }

    public Map<String, Object> getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(Map<String, Object> additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public void putAll(WorkspaceContent rf) {
        this.notReadObjects.addAll(rf.notReadObjects);
        this.readObjects.putAll(rf.readObjects);
        this.additionalInformation.putAll(rf.additionalInformation);
        this.parsedRels.putAll(rf.parsedRels);
    }

    public Map<String, Relationships> getParsedRels() {
        return parsedRels;
    }

    public void setParsedRels(Map<String, Relationships> parsedRels) {
        this.parsedRels = parsedRels;
    }

    public String toJson(){
        StringBuilder res = new StringBuilder();
        res.append("{");

        Gson gson = new Gson();

        res.append("\"Energyml objects\": [");
        for(String uuid: readObjects.keySet()){
            res.append("{");
            res.append("\"uuid\": \"").append(uuid).append("\",");
            res.append("\"title\": ").append(gson.toJson(ObjectController.getObjectAttributeValue(readObjects.get(uuid), "Citation.Title")));
            res.append("},");
        }
        if(readObjects.size()>0){ // removing last comma
            res.deleteCharAt(res.length() - 1);
        }
        res.append("],");

        res.append("\"Other objects\": [");
        for(Pair<String, byte[]> misc: notReadObjects){
            res.append(gson.toJson(misc.l())).append(",");
        }
        if(notReadObjects.size()>0){ // removing last comma
            res.deleteCharAt(res.length() - 1);
        }
        res.append("],");

        res.append("\"Other information\": [");
        for(String name: additionalInformation.keySet()){
            res.append("{");
            res.append("\"name\": ").append(name);
            res.append("\"content\": ").append(gson.toJson(additionalInformation.get(name)));
            res.append("},");
        }
        if(additionalInformation.size()>0){ // removing last comma
            res.deleteCharAt(res.length() - 1);
        }
        res.append("]");

        res.append("}");
        return res.toString();
    }
}
