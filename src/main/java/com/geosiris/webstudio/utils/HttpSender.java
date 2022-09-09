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
package com.geosiris.webstudio.utils;

import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.logs.ServerLogMessage.MessageType;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.codec.binary.Base64;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpSender {
    public static Logger logger = LogManager.getLogger(HttpSender.class);

	private static final String LINE_FEED = "\r\n";

	private static void addFilePart(HttpURLConnection con, String boundary,
			String fieldName, String fileName, String fileContent, PrintWriter writer)
					throws IOException {
		//    	OutputStream outputStream = con.getOutputStream();
		//PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
		//                true);
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append(
				"Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"")
		.append(LINE_FEED);
		writer.append(
				"Content-Type: " + URLConnection.guessContentTypeFromName(fileName))
		.append(LINE_FEED);
		writer.append("Content-Transfer-Encoding: text/plain").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();

		writer.append(fileContent);
		writer.append(LINE_FEED);
		writer.flush(); 
		//        writer.close();
	}

	private static void addFormField(String name, String value, String boundary, PrintWriter writer) {
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
		.append(LINE_FEED);
		writer.append("Content-Type: text/plain; charset=" + StandardCharsets.UTF_8).append(
				LINE_FEED);
		writer.append(LINE_FEED);
		writer.append(value).append(LINE_FEED);
		writer.flush();
	}

	private static List<String> finish(HttpURLConnection con, String boundary, PrintWriter writer) throws IOException {
		List<String> response = new ArrayList<String>();

		writer.flush();
		writer.append("--" + boundary + "--").append(LINE_FEED);
		writer.close();


		// checks server's status code first
		int status = con.getResponseCode();
		if (status == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				response.add(line);
			}
			reader.close();
			con.disconnect();
		} else {
			throw new IOException("Server returned non-OK status: " + status + " : " + con.getResponseMessage());
		}

		return response;
	}

	public static void sendfileWithPostRequest(
			HttpSession session,
			List<Pair<String, String>> filesNameAndContent,
			String _url, String login, String pwd,
			String fileParamName, HashMap<String, String> otherParams) {
		String boundary = "===" + System.currentTimeMillis() + "===";

		try {
			URL url = new URL(_url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			if(login!=null && login.length()>0) {
				String auth = login + ":" + pwd;
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
				String authHeaderValue = "Basic " + new String(encodedAuth);
				con.setRequestProperty("Authorization", authHeaderValue);
			}

			con.setUseCaches(false);
			con.setDoOutput(true); // indicates POST method
			con.setDoInput(true);
			con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary+"");


			OutputStream outputStream = con.getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

			for(String paramName : otherParams.keySet()) {
				logger.error("Setting param '"+paramName+"' with value : '" + otherParams.get(paramName)+"'");
				addFormField(paramName, otherParams.get(paramName), boundary, writer);
			}

			for(Pair<String,String> fNameAndContent : filesNameAndContent) {
				addFilePart(con, boundary, fileParamName, fNameAndContent.l(), fNameAndContent.r(), writer);
			}
//			if(!WebStudioConfig.ENV_VAR_IS_IN_PRODUCTION)
			logger.info(finish(con, boundary, writer).stream().reduce("", (subtotal, element) -> subtotal + "\n"+ element));
		} catch (ConnectException e) {
			SessionUtility.log(session, new ServerLogMessage(MessageType.DEBUG, "Workspace database connection exception", SessionUtility.WORKSPACE_NAME));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void sendfileWithPostRequest_NoFileName(
			HttpSession session,
			List<String> filesContent, 
			String _url, String login, String pwd,
			String fileParamName, HashMap<String, String> otherParams) {
		sendfileWithPostRequest(session, filesContent.stream().map(x -> new Pair<String, String>("fileName", x)).collect(Collectors.toList()), 
				_url, login, pwd,
				fileParamName, otherParams);	
	}


	public static InputStream sendGet(HttpSession session, String get_url, String login, String pwd, Map<String, String> parameters) {
		try {
			String urlWithParameters = get_url;

			if(parameters.size()>0) {
				urlWithParameters += "?";
			}
			for(String key: parameters.keySet()) {
				urlWithParameters+=key+"="+parameters.get(key) + "&";
			}

			URL url = new URL(urlWithParameters);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			if(login!=null && login.length()>0) {
				String auth = login + ":" + pwd;
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
				String authHeaderValue = "Basic " + new String(encodedAuth);
				con.setRequestProperty("Authorization", authHeaderValue);
			}

			con.setRequestMethod("GET");
			con.setDoOutput(true);

			int responseCode = con.getResponseCode();
			logger.info("GET Response Code :: " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				return con.getInputStream();

			} else {
				logger.info("GET request not worked");
			}
		} catch (ConnectException e) {
			SessionUtility.log(session, new ServerLogMessage(MessageType.DEBUG, "Workspace database connection exception", SessionUtility.WORKSPACE_NAME));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

}
