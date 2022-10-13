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

import {UUID_REGEX} from "./variables.js"


export function rws__dateNowStr(){
    const d_now = new Date(Date.now());
    var hours = "" + d_now.getHours();
    if(hours.length<2) hours = "0" + hours;
    var min   = "" + d_now.getMinutes();
    if(min.length<2) min = "0" + min;
    var sec   = "" + d_now.getSeconds();
    if(sec.length<2) sec = "0" + sec;


    return hours+":"+min+":"+sec;
}

export function generateUUID(){
    // [0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}
    var result = "";
    var characters = 'abcdef0123456789';

    var charactersLength = characters.length;

    var sequences = [8, 4, 4, 4, 12]

    for ( var s = 0; s < sequences.length; s++ ) {
        for ( var i = 0; i < sequences[s]; i++ ) {
            result += characters.charAt(Math.floor(Math.random() *  charactersLength));
        }
        result += "-"
    }
    return result.substring(0, result.length - 1)
}

export function isUUID(input){
    return input != null && input.match(UUID_REGEX) != null;
}

export function isResqmlListType(typeName){
    return typeName.endsWith("List"); // ou Collection
}

export function isResqmlFinalElt(elt){
    return (elt.attributes == null || elt.attributes.length<=0)
    && (elt.properties == null || elt.properties.length<=0)
    && !isResqmlListType(elt.type)
    ;// || elt.type.includes("XMLGregorianCalendar");
}