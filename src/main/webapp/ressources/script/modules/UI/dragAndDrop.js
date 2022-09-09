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



export function geosiris_getElementPosition(elt) {
    var rect   = elt.getBoundingClientRect(),
    scrollLeft = window.pageXOffset || document.documentElement.scrollLeft,
    scrollTop  = window.pageYOffset || document.documentElement.scrollTop;
    return { x: rect.left + scrollLeft, y: rect.top + scrollTop, height : rect.height, width : rect.width };
}

export const geosiris_DndHandler = {

    draggedElement: null, // Propriété pointant vers l'élément en cours de déplacement

    applyDragEvents: function(element, dragEndFunc) {

        element.draggable = true;

        const dndHandler = this; // Cette variable est nécessaire pour que l'événement « dragstart » ci-dessous accède facilement au namespace « dndHandler »

        element.ondragstart = function(e) {
            if(e.target.className.includes("draggableElt") || e.target.parentNode.className.includes("draggableElt")){
                e.target.parentNode.className += " dragingElt";
                //console.log("inital e "); console.log(e);
                var targetElt = e.target;
                //console.log("draggable "); console.log(targetElt);
                if(!e.target.className.includes("draggableElt")){
                    targetElt = e.target.parentNode;
                    //console.log("change ");
                }
                dndHandler.draggedElement = targetElt; // On sauvegarde l'élément en cours de déplacement
                console.log("sending draggable "); console.log(targetElt);
                e.dataTransfer.setData('text/plain', ''); // Nécessaire pour Firefox
            }
            //console.log(e.target);
            //e.stopPropagation();
        };

        // Fonction appellee apre le ondrop dans le container dans lequel l'element est depose
        element.ondragend = function(e){
            dragEndFunc();
            e.target.parentNode.className = e.target.className.replace(/dragingElt/g, "");
            e.stopPropagation();
        };

    },

    applyDropEvents: function(dropper) {
        const dndHandler = this; // Cette variable est nécessaire pour que l'événement « drop » ci-dessous accède facilement au namespace « dndHandler »
        const constDrop = dropper;

        dropper.ondragover = function(e) {
            e.preventDefault(); // On autorise le drop d'éléments
            var draggedElement = dndHandler.draggedElement; 
            if( (constDrop === draggedElement.parentNode || constDrop === draggedElement.parentNode.parentNode) // si on veut drop dans un parent on active sinon non
                && (e.target.parentNode === draggedElement.parentNode || e.target.parentNode === draggedElement.parentNode.parentNode)
                && !constDrop.className.includes("drop_hover")
                ){
                /*console.log(dndHandler.draggedElement.textContent)
                console.log("===")
                console.log(constDrop.textContent)
                console.log("-------")*/
                dropper.className += ' drop_hover'; // Et on applique le style adéquat à notre zone de drop quand un élément la survole
                dndHandler.displace(e, constDrop);
                
            }
        };

        dropper.ondragleave = function() {
            dropper.className = dropper.className.replace("drop_hover", ""); // On revient au style de base lorsque l'élément quitte la zone de drop
        };

        dropper.ondrop = function(e) {
            var draggedElement = dndHandler.draggedElement; 
            if(        (constDrop === draggedElement.parentNode || constDrop === draggedElement.parentNode.parentNode) // si on veut drop dans un parent on active sinon non
                && (e.target.parentNode === draggedElement.parentNode || e.target.parentNode === draggedElement.parentNode.parentNode)
                ){
                dndHandler.displace(e, constDrop);            
                dropper.className = dropper.className.replace("drop_hover", ""); // Application du style par défaut
            }
        };



    },

    displace : function(e, dropper){
        const dndHandler = this;

        var target = e.target,
                draggedElement = dndHandler.draggedElement; // Récupération de l'élément concerné
        /*console.log("#### DROPER ###");
        console.log(target.textContent);
        console.log(dropper.textContent);
        console.log(draggedElement.textContent);
        console.log("#########");*/
        if(dropper === draggedElement.parentNode || dropper === draggedElement.parentNode.parentNode){
            // On drop seulement dans le conteneur parent
            while (target.className.indexOf('dropper') == -1) { // Cette boucle permet de remonter jusqu'à la zone de drop parente
                target = target.parentNode;
        }

            /*
                ___  ___________________   ________________  _   __
               /   |/_  __/_  __/ ____/ | / /_  __/  _/ __ \/ | / /
              / /| | / /   / / / __/ /  |/ / / /  / // / / /  |/ /
             / ___ |/ /   / / / /___/ /|  / / / _/ // /_/ / /|  /
            /_/  |_/_/   /_/ /_____/_/ |_/ /_/ /___/\____/_/ |_/
            */
            // ATTENTION ! fonctionne uniquement pour les dropper verticaux !
            var inserted = false;
            //console.log(target)
            for(var childIdx=0; childIdx<target.childNodes.length; childIdx++){
                var eltBBox = geosiris_getElementPosition(target.childNodes[childIdx]);
                if(e.clientY<eltBBox.y + eltBBox.height*0.5 && !(target.childNodes[childIdx]===draggedElement)){
                    target.insertBefore(draggedElement, target.childNodes[childIdx]);
                    inserted = true;
                    break;
                }
            }
            if(!inserted){
                target.appendChild(draggedElement);
            }
        }else{
            console.log("bad drop : ");console.log(draggedElement);
            console.log("into : ");console.log(dropper);
        }
    }

};