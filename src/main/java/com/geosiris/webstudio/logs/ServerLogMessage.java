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

import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.webstudio.utils.SessionUtility;
import com.geosiris.webstudio.utils.Utility;

public class ServerLogMessage extends JsonifyableMessage{
	public enum MessageType {
		INFO, ERROR, DEBUG, LOG, WARNING, TOAST, ACTION
	};

	private MessageType severity;
	private String content;

	private String originator;

	public ServerLogMessage(MessageType severity, String content, String originator) {
		this.severity = severity;
		this.content = content;
		this.originator = originator;
	}

	public static ServerLogMessage parseLogMessage(LogMessage msg) {
		if (msg instanceof LogResqmlVerification) {
			LogResqmlVerification msg_cast = (LogResqmlVerification) msg;
			return new ServerLogMessage(msg_cast.severity,
					"[" + msg_cast.rootUUID + "]" + msg_cast.rootType + " '" + msg_cast.rootTitle + "' => "
							+ msg_cast.getTitle() + ": " + msg_cast.getMsg(),
					SessionUtility.EDITOR_NAME);
		} else {
			MessageType msgType = (MessageType) ObjectController.getObjectAttributeValue(msg, "severity");
			return new ServerLogMessage(msgType!=null? msgType : MessageType.WARNING, msg.getTitle() + ": " + msg.getMsg(),
					SessionUtility.EDITOR_NAME);
		}
	}

	public ServerLogMessage(LogResqmlVerification msg) {
		this.severity = MessageType.WARNING;
		this.content = "[" + msg.rootUUID + "]" + msg.rootType + " '" + msg.rootTitle + "'" + msg.getTitle() + ": "
				+ msg.getMsg();
		this.originator = SessionUtility.EDITOR_NAME;
	}

	public MessageType getSeverity() {
		return severity;
	}

	public void setSeverity(MessageType severity) {
		this.severity = severity;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getOriginator() {
		return originator;
	}

	public void setOriginator(String originator) {
		this.originator = originator;
	}

	@Override
	public String toJSON() {
		String result = "{\n"
				+ "\"severity\": \"" + severity + "\", \n"
				+ "\"originator\": \"" + originator + "\", \n"
				+ "\"message\": ";

		result += Utility.transformStringForJsonCompatibility(content) + "\n";

		/*try {
			JSONParser parser = new JSONParser(content);
			if(SessionUtility.configIsMoreVerborseThan(ConfigurationType.debug)){
				logger.info("Trying to parse json log message : ");
				logger.info(content);
			}
			Object jsonObj = parser.parse();
			if (jsonObj != null) {
				result += content + "\"\n";
			} else {
				result += "\"" + content.replace("\"", "\\\"") + "\"\n";
			}
		} catch (Exception e) {
			result += "\"" + content.replace("\"", "\\\"") + "\"\n";
		}*/
		result += "}";
		return result;
	}

	@Override
	public String toString() {
		return toJSON();
	}

}