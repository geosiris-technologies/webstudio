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

import {create_dataspace_input, geosiris_createETP_connector_form, update_etp_connexion_views} from "../../etp/etp_connection.js"
import {modal_getParentContainer, sendForm} from "./modalEntityManager.js"
import {importObjectIn3DView} from "./misc.js"
import {appendConsoleMessage} from "../../logs/console.js"
import {call_after_DOM_updated} from "../htmlUtils.js"
import {sendPostForm_Promise, sendPostRequestJson_Promise} from "../../requests/requests.js"
import {resquestValidation} from "../../energyml/epcContentManager.js"
import {createTableFromData, transformTabToFormCheckable} from "../table.js"
import {checkAllRelations} from "./exportEPC.js"
import {refreshHighlightedOpenedObjects} from "../ui.js"
import {REGEX_ETP_URI, __ENUM_CONSOLE_MSG_SEVERITY_TOAST__, __ID_CONSOLE__, __RWS_CLIENT_NAME__} from "../../common/variables.js"


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

    // Initiate

    $("#modal_ETP").on('show.bs.modal', function(){
        modal_ETP_form_connection.updateView();
    });

    setErrorsVisibility(null, false);
    setEnableETPButtons(false);

    // Dataspaces list

    var form_ids = Array.prototype.slice.call(document.querySelectorAll('*')).filter(function (el) { 
        return el.id.match("ETPRequest.*_[Ff]orm$");
    });



    for(var f_idx in  form_ids){
        var f_elt = form_ids[f_idx];
        f_elt.insertBefore(create_dataspace_input(), f_elt.firstChild);
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

export function loadUrisIn3DVue(){
    var formETPObjectList = document.getElementById("ETPRequest_import_Form-etp_object_list")
    var dataspace = document.getElementById("etp_dataspace_import").value
    var checkedUris = [...formETPObjectList.querySelectorAll(".custom-checkbox input:checked")].map(elt => {
            var uri = elt.value;
            if(dataspace != null && dataspace.length > 0){
                if(!uri.includes("dataspace")){
                    uri = uri.replace("eml:///", "eml:///dataspace('" + dataspace + "')/")
                }
            }
            return uri;
        })
    var importType = "SURFACES";
    try{
        var selectImportType = document.getElementById("etpImport_importType");
        importType = selectImportType.options[selectImportType.selectedIndex].value;
    }catch(ex){console.log(ex);}

    var pointSize = "0.1";
    try{
        pointSize = document.getElementById("etpImport_pointSize").value;
    }catch(ex){console.log(ex);}

    if(checkedUris != null && checkedUris.length > 0){
        importObjectIn3DView(checkedUris, importType, pointSize).then(msg => appendConsoleMessage(__ID_CONSOLE__, { 
                            severity: __ENUM_CONSOLE_MSG_SEVERITY_TOAST__,
                            originator: __RWS_CLIENT_NAME__,
                            message: msg
                        }));
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
            formInputDataspace.value = document.getElementById(eltId_formRequest).getElementsByClassName("etp_select_dataspace")[0].value;
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
        formImportETPobjects.appendChild(formSubmit_import);


        
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
        formImportETPobjects.appendChild(formSubmit_delete);
        endETPRequest();
    });
}
export function createETPImportObjects(urisAsTable){
    // console.log(urisAsTable)
    var tableETP = null;
    if(urisAsTable!=null && urisAsTable.length > 0 && urisAsTable[0].related instanceof Array){
        // Si on est sur un tableau de related
        const tableObjRelated = [];
        urisAsTable.forEach( 
            obj =>
            {
                obj.related.forEach(uri => {
                    var tableUri = transformETPuriAsTable(uri)
                    tableUri.push(obj.uri);
                    tableObjRelated.push(tableUri);
                });
            }
        );

        const tableObjRelated_noDuplicates = [];

        // removing duplicates and morphing epoch datetimes
        tableObjRelated.forEach( 
            related_obj_table => {
                var found_uri_table = null;

                for(var idx_p=0; idx_p<tableObjRelated_noDuplicates.length; idx_p++){
                    if(tableObjRelated_noDuplicates[idx_p][3] == related_obj_table[3]){ // On compare les uri
                        found_uri_table = tableObjRelated_noDuplicates[idx_p];
                        break;
                    }
                }
                if(found_uri_table == null){
                    try{
                        tableObjRelated_noDuplicates["storeCreated"] = (new Date(tableObjRelated_noDuplicates["storeCreated"]*1000)).toGMTString()
                        tableObjRelated_noDuplicates["storeLastWrite"] = (new Date(tableObjRelated_noDuplicates["storeLastWrite"]*1000)).toGMTString()
                    }catch(exception){
                        console.log(exception);
                    }
                    tableObjRelated_noDuplicates.push(related_obj_table);
                }else if(!found_uri_table[4].includes(related_obj_table[4])){
                    found_uri_table[4] += " AND " + related_obj_table[4];
                }
            }
        );

        tableETP = createTableFromData(tableObjRelated_noDuplicates, 
                        ["pkg", "type", "uuid", "uri", "related", "storeCreated", "storeLastWrite"], 
                        ["Package", "Type", "UUID", "Uri", "Related to", "Store Created", "Store Last Write"], 
                        null, null, null);

        const cst_etpTable = tableETP;
        transformTabToFormCheckable(tableETP, tableObjRelated_noDuplicates.map(elt => elt[3]), "etp_uri", 
            (isChecked, uri) => {
                etp_triggerViewUpdateOnResourceChecked(cst_etpTable, isChecked, uri);
            });
    }else if(urisAsTable!=null && urisAsTable.resources != null){
        //console.log("============")
        //console.log(urisAsTable.resources.map(resources => [resources.name].concat(transformETPuriAsTable(resources.uri))
        //    .concat([(new Date(resources.storeCreated*1000)).toGMTString(), (new Date(resources.storeLastWrite*1000)).toGMTString()])))
        tableETP = createTableFromData(
                        urisAsTable.resources.map(resources => [resources.name].concat(transformETPuriAsTable(resources.uri))
                            .concat([(new Date(resources.storeCreated*1000)).toGMTString(), (new Date(resources.storeLastWrite*1000)).toGMTString()])), 
                        ["title", "pkg", "type", "uuid", "uri", "storeCreated", "storeLastWrite"], 
                        ["Title", "Package", "Type", "UUID", "Uri", "Store Created", "Store Last Write"], 
                        null, null, null);
        const cst_etpTable = tableETP;
        transformTabToFormCheckable(tableETP, urisAsTable.resources.map(elt => elt.uri), "etp_uri", 
            (isChecked, uri) => {
                etp_triggerViewUpdateOnResourceChecked(cst_etpTable, isChecked, uri);
            });
    }else if(urisAsTable!=null && urisAsTable.length > 0){
        tableETP = createTableFromData(
                        urisAsTable.map(uri => transformETPuriAsTable(uri)), 
                        ["pkg", "type", "uuid", "uri"], 
                        ["Package", "Type", "UUID", "Uri"], 
                        null, null, null);
        const cst_etpTable = tableETP;
        transformTabToFormCheckable(tableETP, urisAsTable.map(elt => elt), "etp_uri", 
            (isChecked, uri) => {
                etp_triggerViewUpdateOnResourceChecked(cst_etpTable, isChecked, uri);
            });
    }

    return tableETP;
}

export function transformETPuriAsTable(uri){
    var groups = uri.match(REGEX_ETP_URI).groups;

    try { 
        return [groups.domain + groups.domainVersion, groups.objectType, groups.uuid, uri]
    } catch(e) { console.log(e); }

    
    return ["--", "--", "--", "--"]
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

        // on doit convertir en liste pour creer le tableau
        const table = createExportToETPView(tableContent, checkboxesName, 
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
            }
        );

        
        elt_obj_list.appendChild(table);
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