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
import {sendGetURLAndReload, refreshWorkspace} from "../UI/eventHandler.js"
import {__USER_NAME__, beginTask, endTask, getVueOrientation, setVueOrientation, initRootEltSelector, highlightExistingElt} from "../UI/ui.js"
import {createCheck} from "../UI/htmlUtils.js"
import {sendGetURL, sendGetURL_Promise, sendPostRequestJson, getJsonObjectFromServer} from "../requests/requests.js"
import {appendConsoleMessage} from "../logs/console.js"
import {__ENUM_CONSOLE_MSG_SEVERITY_INFO__, __ENUM_CONSOLE_MSG_SEVERITY_WARNING__, 
        __ENUM_CONSOLE_MSG_SEVERITY_ERROR__, __RWS_CLIENT_NAME__, __RWS_SERVER_NAME__, 
        getSeverityEnum, REGEX_ENERGYML_FILE_NAME, CLASS_HIGHLIGHT_EXISTING_OBJECT,
        getObjectTableCellClass} from "../common/variables.js"

/**********************************************************/
/** Do not forget to access in map with LOWERCASE key !! **/
/**********************************************************/

export var mapResqmlTypeToSubtypes = {};
export var mapResqmlTypesComment   = {};
export var mapResqmlEnumToValues   = {};
//export var epcPackagesGroups       = [];
export var extTypeAttributes       = {};
export var energymlRootTypes       = {};


export function updateTypesMap(){
    // Recuperation des types accessibles et des classes qui peuvent les instancier
    getJsonObjectFromServer("ResqmlAccessibleTypes").then( (jsonResponse) => {
        try{
            mapResqmlTypeToSubtypes = jsonResponse;
        }catch(err){console.log(err);}
    }).catch((error) => console.error(error));

    // Recuperation des types enum et de leurs valeurs possibles
    getJsonObjectFromServer("ResqmlAccessibleEnumAndValues").then( (jsonResponse) => {
        try{
            mapResqmlEnumToValues = jsonResponse;
        }catch(err){console.log(err);}
    }).catch((error) => console.error(error));

    // Recuperation des commentaires des classes
    getJsonObjectFromServer("ResqmlTypesComments").then( (jsonResponse) => {
        try{
            mapResqmlTypesComment = jsonResponse;
        }catch(err){console.log(err);}
    }).catch((error) => console.error(error));

    // Recuperation des groups de packages
    getJsonObjectFromServer("EPCExtTypesAttributes").then( (jsonResponse) => {
        try{
            extTypeAttributes = jsonResponse;
        }catch(err){console.log(err);}
    }).catch((error) => console.error(error));

    // Recuperation des types racines
    getJsonObjectFromServer("ResqmlRootTypes").then( (jsonResponse) => {
        try{
            energymlRootTypes = jsonResponse;
            initRootEltSelector(document.getElementById("selectorCreateRootElt"));
        }catch(err){console.log(err);}
    }).catch((error) => console.error(error));
}

export function openHDFViewAskH5Location(h5Path, subPath){
    openHDFView(h5Path, subPath);
}

export function openHDFView(h5Path, subPath){
    //var path = "http://h5viewer:3000/"+h5Path;
    console.log("UserName '" + __USER_NAME__ + "'")
    var path = "http://localhost:9996/"+ (__USER_NAME__!="" ? __USER_NAME__+"/" : "") +h5Path + subPath;
    // TODO: changer l'url
    console.log("opening HDFView with url : '" + path+"' sub path was : '" + subPath +"'");
    window.open(path, '_blank');
}

export function resquestObjectCopy(idConsoleElt, rootUUID){
    var url = "ObjectEdit";
    if(rootUUID != null)
    {
        url += "?Root_UUID=" + rootUUID + "&command=copy&version=2.3";
    }
    
    sendGetURLAndReload(url, false);
}


