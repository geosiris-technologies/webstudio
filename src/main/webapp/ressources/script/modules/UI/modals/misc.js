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

import {downloadGetURL_Promise, sendGetURL_Promise, sendPostRequestJson_Promise, sendPostRequest_Promise} from "../../requests/requests.js";
import {beginTask, endTask} from "../ui.js";
import {modal_waitDomUpdate, sendForm} from "./modalEntityManager.js";
import {sendGetURLAndReload} from "../eventHandler.js";
import {saveAllResqmlObject_promise} from "../tabulation.js";
import {appendConsoleMessage} from "../../logs/console.js";
import {__ENUM_CONSOLE_MSG_SEVERITY_TOAST__, __ID_CONSOLE__, __RWS_CLIENT_NAME__, __RWS_ETP_LOGIN__, __RWS_ETP_PWD__, __RWS_ETP_URI__, set__RWS_ETP_URI__} from "../../common/variables.js";


export function dochangeDorReference(){
    var rootObjectUuid = document.getElementById("changeDorReferenceFilePath_root").value;
    var inputUuid = document.getElementById("changeDorReferenceFilePath_target").value;
    if(rootObjectUuid.replace(/ /g, '').length>0 && inputUuid.replace(/ /g, '').length>0 ){
        document.getElementById("changeDorReferenceSubmitBut").disabled = true;
        beginTask();
        fetch("EPCOperation?command=changereference&Root_UUID=" + rootObjectUuid + "&input=" + inputUuid).then(
            function(){
                endTask();
                document.getElementById("changeDorReferenceSubmitBut").disabled = false;
                document.getElementById('closeBut_changeDorReference').click();
                document.getElementById('changeDorReferenceFilePath_target').value = "";
                document.getElementById('changeDorReferenceFilePath_root').value = "";
            }
        ).catch(() => endTask());
    }
}

// --- CloseEPCFile
export function closeActionNo(){
    beginTask();

    //modal_waitDomUpdate(function(){return roller.style.display=='';})

    sendGetURLAndReload('FileReciever?close=true', false).then(function(){
        endTask();
    }).catch(() => endTask())
}

export function closeActionYes(){
    var promessesSave = saveAllResqmlObject_promise();
    beginTask();

    Promise.all(promessesSave).then(function (data) {
        endTask();
        $("#modal_exportEPCAndClose").modal();
    }).catch(() => endTask());
}
// --- 
// --- CreateNewRootElement

export function createNewElt(idForm, modalId, rollingId){
    var selectedType = document.getElementById("selectorCreateRootElt").value
    // console.log("selected type : ");
    sendForm('createRootEltForm', 'modal_createRootElt', 'rolling_createRootElt', false, false, false, true);
    // console.log(selectedType);
}

// ExportEpcFile
export function doExportEPC(){
    document.getElementById("exportEpcSubmitBut").disabled = true;
    var epcFilePath = document.getElementById("exportEpcFilePath").value;
    if(epcFilePath.replace(/ /g, '').length>0){
        beginTask();
        downloadGetURL_Promise("ExportEPCFile?epcFilePath="+epcFilePath+"&exportVersion=" + document.querySelector('input[name="exportVersion"]:checked').value).then(
            function(){
                document.getElementById("exportEpcSubmitBut").disabled = false;
                document.getElementById('closeBut_export').click();
                endTask();
            }
        ).catch(() => endTask());
    }
}

// unity
export function importObjectIn3DView(uris){//, importType, pointSize){
    appendConsoleMessage(__ID_CONSOLE__, { 
                                severity: __ENUM_CONSOLE_MSG_SEVERITY_TOAST__,
                                originator: __RWS_CLIENT_NAME__,
                                message: "[Please WAIT] Trying to import uris :" + uris.toString()
                            });
    var data = {"uris": uris};

    return sendPostRequestJson_Promise("/ETPLoadSurfaceInVue", data, false);

    /*if(__RWS_ETP_URI__ != null && __RWS_ETP_LOGIN__ != null && __RWS_ETP_PWD__ != null){
        if(!__RWS_ETP_URI__.startsWith("ws")){
            set__RWS_ETP_URI__("ws://" + __RWS_ETP_URI__);
        }
        data = {
                    "uris": uris,
                    "serverUrl": __RWS_ETP_URI__,
                    "login": __RWS_ETP_LOGIN__,
                    "password": __RWS_ETP_PWD__,
                    "importType": importType,
                    "pointSize": pointSize
                };
                    
    }*/

    //return sendPostRequestJson_Promise("visu-server/importObjects/", data, true);
}