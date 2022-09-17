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
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;

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

    public static void main(String[] argv){
        Gson gson = new Gson();
        WorkspaceRelationship wr = gson.fromJson("{"
        + "\"firp\": \"Property\","
        + "\"num\": \"706\","
        + "\"relationDown\": [],"
        + "\"relationUp\": ["
        + "    {"
        + "        \"name\": \"PropertyKind\","
        + "        \"uuid\": \"b471afef-0310-43cd-8c7f-03fc08e240eb\""
        + "    },"
        + "    {"
        + "        \"name\": \"SupportingRepresentation\","
        + "        \"uuid\": \"774bf941-f3c1-4d1f-94f8-7a3b68474eda\""
        + "    }"
        + "],"
        + "\"schemaVersion\": \"Resqml V2.2\","
        + "\"title\": \"BH-SD27 [Teneurs] sample_length\","
        + "\"type\": \"ContinuousProperty\","
        + "\"uuid\": \"00093361-8ef8-4e5f-816c-17bc1b0b1c56\""
        + "}", WorkspaceRelationship.class);
        System.out.println(wr.toString());

//        System.out.println(ClassLayout.parseInstance(wr));
//        System.out.println(ClassLayout.parseInstance(wr).instanceSize());
//        System.out.println(VM.current().sizeOf(wr));
        System.out.println(GraphLayout.parseInstance(wr).totalSize());
    }
}
