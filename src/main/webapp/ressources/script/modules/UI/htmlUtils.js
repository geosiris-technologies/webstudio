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

import {openResqmlObjectContentByUUID} from "../main.js"
import {deleteResqmlObject} from "../requests/uiRequest.js"


export function getPopoverOpenUUID(uuidToOpen, posX, posY){
    const content = document.createElement("span");
    content.appendChild(document.createTextNode("Open"));
    content.style.left = '';
    content.onclick = function(){
        openResqmlObjectContentByUUID(uuidToOpen);
        content.remove();
    }
    return content;
}

export function createDeleteButton(className, title){
    const deleteBut = document.createElement("i");
    deleteBut.className = "far fa-trash-alt " + (className!=null? className:"");
    deleteBut.title = title;
    deleteBut.style.cursor = "pointer";
    deleteBut.onmouseover= function(){deleteBut.className = deleteBut.className.replace(/far/g,'fas');}
    deleteBut.onmouseout = function(){deleteBut.className = deleteBut.className.replace(/fas/g,'far');}
    return deleteBut;
}

export function createDropDownDivider(){
    var divider = document.createElement("div");
    divider.className = "dropdown-divider";
    return divider;
}

export function createHoverableHtmlContent(elt, htmlContent){
    var divContent = document.createElement("div");
    divContent.className = "typeCommentContainer";
    divContent.innerHTML = htmlContent;
    elt.onclick = function(){
        if (divContent.style.display === "none") {
            divContent.style.display = "block";
            divContent.style.top = elt.offsetTop;
            divContent.style.left = elt.offsetLeft;
        }else{
            divContent.style.display = "none";
        }
    }

    divContent.onmouseleave = function(){
        divContent.style.display = "none";
    }
    divContent.style.display = "none";
    return divContent;
}

export function createEltAndSubs(    rootType, 
    subsType, 
    subsValueTable, 
    subsClassesTable, 
    subsOnclickFuncTable){
    var root = document.createElement(rootType);
    // console.log("createElt and sub : ");
    // console.log(subsValueTable);

    for(var subIdx=0; subIdx < subsValueTable.length; subIdx++){
        var sub = document.createElement(subsType);

        var subValue = subsValueTable[subIdx];
        if(typeof subValue == 'string'){ // not a node
            subValue = document.createTextNode(subValue);
        }else{
            // console.log("Subvalue : '" + subValue + "'");
            // console.log("Type" + type(subValue));
        }

        /*console.log('class name '); console.log(subsClassesTable);*/
        // recherche d'une class
        var subClass = "";
        if(subsClassesTable!=null && subsClassesTable.length==subsValueTable.length){
            // if as many onclick function as subs, each has its own
            subClass = subsClassesTable[subIdx];
        }else if(subsClassesTable!=null && subsClassesTable.length==1){
            // if only one onclick funct given, it is used for all
            subClass = subsClassesTable[0];
        }
        sub.className = subClass;

        // recherche d'un onclick
        var onclickFunc = null
        if(subsOnclickFuncTable!=null && subsOnclickFuncTable.length==subsValueTable.length){
            // if as many onclick function as subs, each has its own
            onclickFunc = subsOnclickFuncTable[subIdx];
        }else if(subsOnclickFuncTable!=null && subsOnclickFuncTable.length==1){
            // if only one onclick funct given, it is used for all
            onclickFunc = subsOnclickFuncTable[0];
        }

        // if an onclick func has been found
        if(onclickFunc != null){
            var subClickable = document.createElement("span");
            subClickable.style.cursor = "pointer";
            subClickable.onclick = onclickFunc;
            subClickable.appendChild(subValue);    
            subValue = subClickable;
        }

        if(subValue!=null){
            // console.log(">>> ");
            // console.log(subValue);
            // console.log(typeof(subValue));
            sub.appendChild(subValue);
        }

        root.appendChild(sub);
    }
    return root;
}

