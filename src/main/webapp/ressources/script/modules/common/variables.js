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



/**
    Usage : to set a elt id to a variable value, add the attribute : 
    rws_update_id and set its value to the variable name.

    e.g. : <div rws_update_id="__ID_CONSOLE__"></div>

    Same usage for classes : 
    e.g. : <div rws_update_class="__ID_CONSOLE__"></div>
**/

export function rws_updateIds(){
    $("[rws_update_id]" ).each( function(){
        $( this ).attr("id", eval($(this).attr("rws_update_id")));
    })
}

export function rws_updateClasses(){
    $("[rws_update_class]" ).each( function(){
        $( this ).attr("class", $( this ).attr("class") + " " + eval($(this).attr("rws_update_class")));
    })
}

$( document ).ready(function() {
    // console.log("#> rws_updateIds");
    rws_updateIds();
    rws_updateClasses();
});


/*
    ________
   /  _/ __ \
   / // / / /
 _/ // /_/ /
/___/_____/
*/

export const __ID_CONSOLE_MODAL__  = "rws__ID_CONSOLE_MODAL__";
export const __ID_CONSOLE__        = "rws__ID_CONSOLE__";
export const __CL_CONSOLE_UL__     = "rws__CL_CONSOLE_UL__";
export const __CL_CONSOLE_LI__     = "rws__CL_CONSOLE_LI__";


export const __ID_EPC_TABLE_CONTENT__  = "rws__EPC_TABLE_CONTENT__";
export const __ID_EPC_TABLE_DIV__      = "rws__EPC_TABLE_DIV__";

export const __ID_EPC_TABS_HEADER__    = "rws__EPC_TABS_HEADER__";        // Open object tabs
export const __ID_EPC_TABS_CONTAINER__ = "rws__EPC_TABS_CONTAINER__"; 

export const __CL_CONSOLE_MSG_DATE__    = "rws__CL_CONSOLE_MSG_DATE__";
export const __CL_CONSOLE_MSG_CONTENT__ = "rws__CL_CONSOLE_MSG_CONTENT__";


