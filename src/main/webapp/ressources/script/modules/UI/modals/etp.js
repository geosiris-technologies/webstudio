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

import {create_dataspace_input, geosiris_createETP_connector_form, update_etp_connexion_views} from "../../etp/etp_connection.js";
import {modal_getParentContainer, sendForm} from "./modalEntityManager.js";
import {importObjectIn3DView} from "./misc.js";
import {appendConsoleMessage} from "../../logs/console.js";
import {call_after_DOM_updated, createSelector} from "../htmlUtils.js";
import {sendPostForm_Promise, sendPostRequestJson_Promise, sendPostRequest} from "../../requests/requests.js";
import {resquestValidation} from "../../energyml/epcContentManager.js";
import {createTableFromData, transformTabToFormCheckable} from "../table.js";
import {JsonTableColumnizer_Checkbox, JsonTableColumnizer_DotAttrib, toTable} from "../jsonToTable.js";
import {checkAllRelations} from "./exportEPC.js";
import {refreshHighlightedOpenedObjects} from "../ui.js";
import {REGEX_ETP_URI, __ENUM_CONSOLE_MSG_SEVERITY_TOAST__, __ID_CONSOLE__, __RWS_CLIENT_NAME__, CLASS_TABLE_FIXED} from "../../common/variables.js";
import {removeListDuplicatesByObjectKey, getAttribute} from "../../common/utils.js";
import {geo3DVue, openResqmlObjectContentByUUID} from "../../main.js";


var ETP_REQUEST_LAUNCH = 0

export function beginETPRequest(){
    ETP_REQUEST_LAUNCH ++;
    _updateETP_progressBar();

}
export function endETPRequest(){
    ETP_REQUEST_LAUNCH --;
    _updateETP_progressBar();
}
export function _updateETP_progressBar(){
    if(ETP_REQUEST_LAUNCH > 0){
        document.getElementById("ETPRequest_send_progressBar").style.display ="";
    }else{
        document.getElementById("ETPRequest_send_progressBar").style.display ="none";
    }
}

export function init_etp_forms(){

    // Connexion
    const modal_ETP_form_connection = geosiris_createETP_connector_form(
        function(){setEnableETPButtons(true);}, 
        function(){setErrorsVisibility(null, true);setEnableETPButtons(false);}, 
        function(){document.getElementById('rolling_ETP').style.display = ""}, 
        function(){document.getElementById('rolling_ETP').style.display = "none"});

    document.getElementById("modal_ETP_form_div").appendChild(modal_ETP_form_connection);
    document.getElementById("modal_ETP_form_div").append(create_dataspace_input(null, ["etp_dataspace_values"]));

    // Initiate

    $("#modal_ETP").on('show.bs.modal', function(){
        update_etp_connexion_views();
    });

    setErrorsVisibility(null, false);
    setEnableETPButtons(false);

    // Dataspaces list
    var form_ids = Array.prototype.slice.call(document.querySelectorAll('*')).filter(function (el) { 
        return el.id.match("ETPRequest.*_[Ff]orm$");
    });

    for(var f_idx in  form_ids){
        var f_elt = form_ids[f_idx];
        var dataspace_value_hidden = document.createElement("input");
        dataspace_value_hidden.name = "dataspace";
        dataspace_value_hidden.hidden = "hidden";
        dataspace_value_hidden.className = "etp_dataspace_values";
        f_elt.insertBefore(dataspace_value_hidden, f_elt.firstChild);
    }
}


export function setErrorsVisibility(currentElement, visibleValue){
    var rootElt = document.getElementById("modal_ETP");
    if(currentElement != null){
        rootElt = modal_getParentContainer(currentElement);
    }

    if(rootElt!=null){
        // console.log("updating errors");
        // const setVisible = visibleValue;
        var els = rootElt.getElementsByClassName("msg-error");
        Array.prototype.forEach.call(els, function(elt) {
            elt.style.display = visibleValue ? "" : "none";
        });
    }
}


export function setEnableETPButtons(enableValue){
    var rootElt = document.getElementById("modal_ETP");
    if(rootElt!=null){
        //console.log("=> " + enableValue)
        var els = rootElt.getElementsByClassName("geosiris-btn-etp");
        Array.prototype.forEach.call(els, function(elt) {
            elt.disabled = !enableValue;
            if(!enableValue){
                elt.title = "Please connect to ETP server";
            }else{
                elt.title = "";
            }
        });
    }

}

