package com.jsonlite.jsonlite.exception;

public class MalformedJSONException extends JSONException {

    public MalformedJSONException(){
        super();
    }

    public MalformedJSONException(String message){
        super(message);
    }

    public MalformedJSONException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