/*
    ________________
   / ____/_  __/ __ \
  / __/   / / / /_/ /
 / /___  / / / ____/
/_____/ /_/ /_/
*/
export const REGEX_ETP_URI = /^eml:\/\/\/(?:dataspace\('(?<dataspace>[^']*?(?:''[^']*?)*)'\)\/?)?((?<domain>[a-zA-Z]+\w+)(?<domainVersion>[1-9]\d)\.(?<objectType>\w+)(\((?:(?<uuid>(uuid=)?[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})|uuid=(?<uuid2>[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}),\s*version='(?<version>[^']*?(?:''[^']*?)*)')\))?)?(\/(?<collectionDomain>[a-zA-Z]+\w+)(?<collectionDomainVersion>[1-9]\d)\.(?<collectionType>\w+))?(?:\?(?<query>[^#]+))?$/

export const UUID_REGEX_str = '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$';
export const UUID_REGEX_raw = /(\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\b)/g;
export const UUID_REGEX = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/g;

export const REGEX_ENERGYML_FILE_NAME = /^(.*\/)?(((?<type>[\w]+)_[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})|([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\.(?<objectVersion>\d+(\.\d+)*)))\.xml$/g;



/*
 _    _____    ____  _______    ____  __    ___________       __   _______   ____  ____  ________
| |  / /   |  / __ \/  _/   |  / __ )/ /   / ____/ ___/     _/_/  / ____/ | / / / / /  |/  / ___/
| | / / /| | / /_/ // // /| | / __  / /   / __/  \__ \    _/_/   / __/ /  |/ / / / / /|_/ /\__ \
| |/ / ___ |/ _, _// // ___ |/ /_/ / /___/ /___ ___/ /  _/_/    / /___/ /|  / /_/ / /  / /___/ /
|___/_/  |_/_/ |_/___/_/  |_/_____/_____/_____//____/  /_/     /_____/_/ |_/\____/_/  /_//____/
*/

// Console Enum
export const __ENUM_CONSOLE_MSG_SEVERITY_LOG__     = "LOG";
export const __ENUM_CONSOLE_MSG_SEVERITY_INFO__    = "INFO";
export const __ENUM_CONSOLE_MSG_SEVERITY_DEBUG__   = "DEBUG";
export const __ENUM_CONSOLE_MSG_SEVERITY_WARNING__ = "WARNING";
export const __ENUM_CONSOLE_MSG_SEVERITY_ERROR__   = "ERROR";
export const __ENUM_CONSOLE_MSG_SEVERITY_TOAST__   = "TOAST";
export const __ENUM_CONSOLE_MSG_SEVERITY_ACTION__  = "ACTION";

export const __TB_MSG_SEVERITY__ = [
                                __ENUM_CONSOLE_MSG_SEVERITY_LOG__,
                                __ENUM_CONSOLE_MSG_SEVERITY_INFO__,
                                __ENUM_CONSOLE_MSG_SEVERITY_DEBUG__,
                                __ENUM_CONSOLE_MSG_SEVERITY_WARNING__,
                                __ENUM_CONSOLE_MSG_SEVERITY_ERROR__,
                                __ENUM_CONSOLE_MSG_SEVERITY_TOAST__,
                            ];

export function getSeverityEnum(severity){
    var severity_upc = severity.toUpperCase();
    for(var idx=0;idx<__TB_MSG_SEVERITY__.length; idx++){
        if(severity_upc == __TB_MSG_SEVERITY__[idx].toUpperCase()){
            return __TB_MSG_SEVERITY__[idx];
        }
    }
    return null;
}

export function getSeverityClass(base, severity){
    var sev = getSeverityEnum(severity);
    if(sev == null)
        sev = __ENUM_CONSOLE_MSG_SEVERITY_LOG__;
    return base + sev;
}

export const __MIN_PWD_SIZE__ = 8;


/*
const __CL_CONSOLE_UL__LOG      = __CL_CONSOLE_UL__ + __ENUM_CONSOLE_MSG_SEVERITY_LOG__;
const __CL_CONSOLE_UL__INFO     = __CL_CONSOLE_UL__ + __ENUM_CONSOLE_MSG_SEVERITY_INFO__;
const __CL_CONSOLE_UL__DEBUG    = __CL_CONSOLE_UL__ + __ENUM_CONSOLE_MSG_SEVERITY_DEBUG__;
const __CL_CONSOLE_UL__WARNING  = __CL_CONSOLE_UL__ + __ENUM_CONSOLE_MSG_SEVERITY_WARNING__;
const __CL_CONSOLE_UL__ERROR    = __CL_CONSOLE_UL__ + __ENUM_CONSOLE_MSG_SEVERITY_ERROR__;

const __CL_CONSOLE_LI__LOG      = __CL_CONSOLE_LI__ + __ENUM_CONSOLE_MSG_SEVERITY_LOG__;
const __CL_CONSOLE_LI__INFO     = __CL_CONSOLE_LI__ + __ENUM_CONSOLE_MSG_SEVERITY_INFO__;
const __CL_CONSOLE_LI__DEBUG    = __CL_CONSOLE_LI__ + __ENUM_CONSOLE_MSG_SEVERITY_DEBUG__;
const __CL_CONSOLE_LI__WARNING  = __CL_CONSOLE_LI__ + __ENUM_CONSOLE_MSG_SEVERITY_WARNING__;
const __CL_CONSOLE_LI__ERROR    = __CL_CONSOLE_LI__ + __ENUM_CONSOLE_MSG_SEVERITY_ERROR__;
*/


export const REGEX_HEX_COLOR = new RegExp('#(([0-9]|[a-f]){2}){3}');


// OTHER VARIABLES

export const __RWS_CLIENT_NAME__ = "WebStudio";
export const __RWS_SERVER_NAME__ = "Server";

export var __RWS_ETP_URI__ = "";
export var __RWS_ETP_LOGIN__ = "";
export var __RWS_ETP_PWD__ = "";

export function set__RWS_ETP_URI__(value){__RWS_ETP_URI__ = value;}
export function set__RWS_ETP_LOGIN__(value){__RWS_ETP_LOGIN__ = value;}
export function set__RWS_ETP_PWD__(value){__RWS_ETP_PWD__ = value;}

// Object Classes

export const CLASS_HIGHLIGHT_EXISTING_OBJECT = "highlightIfExist";
export const CLASS_HIGHLIGHT_EXISTING_OBJECT_ENABLED = CLASS_HIGHLIGHT_EXISTING_OBJECT + "_enable";

export function getObjectTableCellClass(uuid){
    return uuid + "-tab";
}

// Semaphores
export var SEM_IS_LOADING_WORKSPACE = false;
export function setSEM_IS_LOADING_WORKSPACE(value){
    SEM_IS_LOADING_WORKSPACE = value;
}


// Caches

var last_dataspace_update_time = 0;

export function dataspaces_needs_update(){
    return (new Date() - last_dataspace_update_time) / 1000 > 180; // si sup a 3 min
}

export function dataspace_reset_timer(){
    last_dataspace_update_time = 0;
}

export function dataspace_has_been_updated(){
    last_dataspace_update_time = new Date();
}