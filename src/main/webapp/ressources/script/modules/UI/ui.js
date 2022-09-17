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

import {call_after_DOM_updated, createDeleteResqmlButton, createDropDownDivider, createRadio, createSplitter} from "./htmlUtils.js"
import {hasBeenDisconnected, refreshWorkspace, rws_addEventListeners} from "./eventHandler.js"
import {appendConsoleMessage} from "../logs/console.js"
import {getAllOpendedObjects} from "../requests/uiRequest.js"
import {compare, copyOtherTableSortingParams, createTableFromData, highlightTableCellFromClass, highlightTableLineFromTdText, transformTab_AddColumn} from "./table.js"
import {SEM_IS_LOADING_WORKSPACE, getObjectTableCellClass, setSEM_IS_LOADING_WORKSPACE} from "../common/variables.js"
import {openResqmlObjectContentByUUID} from "../main.js"
import {energymlRootTypes, savePreferences} from "../energyml/epcContentManager.js"


export var __USER_NAME__ = "";

var NB_TASK = 0;

export function beginTask(exclusiv){
    if(NB_TASK == 0 || exclusiv==null || exclusiv==false){
        try{
            NB_TASK++;
            document.getElementById("taskInProgressRoling").style.display="";
        }catch(e){}

        // Cursor wait
        return true;
    }
    document.body.style.cursor = 'progress';
    return false;
}

export function endTask(){
    try{
        NB_TASK--;
        if(NB_TASK<=0){
            NB_TASK = 0;
            document.getElementById("taskInProgressRoling").style.display="none";
            // Cursor default
            call_after_DOM_updated(
                function() {
                    document.body.style.cursor = 'default';
                }    
            );
        }
    }catch(e){}
    return NB_TASK;
}

export function enableWaitingUi(b_val){
    document.getElementById("page-loader").style.display = b_val ? '' : 'none';
    document.getElementById("main").style.display =  b_val ? 'none': '';
}

/* _____                _
  / ___/___  __________(_)___  ____
  \__ \/ _ \/ ___/ ___/ / __ \/ __ \
 ___/ /  __(__  |__  ) / /_/ / / / /
/____/\___/____/____/_/\____/_/ /_/
*/

export function setUserName(userName, usrGrp, spanSessionCounter_id){

    __USER_NAME__ = userName;
    
    var sessionInfoMenu = document.getElementById("sessionInfoMenu");
    var sessionInfoBut = document.getElementById("sessionInfoBut");
    sessionInfoBut.innerHTML = userName;

    // User name printer
    var spanUserName = document.createElement("span");
    spanUserName.innerHTML = "Hello " + userName;
    sessionInfoMenu.appendChild(spanUserName);

    // Divider
    sessionInfoMenu.appendChild(createDropDownDivider());

    if(usrGrp=='geosiris'){
        sessionInfoBut.className += sessionInfoBut.className.replace("fa-user", "fa-user-tie");
        sessionInfoBut.className += " connectedAdmin";

        // delete user
        var deleteUserBut = document.createElement("a");
        deleteUserBut.href = "deleteuser";
        deleteUserBut.className = "dropdown-item ";
        deleteUserBut.innerHTML = "Delete user";
        sessionInfoMenu.appendChild(deleteUserBut);

        // create user
        var createUserBut = document.createElement("a");
        createUserBut.href = "createuser";
        createUserBut.className = "dropdown-item ";
        createUserBut.innerHTML = "Create user";
        sessionInfoMenu.appendChild(createUserBut);
        
        // Session counter
        initSessionCounter(spanSessionCounter_id);

    }else{
        sessionInfoBut.className += " connectedUser";
        //console.log('usr grp "' + usrGrp+"'" );
    }

    // load workspace Button
    var loadWorkspaceBut = document.createElement("a");
    // loadWorkspaceBut.href = "loadworkspace";
    loadWorkspaceBut.className = "dropdown-item ";
    loadWorkspaceBut.innerHTML = "Load workspace";
    loadWorkspaceBut.onclick = function(){
        beginTask();
        call_after_DOM_updated(
            function(){
                refreshWorkspace();
            });
        endTask();
    }

    sessionInfoMenu.appendChild(loadWorkspaceBut);

    //user settings Button
    var userSettingsBut = document.createElement("a");
    userSettingsBut.href = "usersettings";
    userSettingsBut.className = "fas fa-cog ";
    userSettingsBut.innerHTML = "Settings";

    var userSettingsBut_container = document.createElement("span");
    userSettingsBut_container.className = "dropdown-item";
    userSettingsBut_container.appendChild(userSettingsBut);
    sessionInfoMenu.appendChild(userSettingsBut_container);


    // Disconnexion Button
    var disconnectBut = document.createElement("a");
    disconnectBut.href = "disconnect";
    disconnectBut.className = "logoutBut fa fa-power-off";
    disconnectBut.title = "Log out";
    sessionInfoMenu.appendChild(disconnectBut);

}


