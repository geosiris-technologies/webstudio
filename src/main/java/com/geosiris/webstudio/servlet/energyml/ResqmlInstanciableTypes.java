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

import com.geosiris.webstudio.servlet.Editor;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/ResqmlInstanciableTypes")
public class ResqmlInstanciableTypes extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(ResqmlInstanciableTypes.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ResqmlInstanciableTypes() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!SessionUtility.tryConnectServlet(request, response)) {
			return;
		}
		
		String rootClassName = request.getParameter("rootClass");

		StringBuilder jsonInstanciableTypes = new StringBuilder("[\n");
		if(rootClassName!=null && rootClassName.length()>0) {
			List<Class<?>> subClassList = Editor.pkgManager.getInheritorClasses(rootClassName);
			for(Class<?> cl : subClassList) {
				jsonInstanciableTypes.append("\"").append(cl.getName()).append("\",");
			}
			if(subClassList.size()>0) {
				jsonInstanciableTypes = new StringBuilder(jsonInstanciableTypes.substring(0, jsonInstanciableTypes.length() - 1)); // On enlÃ¨ve la derniÃ¨re virgule
			}
		}
		jsonInstanciableTypes.append("\n]");
		PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(jsonInstanciableTypes.toString());
        out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void contextInitialized(ServletContextEvent event) {
        // Do your thing during webapp's startup.
		logger.info("Starting " + this.getClass().getName());
    }
    public void contextDestroyed(ServletContextEvent event) {
        // Do your thing during webapp's shutdown.
		logger.info("Ending " + this.getClass().getName());
    }

}
