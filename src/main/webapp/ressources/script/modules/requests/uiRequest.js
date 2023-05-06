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

import {createTabulation, openTabulation, saveAllResqmlObject_promise} from "../UI/tabulation.js"
import {beginTask, endTask, refreshHighlightedOpenedObjects} from "../UI/ui.js"
import {sendGetURLAndReload, sendPostFormAndReload} from "../UI/eventHandler.js"
import {downloadGetURL_Promise, sendGetURL, sendGetURL_Promise, getJsonObjectFromServer} from "./requests.js"
import {genObjectContentDivElementId, genObjectContentElementId, genObjectPropertyElementId, resquestCorrection, resquestObjectCopy, resquestValidation} from "../energyml/epcContentManager.js"
import {ResqmlElement} from "../energyml/ResqmlElement.js"
import {appendConsoleMessage} from "../logs/console.js"
import {call_after_DOM_updated, createSplitter} from "../UI/htmlUtils.js"
import {sendUserFormWithPasswordValidation} from "../common/passwordValidator.js"
import {createTableFromData, transformTabToFormCheckable} from "../UI/table.js"
import {closeResqmlObjectContentByUUID, openResqmlObjectContentByUUID} from "../main.js"
import {closeModal, openExistingModal, openModal} from "../UI/modals/modalEntityManager.js"


export function saveAllResqmlObjectContent(){
    //console.log("closing all");console.log(document.getElementsByClassName("tabulationCloser"));
    var promessesSave = saveAllResqmlObject_promise();
    beginTask();

    Promise.all(promessesSave).then(function (data) {
        endTask();
        /*$("#modal_exportEPCAndClose").modal();
        console.log("all promises");*/
        //sendGetURLAndReload('FileReciever?close=true', false);
    }).catch(() => endTask());
}

export function exportAndClose(fileName){
    console.log("exporting and closing file  " + fileName);
    beginTask();
    return downloadGetURL_Promise("ExportEPCFile?epcFilePath="+fileName, fileName).then(
        function (data) {
        console.log("closing file");
        endTask();
        sendGetURLAndReload('FileReciever?close=true', false);
    }).catch(() => endTask());;

}

