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
import {add_mouse_event_listener} from "./htmlUtils.js"
import {getAttribute} from "../common/utils.js"


export class JsonTableColumnizer{
    constructor(title, mouseListener, mouseListener_title, f_className, f_className_title, cursor) {
        this.title = title;
        this.mouseListener = mouseListener;
        this.mouseListener_title = mouseListener_title;
        this.f_className = f_className;
        this.f_className_title = f_className_title;
        this.cursor = cursor;
    }
    getDomElt(obj, parent){
        var elt = this._getDomElt(obj);
        if(this.cursor != null){
            elt.style.cursor = this.cursor;   
        }
        if(parent != null){
            if(this.mouseListener != null){
                add_mouse_event_listener(parent, (event) => this.mouseListener(event, obj));
            }
            if(this.f_className != null){
                parent.className += " " + this.f_className(obj);
            }
        }else{
            if(this.mouseListener != null){
                add_mouse_event_listener(elt, (event) => this.mouseListener(event, obj));
            }
            if(this.f_className != null){
                elt.className += " " + this.f_className(obj);
            }
        }
        return elt;
    }
    getColumnTitleElt(){
        var elt = this._getColumnTitleElt(); 
        if(this.mouseListener_title != null){
            add_mouse_event_listener(elt, (event) => this.mouseListener_title(event, this));
        }
        if(this.f_className_title != null){
            elt.className += " " + this.f_className_title(obj);
        }
        return elt;
    }

    getSortAttribute(){return null;} // TODO: redefine
    _getDomElt(obj){ return document.createTextNode(obj); } // TODO: redefine
    _getColumnTitleElt(){ return document.createTextNode(this.title!=null?this.title:""); } // TODO: redefine
}

export class JsonTableColumnizer_DotAttrib extends JsonTableColumnizer{
    constructor(title, attrib, mouseListener, mouseListener_title, f_className, f_className_title, cursor) {
        super(title, mouseListener, mouseListener_title, f_className, f_className_title, cursor);
        this.attrib = attrib;
    }
    getSortAttribute(){return this.attrib;}
    _getDomElt(obj){ 
        var span = document.createElement("span");
        span.appendChild(document.createTextNode(getAttribute(obj, this.attrib)));
        return span;
    }
    _getColumnTitleElt(){ return document.createTextNode(this.title); }
}

export class JsonTableColumnizer_Icon extends JsonTableColumnizer{
    constructor(iconClassCode, iconClassCodeHovered, mouseListener, mouseListener_title, f_className, f_className_title) {
        super(null, mouseListener, mouseListener_title, f_className, f_className_title);
        this.iconClassCode = iconClassCode;
        this.iconClassCodeHovered = iconClassCodeHovered;
    }
    getSortAttribute(){return this.attrib;}
    _getDomElt(obj){
        const const_this = this;
        const iconBut = document.createElement("i");
        iconBut.className = this.iconClassCode;
        iconBut.style.cursor = "pointer";
        iconBut.onmouseover= function(){iconBut.className = iconBut.className.replace(new RegExp(const_this.iconClassCode, "g"), const_this.iconClassCodeHovered);}
        iconBut.onmouseout = function(){iconBut.className = iconBut.className.replace(new RegExp(const_this.iconClassCodeHovered, "g"), const_this.iconClassCode);}
        return iconBut;
    }
}

export class JsonTableColumnizer_Checkbox extends JsonTableColumnizer{
    constructor(name, f_value, onChange) {
        super(null, null, null, null, null);
        this.f_value = f_value;
        this.name = name;
        this.onChange = onChange;
        this.class_id = (Math.random() + 1).toString(36).substring(7);
    }
    _getDomElt(obj){
        const check_obj = document.createElement("input");
        check_obj.type = "checkbox";
        check_obj.value = this.f_value(obj);
        check_obj.name = this.name;
        check_obj.className = this.class_id;
        check_obj.addEventListener('change', 
            (event) => {
                var check_title = document.getElementById(this.class_id + "-title");
                check_title.update();
                if(this.onChange != null){
                    this.onChange(check_obj.checked, obj);
                }
            }
        )
        return check_obj;
    }
    _getColumnTitleElt(){
        const const_this = this;

        const div_check = document.createElement("div");
        div_check.className = "form-check";

        const check_title = document.createElement("input");
        div_check.appendChild(check_title);
        check_title.className = "form-check-input";
        check_title.type = "checkbox";
        check_title.name = this.name;
        check_title.id = this.class_id + "-title";
        
        const check_title_label = document.createElement("label");
        div_check.appendChild(check_title_label);
        check_title_label.className = "form-check-label";
        check_title_label.appendChild(document.createTextNode("[0]"));

        /* Title checkbox is toggled */
        check_title.addEventListener('change', 
            (event) => {
                var parent = check_title;
                while(parent.tagName.toLowerCase() != "table" && parent.parent != null){
                    parent = parent.parent;
                }
                if (parent.tagName.toLowerCase() != "table"){
                    var countChecked = 0;
                    Array.prototype.forEach.call(document.getElementsByClassName(const_this.class_id),
                        (c) => {
                            c.checked = check_title.checked;
                            countChecked += c.checked ? 1 : 0;
                        }
                    )
                    while(check_title_label.firstChild){
                        check_title_label.firstChild.remove()
                    }
                    check_title_label.appendChild(document.createTextNode("[" + countChecked + "]"));
                }
            }
        );

        check_title.update = function(){
            var parent = check_title;
            while(parent.tagName.toLowerCase() != "table" && parent.parent != null){
                parent = parent.parent;
            }
            if (parent.tagName.toLowerCase() != "table"){
                var countChecked = 0;
                var countNotChecked = 0;
                Array.prototype.forEach.call(document.getElementsByClassName(const_this.class_id),
                    (c) => {
                        countChecked += c.checked ? 1 : 0;
                        countNotChecked += c.checked ? 0 : 1;
                    }
                )
                while(check_title_label.firstChild){
                    check_title_label.firstChild.remove()
                }
                check_title.checked = (countNotChecked == 0);
                check_title_label.appendChild(document.createTextNode("[" + countChecked + "]"));
            }
        }

        return div_check;
    }
}

