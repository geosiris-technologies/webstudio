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
package com.geosiris.webstudio.servlet.db.minio;

import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Servlet implementation class FileReceiver
 */
@WebServlet("/h5reciever")
@MultipartConfig
public class H5Reciever extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(H5Reciever.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public H5Reciever() {
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

		getServletContext().getRequestDispatcher("/jsp/uploadh5.jsp").forward(request, response);
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
		String answer = "";
		if (ServletFileUpload.isMultipartContent(request)) {

			// Create a factory for disk-based file items
			DiskFileItemFactory factory = new DiskFileItemFactory();

			ServletFileUpload upload = new ServletFileUpload(factory);

			try {
				FileItemIterator iterator = upload.getItemIterator(request);
				while (iterator.hasNext()) {
					FileItemStream item = iterator.next();
					InputStream stream = item.openStream();

					if (item.isFormField() && item.getFieldName().compareTo("h5InputURL") == 0) {
						String fieldValue = Streams.asString(stream);

						if (fieldValue.length() > 0) {
							try {
								URL epcURL = new URL(fieldValue);
								// InputStream epcFile = new BufferedInputStream(epcURL.openStream());
								File epcFile = new File(epcURL.toURI());
								uploadFile(epcFile);

							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}
						}

					} else if (item.getFieldName().compareTo("h5InputFile") == 0) {
						logger.error("=> " + item + " -- " + item.getName());
						if (item != null && item.getName() != null && item.getName().length() > 0) {
							// uploadFile(item);
							// uploadFile(stream);
						}
					} else {
						logger.error("FileReciever : not readable parameter : '" + item.getFieldName() + "'");
					}
				}

			} catch (FileUploadException e) {
				logger.error(e.getMessage(), e);
			}

		} else {
			answer = "Wrong input";
		}

		PrintWriter out = response.getWriter();
        response.setContentType("application/text");
        response.setCharacterEncoding("UTF-8");
        out.write(answer);
        out.flush();
	}

	private static void uploadFile(File input) {

		try {
			// On essaie de lire en zip
			ZipInputStream zipStream = new ZipInputStream(new FileInputStream(input));
			ZipEntry entry = null;
			try {
				entry = zipStream.getNextEntry();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			if (entry != null) { // Si on a bien reussi a lire un zip
				// byte[] buffer = new byte[2048];
				do {
					String entryName = entry.getName();
					logger.info("Zip entry file : " + entryName);
					// On ne lit pas les fichiers de relations ".rels"
					// if(!entryName.toLowerCase().endsWith(".rels")
					// && entryName.toLowerCase().endsWith(".xml")
					// && !entryName.toLowerCase().contains("content_types")
					// && entryName.toLowerCase().compareTo("core.xml")!=0
					// && !entryName.toLowerCase().endsWith("/core.xml")) {
					// ByteArrayOutputStream bos = new ByteArrayOutputStream();
					// int len;
					// while ((len = zipStream.read(buffer)) > 0)
					// {
					// bos.write(buffer, 0, len);
					// }
					// Pair<List<Pair<String, String>>, HashMap<String, Object>> currentFileRes =
					// readFile(new ByteArrayInputStream( bos.toByteArray() ), entryName);
					// notReadableFiles.addAll(currentFileRes.l());
					// filesAndContent.putAll(currentFileRes.r());
					// bos.close();
					// }
				} while ((entry = zipStream.getNextEntry()) != null);
				zipStream.close();
			} else {
				logger.error("entry not read");
				// MinioDBInfo.upload("testBucket", "h5File.h5", input);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
