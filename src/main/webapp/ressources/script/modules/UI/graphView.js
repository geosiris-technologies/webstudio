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

import {createCheck, createCollapser, createSplitter} from "./htmlUtils.js"
import {getOpenObjectsUuid, openResqmlObjectContentByUUID} from "../main.js"
import {getJsonObjectFromServer} from "../requests/requests.js"


import cytoscape from "./lib/cytoscape/cytoscape.esm.min.js"
cytoscapeFcose(cytoscape)

var CY_DBL_LEFT_CLICK_DATE = new Date();
var CY_DBL_LEFT_CLICK_LAST_NODE_CLICKED_UUID = "";

var CY_DBL_RIGHT_CLICK_DATE = new Date();
var CY_DBL_RIGHT_CLICK_LAST_NODE_CLICKED_UUID = "";

var CY_DBL_CLICK_TIME_LIMIT = 0.4;

function initiateGraphView(containerDIV){

    containerDIV.className = "__________cytoscape_container";
    var cy = cytoscape({

            container: containerDIV, // container to render in
            elements: [],
            style: cyGetStyle('data(name)', 'data(name)'),

            layout: {
                name: 'grid',
                rows: 1
            },
            ready: function (e) {

               /* const currentCY = e.cy;
                currentCY.on('render', function (e) {
                });
                currentCY.on('pan', function (e) {
                });
                currentCY.on('viewport', function (e) {
                });
                currentCY.on('style', function (e) {
                });

                currentCY.on('layoutstart', function (e) {
                });
                currentCY.on('layoutstop', function (e) {
                });*/
            }

        });
    return cy;
}

export function cyGetStyle(nodeLabel, edgeLabel, enableArrow){
    
    if(edgeLabel==null){
        edgeLabel = 'data(name)';
    }

    return [ // the stylesheet for the graph
            cyGetNodeStyle(nodeLabel),
            cyGetEdgeStyle(edgeLabel, enableArrow)
            ]
}

export function cyGetEdgeStyle(edgeLabel, enableArrow){
    if(edgeLabel==null){
        edgeLabel = 'data(name)';
    }
    if(enableArrow!=null && enableArrow){
        return {
                    selector: 'edge',
                    style: 
                    {
                        'width': 2,
                        'line-color': '#555',
                        'target-arrow-color': '#000',
                        'target-arrow-shape': 'triangle',
                        'curve-style' : 'straight',
                        'label': edgeLabel,
                        'color' : '#555'

                    }

                };
    }else{
        return {
                    selector: 'edge',
                    style: 
                    {
                        'width': 2,
                        'line-color': '#555',
                        'label': edgeLabel,
                        'color' : '#555'
                    }

                };
    }
}

export function cyGetNodeStyle(nodeLabel){
    if(nodeLabel==null){
        nodeLabel = 'data(name)';
    }
    return {
                selector: 'node',
                css: 
                {
                    'background-color': '#448',
                    'label': nodeLabel
                },

            };
}

export function getFcoseLayout(){
    var fcoseLayout = {
        name:"fcose",
        // 'draft', 'default' or 'proof' 
        // - "draft" only applies spectral layout 
        // - "default" improves the quality with incremental layout (fast cooling rate)
        // - "proof" improves the quality with incremental layout (slow cooling rate) 
        quality: "proof",
        // Use random node positions at beginning of layout
        // if this is set to false, then quality option must be "proof"
        randomize: true, 
        // Whether or not to animate the layout
        animate: true, 
        // Duration of animation in ms, if enabled
        animationDuration: 1000, 
        // Easing of animation, if enabled
        animationEasing: undefined, 
        // Fit the viewport to the repositioned nodes
        fit: true, 
        // Padding around layout
        padding: 10,
        // Whether to include labels in node dimensions. Valid in "proof" quality
        nodeDimensionsIncludeLabels: false,
        // Whether or not simple nodes (non-compound nodes) are of uniform dimensions
        uniformNodeDimensions: false,
        // Whether to pack disconnected components - valid only if randomize: true
        packComponents: true,
        
        /* spectral layout options */
        
        // False for random, true for greedy sampling
        samplingType: true,
        // Sample size to construct distance matrix
        sampleSize: 25,
        // Separation amount between nodes
        nodeSeparation: 150,
        // Power iteration tolerance
        piTol: 0.0000001,
        
        /* incremental layout options */
        
        // Node repulsion (non overlapping) multiplier
        nodeRepulsion: 1000000,
        // Ideal edge (non nested) length
        idealEdgeLength: 100,
        // Divisor to compute edge forces
        edgeElasticity: 0.45,
        // Nesting factor (multiplier) to compute ideal edge length for nested edges
        nestingFactor: 0.1,
        // Maximum number of iterations to perform
        numIter: 2500,
        // For enabling tiling
        tile: true, 
        // Represents the amount of the vertical space to put between the zero degree members during the tiling operation(can also be a function)
        tilingPaddingVertical: 10,
        // Represents the amount of the horizontal space to put between the zero degree members during the tiling operation(can also be a function)
        tilingPaddingHorizontal: 10,
        // Gravity force (constant)
        gravity: 0.25,
        // Gravity range (constant) for compounds
        gravityRangeCompound: 1.5,
        // Gravity force (constant) for compounds
        gravityCompound: 1.0,
        // Gravity range (constant)
        gravityRange: 3.8, 
        // Initial cooling factor for incremental layout    
        initialEnergyOnIncremental: 0.3,    

        /* layout event callbacks */
        ready: () => {}, // on layoutready 
        stop : () => {}  // on layoutstop
    };

    return fcoseLayout;
}