export function resquestValidation(idConsoleElt, rootUUID){
    var url = "ResqmlVerification";
    if(rootUUID != null)
    {
        url += "?uuid=" + rootUUID;
    }
    
    getJsonObjectFromServer(url).then(
        function(jsonValidationMessages){
            var msgList = [];

            var cpt_errors = 0;
            var cpt_warnings = 0;
            for(var validMsgIdx=0; validMsgIdx<jsonValidationMessages.length; validMsgIdx++){
                var msgVerif = jsonValidationMessages[validMsgIdx];
                var severity = null;
                try{
                    severity = getSeverityEnum(msgVerif.severity)
                }catch(exception){}
                if(severity == __ENUM_CONSOLE_MSG_SEVERITY_ERROR__)
                    cpt_errors++;
                if(severity == __ENUM_CONSOLE_MSG_SEVERITY_WARNING__)
                    cpt_warnings++;
            }

            var message = "Validation ";

            if(cpt_warnings > 0 || cpt_errors > 0){
                message += "report:";
                if(cpt_warnings > 0){
                    message += cpt_warnings + " warning" + (cpt_warnings>1?"s ":" ");
                    if(cpt_errors > 0)
                        message +=  "and"
                }
                if(cpt_errors > 0){
                    message += " " + cpt_errors + " error" + (cpt_errors>1?"s":"");
                }
            }else{
                message += "success"
            }
            msgList.push({ 
                severity: __ENUM_CONSOLE_MSG_SEVERITY_INFO__,
                originator: __RWS_CLIENT_NAME__,
                message: message
            });


            for(var validMsgIdx=0; validMsgIdx<jsonValidationMessages.length; validMsgIdx++){
                var msgVerif = jsonValidationMessages[validMsgIdx];
                msgList.push(msgVerif)
            }
            appendConsoleMessage(idConsoleElt, msgList);
        }
    ).catch((error) => console.error(error));
}


export function resquestCorrection(idConsoleElt, rootUUID, correctionType){
    var url = "ResqmlCorrection";
    url += "?correctionType=" + correctionType;
    if(rootUUID != null)
    {
        url += "&uuid=" + rootUUID;
    }

    //console.log("correction request at url " + url);
    beginTask();
    
    return sendGetURL_Promise(url).then(
            function(responseText){
                //console.log("Response is : " + responseText)
                try{
                    var correctionJSON = JSON.parse(responseText);

                    var msgList = [];
                    msgList.push(
                        { 
                            severity: __ENUM_CONSOLE_MSG_SEVERITY_INFO__,
                            originator: __RWS_CLIENT_NAME__,
                            message: "CORRECTIONS : " + correctionJSON.length + "\n"
                        });

                    for(var validMsgIdx=0; validMsgIdx<correctionJSON.length; validMsgIdx++){
                        var msgCorrect = correctionJSON[validMsgIdx];
                        var msgContent = msgCorrect.date + " : " + msgCorrect.title 
                                        + " for " +  msgCorrect.rootType 
                                        + " with uuid " + msgCorrect.rootUUID 
                                        + " and title " + msgCorrect.rootTitle
                                        + "\n";
                        msgContent += "\t" + msgCorrect.msg +"\n";
                        msgList.push(
                            { 
                                severity: __ENUM_CONSOLE_MSG_SEVERITY_WARNING__,
                                originator: __RWS_SERVER_NAME__,
                                message: msgContent
                            });
                    }
                    appendConsoleMessage(idConsoleElt, msgList);
                }catch(exceptResponse){
                    console.log(exceptResponse);
                    console.log("no jsonable text : ");
                    console.log(responseText);
                }
                endTask();
            }
    );
}


export function genObjectPropertyElementId(uuid){
    return "resqml_" + uuid + "_properties";
}

export function genObjectContentElementId(uuid){
    return "resqml_" + uuid + "_content";
}
export function genObjectContentDivElementId(uuid){
    return "tabulation_id_" + uuid;
}



// Preferences
export function updateVueFromPreferences(){
    var url = "Preferences";
    return getJsonObjectFromServer(url).then(
        function(prefJson){
            try{
                if(prefJson["vueOrientation"] != null){
                    setVueOrientation(prefJson["vueOrientation"]);
                }
            }catch(exceptResponse){
                console.log(exceptResponse);
                console.log("no jsonable text : ");
                console.log(responseText);
            }
        }
    );
}

export function savePreferences(){
    var url = "Preferences";
    
    return sendPostRequestJson(url, {
        "vueOrientation": getVueOrientation()
    });
}


// Energyml EPC/Xml content

