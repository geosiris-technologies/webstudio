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

import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Servlet implementation class Connection
 */
@WebServlet("/signup")
public class SignUp extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SignUp() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!SessionUtility.isAdminUser(request)) {
			response.sendRedirect("connexion");
			return;
		}

		getServletContext().getRequestDispatcher("/jsp/signUp.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!SessionUtility.isAdminUser(request)) {
			response.sendRedirect("connexion");
			return;
		}
		HttpSession session = request.getSession(false);
		String userName = (String) request.getSession(false).getAttribute(SessionUtility.SESSION_USER_NAME);
		String usergrp = UserDBInfos.getUserAttribute(userName, UserDBInfos.DB_GRP);

		String login = request.getParameter("login");
		String pwd = request.getParameter("password");
		String mail = request.getParameter("mail");

		if (UserDBInfos.createUser(login, pwd, mail)) {
			// String resCreateUserWorkspace = WorkspaceDBInfo.createUser(session, login,
			// mail, pwd);
			// logger.info("Created user : " + login + " Workspace info : " +
			// resCreateUserWorkspace);
			// Si c'est un membre de geosiris qui cree l'utilisateur on ne le connecte pas a
			// ce nouveau compte
			if ("geosiris".compareTo(usergrp) != 0) {
				session.setAttribute(SessionUtility.SESSION_USER_NAME, login);
				session.setAttribute(SessionUtility.SESSION_USER_GRP,
						UserDBInfos.getUserAttribute(login, UserDBInfos.DB_GRP));
				response.sendRedirect("editor");
			} else {
				request.setAttribute("response", "User created");
				getServletContext().getRequestDispatcher("/jsp/signUp.jsp").forward(request, response);
			}
		} else {
			request.setAttribute("response", "User or mail allready exist");
			getServletContext().getRequestDispatcher("/jsp/signUp.jsp").forward(request, response);
		}
	}

}
