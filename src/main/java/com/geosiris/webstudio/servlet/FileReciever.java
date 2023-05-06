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

import com.geosiris.energyml.pkg.EPCPackage;
import com.geosiris.energyml.pkg.OPCContentType;
import com.geosiris.energyml.pkg.OPCRelsPackage;
import com.geosiris.energyml.utils.EPCGenericManager;
import com.geosiris.energyml.utils.ObjectController;
import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.model.WorkspaceContent;
import com.geosiris.webstudio.property.ConfigurationType;
import com.geosiris.webstudio.servlet.db.workspace.LoadWorkspace;
import com.geosiris.webstudio.utils.SessionUtility;
import energyml.content_types.Types;
import energyml.relationships.Relationships;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Servlet implementation class FileReceiver
 */
@WebServlet("/FileReciever")
@MultipartConfig
public class FileReciever extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(FileReciever.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileReciever() {
        super();
    }

    public static String loadFiles_Unnamed(HttpSession session, List<String> filesContent, boolean isClose, boolean isImport, boolean updateStorage) {
        WorkspaceContent loadedEPC = new WorkspaceContent();
        for (String currentFile : filesContent) {
            WorkspaceContent currentFileResult = readFile(session,
                    new ByteArrayInputStream(currentFile.getBytes()), "");
            loadedEPC.putAll(currentFileResult);
        }
        return _updateWorkspaceContent(session, loadedEPC, isClose, isImport, updateStorage);
    }

    public static String loadFiles(HttpSession session, List<Pair<String, String>> filesContent, boolean isClose, boolean isImport, boolean updateStorage) {
        WorkspaceContent loadedEPC = new WorkspaceContent();
        for (Pair<String, String> currentFile : filesContent) {
            WorkspaceContent currentFileResult = readFile( session,
                    new ByteArrayInputStream(currentFile.r().getBytes()), currentFile.l());
            loadedEPC.putAll(currentFileResult);
        }
        return _updateWorkspaceContent(session, loadedEPC, isClose, isImport, updateStorage);
    }

    private static String _updateWorkspaceContent(HttpSession session,
                                                  WorkspaceContent loadedEPC, boolean isClose, boolean isImport, boolean updateStorage) {
        logger.info("loadedEPC " + loadedEPC.getReadObjects().size());

        String resultAnswer = "";
        if (isClose) {
            resultAnswer += "Cleaning workspace";
            session.setAttribute(SessionUtility.SESSION_WORKSPACE_DATA_ID, new WorkspaceContent());
        } else {
            if (!isImport) {
                if (updateStorage) // It occures when not loading workspace at connexion
                    closeEPC(session);
                resultAnswer += "Updating workspace";
                logger.info("\tUpdating workspace");
                session.setAttribute(SessionUtility.SESSION_WORKSPACE_DATA_ID, loadedEPC);
            } else {
                resultAnswer += "Importing data";
                logger.info("\tImporting data");
                SessionUtility.getWorkspaceContent(session).putAll(loadedEPC);
            }
            List<String> uuidList = new ArrayList<>(loadedEPC.getReadObjects().keySet());
            if (updateStorage) {
                LoadWorkspace.updateWorkspace(session, uuidList);
                LoadWorkspace.updateWorkspaceNotEnergymlFiles(session);
            }
        }

        // Logs for not readed files
        if (loadedEPC.getNotReadObjects().size() > 0) {
            SessionUtility.log(session,
                    new ServerLogMessage(ServerLogMessage.MessageType.ERROR,
                            "Not read files : "
                                    + loadedEPC.getNotReadObjects().stream().map(Pair::l).collect(Collectors.joining(", ")),
                            SessionUtility.EDITOR_NAME));
        }

        logger.debug("#> NB rels found :" + SessionUtility.getWorkspaceContent(session).getParsedRels().size());
        logger.debug("#> NB obj found :" + SessionUtility.getWorkspaceContent(session).getReadObjects().size());

        return resultAnswer;
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
    public static void closeEPC(HttpSession session) {
        logger.info("Closing epc");
        session.setAttribute(SessionUtility.SESSION_WORKSPACE_DATA_ID, null);
        LoadWorkspace.clearWorkspace(session);
    }


    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionUtility.tryConnectServlet(request, response)) {
            return;
        }
        HttpSession session = request.getSession(false);

        for (String paramName : request.getParameterMap().keySet()) {
            if (paramName.compareTo("close") == 0) {
                if (request.getParameterValues("close")[0].toLowerCase().compareTo("true") == 0) {
                    closeEPC(session);
                    break;
                }
            }
        }
        getServletContext().getRequestDispatcher("/jsp/epcContentView.jsp").forward(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!SessionUtility.tryConnectServlet(request, response)) {
            return;
        }
        HttpSession session = request.getSession(false);

        if (ServletFileUpload.isMultipartContent(request)) {

            WorkspaceContent loadedEPC = new WorkspaceContent();

            boolean isImport = false;
            boolean isClose = false;

            String resultAnswer = "";

            // Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();

            ServletFileUpload upload = new ServletFileUpload(factory);

            logger.info("==> Start Loading");
            long timelapsBegin = System.currentTimeMillis();
            try {
                FileItemIterator iterator = upload.getItemIterator(request);
                while (iterator.hasNext()) {
                    FileItemStream item = iterator.next();
                    InputStream stream = item.openStream();

                    if (item.isFormField() && item.getFieldName().compareTo("epcInputURL") == 0) {
                        String fieldValue = Streams.asString(stream);

                        if (fieldValue.length() > 0) {
                            try {
                                URL epcURL = new URL(fieldValue);
                                InputStream epcFile = new BufferedInputStream(epcURL.openStream());
                                WorkspaceContent currentFile = readFile(session, epcFile, "");
                                loadedEPC.putAll(currentFile);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }

                    } else if (item.getFieldName().compareTo("epcInputFile") == 0
                            || item.getFieldName().compareTo("epcInputFile[]") == 0 
                            || item.getFieldName().compareTo("file") == 0 
                            || item.getFieldName().compareTo("files[]") == 0) {
                        if (item.getName() != null && item.getName().length() > 0) {
                            logger.info("File input : " + item.getName());
                            if (item.getName().toLowerCase().endsWith(".h5")) {
                                SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.ERROR, "H5 Recieved",
                                        SessionUtility.EDITOR_NAME));
                                try {
                                    File h5File = new File(
                                            "/store/" + session.getAttribute(SessionUtility.SESSION_USER_NAME) + "/"
                                                    + item.getName());
                                    logger.info("Trying to create file " + h5File.getAbsolutePath());
                                    h5File.mkdirs();
                                    if (!h5File.createNewFile()) {
                                        h5File.delete();
                                        h5File.createNewFile();
                                    }
                                    OutputStream outStream = new FileOutputStream(h5File);

                                    byte[] buffer = new byte[8 * 1024];
                                    int bytesRead;
                                    while ((bytesRead = stream.read(buffer)) != -1) {
                                        outStream.write(buffer, 0, bytesRead);
                                    }
                                    outStream.close();
                                    stream.close();
                                    SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.ERROR,
                                            "H5 saved at " + h5File.getAbsolutePath(), SessionUtility.EDITOR_NAME));
                                } catch (Exception e) {
                                    SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.ERROR,
                                            "ERROR saving h5: " + e.getMessage() + "\n", SessionUtility.EDITOR_NAME));
                                    logger.error(e.getMessage(), e);
                                }
                            } else {
                                WorkspaceContent currentFile = readFile(session, stream, item.getName());
                                loadedEPC.putAll(currentFile);
                            }
                        }
                    } else if (item.getFieldName().compareTo("import") == 0) {
                        String fieldValue = Streams.asString(stream);
                        if (fieldValue.toLowerCase().compareTo("true") == 0) {
                            isImport = true;
                        }
                    } else if (item.getFieldName().compareTo("close") == 0) {
                        String fieldValue = Streams.asString(stream);
                        if (fieldValue.toLowerCase().compareTo("true") == 0) {
                            isClose = true;
                            // We close the epc and remove all data
                            closeEPC(session);
                        }
                    } else {
                        logger.error("FileReciever : not readable parameter : '" + item.getFieldName()
                                + "' for file " + item.getName());
                    }
                }

            } catch (FileUploadException e) {
                logger.error(e.getMessage(), e);
            }

            logger.info("==> end Loading");
            long timelapsEnd = System.currentTimeMillis();
            SessionUtility.log(session, new ServerLogMessage(ServerLogMessage.MessageType.DEBUG,
                    "==> Timelaps : " + (timelapsEnd - timelapsBegin) + "ms", "WS"));
            logger.info("==> Timelaps : " + (timelapsEnd - timelapsBegin) + "ms");

            resultAnswer = _updateWorkspaceContent(request.getSession(false), loadedEPC, isClose, isImport, true);

            PrintWriter out = response.getWriter();
            response.setContentType("application/text");
            response.setCharacterEncoding("UTF-8");
            out.write(resultAnswer);
            out.flush();
        } else {
            PrintWriter out = response.getWriter();
            response.setContentType("application/text");
            response.setCharacterEncoding("UTF-8");
            out.write("Wrong input");
            out.flush();
        }
    }
}