export class JsonTableColumnizer_Radio extends JsonTableColumnizer{
    constructor(name, f_value) {
        super(null, null, null, null, null);
        this.f_value = f_value;
        this.name = name;
        this.class_id = (Math.random() + 1).toString(36).substring(7);
    }
    _getDomElt(obj){
        const radio_obj = document.createElement("input");
        radio_obj.type = "radio";
        radio_obj.value = this.f_value(obj);
        radio_obj.name = this.name;
        radio_obj.className = this.class_id;
        return radio_obj;
    }
    _getColumnTitleElt(){
        const const_this = this;
        const radio_title = document.createElement("input");
        radio_title.type = "radio";
        radio_title.value = "";
        radio_title.name = this.name;
        radio_title.id = this.class_id + "-title";
        return radio_title;
    }
}

export function _toTable_body(
    jsonDataList,
    list_JsonTableColumnizer
){
    const _table_body = document.createElement("tbody");
    jsonDataList.forEach(
        elt =>{
            const _table_line = document.createElement("tr");
            _table_line.object = elt;
            _table_body.appendChild(_table_line);
            const const_elt = elt;
            list_JsonTableColumnizer.forEach(
                (f_col) => {
                    const _table_line_col = document.createElement("td");
                    _table_line.appendChild(_table_line_col);
                    //_table_line_col.className += " colName_" + key;
                    _table_line_col.appendChild(f_col.getDomElt(const_elt, _table_line_col));
                    _table_line_col.title = _table_line_col.textContent;
                }
            );
        }
    );
    return _table_body;
}

export function toTable(
    jsonDataList,
    list_JsonTableColumnizer
){
    const _table = document.createElement("table");
    _table.className = "table-striped table-bordered table-hover table-fixed table-top-fixed";

    const _table_head = document.createElement("thead");
    _table.appendChild(_table_head);
    const _table_head_line = document.createElement("tr");
    _table_head.appendChild(_table_head_line);

    list_JsonTableColumnizer.forEach(
        (f_col) => {
            const _table_head_cell = document.createElement("th");
            _table_head_cell.appendChild(f_col.getColumnTitleElt());
            _table_head_line.appendChild(_table_head_cell);

            // computation attributes
            _table_head_cell._sorted = false;

            const sort_attrib = f_col.getSortAttribute();
            if(sort_attrib != null){
                // click
                _table_head_cell.onclick = function(){
                    _table_head_cell._sorted = !_table_head_cell._sorted;
                    var t_body = _table.querySelector("tbody");
                    var lines = t_body.childNodes;
                    var linesArr = [];
                    for (var i in lines) {
                        if (lines[i].nodeType == 1) { // get rid of the whitespace text nodes
                            linesArr.push(lines[i]);
                        }
                    }

                    if (!_table_head_cell._sorted){
                        linesArr.sort(function(a, b) {
                            return getAttribute(b.object, sort_attrib).localeCompare(getAttribute(a.object, sort_attrib));
                        });
                    }else{
                        linesArr.sort(function(a, b) {
                          return getAttribute(a.object, sort_attrib).localeCompare(getAttribute(b.object, sort_attrib));
                        });
                    }
                    for (i = 0; i < linesArr.length; ++i) {
                        t_body.appendChild(linesArr[i]);
                    }
                }
            }
        }
    );

    const _table_body = _toTable_body(
        jsonDataList,
        list_JsonTableColumnizer
    );

    _table.appendChild(_table_body);
    
    return _table;
}


/*
export function sample(){
    var in_text = JSON.parse(document.getElementById("content").value);

    var div_result = document.getElementById("result");
    while (div_result.firstChild) {
        div_result.removeChild(div_result.firstChild);
    }

    const f_cols = []
    
    f_cols.push(new JsonTableColumnizer_Checkbox("sample_check", (obj) => getAttribute(obj, "uuid")));
    f_cols.push(new JsonTableColumnizer_Radio("sample_radio", (obj) => getAttribute(obj, "uuid")));
    f_cols.push(new JsonTableColumnizer_Icon("far fa-trash-alt ", "fas fa-trash-alt "));

    const attrib_list = ["title", "uuid", "package"];
    attrib_list.forEach(
        (attrib) => {
            f_cols.push(
                new JsonTableColumnizer_DotAttrib(
                    attrib.substring(0, 1).toUpperCase() + attrib.substring(1),
                    attrib,
                    function(event, elt){
                        if(event.type == "click"){
                            console.log(event);
                            console.log(elt);
                        }
                    },
                    null,
                    (elt)=>elt["uuid"]+ "-tab",
                    null,
                    "pointer"
                )
            );
        }
    );

    new_table = toTable(in_text, f_cols);
    new_table.className += " table-striped table-bordered table-hover table-fixed table-top-fixed";
    new_table.id = "epcTableContent";
    div_result.appendChild(new_table);
}
*/