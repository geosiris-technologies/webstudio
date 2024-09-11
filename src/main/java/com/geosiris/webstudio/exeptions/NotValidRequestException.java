package com.geosiris.webstudio.exeptions;

public class NotValidRequestException extends Exception{

    public NotValidRequestException(String msg){
        super("NotValidRequestException : " + msg);
    }
}

