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
package com.geosiris.webstudio.servlet.energyml;

import com.geosiris.energyml.pkg.EPCPackageManager;
import com.geosiris.webstudio.servlet.Editor;
import com.geosiris.webstudio.utils.SessionUtility;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet implementation class FirstServlet
 */
@WebServlet("/EPCExtTypesAttributes")
public class EPCExtTypesAttributes extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static Logger logger = LogManager.getLogger(EPCExtTypesAttributes.class);

	public static final Map<String, String> extTypesAttributes = getExtTypes();

	public static Map<String, String> getExtTypes() {
		try {
			return Editor.pkgManager.getExtTypesAsJson(SessionUtility.wsProperties.getDirPathToExtTypes());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new HashMap<>();
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EPCExtTypesAttributes() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!SessionUtility.tryConnectServlet(request, response)) {
			return;
		}
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			out.write(new Gson().toJson(extTypesAttributes));
		} catch (Exception e) {
			logger.error(extTypesAttributes);
			logger.error(e.getMessage(), e);
			out.write("{}");
		}
		out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public static void main(String[] argv) {
		logger.info(Editor.pkgManager.getExtTypesAsJson(EPCPackageManager.DEFAULT_EXT_TYPES_ATTRIBUTES_FOLDER_PATH));
	}

}
