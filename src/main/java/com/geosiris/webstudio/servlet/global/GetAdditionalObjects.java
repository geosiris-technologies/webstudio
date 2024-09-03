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
package com.geosiris.webstudio.servlet.global;

import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.model.WorkspaceContent;
import com.geosiris.webstudio.utils.HttpSender;
import com.geosiris.webstudio.utils.SessionUtility;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet implementation class GetAdditionalObjects
 * Allows to get all additional objects (no in the workspace), that are commonly
 * used by resqml.
 * For example, it allows to get all PropertyKind from the commonly used
 * dictionnary
 */
@WebServlet("/GetAdditionalObjects")
public class GetAdditionalObjects extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public static Logger logger = LogManager.getLogger(GetAdditionalObjects.class);
	public static Map<String, Object> ADDITIONAL_ENERGYML_OBJECTS = readAdditionalObjects(
			SessionUtility.wsProperties.getPathToAdditionalObjectsDir());

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetAdditionalObjects() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 * response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!SessionUtility.tryConnectServlet(request, response)) {
			return;
		}

		Gson gson = new Gson();

		PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(gson.toJson(getExternalProperties()));
        out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 * response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!SessionUtility.tryConnectServlet(request, response)) {
			return;
		}
		doGet(request, response);
	}

	public static Map<String, Object> readAdditionalObjects(String additionalDataFolderPath) {
		logger.error("Try to read additional data in folder " + additionalDataFolderPath);
		Map<String, Object> result = new HashMap<>();
		if (additionalDataFolderPath != null) {
			try {
				File dir = new File(additionalDataFolderPath);
				if (dir.exists() && dir.isDirectory()) {
					String[] filesPath = dir.list();
					if (filesPath != null) {
						for (String fPath : filesPath) {
							File f = new File(additionalDataFolderPath + "/" + fPath);
							if (f.exists() && f.isFile()) {
								WorkspaceContent readed = HttpSender
										.readFile(null, new FileInputStream(f), f.getName());
								result.putAll(readed.getReadObjects());
								for (Pair<String, byte[]> notReadedFiles : readed.getNotReadObjects()) {
									logger.error("[Additional data reading : ] Not read file : " + notReadedFiles.l());
								}
							} else {
								result.putAll(readAdditionalObjects(additionalDataFolderPath + "/" + fPath));
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return result;
	}

	public static Map<String, Object> getExternalProperties() {
		Map<String, Object> properties = new HashMap<>();

		for (Object p_dict : ADDITIONAL_ENERGYML_OBJECTS.values()) {
			try {
				List<Object> props = (List<Object>) ObjectController.getObjectAttributeValue(p_dict, "propertyKind");
				for (Object p : props) {
					try {
						properties.put((String) ObjectController.getObjectAttributeValue(p, "uuid"), p);
					} catch (Exception e) {
						logger.debug(e.getMessage(), e);
					}
				}
			} catch (Exception e) {
				logger.debug(e.getMessage(), e);
			}
		}
		return properties;
	}
}
