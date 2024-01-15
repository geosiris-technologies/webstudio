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
package com.geosiris.webstudio.servlet.editing;

import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.logs.ServerLogMessage;
import com.geosiris.webstudio.model.WorkspaceContent;
import com.geosiris.webstudio.servlet.db.workspace.LoadWorkspace;
import com.geosiris.webstudio.utils.HttpSender;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            WorkspaceContent currentFileResult = HttpSender.readFile(session,
                    new ByteArrayInputStream(currentFile.getBytes()), "");
            loadedEPC.putAll(currentFileResult);
        }
        return _updateWorkspaceContent(session, loadedEPC, isClose, isImport, updateStorage);
    }

    public static String loadFiles(HttpSession session, List<Pair<String, String>> filesContent, boolean isClose, boolean isImport, boolean updateStorage) {
        WorkspaceContent loadedEPC = new WorkspaceContent();
        for (Pair<String, String> currentFile : filesContent) {
            WorkspaceContent currentFileResult = HttpSender.readFile( session,
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
                                WorkspaceContent currentFile = HttpSender.readFile(session, epcFile, "");
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
                                WorkspaceContent currentFile = HttpSender.readFile(session, stream, item.getName());
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
                    } else if (item.getFieldName().compareTo("loadDefault") == 0) {
                        String fieldValue = Streams.asString(stream);
                            System.out.println("Loading url EPC default : fieldValue == " + fieldValue);
                        if (fieldValue.toLowerCase().compareTo("true") == 0) {
                            System.out.println("Loading url EPC default : " + SessionUtility.wsProperties.getDefaultDataEPCUrl());
                            try {
                                URL epcURL = new URL(SessionUtility.wsProperties.getDefaultDataEPCUrl());
                                InputStream epcFile = new BufferedInputStream(epcURL.openStream());
                                WorkspaceContent currentFile = HttpSender.readFile(session, epcFile, "");
                                loadedEPC.putAll(currentFile);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
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
