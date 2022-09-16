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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet implementation class ResqmlRootTypes
 */
@WebServlet("/ResqmlRootTypes")
public class ResqmlRootTypes extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(ResqmlRootTypes.class);
    private static final List<Class<?>> rootClasses = Editor.pkgManager.getRootClasses()
				.stream().filter(cl -> cl != null && !Modifier.isAbstract(cl.getModifiers())).collect(Collectors.toList());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResqmlRootTypes() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!SessionUtility.tryConnectServlet(request, response)) {
			return;
		}
		String jsonList = "[\n";
		for(Class<?> rc : rootClasses) {
			jsonList += "\t\"" + rc.getName() + "\",\n";
		}
		if(jsonList.contains(",")) {
			jsonList = jsonList.substring(0, jsonList.lastIndexOf(","));
		}
		jsonList += "]";
		
		PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(jsonList);
        out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public static void main(String[] argv) {
		logger.info("Begin");
		List<Class<?>> rootClasses = Editor.pkgManager.getRootClasses()
				.stream().filter(cl -> cl != null && !Modifier.isAbstract(cl.getModifiers())).collect(Collectors.toList());
		
		String jsonList = "[\n";
		for(Class<?> rc : rootClasses) {
			jsonList += "\t\"" + rc.getName() + "\",\n";
		}
		if(jsonList.contains(",")) {
			jsonList = jsonList.substring(0, jsonList.lastIndexOf(","));
		}
		jsonList += "]";
		logger.info(jsonList);
	}

}
