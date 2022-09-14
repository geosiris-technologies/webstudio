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
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.servlet.global.SessionCounter;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Servlet implementation class EPCEvent
 */
@WebServlet(urlPatterns = {"/EPCEvent"}, asyncSupported = true)
public class EPCEvent extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(EPCEvent.class);
	private static final long SESSION_DURATION = 1000L*60L*30L; // 30 min in ms

	private final Queue<Pair<HttpSession, AsyncContext>> ongoingRequests = new ConcurrentLinkedQueue<>();
	private ScheduledExecutorService service;

	@Override
	public void init(ServletConfig config) throws ServletException {
		final Runnable notifier = new Runnable() {
			@Override
			public void run() {
				final Iterator<Pair<HttpSession, AsyncContext>> iterator = ongoingRequests.iterator();
				//not using for : in to allow removing items while iterating

				if(iterator.hasNext()) {
					Pair<HttpSession, AsyncContext> ac = iterator.next();
					final ServletResponse res = ac.r().getResponse();
					final HttpSession session = ac.l();

					String eventName = ac.r().getRequest().getParameter("event");
					//logger.info("RUNNABLE <== " + eventName);
					PrintWriter writer;
					try {
						writer = res.getWriter();

						if(eventName.compareToIgnoreCase("sessionDuration") == 0 
								|| eventName.compareToIgnoreCase("all") == 0) {
							writer.write("event: sessionDuration\n\n");
							Date date = new Date();
							writer.write("data: " 
									+ ( SESSION_DURATION - (date.getTime() - session.getLastAccessedTime()))
									+ "\n\n");
							//logger.info("EPCEvent : printing session duration ");
						}


						if(eventName.compareToIgnoreCase("sessionCount") == 0
								|| eventName.compareToIgnoreCase("all") == 0) {
							//if("geosiris".compareTo(usergrp) == 0) {	// On ne donne l'info qu'au geosiris users
							writer.write("event: sessionCount\n\n");
							writer.write("data: "
									+ SessionCounter.getActiveSessions()
									+ "\n\n");
//							logger.info("EPCEvent : printing session count ");
							//}
						}

						if(eventName.compareToIgnoreCase("logs") == 0
								|| eventName.compareToIgnoreCase("all") == 0) {
							ConcurrentLinkedQueue<ServerLogMessage> logs = SessionUtility.getLogs(session);
							if(logs != null && logs.size() > 0) {
								ServerLogMessage msg = logs.poll();
								String msg_str = msg.toJSON().replaceAll("[\n\r]", "");
								writer.write("event: logs\n\n");
								writer.write("data: ");
//										writer.write(msg.toJson().replace("\n", ""));
								writer.write(Base64.getEncoder().encodeToString(msg_str.getBytes()));
								writer.write("\n\n");
								writer.flush();
//								logger.info("EPCEvent : printing msg " + msg_str);
							}
						}
						writer.write("\n\n");
						writer.flush();

						if (writer.checkError()) { //checkError calls flush, and flush() does not throw IOException
							iterator.remove();
							logger.error("EPCEvent : closing event handler");
						}
					} catch (IOException ignored) {
						iterator.remove();
						logger.error("EPCEvent : closing event handler");
					}

				}
			}
		};
		service = Executors.newScheduledThreadPool(10);
		service.scheduleAtFixedRate(notifier, 1, 2, TimeUnit.SECONDS);
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EPCEvent() {
		super();
	}

	// TODO : faire des evÃ¨nements pour demander Ã la vue de se remettre Ã jour pour la lecture des donnees
	// 			--> cela implique de faire attention aux onglets


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
//		logger.info("RUNNABLE <== " + eventName);
		// TODO : Le code suivant semble inutile car il semble que c'est la fonction init qui est utilisee par la vue
		PrintWriter writer;
		try {
			writer = response.getWriter();

			while(session != null && request.isRequestedSessionIdValid()) {
				if(eventName.compareToIgnoreCase("sessionDuration") == 0 
						|| eventName.compareToIgnoreCase("all") == 0) {
					writer.write("event: sessionDuration\n");
					Date date = new Date();
					writer.write("data: " 
							+ ( SESSION_DURATION - (date.getTime() - session.getLastAccessedTime()))
							+ "\n\n");
//					logger.info("EPCEvent : printing session duration ");
				}


				if(eventName.compareToIgnoreCase("sessionCount") == 0
						|| eventName.compareToIgnoreCase("all") == 0) {
					//if("geosiris".compareTo(usergrp) == 0) {	// On ne donne l'info qu'au geosiris users
					writer.write("event: sessionCount\n");
					writer.write("data: "
							+ SessionCounter.getActiveSessions()
							+ "\n\n");
//					logger.info("EPCEvent : printing session count ");
					//}
				}

				if(eventName.compareToIgnoreCase("logs") == 0
						|| eventName.compareToIgnoreCase("all") == 0) {
					ConcurrentLinkedQueue<ServerLogMessage> logs = SessionUtility.getLogs(session);
					while(logs != null && logs.size() > 0) {
						ServerLogMessage msg = logs.poll();

						String msg_str = msg.toJSON().replaceAll("[\n\r]", "");
//						String msg_str = msg.toJSON();

						writer.write("event: logs\n\n");
						writer.write("data: ");
						writer.write(Base64.getEncoder().encodeToString(msg_str.getBytes()));
						writer.write("\n\n");
//						logger.info("2EPCEvent : printing msg " + msg_str);
						writer.flush();
					}
				}
				writer.write("\n\n");
				writer.flush();

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
