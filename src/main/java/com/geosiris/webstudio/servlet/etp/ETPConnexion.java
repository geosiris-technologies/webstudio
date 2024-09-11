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

import com.geosiris.etp.websocket.ETPClient;
import com.geosiris.webstudio.utils.ETPUtils;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.eclipse.jetty.http.HttpURI;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;

/**
 * Servlet implementation class ETPConnexion
 */
@WebServlet("/ETPConnexion")
public class ETPConnexion extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(ETPConnexion.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ETPConnexion() {
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
		String req = request.getParameter("request");
		String answer = "";
		if ("isconnected".compareToIgnoreCase(req) == 0) {
			ETPClient etpClient = (ETPClient) request.getSession(false)
					.getAttribute(SessionUtility.SESSION_ETP_CLIENT_ID);
			boolean isConnected = etpClient != null && etpClient.isConnected();
			answer = "" + isConnected;
		} else {
			answer = "unkown request";
		}
		PrintWriter out = response.getWriter();
        response.setContentType("application/text");
        response.setCharacterEncoding("UTF-8");
        out.write(answer);
        out.flush();
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

		String userName = null;
		String password = null;
		boolean askConnection = true;
		HttpURI host_uri = null;

		if (ServletFileUpload.isMultipartContent(request)) {
			DiskFileItemFactory factory = new DiskFileItemFactory();

			ServletFileUpload upload = new ServletFileUpload(factory);

			try {
				FileItemIterator iterator = upload.getItemIterator(request);

				while (iterator.hasNext()) {
					FileItemStream item = iterator.next();
					InputStream stream = item.openStream();

					if (item.isFormField()) {
						String value = Streams.asString(stream);
						if ("etp-server-uri".compareToIgnoreCase(item.getFieldName()) == 0) {
							if(value.toLowerCase(Locale.ROOT).startsWith("http:")) {
								value = "ws" + value.substring(4);
							}else if(!value.toLowerCase(Locale.ROOT).startsWith("ws") && !value.toLowerCase(Locale.ROOT).startsWith("wss") ){
								value = "ws://" + value;
							}
							host_uri = new HttpURI(value);
						} else if ("etp-server-username".compareToIgnoreCase(item.getFieldName()) == 0) {
							userName = value;
						} else if ("etp-server-password".compareToIgnoreCase(item.getFieldName()) == 0) {
							password = value;
						} else if ("request-type".compareToIgnoreCase(item.getFieldName()) == 0) {
							if (value.compareToIgnoreCase("disconnect") == 0) {
								askConnection = false;
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			for (String k : request.getParameterMap().keySet()) {
				String value = request.getParameterMap().get(k)[0];
				if ("etp-server-uri".compareToIgnoreCase(k) == 0) {
					if(!value.toLowerCase(Locale.ROOT).startsWith("ws") || !value.toLowerCase(Locale.ROOT).startsWith("wss") ){
						value = "ws://" + value;
					}
					host_uri = new HttpURI(value);
				} else if ("etp-server-username".compareToIgnoreCase(k) == 0) {
					userName = value;
				} else if ("etp-server-password".compareToIgnoreCase(k) == 0) {
					password = value;
				} else if ("request-type".compareToIgnoreCase(k) == 0) {
					logger.info("Value request type " + value);
					if (value.compareToIgnoreCase("disconnect") == 0) {
						askConnection = false;
					}
				}
			}
		}

		logger.info("#ETP : host " + host_uri);
		logger.info(request.getSession(false));
		ETPClient client = ETPUtils.establishConnexion(request.getSession(false), host_uri, userName, password, null, new HashMap<>(), askConnection);

		PrintWriter out = response.getWriter();
        response.setContentType("application/text");
        response.setCharacterEncoding("UTF-8");
        out.write(String.valueOf(client != null));
        out.flush();
	}


}