export const XSLT_XML_TO_SIMPLE_JSON = `
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:eml="http://www.energistics.org/energyml/data/commonv2">
    
    <xsl:output indent="no" omit-xml-declaration="yes" method="text" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>
    
    <xsl:template match="/">
        <xsl:text>{&#xa;</xsl:text>
        <xsl:text>"uuid": "</xsl:text>
        <xsl:value-of select="/*/@uuid"/>
        <xsl:text>"</xsl:text>
        <xsl:text>,&#xa;"type": "</xsl:text>
        <xsl:value-of select="local-name(/*)"/>
        <xsl:text>"</xsl:text>
        <xsl:apply-templates select="/*"/>
        <xsl:text>&#xa;}</xsl:text>
    </xsl:template>
    <xsl:template match="/*/eml:Citation/eml:Title">
        <xsl:text>,&#xa;"title" : "</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>"</xsl:text>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:transform>
`

export function energyml_file_to_json_simple(file_content, xslt){
    const xsltProcessor = new XSLTProcessor();
    const serializer = new XMLSerializer();
    const parser = new DOMParser();

    const xslt_node = parser.parseFromString(xslt, "text/xml");
    xsltProcessor.importStylesheet(xslt_node);
    const fragment = xsltProcessor.transformToFragment(parser.parseFromString(file_content, "text/xml"), parser.parseFromString("", "text/xml"));
    const document_fragment_string = serializer.serializeToString(fragment);
    return JSON.parse(document_fragment_string);
}

export function read_file(file, action_on_content_and_name, sub_path){
    if(file.name.toLowerCase().endsWith(".epc")){
        return JSZip.loadAsync(file)
            .then(function(zip) {
                var promiseList = []
                if(sub_path != null){
                    promiseList.push(zip.files[sub_path].async('text').then((content) =>{
                        action_on_content_and_name(content, sub_path);
                        return [content, sub_path];
                    }));
                }else{
                    zip.forEach(async function (relativePath, zipEntry) {
                        if(zipEntry.name.match(REGEX_ENERGYML_FILE_NAME)){
                            promiseList.push(zipEntry.async("text").then( (file_content) => {
                                action_on_content_and_name(file_content, zipEntry.name);
                                return [file_content, zipEntry.name];
                            }));
                        }
                    });
                }
                return promiseList;
            }, function (e) {
                result.appendChild(document.createTextNode( "Error reading " + f.name + ": " + e.message))
            });
    }else{
        const reader = new FileReader();
        reader.addEventListener('load', (event) => {
            action_on_content_and_name(event.target.result, file.name);
        });
        return Promise.resolve(reader.readAsText(file));
        //reader.readAsDataURL(f);
    }
}
//highlightExistingElt
export function epc_partial_importer(input_elt, parent, idConsoleElt, f_eraseWorkspace=()=>false){
    const attrib_list = ["title", "uuid", "type", "path"];

    var files = input_elt.files;
    const epc_importer_div = document.createElement("div");
    parent.appendChild(epc_importer_div)

    for (var i = 0; i < files.length; i++) {
        const f = files[i];
        const name_id = "partial_import_" + f.name;

        const file_div = document.createElement("div");

        const title = document.createElement("h4");
        title.appendChild(document.createTextNode(f.name));
        file_div.appendChild(title);
        
        var import_but = document.createElement("input");
        import_but.value = "Import";
        import_but.style.maxWidth = "100px";
        import_but.className = "btn btn-success form-control";
        file_div.appendChild(import_but);

        import_but.type = "button";
        import_but.onclick = function(event){
            Promise.all($('input[name="' + name_id + '"]').filter(':checked').map(function(e, v) {
                return v.value;
            }).get().map( (path) => {
                if(f.name.toLowerCase().endsWith(".epc")){
                    return JSZip.loadAsync(f)
                        .then(function(zip) {
                            return zip.files[path].async('text').then(content=> [content, path]);
                        }, function (e) {
                            console.log( "Error reading " + f.name + ": " + e.message);
                        });
                }else{
                    return new Promise((resolve, reject) => {
                        var reader = new FileReader();  
                        reader.onload = resolve;  // CHANGE to whatever function you want which would eventually call resolve
                        reader.onerror = reject;
                        reader.readAsText(f);
                    }).then(event => [event.target.result, f.name]);
                }
            })).then((contents) => importMultipleFilesToWorkspace(contents, idConsoleElt, f_eraseWorkspace()));
        }

        const doc_range = document.createRange();

        const table_frag = doc_range.createContextualFragment(
            `<table class="table-striped table-bordered table-hover table-fixed table-top-fixed">
                <thead><tr></tr></thead>
                <tbody></tbody>
            </table>`
        );

        const table = table_frag.firstChild;
        const t_head = table.getElementsByTagName("thead")[0];
        const t_body = table.getElementsByTagName("tbody")[0];

        const all_line_checkbox = [];
        const global_check = createCheck("", "", `partial_import`, function(checked, value){
            for(var ci=0; ci<all_line_checkbox.length; ci++){
                all_line_checkbox[ci].querySelector("input[type=checkbox]").checked = checked;
            }
        });

        global_check._updateCheck = function (){
            var oneUnchecked = false;
            for(var ci=0; ci<all_line_checkbox.length; ci++){
                oneUnchecked = !all_line_checkbox[ci].querySelector("input[type=checkbox]").checked;
                if (oneUnchecked){
                    break;
                }
            }
            global_check.querySelector("input[type=checkbox]").checked = !oneUnchecked;
        }
        var th_check = document.createElement("th");
        th_check.appendChild(global_check);
        t_head.firstChild.appendChild(th_check);

        attrib_list.forEach( (attrib) =>{
            var th_elt = document.createElement("th");
            th_elt.appendChild(document.createTextNode(attrib.substring(0, 1).toUpperCase() + attrib.substring(1)));
            t_head.firstChild.appendChild(th_elt);
        });

        function addEntryLine(file_content, file_name){
            const json_obj = energyml_file_to_json_simple(file_content, XSLT_XML_TO_SIMPLE_JSON);
            json_obj["path"] = file_name;
            var tr_elt = document.createElement("tr");
            t_body.appendChild(tr_elt);
            tr_elt.className = getObjectTableCellClass(json_obj["uuid"]) + " " + CLASS_HIGHLIGHT_EXISTING_OBJECT;

            var td_check_elt = document.createElement("td");
            tr_elt.appendChild(td_check_elt);
            var currentCheckDiv = createCheck("", file_name, name_id, () => global_check._updateCheck());
            all_line_checkbox.push(currentCheckDiv);
            td_check_elt.appendChild(currentCheckDiv);

            attrib_list.forEach( (attrib) =>{
                var td_elt = document.createElement("td");
                td_elt.appendChild(document.createTextNode(json_obj[attrib]));
                tr_elt.appendChild(td_elt);
            });
        }

        read_file(f, addEntryLine).then((v) =>{
            highlightExistingElt();
        });

        file_div.appendChild(table);
        epc_importer_div.appendChild(file_div);
    }
    return epc_importer_div;
}

