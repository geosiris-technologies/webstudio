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

import {downloadGetURL_Promise, sendDeleteURL_Promise, getJsonObjectFromServer} from "../../requests/requests.js"
import {addJsonData} from "../../energyml/JsonElementVue.js"
import {randomColor, createDeleteButton} from "../htmlUtils.js"
import {beginTask, endTask} from "../ui.js"


export function refreshPropertyDictVue(){
    beginTask();
    return updateJsonDictUI("/GetAdditionalObjects", 'container_PropertiesDict', 
                    'modal_PropertiesDict_progressBar', "counter_PropertiesDict", 
                    true,
                    null).then(x => endTask()).catch((e) =>{console.log(e); endTask()});
}

export function refreshWorkspaceDictVue(){
    beginTask();
    return updateJsonDictUI("/WorkspaceOverview", 'container_WorkspaceDict', 
                    'modal_WorkspaceDict_progressBar', "counter_WorkspaceDict", 
                    false,
                    function (objKey, spanElt){
                        try{
                            if(objKey.toLowerCase() == "other objects"){
                                [...spanElt.nextElementSibling.childNodes].forEach(child => {
                                    const par = document.createElement("p");
                                    const filePath = child.textContent;
                                    const fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
                                    const url = encodeURI('/WorkspaceAdditionalData?filePath='+filePath);

                                    par.style.color = randomColor();
                                    par.style.fontWeight = 'bold';
                                    par.style.cursor = "pointer";
                                    
                                    if(filePath.toLowerCase().endsWith(".pdf")){
                                        par.title = "open in a new tab";
                                        par.onclick = function(){
                                            window.open(url);
                                        }
                                    }else{
                                        par.title = "download";
                                        par.onclick = function(){
                                            downloadGetURL_Promise(url, fileName);
                                        }
                                    }

                                    var deleteBut = createDeleteButton("", "Delete");
                                    deleteBut.onclick = function(){
                                        sendDeleteURL_Promise(url).then(resp => {
                                            refreshWorkspaceDictVue().catch((error) => console.error(error));
                                        });
                                    }
                                    deleteBut.style.marginLeft = "10px";
                                    deleteBut.style.marginBottom = "0px";

                                    par.appendChild(child.firstChild);
                                    child.appendChild(par);
                                    child.appendChild(deleteBut);

                                    child.style.display = "flex";
                                });
                            }
                        }catch(e){}

                    }).then(x => endTask()).catch(() => endTask());
}


export function updateJsonDictUI(uri, containerId, progressBarId, counterId, printListIdx, f_applyOnKey){
    document.getElementById(progressBarId).style.display = "";
    return getJsonObjectFromServer(uri).then(
        objectList => {
            var container = document.getElementById(containerId);
            while(container.firstChild){
                container.removeChild(container.firstChild);
            }
            addJsonData(objectList, container, printListIdx, f_applyOnKey);
            document.getElementById(progressBarId).style.display = "none";
            updateCountViewableJsonDictUI(containerId, counterId);
    }).catch((e) => {console.log(e); document.getElementById(progressBarId).style.display = "none";});
}

export function filterJsonDictUI(containerId, counterId, filter, caseSensitive, splitPhraseInWords){
    if(filter == null)
        filter = "";

    var filterList = [filter];
    if(splitPhraseInWords){
        filterList = filter.trim().split(' ');
    }
    
    var containerDiv = document.getElementById(containerId).children;
    if(filterList.length <= 0){
        for(var i=0; i < containerDiv.length; i++){
            var child = containerDiv[i];
            child.style.display = "";
        }
    }else{
        for(var i=0; i < containerDiv.length; i++){
            var child = containerDiv[i];
            var found = true;
            var txtContent = child.textContent;
            if(!caseSensitive){
                txtContent = txtContent.toLowerCase()
            }

            for(var fi in filterList){
                if(!txtContent.includes(caseSensitive?filterList[fi]:filterList[fi].toLowerCase())){
                    found = false;
                    break;
                }
            }
            if(found)
                child.style.display = "";
            else
                child.style.display = "none";
        }
    }
    updateCountViewableJsonDictUI(containerId, counterId);
}

function updateCountViewableJsonDictUI(containerId, counterId){
    var counterElt = document.getElementById(counterId);
    var containerDiv = document.getElementById(containerId).children;
    var count = 0;
    for(var i=0; i < containerDiv.length; i++){
        if(containerDiv[i].style.display == null || containerDiv[i].style.display.toLowerCase() != "none"){
            count++;
        }
    }
    while (counterElt.firstChild) {
        counterElt.firstChild.remove();
    }
    counterElt.appendChild(document.createTextNode(count + ""));
}