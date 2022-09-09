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

import {sendGetURL_Promise} from "../../requests/requests.js"
import {openResqmlObjectContentByUUID} from "../../main.js"
import {createCollapser} from "../htmlUtils.js"
import {mapResqmlTypeToSubtypes} from "../../energyml/epcContentManager.js"
import {getObjectTableCellClass} from "../../common/variables.js"
import {refreshHighlightedOpenedObjects} from "../ui.js"

const __organizationType__ = [];

export function __initOrganizationType__(){
    for (var superType in mapResqmlTypeToSubtypes){
        if(superType.toLowerCase().includes("organization")){
            __organizationType__.push(superType);
        }
    }
/*    console.log("__organizationType__ => ");
    console.log(__organizationType__)*/
}

export function __getObjectCitation(objUUID){
    sendGetURL_Promise("ResqmlObjectTree?uuid=" + objUUID + "&path=" + ".Citation").then(
        responseText => {
            //console.log(responseText)
            try{
                var citationView = document.getElementById("modal_FIRPView_citationView");

                var prop = JSON.parse(responseText).properties;

                var citationContainer = document.createElement("div");
                citationContainer.style.display = 'contents';

                var title = document.createElement("h3");
                title.appendChild(document.createTextNode(objUUID + " Citation"));
                title.onclick = () => openResqmlObjectContentByUUID(objUUID);
                title.style.cursor = "pointer"
                title.title = "open object to edit"

                citationContainer.appendChild(title);
                
                var table = document.createElement("table");
                table.className = "table-striped table-bordered table-hover table-fixed table-top-fixed";

                var tableBody = document.createElement("tbody");
                table.appendChild(tableBody);

                for(var propertyIdx in prop){
                    var property = prop[propertyIdx];
                    var tableLine = document.createElement("tr");
                    var propTitle = document.createElement("td");
                    var propValue = document.createElement("td");

                    propTitle.appendChild(document.createTextNode(
                                            property.name.includes(".") 
                                            ? property.name.substring(property.name.lastIndexOf("."))
                                            : property.name
                                        ) );
                    propValue.appendChild(document.createTextNode(property.value));
                    tableLine.appendChild(propTitle);
                    tableLine.appendChild(propValue);
                    tableBody.appendChild(tableLine);
                }
                citationContainer.appendChild(table);

                while(citationView.firstChild){
                    citationView.removeChild(citationView.firstChild);
                }
                citationView.appendChild(citationContainer);
                //return citationContainer;
            }catch(e){
                console.log(e)
            }
        }
        );
}

export function isAnOrganizationtype(str_type){
    if (__organizationType__.length <= 0){
        __initOrganizationType__();
    }

    var str_type_lc = str_type.toLowerCase();

    for(var orgaType in __organizationType__){
        for(var subOrgaType in mapResqmlTypeToSubtypes[orgaType]){
            if(subOrgaType.toLowerCase().includes(str_type_lc)){
                return true;
            }
        }
    }
    return str_type.toLowerCase().includes("model") || str_type.toLowerCase().includes("organization");
}

export function getFirpObjType(obj){
    var rel_type = obj.type;
    var lastDotIdx = rel_type.lastIndexOf(".")
    if(lastDotIdx >= 0){
        rel_type = rel_type.substr(lastDotIdx + 1);
    }
    return rel_type;
}

export function getRelatedDown(uuid, firp_data){
    //console.log("#getRelatedDown " + uuid);
    var obj = null;
    try {
        obj = firp_data[uuid]
    } catch(e) { 
        console.log(e);
    }

    if(obj){
        if(obj.relationDown.length>0)
            return obj.relationDown.map((elt) => getRelatedDown(elt.uuid, firp_data) ).flat(10);
    }
    return []
}

