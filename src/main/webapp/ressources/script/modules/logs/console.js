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

import {isUUID, rws__dateNowStr} from "../common/utils.js";
import {openResqmlObjectContentByUUID} from "../main.js";
import {UUID_REGEX_raw, __CL_CONSOLE_LI__, __CL_CONSOLE_MSG_CONTENT__, __CL_CONSOLE_MSG_DATE__, __CL_CONSOLE_UL__, __ENUM_CONSOLE_MSG_SEVERITY_DEBUG__, __ENUM_CONSOLE_MSG_SEVERITY_ERROR__, __ENUM_CONSOLE_MSG_SEVERITY_LOG__, __ENUM_CONSOLE_MSG_SEVERITY_TOAST__, __RWS_CLIENT_NAME__, __TB_MSG_SEVERITY__, getSeverityClass} from "../common/variables.js";
import {createCheck, createEltAndSubs} from "../UI/htmlUtils.js";
import {createSnackBar, createToast} from "../UI/snackbar.js";


export function _format_msg_html(input){
    var value = input;
    value = value.replaceAll('\\"', '"')
    value = value.replaceAll('\\n', '<br/>')
    value = value.replaceAll('\\t', '&nbsp;&nbsp;&nbsp;&nbsp;')

    // UUIDS
    try{
        var texts = value.split(UUID_REGEX_raw)
        // found_uuids = [...new Set(value.match(UUID_REGEX_str))];
        var res = document.createElement("span");
        for (var txt_id in texts){
            var txt = texts[txt_id];
            if(isUUID(txt)){
                var node = document.createElement("span");
                node.appendChild(document.createTextNode(txt));
                node.style.cursor = "pointer";
                const c_uuid = txt;
                node.onclick = function(){
                    openResqmlObjectContentByUUID(c_uuid);
                }
                res.appendChild(node);
            }else{
                res.appendChild(document.createTextNode(txt));
            }
            value = res;
        }
        value = res;
    }catch(exception){    
        console.log(exception);
        value = document.createTextNode(value);    
    }    
    return value;
}


export function appendConsoleMessage(console_id, msg){
    if(msg instanceof Array){
        msg.forEach(elt => appendConsoleMessage(console_id, elt));
    }else if(typeof(msg) === typeof "" || msg.message == null){
        try{ 
            msg = JSON.parse(msg)
            appendConsoleMessage(console_id, msg);
        }catch(e){
            //console.log(e)
            try{
                var msg2 = msg;
                //console.log("==> ")
                msg2.message = msg2.replaceAll("\\\"", "\"");
                //console.log(msg2);
                appendConsoleMessage(console_id, JSON.parse(msg2));

            }catch(e2){

                //console.log(msg)
                appendConsoleMessage(console_id, 
                            { 
                                severity: __ENUM_CONSOLE_MSG_SEVERITY_LOG__,
                                originator: __RWS_CLIENT_NAME__,
                                message: msg
                            });
            }
        }
        
    }else{
        //var elt_msg = document.createElement("li");
        var date_now_str = rws__dateNowStr();

        try{ 
            msg = JSON.parse(msg)
        }catch(e) {
            /*console.log(e);
            console.log(">>");
            console.log(msg.message);*/
        }
        //console.log(msg)

        var severityClass = getSeverityClass(__CL_CONSOLE_LI__, __ENUM_CONSOLE_MSG_SEVERITY_LOG__);
        if(msg.severity != null){
            severityClass = getSeverityClass(__CL_CONSOLE_LI__, msg.severity)
        }
 
        var content = msg;
        if(msg.message != null){
            content = msg.message;
        }
        try{
            content = JSON.stringify(content, null, 4)
            // if(typeof(content)== typeof ""){
                // content = JSON.stringify(content, null, 4)
            // }else{
                // content = JSON.stringify('"'+JSON.stringify(content, null, 4) +'"')
            // }
            if(content[0] == "\""){
                content = content.substring(1);
            }
            if(content[content.length-1] == "\""){
                content = content.substring(0,content.length-1);
            }
        }catch(e){console.log("ERR " + e)}
        //console.log("MSG : "+msg.message);
        //console.log(content);

        var div_content_formatted = document.createElement('div');
        //div_content_formatted.innerHTML = content.trim();
        div_content_formatted.appendChild(_format_msg_html(content));

        var elt_msg = createEltAndSubs("li", "div",
                                        [document.createTextNode(date_now_str), div_content_formatted],
                                        [__CL_CONSOLE_MSG_DATE__, __CL_CONSOLE_MSG_CONTENT__],
                                        null);
        elt_msg.className = __CL_CONSOLE_LI__ + " " + severityClass;
        var ul_list = document.getElementById(console_id).getElementsByClassName(__CL_CONSOLE_UL__);


        var scroll_was_at_bottom = true;
        try{
            scroll_was_at_bottom = ul_list.scrollTop == ul_list.scrollHeight;
        }catch(e){}

        if(ul_list.length>0){
            ul_list[0].appendChild(elt_msg);

            // Toast to show a popup msg
            //if(msg.severity == __ENUM_CONSOLE_MSG_SEVERITY_ERROR__ || msg.severity == __ENUM_CONSOLE_MSG_SEVERITY_TOAST__)
            if(msg.severity == __ENUM_CONSOLE_MSG_SEVERITY_TOAST__){
//                createSnackBar(content);
                createToast(
                    {
                        title: msg.originator,
                        time: (new Date(Date.now())).toLocaleTimeString('en-US'),
                        body: msg.message,
                        option: {
                          animation: true,
                          autohide: true,
                          delay: 10000
                        }
                    }
                );
            }
        }else {
            console.log("#appendConsoleMessage error no console content found");
        }

        // no scroll if mouse is over console : 
        if(scroll_was_at_bottom)
            ul_list.scrollTop = ul_list.scrollHeight;
    }
}


