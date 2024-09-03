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

import {createTableFilterInput, createTableFromData, transformTabToFormCheckable} from "../table.js"
import {call_after_DOM_updated} from "../htmlUtils.js"
import {refreshHighlightedOpenedObjects} from "../ui.js"


export function createPartialExportView(jsonContent, checkboxesName, f_OnCheckToggle){
    var tableDOR = createTableFromData(
                            jsonContent, 
                            ["num", "title", "type", "uuid", "schemaVersion"], 
                            ["Num", "Title", "Type", "UUID", "SchemaVersion"], 
                            null, null, null);

    transformTabToFormCheckable(tableDOR, jsonContent.map(elt => elt.uuid), checkboxesName, f_OnCheckToggle);
    return tableDOR;
}

export function checkElement(uuid, checkboxesName, checkedValue){
    //console.log("checking : " + uuid + " checkedValue " + checkedValue);
    const checkboxes = document.getElementsByName(checkboxesName);
    for(var i=0; i<checkboxes.length; i++){
        if(checkboxes[i].value == uuid){
            checkboxes[i].checked = checkedValue;
        }
    }
}

export function checkAllRelations(uuid, relations, checkboxesName, checkedValue, cbUpRelId, cbDownRelId){
    call_after_DOM_updated(function(){


    var uuidStack = [];
    uuidStack.push(uuid);
    
    var uuidView = [];
    while(uuidStack.length>0){
        var currentUUID = uuidStack.pop();
        uuidView.push(currentUUID);
        
        if(relations[currentUUID] != null){
            if(document.getElementById(cbUpRelId).checked){
                for(var r_upIdx=0; r_upIdx<relations[currentUUID].relationUp.length; r_upIdx++){
                    try{
                        var r_up = relations[currentUUID].relationUp[r_upIdx];
                        checkElement(r_up.uuid, checkboxesName, checkedValue);
                        
                        var r_up_lowerType = relations[r_up.uuid].type.toLowerCase();

                        if(!uuidStack.includes(r_up.uuid) && !uuidView.includes(r_up.uuid)){
                            uuidStack.push(r_up.uuid);
                        }
                    }catch(e){
                        console.log(e);
                    }
                }
            }
    
            if(document.getElementById(cbDownRelId).checked){
                /***********************************************
                    On filtre pour ne pas tout cocher sans le vouloir : les Local3DCRS sont lies a tout !                
                ***********************************************/
                var lowerType = relations[currentUUID].type.toLowerCase();
                if( !(lowerType.includes("local") && lowerType.includes("3dcrs") ) 
                    && !lowerType.includes("externalpartref")
                    && !lowerType.includes("propertykind")
                    && !lowerType.includes("propertyset")
                    && !lowerType.includes("tablelookup")
                 ){
                    //console.log(lowerType)
                    for(var r_downIdx=0; r_downIdx<relations[currentUUID].relationDown.length; r_downIdx++){
                        var r_down = relations[currentUUID].relationDown[r_downIdx];

                        /***********************************************
                            On inclus pas les WelboreMarkerFrameRep pour qu'ils n'appellent pas d'autres interpretations                
                        ***********************************************/
                        var r_down_lowerType = relations[r_down.uuid].type.toLowerCase();

                        if( !(r_down_lowerType.includes("wellbore") && r_down_lowerType.includes("marker") && r_down_lowerType.includes("representation") ) ) {

                            

                            checkElement(r_down.uuid, checkboxesName, checkedValue);
                            if(!uuidStack.includes(r_down.uuid) && !uuidView.includes(r_down.uuid)){
                                uuidStack.push(r_down.uuid);
                            }
                        }
                    }
                }
            }
        }else{
            console.log("err : no uuid '"+currentUUID+"' in relation map");

            console.log(currentUUID);
            console.log("-----------");
            console.log(relations);
            console.log("-----------");
        }
    }

    });
}

export function updatePartialExportTableContent(relations){
    document.getElementById("exportParial_progressBar").style.display ="";
    var tableContent = [];
    for(var uuid in relations){
        tableContent.push(relations[uuid]);
    }

    const checkboxesName = "partialExportUUID";

    // on doit convertir en liste pour creer le tableau
    const table = createPartialExportView(tableContent, checkboxesName, 
                        function(checkedValue, uuid){
                            if(checkedValue && relations[uuid] != null){
                                checkAllRelations(uuid, relations, checkboxesName, checkedValue, 
                                    "epcPartialExport_checkUpRelations", 
                                    "epcPartialExport_checkDownRelations");
                            }else{
                                //console.log("err > checkedValue " + checkedValue  + " -- " + uuid);
                            }
                        }
                    );

    const elt_partialExportEPC_table = document.getElementById("partialExportEPC_table");
    elt_partialExportEPC_table.parentNode.parentNode.insertBefore(createTableFilterInput(table), elt_partialExportEPC_table.parentNode);
    elt_partialExportEPC_table.appendChild(table);
    refreshHighlightedOpenedObjects();
    document.getElementById("exportParial_progressBar").style.display ="none";
}