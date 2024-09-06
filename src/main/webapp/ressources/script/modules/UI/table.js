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

import {call_after_DOM_updated, createCheck, createEltAndSubs, createInputGroup, createRadio, findFirstTextNodeValue, onEnterPressed} from "./htmlUtils.js"
import {beginTask, endTask} from "./ui.js"
import {getObjectTableCellClass, CLASS_TABLE_FIXED} from "../common/variables.js"


export function geosiris_applyTableSort(divTable, dataTableEltIndices, dataCellClassPrefix){
    var tableBody = divTable.firstChild;
    while(tableBody!=null && !tableBody.nodeName.toLowerCase().includes("tbody")){
        tableBody = tableBody.nextSibling;
    }

    var tableTrSorted = [];
    if(divTable.geosiris_sortOrder){
        tableTrSorted = [].slice.call(tableBody.children).sort(
            function(a, b) {
                var aFoundCol = a.getElementsByClassName(divTable.geosiris_lastcolumnSortedName);
                var bFoundCol = b.getElementsByClassName(divTable.geosiris_lastcolumnSortedName);

                if(aFoundCol.length>0 && bFoundCol.length>0){
                    return compare( findFirstTextNodeValue(aFoundCol[0]), 
                        findFirstTextNodeValue(bFoundCol[0]), 
                        false);
                }
                return 0;
            });
    }else{
        tableTrSorted = [].slice.call(tableBody.children).sort(
            function(a, b) {
                var aFoundCol = a.getElementsByClassName(divTable.geosiris_lastcolumnSortedName);
                var bFoundCol = b.getElementsByClassName(divTable.geosiris_lastcolumnSortedName);
                if(aFoundCol.length>0 && bFoundCol.length>0){
                    return compare( findFirstTextNodeValue(bFoundCol[0]), 
                        findFirstTextNodeValue(aFoundCol[0]), 
                        false);
                }
                return 0;
            });
    }
    //console.log('sorted table ');console.log(tableTrSorted);
    for(var trLineIdx=0;  trLineIdx<tableTrSorted.length; trLineIdx++){
        var trLine = tableTrSorted[trLineIdx];
        trLine.remove();
        if(divTable.geosiris_tableFilterfunc==null 
            || divTable.geosiris_tableFilterfunc === undefined 
            || divTable.geosiris_tableFilterfunc(trLine)){
            tableBody.appendChild(trLine);                                                
    }
}
}

export function highlightTableLineFromTdText(tdClass, textTableToHighlight){
    var cellList = document.getElementsByClassName(tdClass);
    for(var ci=0; ci<cellList.length; ci++){
        var currentCell = cellList[ci];
        if(textTableToHighlight.includes(currentCell.textContent)){
            if(!currentCell.parentNode.className.includes("openedObject"))
                currentCell.parentNode.className += " openedObject";
        }else{
            currentCell.parentNode.className = currentCell.parentNode.className.replace(/openedObject/g,'');
        }
    }
}

export function highlightTableCellFromClass(cellClass, enable){
    //console.log("coloring cell "+ cellClass)
    Array.prototype.forEach.call(
        document.getElementsByClassName(cellClass),  
        function(element) {
            if(enable && !element.className.includes("openedObject"))
                element.className += " openedObject";
            else if(!enable)
                element.className = element.className.replace(/openedObject/g,'');
            
        });
}

export function copyOtherTableSortingParams(currentTable, otherTable, dataTableEltIndices){
    if(otherTable!=null){
        const dataCellClassPrefix = "colName_";    // Any modification here needs modification in @createTableFromData
        currentTable.geosiris_tableFilterfunc = otherTable.geosiris_tableFilterfunc;
        currentTable.geosiris_lastcolumnSortedName = otherTable.geosiris_lastcolumnSortedName;
        currentTable.geosiris_sortOrder = otherTable.geosiris_sortOrder;
        geosiris_applyTableSort(currentTable, dataTableEltIndices, dataCellClassPrefix);
    }
}


/*
 *    Fonction qui permet de creer un tableau en fonction d'une matrice dont chaque ligne est un tableau/objet et chaque (case du tableau)/(attribut) est une donnée
 */
