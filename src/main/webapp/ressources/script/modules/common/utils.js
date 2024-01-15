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

import {
    UUID_REGEX
} from "./variables.js"


export function rws__dateNowStr() {
    const d_now = new Date(Date.now());
    var hours = "" + d_now.getHours();
    if (hours.length < 2) hours = "0" + hours;
    var min = "" + d_now.getMinutes();
    if (min.length < 2) min = "0" + min;
    var sec = "" + d_now.getSeconds();
    if (sec.length < 2) sec = "0" + sec;


    return hours + ":" + min + ":" + sec;
}

export function generateUUID() {
    // [0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}
    var result = "";
    var characters = 'abcdef0123456789';

    var charactersLength = characters.length;

    var sequences = [8, 4, 4, 4, 12]

    for (var s = 0; s < sequences.length; s++) {
        for (var i = 0; i < sequences[s]; i++) {
            result += characters.charAt(Math.floor(Math.random() * charactersLength));
        }
        result += "-"
    }
    return result.substring(0, result.length - 1)
}

export function isUUID(input) {
    return input != null && input.match(UUID_REGEX) != null;
}

export function isResqmlListType(typeName) {
    return typeName.endsWith("List"); // ou Collection
}

export function isResqmlFinalElt(elt) {
    return (elt.attributes == null || elt.attributes.length <= 0) &&
        (elt.properties == null || elt.properties.length <= 0) &&
        !isResqmlListType(elt.type); // || elt.type.includes("XMLGregorianCalendar");
}

export function getAttribute(obj, att) {
    var paths = att.split(".");
    if (paths.length > 1) {
        return getAttribute(obj[paths[0]], paths.slice(1).join("."))
    } else if (paths.length == 1) {
        return obj[paths[0]]
    } else {
        return null;
    }
}

export function rgbToString(r, g, b) {
    var rs = Math.round(r * 255).toString(16);
    var gs = Math.round(g * 255).toString(16);
    var bs = Math.round(b * 255).toString(16);

    if (rs.length() < 2) {
        rs = "0" + rs;
    }
    if (gs.length() < 2) {
        gs = "0" + gs;
    }
    if (bs.length() < 2) {
        bs = "0" + bs;
    }
    return "#" + rs + gs + bs;
}

export function hsvToRgb(hue, saturation, value) {
    // Hue must be [0;360]
    if (saturation > 1) // [0;1] or [0;100]
        saturation = saturation * 0.01;
    if (value > 1) // [0;1] or [0;100]
        value = value * 0.01;

    var h = (hue / 60);
    var C = value * saturation;
    var X = C * (1. - Math.abs((h % 2) - 1.));

    var m = value - C;

    if (h < 1)
        return rgbToString(C + m, X + m, 0 + m);
    else if (h < 2)
        return rgbToString(X + m, C + m, 0 + m);
    else if (h < 3)
        return rgbToString(0 + m, C + m, X + m);
    else if (h < 4)
        return rgbToString(0 + m, X + m, C + m);
    else if (h < 5)
        return rgbToString(X + m, 0 + m, C + m);
    else
        return rgbToString(C + m, 0 + m, X + m);
}

export function compareVersionNumber(va, vb){
    const va_array = va.split(/[\.\_]/);
    const vb_array = vb.split(/[\.\_]/);

    //console.log("compare " + va + " and " + vb);
    var idx = 0;
    var aIsDev = false;
    var bIsDev = false;

    if(va_array[0].startsWith("dev")){
        aIsDev = true;
        va_array.shift();
    }
    if(vb_array[0].startsWith("dev")){
        bIsDev = true;
        vb_array.shift();
    }

    while(idx < Math.min(va_array.length, vb_array.length)){
        var comp = parseInt(va_array[idx], 10) - parseInt(vb_array[idx], 10);
        //console.log("\tcompare " + va_array[idx] + " and " + vb_array[idx] + " : " + comp);
        if(comp != 0) return comp;
        idx++;
    }
    if(va_array.length == vb_array.length){
        if(aIsDev == bIsDev){
            return 0;
        }else{
            return aIsDev? -1 : 1;
        }
    } 
    else if(idx>va_array.length) return 1;
    else return -1;
}