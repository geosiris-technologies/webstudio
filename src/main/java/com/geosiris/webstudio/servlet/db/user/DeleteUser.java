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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Servlet implementation class Connection
 */
@WebServlet("/deleteuser")
public class DeleteUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(DeleteUser.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DeleteUser() {
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

		request.setAttribute("userList", UserDBInfos.listUsersInfos());
		getServletContext().getRequestDispatcher("/jsp/deleteUser.jsp").forward(request, response);
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

		String[] logins = request.getParameterValues("login");
		logger.info(logins);
		if (logins.length > 0) {
			boolean deleted = UserDBInfos.deleteUser(logins);
			request.setAttribute("userList", UserDBInfos.listUsersInfos());
			if (deleted) {
				if (logins.length == 1) {
					request.setAttribute("message", "User '" + logins[0] + "' deleted with success !");
				} else {
					request.setAttribute("message", "All users deleted with success !");
				}
				// On supprime leur workspace
				// for(String deletedUser : logins) {
				// WorkspaceDBInfo.removeFileFromWorkspace(session, deletedUser, new
				// ArrayList<String>() );
				// }
				getServletContext().getRequestDispatcher("/jsp/deleteUser.jsp").forward(request, response);
			} else {
				request.setAttribute("message", "User doesn't exist");
				getServletContext().getRequestDispatcher("/jsp/deleteUser.jsp").forward(request, response);
			}
		} else {
			request.setAttribute("userList", UserDBInfos.listUsersInfos());
			request.setAttribute("message", "No user selected to delete");

			getServletContext().getRequestDispatcher("/jsp/deleteUser.jsp").forward(request, response);
		}
	}

}