export function initSessionTimeOut(spanTimeout_id){
    try {
        
        ////////////////////////////
        // test de SSE
        // var source = new EventSource("EPCEvent?event=sessionDuration");
        var spanTimeOut = null;
        var timeoutClass = ""
        if(spanTimeout_id != null){
            spanTimeOut = document.getElementById(spanTimeout_id);
            timeoutClass = spanTimeOut.className;
        }
        const cst_timeoutClass = timeoutClass;
        const cst_spanTimeOut = spanTimeOut;

        const sessionInfoButClass = document.getElementById("sessionInfoBut").className
        const ws_top_titleClass = document.getElementById("ws_top_title").className

        /////////////////
        // Event Listener 
        /////////////////

        const evt_list_onMessage = function(event) {
            //console.log("updating " + event.data)
            var timeSinceSessionClose = parseInt(event.data);
            var minutes = Math.trunc(timeSinceSessionClose/60000);
            var secondes = Math.trunc( (timeSinceSessionClose - minutes*60000) / 1000) ;
            if(cst_spanTimeOut != null){
                try{
                    var timeOutText = "Session will close in ";
                    if(minutes < 1){
                        cst_spanTimeOut.className = "timeoutCritical blink";
                        if(secondes <=1){
                            timeOutText = "Please reload page (session expired)";
                        }else{
                            timeOutText += secondes + " s  ";
                        }
                    }else{
                        if(secondes > 30){
                            minutes++;
                        }
                        cst_spanTimeOut.className = "timeoutNormal";
                        timeOutText += minutes + " min";
                    }
                    cst_spanTimeOut.innerHTML = timeOutText;

                }catch(e){console.log(e)}
            }else{    // On recharge la page si la session est fini pour ne pas laisser l'utilisateur faire des manipulations
                //console.log("session timeout : " + minutes+"m"+secondes+"s");
                // source.close();
                if(minutes < 1 && secondes<1){
                    document.location.reload(true);
                }
            }
        };

        const evt_list_onErr = function(e) {
            if(cst_spanTimeOut != null){
                cst_spanTimeOut.className = cst_timeoutClass + " timeoutCritical blink";
                cst_spanTimeOut.innerHTML = "Connexion lost with server, please reload the page";

                document.getElementById("ws_top_title").className = ws_top_titleClass + " timeoutCritical blink";
                document.getElementById("ws_top_title").title = "Server may be down, please reload the page";
                document.getElementById("sessionInfoBut").className = sessionInfoButClass + " timeoutCritical blink";
                document.getElementById("sessionInfoBut").title = "Server may be down, please reload the page";
                enableWaitingUi(true);
                setTimeout(function(){
                    if (hasBeenDisconnected) {
                        document.location.reload(true);
                    }
                }, 4000);
            }else{    // On recharge la page si la session est fini pour ne pas laisser l'utilisateur faire des manipulations

                if(e.explicitOriginalTarget == undefined || e.explicitOriginalTarget.readyState == 0)    // si c'est une déconnexion du serveur ou de la session
                    document.location.reload(true);
            }
        };

        rws_addEventListeners('sessionDuration', evt_list_onMessage, evt_list_onErr);
    } catch(e) {
        //console.log(e);
    }
}

export function initSessionCounter(spanSessionCounter_id){
    // var source = new EventSource("EPCEvent?event=sessionCount");
    const spanSessionCounter = document.getElementById(spanSessionCounter_id);

    const sessionInfoButClass = document.getElementById("sessionInfoBut").className
    const ws_top_titleClass = document.getElementById("ws_top_title").className

    const evt_list_onMessage = function(event) {
        spanSessionCounter.style.display = '';
        spanSessionCounter.innerHTML = event.data;
    };

    const evt_list_onErr = function(e) {
        spanSessionCounter.style.display = 'none';
        spanSessionCounter.innerHTML = "";

        document.getElementById("ws_top_title").className = ws_top_titleClass + " timeoutCritical blink";
        document.getElementById("ws_top_title").title = "Server may be down, please reload the page";
        
        document.getElementById("sessionInfoBut").className = sessionInfoButClass + " timeoutCritical blink";
        document.getElementById("sessionInfoBut").title = "Server may be down, please reload the page";
    };


    rws_addEventListeners('sessionCount', evt_list_onMessage, evt_list_onErr);
}


