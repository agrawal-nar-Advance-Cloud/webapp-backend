package com.webapp.backend.Exception;

public class AuthorizationException extends Exception{

    public AuthorizationException(){
        super();
    }
    public AuthorizationException(String message){
        super(message);
    }
}