export function createTableFromData(dataTable, dataTableEltIndices, dataTableHeader, funcClickMatrix, filterLineFunc, oldTable){
    const divTable = document.createElement("table");
    // ======== specific elements ===========//
    divTable.geosiris_tableFilterfunc = filterLineFunc;
    divTable.geosiris_lastcolumnSortedName = null;
    divTable.geosiris_sortOrder = 1;
    // ======== specific elements ===========//

    if(oldTable!=null){
        divTable.geosiris_tableFilterfunc = oldTable.geosiris_tableFilterfunc;
        divTable.geosiris_lastcolumnSortedName = oldTable.geosiris_lastcolumnSortedName;
        divTable.geosiris_sortOrder = oldTable.geosiris_sortOrder;
    }


    divTable.className = CLASS_TABLE_FIXED;

    const dataCellClassPrefix = "colName_"; // Any modification here needs modification in @copyOtherTableSortingParams

    var tableHeader = document.createElement("thead");
    divTable.appendChild(tableHeader);

    const tableBody = createTableFromData_body(dataTable, dataTableEltIndices, dataCellClassPrefix, funcClickMatrix);

    // Creation des fonctions de tri par colonne
    var tableHeaderFuncClick = [];
    for(var hi=0; hi<dataTableHeader.length; hi++){
        //const constHi = hi;
        const currentColumnName = dataCellClassPrefix + ( (dataTableEltIndices!=null && dataTableEltIndices.length>0) ? dataTableEltIndices[hi] :hi );

        tableHeaderFuncClick.push(    function(mousEvt){
            beginTask();
            call_after_DOM_updated(function () {
                var parentTH = mousEvt.target.parentNode;
                var lastChild = mousEvt.target;
                try{
                    while(parentTH.nodeName.toLowerCase() != "tr"){
                        lastChild = parentTH;
                        parentTH = parentTH.parentNode;
                    }
                }catch(searchThExcept){
                    parentTH = mousEvt.target.parentNode;
                    lastChild = mousEvt.target;
                }
                // On cherche l'index de la colonne courante (qui peut avoir changée par rapport a hi si le tableau a ete transforme)
                //const currentColIdx = Array.prototype.indexOf.call(parentTH.children, lastChild);
                // on effectue un tri sur le contenu des cellules de tableau et non sur les data.
                // Cela permet de trier egalement les eventuel ajouts de colonnes (checkbox/radiobouton etc.) dans le tableau

                if(divTable.geosiris_lastcolumnSortedName == currentColumnName){
                    divTable.geosiris_sortOrder = (divTable.geosiris_sortOrder + 1) % 2;
                }
                divTable.geosiris_lastcolumnSortedName = currentColumnName;
                geosiris_applyTableSort(divTable, dataTableEltIndices, dataCellClassPrefix);

                endTask();
            });
        });
    }


    tableHeader.appendChild(createEltAndSubs("tr", "th", dataTableHeader, [], tableHeaderFuncClick));

    divTable.appendChild(tableBody);
    geosiris_applyTableSort(divTable, dataTableEltIndices, dataCellClassPrefix);
    
    return divTable;
}

export function transformTab_AddColumn(tableElt, headerContent, columnIdx, cellsContent, colTdClassName, colThClassName){
    /*console.log("tranforming table ")
        console.log("type : " + typeof(cellsContent));
        console.log("type2 : " + typeof(function(input){return null;}));*/

        try{
            var head = tableElt.getElementsByTagName("thead")[0];
            var tableHeaderLines = head.getElementsByTagName("tr")[0];
            var ths = tableHeaderLines.getElementsByTagName("th");

            var th = document.createElement("th");
            if(colThClassName!=null){
                th.className = colThClassName;
            }
            if(typeof(headerContent) === "string"){
                th.appendChild(document.createTextNode(headerContent));
            }else{
                th.appendChild(headerContent);
            }
            if(ths.length<=columnIdx){
                tableHeaderLines.prepend(th);
            }else{
                tableHeaderLines.insertBefore(th, ths[columnIdx])
            }
        }catch(exceptHeader){console.log(exceptHeader);}

        var body = tableElt.getElementsByTagName("tbody")[0];

        var tableLines = body.getElementsByTagName("tr");
        for(var lineIdx=0; lineIdx < tableLines.length; lineIdx++){
            var currentLine = tableLines[lineIdx];
            var tds = currentLine.getElementsByTagName("td");

            var td = document.createElement("td");

            if(colTdClassName!=null){
                td.className = colTdClassName;
            }

            td.appendChild(cellsContent[lineIdx]);

            if(tds.length<=columnIdx){
                currentLine.prepend(td);
            }else{
                currentLine.insertBefore(td, tds[columnIdx])
            }
        }
    }