export function createGraphView(container, layout, dataMap){
    var cy = initiateGraphView(container);

    for(var uuid in dataMap){
        var data = dataMap[uuid];
        addNodeToGraph(data, cy, dataMap);
    }
    
    var lay = cy.layout(layout);
    lay.run(); 
    return cy;
}

export function updateNodeStatusRelations(node, cy, relations){
    var edown = cy.edges('[target = "' + node.uuid + '"]');
    var eup   = cy.edges('[source = "' + node.uuid + '"]');
    if(node.relationDown.length != edown.length
        || node.relationUp.length != eup.length){
        var cyCurNode = cy.nodes('[id = "'+node.uuid+'"]');
        cyCurNode.style('border-width', 2);
        cyCurNode.style('border-color', 'black');
        cyCurNode.style('border-style', 'dashed');
    }else{
        var cyCurNode = cy.nodes('[id = "'+node.uuid+'"]');
        /*cyCurNode.style('border-width', 0);
        cyCurNode.style('border-color', '');*/
        cyCurNode.style('border-width', 1);
        cyCurNode.style('border-color', 'black');
        cyCurNode.style('border-style', 'solid');
    }
}


export function cyToggleNode(    node, cy, relations, 
                        nodeActionFunc_LeftClick, nodeActionFunc_LeftClickDBL, 
                        nodeActionFunc_RightClick, nodeActionFunc_RightClickDBL){
    if(cy.nodes('[id = "'+node.uuid+'"]').length<=0){
        addNodeToGraph(node, cy, relations, 
                        nodeActionFunc_LeftClick, nodeActionFunc_LeftClickDBL, 
                        nodeActionFunc_RightClick, nodeActionFunc_RightClickDBL);
    }else{
        removeNodeFromGraph(node.uuid, cy, relations);
    }
}

export function removeNodeFromGraph(nodeUUID, cy, relations){
    var oldLinkedEdges_T = cy.edges('[target = "' + nodeUUID + '"]');
    var oldLinkedEdges_S = cy.edges('[source = "' + nodeUUID + '"]');

    var oldNodes_T = oldLinkedEdges_T.map(edge => edge.source().data("id"));
    var oldNodes_S = oldLinkedEdges_S.map(edge => edge.target().data("id"));
    
    oldLinkedEdges_T.remove();
    oldLinkedEdges_S.remove();

    cy.nodes('[id = "' + nodeUUID + '"]').remove();

    oldNodes_T.forEach( function(uuid, index) {
        updateNodeStatusRelations(relations[uuid], cy, relations);
    });
    oldNodes_S.forEach( function(uuid, index) {
        updateNodeStatusRelations(relations[uuid], cy, relations);
    });
    
}

export function updateNodeData(node, cy, relations,
                        nodeActionFunc_LeftClick, nodeActionFunc_LeftClickDBL, 
                        nodeActionFunc_RightClick, nodeActionFunc_RightClickDBL){
    var currentNode = cy.nodes('[id = "' + node.uuid + '"]');
    var posX = null;
    var posY = null;
    if(currentNode.length>0){
        posX = currentNode.position('x');
        posY = currentNode.position('y');
    }
    removeNodeFromGraph(node.uuid, cy, relations);
    addNodeToGraph(node, cy, relations,
                        nodeActionFunc_LeftClick, nodeActionFunc_LeftClickDBL, 
                        nodeActionFunc_RightClick, nodeActionFunc_RightClickDBL, 
                        posX, posY);
}


