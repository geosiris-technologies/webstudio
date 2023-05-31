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

import {genObjectContentDivElementId} from "../energyml/epcContentManager.js";
import {getResqmlEltTabText, getResqmlEltTitleText} from "../energyml/ResqmlElement.js";
import {highlightTableCellFromClass} from "./table.js";
import {refreshHighlightedOpenedObjects} from "./ui.js";
import {getObjectTableCellClass} from "../common/variables.js";


export function getActiveOpenedObject(idTabHeader){
    var tabHeader = document.getElementById(idTabHeader);
    var navs = tabHeader.getElementsByClassName("nav-link");
    for (var i = navs.length - 1; i >= 0; i--) {
        if(navs[i].className.includes(" active")){
          return nav[i].parent;
        }
    }
    return null;
}

export function closeOpenedObject(navItem){
    if(navItem != null){
        try{
            navItem.getElementsByClassName("tabulationCloser").click();
        }catch(e){console.log(e);}
    }
}

export function closeAllResqmlObjectContent(){
    //console.log("closing all");console.log(document.getElementsByClassName("tabulationCloser"));
    var tabsClosers = document.getElementsByClassName("tabulationCloser");
    for (var i = tabsClosers.length - 1; i >= 0; i--) {
        tabsClosers[i].click();
    }
}

export function saveAllResqmlObject_promise(){
    var promessesSave = [];
    var tabsSaver = document.getElementsByClassName("tabulationSave");
    for (var i = tabsSaver.length - 1; i >= 0; i--) {
        promessesSave.push(new Promise(    function (resolve, reject) {
                                        resolve(tabsSaver[i].click());
                                    })
                    );
    }
    return promessesSave;
}

export function saveResqmlObject_promise(uuid, idTabHeader){
    var promessesSave = [];
    var objContentElt = document.getElementById(genObjectContentDivElementId(uuid));
    if(objContentElt != null){
        var butObjSave = objContentElt.getElementsByClassName("tabulationSave");
        for (var i = butObjSave.length - 1; i >= 0; i--) {
            promessesSave.push(new Promise(    function (resolve, reject) {
                                            // console.log("click " + i);
                                            resolve(butObjSave[i].click());
                                        })
                        );
        }
    }else{
        console.log("Failed to find tab '" + uuid + "'")
    }
    return promessesSave;
}

export function createTabulation(uuid, content, idTabHeader, idTabContainer, resqmlElt){
    // HEADER
    var tabulationHeader = document.getElementById(idTabHeader);

    var li = document.createElement("li");
    li.className     = "nav-item geosirisOpenedObject";
    li.resqmlElt = resqmlElt;

    var linkToClick = document.createElement("a");

    // On met une fonction de rafraichissement pour pouvoir refresh les element apres modification
    li.refresh = function(){
        resqmlElt.refresh().then(
            function(){
                while (linkToClick.firstChild) {
                    linkToClick.firstChild.remove();
                }
                var newText = getResqmlEltTabText(resqmlElt);

                //console.log("refreshing " + newText)
                linkToClick.appendChild(document.createTextNode(newText));
            });
    }


    const spanCloseTabulation = document.createElement("span");

    var h_txt = getResqmlEltTabText(resqmlElt);
    linkToClick.appendChild(document.createTextNode(h_txt));
    linkToClick.href = "#tabulation_id_" + uuid; 
    linkToClick.className = "nav-link";
    linkToClick.setAttribute("data-toggle", "tab");
    linkToClick.title = getResqmlEltTitleText(resqmlElt);
/*
    linkToClick.onclick = function(){
        if(linkToClick.className.includes('active') && !spanCloseTabulation.className.includes('active')){
            spanCloseTabulation.className += ' active';
        }else{
            spanCloseTabulation.className = spanCloseTabulation.className.replace('active', '');
        }
    }*/


    li.appendChild(linkToClick);
    const c_uuid = uuid;
    const c_idTabHeader = idTabHeader;

    spanCloseTabulation.className = "tabulationCloser fas fa-times-circle";
    spanCloseTabulation.onclick = function(){
        closeTabulation(c_uuid, c_idTabHeader);

    };
    spanCloseTabulation.addEventListener("mouseover", function() {
        spanCloseTabulation.className = "tabulationCloser far fa-times-circle";
    });

    spanCloseTabulation.addEventListener("mouseout", function() {
        spanCloseTabulation.className = "tabulationCloser fas fa-times-circle";
    })

    li.appendChild(spanCloseTabulation);

    tabulationHeader.appendChild(li);

    // CONTENT
    var tabulationContainer = document.getElementById(idTabContainer);

    content.id = genObjectContentDivElementId(uuid);
    content.style.height = "100%";
    content.className += " container tab-pane fade";

    tabulationContainer.appendChild(content);
}

/**
 * 
 * 
 *    Mettre la class " table-success " sur la ligne du tableau qui correspond à la tabulation ouverte
 *
 *
 *
 */

export function getTabulation(uuid, idTabHeader){
    var headerTabs = document.getElementById(idTabHeader).children;

    // On cherche si un des enfant a comme lien l'element dont l'identifiant est '#[uuid]'
    for(var child in headerTabs){
        try{
            // on cherche dans le fils <li> le lien <a> donc le href est '#[uuid]'
            if(headerTabs[child].firstChild.href.includes("#tabulation_id_"+uuid)){
                return headerTabs[child];
            }
        }catch(except){/*console.log(except);*/}
    }
    return null;
}

export function getOpenObjectsUuid_GivingTabHeader(idTabHeader){
    var headerTabs = document.getElementById(idTabHeader).children;
    //console.log(headerTabs)
    var uuidList = [];
    for(var child in headerTabs){
        try{
            var hrefLink = headerTabs[child].firstChild.href;
            uuidList.push(hrefLink.substring(hrefLink.indexOf("#tabulation_id_") + "#tabulation_id_".length));
        }catch(except){/*console.log(except);*/}
    }
    return uuidList;
}

export function closeTabulation(uuid, idTabHeader){//}, idTabcontent){
    highlightTableCellFromClass(getObjectTableCellClass(uuid), false);
    //refreshHighlightedOpenedObjects();
    var foundTab = getTabulation(uuid, idTabHeader);
    if(foundTab != null){
        if(foundTab.previousSibling != null){
            foundTab.previousSibling.firstChild.click();
        }else if(foundTab.nextSibling != null){
            foundTab.nextSibling.firstChild.click();
        }
        foundTab.remove();
        document.getElementById("tabulation_id_"+uuid).remove();
        
        refreshHighlightedOpenedObjects();
        return true;
    }
    return false;
}

export function openTabulation(uuid, idTabHeader){
    var foundTab = getTabulation(uuid, idTabHeader);
    if(foundTab != null){
        foundTab.firstChild.click();
        //refreshHighlightedOpenedObjects();

        refreshHighlightedOpenedObjects();
        //highlightTableCellFromClass(getObjectTableCellClass(uuid), true);
        return true;
    }
    return false;
}