// On cherche recursivement le premier noeud de type text : 
// si le premier fils de 'elt' n'est pas un text, on cherche dans ses fils,
// si on ne trouve pas de text recursivement, on cherche pour les fils suivants
export function findFirstTextNodeValue(elt){
    if(elt!=null){
        if(elt.nodeName.toLowerCase()=="#text"){
            //console.log('FOUND' ); console.log(elt); console.log(elt.nodeValue);
            return elt.nodeValue;
        }else if(elt.childNodes!=null){
            //console.log('not a text' ); console.log(elt);
            for(var chi=0; chi< elt.childNodes.length; chi++){
                //console.log('search for '); console.log(elt.childNodes[chi]);
                var firstTextNodeFound = findFirstTextNodeValue(elt.childNodes[chi]);
                if(firstTextNodeFound!=null)
                    return firstTextNodeFound;
                
            }
        }else{
            //console.log("null childNodes for "); console.log(elt);
        }
    }
    return null;
}

export function createRadio(labelTxt, value, radioName, checkableOnclickFunc_With_Checked_and_Value_as_Param, checked){

    var radio = document.createElement("input");
    radio.type = "radio";
    radio.name = radioName;
    radio.value = value;
    radio.className = "form-check-input";
    if(checked!=null){
        radio.checked = checked;
    }

    var result = radio;
    if(checkableOnclickFunc_With_Checked_and_Value_as_Param!=null){
        radio.onclick = function(){
            checkableOnclickFunc_With_Checked_and_Value_as_Param(radio.checked, value);
        }
    }

    if(labelTxt!= null && labelTxt.length>0){
        // Si a un label alors on l'ajoute
        var label = document.createElement("label");
        label.appendChild(document.createTextNode(labelTxt));
        label.className = "form-check-label";

        var divRadio = document.createElement("div");

        divRadio.appendChild(radio);
        divRadio.appendChild(label);
        divRadio.className = "form-check";

        result = divRadio;
    }

    return result;
}

export function createCheck(labelTxt, value, checkName, checkableOnclickFunc_With_Checked_and_Value_as_Param, checked){

    const ENABLE_CUSTOM_CHECK_WITH_EMPTY_LABEL = true;

    var dateNow = Date.now();

    var check = document.createElement("input");

    var ua = navigator.userAgent;
    check.type = "checkbox";
    check.name = checkName;
    check.checked = checked;
    check.value = value;
    if(checkableOnclickFunc_With_Checked_and_Value_as_Param!=null){
        check.onclick = function(){
            checkableOnclickFunc_With_Checked_and_Value_as_Param(check.checked, value);
        }
    }
    var result = check;

    if(ENABLE_CUSTOM_CHECK_WITH_EMPTY_LABEL || (labelTxt!= null && labelTxt.length>0) ){
        check.className = "custom-control-input";
        // Si a un label alors on l'ajoute
        var label = document.createElement("label");
        label.appendChild(document.createTextNode(labelTxt));
        label.className = "custom-control-label";

        var divCheck = document.createElement("div");
        divCheck.className = "custom-control custom-checkbox";

        var countIdUnique = 0;
        var valueRefactored = value.replace(/[^0-9a-z-A-Z]/g,'');
        while(document.getElementById(checkName+"-"+valueRefactored+"-"+dateNow+"-"+countIdUnique)!=null){
            countIdUnique++;
        }
        check.id = checkName+"-"+valueRefactored+"-"+dateNow+"-"+countIdUnique;
        label.setAttribute("for", check.id);

        divCheck.appendChild(check);
        divCheck.appendChild(label);

        result = divCheck;
    }

    return result;
}


