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

import {sendGetURL_Promise} from "../requests/requests.js"
import {createSelect_selector, geosiris_createEditableSelector} from "../UI/htmlUtils.js"


export function get_ETP_Functions_Promise() {
    return sendGetURL_Promise("ETPRequest");
}

export function createJavaTypeView(javaParam, funcName){
    var result = null;
    //console.log(javaParam);
    var str_javaTypeName = javaParam.typeName.toLowerCase();
    if(str_javaTypeName.endsWith("uuid") || str_javaTypeName.endsWith("string")){
        result = document.createElement("input");
        result.className = "form-control";
        result.placeholder = javaParam.name;

    }else if(str_javaTypeName.endsWith("boolean")){
        result = geosiris_createEditableSelector(["True", "False"], "True", false);
    }else if(javaParam.values != null && javaParam.values.length > 0){
        result = geosiris_createEditableSelector(javaParam.values, javaParam.values[0], false);
    }else{
        result = document.createTextNode("<::NOT IMPLEMENTED>"+str_javaTypeName+"<::>");
    }

    result.name = javaParam.name;

    return result;
}

export function createETPRequestFuncForm(){
    const result = document.createElement("div");
    const targetDiv = document.createElement("div");
    targetDiv.className = "etp-object-parameter-block"
    targetDiv.name = "etp-object-parameter-block"
    const objectsName = []
    const objectsParams = []
    
    get_ETP_Functions_Promise().then(
        funcsAsJSON => {
            JSON.parse(funcsAsJSON).forEach(
                (etpFunc, idx) => {
                    // Le nom de l'objet ETP
                    idxPkg = etpFunc.return.lastIndexOf(".")
                    if(idxPkg > 0){
                        objectsName[idx] = etpFunc.return.substring(idxPkg+1);
                    }else{
                        objectsName[idx] = etpFunc.return.substring(etpFunc.return);
                        //objectsName[idx] = etpFunc.return.substring(5);
                    }

                    // Les parametres a fournir : 
                    paramDiv = document.createElement("div")
                    etpFunc.parameters.forEach( 
                        function(element, index) {
                            var parameterView = createJavaTypeView(element);
                            paramDiv.appendChild(parameterView);
                    });
                    objectsParams.push(paramDiv);

                }
            );
            result.appendChild(createSelect_selector(objectsName, objectsParams, targetDiv));
        }
    );

    return result;
}