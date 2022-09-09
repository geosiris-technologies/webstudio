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
package com.geosiris.webstudio.servlet.etp;

import com.geosiris.etp.websocket.ETPClient;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet implementation class ETPConnexionInfos
 */
@WebServlet("/ETPConnexionInfos")
public class ETPConnexionInfos extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ETPConnexionInfos() {
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
		HttpSession session = request.getSession(false);

		ETPClient etpClient = (ETPClient) request.getSession(false).getAttribute(SessionUtility.SESSION_ETP_CLIENT_ID);

		String answer = "{}";

		if(etpClient != null && etpClient.isConnected()){
			StringBuilder jsonResp = new StringBuilder();
			jsonResp.append("{\"serverUrl\": \"");
			jsonResp.append(session.getAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_URL));
			jsonResp.append("\", \"login\": \"");
			if(session.getAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_USERNAME) != null) {
				jsonResp.append(session.getAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_USERNAME));
			}
			jsonResp.append("\", \"password\": \"");
			if(session.getAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_PASSWORD) != null) {
				jsonResp.append(session.getAttribute(SessionUtility.SESSION_ETP_CLIENT_LAST_PASSWORD));
			}
			jsonResp.append("\"}");
			answer = jsonResp.toString();
		}

		PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(answer);
        out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!SessionUtility.tryConnectServlet(request, response)) {
			return;
		}
		doGet(request, response);
	}


}
