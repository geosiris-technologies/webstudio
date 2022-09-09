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
package com.geosiris.webstudio.servlet.db.user;

import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.servlet.global.SessionCounter;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Servlet implementation class Connection
 */
@WebServlet("/connexion")
public class Connexion extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Connexion() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getSession();
		if(SessionUtility.isConnectedUser(request)) {
			response.sendRedirect("editor");
			return;
		}

		getServletContext().getRequestDispatcher("/jsp/connexion.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String login = request.getParameter("login");
		String password = request.getParameter("password");
		
		Pair<String, String> loggedUser = UserDBInfos.logUser(login, password);
		if(loggedUser != null) {
			HttpSession session = request.getSession(false);
			if(session != null)
				session.invalidate(); // invalidate the previous session
			
			session = request.getSession();
			SessionCounter.newConnexion();
			session.setAttribute(SessionUtility.SESSION_USER_NAME, login);
			session.setAttribute(SessionUtility.SESSION_USER_GRP, loggedUser.r());
			session.setAttribute(SessionUtility.SESSION_WORKSPACE_DATA_ID, null);
			session.setAttribute(SessionUtility.SESSION_LOGS, new ConcurrentLinkedQueue<ServerLogMessage>());
			response.sendRedirect("loadworkspace");
			SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.INFO,
					"CONNEXION : user '" + login + "' connected", 
					SessionUtility.EDITOR_NAME));
		}else{
			request.setAttribute("response", "Incorrect login or password");
			getServletContext().getRequestDispatcher("/jsp/connexion.jsp").forward(request, response);
		}
	}

}
