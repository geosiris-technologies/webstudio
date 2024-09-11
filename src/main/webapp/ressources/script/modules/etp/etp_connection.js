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
import {__RWS_ETP_LOGIN__, __RWS_ETP_PWD__, __RWS_ETP_URI__, set__RWS_ETP_LOGIN__, set__RWS_ETP_PWD__, set__RWS_ETP_URI__, dataspaces_needs_update, dataspace_reset_timer, dataspace_has_been_updated} from "../common/variables.js"


export function geosiris_createETP_connector_form(fun_isConnected, fun_isDisconnected, callpre_func, callback_func){
    var HAS_BEEN_CONNECTED_ONCE = false;

    const form = document.createElement('form');
    form.setAttribute('action', 'ETPConnexion');
    form.setAttribute('method', 'post');
    form.classList.add('ETPConnexion_form');

    const inputGroup = document.createElement('div');
    inputGroup.classList.add('input-group');

    const serverAddressLabel = document.createElement('label');
    serverAddressLabel.classList.add('input-group-text');
    serverAddressLabel.setAttribute('type', 'label');
    serverAddressLabel.textContent = 'Server address';
    inputGroup.appendChild(serverAddressLabel);

    const serverAddressInput = document.createElement('input');
    serverAddressInput.classList.add('form-control');
    serverAddressInput.setAttribute('type', 'text');
    serverAddressInput.setAttribute('name', 'etp-server-uri');
    serverAddressInput.setAttribute('placeholder', 'e.g. xxx.xxx.xxx.xxx or mydomain.com');
    inputGroup.appendChild(serverAddressInput);

    const usernameLabel = document.createElement('label');
    usernameLabel.classList.add('input-group-text');
    usernameLabel.setAttribute('type', 'label');
    usernameLabel.textContent = 'Username';
    inputGroup.appendChild(usernameLabel);

    const usernameInput = document.createElement('input');
    usernameInput.classList.add('form-control');
    usernameInput.setAttribute('type', 'text');
    usernameInput.setAttribute('name', 'etp-server-username');
    usernameInput.setAttribute('placeholder', 'Username');
    usernameInput.setAttribute('autocomplete', 'on');
    inputGroup.appendChild(usernameInput);

    const passwordLabel = document.createElement('label');
    passwordLabel.classList.add('input-group-text');
    passwordLabel.textContent = 'Password';
    inputGroup.appendChild(passwordLabel);

    const passwordInput = document.createElement('input');
    passwordInput.classList.add('form-control');
    passwordInput.setAttribute('type', 'password');
    passwordInput.setAttribute('name', 'etp-server-password');
    passwordInput.setAttribute('autocomplete', 'on');
    inputGroup.appendChild(passwordInput);

    form.appendChild(inputGroup);

    const connectButton = document.createElement('input');
    connectButton.setAttribute('type', 'button');
    connectButton.setAttribute('name', 'request-type');
    connectButton.setAttribute('value', 'Establish etp connexion');
    connectButton.classList.add('btn', 'btn-primary');
    connectButton.className += ' mt-2';

    const d_err = document.createElement('p');
    d_err.style.color = 'red';
    d_err.style.display = 'none';
    d_err.textContent = 'Please fill host and port before trying to connect';

    const inreq = document.createElement('input');
    inreq.setAttribute('type', 'text');
    inreq.setAttribute('name', 'request-type');
    inreq.setAttribute('hidden', '');

    const cst_fun_isConnected = fun_isConnected;
    const cst_fun_isDisconnected = fun_isDisconnected;
    const cst_callpre_func = callpre_func;
    const cst_callback_func = callback_func;

    const func_update_btn_view = function(btnConn, input_req, isConnected){
        cst_callpre_func();

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
                input_req.value = "disconnect";
                inputGroup.style.display = 'none';
                if(cst_fun_isConnected != null){
                    cst_fun_isConnected();
                }
            }else{
                if(serverAddressInput.value != null && serverAddressInput.value.length>0){
                    set__RWS_ETP_URI__("");
                    set__RWS_ETP_LOGIN__("");
                    set__RWS_ETP_PWD__("");
                }
                btnConn.value = "Establish etp connexion";
                btnConn.className = "btn btn-primary mt-2";
                input_req.value = "connect";
                inputGroup.style.display = '';
                if(HAS_BEEN_CONNECTED_ONCE && cst_fun_isDisconnected != null){
                    cst_fun_isDisconnected();
                }
            }
        update_dataspaces_inputs(cst_callback_func);
    }
    form.updateView = function(isConnected){
        func_update_btn_view(connectButton, inreq, isConnected);
    }

    connectButton.onclick = function(){
        if(inreq.value=="disconnect" || serverAddressInput.value.length>0 ){
            sendPostForm_Promise(form, "ETPConnexion", false).then(
                function(){
                    dataspace_reset_timer();
                    update_etp_connexion_views();
                }
            )
            d_err.style.display = 'none';
        }else{
            d_err.style.display = '';
        }

    }

    form.appendChild(connectButton);
    form.appendChild(d_err);
    form.appendChild(inreq);

    return form;
}

export function update_etp_connexion_views(){
    sendGetURL_Promise("ETPConnexion?request=isconnected").then(
            responseText =>{
                var isConnected = responseText.toLowerCase()=="true";
        [].forEach.call(document.getElementsByClassName("ETPConnexion_form"), (elt, idx)=> {
                        elt.updateView(isConnected);
                    });
    });
}

export function create_dataspace_input(callback_func, inputs_classes_to_update){
    var div = document.createElement("div");
    div.className = "input-group mb-3 mt-3";

    var label = document.createElement("label");
    label.for = "dataspace";
    label.appendChild(document.createTextNode("Dataspace :"));
    label.style.display = 'ruby-base';
    label.className = "input-group-text";
    label.type = "label";
    div.appendChild(label);

    const selectDataspace = document.createElement("select");
    selectDataspace.name = 'dataspace';
    selectDataspace.className += 'form-select etp_select_dataspace';
//    selectDataspace.style.width = 'max-content';

    const const_inputs_classes_to_update = inputs_classes_to_update;


    selectDataspace.addEventListener("change", function(event){
         if(const_inputs_classes_to_update != null){
            if(Array.isArray(const_inputs_classes_to_update)){
                const_inputs_classes_to_update.forEach(cl => $((cl.startsWith(".") ? "" : ".") + cl).each((i, _elt) => _elt.value = event.target.value));
            }else{
                $("."+const_inputs_classes_to_update).each((i, _elt) => _elt.value = event.target.value);
            }
        }
    });


    selectDataspace.update = function (dataspacesNamesArray){
        var selectedValue = null;
        // keep previous selected value during updates
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
    div.appendChild(selectDataspace);

    return div;
}

export function update_dataspaces_inputs(callback_func, force){
    // console.log("dataspaces_needs_update" + dataspaces_needs_update());
    if( (force != null && force) || dataspaces_needs_update() ){
        dataspace_has_been_updated()
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
    }else if(callback_func != null){
        callback_func()
    }
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