/** node click functions take 3 param : the node in the relations map, the cy and the relations  **/
/** returns true if added, else false **/
export function addNodeToGraph(node, cy, relations, 
                        nodeActionFunc_LeftClick, nodeActionFunc_LeftClickDBL, 
                        nodeActionFunc_RightClick, nodeActionFunc_RightClickDBL, 
                        posX, posY){
    // si n'existe pas deja
    //console.log(node);
    if(node != null && cy.nodes('[id = "'+node.uuid+'"]').length<=0){
        var tableEdgeUptoAdd = [];
        var tableEdgeDowntoAdd = [];
        
        var nbNeighbor = 0.0;
        var baryPosX   = 0.0;
        var baryPosY   = 0.0;

        for(var downIdx=0; downIdx<node.relationDown.length; downIdx++){
            var dataDown = node.relationDown[downIdx];
            var neighborsDown = cy.nodes('[id = "' + dataDown.uuid + '"]');
            //console.log("relDown : " ); console.log(neighborsDown);
            if(neighborsDown.length>0){
                nbNeighbor++;
                if(nbNeighbor==1){
                    baryPosX = neighborsDown[0].position('x');
                    baryPosY = neighborsDown[0].position('y');
                }else{
                    baryPosX = (nbNeighbor-1.0)/nbNeighbor*baryPosX + neighborsDown[0].position('x') / nbNeighbor;
                    baryPosY = (nbNeighbor-1.0)/nbNeighbor*baryPosY + neighborsDown[0].position('y') / nbNeighbor;
                }
                tableEdgeDowntoAdd.push({ 
                        group: 'edges', 
                        data: { 
                            id : "eDOWN_" + node.uuid +"_"+dataDown.uuid, 
                            source : dataDown.uuid, target: node.uuid, 
                            name : dataDown.name
                        },
                        /*css: {
                            'line-color': '#000',
                        }*/
                    });
            }
        }
        for(var upIdx=0; upIdx<node.relationUp.length; upIdx++){
            var dataUp = node.relationUp[upIdx];
            var neighborsUp = cy.nodes('[id = "' + dataUp.uuid + '"]');
            //console.log("dataUp : " ); console.log(neighborsUp);
            if(neighborsUp.length>0){
                nbNeighbor++;
                if(nbNeighbor==1){
                    baryPosX = neighborsUp[0].position('x');
                    baryPosY = neighborsUp[0].position('y');
                }else{
                    baryPosX = (nbNeighbor-1)/nbNeighbor*baryPosX + neighborsUp[0].position('x') / nbNeighbor;
                    baryPosY = (nbNeighbor-1)/nbNeighbor*baryPosY + neighborsUp[0].position('y') / nbNeighbor;
                }
                tableEdgeUptoAdd.push({ 
                        group: 'edges', 
                        data: { 
                            id: "eUP_" + dataUp.uuid + "_" + node.uuid, 
                            target: dataUp.uuid, source: node.uuid, 
                            name : dataUp.name
                        },
                        /*css: {
                            'line-color': '#000',
                        }*/
                    });
            }
        }

        // Si un seul voisin, on positionne le noeud aleatoirement autour a une distance de @distToNeighbor
        if(nbNeighbor==1.0){
            var randX = Math.random()*2.0 - 1.0;
            var distToNeighbor = 200; // distance au seul voisin;
            var isYPositiv = Math.random() > 0.5;
            baryPosX += randX*distToNeighbor;
            var yDisplacement = Math.sqrt(distToNeighbor*distToNeighbor - (randX*distToNeighbor) * (randX*distToNeighbor));
            if(isYPositiv){
                baryPosY += yDisplacement;
            }else{
                baryPosY -= yDisplacement;
            }
        }else if(nbNeighbor==0){
            // Si 0 voisin, on place au milieu de la vue
            baryPosX = cyGetCenterX(cy);
            baryPosY = cyGetCenterY(cy);
        }
        var nodeSize = Math.min(30 + 4*(node.relationUp.length + node.relationDown.length), 120);
        if(node.firp.toLowerCase()=="others"){
            nodeSize = 30;
        }
        if(posX != null){
            baryPosX = posX;
        }
        if(posY != null){
            baryPosY = posY;
        }
        var addedNode = cy.add({ 
                group: 'nodes', 
                data: { name: cyGetNodeNamePrefix(node) + node.title, id: node.uuid }, 
                position: { x: baryPosX, y: baryPosY } ,
                css: {
                    'background-color': cyGetNodeColor(node),
                    'width'  : nodeSize,
                    'height' : nodeSize
                }
            }); 


        /*  
            ____  ____________  ________   ________    ____________ __
           / __ \/  _/ ____/ / / /_  __/  / ____/ /   /  _/ ____/ //_/
          / /_/ // // / __/ /_/ / / /    / /   / /    / // /   / ,<
         / _, _// // /_/ / __  / / /    / /___/ /____/ // /___/ /| |
        /_/ |_/___/\____/_/ /_/ /_/     \____/_____/___/\____/_/ |_|

        */


        addedNode.on('cxttap', function(e){
            var currentDate = new Date();

            var timeSinceLastClick_in_sec = (currentDate - CY_DBL_RIGHT_CLICK_DATE)/1000;
            CY_DBL_RIGHT_CLICK_DATE = currentDate;

            //console.log(timeSinceLastClick_in_sec +"s since last click");

            var clickedNode = e.target;
            var currentNode = relations[clickedNode.data().id];

            // FUNCTION EVT
            if(nodeActionFunc_RightClick!=null){
                try{
                    nodeActionFunc_RightClick(currentNode, cy, relations);
                }catch(e){console.log(e);}
            }

            // Si on a bien double clické 2 fois sur le meme noeud en moins de 20ms
            if(timeSinceLastClick_in_sec < CY_DBL_CLICK_TIME_LIMIT && CY_DBL_RIGHT_CLICK_LAST_NODE_CLICKED_UUID.localeCompare(clickedNode.data().id) == 0){
                // FUNCTION EVT
                if(nodeActionFunc_RightClickDBL!=null){
                    try{
                        nodeActionFunc_RightClickDBL(currentNode, cy, relations);
                    }catch(e){console.log(e);}
                }
                //removeNodeFromGraph(currentNode, cy);
            }
            CY_DBL_RIGHT_CLICK_LAST_NODE_CLICKED_UUID = clickedNode.data().id;
        });



        /* 
            __    __________________   ________    ____________ __
           / /   / ____/ ____/_  __/  / ____/ /   /  _/ ____/ //_/
          / /   / __/ / /_    / /    / /   / /    / // /   / ,<
         / /___/ /___/ __/   / /    / /___/ /____/ // /___/ /| |
        /_____/_____/_/     /_/     \____/_____/___/\____/_/ |_|

        */

        // On link la fonction de onclick pour les nouveaux noeuds
        addedNode.on('click', function(e){
            var currentDate = new Date();

            var timeSinceLastClick_in_sec = (currentDate - CY_DBL_LEFT_CLICK_DATE)/1000;
            CY_DBL_LEFT_CLICK_DATE = currentDate;

            //console.log(timeSinceLastClick_in_sec +"s since last click");

            var clickedNode = e.target;

            var currentNode = relations[clickedNode.data().id];

            // FUNCTION EVT
            if(nodeActionFunc_LeftClick!=null){
                try{
                    /*console.log("dbl : " );
                    console.log(currentNode);*/
                    nodeActionFunc_LeftClick(currentNode, cy, relations);
                }catch(e){console.log(e);}
            }

            // Si on a bien double clické sur le meme noeud en moins de 20ms
            if(timeSinceLastClick_in_sec < CY_DBL_CLICK_TIME_LIMIT && CY_DBL_LEFT_CLICK_LAST_NODE_CLICKED_UUID.localeCompare(clickedNode.data().id) == 0){

                // FUNCTION EVT
                if(nodeActionFunc_LeftClickDBL!=null){
                    try{
                        /*console.log("dbl : " );
                        console.log(currentNode);*/
                        nodeActionFunc_LeftClickDBL(currentNode, cy, relations);
                    }catch(e){console.log(e);}
                }

                // On a click sur un noeud 
                //     - si au moins un des voisin n'est pas encore dans le graphe, on ajoute tous les voisin non deja presents
                //     - si tous les voisins sont deja dans le graphe, on les supprime tous


                
            }
            CY_DBL_LEFT_CLICK_LAST_NODE_CLICKED_UUID = clickedNode.data().id;
        });

        cy.add(tableEdgeUptoAdd);
        cy.add(tableEdgeDowntoAdd);

        for(var neUp_Idx=0; neUp_Idx<tableEdgeUptoAdd.length; neUp_Idx++){
            updateNodeStatusRelations(relations[tableEdgeUptoAdd[neUp_Idx].data.target], cy, relations);
        }
        for(var neDown_Idx=0; neDown_Idx<tableEdgeDowntoAdd.length; neDown_Idx++){
            updateNodeStatusRelations(relations[tableEdgeDowntoAdd[neDown_Idx].data.source], cy, relations);
        }
        updateNodeStatusRelations(node, cy, relations);

        return true;
    }
    return false;
}


