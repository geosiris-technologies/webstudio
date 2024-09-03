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

import {createCollapser} from "../UI/htmlUtils.js"
import {UUID_REGEX_raw} from "../common/variables.js"


export function isComplexObject(obj){
    return Array.isArray(obj) || typeof obj == typeof {}
}

export function addJsonData(jsObject, jsonEltVue, printListIdx, f_applyOnKey){
    if(isComplexObject(jsObject)){
        var jsObject_keys = Object.keys(jsObject);
        jsObject_keys.sort();
        jsObject_keys.forEach(i => {
            var spanTitle = null;
            if(!Array.isArray(jsObject) || printListIdx){
                // besoin d'un title
                var title = i;
                if (i.match(UUID_REGEX_raw)){
                    try{
                        title += " \"" + jsObject[i].citation.title + "\"";
                    }catch(e){}
                }
                title += " :";
                spanTitle = document.createElement("span");
                spanTitle.appendChild(document.createTextNode(title));
            }

            var eltToPutChildIn = jsonEltVue;

            if(isComplexObject(jsObject[i])){
                eltToPutChildIn = document.createElement("div")
                eltToPutChildIn.style.display = "block";
                eltToPutChildIn.style.paddingLeft = "20px";
                eltToPutChildIn.style.borderLeft = "1px dashed black";
                eltToPutChildIn.className = 'jsonTreeDiv';
                var collapser = null;
                if(spanTitle != null){
                    collapser = createCollapser(spanTitle, eltToPutChildIn);
                    jsonEltVue.appendChild(collapser);
                }else{
                    collapser = createCollapser(document.createElement("span"), eltToPutChildIn);
                    jsonEltVue.appendChild(collapser);
                }
                addJsonData(jsObject[i], eltToPutChildIn, printListIdx, f_applyOnKey);
                
                if(spanTitle == null){
                    // test children to see if collapse should not be present
                    var divFound = false;
                    const childs = [];

                    [].forEach.call(eltToPutChildIn.children, function(x){
                        childs.push(x);
                        if(x.tagName.toLowerCase() == "div")
                            divFound = true;
                    });
                    if(!divFound){
                        collapser.parentNode.appendChild(eltToPutChildIn);
                        eltToPutChildIn.style.display = '';
                        collapser.parentNode.removeChild(collapser);
                    }
                    if(f_applyOnKey != null){
                        try{
                            f_applyOnKey(i, eltToPutChildIn);
                        }catch(e){console.log(e);}
                    }
                }else{
                    if(f_applyOnKey != null){
                        try{
                            f_applyOnKey(i, spanTitle);
                        }catch(e){console.log(e);}
                    }
                }
            }else{
                if(spanTitle != null){
                    var boldSpan = document.createElement("b");
                    boldSpan.appendChild(spanTitle.firstChild);
                    spanTitle.appendChild(boldSpan);
                    spanTitle.style.display = 'block';
                    jsonEltVue.appendChild(spanTitle);
                    addJsonData(jsObject[i], spanTitle, printListIdx, f_applyOnKey);
                    if(f_applyOnKey != null){
                        try{
                            f_applyOnKey(i, spanTitle);
                        }catch(e){console.log(e);}
                    }
                }else{
                    //addJsonData(jsObject[i], eltToPutChildIn, printListIdx, f_applyOnKey);
                    var spanContent = document.createElement("span");
                    spanContent.style.display = 'block'
                    spanContent.appendChild(document.createTextNode(jsObject[i]));
                    jsonEltVue.appendChild(spanContent);
                    try{
                        f_applyOnKey(i, spanContent);
                    }catch(e){console.log(e);}
                }
            }
        });
    }else{
        var spanContent = document.createElement("span");
        spanContent.appendChild(document.createTextNode(jsObject));
        jsonEltVue.appendChild(spanContent);
    }
}