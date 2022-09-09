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
package com.geosiris.webstudio.servlet;

import com.geosiris.energyml.pkg.EPCPackageManager;
import com.geosiris.webstudio.property.WorkspaceProperties;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Servlet implementation class FirstServlet
 */
@WebServlet("/editor")
public class Editor extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(Editor.class);

	public static EPCPackageManager pkgManager = new EPCPackageManager(Editor.pkgManager.DEFAULT_XSD_COMMENTS_FOLDER_PATH.replace("config/", "config/data/"),
																		Editor.pkgManager.DEFAULT_ACCESSIBLE_DOR_FILE_PATH.replace("config/", "config/data/"),
																		SessionUtility.wsProperties.getFpathToXSDMapping());

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Editor() {
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

		WorkspaceProperties as = new WorkspaceProperties();
		HttpSession session = request.getSession(false);
		String userName = (String) session.getAttribute(SessionUtility.SESSION_USER_NAME);

		request.setAttribute("login", userName);
		request.setAttribute("user_grp", session.getAttribute(SessionUtility.SESSION_USER_GRP));
		this.getServletContext().getRequestDispatcher("/jsp/epcContentView.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