export function cyGetFIRPColor(firpText){
    var color = "#444444";
    if(firpText.toLowerCase().endsWith("feature")){
        color = "#fffb00";
    }else if(firpText.toLowerCase().endsWith("interpretation")){
        color = "#31c046";
    }else if(firpText.toLowerCase().endsWith("representation")){
        color = "#5fb6cd";
    }else if(firpText.toLowerCase().endsWith("property")){
        color = "#ff6666";
    } 
    return color;
}
export function cyGetNodeColor(node){
    return cyGetFIRPColor(node.firp);
}

export function cyGetNodeNamePrefix(node){
    var prefix = "";
    if(node.firp.endsWith("Feature")){
        prefix = "[F]_";
    }else if(node.firp.endsWith("Interpretation")){
        prefix = "[I]_";
    }else if(node.firp.endsWith("Representation")){
        prefix = "[R]_";
    }else if(node.firp.endsWith("Property")){
        prefix = "[P]_";
    } 
    return prefix;
}

export function cyGetCenterX(cy){
    var fact = 1.0;
    if(cy.zoom()>0.000001){
        fact = 1.0 / cy.zoom();
    }
    return (cy.container().clientWidth  * 0.5 - cy.pan().x) * fact;
}

export function cyGetCenterY(cy){
    var fact = 1.0;
    if(cy.zoom()>0.000001){
        fact = 1.0 / cy.zoom();
    }
    return (cy.container().clientHeight * 0.5 - cy.pan().y) * fact;
}

