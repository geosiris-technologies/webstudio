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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ServerFileLogger extends ServerLogger {
    public static Logger logger = LogManager.getLogger(ServerFileLogger.class);
	private String filePath;

	public ServerFileLogger(String path) {
		filePath = path;
		logger.info("Creating filelogger at path " + filePath);
		File fDirs = new File(filePath.substring(0, Math.max(filePath.lastIndexOf("/"), filePath.lastIndexOf("\\"))));
		File f = new File(filePath);

		fDirs.mkdirs();
		try {
			f.createNewFile();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(f);
			output.write("Debut de fichier".getBytes());
			output.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void print(Object log) {
		logs.add(log);
		File f = new File(filePath);
		if(!f.exists()) {
			f.mkdirs();
			try {
				f.createNewFile();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(f);
			output.write((log+"").getBytes());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		if(output!=null) {
			try {
				output.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
