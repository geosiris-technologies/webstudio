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
package com.geosiris.webstudio.logs;

import com.geosiris.energyml.utils.Utils;

import javax.xml.datatype.XMLGregorianCalendar;

public class LogMessage extends JsonifyableMessage {
    protected String title, msg;
    protected XMLGregorianCalendar date;
	protected ServerLogMessage.MessageType severity;

    public LogMessage(String title, String msg, ServerLogMessage.MessageType severity) {
        this.title = title;
        this.msg = msg;
        this.date = Utils.getCalendarForNow();
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public String getMsg() {
        return msg;
    }

    public XMLGregorianCalendar getDate() {
        return date;
    }

}
