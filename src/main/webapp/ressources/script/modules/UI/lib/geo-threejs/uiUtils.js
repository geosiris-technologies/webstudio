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
export function createRadio(name, label, value, checked=false, inline=true){
    var radioGrp = document.createElement("div");
    radioGrp.className = "form-check";

    var radio = document.createElement("input");
    radio.id = "radio_" + Date.now();
    radio.type = "radio";
    radio.className = "form-check-input";
    radio.name = name;
    radio.value = value;

    if(checked){
        radio.checked = "checked" ;
    }


    var lbl = document.createElement("label");
    lbl.appendChild(document.createTextNode(label));
    //lbl.style.marginLeft = "2px";

    radioGrp.appendChild(radio);
    radioGrp.appendChild(lbl);
    radioGrp.style.marginLeft = "5px";

    if(inline){
        radioGrp.style.display = "inline";
    }

    return radioGrp;
}

export function createToggler(on_icon_class, color_on, color_off, on_callback=null, off_callback=null, defaultIsOn = true){
    const toggler = document.createElement("i");
    toggler.style.margin = "2px";
    toggler.className = on_icon_class;
    toggler.style.cursor = "pointer";
    toggler.style.color = defaultIsOn ? color_on : color_off;

    toggler.toggleValue = defaultIsOn;

    toggler.toggle = function(value){
        if(value == null){
            value = ! toggler.style.color.includes(color_on);
        }

        if(value){
            toggler.style.color = color_on;
            toggler.title = "click to disable";
        }else{
            toggler.style.color = color_off;
            toggler.title = "click to enable";
        }
    }

    toggler.onclick = function(){
        toggler.toggleNoCallback();
        if(toggler.isToggled()){
            if(on_callback != null)
                on_callback();
        }else{
            if(off_callback != null)
                off_callback();
        }
    }

    toggler.isToggled = function(){
        return toggler.toggleValue;
    }

    toggler.toggleNoCallback = function(value = !toggler.toggleValue){
        toggler.toggleValue = value;
        if(toggler.toggleValue){
            toggler.style.color = color_on;
            toggler.title = "click to disable";
        }else{
            toggler.style.color = color_off;
            toggler.title = "click to enable";
        }
    }

    return toggler;
}

export function createScaler(){
    var in_scaler = document.createElement("input");
    in_scaler.style.textAlign = "right";
    in_scaler.style.display = "inline";
    in_scaler.style.cursor = 'pointer';
    in_scaler.style.margin = "2px";
    in_scaler.style.width = "100px";
    in_scaler.style.height = "25px";
    in_scaler.style.paddingRight = '1px';
    in_scaler.className = "form-control";
    in_scaler.type = "number";
    in_scaler.min = "0.1";
    in_scaler.max = "20";
    in_scaler.step = "0.1";
    in_scaler.value = "1.0";
    return in_scaler;
}