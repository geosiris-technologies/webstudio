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
package com.geosiris.webstudio.servlet.editing;

import com.geosiris.energyml.utils.EPCGenericManager;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.utils.ObjectTree;
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
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Servlet implementation class EPCOperation
 */
@WebServlet("/EPCOperation")
public class EPCOperation extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(EPCOperation.class);
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EPCOperation() {
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
		
		request.setCharacterEncoding("UTF-8");
		
		String command = request.getParameter("command");
		String rootUuid = request.getParameter("Root_UUID");
		String input = request.getParameter("input");
		
		logger.info("#EPCOperation : command '" + command +"' root uuid '" + rootUuid + "' input '" + input+"'");
		Map<String, Object> epcObjects = SessionUtility.getResqmlObjects(session);
		
		executeCommand(rootUuid, command, input, epcObjects);
		
		PrintWriter out = response.getWriter();
		response.setContentType("application/text");
		response.setCharacterEncoding("UTF-8");
		out.write("Ok");
		out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	private void executeCommand(String rootUuid, String command, String input, Map<String, Object> epcObjects) {
		if(command!=null && rootUuid != null) {
			Object rootObj = epcObjects.get(rootUuid);
			
			// Changement de reference
			if(command.toLowerCase().compareTo("changereference")==0 && rootObj !=null && input != null) {
				input = input.replaceAll(" ", "");
				rootUuid = rootUuid.replaceAll(" ", "");
				
				List<Pair<String, Object>> referencers = EPCGenericManager.getAllReferencersDORParameters(rootUuid, epcObjects);
				for(Pair<String, Object> refer : referencers) {
					Object referObj = refer.r();
					List<ObjectTree> referencedDOR = ObjectTree.createTree(referObj).filterListByObjectType("DataObjectReference", false, true);
					for(ObjectTree dor : referencedDOR) {
						String dorUUID = (String) ObjectController.getObjectAttributeValue(referObj, dor.getName() + ".uuid");
						if(dorUUID != null && dorUUID.compareTo(rootUuid) == 0) {
							try {
								ObjectController.editObjectAttribute(referObj, dor.getName() + ".uuid", input);
							} catch (Exception e) {
								logger.error("error changing '" + dor.getName() + ".uuid' on " + referObj);
								logger.error(e.getMessage(), e);
							}
						}
					}
				}
			}else {
				logger.error("Command " + command +" can not be invoke");
			}
		}
	}

}
