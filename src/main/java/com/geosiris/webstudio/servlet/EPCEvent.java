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

import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.servlet.global.SessionCounter;
import com.geosiris.webstudio.utils.SessionUtility;
import com.geosiris.webstudio.utils.Utility;
import com.google.gson.Gson;
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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Servlet implementation class EPCEvent
 */
@WebServlet(urlPatterns = {"/EPCEvent"}, asyncSupported = true)
public class EPCEvent extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static Logger logger = LogManager.getLogger(EPCEvent.class);
	private static final long SESSION_DURATION = 1000L*60L*30L; // 30 min in ms

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EPCEvent() {
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


		response.setContentType("text/event-stream");
		response.setCharacterEncoding("UTF-8");

		logger.info("EPCEvent : adding async on event '" + request.getParameter("event") +"'");

		String eventName = request.getParameter("event");

		PrintWriter writer;

		Gson gson = new Gson();

		try {
			writer = response.getWriter();

			while(session != null && request.isRequestedSessionIdValid()) {
				if(eventName.compareToIgnoreCase("sessionDuration") == 0
						|| eventName.compareToIgnoreCase("all") == 0) {
					writer.write("event: sessionDuration\n\n");
					Date date = new Date();
					writer.write("data: "
							+ ( SESSION_DURATION - (date.getTime() - session.getLastAccessedTime()))
							+ "\n\n");
					writer.flush();
//					logger.info("EPCEvent : printing session duration ");
				}


				if(eventName.compareToIgnoreCase("sessionCount") == 0
						|| eventName.compareToIgnoreCase("all") == 0) {
					//if("geosiris".compareTo(usergrp) == 0) {	// On ne donne l'info qu'au geosiris users
					writer.write("event: sessionCount\n\n");
					writer.write("data: "
							+ SessionCounter.getActiveSessions()
							+ "\n\n");
					writer.flush();
//					logger.info("EPCEvent : printing session count ");
					//}
				}

				if(eventName.compareToIgnoreCase("sessionMetrics") == 0
						|| eventName.compareToIgnoreCase("all") == 0) {
					writer.write("event: sessionMetrics\n\n");
					writer.write("data: " + Base64.getEncoder().encodeToString((Utility.getObjectSize(SessionUtility.getWorkspaceContent(session))).getBytes(StandardCharsets.UTF_8)) + "\n\n");
					writer.flush();
				}

				if(eventName.compareToIgnoreCase("logs") == 0
						|| eventName.compareToIgnoreCase("all") == 0) {
					ConcurrentLinkedQueue<ServerLogMessage> logs = SessionUtility.getLogs(session);
					while(logs != null && logs.size() > 0) {
						ServerLogMessage msg = logs.poll();

						String msg_str = msg.toJSON();
						writer.write("event: logs\n\n");
						writer.write("data: ");
						writer.write(Base64.getEncoder().encodeToString(msg_str.getBytes()));
						writer.write("\n\n");
//						logger.info("2EPCEvent : printing msg " + msg_str);
						writer.flush();
					}
				}

				if (writer.checkError()) { //checkError calls flush, and flush() does not throw IOException
					//logger.error("EPCEvent : closing event handler");
					break;
				}
				try { Thread.sleep(2000);
				} catch (Exception e) { logger.error(e.getMessage(), e); }
			}
		} catch (IOException ignored) {
			logger.error("EPCEvent : closing event handler");
		}
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