export function createTableFromData_body(dataTable, dataTableEltIndices, dataCellClassPrefix, funcClickMatrix){
    var tableBody = document.createElement("tbody");

    //    console.log(dataTable);
    var tableCellFuncClick = null;

    var uuid_idx = dataTableEltIndices.findIndex((index) => index.toLowerCase().includes("uuid"));


    for(var lineIdx=0; lineIdx<dataTable.length; lineIdx++){
        var tableValue = dataTable[lineIdx];
        var tableFuncClick = []
        if(funcClickMatrix!=null && funcClickMatrix.length>lineIdx){
            tableCellFuncClick = funcClickMatrix[lineIdx];
        }

        if(dataTable[lineIdx].length == null && dataTableEltIndices != null && dataTableEltIndices.length > 0){
                // Si la case n'est pas un tableau on cherche des attributs par leur noms
                tableValue = [];
                for(var attribIdx=0; attribIdx<dataTableEltIndices.length; attribIdx++){
                    tableValue.push(dataTable[lineIdx][dataTableEltIndices[attribIdx]]);
                }
            }


            //console.log("table Value is : " );console.log(tableValue);
            var line_tr = null;
            if(typeof(tableValue) == "string"){
                line_tr = createEltAndSubs("tr", "td", [tableValue], 
                    dataTableEltIndices!=null?dataTableEltIndices.map(x => dataCellClassPrefix + x) : [], 
                    tableCellFuncClick);
            }else{
                line_tr = createEltAndSubs("tr", "td", tableValue, 
                    dataTableEltIndices!=null?dataTableEltIndices.map(x => dataCellClassPrefix + x) : [], 
                    tableCellFuncClick);
            }

            var second_class = " ";
            if(uuid_idx>0){
                // Si il y a un uuid on ajoute l'index
                second_class = " " + getObjectTableCellClass(tableValue[uuid_idx])
            }
            tableBody.appendChild(line_tr);
            line_tr.className += " " + second_class;
            
        }
        return tableBody
}

export function transformTabToFormRadio(htmlTable, valuesForLines, checkableName){
    var header = htmlTable.getElementsByTagName("thead")[0];

    var tableHeaderLine = header.getElementsByTagName("tr")[0];
    tableHeaderLine.prepend(document.createElement("th"));

    var body = htmlTable.getElementsByTagName("tbody")[0];

    var tableLines = body.getElementsByTagName("tr");
    for(var lineIdx=0; lineIdx < tableLines.length; lineIdx++){
        var td = document.createElement("td");
        td.appendChild(createRadio("", valuesForLines[lineIdx], checkableName));
        //        console.log(tableLines[lineIdx]);
        tableLines[lineIdx].prepend(td);
    }

}


export function transformTabToFormCheckable(htmlTable, valuesForLines, checkableName, checkableOnclickFunc_With_Checked_and_Value_as_Param){
    var header = htmlTable.getElementsByTagName("thead")[0];

    var body = htmlTable.getElementsByTagName("tbody")[0];

    var tableLines = body.getElementsByTagName("tr");

    var allCheckbox = [];
    
    for(var lineIdx=0; lineIdx < tableLines.length; lineIdx++){
        var td = document.createElement("td");
        var currentCheckDiv = createCheck("", valuesForLines[lineIdx], checkableName, checkableOnclickFunc_With_Checked_and_Value_as_Param);
        td.appendChild(currentCheckDiv);
        //        console.log(tableLines[lineIdx]);
        tableLines[lineIdx].prepend(td);

        var currentCheck = null;
        if(currentCheckDiv.type == "checkbox"){
            currentCheck = currentCheckDiv;
        }else{
            for (var ci = 0; ci < currentCheckDiv.children.length; ci++) {
                if(currentCheckDiv.children[ci].type == "checkbox"){
                    currentCheck = currentCheckDiv.children[ci];
                    break;
                }
            }
        }
        if(currentCheck != null){
            allCheckbox.push(currentCheck);
        }
    }

    var tableHeaderLine = header.getElementsByTagName("tr")[0];

    var checkAll_header = document.createElement("th");
    var checkAll = null;


    checkAll = createCheck("["+tableLines.length+"]", "", "",     function(){
        for(var ci=0; ci<allCheckbox.length; ci++){
            allCheckbox[ci].checked = checkAll.querySelector("input[type=checkbox]").checked;
        }
    });
    checkAll_header.appendChild(checkAll);

    tableHeaderLine.prepend(checkAll_header);

}

