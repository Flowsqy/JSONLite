package com.jsonlite.jsonlite.node;

import com.jsonlite.jsonlite.exception.SerializeJSONException;
import com.jsonlite.jsonlite.parser.JSONParser;

import java.util.HashMap;
import java.util.Map;

public class JSONValue <T> implements JSONNode {

    private T value;

    public JSONValue(){
        this(null);
    }

    public JSONValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @SuppressWarnings({"unchecked"})
    public <C> C get(Class<C> clazz){
        return (C) value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "JSONValue{" +
                "value=" + value +
                '}';
    }

    @Override
    public String serialize(boolean prettyPrinting, String indent) {
        if(value == null)
            return "null";
        if (
                value instanceof Boolean ||
                value instanceof Integer ||
                value instanceof Long ||
                value instanceof Float ||
                value instanceof Double

        ){
            return value.toString();
        }
        else if(value instanceof String){
            return JSONParser.quote((String) value);
        }

        throw new SerializeJSONException("Incompatible type : "+ value.getClass().getName()+" . Only boolean, number and string are allowed");
    }

    @Override
    public JSONValue<T> asValue() {
        return this;
    }

    @Override
    public JSONObject asObject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONArray asArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> toFlatMap() {
        final Map<String, Object> value = new HashMap<>();
        value.put("", this.value);
        return value;
    }
}
