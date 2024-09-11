package com.geosiris.webstudio.exeptions;

public class ETPUploadFailed extends Exception{
    public ETPUploadFailed(String msg){
        super("ETPUploadFailed : " + msg);
    }
}