export function createSubList(uuid, firp_data, get_relation_func, filter_func){
    var obj = null;
    try {
        obj = firp_data[uuid];
        if(obj == null || (filter_func!=null && !filter_func(obj))) {
            /*console.log("filtered : ");
            console.log(uuid)*/
            return null;
        }
        //console.log(obj.type)
    } catch(e) { console.log(e); }
    
    const container = document.createElement("span");
    container.className += getObjectTableCellClass(uuid);
    if(uuid in firp_data){
        const container0 = document.createElement("span");
        const container1 = document.createElement("span");
        container1.style.fontWeight = 'bold';
        container0.appendChild(document.createTextNode(firp_data[uuid].firp[0] + ") " + firp_data[uuid].title));
        container1.appendChild(document.createTextNode(" [" + firp_data[uuid].type + "]"));
        container.appendChild(container0);
        container.appendChild(container1);

        container0.style.cursor = 'pointer';
        container0.onclick = function(){__getObjectCitation(uuid);};
    } else{
        container.appendChild(document.createTextNode(uuid));
        container.onclick = () => openResqmlObjectContentByUUID(uuid);
        container.style.cursor = 'pointer';
    }

    var relations = get_relation_func(obj);

    if(relations.length > 0){
        const divList = document.createElement("ul");
        try {
            Array.prototype.forEach.call(relations, (elt) => {
                var sub = createSubList(elt.uuid, firp_data, get_relation_func, filter_func);
                if(sub != null){
                    var liSub = document.createElement("li");
                    liSub.style.listStyle = 'none';
                    liSub.appendChild(sub);
                    divList.appendChild(liSub);
                }
            });
        } catch(e) { console.log(e); }

        if(divList.children.length > 0){
            return createCollapser(container, divList);
        }
    }
    var div_container = document.createElement("div");
    div_container.appendChild(container);
    return div_container;
}

export function sortRelations(relations, firp_data){
    var sortedFirpUuid = [];

    try{
        sortedFirpUuid = relations.sort(
                function(A, B){
                    var typeA = getFirpObjType(firp_data[A.uuid]);
                    var typeB = getFirpObjType(firp_data[B.uuid]);

                    return (typeA != typeB ? 
                            typeA.localeCompare(typeB) 
                            : firp_data[A.uuid].title.localeCompare(firp_data[B.uuid].title))
                }
            );
    }catch(e){
        console.log(e);
        sortedFirpUuid = relations;
    }
    return sortedFirpUuid;
}

export function sortFirpDataUuid(firp_data){
    var sortedFirpUuid = [];

    sortedFirpUuid = Object.keys(firp_data).sort((A, B) => 
                            (getFirpObjType(firp_data[A])!=getFirpObjType(firp_data[B]) ? 
                                getFirpObjType(firp_data[A]).localeCompare(getFirpObjType(firp_data[B])) 
                                : firp_data[A].title.localeCompare(firp_data[B].title)));

    return sortedFirpUuid;
}


export function updateFIRPView_data(firp_data){
    if(firp_data != null){
        const divList = document.getElementById("modal_FIRPView_treeDiv");
        const ulDiv = document.createElement("ul");

        var rel_keys = sortFirpDataUuid(firp_data);
        //Object.keys(firp_data).forEach(
        rel_keys.forEach(
            (element, index) => {
                if(firp_data[element].firp.toLowerCase() == "feature" 
                    && !isAnOrganizationtype(firp_data[element].type) ){
                    ulDiv.appendChild(createSubList(element, firp_data, 
                                        function(obj) {
                                            var relsDown = obj.relationDown;
                                            const lowertype = obj.type.toLowerCase();
                                            return sortRelations(relsDown.filter(rel => 
                                                    (lowertype.includes("representation")
                                                        && rel.name.toLowerCase().includes("supportingrepresentation"))
                                                ||  (lowertype.includes("interpretation")
                                                        && rel.name.toLowerCase().includes("represent"))
                                                ||  (lowertype.includes("feature")
                                                        && rel.name.toLowerCase().includes("interpret"))
                                                ), firp_data);
                                            //console.log(relsDown)
                                        }, 
                                        obj => !isAnOrganizationtype(obj.type)));
                }
        });

        divList.appendChild(ulDiv);
    }
    refreshHighlightedOpenedObjects();
}

