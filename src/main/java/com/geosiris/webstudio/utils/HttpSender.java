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

import com.geosiris.energyml.pkg.EPCPackage;
import com.geosiris.energyml.pkg.OPCContentType;
import com.geosiris.energyml.pkg.OPCRelsPackage;
import com.geosiris.energyml.utils.EPCGenericManager;
import com.geosiris.energyml.utils.ExportVersion;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.logs.ServerLogMessage.MessageType;
import com.geosiris.webstudio.model.WorkspaceContent;
import com.geosiris.webstudio.property.ConfigurationType;
import com.geosiris.webstudio.servlet.Editor;
import com.google.gson.Gson;
import energyml.content_types.Types;
import energyml.relationships.Relationships;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.codec.binary.Base64;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class HttpSender {
	public static Logger logger = LogManager.getLogger(HttpSender.class);

	private static final String LINE_FEED = "\r\n";

	private static void addFilePart(HttpURLConnection con, String boundary,
									String fieldName, String fileName, String fileContent, PrintWriter writer)
			throws IOException {
		//    	OutputStream outputStream = con.getOutputStream();
		//PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
		//                true);
		writer.append("--").append(boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"; filename=\"").append(fileName).append("\"")
				.append(LINE_FEED);
		writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(fileName))
				.append(LINE_FEED);
		writer.append("Content-Transfer-Encoding: text/plain").append(LINE_FEED);
		writer.append(LINE_FEED);
		writer.flush();

		writer.append(fileContent);
		writer.append(LINE_FEED);
		writer.flush();
		//        writer.close();
	}

	public static void addFormField(String name, String value, String boundary, PrintWriter writer) {
		writer.append("--").append(boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"")
				.append(LINE_FEED);
//		writer.append("Content-Type: text/plain; charset=").append(String.valueOf(StandardCharsets.UTF_8)).append(
//				LINE_FEED);
//		writer.append(LINE_FEED);
		writer.append(value).append(LINE_FEED);
		writer.flush();
	}

	public static String finish(HttpURLConnection con, String boundary, PrintWriter writer) throws IOException {
		StringBuilder response = new StringBuilder();

		writer.flush();
		writer.append("--").append(boundary).append("--").append(LINE_FEED);
		writer.close();


		// checks server's status code first
		int status = con.getResponseCode();
		if (status == HttpURLConnection.HTTP_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line).append("\n");
			}
			reader.close();
			con.disconnect();
		} else {
			throw new IOException("Server returned non-OK status: " + status + " : " + con.getResponseMessage());
		}

		return response.toString();
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
			logger.info(finish(con, boundary, writer));
		} catch (ConnectException e) {
			SessionUtility.log(session, new ServerLogMessage(MessageType.DEBUG, "Workspace database connection exception", SessionUtility.EDITOR_NAME));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static String sendfileWithPostRequest(
			HttpSession session,
			Consumer<OutputStream> f_epc_writer,
			String _url, String login, String pwd,
			String fileParamName, Map<String, String> otherParams) {
		String boundary = "===" + System.currentTimeMillis() + "===";
		String result = "";
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
			writer.append(LINE_FEED).flush();
			writer.append("--").append(boundary).append(LINE_FEED).flush();
			writer.append("Content-Disposition: form-data; name=\"").append(fileParamName).append("\"; filename=\"").append("f.epc").append("\"")
					.append(LINE_FEED).flush();
			writer.append("Content-Type: application/octet-stream")
					.append(LINE_FEED).flush();

			f_epc_writer.accept(outputStream);

			writer.append(LINE_FEED).flush();

			result = finish(con, boundary, writer);
		} catch (ConnectException e) {
			Gson gson = new Gson();
			result = gson.toJson(e);
			SessionUtility.log(session, new ServerLogMessage(MessageType.DEBUG, "Http was sending post request but receive a connection exception", SessionUtility.EDITOR_NAME));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Gson gson = new Gson();
			result = gson.toJson(e);
		}
		return result;
	}

	public static void sendfileWithPostRequest_NoFileName(
			HttpSession session,
			List<String> filesContent,
			String _url, String login, String pwd,
			String fileParamName, HashMap<String, String> otherParams) {
		sendfileWithPostRequest(session, filesContent.stream().map(x -> new Pair<>("fileName", x)).collect(Collectors.toList()),
				_url, login, pwd,
				fileParamName, otherParams);
	}


	public static InputStream sendGet(HttpSession session, String get_url, String login, String pwd, Map<String, String> parameters) {
		try {
			StringBuilder urlWithParameters = new StringBuilder(get_url);

			if(parameters.size()>0) {
				urlWithParameters.append("?");
			}
			for(String key: parameters.keySet()) {
				urlWithParameters.append(key).append("=").append(parameters.get(key)).append("&");
			}

			URL url = new URL(urlWithParameters.toString());
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
			SessionUtility.log(session, new ServerLogMessage(MessageType.DEBUG, "Workspace database connection exception", SessionUtility.EDITOR_NAME));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static WorkspaceContent readFile(final HttpSession session, InputStream input, String fileName) {
        WorkspaceContent result = new WorkspaceContent();

        if (!input.markSupported()) {
            input = new BufferedInputStream(input);
        }
        input.mark(1000);

        Map<String, byte[]> potentialEnergymlFile = new HashMap<>();

        Map<String, String> mapFileNameToContentType = new HashMap<>();

        byte[] buffer = new byte[2048];

        try {
            // On essaie de lire en zip
            ZipInputStream zipStream = new ZipInputStream(input);
            ZipEntry entry = null;
            try {
                entry = zipStream.getNextEntry();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            if (entry != null) { // Si on a bien reussi a lire un zip
                energyml.content_types.Types contentTypeFilecontent = null;
                do {
                    if(!entry.isDirectory()){
                        String entryName = entry.getName();
                        String entryName_lc = entryName.toLowerCase();
                        String entryNameSimple = entryName.contains("/") ? entryName.substring(entryName.indexOf("/") + 1) : entryName;

                        ByteArrayOutputStream entryBOS = new ByteArrayOutputStream();
                        int len;
                        while ((len = zipStream.read(buffer)) > 0) {
                            entryBOS.write(buffer, 0, len);
                        }

                        if (entryName_lc.endsWith(".xml")) {
                            if(entryName_lc.contains("content_types")) {
                                try {
                                    contentTypeFilecontent = (Types) OPCContentType.unmarshal(new ByteArrayInputStream(entryBOS.toByteArray()));

                                    for(Object doo: contentTypeFilecontent.getDefaultOrOverride()){
                                        String refFileName = EPCGenericManager.findUUID((String) ObjectController.getObjectAttributeValue(doo, "PartName"));
                                        if(refFileName != null)
                                            mapFileNameToContentType.put(refFileName, (String) ObjectController.getObjectAttributeValue(doo, "ContentType"));
                                    }
                                    logger.info("CONTENT_TYPE found, contains " + contentTypeFilecontent.getDefaultOrOverride().size() + " entries");
                                }catch (Exception e){
                                    logger.debug(e.getMessage(), e);
                                }
                            }else if(!(entryName_lc.compareTo("core.xml") == 0
                                    || entryName_lc.endsWith("/core.xml"))
                            ){ // if not a specific epc file
                                potentialEnergymlFile.put(entryName, entryBOS.toByteArray());
                            }
                        }else if(entryName_lc.endsWith(".rels")){
                            if (entryName_lc.contains("externalpartreference")) {
                                logger.debug("Reading rels '" + entryNameSimple + "'");
                                try {
                                    Relationships rels = OPCRelsPackage.parseRels(new ByteArrayInputStream(entryBOS.toByteArray()));
                                    result.getParsedRels().put(entryNameSimple, rels);
                                } catch (Exception e) {
                                    logger.debug(e.getMessage(), e);
                                }
                            }
                        }else{ // other files
                            logger.error("not resqml readable objet '" + entry + "' named : '" + entryName + "' ");
                            result.getNotReadObjects().add(new Pair<>(entryName, entryBOS.toByteArray()));
                        }
                    }
                } while ((entry = zipStream.getNextEntry()) != null);
                zipStream.close();

            } else { // On a pas reussi a lire le zip, on test en tant que fichier normal

                // On doit reset l'iterateur de lecture qui a ete deplace lors de la tentative de lecture en zip
                try {input.reset();} catch (Exception ignore){}

                String fileName_lc = fileName.toLowerCase();

                ByteArrayOutputStream entryBOS = new ByteArrayOutputStream();
                int len;
                while ((len = input.read(buffer)) > 0) {
                    entryBOS.write(buffer, 0, len);
                }

                if (fileName_lc.endsWith(".xml") || fileName.trim().length() <= 0) {
                    if(!(fileName_lc.contains("content_types")
                            || fileName_lc.compareTo("core.xml") == 0
                            || fileName_lc.endsWith("/core.xml"))
                    ){ // if not a specific epc file
                        potentialEnergymlFile.put(fileName, entryBOS.toByteArray());
                    }
                }else if(fileName_lc.endsWith(".rels")){
                    if (fileName_lc.contains("externalpartreference")) {
                        logger.debug("Reading rels '" + fileName + "'");
                        try {
                            Relationships rels = OPCRelsPackage.parseRels(new ByteArrayInputStream(entryBOS.toByteArray()));
                            result.getParsedRels().put(fileName, rels);
                        } catch (JAXBException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }else{ // other files
                    if(fileName.trim().length() <=0){
                        fileName = "UknownFile_" + UUID.randomUUID();
                    }
                    logger.error("not resqml readable objet named : '" + fileName + "' ");
                    result.getNotReadObjects().add(new Pair<>(fileName, entryBOS.toByteArray()));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        List<Pair<String, Object>> ingester = potentialEnergymlFile.entrySet().parallelStream()
                .map((entry) -> {
                    JAXBElement<?> jxbElt = null;

                    Pair<String, String> pkgIdAndVersion = null;
                    String uuid = EPCGenericManager.findUUID(entry.getKey());
                    if(mapFileNameToContentType.containsKey(uuid)){
                        String contentTypeFound = mapFileNameToContentType.get(uuid);
                        pkgIdAndVersion = EPCGenericManager.getDomainAndVersionFromContentType(contentTypeFound);
                    }

                    if(pkgIdAndVersion != null){
                        for(EPCPackage pkg: Editor.pkgManager.PKG_LIST){
                            if(pkg.getDomain().compareToIgnoreCase(pkgIdAndVersion.l()) == 0
                                    && pkg.getDomainVersion().compareTo(pkgIdAndVersion.r()) == 0){
                                try {
                                    jxbElt =  pkg.parseXmlContent(new String(entry.getValue(), StandardCharsets.UTF_8), false);
                                }catch(Exception e){logger.error(e.getMessage(), e);}
                                if(jxbElt != null && jxbElt.getValue() != null){
                                    break;
                                }else{
                                    jxbElt = null;
                                }
                            }
                        }
                        // if version was 2.0 but pkg version is 2.0.1
                        if(jxbElt == null){
                            for(EPCPackage pkg: Editor.pkgManager.PKG_LIST){
                                if(pkg.getDomain().compareToIgnoreCase(pkgIdAndVersion.l()) == 0
                                        && pkg.getDomainVersion().startsWith(pkgIdAndVersion.r())){
                                    try {
                                        jxbElt =  pkg.parseXmlContent(new String(entry.getValue(), StandardCharsets.UTF_8), false);
                                    }catch(Exception e){logger.error(e.getMessage(), e);}
                                    if(jxbElt != null && jxbElt.getValue() != null){
                                        break;
                                    }else{
                                        jxbElt = null;
                                    }
                                }
                            }
                        }
                    }
                    logger.debug("failed to parse file with contentType info " + entry.getKey() +" -->  " + EPCGenericManager.findUUID(entry.getKey()) + " == " + mapFileNameToContentType.size());
                    try {
                        assert pkgIdAndVersion != null;
                        logger.debug(pkgIdAndVersion.l() + " ==> " + pkgIdAndVersion.r());
                    }catch (Exception e){
                        logger.debug("Not pkgIdAndVersion for " + mapFileNameToContentType.get(entry.getKey()));
                    }

                    // Not worked with contentType found package, we try with others
                    if(jxbElt == null) {
                        try {
                            logger.debug("Try to read " + entry.getKey());
                            jxbElt = Editor.pkgManager.unmarshal(entry.getValue());
                        } catch (Exception e) {
                            logger.debug(e.getMessage(), e);
                        }
                    }

                    if (jxbElt != null && jxbElt.getValue() != null) {
                        Object energymlObj = jxbElt.getValue();
                        if (SessionUtility.configIsMoreVerborseThan(ConfigurationType.verbose))
                            logger.debug(energymlObj);
                        return new Pair<>((String) ObjectController.getObjectAttributeValue(energymlObj, "uuid"), energymlObj);
                    }else{
                        return new Pair<String, Object>(entry.getKey(), entry.getValue());
                    }
                }).collect(Collectors.toList());

        for(Pair<String, Object> p : ingester){
            if(p.r() instanceof byte[]){
                result.getNotReadObjects().add(new Pair<>(p.l(), (byte[])p.r()));
            }else if(p.r() != null){
                result.getReadObjects().put(p.l(), p.r());
            }else{
                SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.ERROR,
                        "File " + p.l() + " not added to the workspace (internal error, you can try UTF-8)",
                        SessionUtility.EDITOR_NAME));
            }
        }

        return result;
    }


	public static void writeEpcAsRequestResponse(HttpServletResponse response, WorkspaceContent workspace, String filePath, ExportVersion exportVersion, String mimeType){
		try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                exportEPCFile(bos,
                        workspace,
                        exportVersion);
                byte[] bos_bytes = bos.toByteArray();

                if (mimeType == null) {
                    // set to binary type if MIME mapping not found
                    mimeType = "application/octet-stream";
                }

                response.setContentType(mimeType);
//                response.setContentLength((int) downloadFile.length());
                response.setContentLength(bos_bytes.length);

                // forces download
                String headerKey = "Content-Disposition";
                String headerValue = String.format("attachment; filename=\"%s\"", filePath);
                response.setHeader(headerKey, headerValue);

                // obtains response's output stream
                OutputStream outStream = response.getOutputStream();

                for(int chunk=0; chunk<bos_bytes.length; chunk+= 4096){
                    outStream.write(bos_bytes, chunk, Math.min(4096, bos_bytes.length-chunk));
                }

                outStream.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
	}

	public static void exportEPCFile(OutputStream out,
                                     WorkspaceContent workspace,
                                     ExportVersion exportVersion){
        logger.info("@exportEPCFile");
        try {
            try(ZipOutputStream epc = new ZipOutputStream(out)) {
                for (Map.Entry<String, Object> kv : workspace.getReadObjects().entrySet()) {
                    ZipEntry ze_resqml = new ZipEntry(EPCGenericManager.genPathInEPC(kv.getValue(), exportVersion));
                    epc.putNextEntry(ze_resqml);
                    Editor.pkgManager.marshal(kv.getValue(), epc);
                    epc.closeEntry();
                }
                EPCGenericManager.exportRels(workspace.getReadObjects(), workspace.getParsedRels(), exportVersion, epc, "Geosiris Resqml WebStudio");

                if (workspace.getNotReadObjects() != null) {
                    /// non resqml obj
                    for (Pair<String, byte[]> nonResqmlObj : workspace.getNotReadObjects()) {
                        ZipEntry ze_resqml = new ZipEntry(nonResqmlObj.l());
                        epc.putNextEntry(ze_resqml);
                        try {
                            byte[] fileContent = nonResqmlObj.r();
                            for(int chunk=0; chunk<fileContent.length; chunk += 4096){
                                epc.write(fileContent, chunk, Math.min(4096, fileContent.length-chunk));
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            logger.error("nonResqmlObj " + nonResqmlObj.l());
                        }
                        epc.closeEntry();
                    }
                }
//                epc.finish();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
