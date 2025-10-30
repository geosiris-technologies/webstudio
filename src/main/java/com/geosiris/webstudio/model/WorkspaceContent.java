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
import java.util.stream.Collectors;

public class WorkspaceContent {

    private List<Pair<String, byte[]>> notReadObjects;
    private Map<String, Object> readObjects;
    private Map<String, Object> additionalInformation;
    private Map<String, Relationships> parsedRels;
    private List<String> loadedEpcNames;

    public WorkspaceContent() {
        this.notReadObjects = new ArrayList<>();
        this.readObjects = new HashMap<>();
        this.additionalInformation = new HashMap<>();
        this.parsedRels = new HashMap<>();
        this.loadedEpcNames = new ArrayList<>();
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

    public List<String> getLoadedEpcNames() {
        return loadedEpcNames;
    }

    public void putAll(WorkspaceContent rf) {
        if(rf != null){
            if(rf.notReadObjects != null)
                this.notReadObjects.addAll(rf.notReadObjects);
            if(rf.readObjects != null)
                this.readObjects.putAll(rf.readObjects);
            if(rf.additionalInformation != null)
                this.additionalInformation.putAll(rf.additionalInformation);
            if(rf.parsedRels != null)
                this.parsedRels.putAll(rf.parsedRels);
            if(rf.loadedEpcNames != null)
                this.loadedEpcNames.addAll(rf.loadedEpcNames);
        }
    }

    public Map<String, Relationships> getParsedRels() {
        return parsedRels;
    }

    public void setParsedRels(Map<String, Relationships> parsedRels) {
        this.parsedRels = parsedRels;
    }


    public static class ReadObjectVue{
        String uuid;
        String title;

        public ReadObjectVue(String uuid, String title) {
            this.uuid = uuid;
            this.title = title;
        }

        public String getUuid() {
            return uuid;
        }

        public String getTitle() {
            return title;
        }
    }

    public String toJson(){
        Map<String, Object> vue = new HashMap<>();

        vue.put("Energyml objects", readObjects.entrySet().stream().map((entry) -> new ReadObjectVue(entry.getKey(),
                                                                                            (String) ObjectController.getObjectAttributeValue(entry.getValue(), "Citation.Title")))
                .collect(Collectors.toList()));

        vue.put("Other objects", notReadObjects.stream().map(Pair::l).collect(Collectors.toList()));
        vue.put("Imported Epc files", loadedEpcNames);

        /*res.append("\"Other information\": [");
        for(String name: additionalInformation.keySet()){
            res.append("{");
            res.append("\"name\": ").append(name);
            res.append("\"content\": ").append(gson.toJson(additionalInformation.get(name)));
            res.append("},");
        }
        if(additionalInformation.size()>0){ // removing last comma
            res.deleteCharAt(res.length() - 1);
        }*/
        Gson gson = new Gson();
        return gson.toJson(vue);
    }
}
