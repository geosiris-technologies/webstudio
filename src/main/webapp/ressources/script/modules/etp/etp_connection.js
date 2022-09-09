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

import {sendGetURL_Promise, sendPostForm_Promise} from "../requests/requests.js"
import {__RWS_ETP_LOGIN__, __RWS_ETP_PWD__, __RWS_ETP_URI__, set__RWS_ETP_LOGIN__, set__RWS_ETP_PWD__, set__RWS_ETP_URI__} from "../common/variables.js"


export function geosiris_createETP_connector_form(fun_isConnected, fun_isDisconnected, callpre_func, callback_func){
    var HAS_BEEN_CONNECTED_ONCE = false;

    const form = document.createElement('form');
    form.action = 'ETPConnexion';
    form.method = 'post';
    form.className ='ETPConnexion_form';

    var div_form = document.createElement('div');
    div_form.className = "form-row";
    form.appendChild(div_form);

    const div_inputs = document.createElement('div');
    div_inputs.className = "form-row";
    div_inputs.style = "width:100%";
    div_form.appendChild(div_inputs);

    // IP
    var d0 = document.createElement("div");
    d0.className = "form-group col-md-3";
    div_inputs.appendChild(d0);

    var lbl0 = document.createElement("label");
    lbl0.name = "etp-server-uri";
    lbl0.appendChild(document.createTextNode("Server address"))
    const in0 = document.createElement("input");
    in0.className = "form-control";
    in0.type = "text";
    in0.name = "etp-server-uri";
    in0.style = "width:100%";
    in0.placeholder = 'e.g. xxx.xxx.xxx.xxx or mydomain.com';
    d0.appendChild(lbl0);
    d0.appendChild(in0);

    // UserName
    var d2 = document.createElement("div");
    d2.className = "form-group col-md-2";
    div_inputs.appendChild(d2);

    var lbl2 = document.createElement("label");
    lbl2.name = "etp-server-username";
    lbl2.appendChild(document.createTextNode("Username"))
    var in2 = document.createElement("input");
    in2.className = "form-control";
    in2.type = "text";
    in2.name = "etp-server-username";
    in2.style = "width:100%";
    in2.placeholder = 'Username';
    d2.appendChild(lbl2);
    d2.appendChild(in2);

    // PASSWORD
    var d3 = document.createElement("div");
    d3.className = "form-group col-md-2";
    div_inputs.appendChild(d3);

    var lbl3 = document.createElement("label");
    lbl3.name = "etp-server-password";
    lbl3.appendChild(document.createTextNode("Password"))
    var in3 = document.createElement("input");
    in3.className = "form-control";
    in3.type = "password";
    in3.name = "etp-server-password";
    in3.style = "width:100%";
    d3.appendChild(lbl3);
    d3.appendChild(in3);

    // Err show
    const d_err = document.createElement("p");
    d_err.style = "color:red";
    d_err.style.display = 'none';
    d_err.appendChild(document.createTextNode("Please fill host and port before trying to connect"))
    div_inputs.appendChild(d_err);


    // BTN CONNECT
    var d4 = document.createElement("div");
    d4.className = "form-group col-md-6";
    div_form.appendChild(d4);

    const inreq = document.createElement("input");
    inreq.type = "text";
    inreq.name = 'request-type';
    inreq.value = 'connect';
    inreq.hidden = "hidden";
    d4.appendChild(inreq);

    const cst_fun_isConnected = fun_isConnected;
    const cst_fun_isDisconnected = fun_isDisconnected;
    const cst_callpre_func = callpre_func;
    const cst_callback_func = callback_func;

    const func_update_btn_view = function(btnConn, input_req){
        cst_callpre_func();
        sendGetURL_Promise("ETPConnexion?request=isconnected").then(
            responseText =>{
                var isConnected = responseText.toLowerCase()=="true";
                if(isConnected){

                    // we retake informations about the connexion. 
                    // It is important if the page has been reload with an active etp
                    // connexion, to be able to send the connexion infos to activity launcher
                    // or to the 3D vue model importer (server-visu)
                    sendGetURL_Promise("ETPConnexionInfos").then(
                        responseText_infos =>{
                            try{
                                const etpInfosJson = JSON.parse(responseText_infos);
                                if(etpInfosJson["serverUrl"] != null){
                                    set__RWS_ETP_URI__(etpInfosJson["serverUrl"]);
                                    set__RWS_ETP_LOGIN__(etpInfosJson["login"]);
                                    set__RWS_ETP_PWD__(etpInfosJson["password"]);
                                }else{
                                    console.log("ETP should be connected but no url found : ");
                                    console.log(responseText_infos);
                                }
                            }catch(jsonFailed){
                                console.log(jsonFailed);
                            }
                        }
                    );
                    

                    HAS_BEEN_CONNECTED_ONCE = true;
                    btnConn.value = "Close ETP connection";
                    btnConn.className = "btn btn-danger";
                    input_req.value="disconnect";
                    div_inputs.style.display = 'none';
                    if(cst_fun_isConnected != null){
                        cst_fun_isConnected();
                    }
                }else{
                    if(in0.value != null && in0.value.length>0){
                        set__RWS_ETP_URI__("");
                        set__RWS_ETP_LOGIN__("");
                        set__RWS_ETP_PWD__("");
                    }
                    btnConn.value = "Establish etp connexion";
                    btnConn.className = "btn btn-primary";
                    input_req.value="connect";
                    div_inputs.style.display = '';
                    if(HAS_BEEN_CONNECTED_ONCE && cst_fun_isDisconnected != null){
                        cst_fun_isDisconnected();
                    }
                }
            }
        );
        update_dataspaces_inputs(cst_callback_func);
    }
    form.updateView = function(){
        func_update_btn_view(btnConnect, inreq);
    }

    const btnConnect = document.createElement("input");
    btnConnect.type = "button";
    btnConnect.name = 'request-type';
    btnConnect.value = 'Establish etp connexion';
    btnConnect.className = "btn btn-primary";
    btnConnect.onclick = function(){
        if(inreq.value=="disconnect" || in0.value.length>0 ){
            sendPostForm_Promise(form, "ETPConnexion", false).then(
                function(){
                    form.updateView();
                }
            )
            d_err.style.display = 'none';
        }else{
            d_err.style.display = '';
        }
        
    }
    d4.appendChild(btnConnect);

    return form;
}

