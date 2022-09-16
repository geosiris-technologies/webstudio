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
package com.geosiris.webstudio.servlet;

import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.servlet.db.workspace.LoadWorkspace;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;

/**
 * Servlet implementation class WorkspaceAdditionalData
 */
@WebServlet("/WorkspaceAdditionalData")
public class WorkspaceAdditionalData extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(WorkspaceAdditionalData.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public WorkspaceAdditionalData() {
		super();
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!SessionUtility.tryConnectServlet(request, response)) {
			return;
		}
		String filePath = request.getParameter("filePath");

		String contentType = "application/octet-stream";

		logger.info("Trying to search file : '" + filePath + "'");
		for(Pair<String, byte[]> data: SessionUtility.getNotResqmlObjects(request.getSession(false))){
			if(data.l().compareTo(filePath) == 0){
				if(filePath.toLowerCase().endsWith(".pdf")){
					contentType = "application/pdf";
					//response.setHeader("Content-Disposition", "attachment;filename=" + Paths.get(filePath).getFileName());
				}else{
					String headerKey = "Content-Disposition";
					String headerValue = String.format("attachment; filename=\"%s\"", Paths.get(filePath).getFileName());
					response.setHeader(headerKey, headerValue);
				}

				byte[] fileContent =  data.r();

				OutputStream out = response.getOutputStream();
				response.setContentType(contentType);
				response.setContentLength(fileContent.length);

				for(int chunk=0; chunk<fileContent.length; chunk += 4096){
					out.write(fileContent, chunk, Math.min(4096, fileContent.length-chunk));
				}

				out.flush();
				return;
			}
		}
		PrintWriter out = response.getWriter();
		response.setContentType("application/text");
		response.setCharacterEncoding("UTF-8");
		out.write("File " + filePath + " not found.");
		out.flush();

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!SessionUtility.tryConnectServlet(request, response)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(!SessionUtility.tryConnectServlet(request, response)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		String filePath = request.getParameter("filePath");
		if(!LoadWorkspace.removeAdditionalFileFromWorkspace(request.getSession(false), filePath)){
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}else{
			response.sendError(HttpServletResponse.SC_ACCEPTED);
			LoadWorkspace.reloadWorkspaceAdditionalFiles(request.getSession(false));
		}
	}
}
