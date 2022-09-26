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

import com.geosiris.energyml.utils.ExportVersion;
import com.geosiris.webstudio.model.WorkspaceContent;
import com.geosiris.webstudio.utils.HttpSender;
import com.geosiris.webstudio.utils.SessionUtility;
import energyml.common2_3.Citation;
import energyml.resqml2_2.TriangulatedSetRepresentation;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servlet implementation class FirstServlet
 */
@WebServlet("/OSDU_Manifest")
public class OSDU_Manifest extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static Logger logger = LogManager.getLogger(OSDU_Manifest.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public OSDU_Manifest() {
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
		HttpSession session = request.getSession(false);

		String token = request.getParameter("token");
		String host = request.getParameter("host");
		String data_partition_id = request.getParameter("data_partition_id");
		if (ServletFileUpload.isMultipartContent(request)) {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			FileItemIterator iterator = upload.getItemIterator(request);
			while (iterator.hasNext()) {
				FileItemStream item = iterator.next();
				InputStream stream = item.openStream();
				if (item.isFormField() && item.getFieldName().compareTo("token") == 0) {
					token = Streams.asString(stream);
				} else if (item.isFormField() && item.getFieldName().compareTo("host") == 0) {
					host = Streams.asString(stream);
				} else if (item.isFormField() && item.getFieldName().compareTo("data_partition_id") == 0) {
					data_partition_id = Streams.asString(stream);
				}
			}
		}

		System.out.print("Parameters : " + token + " -- " + host + " -- " + data_partition_id);

		final WorkspaceContent workspace = new WorkspaceContent();
		workspace.putAll(SessionUtility.getWorkspaceContent(session));

		Map<String, String> otherParams = new HashMap<>();
		otherParams.put("data_partition_id", data_partition_id != null ? data_partition_id : "osdu");
		if(token != null && host != null) {
			otherParams.put("token", token);
			otherParams.put("host", host);
		}

		String answer = HttpSender.sendfileWithPostRequest(session,
				(outputStream -> ExportEPCFile.exportEPCFile(outputStream, workspace, ExportVersion.CLASSIC)),
				"http://osdu-manifest:8000/", null, null, "file", otherParams);

		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		out.write(answer);
		out.flush();
	}

	public static void main(String[] argv) throws IOException {
//		File f = new File("C:/Users/Cryptaro/Downloads/java.epc");
//		FileOutputStream fio = new FileOutputStream(f);

//		try(ZipOutputStream epc = new ZipOutputStream(fio, StandardCharsets.UTF_8)) {
////			epc.setLevel(0);
//			ZipEntry ze_resqml = new ZipEntry("test.xml");
//			epc.putNextEntry(ze_resqml);
//			epc.write("coucou".getBytes(StandardCharsets.UTF_8));
//			epc.closeEntry();
////			epc.finish();
////      		epc.flush();
//		} catch (FileNotFoundException e) {
//			throw new RuntimeException(e);
//		}
//		fio.close();

		Map<String, String> otherParams = new HashMap<>();
		otherParams.put("data_partition_id", "osdu");
		otherParams.put("token", "ya29.a0Aa4xrXMgK0dBJo62n6NwsjUBvofohmy59DMtdpT7rrrewFDDIZOhNFmBUlmGpP33UUz7Y1QpWyq4p3SOj3KwXDgcWEQF7T0dHVAtpmNNTNhpWKim1JsA83s-ep50otqQHisE4uDjeLugnw78pNgYv0RzX_FEaCgYKATASARISFQEjDvL98VqQXGcQ1M5u1F7UTOQVxg0163");
		otherParams.put("host", "https://community.gcp.gnrg-osdu.projects.epam.com/api/dataset/v1");

//		HttpSender.sendfileWithPostRequest(null,
//				(outputStream -> {
//					try(ZipOutputStream epc = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
//				//			epc.setLevel(0);
//							ZipEntry ze_resqml = new ZipEntry("test.xml");
//							epc.putNextEntry(ze_resqml);
//							epc.write("coucou".getBytes(StandardCharsets.UTF_8));
//							epc.closeEntry();
//				//			epc.finish();
//				//      		epc.flush();
//						} catch (FileNotFoundException e) {
//							throw new RuntimeException(e);
//						} catch (IOException e) {
//						throw new RuntimeException(e);
//					}
//				}),
//				"http://localhost:8000/", null, null, "file", otherParams);
		WorkspaceContent wc = new WorkspaceContent();
		TriangulatedSetRepresentation tr = new TriangulatedSetRepresentation();
		String uuid = UUID.randomUUID()+"";
		Citation cit = new Citation();
		cit.setTitle("coucou");
		tr.setUuid(uuid);
		tr.setCitation(cit);
		wc.getReadObjects().put(uuid, tr);
		HttpSender.sendfileWithPostRequest(null,
				(outputStream -> ExportEPCFile.exportEPCFile(outputStream, wc, ExportVersion.CLASSIC)),
				"http://localhost:8000/", null, null, "file", otherParams);

	}
}