var CONST_CY = null;
var CONST_CY_RELATIONS = null;
var CONST_CY_CHECKBOX = {};


export function showOnlyOpened(){
    if(CONST_CY_RELATIONS == null || CONST_CY_RELATIONS.length == 0){
        start_graph().then(showOnlyOpened);    // Un peu sale comme solution...
    }else{
        removeAllNodes();
        var uuids = getOpenObjectsUuid();
        for(var i=0; i<uuids.length; i++){
            toggleNodeFromUUID(uuids[i]);
            //console.log(uuids[i])
        }
        reload_graph();
    }
}

export function updateGraphStyle(){
    if(CONST_CY != null){
        var labelEdgeValue = "";
        if(document.getElementById("cbCyEdgeLabel").checked){
            labelEdgeValue = "data(name)";
        }
        var enableEdgeArrow = document.getElementById("cbCyEdgeArrow").checked;
        
        CONST_CY.style( cyGetStyle(null, labelEdgeValue, enableEdgeArrow));
    }
}

export function removeAllNodes(){
    if(tryStartTaskCY()){
        try{
            if(CONST_CY != null && CONST_CY_RELATIONS != null){
                CONST_CY.edges().remove();
                CONST_CY.nodes().remove();
                updateCheckableFromGraph(CONST_CY, CONST_CY_RELATIONS);
                const roller = document.getElementById("rolling_resqmlGraphView");
            }
        }catch(exception){console.log(exception);}
        endTaskCY();
    }
    
}

