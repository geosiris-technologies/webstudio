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

import {__MIN_PWD_SIZE__} from "./variables.js"


export function hasLowerCase(str) {
    return (/[a-z]/.test(str));
}
export function hasUpperCase(str) {
    return (/[A-Z]/.test(str));
}
export function hasNumber(str) {
    return (/[0-9]/.test(str));
}
export function isValidPassword(str){
    return hasLowerCase(str) && hasUpperCase(str) && hasNumber(str) && str.length >= __MIN_PWD_SIZE__;
}

export function sendUserFormWithPasswordValidation(form, passwordList, elt_log){
    if(passwordList.length>1){
        if(passwordList[0].value.localeCompare(passwordList[1].value) == 0){
            /*console.log(passwordList[0].value + " --> " + hasLowerCase(passwordList[1].value) +" "
                        + hasUpperCase(passwordList[1].value) +" "+ hasNumber(passwordList[1].value) +" "
                        + (passwordList[1].value.length >= __MIN_PWD_SIZE__) );*/

            if(passwordList[0].value.length <= 0 || isValidPassword(passwordList[0].value)){
                form.submit();
            }else{
                var test_lowerC = hasLowerCase(passwordList[0].value);
                var test_upperC = hasUpperCase(passwordList[0].value);
                var test_number = hasNumber(passwordList[0].value);
                var test_size   = passwordList[0].value.length >= __MIN_PWD_SIZE__


                var countMiss = (test_lowerC?0:1) + (test_upperC?0:1) + (test_number?0:1) + (test_size?0:1);
                var currentCount = 0;
                var msg = "Password ";
                if(!test_size){
                    msg += "size must be greater than " + __MIN_PWD_SIZE__;
                    currentCount++;
                    if(currentCount<countMiss){
                        msg += ", "
                    }
                }
                if(!test_lowerC || !test_upperC || !test_number){
                    msg += "must contains at least ";
                }
                if(!test_lowerC){
                    msg += "a lower case letter"
                    currentCount++;
                    if(currentCount<countMiss){
                        msg += ", "
                    }
                }
                
                if(!test_upperC){
                    msg += "an upper case letter"
                    currentCount++;
                    if(currentCount<countMiss){
                        msg += ", "
                    }
                }
                if(!test_number){
                    msg += "a numeric value"
                    currentCount++;
                }
                msg += "."

                elt_log.style.display = "";
                elt_log.innerHTML = msg;
            }
        }else{
            elt_log.style.display = "";
            elt_log.innerHTML = "Password confirmation mismatch";
        }
    }else{
        elt_log.style.display = "none";
        elt_log.innerHTML = "";
    }
}