/* ________      __          __   __  ______
  / ____/ /___  / /_  ____ _/ /  / / / /  _/
 / / __/ / __ \/ __ \/ __ `/ /  / / / // /
/ /_/ / / /_/ / /_/ / /_/ / /  / /_/ // /
\____/_/\____/_.___/\__,_/_/   \____/___/
*/

export function initEditorContent(    idTable, idObjectList, idTabHeader, idTabContainer,
                            idConsoleElt, classObjectContent, classObjectProperty, 
                            idFilterButton){    

    return new Promise(function (resolve, reject) {
                            var xmlHttp = new XMLHttpRequest();
                            beginTask();
                            // On récupère la liste des objets de l'epc
                            xmlHttp.open( "GET", "ResqmlObjectTree", true );

                            //console.log("before loading");
                            xmlHttp.onload = function (e) {
                                endTask();
                                var jsonResqmlObjectList = document.createTextNode(xmlHttp.responseText);
                                var jsonObject = JSON.parse(xmlHttp.responseText);

                                createTable(jsonObject,
                                        idTable, idObjectList, idTabHeader, idConsoleElt,
                                        idTabContainer, classObjectContent, 
                                        classObjectProperty, 0, false, idFilterButton);

                                resolve(xmlHttp.responseText);
                            };
                            xmlHttp.onerror = function (err) {
                                endTask();
                                reject(err);                    // Si erreur
                            }
                            xmlHttp.send( null );
                        });
}


export function refreshHighlightedOpenedObjects(){
    const openedObjects = [].slice.call(getAllOpendedObjects());
    const openedUUID = openedObjects.map(x => x.resqmlElt.rootUUID);
    //highlightTableLineFromTdText("colName_uuid", openedUUID);    // Le colName_uuid provient de la fonction de creation de tableau
    Array.prototype.forEach.call(openedUUID, uuid =>
            highlightTableCellFromClass(getObjectTableCellClass(uuid), true)
            );
}


export function createTable(jsonList,
        idTable, idObjectList, idTabHeader, idConsoleElt,
        idTabContainer, classObjectContent, 
        classObjectProperty, 
        sortColumnNum, sortReverse, 
        idFilterButton){

    //console.log("createTable ")
    if(!SEM_IS_LOADING_WORKSPACE){
        setSEM_IS_LOADING_WORKSPACE(true);
        beginTask();
        // on fait un code asynchrone pour que le curseur soit mis à jour 
        call_after_DOM_updated(function () {
            const tableHeaderList = ["num", "type", "title", "uuid", "schemaVersion", "package"];
            try{
                if(sortColumnNum!=null){
                    jsonList = jsonList.sort(function(a, b) {
                        return compare(a[tableHeaderList[sortColumnNum]], b[tableHeaderList[sortColumnNum]], sortReverse);
                    });
                }
            }catch(except){}

            if(jsonList!=null && jsonList.length>0){
                try{
                    var eltObjectList = getResqmlObjectTable(jsonList, tableHeaderList, document.getElementById(idTable));
                    eltObjectList.id = idTable;
                    // console.log("ID table " + idTable)
                    try{
                        // Si la table existe on l'enlève
                        document.getElementById(idTable).remove();
                    }catch(except){/*console.log(except);*/}

                    var tableContent = document.getElementById(idObjectList);
                    tableContent.appendChild(eltObjectList);
                    document.getElementById(idFilterButton).click();
                }catch(except){console.log(except);}
            }else{
                try{
                    // Si la table existe on l'enlève
                    document.getElementById(idTable).remove();
                }catch(except){/*console.log(except);*/}
            }
            //refreshHighlightedOpenedObjects();
            setSEM_IS_LOADING_WORKSPACE(false);
            endTask();
            refreshHighlightedOpenedObjects();
        });
    }
}

