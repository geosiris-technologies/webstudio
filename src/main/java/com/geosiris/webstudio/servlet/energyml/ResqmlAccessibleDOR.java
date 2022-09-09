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

import com.geosiris.energyml.utils.EPCGenericManager;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.webstudio.servlet.Editor;
import com.geosiris.webstudio.utils.SessionUtility;
import com.geosiris.webstudio.utils.Utility;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet implementation class FirstServlet
 */
@WebServlet("/ResqmlAccessibleDOR")
public class ResqmlAccessibleDOR extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(ResqmlAccessibleDOR.class);
    
	public static final HashMap<String, List<String>> mapAccessibleDORTypes = Editor.pkgManager.getAccessibleDORTypes();
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResqmlAccessibleDOR() {
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
		
		String uuid = request.getParameter("uuid"); // Get the uuid of the object that want to references a DOR

		String subParamPath = request.getParameter("subParamPath"); // Get the uuid of the object that want to references a DOR
		
		String subParamName = request.getParameter("subParamName");
		
		//logger.error("subparam Path : " + subParamPath);
		String answer = "{}";
		if(uuid!=null) {
			Map<String, Object> map = SessionUtility.getResqmlObjects(session);
			if(map.containsKey(uuid)) {
				Object resqmlObj = map.get(uuid);
				Class<?> subParamClass = null;
				Object subParam = null;
				if(subParamPath!=null && subParamPath.length()>0) {
					subParam = ObjectController.getObjectAttributeValue(resqmlObj, subParamPath);
					if(subParam!=null) {
						subParamClass = subParam.getClass();
					}
				}
				//logger.error(" sub param class : " + subParamClass);
				 
				List<Object> dorable = EPCGenericManager.getAccessibleDORs(resqmlObj, subParam, subParamName, map.values(), mapAccessibleDORTypes);
				answer = Utility.getEPCContentAsJSON(dorable);
			}
		}

		PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.write(answer);
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
