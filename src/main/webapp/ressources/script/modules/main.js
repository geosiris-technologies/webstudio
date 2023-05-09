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

import {enableWaitingUi, initEditorContent, setVueOrientation} from "./UI/ui.js"
import {initSessionLogEventHandler, initSessionMetrics} from "./UI/eventHandler.js"
import {updateTypesMap, updateVueFromPreferences} from "./energyml/epcContentManager.js"
import {refreshPropertyDictVue, refreshWorkspaceDictVue} from "./UI/modals/propertiesVue.js"
import {__initOrganizationType__, updateFIRPView} from "./UI/modals/firpVue.js"
import {init_etp_forms, beginETPRequest, updateExportToETPTableContent,
        updateGetRelatedETPTableContent, endETPRequest} from "./UI/modals/etp.js"
import {update_etp_connexion_views} from "./etp/etp_connection.js"
import {rws_addConsoleMessageFilter} from "./logs/console.js"
import {updatePartialExportTableContent} from "./UI/modals/exportEPC.js"
import {createSplitter} from "./UI/htmlUtils.js"
import {openResqmlObjectContent} from "./requests/uiRequest.js"
import {getJsonObjectFromServer} from "./requests/requests.js"
import {closeTabulation, getOpenObjectsUuid_GivingTabHeader, saveResqmlObject_promise, saveAllResqmlObject_promise} from "./UI/tabulation.js"
import {__ID_CONSOLE__, __ID_EPC_TABLE_DIV__, __ID_EPC_TABS_CONTAINER__, __ID_EPC_TABS_HEADER__} from "./common/variables.js"
import {createSnackBar} from "./UI/snackbar.js"

import {GeoThreeJS, fun_import_surface} from "./UI/lib/geo-threejs/geo-threejs.js";

export let geo3DVue = null;

export function initWebStudioView(){
    createSplitter("#cyGrapher","#graphElementChecked", 65, 30, null, 55, 16);
    try{setVueOrientation("right", false);}catch(err){console.log(err);}
    try{initSessionLogEventHandler(__ID_CONSOLE__);}catch(err){console.log(err);}
    refreshPropertyDictVue().catch((error) => console.error(error));
    refreshWorkspaceDictVue().catch((error) => console.error(error));
    try{updateTypesMap();}catch(err){console.log(err);}
    try{__initOrganizationType__();}catch(err){console.log(err);}

    // Vues

    try{updateFIRPView();}catch(err){console.log(err);}
    try{init_etp_forms();}catch(err){console.log(err);}


    $( document ).ready(function() {
        rws_addConsoleMessageFilter(__ID_CONSOLE__);
    });

    $("#modal_exportEPC").on('show.bs.modal', function(){
           // OnModalShow
           // On met a jour le tableau
        document.getElementById("exportParial_progressBar").style.display = "";
        const elt_partialExportEPC_table = document.getElementById("partialExportEPC_table");
        while (elt_partialExportEPC_table.firstChild) {
            elt_partialExportEPC_table.firstChild.remove();
        }
       
        getJsonObjectFromServer("ResqmlEPCRelationship").then(function(relations){
            updatePartialExportTableContent(relations);
        }).catch((error) => console.error(error));
    });

    try{
        updateVueFromPreferences()
            .then(loadResqmlData)
            .then(()=>{
                enableWaitingUi(false);
            }).catch((error) => console.error(error));
    }catch(err){console.log(err);}
    
    $("#visu-3D-iframe").on("load", function() {
        let head = $("#visu-3D-iframe").contents().find("head");
        let css = '<style>#unity-canvas{'
                + '    width:  100%!important;'
                + '    height:  100%!important;'
                + '}'
                + '#unity-container{'
                + '    width:  80%!important;'
                + '    height:  60%!important;'
                + '}</style>';
        $(head).append(css);
    });

    $(document).ready(function() {
        init3DVue(document.getElementById("geo3DVue"));
        $('.dropright button').on("click", function(e) {
            e.stopPropagation();
            e.preventDefault();

            if (!$(this).next('div').hasClass('show')) {
                $(this).next('div').addClass('show');
            } else {
                $(this).next('div').removeClass('show');
            }
        });


        initSessionMetrics("ws_sessionMetrics");

    });

    $("#modal_ETP").on('show.bs.modal', function(){
       // OnModalShow
       // On met a jour le tableau
       beginETPRequest();
       getJsonObjectFromServer("ResqmlEPCRelationship").then(function(relations){;
            updateExportToETPTableContent(relations);
            updateGetRelatedETPTableContent(relations);
            endETPRequest();
            update_etp_connexion_views();
        }).catch((error) => console.error(error));
    });

    /* Other actions */

    document.addEventListener('keydown', e => {
        //console.log(e);
        if (e.ctrlKey && e.key == 's') {
            // Prevent the Save dialog to open
            e.preventDefault();
            // Place your code here
            createSnackBar("Saving all opened objects");
            saveAllResqmlObject_promise();
        }else if(e.ctrlKey && e.key == 'e') {
            e.preventDefault();
            $("#modal_exportEPC").modal();
        }else if(e.ctrlKey && (e.key == 'i' || e.key == 'o')) {
            e.preventDefault();
            $("#modal_import_partialEPC").modal();
        }
    });
}


export function loadResqmlData(){
    return initEditorContent('epcTableContent', __ID_EPC_TABLE_DIV__, __ID_EPC_TABS_HEADER__, __ID_EPC_TABS_CONTAINER__, __ID_CONSOLE__, 'objectContent', 'entityProperty', 'filter_tableFilter_EPCView');
}


export function openResqmlObjectContentByUUID(uuid){
    //console.log("opening " + uuid)
    return openResqmlObjectContent(uuid, __ID_EPC_TABS_HEADER__, __ID_EPC_TABS_CONTAINER__, __ID_CONSOLE__, 'objectContent', 'entityProperty');
}


export function closeResqmlObjectContentByUUID(uuid){
    closeTabulation(uuid, __ID_EPC_TABS_HEADER__);
}


export function saveResqmlObjectByUUID(uuid){
    console.log("saving " + uuid)
    return saveResqmlObject_promise(uuid, __ID_EPC_TABS_HEADER__);
}


export function getOpenObjectsUuid(){
    return getOpenObjectsUuid_GivingTabHeader(__ID_EPC_TABS_HEADER__);
}


export function init3DVue(parentElt){
    const fileDiv = document.createElement("div");
    const fileInput = document.createElement("input");
    const fileBut = document.createElement("button");

    fileInput.type = "file";

    fileBut.appendChild(document.createTextNode("Import"))
    fileBut.onclick = function(){
        try{
            Array.from(fileInput.files).forEach( file => {
                if (file) {
                    var reader = new FileReader();
                    reader.readAsText(file, "UTF-8");
                    reader.onload = function (evt) {
                        fun_import_surface(geo3DVue, evt.target.result, file.name);
                    }
                    reader.onerror = function (evt) {
                        document.getElementById("fileContents").innerHTML = "error reading file";
                    }
                }
            });
        }catch(Except){console.log(Except);}
    };

    fileDiv.appendChild(fileInput);
    fileDiv.appendChild(fileBut);

    parentElt.appendChild(fileDiv);

    geo3DVue = new GeoThreeJS(1200, 800);
    geo3DVue.createView(parentElt);
}