//Ouvre un onglet dans le panneau de contenur d'objet (le crée si non déjà existant)
export function openResqmlObjectContent(    uuid,  
        idTabHeader, idTabContainer, idConsoleElt,
        classObjectContent, classObjectProperty){
    //console.log('openTab ' + uuid);
    if(beginTask(true)){
        if(!openTabulation(uuid, idTabHeader)){ // Si la tabulation existe on n'en re-crée pas une
    //        console.log("create tab");
            var xmlHttp = new XMLHttpRequest();

            xmlHttp.open( "GET", "ResqmlObjectTree?uuid="+uuid, true ); // false for synchronous request

            xmlHttp.onload = function(){

                var objectProperty = document.createElement("div");
                objectProperty.id  = genObjectPropertyElementId(uuid);
                objectProperty.className = classObjectProperty;
                try{
                    const parsedJSON = JSON.parse(xmlHttp.responseText);
                    
                    //console.log(xmlHttp.responseText);

                    //var content = createCollapsableTreeFromJSON_NEW(parsedJSON, objectProperty, uuid);

                    // ----------
                    var resqmlElt = new ResqmlElement(parsedJSON, uuid, objectProperty);
                    var content = resqmlElt.createView();
                    // ----------

                    content.id = genObjectContentElementId(uuid);
                    content.className += " " + classObjectContent;

                    var divObjectContent = document.createElement("div");
                    divObjectContent.style.height = "100%";
                    divObjectContent.style.width  = "100%";
                    divObjectContent.style.padding= "0px";
                    divObjectContent.id= genObjectContentDivElementId();

                    var butExpandObject = document.createElement("span");
                    butExpandObject.title = "Expand tree";
                    butExpandObject.className += " treeExpander fas fa-angle-double-down";
                    butExpandObject.id = "but_Expand_" + uuid;
                    divObjectContent.appendChild(butExpandObject);

                    butExpandObject.onclick = function(){
                        content.querySelectorAll('.resqmlCollapse').forEach( 
                                function(element, index) {
                                    if(!element.className.includes("-down")){    // Si pas deja ouvert
                                        element.click();
                                    }
                                });
                    };

                    var butCollapseObject = document.createElement("span");
                    butCollapseObject.title = "Collapse tree";
                    butCollapseObject.className += " treeExpander fas fa-angle-double-up";
                    butCollapseObject.id = "but_Collapse_" + uuid;
                    divObjectContent.appendChild(butCollapseObject);

                    butCollapseObject.onclick = function(){
                        content.querySelectorAll('.resqmlCollapse').forEach( 
                                function(element, index) {
                                    if(element.className.includes("-down")){    // Si deja ouvert
                                        element.click();
                                    }
                                });
                    };

                    var divBtnGrp = document.createElement("div");
                    divBtnGrp.className = "btn-group";
                    divObjectContent.appendChild(divBtnGrp);

                    var butValidateObject = document.createElement("button");
                    butValidateObject.appendChild(document.createTextNode("Validate"));
                    butValidateObject.className += " btn btn-outline-dark objButtonAction";
                    butValidateObject.id = "but_Validate_" + uuid;
                    divBtnGrp.appendChild(butValidateObject);

                    butValidateObject.onclick = function(){
                        resquestValidation(idConsoleElt, uuid);
                    };

                    var butRefresh = document.createElement("button");
                    butRefresh.appendChild(document.createTextNode("Refresh"));
                    butRefresh.className += " btn btn-outline-dark objButtonAction";
                    butRefresh.id = "but_AutoCorrect_" + uuid;
                    divBtnGrp.appendChild(butRefresh);

                    butRefresh.onclick = function(){
                        resqmlElt.refresh();
                    };

                    /*var butPrintOSDU_Json = document.createElement("button");
                    var butPrintOSDU_Json = document.createElement("button");
                    butPrintOSDU_Json.appendChild(document.createTextNode("OSDU WKE"));
                    butPrintOSDU_Json.title = "OSDU Well Known Entities";
                    butPrintOSDU_Json.className += " btn btn-outline-success objButtonAction";
                    butPrintOSDU_Json.id = "but_PrintOSDU_Json_" + uuid;
                    divBtnGrp.appendChild(butPrintOSDU_Json);

                    butPrintOSDU_Json.onclick = function(){
                        window.open("/GetObjectAsJson?uuid=" + uuid, '_blank').focus()
                    };*/

                    var butPrint_Json = document.createElement("button");
                    butPrint_Json.appendChild(document.createTextNode("Json"));
                    butPrint_Json.title = "Google Gson translation";
                    butPrint_Json.className += " btn btn-outline-success objButtonAction";
                    butPrint_Json.id = "but_Print_Json_" + uuid;
                    divBtnGrp.appendChild(butPrint_Json);

                    butPrint_Json.onclick = function(){
                        window.open("/GetObjectAsJson?uuid=" + uuid, '_blank').focus()
                    };

                    var butPrintXml = document.createElement("button");
                    butPrintXml.appendChild(document.createTextNode("Xml"));
                    butPrintXml.className += " btn btn-outline-success objButtonAction";
                    butPrintXml.id = "but_Print_xml_" + uuid;
                    divBtnGrp.appendChild(butPrintXml);

                    butPrintXml.onclick = function(){
                        window.open("/GetObjectAsXml?uuid=" + uuid, '_blank').focus()
                    };

                    var butAutoCorrect = document.createElement("button");
                    butAutoCorrect.appendChild(document.createTextNode("Auto-correct"));
                    butAutoCorrect.className += " btn btn-outline-info objButtonAction";
                    butAutoCorrect.id = "but_AutoCorrect_" + uuid;
                    divBtnGrp.appendChild(butAutoCorrect);

                    butAutoCorrect.onclick = function(){
                        resquestCorrection(idConsoleElt, uuid, "dor").then(function(){resqmlElt.refresh();});
                    };

                    // Root uuid parameter
                    var formValidator = document.createElement("input");
                    formValidator.type = "submit";
                    formValidator.value = "Save";
                    formValidator.className += "tabulationSave btn btn-outline-danger objButtonAction";
    //                formObjectModification.appendChild(formValidator); // on le met avant le form pour bypass la valdiation de form
                    divBtnGrp.appendChild(formValidator);


                    /// AJOUTER * madatory element
                    var mandatoryLabel = document.createElement("span");
                    mandatoryLabel.appendChild(document.createTextNode("mandatory"));
                    mandatoryLabel.className += "mandatoryElt mandatoryLabel";
                    divObjectContent.appendChild(mandatoryLabel);

                    const formObjectModification = document.createElement("form");
                    formObjectModification.name = "formModif";
                    formObjectModification.acceptCharset = "UTF-8";
                    formObjectModification.style.height = "100%";
                    formObjectModification.style.width  = "100%";
                    formObjectModification.style.padding= "0px";
                    formObjectModification.method = "post";
                    formObjectModification.action = "ObjectEdit";
                    formObjectModification.acceptCharset = "UTF-8";

                    // Root uuid parameter
                    var rootUUIDinput = document.createElement("input");
                    rootUUIDinput.name = "Root_UUID";
                    rootUUIDinput.value = uuid;
                    rootUUIDinput.hidden = "hidden";
                    formObjectModification.appendChild(rootUUIDinput);

                    

                    // ajout des inputs de l'objet au formulaire
                    formObjectModification.appendChild(content);
                    formObjectModification.appendChild(objectProperty);


                    formValidator.onclick = function(){
                        sendPostFormAndReload(formObjectModification, "ObjectEdit", false, 
                                                function(){
                                                    resqmlElt.refresh();
                                                });
                    };

                    divObjectContent.appendChild(formObjectModification);

                    

                    //console.log("#openResqmlObjectContent create tabulation")
                    createTabulation(uuid, divObjectContent, idTabHeader, idTabContainer, resqmlElt);

                    //console.log("#openResqmlObjectContent open tabulation")
                    openTabulation(uuid, idTabHeader); // on l'ouvre
                    
                    call_after_DOM_updated(function () {
                        // On crée le split a la fin car il faut que les elements soient déjà placé pour que ça fonctionne
                        createSplitter("#"+content.id, "#"+objectProperty.id, 65, 30, "vertical", 55, 15);
                    });

                    // console.log("#openResqmlObjectContent call refreshHighlightedOpenedObjects")
                    // refreshHighlightedOpenedObjects(); allready done in openTab
                    // console.log("#openResqmlObjectContent END")
                    endTask();

                }catch(exceptionJSON){
                    console.log("EXCEPT : ");console.log(exceptionJSON);
                    console.log(xmlHttp.responseText);
                    endTask();
                }
            };

            xmlHttp.send( null );
        }else{
            endTask();
        }
    }else{
        console.log("a task is allready running");
    }
}


