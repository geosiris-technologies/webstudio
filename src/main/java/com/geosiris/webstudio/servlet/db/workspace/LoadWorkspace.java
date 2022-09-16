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
package com.geosiris.webstudio.servlet.db.workspace;

import com.geosiris.energyml.pkg.OPCRelsPackage;
import com.geosiris.energyml.utils.Pair;
import com.geosiris.storage.cloud.api.config.AzureBlobStorageConfig;
import com.geosiris.storage.cloud.api.config.GoogleCloudStorageConfig;
import com.geosiris.storage.cloud.api.config.S3Config;
import com.geosiris.storage.cloud.api.property.AzureBlobStorageProperties;
import com.geosiris.storage.cloud.api.property.GoogleCloudStorageProperties;
import com.geosiris.storage.cloud.api.request.*;
import com.geosiris.storage.cloud.api.service.StorageService;
import com.geosiris.webstudio.model.WorkspaceContent;
import com.geosiris.webstudio.property.WorkspaceProperties;
import com.geosiris.webstudio.servlet.Editor;
import com.geosiris.webstudio.servlet.FileReciever;
import com.geosiris.webstudio.utils.SessionUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servlet implementation class Connection
 */
@WebServlet("/loadworkspace")
public class LoadWorkspace extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static Logger logger = LogManager.getLogger(LoadWorkspace.class);

    private static final String folderName_additionalData = "additionalData";
    public static StorageService storageService = getStorageService();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoadWorkspace() {
        super();
    }

    public static void updateWorkspace(HttpSession session, List<String> uuidList) {
        if(SessionUtility.wsProperties.getEnableWorkspace() && SessionUtility.wsProperties.getEnableUserDB()){
            String userName = (String) session.getAttribute(SessionUtility.SESSION_USER_NAME);
            if (userName != null && userName.length() > 0) {
                WorkspaceContent workspace = SessionUtility.getWorkspaceContent(session);
                Map<String, Object> map = workspace.getReadObjects();
                for (String uuid : uuidList) {
                    if (map.containsKey(uuid)) {
                        String fileContent = Editor.pkgManager.marshal(map.get(uuid));
                        try {
                            storageService.uploadFileAsync(new UploadFileRequest(new ByteArrayInputStream(fileContent.getBytes()),
                                    userName, uuid + ".xml", "text/xml", null));
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        // DELETE FILE
                        logger.error("File doesn't exist '" + uuid + "'");
                    }
                }

                logger.info("Rels nb " + workspace.getParsedRels().size());
                for (String relsName : workspace.getParsedRels().keySet()) {
                    logger.info("Trying to store rels " + relsName);
                    try {
                        String fileContent = OPCRelsPackage.marshal(workspace.getParsedRels().get(relsName));
                        storageService.uploadFileAsync(new UploadFileRequest(new ByteArrayInputStream(fileContent.getBytes()),
                                userName + "/rels", relsName, "text/xml", null));
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                // sendfiletoWorkspace(session, userName, filesContent);
            }
        }
    }
    public static void updateWorkspaceNotEnergymlFiles(HttpSession session) {
        if(SessionUtility.wsProperties.getEnableWorkspace() && SessionUtility.wsProperties.getEnableUserDB()){
            String userName = (String) session.getAttribute(SessionUtility.SESSION_USER_NAME);
            if (userName != null && userName.length() > 0) {
                // starting by removing all previous data
                storageService.deleteFile(new DeleteFileRequest(userName + "/"+folderName_additionalData, null));
                WorkspaceContent workspace = SessionUtility.getWorkspaceContent(session);

                for (Pair<String, byte[]> additionalData : workspace.getNotReadObjects()) {
                    logger.info("Trying to store additionalData " + additionalData);
                    try {
                        storageService.uploadFileAsync(new UploadFileRequest(new ByteArrayInputStream(additionalData.r()),
                                userName + "/" + folderName_additionalData, additionalData.l(), getMimetype(additionalData.l()), null));
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    public static void updateWorkspace(HttpSession session, String uuid) {
        if(SessionUtility.wsProperties.getEnableWorkspace() && SessionUtility.wsProperties.getEnableUserDB()){
            List<String> uuidList = new ArrayList<>();
            uuidList.add(uuid);
            updateWorkspace(session, uuidList);
        }
    }

    public static void clearWorkspace(HttpSession session) {
        if(SessionUtility.wsProperties.getEnableWorkspace() && SessionUtility.wsProperties.getEnableUserDB()){
            String userName = (String) session.getAttribute(SessionUtility.SESSION_USER_NAME);
            try {
                storageService.deleteFile(new DeleteFileRequest(userName + "/*", null));
            }catch (Exception e){
                logger.error(e.getMessage(), e);
            }
        }
    }

    public static void removeFileFromWorkspace(HttpSession session, String rootUUID) {
        if(SessionUtility.wsProperties.getEnableWorkspace() && SessionUtility.wsProperties.getEnableUserDB()){
            String userName = (String) session.getAttribute(SessionUtility.SESSION_USER_NAME);
            try {
                storageService.deleteFile(new DeleteFileRequest(userName + "/" + rootUUID, null));
            }catch (Exception e){
                logger.error(e.getMessage(), e);
            }
        }
    }
    public static boolean removeAdditionalFileFromWorkspace(HttpSession session, String filePath) {
        if(SessionUtility.wsProperties.getEnableWorkspace() && SessionUtility.wsProperties.getEnableUserDB()){
            String userName = (String) session.getAttribute(SessionUtility.SESSION_USER_NAME);
            logger.info("Removing file " + filePath + " ");
            try {
                DeleteFileResponse dfr = storageService.deleteFile(new DeleteFileRequest(userName + "/" + folderName_additionalData + "/" + filePath, null));
                logger.info(dfr);
                return dfr.isResult();
            }catch (Exception e){
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    public static void reloadWorkspaceAdditionalFiles(HttpSession session){
        if(SessionUtility.wsProperties.getEnableWorkspace() && SessionUtility.wsProperties.getEnableUserDB()){
            try {
                List<Pair<String, byte[]>> additionalFiles = new ArrayList<>();
                if (storageService != null) {
                    String userName = (String) session.getAttribute(SessionUtility.SESSION_USER_NAME);
                    ListFilesResponse resp = storageService.listFiles(new ListFilesRequest(userName, null));
                    SessionUtility.getNotResqmlObjects(session).clear();
                    for (String fileName : resp.getFileList()) {
                        String realFileName = fileName.substring(fileName.indexOf("/") + 1); // removing user folder name
                        realFileName = realFileName.substring(realFileName.indexOf("/") + 1);// removing "folderName_additionalData" name
                        if (fileName.substring(fileName.indexOf("/") + 1).startsWith(folderName_additionalData + "/")) {
                            additionalFiles.add(new Pair<>(realFileName, storageService.getFile(new GetFileRequest(fileName, null)).getContent()));
                        }
                    }
                    SessionUtility.getNotResqmlObjects(session).addAll(additionalFiles);
                }
            }catch (Exception e){
                logger.error(e.getMessage(), e);
            }
        }
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
        if (storageService != null) {
            HttpSession session = request.getSession(false);
            String userName = (String) session.getAttribute(SessionUtility.SESSION_USER_NAME);
            ListFilesResponse resp = storageService.listFiles(new ListFilesRequest(userName, null));

            if(resp != null) {
                List<Pair<String, byte[]>> additionalFiles = new ArrayList<>();
                if (resp.getFileList().length > 0) {
                    List<Pair<String, String>> workspaceFilesContent = new ArrayList<>();
                    logger.info("File From workspace : ");
                    for (String fileName : resp.getFileList()) {
                        logger.info(" ==> " + fileName + " -- ");

                        String realFileName = fileName.substring(fileName.indexOf("/") + 1); // removing user folder name
                        realFileName = realFileName.substring(realFileName.indexOf("/") + 1);// removing "folderName_additionalData" name

                        if (fileName.substring(fileName.indexOf("/") + 1).startsWith(folderName_additionalData + "/")) {
                            // Non energyml file
                            additionalFiles.add(new Pair<>(realFileName, storageService.getFile(new GetFileRequest(fileName, null)).getContent()));
                        } else {
                            try {
                                workspaceFilesContent.add(new Pair<>(fileName,
                                        new String(storageService.getFile(new GetFileRequest(fileName, null)).getContent(),
                                                StandardCharsets.UTF_8)));
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                    FileReciever.loadFiles(session, workspaceFilesContent, false, false, false);
                    SessionUtility.getNotResqmlObjects(session).addAll(additionalFiles);
                }
            }else{
                logger.error("Error with storage service. Answer to 'listFiles' request for user '" + userName + "' failed. " +
                        "Please check the storage service credentials.");
            }
        }
        // ELSE No storage service configured

        response.sendRedirect("editor");
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    public static String getMimetype(String filePath){
        String res = URLConnection.guessContentTypeFromName(filePath);
        return res==null ? "application/octet-stream" : res;
//        return (new File(filePath)).toURI().openConnection().getContentType();
    }

    public static StorageService getStorageService() {
        if(SessionUtility.wsProperties.getEnableWorkspace()) {
            WorkspaceProperties workspaceProp = new WorkspaceProperties();
            try {
                switch (workspaceProp.getDatabaseType()) {
                    case s3:
                        return S3Config.S3Service();
                    case google:
                        GoogleCloudStorageConfig googleConfig = new GoogleCloudStorageConfig();
                        return googleConfig.googleCloudStorageService(googleConfig.storageClient(new GoogleCloudStorageProperties()));
                    case azure:
                        AzureBlobStorageConfig azureConfig = new AzureBlobStorageConfig();
                        return azureConfig.azureBlobStorageService(azureConfig.blobServiceClient(new AzureBlobStorageProperties()));
                    case existdb:
                    case dropbox:
                    default:
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public static void main(String[] argv){

        logger.info("a/b/c.png => " + getMimetype("a/b/c.png"));
        logger.info("a/b/c.pdf => " + getMimetype("a/b/c.pdf"));
        logger.info("a/b/c.docx => " + getMimetype("a/b/c.docx"));
        logger.info("a/b/c.doc => " + getMimetype("a/b/c.doc"));
        logger.info("a/b/c.txt => " + getMimetype("a/b/c.txt"));
        logger.info("a/b/c.csv => " + getMimetype("a/b/c.csv"));
        logger.info("a/b/c.xml => " + getMimetype("a/b/c.xml"));
    }

}
