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

import {refreshHighlightedOpenedObjects} from "../ui.js"
import {call_after_DOM_updated} from "../htmlUtils.js"
import {closeAllResqmlObjectContent} from "../tabulation.js"
import {sendPostFormAndReload} from "../eventHandler.js"
import {sendPostForm_Promise} from "../../requests/requests.js"


export function createModal(idModal, title, content, onCloseFunction){
    var existingModal = document.getElementById(idModal);
    if(existingModal!=null){
        existingModal.remove();
    }

    const modalElt = document.createElement("div");
    modalElt.className = "modal fade";
    modalElt.id = idModal;

    var modalCenter = document.createElement("div");
    modalCenter.className = "modal-dialog modal-dialog-centered modal-xl";
    modalElt.appendChild(modalCenter);

    var modalCenteredContent = document.createElement("div");
    modalCenteredContent.className = "modal-content";
    modalCenter.appendChild(modalCenteredContent);

    // HEADER 
    var modalHeader = document.createElement("div");
    modalHeader.className = "modal-header";
    modalCenteredContent.appendChild(modalHeader);

    var headerTitle = document.createElement("h4");
    headerTitle.className = "modal-title";
    headerTitle.appendChild(document.createTextNode(title));
    modalHeader.appendChild(headerTitle);

    var closeBut = document.createElement("button");
    closeBut.name = "close_" + idModal;
    closeBut.className = "close";
    closeBut.setAttribute("data-bs-dismiss", "modal");
    closeBut.appendChild(document.createTextNode("x"));

    const const_idModal = idModal;
    closeBut.onclick = function(){
        if(onCloseFunction!=null){
            onCloseFunction();
            //console.log("call close")
        }
        closeModal(const_idModal);
        try{
            modalElt.remove()
        }catch(e){}
    }
    modalHeader.appendChild(closeBut);
    ///

    // CONTENT
    var modalContent = document.createElement("div");
    modalContent.className = "tab-content";
    modalContent.appendChild(content);

    modalCenteredContent.appendChild(modalContent);
    ///

    return modalElt;
}

export function openModal(idModal, title, content, onCloseFunction){
    var containerId = "modalContainer_"+idModal;
    if(document.getElementById(containerId)!=null){
        document.getElementById(containerId).remove();
    }

    var modalElt = createModal(idModal, title, content, onCloseFunction);
    var linkOpener = document.createElement("a");
    linkOpener.hidden = "hidden";
    linkOpener.setAttribute("data-bs-toggle", "modal");
    linkOpener.setAttribute("data-bs-target", "#" + idModal);

    var divContainer = document.createElement("div");
    divContainer.id = containerId;
    divContainer.appendChild(linkOpener);
    divContainer.appendChild(modalElt);

    document.body.appendChild(divContainer);
    linkOpener.click();
    refreshHighlightedOpenedObjects();
    return modalElt;
}

export function openExistingModal(modalElt, idModal){
    var containerId = "modalContainer_"+idModal;
    if(document.getElementById(containerId)!=null){
        document.getElementById(containerId).remove();
    }

    var linkOpener = document.createElement("a");
    linkOpener.hidden = "hidden";
    linkOpener.setAttribute("data-bs-toggle", "modal");
    linkOpener.setAttribute("data-bs-target", "#" + idModal);

    var divContainer = document.createElement("div");
    divContainer.id = containerId;
    divContainer.appendChild(linkOpener);
    divContainer.appendChild(modalElt);

    document.body.appendChild(divContainer);
    linkOpener.click();
    refreshHighlightedOpenedObjects();
}

export function closeModal(idModal){
    var containerId = "modalContainer_"+idModal;
    if(document.getElementById(containerId)!=null){ 
        var modalElt = document.getElementById(containerId);
        //        console.log(modalElt);
        var closeFound = document.getElementsByName("close_" + idModal);
        for(var closeIdx=0; closeIdx<closeFound.length; closeIdx++){
            try{
                closeFound[closeIdx].click();
            }catch(e){console.log(e);}
        }
        try{
            modalElt.remove();
        }catch(e){ }
        //        call_after_DOM_updated(modalElt.remove());
    }
    refreshHighlightedOpenedObjects();
}

export function sendForm(formToSend, modalEntityId, rollingId, closeAllTabs, showResult, closeModal, reloadWorkspace){
    var form = formToSend;
    if(typeof(form) === "string"){
        // Si c'est un identifiant on cherche l'element sinon c'est qu'on a deja l'objet form
        form = document.getElementById(form);
    }
    var modalDiv = null;

    try{
        modalDiv = document.getElementById(modalEntityId);
    }catch(Except){}

    //console.log($("[type=submit]").add($("[name=submit]")));

    const submitters = $("[type=submit]").add($("[name=submit]"));

    submitters.each(function(index){$(this).disable=true;});

    const roller = document.getElementById(rollingId);
    
    try{
        roller.style.display='';
    }catch(Except){}

    if(closeAllTabs){
        closeAllResqmlObjectContent();
    }

    if(reloadWorkspace != null && reloadWorkspace){
        return sendPostFormAndReload(form, form.action, showResult).then(
            function(){
                roller.style.display='none';

                submitters.each(function(index){$(this).disable=false;});

                if(modalDiv!=null && (closeModal==null || closeModal == true) ){
                    try{
                        // On cherche le bouton de fermeture du modal
                        var listModalCloseBut = modalDiv.getElementsByTagName("button");
                        for(var eltClose=0; eltClose<listModalCloseBut.length; eltClose++ ){
                            if(listModalCloseBut[eltClose].className=="close"){
                                try{
                                    listModalCloseBut[eltClose].click();
                                }catch(e){console.log(e);}
                            }
                        } 
                    }catch(Except){console.log(Except);}
                }
            });
    }else{
        return sendPostForm_Promise(form, form.action, showResult).then(
            function(){
                roller.style.display='none';

                submitters.each(function(index){$(this).disable=false;});

                if(modalDiv!=null && (closeModal==null || closeModal == true) ){
                    try{
                        // On cherche le bouton de fermeture du modal
                        var listModalCloseBut = modalDiv.getElementsByTagName("button");
                        for(var eltClose=0; eltClose<listModalCloseBut.length; eltClose++ ){
                            if(listModalCloseBut[eltClose].className=="close"){
                                try{
                                    listModalCloseBut[eltClose].click();
                                }catch(e){console.log(e);}
                            }
                        } 
                    }catch(Except){console.log(Except);}
                }
            });
    }
}

export function modal_waitDomUpdate(testFunc) {
    if (testFunc!=null) {
        window.requestAnimationFrame(testFunc);
    }else {
        //$("#element").do_some_stuff();
    }
};


export function getfolder(e) {
    var files = e.target.files;
    var path = files[0].fullPath;
    var Folder = path; //path.split("/");
    /* alert(">>" + Folder[0]); */
    for(var fi=0; fi<files.length; fi++) {
        console.log("getFolder :--> ");
        console.log(files[fi]); 
    }
}

export function modal_getParentContainer(elt){
    var current = elt;
    while(current.className==null 
        || (!current.className.includes("container")
             && !current.className.includes("modal-dialog")
             && !current.className.includes("tab-pane")
             )
        ){
        current = current.parentNode;
    }
    return current;
}