// Fin partie ETP connexion


export function launchActivity(ce_type_elt_id){
    var formETPObjectList = document.getElementById("ETPRequest_import_Form-etp_object_list")
    var dataspace = document.getElementById("etp_dataspace_import").value
    var checkedUris = $("#ETPRequest_import_Form-etp_object_list  input:checked").map((i,e) => $(e).val())
    .toArray().map(uri => {
            if(!uri.includes("dataspace")){
                if(dataspace != null && dataspace.length > 0){
                    uri = uri.replace("eml:///", "eml:///dataspace('" + dataspace + "')/")
                }
            }
            return uri;
        });
    if(checkedUris != null && checkedUris.length > 0){
        var ce_type = document.getElementById(ce_type_elt_id).value;
        var data = {"uris": checkedUris, "ce-type": ce_type};

        return sendPostRequestJson_Promise("/LaunchActivityWorkflow", data, true);
    }

}
$("#ETPRequest_import_Form-etp_object_list  input:checked").map((i,e) => $(e).val()).toArray()
export function loadUrisIn3DVue(){
    var formETPObjectList = document.getElementById("ETPRequest_import_Form-etp_object_list")
    var dataspace = document.getElementById("etp_dataspace_import").value
    var checkedUris = $("#ETPRequest_import_Form-etp_object_list  input:checked").map((i,e) => $(e).val())
    .toArray().map(uri => {
            if(!uri.includes("dataspace")){
                if(dataspace != null && dataspace.length > 0){
                    uri = uri.replace("eml:///", "eml:///dataspace('" + dataspace + "')/")
                }
            }
            return uri;
        });

    if(checkedUris != null && checkedUris.length > 0){
        importObjectIn3DView(checkedUris).then(fileContent => {
            try{
                var jsonValueList = JSON.parse(fileContent);
                console.log("3D vue is Loading " + jsonValueList.length + " entities");
                console.log(jsonValueList);
                jsonValueList.forEach(obj =>{
                //console.log(obj);
                    // console.log("EPSG : " + obj["epsgCode"]);
                    try{
                        geo3DVue.importSurface(
                          obj["data"],
                          obj["fileType"],
                          obj["type"],
                          obj["uuid"],
                          obj["title"],
                          obj["pointColor"],
                          obj["lineColor"],
                          obj["faceColor"],
                          obj["epsgCode"]
                        );
                    }catch(exception){
                        console.log(fileContent);
                        console.log(exception);
                        appendConsoleMessage(__ID_CONSOLE__, {
                            severity: __ENUM_CONSOLE_MSG_SEVERITY_TOAST__,
                            originator: "3D Vue",
                            message: "Error while importing 3D object " + obj["uuid"]
                        });
                    }
                });
            }catch(exception){
                console.log(fileContent);
                console.log(exception);
            }
        }).catch((error) => console.error(error));
    }

}