export function toggleNodeFromUUID(nodeToggleInput){
    if(CONST_CY_RELATIONS!=null && CONST_CY!=null){
        if(CONST_CY_RELATIONS[nodeToggleInput.replace(/\s/g, '')]!=null){
            cyToggleNode(CONST_CY_RELATIONS[nodeToggleInput.replace(/\s/g, '')], CONST_CY, CONST_CY_RELATIONS, null, leftDblClickOnNodeAction, null, rightDblClickOnNodeAction);
        }else{
            // Si nodeToggleInput n'est pas un uuid dans les relations, on regarde si l'utilisateur n'aurait pas saisi
            // plutot le nom de l'objet ou un uuid partiel

            var matchedObjectsUUID = [];
            for(var uuid in CONST_CY_RELATIONS){
                if(uuid.toLowerCase().includes(nodeToggleInput.toLowerCase()) 
                    || CONST_CY_RELATIONS[uuid].title.toLowerCase().includes(nodeToggleInput.toLowerCase())){
                    matchedObjectsUUID.push(uuid);
                }
            }
            var matchedNotIn = matchedObjectsUUID.filter( matchedUUID =>  CONST_CY.nodes('[id = "' + matchedUUID + '"]').length <= 0);
            //console.log(matchedNotIn);
            if(matchedNotIn.length>0){ // Si pas tous la on ajoute les manquants
                for(var notIn_Idx=0; notIn_Idx<matchedNotIn.length; notIn_Idx++){
                    addNodeToGraph(CONST_CY_RELATIONS[matchedNotIn[notIn_Idx]], CONST_CY, CONST_CY_RELATIONS, null, leftDblClickOnNodeAction, null, rightDblClickOnNodeAction);
                }
            }else{ // On remove
                for(var match_Idx=0; match_Idx<matchedObjectsUUID.length; match_Idx++){
                    removeNodeFromGraph(CONST_CY_RELATIONS[matchedObjectsUUID[match_Idx]], CONST_CY, CONST_CY_RELATIONS);
                }
            }
        }
        updateCheckableFromGraph(CONST_CY, CONST_CY_RELATIONS);
    }
}

export function leftDblClickOnNodeAction(currentNode, cy, relations){
    var mode = document.querySelector('input[name=cy-radio-mode]:checked').value;
    if("adding".localeCompare(mode)==0){
        openRelations(currentNode, cy, relations);
    }else if("removing".localeCompare(mode)==0){
        removeNodeFromGraph(currentNode.uuid, cy, relations);
    }
}


export function rightDblClickOnNodeAction(currentNode, cy, relations){
    openResqmlObjectContentByUUID(currentNode.uuid);
}

export function openRelations(currentNode, cy, relations){
    var notInNeighborsUp   = currentNode.relationUp.filter(  neighborRels =>  cy.nodes('[id = "' + neighborRels.uuid + '"]').length <= 0);
    var notInNeighborsDown = currentNode.relationDown.filter(neighborRels =>  cy.nodes('[id = "' + neighborRels.uuid + '"]').length <= 0);

    if(notInNeighborsUp.length==0 && notInNeighborsDown==0){
        // Tout est deja dans le graphe, on supprime tous les voisins
        for (var i = currentNode.relationUp.length - 1; i >= 0; i--) {
            removeNodeFromGraph(relations[currentNode.relationUp[i].uuid].uuid, cy, relations);
        }
        for (var i = currentNode.relationDown.length - 1; i >= 0; i--) {
            removeNodeFromGraph(relations[currentNode.relationDown[i].uuid].uuid, cy, relations);
        }
    }else{
        // Au moins un des voisins n'est pas present, on l'ajoute
        for (var i = notInNeighborsUp.length - 1; i >= 0; i--) {
            addNodeToGraph(relations[notInNeighborsUp[i].uuid], cy, relations, null, leftDblClickOnNodeAction, null, rightDblClickOnNodeAction);
        }
        for (var i = notInNeighborsDown.length - 1; i >= 0; i--) {
            addNodeToGraph(relations[notInNeighborsDown[i].uuid], cy, relations, null, leftDblClickOnNodeAction, null, rightDblClickOnNodeAction);
        }
    }
    updateCheckableFromGraph(cy, relations);
}

export function updateCheckableFromGraph(cy, relations){
    var subCheck = document.getElementById("graphElementChecked").querySelectorAll('input[name="' + "cy_graphChecker" + '"]');
    for(var subCheckIdx=0; subCheckIdx<subCheck.length; subCheckIdx++){
        if("checkbox".localeCompare(subCheck[subCheckIdx].type+"")==0){
            if(cy.nodes('[id = "'+subCheck[subCheckIdx].value+'"]').length<=0){
                subCheck[subCheckIdx].checked = false;
            }else{
                subCheck[subCheckIdx].checked = true;
            }
        }
    }
    updateTAGcheckable(relations);
}