export function updateModelView_data(firp_data){
    if(firp_data != null){
        const divList = document.getElementById("modal_ModelView_treeDiv");
        const ulDiv = document.createElement("ul");

        var rel_keys = sortFirpDataUuid(firp_data);
        //Object.keys(firp_data).forEach(
        rel_keys.forEach(
            (element, index) => {
                if(isAnOrganizationtype(firp_data[element].type) 
                    && !firp_data[element].type.toLowerCase().includes("feature")
                    && firp_data[element].type.toLowerCase() != "model"
                    ){
                    var subElt = createSubList(element, firp_data,
                                        function(obj){

                                            // return sortRelations(obj.relationDown, firp_data);
                                            // Si c'est une columnRankInterp : on élimine tout ce qui n'est pas une interprétation d'un RockVolume
                                            // Pour l'instant on filtre les HorizonInterpreation (pour les enlever)
                                            try{
                                            if(obj.type.toLowerCase().includes("columnrankinte")){
                                                var tempRelations = obj.relationUp;
                                                var finalRelations = [];

                                                for(var rel_idx in tempRelations){
                                                    var rel = tempRelations[rel_idx];
                                                    if(!firp_data[rel.uuid].type.toLowerCase().includes("horizoninterpretation")){
                                                        finalRelations.push(rel);
                                                    }else{
                                                        // console.log("on bypass : ");
                                                        // console.log(rel)
                                                    }
                                                }
                                                return sortRelations(finalRelations, firp_data);
                                            }else if(obj.type.toLowerCase().includes("earthmodelinterpretation")){
                                                var relations = obj.relationUp;
                                                for(var relDownIdx in obj.relationDown){
                                                    var rel = obj.relationDown[relDownIdx];
                                                    if(rel.name.toLowerCase().includes("represent")){
                                                        relations.push(rel);
                                                    }
                                                }
                                                return sortRelations(relations, firp_data);
                                            }else if(obj.type.toLowerCase().includes("representation")
                                                || obj.type.toLowerCase().includes("property")){

                                                return sortRelations(obj.relationDown.filter(rel => 
                                                    rel.name.toLowerCase().includes("supportingrepresentation")), firp_data);
                                            }
                                            return sortRelations(obj.relationUp, firp_data);
                                            }catch(e){
                                                console.log(e)
                                                return [];
                                            }
                                        }, 
                                        obj => !obj.type.toLowerCase().includes("feature")
                                                && obj.type.toLowerCase() != "model");
                    if(subElt){
                        ulDiv.appendChild(subElt);
                    }else{
                        console.log("Null list");
                        console.log(element);
                    }
                }
        });

        divList.appendChild(ulDiv);
    }
    refreshHighlightedOpenedObjects();
}

export function updateFIRPView(){
    document.getElementById("modal_FIRPView_progressBar").style.display = "";
    const elt_FIRPViewEPC_table = document.getElementById("modal_FIRPView_treeDiv");
    while (elt_FIRPViewEPC_table.firstChild) {
        elt_FIRPViewEPC_table.removeChild(elt_FIRPViewEPC_table.firstChild);
    }

    const elt_ModelViewEPC_table = document.getElementById("modal_ModelView_treeDiv");
    while (elt_ModelViewEPC_table.firstChild) {
        elt_ModelViewEPC_table.removeChild(elt_ModelViewEPC_table.firstChild);
    }
       // console.log("updateFIRPView");
       var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", "ResqmlEPCRelationship", true );

    xmlHttp.onload = function(){
        try{
            const relations = JSON.parse(xmlHttp.responseText);
            updateFIRPView_data(relations);
            updateModelView_data(relations);
        }catch(e){
            console.log(e);
        }
        document.getElementById("modal_FIRPView_progressBar").style.display = "None";
    };
    xmlHttp.send(null);
}