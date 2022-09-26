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

import {sendGetURLAndReload} from "../UI/eventHandler.js"
import {__USER_NAME__, beginTask, endTask, getVueOrientation, setVueOrientation, initRootEltSelector} from "../UI/ui.js"
import {sendGetURL, sendGetURL_Promise, sendPostRequestJson} from "../requests/requests.js"
import {appendConsoleMessage} from "../logs/console.js"
import {__ENUM_CONSOLE_MSG_SEVERITY_INFO__, __ENUM_CONSOLE_MSG_SEVERITY_WARNING__, 
        __ENUM_CONSOLE_MSG_SEVERITY_ERROR__, __RWS_CLIENT_NAME__, __RWS_SERVER_NAME__, 
        getSeverityEnum} from "../common/variables.js"

/**********************************************************/
/** Do not forget to access in map with LOWERCASE key !! **/
/**********************************************************/

export var mapResqmlTypeToSubtypes = {};
export var mapResqmlTypesComment   = {};
export var mapResqmlEnumToValues   = {};
//export var epcPackagesGroups       = [];
export var extTypeAttributes       = {};
export var energymlRootTypes       = {};


export function updateTypesMap(){
    // Recuperation des types accessibles et des classes qui peuvent les instancier
    beginTask();
    var xmlHttp_TtoST = new XMLHttpRequest();
    xmlHttp_TtoST.onload = function (e) {
        try{
            mapResqmlTypeToSubtypes = JSON.parse(xmlHttp_TtoST.responseText);
            /*console.log("filling : mapResqmlTypeToSubtypes");
            console.log(mapResqmlTypeToSubtypes);*/
        }catch(err){console.log(err);}
        endTask();
    };

    xmlHttp_TtoST.onerror = function (e) {
        console.log(e);
        endTask();
    }
    xmlHttp_TtoST.open( "GET", "ResqmlAccessibleTypes", true );
    xmlHttp_TtoST.send();


    // Recuperation des types enum et de leurs valeurs possibles
    beginTask();
    var xmlHttp_Enums = new XMLHttpRequest();
    xmlHttp_Enums.onerror = function(e){console.log(xmlHttp_Enums);console.log(e);
        endTask();}
    xmlHttp_Enums.onload = function (e) {
        try{
            mapResqmlEnumToValues = JSON.parse(xmlHttp_Enums.responseText);
        }catch(err){console.log(err);}
        endTask();
    };
    xmlHttp_Enums.open( "GET", "ResqmlAccessibleEnumAndValues", true );
    xmlHttp_Enums.send();

    // Recuperation des commentaires des classes
    beginTask();
    var xmlHttp_TComments = new XMLHttpRequest();
    xmlHttp_TComments.onerror = function(e){console.log(xmlHttp_TComments);console.log(e);
        endTask();}
    xmlHttp_TComments.onload = function (e) {
        try{
            mapResqmlTypesComment = JSON.parse(xmlHttp_TComments.responseText);
        }catch(err){
            console.log(err);
            mapResqmlTypesComment = {}
        }
        endTask();
    };
    xmlHttp_TComments.open( "GET", "ResqmlTypesComments", true );
    xmlHttp_TComments.send();

    // Recuperation des groups de packages
    beginTask();
    var xmlHttp_ExtTypesAttributes = new XMLHttpRequest();
    xmlHttp_ExtTypesAttributes.onerror = function(e){console.log(xmlHttp_ExtTypesAttributes);console.log(e);
        endTask();}
    xmlHttp_ExtTypesAttributes.onload = function (e) {
        try{
            extTypeAttributes = JSON.parse(xmlHttp_ExtTypesAttributes.responseText);
        }catch(err){console.log(err);}
        endTask();
    };
    xmlHttp_ExtTypesAttributes.open( "GET", "EPCExtTypesAttributes", true );
    xmlHttp_ExtTypesAttributes.send();

    // Recuperation des types racines
    beginTask();
    var xmlHttp_EnergymlRootTypes = new XMLHttpRequest();
    xmlHttp_EnergymlRootTypes.onerror = function(e){console.log(xmlHttp_EnergymlRootTypes);console.log(e);
        endTask();}
    xmlHttp_EnergymlRootTypes.onload = function (e) {
        try{
            energymlRootTypes = JSON.parse(xmlHttp_EnergymlRootTypes.responseText);
            initRootEltSelector(document.getElementById("selectorCreateRootElt"));
        }catch(err){console.log(err);}
        endTask();
    };
    xmlHttp_EnergymlRootTypes.open( "GET", "ResqmlRootTypes", true );
    xmlHttp_EnergymlRootTypes.send();

}

export function openHDFViewAskH5Location(h5Path, subPath){
    openHDFView(h5Path, subPath);
}

export function openHDFView(h5Path, subPath){
    //var path = "http://h5viewer:3000/"+h5Path;
    console.log("UserName '" + __USER_NAME__ + "'")
    var path = "http://localhost:9996/"+ (__USER_NAME__!="" ? __USER_NAME__+"/" : "") +h5Path + subPath;
    // TODO: changer l'url
    console.log("opening HDFView with url : '" + path+"' sub path was : '" + subPath +"'");
    window.open(path, '_blank');
}