export function rws_clearConsole(console_id){
    var ul_list = document.getElementById(console_id).getElementsByClassName(__CL_CONSOLE_UL__);
    if(ul_list.length>0){
        while(ul_list[0].firstChild){
            ul_list[0].removeChild(ul_list[0].firstChild);
        }
    }
}

export function rws_addConsoleMessageFilter(console_id){
    const console_elt = document.getElementById(console_id);
    // createCheck(labelTxt, value, checkName, checkableOnclickFunc_With_Checked_and_Value_as_Param, checked)
    var div_checkboxes = document.createElement("div");

    const check_classes = "__FILTER_CONSOLE_MSG_CHECK__";


    const f_consoleFilter = function(){
        var msg_list = console_elt.getElementsByClassName(__CL_CONSOLE_LI__);
        const check_msg_type = console_elt.getElementsByClassName(check_classes);
        for(let li_msg of msg_list){
            //console.log(li_msg);
            for (let checkboxDiv of check_msg_type) {
                var checkbox = checkboxDiv;
                if(checkbox.type==null || checkbox.type.toLowerCase()!="checkbox"){
                    checkbox = checkbox.getElementsByTagName("input")[0];
                }
                if(li_msg.className.includes(getSeverityClass(__CL_CONSOLE_LI__, checkbox.value)) ){
                    if(checkbox.checked){
                        li_msg.style.display = "";
                    }else{
                        li_msg.style.display = "none";
                    }
                }
            }
        }

        
    };

    // Ajout des checkbox
    for(var idx=0; idx<__TB_MSG_SEVERITY__.length; idx++){
        var severity = __TB_MSG_SEVERITY__[idx];

        var check = createCheck(severity.toLowerCase(), severity, severity, f_consoleFilter, severity != __ENUM_CONSOLE_MSG_SEVERITY_DEBUG__);
        check.className += " " + check_classes;
        div_checkboxes.appendChild(check);
    }

    // Options de l'observateur (quelles sont les mutations à observer)
    var config = { attributes: true, childList: true };

    function callback(mutationsList) {
        for (var mutation of mutationsList) {
            if(mutation.type == "childList"){
                var target = mutation.target;
                const check_msg_type = console_elt.getElementsByClassName(check_classes);
                for (let checkboxDiv of check_msg_type) {
                    var checkbox = checkboxDiv;
                    if(checkbox.type==null || checkbox.type.toLowerCase()!="checkbox"){
                        checkbox = checkbox.getElementsByTagName("input")[0];
                    }
                    if(target.className.includes(getSeverityClass(__CL_CONSOLE_LI__, checkbox.value)) ){
                        if(checkbox.checked){
                            target.style.display = "";
                        }else{
                            target.style.display = "none";
                        }
                    }
                }
            }
        }

    }

    // Créé une instance de l'observateur lié à la fonction de callback
    var observer = new MutationObserver(callback);

    // Event lors de l'ajout d'un message
    for(let lu_msg of console_elt.getElementsByClassName(__CL_CONSOLE_UL__)){
        observer.observe(lu_msg, config);
    }

    // Creation du clear button
    var clear_but = document.createElement("span");
    clear_but.className = "rws__CL_CONSOLE_CLEAR_BUT fas fa-eraser";
    clear_but.onclick = function(){ rws_clearConsole(console_id); }
    clear_but.title = "Clear console";

    var but_hide_console = document.createElement("span");
    but_hide_console.className = "rws__CL_CONSOLE_HIDE_BUT fas fa-eye-slash";
    but_hide_console.title = "Hide console";
    but_hide_console.addEventListener("mouseover", function() {
        but_hide_console.className = "rws__CL_CONSOLE_HIDE_BUT far fa-eye-slash";
    });
    but_hide_console.addEventListener("mouseout", function() {
        but_hide_console.className = "rws__CL_CONSOLE_HIDE_BUT fas fa-eye-slash";
    })
    but_hide_console.onclick = function(){
        try{
            document.getElementById("rws__CL_CONSOLE_TOGGLE_BUT").click();
        }catch(e){}
    }

    
    //<i class="far fa-eraser"></i>

    div_checkboxes.appendChild(clear_but);
    div_checkboxes.appendChild(but_hide_console);


    console_elt.insertBefore(div_checkboxes, console_elt.getElementsByClassName(__CL_CONSOLE_UL__)[0]);
    f_consoleFilter();
}
