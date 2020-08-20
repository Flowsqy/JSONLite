package com.jsonlite.jsonlite.node;

import java.util.*;
import java.util.function.Consumer;

public class JSONArray implements JSONNode, Iterable<JSONNode> {

    private List<JSONNode> elements;

    public JSONArray() {
        this(new ArrayList<>());
    }

    public JSONArray(List<JSONNode> elements) {
        this.elements = elements;
    }

    public List<JSONNode> getElements() {
        return elements;
    }

    public void setElements(List<JSONNode> elements) {
        this.elements = elements;
    }

    @Override
    public String toString() {
        return "JSONArray{" +
                "elements=" + elements +
                '}';
    }

    @Override
    public String serialize(boolean prettyPrinting, String indent) {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        elements.forEach(new Consumer<JSONNode>() {
            private boolean first = true;

            @Override
            public void accept(JSONNode node) {
                if(!first)
                    sb.append(",");
                else
                    first = false;
                if(prettyPrinting)
                    sb.append('\n').append(indent).append("  ");
                sb.append(node.serialize(prettyPrinting, indent+"  "));
            }
        });
        if(prettyPrinting)
            sb.append('\n').append(indent);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public JSONValue<?> asValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject asObject() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONArray asArray() {
        return this;
    }

    @Override
    public Map<String, Object> toFlatMap() {
        final Map<String, Object> array = new HashMap<>();
        for(int index = 0; index < elements.size(); index++){
            final JSONNode element = elements.get(index);
            final Map<String, Object> object = element.toFlatMap();
            for(Map.Entry<String, Object> entry : object.entrySet()){
                array.put(index+((element instanceof JSONValue) ? "" : ("."+entry.getKey())), entry.getValue());
            }
        }
        return array;
    }

    public void add(JSONNode node){
        elements.add(node);
    }

    public void remove(JSONNode node){
        elements.remove(node);
    }

    public JSONNode get(int index){
        return elements.get(index);
    }

    public int size(){
        return elements.size();
    }

    @Override
    public Iterator<JSONNode> iterator() {
        return elements.iterator();
    }
}