export function createSplitter(idFirst, idSecond, fistPercent, secPercent, orientation, minS, gutS){
    if(orientation == null){
        orientation = "";
    }

    Split([idFirst, idSecond], {
        gutterStyle: function (dimension, gutterSize) {

            if(orientation == "vertical"){
                return     {
                    'height':  gutterSize + "px"
                };
            }else{
                return     {
                    'width':  gutterSize + "px"
                };
            }
        },
        direction: orientation,
        sizes: [fistPercent, secPercent],
        minSize: minS,
        gutterSize: gutS,
    });
}
export function createCollapser(eltHead, eltCollapsable){
    eltHead.style.display = 'inline';


    const spanArrow = document.createElement("span");
    spanArrow.style.cursor = "pointer";
    spanArrow.className    = "resqmlCollapse fas fa-chevron-right";
    spanArrow.style.marginRight = '10px';

    spanArrow.onclick = 
    function(){
        if(eltCollapsable != null){
            if(spanArrow.className.includes("fa-chevron-right")){
                spanArrow.className = spanArrow.className.replace("fa-chevron-right", "fa-chevron-down");
                eltCollapsable.style.display   = "";
            }else{
                spanArrow.className = spanArrow.className.replace("fa-chevron-down", "fa-chevron-right");
                eltCollapsable.style.display   = "none";
            }
        }
    };

    var divCollapser = document.createElement("div");
    divCollapser.appendChild(spanArrow);
    divCollapser.appendChild(eltHead);
    divCollapser.appendChild(eltCollapsable);

    eltCollapsable.style.display = 'none';

    return divCollapser;
}

export function geosiris_createEditableSelector(listValue, selectedValue, isEditable){
    const selectElt = document.createElement("select");

    if(listValue!=null){
        var valueFound = false;
        for(var val_Idx=0; val_Idx<listValue.length; val_Idx++){
            var currentEnumVal = listValue[val_Idx];
            
            var optionElt = document.createElement("option");

            var currentValue = currentEnumVal;
            if(typeof(currentEnumVal) != "string" && currentEnumVal.length>0){    // Si on tombe sur une pair '(enumConstant, valeurEnString)'
                currentValue = currentEnumVal[1];
                //optionElt.appendChild(document.createTextNode(""+currentEnumVal[1]));
            }/*else{                                                                // Si on est sur un string
                optionElt.appendChild(document.createTextNode(""+currentValue));
            }*/
            optionElt.appendChild(document.createTextNode(""+currentValue));

            optionElt.value = currentValue;
            if(selectedValue!=null && (currentValue+"").localeCompare(selectedValue+"")==0){
                valueFound = true;
                optionElt.selected = "selected";
            }

            selectElt.appendChild(optionElt);
        }
        if(selectedValue!=null && !valueFound){
            var optionElt = document.createElement("option");
            optionElt.appendChild(document.createTextNode(""+selectedValue));
            optionElt.value = selectedValue;
            optionElt.selected = "selected";
            selectElt.appendChild(optionElt);
        }
    }
    if(isEditable){
        const optionAddElt = document.createElement("option");
        optionAddElt.className += " addedOptionInSelect"
        optionAddElt.appendChild(document.createTextNode("(Add Element)"));
        optionAddElt.value = "________choice________";
        selectElt.appendChild(optionAddElt);
        selectElt.onchange = function(){
            if(selectElt.value == optionAddElt.value) {
                var choix = document.createElement('input');
                choix.onkeypress  = function(event){
                    onEnterPressed(event, function() {
                        var option = document.createElement('option');
                        option.innerHTML = choix.value;
                        option.value = choix.value;
                        choix.parentNode.replaceChild(selectElt, choix);
                        selectElt.insertBefore(option, selectElt.firstChild);
                        selectElt.selectedIndex = 0;
                        option.className = " addedOptionInSelect";
                    });
                }
                //console.log(choix);
                choix.addEventListener("focusout", function(){
                    var option = document.createElement('option');
                    option.innerHTML = choix.value;
                    option.value = choix.value;
                    choix.parentNode.replaceChild(selectElt, choix);
                    selectElt.insertBefore(option, selectElt.firstChild);
                    selectElt.selectedIndex = 0;
                    option.className = " addedOptionInSelect";
                });
                selectElt.parentNode.replaceChild(choix, selectElt);
            }
        };
    }

    return selectElt;
}