export function resquestObjectCopy(idConsoleElt, rootUUID){
    var url = "ObjectEdit";
    if(rootUUID != null)
    {
        url += "?Root_UUID=" + rootUUID + "&command=copy&version=2.3";
    }
    
    sendGetURLAndReload(url, false);
}


export function resquestValidation(idConsoleElt, rootUUID){
    var url = "ResqmlVerification";
    if(rootUUID != null)
    {
        url += "?uuid=" + rootUUID;
    }
    //console.log("verication request at url " + url);
    beginTask();
    
    sendGetURL(url, false,
            function(responseText){
                try{
                    var validationJSON = JSON.parse(responseText);

                    var msgList = [];

                    var cpt_errors = 0;
                    var cpt_warnings = 0;
                    for(var validMsgIdx=0; validMsgIdx<validationJSON.length; validMsgIdx++){
                        var msgVerif = validationJSON[validMsgIdx];
                        var severity = null;
                        try{
                            severity = getSeverityEnum(msgVerif.severity)
                        }catch(exception){}
                        if(severity == __ENUM_CONSOLE_MSG_SEVERITY_ERROR__)
                            cpt_errors++;
                        if(severity == __ENUM_CONSOLE_MSG_SEVERITY_WARNING__)
                            cpt_warnings++;
                    }

                    var message = "Validation ";

                    if(cpt_warnings > 0 || cpt_errors > 0){
                        message += "report:";
                        if(cpt_warnings > 0){
                            message += cpt_warnings + " warning" + (cpt_warnings>1?"s ":" ");
                            if(cpt_errors > 0)
                                message +=  "and"
                        }
                        if(cpt_errors > 0){
                            message += " " + cpt_errors + " error" + (cpt_errors>1?"s":"");
                        }
                    }else{
                        message += "success"
                    }
                    msgList.push({ 
                        severity: __ENUM_CONSOLE_MSG_SEVERITY_INFO__,
                        originator: __RWS_CLIENT_NAME__,
                        message: message
                    });


                    for(var validMsgIdx=0; validMsgIdx<validationJSON.length; validMsgIdx++){
                        var msgVerif = validationJSON[validMsgIdx];
                        msgList.push(msgVerif)
                    }
                    appendConsoleMessage(idConsoleElt, msgList);

                }catch(exceptResponse){
                    console.log(exceptResponse);
                    console.log("Initial Message was ");
                    console.log(responseText);
                    appendConsoleMessage(idConsoleElt, { 
                                                            severity: __ENUM_CONSOLE_MSG_SEVERITY_INFO__,
                                                            originator: __RWS_CLIENT_NAME__,
                                                            message: "Nothing to validate\n"
                                                        });
                }
                endTask();
            }
    );
}


export function resquestCorrection(idConsoleElt, rootUUID, correctionType){
    var url = "ResqmlCorrection";
    url += "?correctionType=" + correctionType;
    if(rootUUID != null)
    {
        url += "&uuid=" + rootUUID;
    }

    //console.log("correction request at url " + url);
    beginTask();
    
    return sendGetURL_Promise(url).then(
            function(responseText){
                //console.log("Response is : " + responseText)
                try{
                    var correctionJSON = JSON.parse(responseText);

                    var msgList = [];
                    msgList.push(
                        { 
                            severity: __ENUM_CONSOLE_MSG_SEVERITY_INFO__,
                            originator: __RWS_CLIENT_NAME__,
                            message: "CORRECTIONS : " + correctionJSON.length + "\n"
                        });

                    for(var validMsgIdx=0; validMsgIdx<correctionJSON.length; validMsgIdx++){
                        var msgCorrect = correctionJSON[validMsgIdx];
                        var msgContent = msgCorrect.date + " : " + msgCorrect.title 
                                        + " for " +  msgCorrect.rootType 
                                        + " with uuid " + msgCorrect.rootUUID 
                                        + " and title " + msgCorrect.rootTitle
                                        + "\n";
                        msgContent += "\t" + msgCorrect.msg +"\n";
                        msgList.push(
                            { 
                                severity: __ENUM_CONSOLE_MSG_SEVERITY_WARNING__,
                                originator: __RWS_SERVER_NAME__,
                                message: msgContent
                            });
                    }
                    appendConsoleMessage(idConsoleElt, msgList);
                }catch(exceptResponse){
                    console.log(exceptResponse);
                    console.log("no jsonable text : ");
                    console.log(responseText);
                }
                endTask();
            }
    );
}


export function genObjectPropertyElementId(uuid){
    return "resqml_" + uuid + "_properties";
}

export function genObjectContentElementId(uuid){
    return "resqml_" + uuid + "_content";
}
export function genObjectContentDivElementId(uuid){
    return "tabulation_id_" + uuid;
}



// Preferences
export function updateVueFromPreferences(){
    var url = "Preferences";

    beginTask();
    
    return sendGetURL_Promise(url).then(
            function(responseText){
                try{
                    var prefJson = JSON.parse(responseText);

                    if(prefJson["vueOrientation"] != null){
                        setVueOrientation(prefJson["vueOrientation"]);
                    }
                    
                }catch(exceptResponse){
                    console.log(exceptResponse);
                    console.log("no jsonable text : ");
                    console.log(responseText);
                }
                endTask();
            }
    );
}

export function savePreferences(){
    var url = "Preferences";
    
    return sendPostRequestJson(url, {
        "vueOrientation": getVueOrientation()
    });
}