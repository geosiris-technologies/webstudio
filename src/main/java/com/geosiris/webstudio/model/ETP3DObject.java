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

import com.google.gson.Gson;

public class ETP3DObject {

    private String pointColor, lineColor, faceColor;
    private String data, fileType;
    private String type, uuid, title;

    private String epsgCode;

    public ETP3DObject(String data, String fileType, String type, String uuid, String title, String pointColor, String lineColor, String faceColor, String epsgCode) {
        this.data = data;
        this.fileType = fileType;
        this.type = type;
        this.uuid = uuid;
        this.title = title;
        this.pointColor = pointColor;
        this.lineColor = lineColor;
        this.faceColor = faceColor;
        this.epsgCode = epsgCode;
    }

    @Override
    public String toString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String getData() {
        return data;
    }

    public String getFileType() {
        return fileType;
    }
}
