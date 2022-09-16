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

import com.geosiris.energyml.utils.ResqmlAbstractType;
import com.google.gson.Gson;

import java.util.List;

public class WorkspaceRelationship {
    public static class SimpleRelationship{
        private String uuid;
        private String name;

        public SimpleRelationship(String uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private String uuid;
    private String num;
    private String title;
    private String type;
    private String schemaVersion;
    private ResqmlAbstractType firp;

    private List<SimpleRelationship> relationUp;
    private List<SimpleRelationship> relationDown;

    public WorkspaceRelationship(String uuid, String num, String title, String type, String schemaVersion, ResqmlAbstractType firp, List<SimpleRelationship> relationUp, List<SimpleRelationship> relationDown) {
        this.uuid = uuid;
        this.num = num;
        this.title = title;
        this.type = type;
        this.schemaVersion = schemaVersion;
        this.firp = firp;
        this.relationUp = relationUp;
        this.relationDown = relationDown;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public ResqmlAbstractType getFirp() {
        return firp;
    }

    public void setFirp(ResqmlAbstractType firp) {
        this.firp = firp;
    }

    public List<SimpleRelationship> getRelationUp() {
        return relationUp;
    }

    public void setRelationUp(List<SimpleRelationship> relationUp) {
        this.relationUp = relationUp;
    }

    public List<SimpleRelationship> getRelationDown() {
        return relationDown;
    }

    public void setRelationDown(List<SimpleRelationship> relationDown) {
        this.relationDown = relationDown;
    }

    @Override
    public String toString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
