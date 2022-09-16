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

import java.util.ArrayList;
import java.util.List;

public class ServerLogger {
	protected List<Object> logs;
	
	public ServerLogger() {
		logs = new ArrayList<Object>();
	}
	
	public void print(Object log) {
		logs.add(log);
	}
	
	public List<Object> getLogs(){
		return logs;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		for(Object log : logs) {
			str.append(log + "");
		}
		
		return str.toString();
	}
}
