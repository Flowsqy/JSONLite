package com.jsonlite.jsonlite.parser;

public class JSONToken <T> {

    public final static JSONTokenType<String> STRING = new JSONTokenType<>("String");
    public final static JSONTokenType<Integer> INT = new JSONTokenType<>("Integer");
    public final static JSONTokenType<Long> LONG = new JSONTokenType<>("Long");
    public final static JSONTokenType<Float> FLOAT = new JSONTokenType<>("Float");
    public final static JSONTokenType<Double> DOUBLE = new JSONTokenType<>("Double");
    public final static JSONTokenType<Boolean> BOOLEAN = new JSONTokenType<>("Boolean");
    public final static JSONTokenType<Void> NULL = new JSONTokenType<>("Null");
    public final static JSONTokenType<Void> BEGIN_OBJECT = new JSONTokenType<>("Begin object");
    public final static JSONTokenType<Void> END_OBJECT = new JSONTokenType<>("End object");
    public final static JSONTokenType<Void> BEGIN_ARRAY = new JSONTokenType<>("Begin array");
    public final static JSONTokenType<Void> END_ARRAY = new JSONTokenType<>("End array");
    public final static JSONTokenType<String> KEY = new JSONTokenType<>("Key");

    public final static class JSONTokenType <T> {

        private final String id;

        public JSONTokenType(String id){
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return "JSONTokenType{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }

    private final JSONTokenType<T> type;
    private final T value;

    public JSONToken(JSONTokenType<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    public JSONTokenType<T> getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "JSONToken{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }
}