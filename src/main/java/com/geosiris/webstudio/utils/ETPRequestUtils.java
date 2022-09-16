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
package com.geosiris.webstudio.utils;

import com.geosiris.etp.utils.ETPDefaultProtocolBuilder;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ETPRequestUtils {
    public static Logger logger = LogManager.getLogger(ETPRequestUtils.class);

	public static String jsonifyParameter(Parameter param) {
		String result = "{\"name\": \"" + param.getName() + "\", "
				+ "\"typeName\" : \"" + param.getParameterizedType().getTypeName() + "\"";
		try {
			Class<?> paramClass = Class.forName(param.getParameterizedType().getTypeName());
			Object[] enumValues = paramClass.getEnumConstants();
			if(enumValues != null) {
				StringBuilder enumValues_str = new StringBuilder(", \"values\" : [");
				for(int ev_idx=0; ev_idx<enumValues.length; ev_idx++) {
					enumValues_str.append("\"").append(enumValues[ev_idx]).append("\"");
					if(ev_idx<enumValues.length-1) {
						enumValues_str.append(", ");
					}
				}
				enumValues_str.append("]");
				result += enumValues_str;
			}
		} catch (ClassNotFoundException ignore) { }
		result += "}";
		return result;
	}
	
	public static String jsonifyFunc(Method m) {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n\t\"name\" : \"");
		sb.append(m.getName());
		sb.append("\",\n\t\"return\" : \"");
		sb.append(m.getReturnType());
		sb.append("\", \n\t\"parameters\" : [");
		Parameter[] params = m.getParameters();
		for(int i=0; i<params.length; i++) {
			sb.append(jsonifyParameter(params[i]));
			if(i<params.length - 1) {
				sb.append(", ");
			}
		}
		sb.append("]");

		sb.append("\n}");
		return sb.toString();
	}
	
	public static String getAllBuildableMessages() {
		StringBuilder sb = new StringBuilder();
		int nbMethFound = 0;
		for(Method meth : ETPDefaultProtocolBuilder.class.getMethods()) {
			if(meth.getName().startsWith("build") 
					&& SpecificRecordBase.class.isAssignableFrom(meth.getReturnType())
					&& !meth.getReturnType().getName().endsWith("RequestSession")) {
				sb.append("\n\t");
				sb.append(jsonifyFunc(meth).replaceAll("\n", "\n\t"));
				sb.append(",");
				nbMethFound++;
			}
		}
		
		String res = "[" + sb;
		if(nbMethFound>0) {
			res = res.substring(0, res.length()-1);
		}
		return res + "\n]";
	}
	
	public static void main(String[] argv) {
		logger.info(getAllBuildableMessages());
		logger.info(ETPRequestUtils.class.getDeclaredMethods()[1].getName());
		logger.info(ETPRequestUtils.class.getDeclaredMethods()[1].getParameters()[0].getName());
	}
}