export function initRootEltSelector(typeSelector){
    const rgxPkg = new RegExp("_?(?<version>((?<dev>dev[\\d]+)x_)?(?<versionNum>([\\d]+[_])*\\d))$");

    const mapPackageToVersionToTypes = {};
    //createRadio
    
    for(var typeIdx=0; typeIdx<energymlRootTypes.length; typeIdx++){
        // On recupere tout sauf le nom de la classe
        var versionNum = energymlRootTypes[typeIdx].substring(0, energymlRootTypes[typeIdx].lastIndexOf(".")); 
        // on cherche la deniere partie du nom de package, qui contient le numero de version
        versionNum = versionNum.substring(versionNum.lastIndexOf(".")+1).replace(/[^\d_]/g, '');

        var packageName = energymlRootTypes[typeIdx].substring(0, energymlRootTypes[typeIdx].lastIndexOf("."));
        packageName = packageName.substring(packageName.lastIndexOf(".")+1).substring(0, packageName.length - versionNum.length);
        while(!isNaN(parseInt(packageName.slice(-1), 10)) 
            || packageName.slice(-1) == "_" ){
            packageName = packageName.substring(0, packageName.length - 1);
        }


        //console.log(energymlRootTypes[typeIdx] +" version '" + versionNum + "'' pkg '" + packageName +"'")

        if(mapPackageToVersionToTypes[packageName]==null){
            mapPackageToVersionToTypes[packageName] = {};
        }
        if(mapPackageToVersionToTypes[packageName][versionNum]==null){
            mapPackageToVersionToTypes[packageName][versionNum] = [];
        }
        mapPackageToVersionToTypes[packageName][versionNum].push(energymlRootTypes[typeIdx]);
    }

    const selectVersionContainer = document.getElementById("modal_createRootElt_pkgChooserSelect");
    const radioVersionContainer = document.getElementById("modal_createRootElt_versionChooserDiv");

    const radioIdPrefix = "modal_createRootElt_radioVersion_";

    var countPackage = 0;
    for(var pkg in mapPackageToVersionToTypes){
        const constPkg = pkg;
        const pkgMapVersionToType = mapPackageToVersionToTypes[pkg];

        const pkgTypesDiv = document.createElement("div");
        pkgTypesDiv.name = constPkg+"_versions";
        pkgTypesDiv.style.display = 'none';
        pkgTypesDiv.className = "form-check form-check-inline";

        const radioPkgVersionName = radioIdPrefix + constPkg;

        var countType = 0;
        for(var version in mapPackageToVersionToTypes[pkg]){
            const constVersion = version;
            var found = version.match(rgxPkg);
            var versionVue = found.groups["versionNum"].replace("_", ".")
            if(found.groups["dev"]!=null){
                versionVue += "(" + found.groups["dev"] +")";
            }
            var radioVersion = createRadio("v " + versionVue, "", radioPkgVersionName, 
                                            function(){
                                                while(typeSelector.firstChild){
                                                    typeSelector.firstChild.remove();
                                                }
                                                pkgMapVersionToType[constVersion].sort();

                                                for(var eltIdx=0; eltIdx<pkgMapVersionToType[constVersion].length; eltIdx++){
                                                    var opt = document.createElement("option");
                                                    opt.value = pkgMapVersionToType[constVersion][eltIdx];
                                                    opt.appendChild(document.createTextNode(opt.value.substring(opt.value.lastIndexOf(".") + 1)));
                                                    typeSelector.appendChild(opt);
                                                }
                                            }, 
                                            countType==0 && countPackage==0);
            radioVersion.className += " form-check-inline";
            
            if(countType==0 && countPackage==0){
                pkgMapVersionToType[constVersion].sort();
                for(var eltIdx=0; eltIdx<pkgMapVersionToType[constVersion].length; eltIdx++){
                    var opt = document.createElement("option");
                    opt.value = pkgMapVersionToType[constVersion][eltIdx];
                    opt.appendChild(document.createTextNode(opt.value.substring(opt.value.lastIndexOf(".") + 1)));
                    typeSelector.appendChild(opt);
                    pkgTypesDiv.style.display = ''; 
                }
                countType++;
            }

            pkgTypesDiv.appendChild(radioVersion);
            countType++;
        }

        radioVersionContainer.appendChild(pkgTypesDiv);


        var optionPkg = document.createElement("option");
        optionPkg.value = constPkg;
        optionPkg.appendChild(document.createTextNode(constPkg));
        selectVersionContainer.appendChild(optionPkg);


        countPackage++;
    }
    selectVersionContainer.onchange = function(){
        for(var pkVersionChooserIdx=0; 
                pkVersionChooserIdx < radioVersionContainer.childNodes.length; 
                pkVersionChooserIdx++){
            try{
                // on invisibilise toutes les versions
                if(radioVersionContainer.childNodes[pkVersionChooserIdx].name == this.value + "_versions"){
                    radioVersionContainer.childNodes[pkVersionChooserIdx].style.display = "";
                }else{
                    radioVersionContainer.childNodes[pkVersionChooserIdx].style.display = "none";
                }
                //console.log(radioVersionContainer.childNodes[pkVersionChooserIdx].name + " == " + this.value + "_versions")
            }catch(e){}
        } 
        //pkgTypesDiv.style.display = '';
        //console.log("Enabeling : " + radioPkgVersionName)
        console.log(">> : " + radioIdPrefix+this.value)
        radioVersionContainer.querySelector('input[name='+radioIdPrefix+this.value+']').click();
    }
}

