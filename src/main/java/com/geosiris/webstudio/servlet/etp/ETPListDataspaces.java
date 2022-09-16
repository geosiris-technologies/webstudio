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
package com.geosiris.webstudio.servlet.etp;

import Energistics.Etp.v12.Datatypes.Object.Dataspace;
import Energistics.Etp.v12.Protocol.Dataspace.GetDataspaces;
import Energistics.Etp.v12.Protocol.Dataspace.GetDataspacesResponse;
import com.geosiris.etp.communication.Message;
import com.geosiris.etp.websocket.ETPClient;
import com.geosiris.webstudio.utils.SessionUtility;
import com.geosiris.webstudio.utils.Utility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet implementation class ETPListDataspaces
 */
@WebServlet("/ETPListDataspaces")
public class ETPListDataspaces extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ETPListDataspaces() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!SessionUtility.tryConnectServlet(request, response)) {
			return;
		}
		List<String> dataspacesNames = new ArrayList<>();

		ETPClient etpClient = (ETPClient) request.getSession(false).getAttribute(SessionUtility.SESSION_ETP_CLIENT_ID);
		boolean isConnected = etpClient != null && etpClient.isConnected();
		if(isConnected){
			GetDataspaces getRess = GetDataspaces.newBuilder().setStoreLastWriteFilter(0L).build();
			long id = etpClient.send(getRess);
			List<Message> ressResp_l = etpClient.getEtpClientSession().waitForResponse(id, 100000);
			for(Dataspace ds : ((GetDataspacesResponse)ressResp_l.get(0).getBody()).getDataspaces()){
				dataspacesNames.add(searchDataspaceNameFromUri(ds.getUri()+""));
			}
		}

		PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(Utility.toJson(dataspacesNames, null));
        out.flush();
	}

	public static String searchDataspaceNameFromUri(String dataspaceUri){
		return dataspaceUri.replaceAll("eml:\\/\\/\\/dataspace\\('?([^)']+)'?\\)", "$1");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!SessionUtility.tryConnectServlet(request, response)) {
			return;
		}
		doGet(request, response);
	}
}