export function createSelect_selector(optionList_str, targetList_htmlElt, targetDiv){

    if(targetDiv == null){
        targetDiv = document.createElement("div");
    }
    
    container = document.createElement("div");
    const selector = document.createElement("select");
    const htmlTargets = targetDiv;
    container.appendChild(selector)
    container.appendChild(htmlTargets)

    selector.className = "form-control";

    const targets = targetList_htmlElt;
    selector.onchange = function(e, x){
        const idx = selector.selectedIndex
        while (htmlTargets.firstChild) {htmlTargets.removeChild(htmlTargets.firstChild);}
        htmlTargets.appendChild(targets[idx])
    }
    optionList_str.forEach(
        function(element, index, array) {
            option = document.createElement("option")
            option.appendChild(document.createTextNode(element))
            selector.appendChild(option)
        }
        );


    if(optionList_str.length>0){
        htmlTargets.appendChild(targets[0]);
        htmlTargets.appendChild(targets[0]);
    }

    return container;

}

export function createDropDownButton(htmlEltMenuList, id){
    var rootDiv = document.createElement("div");
    rootDiv.className = "dropdown btn-group";

/*    var button = document.createElement("button");
    button.className="btn btn-primary dropdown-toggle";*/
    const button = document.createElement("span");
    button.className="dropDownCreateSubAttributeButton fas fa-plus-square";
    button.onmouseover= function(){button.className = button.className.replace(/fas/g,'far');}
    button.onmouseout = function(){button.className = button.className.replace(/far/g,'fas');}
    //button.type="button";
    button.id = id;
    button.setAttribute("data-toggle", "dropdown");
    button.setAttribute("aria-haspopup", "true");
    button.setAttribute("aria-expanded", "false");
    //button.appendChild(document.createTextNode("+"));
    button.style.padding ="1px";
    button.style.fontSize ="small";

    rootDiv.appendChild(button);

    var divMenu = document.createElement("div");
    divMenu.className = "dropdown-menu";
    divMenu.setAttribute("aria-labelledby", id);


    for(var eltId=0; eltId<htmlEltMenuList.length; eltId++){
        const elt = htmlEltMenuList[eltId];
        elt.className += " dropdown-item";
        elt.style.cursor = "pointer";
        elt.style.fontSize ="small";
        divMenu.appendChild(elt);
    }

    rootDiv.appendChild(divMenu);

    return rootDiv;
}

export function createInputGroup(elt_array, bool_array_PutInDiv){
    var div_input_grp = document.createElement("div");
    div_input_grp.className = "input-group mb-3";

    var cpt = 0;
    for(const elt of elt_array){
        if(!bool_array_PutInDiv || bool_array_PutInDiv[cpt]){
            var div_grp = document.createElement("div");
            div_grp.appendChild(elt);
            div_input_grp.appendChild(div_grp);
            if(cpt==0){
                div_grp.className = "input-group-prepend";
            }else{
                div_grp.className = "input-group-append";
            }
        }else{ // si dans le tableau on a un false
            div_input_grp.appendChild(elt);
        }
        cpt++;
    }
    return div_input_grp;
}

export function createDeleteResqmlButton(dataResqml){
    var deleteBut = createDeleteButton("deleteButton", "Remove " + dataResqml["uuid"] + " element from epc");

    deleteBut.onclick = function(){deleteResqmlObject(    dataResqml["uuid"], 
                                                        dataResqml["type"], 
                                                        dataResqml["title"]);};
    return deleteBut;
}

//execute la fonction "func" lorsque la touche entrée est pressée
export function onEnterPressed(keyPressed, func){
    if(keyPressed.keyCode == 13){
        func.call(); 
    }else{
        // console.log("keypressed : " + keyPressed.keyCode);
    }
}

export function call_after_DOM_updated(fn){
    var intermediate = function () {window.requestAnimationFrame(fn)}
    window.requestAnimationFrame(intermediate)
}

export function sleep(milliseconds) {
    var start = new Date().getTime();
    for (var i = 0; i < 1e7; i++) {
        if ((new Date().getTime() - start) > milliseconds){
            break;
        }
    }
}

export function randomColor(){
    return "#" + Math.floor(Math.random()*16777215).toString(16);
}