export function createTableFilterInput(elt_table, placeholder){
    var div_input_grp = document.createElement("div");
    div_input_grp.className = "input-group mb-3";


    // declaration pour les references d'objets
    const inputFilter = document.createElement("input");
    const butFilter = document.createElement("button");
    const inputCheckSensitive = document.createElement("input");
    const spanClearSearch = document.createElement("span");

    // -- Filter But

    butFilter.className = "btn btn-success form-control";
    butFilter.appendChild(document.createTextNode("Go"));
    butFilter.onclick = function(){filterTable(elt_table, inputFilter.value, inputCheckSensitive.checked);};

    // -- CaseSensitive Checkbox

    inputCheckSensitive.className = "form-check-input";
    inputCheckSensitive.typeName = "checkbox";

    // -- Text input

    inputFilter.className = "form-control";
    inputFilter.typeName = "search";
    if(placeholder){
        inputFilter.placeholder = placeholder;
    }else{
        inputFilter.placeholder = "Filter by title, uuid, type or version";
    }
    inputFilter.onkeypress = function(event){
        onEnterPressed(event, function(){butFilter.click();});
    };


    // -- Clear Search

    spanClearSearch.className = "inputTextClearBut fas fa-times-circle";
    spanClearSearch.onclick = function(){inputFilter.value=''; butFilter.click();};
    spanClearSearch.onmouseover = function(){spanClearSearch.className=spanClearSearch.className.replace(/fas/g,'far');};
    spanClearSearch.onmouseout = function(){spanClearSearch.className=spanClearSearch.className.replace(/far/g,'fas');};

    // -- preparation pour ajout dans la div globale

    var div_prepend = document.createElement("div");
    div_prepend.className = "input-group-prepend";

    // - Check
    var div_check = document.createElement("div");
    div_check.className = "input-group-text form-control";

    var div_check_form_grp = document.createElement("div");
    div_check_form_grp.className = "form-check form-check-inline";
    div_check.appendChild(div_check_form_grp);

    var labelCheck = document.createElement("label");
    labelCheck.className = "form-check-label";
    labelCheck.appendChild(inputCheckSensitive);
    labelCheck.appendChild(document.createTextNode("Case sensitive"))
    div_check_form_grp.appendChild(labelCheck);

    
    const inpt_grp = createInputGroup([inputFilter, spanClearSearch, div_check, butFilter], 
                            [false, false, true, true]);
    const table_rm_func = elt_table.remove;
    // Quand la table est enlevée, on enleve egalement la ligne de filtre
    elt_table.remove = function(){inpt_grp.remove(); elt_table.remove=table_rm_func; elt_table.remove();}
    return inpt_grp;
}


//Source : https://www.w3schools.com/howto/tryit.asp?filename=tryhow_js_sort_table
//retravaillé pour la possibilité d'inverser la selection

