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

import {appendConsoleMessage} from "../logs/console.js"
import {__ID_CONSOLE_MODAL__} from "../common/variables.js"
import {beginTask, endTask} from "../UI/ui.js"


export function sendGetURL(url, showRequestResult, onloadFunction) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.open( "GET", url, true );
    // Si la fonction demande un argument en parametre, on passe le resultat text de la requete
    if( !(onloadFunction === undefined) && onloadFunction!=null && onloadFunction.length>0){
        xmlHttp.addEventListener("load", 
            function(){
                //console.log("response is " + xmlHttp.responseText)
                onloadFunction(xmlHttp.responseText);
            }
            );
    }else{
        xmlHttp.addEventListener("load", onloadFunction);
    }
    xmlHttp.addEventListener('error', (x) => {console.log("Error in GET request "); console.log(x)});
    xmlHttp.addEventListener('abort', (x) => {console.log("Error in GET request "); console.log(x)});
    xmlHttp.send( null );
    //console.log("sending get on '" + url + "'");
    return sendGetURL_Promise(url); //.then((result) => console.log("HAHAHA "+ result));
}

export function sendGetURL_Promise(url) {
    //console.log("GET promise on " + url)
    return new Promise( function (resolve, reject) {
        var xmlHttp = new XMLHttpRequest();

        xmlHttp.open( "GET", url, true );
        xmlHttp.onload = function (event) {
                                resolve(xmlHttp.responseText);     // Si Ok
                            };

        xmlHttp.onerror = function (err) {
            reject(err);                    // Si erreur
        }
        xmlHttp.send( null );
    });
}


export function downloadGetURL_Promise(uri, fileName) {
    return new Promise( function (resolve, reject) {
        var link = document.createElement("a");
        link.href = uri;
        link.download=fileName;
        resolve(link.click());
    });
}

export function sendPostForm_Promise(form, url, showRequestResult) {
    return new Promise(function (resolve, reject) {
        var XHR = new XMLHttpRequest();

        // Liez l'objet FormData et l'élément form
        var FD = new FormData(form);

        if(showRequestResult==null || showRequestResult==true){
            // Définissez ce qui se passe si la soumission s'est opérée avec succès
            XHR.addEventListener("load", function(event) {
                //alert(event.target.responseText);
                try{
                    appendConsoleMessage(__ID_CONSOLE_MODAL__, event.target.responseText);
                }catch(exceptConsole){console.log(exceptConsole);}
                resolve(event.target.responseText)
            });

            // Definissez ce qui se passe en cas d'erreur
            XHR.addEventListener("error", function(err) {
                reject(err);
            });
        }else{
            XHR.addEventListener("load", function(event) {
                resolve(event.target.responseText)
            });
        }
        if(form.method!=null)
            XHR.open(form.method, url, true);
        else
            XHR.open("post", url, true);

        // Les données envoyées sont ce que l'utilisateur a mis dans le formulaire
        XHR.send(FD);
    });
}

export function sendPostForm(form, url, showRequestResult) {
    var XHR = new XMLHttpRequest();

    // Liez l'objet FormData et l'élément form
    var FD = new FormData(form);

    if(showRequestResult==null || showRequestResult==true){
        // Définissez ce qui se passe si la soumission s'est opérée avec succès
        XHR.addEventListener("load", function(event) {
            // alert(event.target.responseText);
            appendConsoleMessage(__ID_CONSOLE_MODAL__, event.target.responseText);
        });

        // Definissez ce qui se passe en cas d'erreur
        XHR.addEventListener("error", function(event) {
            // alert('Oups! Quelque chose s\'est mal passé.');
        });
    }

    // Configurez la requête
    /*console.log(form.method + " - ");
    console.log(url); 
    console.log(form); */
    if(form.method!=null)
        XHR.open(form.method, url, true);
    else
        XHR.open("post", url, true);

    // Les données envoyées sont ce que l'utilisateur a mis dans le formulaire
    XHR.send(FD);

    return XHR;
}

