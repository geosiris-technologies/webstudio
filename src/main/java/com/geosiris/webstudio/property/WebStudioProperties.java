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
package com.geosiris.webstudio.property;

import com.geosiris.storage.cloud.api.property.Properties;
import org.apache.tomcat.util.json.JSONParser;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class WebStudioProperties extends Properties {
    protected Boolean enableUserDB;
    protected Boolean enableWorkspace;
    protected DeploymentVersion deploymentVersion;
    protected ConfigurationType configurationType;
    protected String pathToAdditionalObjectsDir;
    protected String fpathToXSDMapping;

    protected String pluginsDirPath;

    public WebStudioProperties() {
        super();
        if (deploymentVersion == null) {
            deploymentVersion = DeploymentVersion.full;
        }
        if (configurationType == null) {
            configurationType = ConfigurationType.debug;
        }

        logger.info("Reading WS Properties : " + this);
    }

    public String getPathToAdditionalObjectsDir() {
        return pathToAdditionalObjectsDir;
    }

    public void setPathToAdditionalObjectsDir(String pathToAdditionalObjectsDir) {
        this.pathToAdditionalObjectsDir = pathToAdditionalObjectsDir;
    }

    public ConfigurationType getConfigurationType() {
        return configurationType;
    }

    public void setConfigurationType(ConfigurationType configurationType) {
        this.configurationType = configurationType;
    }

    public DeploymentVersion getDeploymentVersion() {
        return deploymentVersion;
    }

    public void setDeploymentVersion(DeploymentVersion deploymentVersion) {
        this.deploymentVersion = deploymentVersion;
    }

    public Boolean getEnableUserDB() {
        return enableUserDB;
    }

    public void setEnableUserDB(Boolean enableUserDB) {
        this.enableUserDB = enableUserDB;
    }

    public Boolean getEnableWorkspace() {
        return enableWorkspace;
    }

    public void setEnableWorkspace(Boolean enableWorkspace) {
        this.enableWorkspace = enableWorkspace;
    }

    public String getPluginsDirPath() {
        return pluginsDirPath;
    }

    public void setPluginsDirPath(String pluginsDirPath) {
        this.pluginsDirPath = pluginsDirPath;
    }

    public String getFpathToXSDMapping() {
        return fpathToXSDMapping;
    }

    public Map<String, String> getXSDFilePaths(){
        Map<String, String> res = new HashMap<>();
        try {
            JSONParser parser = new JSONParser(new FileInputStream(this.fpathToXSDMapping));
            for(Map.Entry<String, Object> entry : parser.object().entrySet()){
                res.put(entry.getKey(), (String) entry.getValue());
            }
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
        return res;
    }

    public String getXSDFilePathFromPkgName(String pkg){
        logger.error("Searching schema for pkg : " + pkg);
        try {
            return getXSDFilePaths().get(pkg);
        }catch (Exception ignore){}
        return null;
    }

    public static void main(String[] arv) {
        logger.info(new WebStudioProperties());
        WebStudioProperties ws = new WebStudioProperties();
        logger.info(ws.getXSDFilePathFromPkgName("common2_0"));
    }
}