//ATTENTION : c'est tres lent pour les gros tableaux ! 
export function sortTable(tableId, columnNum, reverse) {
    var table, rows, switching, i, x, y, shouldSwitch;
    table = document.getElementById(tableId);
    switching = true;
    /*Make a loop that will continue until
      no switching has been done:*/
    while (switching) {
        //start by saying: no switching is done:
        switching = false;
        rows = table.rows;
        /*Loop through all table rows (except the
        first, which contains table headers):*/
        for (i = 1; i < (rows.length - 1); i++) {
            //start by saying there should be no switching:
            shouldSwitch = false;
            /*Get the two elements you want to compare,
          one from current row and one from the next:*/
            x = rows[i].getElementsByTagName("a")[columnNum];
            if(x==null)
                x = rows[i].getElementsByTagName("td")[columnNum];

            y = rows[i + 1].getElementsByTagName("a")[columnNum];
            if(y==null)
                y = rows[i + 1].getElementsByTagName("td")[columnNum];

            //check if the two rows should switch place:
            if (compare(x.innerHTML.toLowerCase(), y.innerHTML.toLowerCase(), reverse)<0){//x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
                //if so, mark as a switch and break the loop:
                shouldSwitch = true;
                break;
            }
        }
        if (shouldSwitch) {
            /*If a switch has been marked, make the switch
                  and mark that a switch has been done:*/
            rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
            switching = true;
        }
    }
}

export function filterTable(elt_table, filter, caseSensitiv) {
    var table = elt_table;
    if(typeof table === "string"){
        table = document.getElementById(table);
    }

    if(table != null){
        var rows = table.rows;
        var count = 0; // Count the number of validated lines
        // On squizz le table header
        for (var i = 1; i < rows.length; i++) {
            var match = false;
            for (var j = 0; j < rows[i].cells.length; j++){
                var cellText = rows[i].cells[j].innerHTML;

                if(filter!=null && filter.length > 0){
                    if(caseSensitiv!=null && caseSensitiv){
                        if(cellText.includes(filter))
                            match = true;
                    }else if(cellText.toLowerCase().includes(filter.toLowerCase())){
                        match = true;
                    }
                }else{
                    match = true;
                }
            }
            if(!match){
                rows[i].style.display = "none";
            }else{
                rows[i].style.display = "";
                count ++;
            }
        }
        // We try to print the number of valid lines in a header with a name starting with "num"
        //console.log("filtering count " + count + " '" + table.geosiris_tableFilterCountHeaderName+"'")
        for(var hi = 0; hi<table.rows[0].cells.length; hi++){
            if(table.geosiris_tableFilterCountHeaderName != null){
                if(table.rows[0].cells[hi].textContent.startsWith(table.geosiris_tableFilterCountHeaderName + " (") ){
                    var textContainer = table.rows[0].cells[hi];
                    // on cherche si il y a un noeud html fils qui contient le text en entier
                    while(textContainer.children.length>0){
                        var found = false;
                        for(var chi=0; chi<textContainer.children.length; chi++){
                            if(textContainer.children[chi].textContent.startsWith(table.geosiris_tableFilterCountHeaderName + " (") ){
                                textContainer = textContainer.children[chi];
                                found = true;
                            }
                        }
                        if(!found){
                            break;
                        }
                    }
                    //console.log("FIL 1 : " );console.log(textContainer);
                    textContainer.textContent = table.geosiris_tableFilterCountHeaderName + " ("+ count +")";
                    break;
                }
            }else if(table.rows[0].cells[hi].textContent.toLowerCase().startsWith("num")){
                table.geosiris_tableFilterCountHeaderName = table.rows[0].cells[hi].textContent;

                var textContainer = table.rows[0].cells[hi];
                while(textContainer.children.length>0){
                    var found = false;
                    for(var chi=0; chi<textContainer.children.length; chi++){
                        if(textContainer.children[chi].textContent.startsWith(table.geosiris_tableFilterCountHeaderName) ){
                            textContainer = textContainer.children[chi];
                            found = true;
                        }
                    }
                    if(!found){
                        break;
                    }
                }

                //console.log("FIL 2 : " );console.log(textContainer);
                textContainer.textContent = table.geosiris_tableFilterCountHeaderName + " ("+ count +")";
                break;
            }
        }
    }
}

export function compare(rowA, rowB, reverse){
    var comp = 0;
//    console.log("compare : '"+rowA+"' & '"+rowB+"'"  + isNaN(rowA) + " -- " + isNaN(rowB));
    if(!isNaN(rowA) && !isNaN(rowB)){ // si c'est des chiffres
        comp = parseFloat(rowA) - parseFloat(rowB);
    }else{
        comp = (rowA+"").localeCompare(rowB+"");//strcmp(rowA+"", rowB+"");
    }
    if(reverse==1)
        comp = - comp;
    return comp;
}