export function getAllOpendedObjects(){
    return document.getElementsByClassName("geosirisOpenedObject");
}

export function sendCreateUser(){
    var form = document.getElementById("form_CreateUser");
    var pwds = document.getElementsByName("password");
    var wrongPasswordLog = document.getElementById("err_pwd");

    sendUserFormWithPasswordValidation(form, pwds, wrongPasswordLog);
}

export function createUserTableView(checkboxesName, f_OnCheckToggle, jsonContent){
    console.log(jsonContent["user"]);
    var tableDOR = createTableFromData(jsonContent, ["login", "mail", "usr_grp"], ["User name", "User", "Group"], null, null, null);

    transformTabToFormCheckable(tableDOR, jsonContent.map(elt => elt["login"]), checkboxesName, f_OnCheckToggle);
    tableDOR.className += " deleteUserTable";
    return tableDOR;
}


export function sendSignUp(){
    var form = document.getElementById("form_signUp");
    var pwds = document.getElementsByName("password");
    var wrongPasswordLog = document.getElementById("err_pwd");

    sendUserFormWithPasswordValidation(form, pwds, wrongPasswordLog);
}

export function sendUserSettings(form, passwordList, eltErr_pwd, eltErr_Missmatch){
    var form = document.getElementById("form_UserSettings");
    var pwds = document.getElementsByName("newPassword");
    var wrongPasswordLog = document.getElementById("err_pwd");

    sendUserFormWithPasswordValidation(form, pwds, wrongPasswordLog);
}

