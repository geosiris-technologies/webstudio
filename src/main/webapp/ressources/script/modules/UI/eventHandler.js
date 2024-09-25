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

import {createSnackBar, createToast} from "./snackbar.js"
import {appendConsoleMessage} from "../logs/console.js"
import {beginTask, endTask, refreshHighlightedOpenedObjects} from "./ui.js"
import {closeResqmlObjectContentByUUID, loadResqmlData} from "../main.js"
import {getAllOpendedObjects} from "../requests/uiRequest.js"
import {sendGetURL_Promise, sendPostForm_Promise, getJsonObjectFromServer} from "../requests/requests.js"
import {__ENUM_CONSOLE_MSG_SEVERITY_ACTION__, __ID_CONSOLE_MODAL__} from "../common/variables.js"
import {beginETPRequest, updateGetRelatedETPTableContent, endETPRequest} from "./modals/etp.js";
import {update_etp_connexion_views, update_dataspaces_inputs} from "../etp/etp_connection.js";


export let hasBeenDisconnected = false;

export function rws_addEventListeners(eventTag, f_onMessage, f_onError){
    console.log("Adding event listener for '" + eventTag + "'");

    try{
        const rws_eventSource = new EventSource("EPCEvent?event="+eventTag);
        //rws_eventSource.addEventListener(eventTag, f_onMessage, true);

        rws_eventSource.onerror = (e) => {
            if(!hasBeenDisconnected){
                hasBeenDisconnected = true;
                var msg = rws_eventSource.readyState + "] Connexion lost with the server. Please try to refresh the page";
                // createSnackBar(msg, -1);
                createToast(
                    {
                        title: "Event handler",
                        time: (new Date(Date.now())).toLocaleTimeString('en-US'),
                        body: msg,
                        option: {
                          animation: true,
                          autohide: false,
                          delay: 10000
                        }
                    }
                );
            }
            if(f_onError!=null){
                f_onError(e);
            }
            
            try {
                rws_eventSource.close();
            } catch(e) {
                if(!hasBeenDisconnected){
                    console.log(e);
                }
            }
            //rws_addEventListeners(eventTag, f_onMessage, f_onError);
        };

        rws_eventSource.onmessage = (e) => {
            hasBeenDisconnected = false;
            //console.log("#rws_addEventListeners : on message : '" + eventTag + "' => ");
            //console.log(e);
            if(f_onMessage!=null){
                f_onMessage(e);
            }
        };
    }catch(exception){
        console.log(exception);
        hasBeenDisconnected = true;
        var msg = "Connexion lost with the server. Please try to refresh the page";
        // createSnackBar(msg, -1);
        createToast(
            {
                title: "Event handler",
                time: (new Date(Date.now())).toLocaleTimeString('en-US'),
                body: msg,
                option: {
                  animation: true,
                  autohide: false,
                  delay: 10000
                }
            }
        );
    }
}

export function initSessionLogEventHandler(console_id){
    // const source = new EventSource("?event=logs");
    const console_elt = document.getElementById(console_id);

    const evt_list_onMessage = function(event) {
        // Decode message
        try{
            var decodedMsg = JSON.parse(window.atob(event.data));
            if(decodedMsg.severity != null && decodedMsg.severity == __ENUM_CONSOLE_MSG_SEVERITY_ACTION__){
                if(decodedMsg.message && decodedMsg.message.toLowerCase() == "reload"){
                    refreshWorkspace().catch((error) => console.error(error));
                }else if(decodedMsg.message && decodedMsg.message.toLowerCase() == "updatedataspace"){
                    update_dataspaces_inputs(null, true);
                }
            }else{
                //console.log("Not an action '" + decodedMsg.severity + "' != '" + __ENUM_CONSOLE_MSG_SEVERITY_ACTION__ + "'")
                appendConsoleMessage(__ID_CONSOLE_MODAL__, decodedMsg);
            }
        }catch(exception){
            console.log(exception);
        }
        //console.log("EVENT: ");
        //console.log(decodedMsg);
    };
    rws_addEventListeners('logs', evt_list_onMessage, null);
}


export function initSessionMetrics(spanSessionMetrics_id){
    let spanSessionCounter = document.getElementById(spanSessionMetrics_id);
    spanSessionCounter.title = "Your session weight";

    const evt_list_onMessage = function(event) {
        let span = document.getElementById(spanSessionMetrics_id);
        var decodedMsg = window.atob(event.data);
        span.textContent = decodedMsg;
    };

    const evt_list_onErr = function(e) {
        let span = document.getElementById(spanSessionMetrics_id);
        span.textContent = "T_T";
    };

    rws_addEventListeners('sessionMetrics', evt_list_onMessage, evt_list_onErr);
}

/*  ____       ____               __
   / __ \___  / __/_______  _____/ /_
  / /_/ / _ \/ /_/ ___/ _ \/ ___/ __ \
 / _, _/  __/ __/ /  /  __(__  ) / / /
/_/ |_|\___/_/ /_/   \___/____/_/ /_/
*/

export function refreshWorkspace(){
    beginTask();
    console.log("refreshing workspace")
    return loadResqmlData().then(
        function(){
            endTask();
            const openedObjects = getAllOpendedObjects();
            // On remet a jour le tableau après la sauvegarde au cas
            // où le nom d'un element aurait changé
            for(var i=0; i<openedObjects.length; i++){
                const currentObj = openedObjects[i];
                try{
                    currentObj.resqmlElt.refresh().catch(err => {
                        closeResqmlObjectContentByUUID(currentObj.resqmlElt.rootUUID);
                    });
                }catch(exep_notRefreshed){
                    closeResqmlObjectContentByUUID(currentObj.resqmlElt.rootUUID);
                }
            }
            if($('#modal_ETP').hasClass('show')){
                beginETPRequest();
                getJsonObjectFromServer("ResqmlEPCRelationship").then(
                    function(relations){;
                        updateGetRelatedETPTableContent(relations);
                        endETPRequest();
                        update_etp_connexion_views();
                    }).catch((error) => {
                        console.error(error);
                        endETPRequest();
                });
            }
        }, 
        function(){
            console.log("2) error occured in sendGetURLAndReload")
        }
    ).then(()=>endTask());
}

export function sendGetURLAndReload(url, showRequestResult){
    // console.log("debut reload get")
    return sendGetURL_Promise(url).then(
        function(){
            return refreshWorkspace();
        }, 
        function(){
            console.log("1) error occured in sendGetURLAndReload")
        });
}

export function sendPostFormAndReload(form, url, showRequestResult, functAfterReload){
    const openedObjects = getAllOpendedObjects();

    beginTask();
    return sendPostForm_Promise(form, url, showRequestResult).then(
        function(){
            endTask();
            // while(endTask()>0){} // Attention, si une tache est en cours, le loadResqmlData ne marche pas !
            return loadResqmlData().then( 
                function(){
                    // On remet à jour le tableau après la sauvegarde au cas
                    // où le nom d'un element aurait changé
                    for(var i=0; i<openedObjects.length; i++){
                        const currentObj = openedObjects[i];
                        try{
                            currentObj.resqmlElt.refresh().catch(err => closeResqmlObjectContentByUUID(currentObj.resqmlElt.rootUUID));
                        }catch(exep_notRefreshed){
                            closeResqmlObjectContentByUUID(currentObj.resqmlElt.rootUUID);
                        }
                    }
                    //console.log("opened tabs "); console.log(openedObjects);
                    if(functAfterReload!=null){
                        functAfterReload();
                    }
                    refreshHighlightedOpenedObjects();
                });
        }).catch(() => endTask());
}