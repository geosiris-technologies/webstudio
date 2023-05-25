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

import Energistics.Etp.v12.Datatypes.Object.Resource;
import com.geosiris.energyml.utils.EPCGenericManager;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.etp.utils.ETPUri;
import com.geosiris.webstudio.servlet.Editor;
import com.geosiris.webstudio.utils.ETPUtils;
import com.geosiris.webstudio.utils.SessionUtility;
import com.geosiris.webstudio.utils.Utility;
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
		StringBuilder answer = new StringBuilder("{}");
		if(uuid!=null) {
			Map<String, Object> map = SessionUtility.getResqmlObjects(session);
			if(map.containsKey(uuid)) {
				Object resqmlObj = map.get(uuid);
				Object subParam = null;
				if(subParamPath!=null && subParamPath.length()>0) {
					subParam = ObjectController.getObjectAttributeValue(resqmlObj, subParamPath);
				}
				List<Object> dorable = EPCGenericManager.getAccessibleDORs(resqmlObj, subParam, subParamName, map.values(), mapAccessibleDORTypes);

				answer = new StringBuilder("{ \"workspace\": ");
				answer.append(Utility.getEPCContentAsJSON(dorable));
				answer.append(", \"etp\": [");
				try {
					String dataspace = request.getParameter("dataspace");
					int cpt = 0;
					for(Resource r: ETPUtils.getResources(session, dataspace)){
						ETPUri uri = ETPUri.parse(r.getUri().toString());
						if(!uri.hasDataspace() && dataspace != null && dataspace.length() > 0){
							uri.setDataspace(dataspace);
						}

						answer.append("{ ");
						answer.append("\"num\" : \"").append(cpt).append("\", ");
						answer.append("\"title\" : \"").append(r.getName()).append("\", ");
						answer.append("\"type\" : \"").append(uri.getObjectType()).append("\", ");
						answer.append("\"uuid\" : \"").append(uri.getUuid()).append("\", ");
						answer.append("\"schemaVersion\" : \"").append(uri.getDomainVersion()).append("\", ");
						answer.append("\"package\" : \"").append(uri.getDomain()).append("\", ");
						answer.append("\"uri\" : \"").append(uri.toString()).append("\"");
						answer.append("},");
						cpt++;
					}
					if(answer.toString().endsWith(",")){
						answer.replace(answer.length()-1, answer.length(), "");
					}

				}catch (Exception e){
					logger.error(e);
				}
				answer.append("]");
				answer.append("}");
			}
		}

		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		out.write(answer.toString());
		out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
