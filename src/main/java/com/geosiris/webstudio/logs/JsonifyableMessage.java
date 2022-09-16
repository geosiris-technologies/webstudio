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
		String json = "{\n";
		List<Field> fieldList = ObjectController.getAllFields(this.getClass());
		for(int fi=0; fi<fieldList.size()-1; fi++){
			Field f = fieldList.get(fi);
			Method m = null;
			try {
				m = ObjectController.getAttributeAccessMethod(this, f.getName());

				if(m!=null) {
					json += "\"" + f.getName() + "\" :" + Utility.transformStringForJsonCompatibility(m.invoke(this) + "" ) + ",";
				}
			} catch (Exception ignore){}
		}
		if(fieldList.size()>0) {
			Field f = fieldList.get(fieldList.size()-1);
			Method m = null;
			try {
				m = ObjectController.getAttributeAccessMethod(this, f.getName());

				if(m!=null) {
					json += "\"" + f.getName() + "\" :" + Utility.transformStringForJsonCompatibility(m.invoke(this) + "");
				}
			} catch (Exception ignore){}
		}

		json +=  "}";
		return json;
	}

	public static String toJSON(List<? extends JsonifyableMessage> msgList) {
		String jsonContent = "[";
		if(msgList!=null) {
			for(int i=0; i<msgList.size()-1; i++) {
				jsonContent += msgList.get(i).toJSON() + ",\n";
			}
			if(msgList.size()>0) {
				jsonContent += msgList.get(msgList.size()-1).toJSON() + "\n";
			}
		}
		jsonContent += "]";
		return jsonContent;
	}
}