export function sendPostForm_Func(form, url, onloadFunc) {
    var XHR = new XMLHttpRequest();

    // Liez l'objet FormData et l'élément form
    var FD = new FormData(form);
    if(onloadFunc!=null){
        // Définissez ce qui se passe si la soumission s'est opérée avec succès
        
        if( !(onloadFunc === undefined) && onloadFunc.length>0){
            XHR.addEventListener("load", function(event) {
                onloadFunc(event.target.responseText);
            });
        }else{
            XHR.addEventListener("load", onloadFunc);
        }

        // Definissez ce qui se passe en cas d'erreur
        XHR.addEventListener("error", function(event) {
            alert('Oups! Quelque chose s\'est mal passé.');
        });
    }
    if(form.method!=null){
        XHR.open(form.method, url, true);
        //console.log("form methode " + form.method);
    }else
    XHR.open("POST", url, true);

    // Les données envoyées sont ce que l'utilisateur a mis dans le formulaire
    XHR.send(FD);
    return XHR;
}

export function sendPostRequest(str_url, dict_parameters){
    // Creation dynamique du formulaire
    var form = document.createElement(form);
    form.setAttribute(method, POST);
    form.setAttribute(action, str_url);
    // Ajout des parametres
    for(var cle in dict_parameters) {
        if(dict_parameters.hasOwnProperty(cle)) {
            var champCache = document.createElement(input);
            champCache.type = hidden;
            champCache.name = cle;
            champCache.value = dict_parameters[cle];
            form.appendChild(champCache);
        }
    }
    // Soumission du formulaire
    document.body.appendChild(form);
    form.submit();
}

export function sendPostRequest_Promise(str_url, dict_parameters){
    return new Promise(function (resolve, reject) {
        var XHR = new XMLHttpRequest();
        XHR.addEventListener("load", function(event) {
            resolve(event.target.responseText)
        });
        XHR.open("POST", str_url, true);
        XHR.send(dict_parameters);
    });
}
export function sendPostRequestJson(str_url, dict){
    // Creation dynamique du formulaire
    var xhr = new XMLHttpRequest();
    xhr.open("POST", str_url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            try{
                var json = JSON.parse(xhr.responseText);
            }catch(e){console.log(e);}
        }
    };
    var data = JSON.stringify(dict);
    xhr.send(data);
}

export async function sendPostRequestJson_Promise(str_url, dict, showRequestResult){
    return new Promise(function (resolve, reject) {
        var xhr = new XMLHttpRequest();
        if(showRequestResult==null || showRequestResult==true){
            xhr.addEventListener("load", function(event) {
                try{
                    appendConsoleMessage(__ID_CONSOLE_MODAL__, event.target.responseText);
                }catch(exceptConsole){console.log(exceptConsole);}
                resolve(event.target.responseText)
            });

            // Definissez ce qui se passe en cas d'erreur
            xhr.addEventListener("error", function(err) {
                console.log("#ERR : HEADER : ")
                console.log(xhr.getAllResponseHeaders())
                reject(err);
            });
        }else{
            xhr.addEventListener("load", function(event) {
                resolve(event.target.responseText)
            });
        }
        xhr.open("post", str_url, true);
        
        xhr.setRequestHeader("Accept", "application/json");
        xhr.setRequestHeader("Content-Type", "application/json");
//        xhr.setRequestHeader("Sec-Fetch-Site", "cross-site");
//        xhr.setRequestHeader("Sec-Fetch-Mode", "no-cors");

        /*console.log("Sending");
        console.log(dict);
        console.log(xhr);*/
        var data = JSON.stringify(dict);
        xhr.send(data);
    });
}

export function sendDeleteURL_Promise(url) {
    return new Promise( function (resolve, reject) {
        var xmlHttp = new XMLHttpRequest();

        xmlHttp.open( "DELETE", url, true );
        xmlHttp.onload = function (event) {
                                resolve(event.target.responseText);     // Si Ok
                            };

        xmlHttp.onerror = function (err) {
            console.log(err);
            reject(err);                    // Si erreur
        }
        xmlHttp.send( null );
    });
}


export function sendGetWorkspaceObjectsList(){    
    return getJsonObjectFromServer("ResqmlObjectTree");
}


export function getJsonObjectFromServer(url){
    beginTask();
    return fetch(url)
    .then(response => {
        endTask();
        if (!response.ok) {
            return response.json()
                .catch(() => {
                    // Couldn't parse the JSON
                    console.log(new Error(response.status));
                    return {};
                })
                .then(({message}) => {
                    // Got valid JSON with error response, use it
                    console.log(new Error(message || response.status));
                    return {};
                });
        }
        // Successful response, parse the JSON and return the data
        return response.json();
    });
}