export function updateTAGcheckable(relations){
    var firpTags = [];
    for(var uuid in relations){
        var elt = relations[uuid];
        if(!firpTags.includes(elt.firp)){
            firpTags.push(elt.firp);
        }
    }

    for(var tagIdx=0; tagIdx<firpTags.length; tagIdx++){
        var tag = firpTags[tagIdx];
        var tagChecker = document.getElementById("graphElementChecked").querySelector('input[name="' + tag + "_" + "cy_graphChecker" + '"]');
        var subTagCheck = tagChecker.parentNode.parentNode.querySelectorAll('input[name="' + "cy_graphChecker" + '"]');
        var nbNotChecked = 0;
        var nbChecked = 0;
        for(var subTagCheckIdx=0; subTagCheckIdx<subTagCheck.length; subTagCheckIdx++){
            if("checkbox".localeCompare(subTagCheck[subTagCheckIdx].type+"")==0){
                if("false".localeCompare(subTagCheck[subTagCheckIdx].checked)==0){
                    nbNotChecked++;
                }else {
                    nbChecked++;
                }
            } 
        }
        //console.log('nb not checked ' + tag + " : " + nbNotChecked );
        tagChecker.checked = (nbNotChecked==0);
        if(nbChecked>0){
            tagChecker.nextSibling.style.fontWeight = 'bold';
        }else{
            tagChecker.nextSibling.style.fontWeight = '';
        }
    }
}

export function tryStartTaskCY(){
    const roller = document.getElementById("rolling_resqmlGraphView");
    if("none".localeCompare(roller.style.display)==0 || roller.style.display == null || roller.style.display == undefined){
        roller.style.display = "";
        return true;
    }
    return false;
}

export function endTaskCY(){
    document.getElementById("rolling_resqmlGraphView").style.display = "none";
}


export function start_graph(){

    // Si on est pas deja en train de faire une action
    if(tryStartTaskCY()){
        return getJsonObjectFromServer("ResqmlEPCRelationship").then(
            relations =>{
                try{
                    CONST_CY_RELATIONS = relations;
                    
                    while(document.getElementById("graphElementChecked").firstChild){
                        document.getElementById("graphElementChecked").firstChild.remove();
                    }
                    document.getElementById("graphElementChecked").appendChild(
                        createCheckableFIRPlist(relations, "cy_graphChecker"));
                    
                    if(CONST_CY == null){
                        CONST_CY = initiateGraphView(document.getElementById("cyGrapher"));
                    }else{
                        // TODO chercher tous les noeuds et arc et sotcker les id dans une liste pour les ajouter de nouveau apres suppressions!
                        // ca permet de tout remettre a jour
                        var previousNodesUUID = CONST_CY.nodes().map(node => node.data("id") );

                        for(var prevN_Idx=0; prevN_Idx<previousNodesUUID.length; prevN_Idx++){
                            if(relations[previousNodesUUID[prevN_Idx]] != undefined){
                                updateNodeData(relations[previousNodesUUID[prevN_Idx]], CONST_CY, relations, null, leftDblClickOnNodeAction, null, rightDblClickOnNodeAction);
                            }else{
                                removeNodeFromGraph(previousNodesUUID[prevN_Idx], CONST_CY, relations);
                            }
                        }
                    }
                    updateCheckableFromGraph(CONST_CY, relations);
                    updateGraphStyle();
                }catch(e){
                    console.log(e);
                }
                endTaskCY();
            }
        );
        /*return sendGetURL_Promise("ResqmlEPCRelationship").then(
                function(relsJson){
                    try{
                        const relations = JSON.parse(relsJson);
                        CONST_CY_RELATIONS = relations;
                        
                        while(document.getElementById("graphElementChecked").firstChild){
                            document.getElementById("graphElementChecked").firstChild.remove();
                        }
                        document.getElementById("graphElementChecked").appendChild(
                            createCheckableFIRPlist(relations, "cy_graphChecker"));

                        
                        if(CONST_CY == null){
                            CONST_CY = initiateGraphView(document.getElementById("cyGrapher"));
                        }else{
                            // TODO chercher tous les noeuds et arc et sotcker les id dans une liste pour les ajouter de nouveau apres suppressions!
                            // ca permet de tout remettre a jour
                            var previousNodesUUID = CONST_CY.nodes().map(node => node.data("id") );

                            for(var prevN_Idx=0; prevN_Idx<previousNodesUUID.length; prevN_Idx++){
                                if(relations[previousNodesUUID[prevN_Idx]] != undefined){
                                    updateNodeData(relations[previousNodesUUID[prevN_Idx]], CONST_CY, relations, null, leftDblClickOnNodeAction, null, rightDblClickOnNodeAction);
                                }else{
                                    removeNodeFromGraph(previousNodesUUID[prevN_Idx], CONST_CY, relations);
                                }
                            }
                        }
                        updateCheckableFromGraph(CONST_CY, relations);
                        updateGraphStyle();
                    }catch(e){
                        console.log(e);
                    }
                    endTaskCY();
                }
            );*/
    }else{
        return null;
    }
    
} 


