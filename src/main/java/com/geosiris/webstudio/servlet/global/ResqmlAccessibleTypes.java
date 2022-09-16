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

import com.geosiris.webstudio.servlet.Editor;
import com.geosiris.webstudio.utils.SessionUtility;
import com.geosiris.webstudio.utils.Utility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet implementation class ResqmlAccessibleTypes
 */
@WebServlet("/ResqmlAccessibleTypes")
public class ResqmlAccessibleTypes extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(ResqmlAccessibleTypes.class);

	private static final Method getClassName = Arrays.stream(Class.class.getMethods())
											.filter(method -> method.getName().compareTo("getName") == 0)
											.collect(Collectors.toList()).get(0);
	public final static HashMap<Class<?>, List<Class<?>>> CLASS_INSTANCIABLE_BY = Editor.pkgManager.getResqmlTypesInstanciableBy();
	public final static String jsonContent = Utility.toJson(Editor.pkgManager.getResqmlTypesInstanciableBy(),
												getClassName,
												getClassName);
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResqmlAccessibleTypes() {
        super();
//        logger.info("Lancement de ResqmlAccesibleTypes " + jsonContent);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!SessionUtility.tryConnectServlet(request, response)) {
			return;
		}

		PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(jsonContent);
        out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public static void main(String[] argv){
		logger.info(jsonContent);
	}

}
