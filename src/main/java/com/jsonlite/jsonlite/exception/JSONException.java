package com.jsonlite.jsonlite.exception;

public class JSONException extends Exception{

    public JSONException(){
        super();
    }

    public JSONException(String message){
        super(message);
    }

    public JSONException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public final void throwException(){
        try {
            throw this;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