export function reload_graph(){
    if(CONST_CY!=null){
        var lay = CONST_CY.layout(getFcoseLayout());
        lay.run(); 
    }else{
        console.log("null cy");
    }
}

export function createCheckableFIRPlist(relations, checkableName){
    var listFIRP = document.createElement("div");
    
    const CONST_CY_CHECKBOX = {};

    var mapFIRP = {"Feature" : [], "Interpretation" : [], "Representation" : [], "Property" : []};
    for(var uuid in relations){
        var elt = relations[uuid];
        if(mapFIRP[elt.firp] == null){
            mapFIRP[elt.firp] = [];
        }
        mapFIRP[elt.firp].push(elt);
        /*var checkbox = createCheck(elt.title + " [ " + elt.uuid + " ]", elt.uuid, checkableName, 
                            function(checked, value){
                                if(checked){
                                    addNodeToGraph(relations[value], CONST_CY, relations, null, leftDblClickOnNodeAction, null, rightDblClickOnNodeAction);
                                }else{
                                    removeNodeFromGraph(relations[value].uuid, CONST_CY, relations);
                                }
                                updateTAGcheckable(relations);
                            }, 
                            false
                        );
        checkbox.querySelectorAll('label').forEach( 
            function(element, index) {
                element.setAttribute("data-toggle", "tooltip");
                element.setAttribute("data-placement", "bottom");
                element.setAttribute("title", elt.type);
            });

        mapFIRP[elt.firp].push(checkbox);
        CONST_CY_CHECKBOX[uuid] = checkbox.querySelector('input[type="checkbox"]');*/
    }

    for(var firpTag in mapFIRP){
        mapFIRP[firpTag].sort((a,b) => a.title.localeCompare(b.title));
        mapFIRP[firpTag] = mapFIRP[firpTag].map(elt => 
            {
                var checkbox = createCheck(elt.title + " [ " + elt.uuid + " ]", elt.uuid, checkableName, 
                            function(checked, value){
                                if(checked){
                                    addNodeToGraph(relations[value], CONST_CY, relations, null, leftDblClickOnNodeAction, null, rightDblClickOnNodeAction);
                                }else{
                                    removeNodeFromGraph(relations[value].uuid, CONST_CY, relations);
                                }
                                updateTAGcheckable(relations);
                            }, 
                            false
                        );
                checkbox.querySelectorAll('label').forEach( 
                    function(element, index) {
                        element.setAttribute("data-toggle", "tooltip");
                        element.setAttribute("data-placement", "bottom");
                        element.setAttribute("title", elt.type);
                    });
                CONST_CY_CHECKBOX[uuid] = checkbox.querySelector('input[type="checkbox"]');
                return checkbox;
            });

    }

    // Maj de l'autocompletion de l'ajout de noeud via uuid : ne fonctionne pas !
    /*console.log($('#cyNodeUuidToAdd'));
    $('#cyNodeUuidToAdd').autocomplete({
        source : Object.keys(CONST_CY_CHECKBOX)
    });*/


    for(var firpTAG in mapFIRP){
        //var firp_TAG = document.createElement("li");

        //const firp_TAP_subCheckList = document.createElement("ul");

        const subEltDiv = document.createElement("ul");

        var tagCheckbox = createCheck(firpTAG, "", firpTAG+"_"+checkableName, 
                            function(checked, value){
                                var subCheck = subEltDiv.querySelectorAll('input[name="'+checkableName+'"]');
                                for(var subCheckIdx=0; subCheckIdx<subCheck.length; subCheckIdx++){
                                    if("checkbox".localeCompare(subCheck[subCheckIdx].type+"")==0){
                                        if(subCheck[subCheckIdx].checked != checked){
                                            subCheck[subCheckIdx].click();
                                        }
                                    }
                                }
                            }, 
                            false
                        );

        var circleColored = document.createElement("span");
        circleColored.className = "fas fa-circle colorCircle";
        circleColored.style.color = cyGetFIRPColor(firpTAG);
        tagCheckbox.appendChild(circleColored);

        for(var subIdx in mapFIRP[firpTAG]){
            var sub_li = document.createElement("li");
            sub_li.appendChild(mapFIRP[firpTAG][subIdx]);
            subEltDiv.appendChild(sub_li);
        }

        listFIRP.appendChild(createCollapser(tagCheckbox, subEltDiv))
    }

    // On active les nouveaux tooltips
    $(document).ready(function(){
        $('[data-toggle="tooltip"]').tooltip();
    });

    return listFIRP;
}