export function loadETPObjectList(eltId_objectList, eltId_formRequest){
    // Progressbar
    beginETPRequest();

    call_after_DOM_updated(function(){

        const divETP = document.getElementById(eltId_objectList);
        while(divETP.firstChild){
            divETP.removeChild(divETP.firstChild)
        }

        const formImportETPobjects  = document.createElement("form");
        formImportETPobjects.method = "post";
        formImportETPobjects.action = "ETPRequest";

        const formInputRequest = document.createElement("input");
        formInputRequest.hidden = "hidden";
        formInputRequest.name = "request";
        formInputRequest.value = "import";
        formInputRequest.className = "geosiris-btn-etp"
        formImportETPobjects.appendChild(formInputRequest);


        try{
            const formInputDataspace = document.createElement("input");
            formInputDataspace.id = "etp_dataspace_import";
            formInputDataspace.hidden = "hidden";
            formInputDataspace.name = "dataspace";
            formInputDataspace.value = document.getElementsByClassName("etp_select_dataspace")[0].value;
            formInputDataspace.className = "form-control geosiris-btn-etp";
            formInputDataspace.style.width = "fit-content";
            formImportETPobjects.appendChild(formInputDataspace);
        }catch(e){}

        const formDivTable = document.createElement("div");
        formDivTable.className="modal_tab_table";
        formImportETPobjects.appendChild(formDivTable);

        sendPostForm_Promise(document.getElementById(eltId_formRequest), "ETPRequest", false).then(
            uriAsJsonList => {
                beginETPRequest();
                // console.log("Recievec etp object list : ");
                //console.log(uriAsJsonList);
                try{
                    var tableETP = JSON.parse(uriAsJsonList);
                    var eltTable = createETPImportObjects(tableETP);
                    // console.log(eltTable);
                    if(eltTable != null){
                        formDivTable.appendChild(eltTable);
                    }
                    while(divETP.firstChild){
                        divETP.removeChild(divETP.firstChild)
                    }
                    divETP.appendChild(formImportETPobjects); // On ajoute qu'une fois tout fini
                    setErrorsVisibility(divETP, false);
                }catch(e){
                    console.log("Json input : ");
                    console.log(uriAsJsonList);
                    console.log(e);
                }
                endETPRequest();

                update_etp_connexion_views();
            }
        );

        var div_but_import = document.createElement("div");
        div_but_import.className = "input-group";
        formImportETPobjects.appendChild(div_but_import);

        const formSubmit_import = document.createElement("input");
        formSubmit_import.value = "ImportDataObject";
        formSubmit_import.type = "button";
        formSubmit_import.className = "btn btn-primary geosiris-btn-etp";
        formSubmit_import.onclick = function(){
                                sendForm(formImportETPobjects, 'modal_ETP', 'rolling_ETP',
                                    false, false, true, false).then(
                                        function(){resquestValidation(__ID_CONSOLE__, null);});
                            }
        formSubmit_import.appendChild(document.createTextNode("import"));
        div_but_import.appendChild(formSubmit_import);


        
        const formSubmit_delete = document.createElement("input");
        formSubmit_delete.value = "Delete data object";
        formSubmit_delete.type = "button";
        formSubmit_delete.className = "btn btn-danger geosiris-btn-etp";
        formSubmit_delete.onclick = function(){
                                formInputRequest.value = "deletedataobject";
                                sendForm(formImportETPobjects, 'modal_ETP', 'rolling_ETP', 
                                    false, false, false, false).then(
                                        function(){
                                            formInputRequest.value = "import";
                                            loadETPObjectList('ETPRequest_import_Form-etp_object_list', 
                                            'ETPRequest_import_Form');
                                        }
                                    );
                            }
        formSubmit_delete.appendChild(document.createTextNode("Delete data object"));
        div_but_import.appendChild(formSubmit_delete);


        const formSubmit_visualize = document.createElement("input");
        formSubmit_visualize.value = "Visualize data object";
        formSubmit_visualize.type = "button";
        formSubmit_visualize.className = "btn btn-info geosiris-btn-etp";
        formSubmit_visualize.onclick = function(){
                                loadUrisIn3DVue();
                            }
        formSubmit_visualize.appendChild(document.createTextNode("Visualize data object"));
        div_but_import.appendChild(formSubmit_visualize);

        // activity launcher
        const launch_select_id = "etp_select_activity_type";
        const div_activityLaunch = createSelector(["Process_0hv5t1z.started", "MULTIPLE_MBA.started"], "ce-type", launch_select_id);

        const formSubmit_launch_activity = document.createElement("input");
        formSubmit_launch_activity.value = "Launch activity";
        formSubmit_launch_activity.type = "button";
        formSubmit_launch_activity.className = "btn btn-info geosiris-btn-etp";
        formSubmit_launch_activity.onclick = function(){
                                launchActivity(launch_select_id).catch((error) => console.error(error));
                            }

        formSubmit_launch_activity.appendChild(document.createTextNode("Launch activity"));
        div_activityLaunch.appendChild(formSubmit_launch_activity);
        formImportETPobjects.appendChild(div_activityLaunch);

        endETPRequest();
    });
}


