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
@WebServlet("/usersettings")
public class UserSettings extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserSettings() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!SessionUtility.tryConnectServlet(request, response)) {
			return;
		}
		HttpSession session = request.getSession(false);
		String userName = (String) session.getAttribute(SessionUtility.SESSION_USER_NAME);
		
		request.setAttribute("login", userName);
		request.setAttribute("mail", UserDBInfos.getUserAttribute(userName, UserDBInfos.DB_MAIL));
		getServletContext().getRequestDispatcher("/jsp/userSettings.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!SessionUtility.tryConnectServlet(request, response)) {
			return;
		}
		HttpSession session = request.getSession(false);
		String userName = (String) session.getAttribute(SessionUtility.SESSION_USER_NAME);
		
		String login = request.getParameter("login");
		String pwd = request.getParameter("password");
		String newPwd = request.getParameter("newPassword");
		String mail = request.getParameter("mail");
		String grp = request.getParameter("group");
		

		if(login.compareTo(userName)==0) {
			boolean updated = UserDBInfos.updateUser(login, pwd, newPwd, mail, grp);
			if(updated) {
				request.setAttribute("response", "Modifications done");
			}else{
				request.setAttribute("response", "Incorrect password");
			}
			
			request.setAttribute("login", userName);
			request.setAttribute("mail", UserDBInfos.getUserAttribute(userName, UserDBInfos.DB_MAIL));
	
			getServletContext().getRequestDispatcher("/jsp/userSettings.jsp").forward(request, response);
		}else {
			request.setAttribute("login", userName);
			request.setAttribute("mail", UserDBInfos.getUserAttribute(userName, UserDBInfos.DB_MAIL));
			request.setAttribute("response", "Error : '" + login + "' is not your user name");
			getServletContext().getRequestDispatcher("/jsp/userSettings.jsp").forward(request, response);
		}
	}

}
