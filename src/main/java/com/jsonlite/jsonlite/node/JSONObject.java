package com.jsonlite.jsonlite.node;

import com.jsonlite.jsonlite.parser.JSONParser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class JSONObject implements JSONNode, Iterable<Map.Entry<String, JSONNode>> {

    private Map<String, JSONNode> keys;

    public JSONObject() {
        this(new HashMap<>());
    }

    public JSONObject(Map<String, JSONNode> keys) {
        this.keys = keys;
    }

    public Map<String, JSONNode> getKeys() {
        return keys;
    }

    public void setKeys(Map<String, JSONNode> keys) {
        this.keys = keys;
    }

    @Override
    public String toString() {
        return "JSONObject{" +
                "keys=" + keys +
                '}';
    }

    @Override
    public String serialize(boolean prettyPrinting, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        keys.entrySet().forEach(new Consumer<Map.Entry<String, JSONNode>>() {
            private boolean first = true;

            @Override
            public void accept(Map.Entry<String, JSONNode> entry) {
                if(!first)
                    sb.append(",");
                else
                    first = false;
                if(prettyPrinting)
                    sb.append('\n').append(indent).append("  ");
                sb.append(JSONParser.quote(entry.getKey())).append(":").append(entry.getValue().serialize(prettyPrinting, indent+"  "));
            }
        });
        if(prettyPrinting)
            sb.append('\n').append(indent);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public JSONValue<?> asValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject asObject() {
        return this;
    }

    @Override
    public JSONArray asArray() {
        throw new UnsupportedOperationException();
    }

    public JSONNode getOrDefault(String key, JSONNode defaultValue){
        return keys.getOrDefault(key, defaultValue);
    }

    public JSONNode get(String key){
        return getOrDefault(key, null);
    }

    public void set(String key, JSONNode node){
        Objects.requireNonNull(node, "Node can not be null");
        keys.put(key, node);
    }

    public boolean hasKey(String key){
        return keys.containsKey(key);
    }

    public void set(String key, Object value){
        set(key, new JSONValue<>(value));
    }

    public void setNull(String key){
        set(key, new JSONValue<>());
    }

    public void remove(String key){
        keys.remove(key);
    }

    @Override
    public Map<String, Object> toFlatMap() {
        final Map<String, Object> object = new HashMap<>();
        for(Map.Entry<String, JSONNode> entry : keys.entrySet()){
            final Map<String, Object> objects = entry.getValue().toFlatMap();
            for(Map.Entry<String, Object> entry2 : objects.entrySet()){
                object.put(entry.getKey()+((entry.getValue() instanceof JSONValue) ? "" : ("."+entry2.getKey())), entry2.getValue());
            }
        }
        return object;
    }

    @Override
    public Iterator<Map.Entry<String, JSONNode>> iterator() {
        return keys.entrySet().iterator();
    }
}
