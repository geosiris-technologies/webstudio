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

import java.util.List;
import java.util.stream.Collectors;

public class LogResqmlVerification extends LogMessage{
	protected String rootUUID;
	protected String rootTitle;
	protected String rootType;

	public LogResqmlVerification(String title, String msg, String rootUUID, 
								String rootTitle, String rootType, ServerLogMessage.MessageType severity) {
		super(title, msg, severity);
		this.rootUUID = rootUUID;
		this.rootTitle = rootTitle;
		this.rootType = rootType;
		this.severity = severity;
	}
	
	public static String verificationsToJSON(List<LogMessage> list) {
		if(list!=null)
			return toJSON(list.stream()
								.filter(l -> l instanceof LogResqmlVerification)
								.collect(Collectors.toList()));
		else return "[]";
	}

	public String getRootUUID() {
		return rootUUID;
	}

	public String getRootTitle() {
		return rootTitle;
	}
	
	public String getRootType() {
		return rootType;
	}

	public ServerLogMessage.MessageType getSeverity() {
		return severity;
	}

}
