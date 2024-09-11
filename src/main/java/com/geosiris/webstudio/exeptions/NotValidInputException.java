package com.geosiris.webstudio.exeptions;

public class NotValidInputException extends Exception{
    public NotValidInputException(String msg){
        super("NotValidInputException : " + msg);
    }
}