export function createETPImportObjects(urisAsTable){
    var attrib_list = []
    if(urisAsTable.resources != null){
        urisAsTable = urisAsTable.resources;
    }

    urisAsTable = removeListDuplicatesByObjectKey(urisAsTable, "uri");

    urisAsTable.forEach((item, i) => {
        try{
            item["storeCreated"] = (new Date(item["storeCreated"]*1000)).toGMTString();
        }catch(exception){}
        try{
            item["storeLastWrite"] = (new Date(item["storeLastWrite"]*1000)).toGMTString();
        }catch(exception){}

        try{

            for (const [key, value] of Object.entries(transformETPuriAsObj(item["uri"]))) {
                item[key] = value;
            }
        }catch(exception){}
    });


    if(urisAsTable!=null && urisAsTable.length > 0 && urisAsTable[0].related instanceof Array){
        // Si on est sur un tableau de related
        attrib_list = ["title", "type", "uuid", "uri", "related", "storeCreated", "storeLastWrite", "pkg"];

    }else if(urisAsTable!=null && urisAsTable.resources != null){
        attrib_list = ["title", "type", "uuid", "uri", "storeCreated", "storeLastWrite", "pkg"];
       
    }else if(urisAsTable!=null && urisAsTable.length > 0){
        attrib_list = ["name", "type", "uuid", "storeLastWrite", "uri", "storeCreated", "pkg"];
    }

    const f_cols = []

    const name_id = "etp_uri";
    const col_check = new JsonTableColumnizer_Checkbox(name_id, (obj) => getAttribute(obj, "uri"), null, (elt)=> elt["uuid"]+ "-tab",);
    f_cols.push(col_check);

    attrib_list.forEach(
        (attrib) => {
            f_cols.push(
                new JsonTableColumnizer_DotAttrib(
                    attrib=="name"?"Title":attrib.substring(0, 1).toUpperCase() + attrib.substring(1),
                    attrib,
                    function(event, elt){
                        if(event.type == "click"){
                            // openResqmlObjectContentByUUID(elt['uuid']);  // On ne veut pas ouvrir dans la vue arriere depuis la vue etp
                        }
                    },
                    null,
                    (elt)=>elt["uuid"]+ "-tab",
                    null,
                    "pointer"
                )
            );
        }
    );
    var tableETP = toTable(urisAsTable, f_cols);
    tableETP.className += CLASS_TABLE_FIXED;

    return tableETP;
}

export function transformETPuriAsTable(uri){
    var groups = uri.match(REGEX_ETP_URI).groups;

    try { 
        return [groups.domain + groups.domainVersion, groups.objectType, groups.uuid, uri]
    } catch(e) { console.log(e); }

    
    return ["--", "--", "--", "--"]
}

export function transformETPuriAsObj(uri){
    var groups = uri.match(REGEX_ETP_URI).groups;

    try { 
        return {
            "pkg": groups.domain + groups.domainVersion, 
            "type": groups.objectType, 
            "uuid": groups.uuid, 
            "uri": uri
        }
    } catch(e) { console.log(e); }

    
    return {}
}

export function etp_triggerViewUpdateOnResourceChecked(etpTable, checkedStatus, uri){
    var enableLoadIn3D = false;

    etpTable.querySelectorAll('input[type=checkbox]').forEach(
            (elt_check) =>{
                if(elt_check.checked){
                    if(elt_check.value.toLowerCase().includes("representation")){
                        enableLoadIn3D = true;
                    }
                }
            }
        )
}


/*  ______                      __
   / ____/  ______  ____  _____/ /_
  / __/ | |/_/ __ \/ __ \/ ___/ __/
 / /____>  </ /_/ / /_/ / /  / /_
/_____/_/|_/ .___/\____/_/   \__/
          /_/
*/

export function createExportToETPView(jsonContent, checkboxesName, f_OnCheckToggle){
    //document.getElementById("ETPRequest_send_Form-etp-server-ip").value = "localhost"
    var tableDOR = createTableFromData(
        jsonContent, 
        ["num", "title", "type", "uuid", "schemaVersion"], 
        ["Num", "Title", "Type", "UUID", "SchemaVersion"], 
        null, null, null);

    transformTabToFormCheckable(tableDOR, jsonContent.map(elt => elt.uuid), checkboxesName, f_OnCheckToggle);
    return tableDOR;
}