export function update_etp_connexion_views(){
    [].forEach.call(document.getElementsByClassName("ETPConnexion_form"), (elt, idx)=> {
                        elt.updateView();
                    });
}

export function create_dataspace_input(callback_func){
    var div = document.createElement("div");
    div.class = "form-control";


    var label = document.createElement("label");
    label.for = "dataspace";
    label.appendChild(document.createTextNode("Dataspace :"));
    label.style.display = 'ruby-base';
    div.appendChild(label);

    const selectDataspace = document.createElement("select");
    selectDataspace.name = 'dataspace';
    selectDataspace.className += 'form-control etp_select_dataspace';
    selectDataspace.style.width = 'max-content';

    selectDataspace.update = function (dataspacesNamesArray){
        var selectedValue = null;
        try{
            selectedValue = selectDataspace.selectedOptions[0].value
        }catch{}

        while(selectDataspace.firstChild){
            selectDataspace.firstChild.remove();
        }
        if(dataspacesNamesArray.length > 0){
            var option = document.createElement("option");
            option.value = "";
            option.appendChild(document.createTextNode("*default-dataspace*"));
            selectDataspace.appendChild(option);
        }

        [].forEach.call(dataspacesNamesArray, (elt, idx) =>{
            var option = document.createElement("option");
            option.value = elt;
            option.appendChild(document.createTextNode(elt));
            selectDataspace.appendChild(option);
        });

        if(callback_func != null){
            callback_func();
        }
        if(selectedValue != null){
            selectDataspace.childNodes.forEach((child_opt, idx)=>{
                if(child_opt.value == selectedValue){
                    selectDataspace.selectedIndex = idx;
                }
            })
        }
    };
    selectDataspace.update([]);
    label.appendChild(selectDataspace);

    return div;
}

export function update_dataspaces_inputs(callback_func){
    sendGetURL_Promise("ETPListDataspaces").then(
            responseText =>{
                try{
                    var dataspacesNames = JSON.parse(responseText);
                    [].forEach.call(document.getElementsByClassName("etp_select_dataspace"), (elt, idx)=> {
                        elt.update(dataspacesNames);
                    });
                }catch(except){console.log(except);console.log(responseText)}
                if(callback_func != null){
                    callback_func()
                }
            });
    
}


export function geosiris_reactToETPConnectionStatus(fun_isConnected, fun_isDisconnected){
    sendGetURL_Promise("ETPConnexion?request=isconnected").then(
            responseText =>{
                if(responseText.toLowerCase()=="true"){
                    if(fun_isConnected != null){
                        fun_isConnected();
                    }
                }else if(fun_isDisconnected != null){
                        fun_isDisconnected();
                }
            });
}