export function importMultipleFilesToWorkspace(fileContentAndNameList, idConsoleElt, eraseWorkspace=false){
    const f_n_list = fileContentAndNameList;
    var formData = new FormData();

    f_n_list.forEach(fileAndNamePair => {
        var file = fileAndNamePair[0];
        var fileName = "default.xml";
        var extension = "xml";

        try{
            var extension = fileName.substring(fileName.lastIndexOf(".") + 1)
            var fileName = fileAndNamePair[1];
        }catch(exception){console.log(e);}

        if(typeof file == "string"){
            var blob = new Blob([file], { type: `text/${extension}` });
            file = new File([blob], fileName, {type: `text/${extension}`});
        }

        formData.append("epcInputFile[]", file, fileName);
    });
    formData.append("import", !eraseWorkspace);

    fetch('/FileReciever', {method: "POST", body: formData})
    .then( ()=> refreshWorkspace() )
    .then( () => resquestValidation(idConsoleElt, null));
}

export function importFileToWorkspace(file, fileName, idConsoleElt, eraseWorkspace=false){
    const c_original_file = file;

    var formData = new FormData();
    if(typeof file == "string"){
        var blob = new Blob([file], { type: 'text/xml' });
        file = new File([blob], fileName, {type: "text/xml"});
    }

    formData.append("epcInputFile", file);
    formData.append("import", !eraseWorkspace);
        fetch('/FileReciever', {method: "POST", body: formData}).then(function(response) {
    }).then( () => {
        if(typeof c_original_file == "string"){
            var json_obj = energyml_file_to_json_simple(c_original_file, XSLT_XML_TO_SIMPLE_JSON);
            resquestValidation(idConsoleElt, json_obj["uuid"]);
        }
    }
    ).then( ()=> refreshWorkspace() );
}