export function updateExportToETPTableContent(relations){
    beginETPRequest();
    call_after_DOM_updated( () => {
        const elt_obj_list = document.getElementById("ETPRequest_send_workspace_objList");
           while (elt_obj_list.firstChild) {
               elt_obj_list.removeChild(elt_obj_list.firstChild);
           }

        var tableContent = [];
        for(var uuid in relations){
            tableContent.push(relations[uuid]);
        }

        const checkboxesName = "exportToETP_UUID";

        const f_cols = []

        const col_check = new JsonTableColumnizer_Checkbox(
            checkboxesName, (obj) => getAttribute(obj, "uuid"),
            function(checkedValue, uuid){
                beginETPRequest();
                if(checkedValue && relations[uuid] != null){
                    checkAllRelations(uuid, relations, checkboxesName, checkedValue,
                                        "epcETPRequest_send_checkUpRelations",
                                        "epcETPRequest_send_checkDownRelations");
                }else{
                    // console.log("err > checkedValue " + checkedValue  + " -- " + uuid);
                }

                endETPRequest();
            },
            (elt)=> elt["uuid"]+ "-tab",
        );
        f_cols.push(col_check);

        ["title", "type", "uuid", "schemaVersion"].forEach(
            (attrib) => {
                f_cols.push(
                    new JsonTableColumnizer_DotAttrib(
                        attrib=="name"?"Title":attrib.substring(0, 1).toUpperCase() + attrib.substring(1),
                        attrib,
                        function(event, elt){
                            if(event.type == "click"){
                                // openResqmlObjectContentByUUID(elt['uuid']);  // On ne veut pas ouvrir dans la vue arriere depuis la vue etp
                            }
                        },
                        null,
                        (elt)=>elt["uuid"]+ "-tab",
                        null,
                        "pointer"
                    )
                );
            }
        );
        var tableETP = toTable(tableContent, f_cols);
        tableETP.className += CLASS_TABLE_FIXED;


        elt_obj_list.appendChild(tableETP);

        //return tableETP;

        refreshHighlightedOpenedObjects();
        endETPRequest();
        update_etp_connexion_views();
    });
}

export function updateGetRelatedETPTableContent(relations){
    beginETPRequest();

    call_after_DOM_updated( () => {
        const elt_obj_list = document.getElementById("ETPRequest_getRelated_workspace_objList");
           while (elt_obj_list.firstChild) {
               elt_obj_list.removeChild(elt_obj_list.firstChild);
           }

        var tableContent = [];
        for(var uuid in relations){
            tableContent.push(relations[uuid]);
        }

        const checkboxesName = "getrelated_UUID";

        // on doit convertir en liste pour creer le tableau
        const table = createExportToETPView(tableContent, checkboxesName, null);

        
        elt_obj_list.appendChild(table);
        refreshHighlightedOpenedObjects();
        endETPRequest();
        update_etp_connexion_views();
    });
}



export function populate_getRelated(formId, divResId, importRelatedFormId){
    // Progressbar
    beginETPRequest();

    call_after_DOM_updated(function(){

        var divETP = document.getElementById(divResId);
        while(divETP.firstChild){
            divETP.removeChild(divETP.firstChild)
        }

       /* const formImportETPobjects  = document.createElement("form");
        formImportETPobjects.method = "post";
        formImportETPobjects.action = "ETPRequest";*/

        const formSubmit = document.createElement("input");
        formSubmit.value = "import";
        formSubmit.type = "button";
        formSubmit.className = "btn btn-primary";
        formSubmit.onclick = function(){
                                sendForm(document.getElementById(importRelatedFormId), 'modal_ETP', 'rolling_ETP', false, false, false, false).then(
                                    function(){resquestValidation(__ID_CONSOLE__, null);});
                             }
        formSubmit.appendChild(document.createTextNode("import"));
        //formImportETPobjects.appendChild(formSubmit);

        /*const formInputRequest = document.createElement("input");
        formInputRequest.hidden = "hidden";
        formInputRequest.name = "request";
        formInputRequest.value = "import";
        formImportETPobjects.appendChild(formInputRequest);*/

        

        const formDivTable = document.createElement("div");
        formDivTable.className="modal_tab_table";
        //formImportETPobjects.appendChild(formDivTable);

        sendPostForm_Promise(document.getElementById(formId), "ETPRequest", false).then(
            uriAsJsonList => {
                try{
                    document.getElementById(importRelatedFormId).style.display = "";
                    var tableETP = JSON.parse(uriAsJsonList);
                    var eltTable = createETPImportObjects(tableETP);
                    if(eltTable != null){
                        formDivTable.appendChild(eltTable);
                    }
                    while(divETP.firstChild){
                        divETP.removeChild(divETP.firstChild)
                    }
                    //divETP.appendChild(formImportETPobjects); // On ajoute qu'une fois tout fini
                    divETP.appendChild(formSubmit);
                    divETP.appendChild(formDivTable);
                }catch(e){
                    console.log("ERR : Json input : ");
                    console.log(uriAsJsonList);
                    console.log(e);
                }
                update_etp_connexion_views();
            }
        );
        endETPRequest();            
    });
}