export function getResqmlObjectTable(dataTableContent, dataTableColumn, oldTable){
    var dataTableHeader = dataTableColumn.map(title => title[0].toUpperCase() + title.substring(1));
    var table = createTableFromData(dataTableContent, dataTableColumn, dataTableHeader, 
                                    dataTableContent.map(elt => [ function(){openResqmlObjectContentByUUID(elt['uuid'])} ]), 
                                    null,
                                    null); 
                                        // On met le oldTable à null ici pour trier seulement apres l'ajout de la nouvelle colonne,
                                        // sinon les boutons de suppressions ne seront pas en face des bons elements.

    transformTab_AddColumn(table, "", 0, dataTableContent.map(elt => createDeleteResqmlButton(elt)), "colName_delete", "");
    copyOtherTableSortingParams(table, oldTable, dataTableColumn);

    return table;
}




/*
 _    __              ____       _            __        __  _
| |  / /_  _____     / __ \_____(_)__  ____  / /_____ _/ /_(_)___  ____
| | / / / / / _ \   / / / / ___/ / _ \/ __ \/ __/ __ `/ __/ / __ \/ __ \
| |/ / /_/ /  __/  / /_/ / /  / /  __/ / / / /_/ /_/ / /_/ / /_/ / / / /
|___/\__,_/\___/   \____/_/  /_/\___/_/ /_/\__/\__,_/\__/_/\____/_/ /_/
*/

// define the position of the objects list
export function setVueOrientation(orient, savePref){
    var mainDiv = document.getElementById("mainSplitter");
    var mainDivChilds =mainDiv.children;

    for(var i=0; i < mainDivChilds.length; i++){
        var child = mainDivChilds[i];
        // On enleve le split actuel
        if(child.className.includes("gutter")){
            child.remove();
        }
    }

    var workspaceListVue = document.getElementById("workspaceObjectListPanel");
    var objectVue = document.getElementById("objectsVuePanel");

    if(orient == null || orient.toLowerCase() == "right"){
        mainDiv.insertBefore(objectVue, workspaceListVue);
        createSplitter("#objectsVuePanel", "#workspaceObjectListPanel", 35, 60, null, 55, 16);
        objectVue.className = objectVue.className.replace("vue-right-part", "vue-left-part");
        workspaceListVue.className = workspaceListVue.className.replace("vue-left-part", "vue-right-part");
    }else{
        mainDiv.appendChild(objectVue);
        createSplitter("#workspaceObjectListPanel","#objectsVuePanel", 60, 35, null, 55, 16);
        workspaceListVue.className = workspaceListVue.className.replace("vue-right-part", "vue-left-part");
        objectVue.className = objectVue.className.replace("vue-left-part", "vue-right-part");
    }
    if(savePref==null || savePref != false){
        savePreferences();
    }
}

export function reverseVueOrientation(){
    setVueOrientation(getVueOrientationReverse());
}

export function getVueOrientation(){
    var mainDivChilds = document.getElementById("mainSplitter").children;

    for(var i=0; i < mainDivChilds.length; i++){
        var child = mainDivChilds[i];
        if(child.id.includes("workspaceObjectListPanel")){
            return "left";
        }else if(child.id.includes("objectsVuePanel")){
            return "right";
        }
    }
    return "";
}
export function getVueOrientationReverse(){
    var currentOrient = getVueOrientation();
    if(currentOrient == null || currentOrient == "" || currentOrient.toLowerCase() == "right"){
        return "left";
    }else{
        return "right";
    }
}