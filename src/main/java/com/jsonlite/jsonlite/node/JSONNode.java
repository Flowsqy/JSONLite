package com.jsonlite.jsonlite.node;

import java.util.Map;

public interface JSONNode {

    String serialize(boolean prettyPrinting, String indent);

    default String serialize(boolean prettyPrinting){
        return serialize(prettyPrinting, "");
    }

    default String serialize(){
        return serialize(true);
    }

    JSONValue<?> asValue();

    JSONObject asObject();

    JSONArray asArray();

    Map<String, Object> toFlatMap();

}
