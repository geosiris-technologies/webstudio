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
import {__CL_TOAST_CONTAINER__} from "../common/variables.js"


var geosiris_SNACKBAR_COUNT = 0;

const geosiris_separatorHeight = 5;
const geosiris_bottomOffset = 30;

export function createSnackBar(msg, _timeout){
    geosiris_SNACKBAR_COUNT += 1;
    const snack = document.createElement("div");
    snack.className = "snackbar";
    //console.log("snacking")
    if(typeof(msg)===typeof(""))
        snack.appendChild(document.createTextNode(msg));
    else
        snack.appendChild(msg);

    if(_timeout==null){
        _timeout = 4000;
    }


    snack.style.opacity = 0.1;
    snack.style.bottom = (geosiris_bottomOffset + _getTotalSnackStackHeight()) + "px";
    document.body.appendChild(snack);


    var animDelay = 1000;
    var viewDelay = _timeout > 0 ? _timeout : 2000;

    snack.animate({
        opacity: [ 0, 0.9, 1 ],
        offset: [ 0, 0.8 ], // Shorthand for [ 0, 0.8, 1 ]
        easing: [ 'ease-in', 'ease-out' ],
    }, animDelay/2);
    setTimeout(function(){snack.style.opacity = 1;}, animDelay/2-animDelay/20);
    setTimeout(
        function (){
            snack.animate({
                opacity: [ 1, 0.9, 0 ],
                offset: [ 0, 0.8 ], // Shorthand for [ 0, 0.8, 1 ]
                easing: [ 'ease-in', 'ease-out' ]
            }, animDelay);
        }, animDelay + viewDelay);
    
    if(_timeout > 0){
        setTimeout(function(){snack.style.opacity = 0;}, 2*animDelay + viewDelay - animDelay/20);
        setTimeout(function(){_removeSnackBar(snack);}, 2*animDelay + viewDelay + 10);
    }
};

export function _removeSnackBar(elt_snack){
    elt_snack.remove();
    var snacks = document.getElementsByClassName("snackbar");
    var cpt = 0;
    var sumHeight = 0;
    [].forEach.call(snacks, (sn => {
        try{
            sn.style.bottom = (geosiris_bottomOffset + sumHeight + geosiris_separatorHeight) + "px";
            sumHeight += sn.offsetHeight + geosiris_separatorHeight;
            cpt++;
        }catch(e){}
    }));

    //var snacks = document.getElementsByClassName("input-group-prepend");
}

export function _getTotalSnackStackHeight(){
    var snacks = document.getElementsByClassName("snackbar");
    var sumHeight = 0;
    [].forEach.call(snacks, (sn => {
        try{
            sumHeight += sn.offsetHeight + geosiris_separatorHeight;
        }catch(e){}
    }));
    return sumHeight;
}

/* Bootstrap toast */

function htmlToNodes(html) {
    const template = document.createElement('template');
    template.innerHTML = html;
    return template.content.childNodes;
}

const __HTML_TOAST_CONTAINER__ = `<div class="toast-container position-absolute bottom-0 end-0 p-3" style="z-index: 3000"></div>`;
const __HTML_RECT_BLUE__ = `<svg class="bd-placeholder-img rounded me-2" width="20" height="20" xmlns="http://www.w3.org/2000/svg" aria-hidden="true" preserveAspectRatio="xMidYMid slice" focusable="false"><rect width="100%" height="100%" fill="#007aff"></rect></svg>`;
//htmlToNodes(__HTML_TOAST_CONTAINER__).forEach( e => document.body.appendChild(e));

var __HTML_TOAST__ = `<div class="toast" role="alert" aria-live="assertive" aria-atomic="true">
  <div class="toast-header">
    __ICON__
    <strong class="me-auto">__TITLE__</strong>
    <small class="text-muted">__TIME__</small>
    <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
  </div>
  <div class="toast-body">__BODY__</div>
</div>
`;


function elementToString(elt){
    if(typeof(elt) == "string"){
        return elt
    }
    return elt.outerHTML;
}

/*function createToast(
    container=document.getElementById(__CL_TOAST_CONTAINER__),
    iconElt=__HTML_RECT_BLUE__,
    title="Notification", time="",
    body="<span>Nothing to say</span>",
    option = {
      animation: true,
      autohide: true,
      delay: 10000
    }
){*/
export function createToast(
    {
        container = document.getElementById(__CL_TOAST_CONTAINER__),
        iconElt = __HTML_RECT_BLUE__,
        title = "Notification",
        time = "",
        body = "<span>Nothing to say</span>",
        option = {
          animation: true,
          autohide: true,
          delay: 10000
        },
        identifier=""
    }={}
){
    var toaster = __HTML_TOAST__;
    toaster = toaster.replace("__ICON__", elementToString(iconElt));
    toaster = toaster.replace("__TITLE__", elementToString(title));
    toaster = toaster.replace("__TIME__", elementToString(time));
    toaster = toaster.replace("__BODY__", elementToString(body));

    const telt = htmlToNodes(toaster);
    telt.forEach( e => {
        const constE = e;
        container.appendChild(e);
        (new bootstrap.Toast(e, option)).show();
        e.id=identifier;
        e.addEventListener('hidden.bs.toast ', function () {
            console.log(constE.parent);
            alert("Shown bs Toast called");
            constE.remove();
            console.log(constE.parent);
        })
    });
    return telt;
}