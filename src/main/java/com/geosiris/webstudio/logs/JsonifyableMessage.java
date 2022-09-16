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
import com.geosiris.webstudio.utils.Utility;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public abstract class JsonifyableMessage {
    public String toJSON() {
		StringBuilder json = new StringBuilder("{\n");
		List<Field> fieldList = ObjectController.getAllFields(this.getClass());
		for(int fi=0; fi<fieldList.size()-1; fi++){
			Field f = fieldList.get(fi);
			try {
				Method m = ObjectController.getAttributeAccessMethod(this, f.getName());
				if(m!=null) {
					json.append("\"").append(f.getName()).append("\" :").append(Utility.transformStringForJsonCompatibility(m.invoke(this) + "")).append(",");
				}
			} catch (Exception ignore){}
		}
		if(fieldList.size()>0) {
			Field f = fieldList.get(fieldList.size()-1);
			try {
				Method m = ObjectController.getAttributeAccessMethod(this, f.getName());
				if(m!=null) {
					json.append("\"").append(f.getName()).append("\" :").append(Utility.transformStringForJsonCompatibility(m.invoke(this) + ""));
				}
			} catch (Exception ignore){}
		}

		json.append("}");
		return json.toString();
	}

	public static String toJSON(List<? extends JsonifyableMessage> msgList) {
		StringBuilder jsonContent = new StringBuilder("[");
		if(msgList!=null) {
			for(int i=0; i<msgList.size()-1; i++) {
				jsonContent.append(msgList.get(i).toJSON()).append(",\n");
			}
			if(msgList.size()>0) {
				jsonContent.append(msgList.get(msgList.size() - 1).toJSON()).append("\n");
			}
		}
		jsonContent.append("]");
		return jsonContent.toString();
	}
}
