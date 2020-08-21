package com.jsonlite.jsonlite.exception;

public class JSONException extends RuntimeException {

    public JSONException(){
        super();
    }

    public JSONException(String message){
        super(message);
    }

    public JSONException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