export function deleteResqmlObject(uuid, type, title){
    if(beginTask(true)){
        const formDelete = document.createElement("form");
        formDelete.action = "ObjectEdit";
        formDelete.method = "post";

        const inputCommand = document.createElement("input");
        inputCommand.name = "command"; 
        inputCommand.value = "delete";
        formDelete.appendChild(inputCommand);

        const inputUUID = document.createElement("input");
        inputUUID.name = "Root_UUID";
        inputUUID.value = uuid;
        formDelete.appendChild(inputUUID);

        getJsonObjectFromServer("ResqmlLinkedObjects?uuid=" + uuid).then( 
                function(objectList){
                    console.log(`ResqmlLinkedObjects ${objectList}`);
                    var resultAttributeToSearch = ["num", "type", "title", "uuid"];
                    var dataTableHeader = ["Num", "Type", "Title", "Uuid"];

                    // Si il y a des objets reliés on affiche la liste pour prévenir l'utilisateur
                    if(objectList != null && objectList.length>0){
                        var objectMatrix = [];
                        for(var objIdx=0; objIdx < objectList.length; objIdx++){
                            var obj = objectList[objIdx];
                            try{
                                var objLine = {};
                                for(var itemIdx=0; itemIdx < resultAttributeToSearch.length; itemIdx++){
                                    objLine[resultAttributeToSearch[itemIdx]] = obj[resultAttributeToSearch[itemIdx]];
                                }
                                objectMatrix.push(objLine);
                            }catch(exceptObject){
                                console.log(exceptObject);
                            }
                        }
                        var currentObjectStringRepresentation = type + " " + uuid + " " + title;
                        var divContentDeletion = document.createElement("div");

                        // table
                        var relatedEltTableDeletion = createTableFromData(objectMatrix, resultAttributeToSearch, dataTableHeader, 
                                                                        objectMatrix.map(contentElt => [ function(){openResqmlObjectContentByUUID(contentElt['uuid'])} ]), 
                                                                        null );
                        divContentDeletion.appendChild(relatedEltTableDeletion);

                        // Bottom
                        var bottomModalDeletion = document.createElement("div");
                        bottomModalDeletion.style.textAlign = "center";
                        divContentDeletion.appendChild(bottomModalDeletion);

                        // Warning Confirmation text 
                        var warningconfirmTextDeletion = document.createElement("p");
                        warningconfirmTextDeletion.className = "warningMSG";
                        //warningconfirmTextDeletion.appendChild(document.createTextNode("Do you confirm the object '" + uuid + "' removing ?"));
                        warningconfirmTextDeletion.appendChild(document.createTextNode("! WARNING ! TO AVOID UNCONSISTENCY IN THIS EPC !"));
                        bottomModalDeletion.appendChild(warningconfirmTextDeletion);

                        var infoConfirmTextDeletion = document.createElement("p");
                        infoConfirmTextDeletion.className = "infoMSG";
                        infoConfirmTextDeletion.appendChild(document.createTextNode("If you want to eliminate this object form this EPC, it would be necessary "
                                                                                    + "to eliminate before the above refereced objects and their dependencies"));
                        bottomModalDeletion.appendChild(infoConfirmTextDeletion);


                        var msgConfirmTextDeletion = document.createElement("p");
                        msgConfirmTextDeletion.appendChild(document.createTextNode("Do you want to eliminate : " + currentObjectStringRepresentation));
                        bottomModalDeletion.appendChild(msgConfirmTextDeletion);

                        // button ok
                        var butOkDeletion = document.createElement("button");
                        butOkDeletion.className = "btn btn-secondary";
                        butOkDeletion.appendChild(document.createTextNode("OK"));
                        bottomModalDeletion.appendChild(butOkDeletion);

                        // button Cancel
                        var butCancelDeletion = document.createElement("button");
                        butCancelDeletion.className = "btn btn-secondary";
                        butCancelDeletion.appendChild(document.createTextNode("CANCEL"));
                        bottomModalDeletion.appendChild(butCancelDeletion);

                        const modalIdDeletion = "modal_deletion";
                        //const modalDeletion = openModal(modalIdDeletion, "Elements that references '" + uuid + "'", divContentDeletion);
                        const modalDeletion = openModal(modalIdDeletion, currentObjectStringRepresentation + " is referenced by", divContentDeletion, 
                                                            function(){
                                                                endTask();
                                                            });
                        butCancelDeletion.onclick = function(){
                                                    closeModal(modalIdDeletion);
                                                    endTask();
                                                };
                        butOkDeletion.onclick = function(){
                                                    sendPostFormAndReload(formDelete, "ObjectEdit", false);
                                                    closeResqmlObjectContentByUUID(uuid);
                                                    closeModal(modalIdDeletion);
                                                    endTask();
                                                };
                        openExistingModal(modalDeletion, modalIdDeletion);

                    }else{    // Si pas d'objets reliés, on supprime directement
                        //console.log("null or empty : ");
                        //console.log(objectList);
                        sendPostFormAndReload(formDelete, "ObjectEdit", false);
                        closeResqmlObjectContentByUUID(uuid);
                        endTask();
                    }

                }
        ).catch(() => endTask());
    }else{
        console.log('Delete while task allready running')
    }
}
