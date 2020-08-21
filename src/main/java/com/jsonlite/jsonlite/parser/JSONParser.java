package com.jsonlite.jsonlite.parser;

import com.jsonlite.jsonlite.exception.MalformedJSONException;
import com.jsonlite.jsonlite.node.JSONValue;
import com.jsonlite.jsonlite.node.JSONArray;
import com.jsonlite.jsonlite.node.JSONNode;
import com.jsonlite.jsonlite.node.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class JSONParser {

    public static List<JSONToken<?>> decode(Reader reader) {

        try (BufferedReader bis = new BufferedReader(reader)) {
            final List<JSONToken<?>> tokens = new ArrayList<>();


            boolean needComma = false;

            boolean inString = false;
            StringBuilder currentString = new StringBuilder();
            boolean escape = false;

            boolean inNumber = false;
            StringBuilder currentNumber = new StringBuilder();
            boolean hasPoint = false;
            boolean hasExponent = false;
            boolean hasBeenRelative = false;
            boolean hasBeenExponent = false;

            boolean inBoolean = false;
            StringBuilder currentBoolean = new StringBuilder();
            for (int part = bis.read(); part != -1; part = bis.read()) {

                final char c = (char) part;

                if(!inString){
                    switch (c){
                        case  ' ':
                        case '\b':
                        case '\t':
                        case '\n':
                        case '\f':
                        case '\r':
                            continue;
                        default:
                            break;
                    }
                }

                if(needComma){
                    switch (c){
                        case ' ':
                        case '\n':
                            continue;
                        case '}':
                        case ']':
                            needComma = false;
                            break;
                        case ',':
                            needComma = false;
                            continue;
                        case ':':
                            final int lastIndex = tokens.size()-1;
                            if(tokens.isEmpty() || tokens.get(lastIndex).getType() != JSONToken.STRING){
                                throw new MalformedJSONException("There is colon without string before");
                            }
                            final String key = (String) tokens.remove(lastIndex).getValue();
                            tokens.add(new JSONToken<>(JSONToken.KEY, key));
                            needComma = false;
                            continue;
                        default:
                            throw new MalformedJSONException("Impossible character after a value or a key");
                    }
                }
                /*
                 * Handle String
                 */

                if(escape){
                    switch (c){
                        case 'b':
                            currentString.append("\b");
                            break;
                        case 't':
                            currentString.append("\t");
                            break;
                        case 'n':
                            currentString.append("\n");
                            break;
                        case 'f':
                            currentString.append("\f");
                            break;
                        case 'r':
                            currentString.append("\r");
                            break;
                        case '"':
                            currentString.append("\"");
                            break;
                        case '\\':
                            currentString.append("\\");
                            break;
                        default:
                            throw new MalformedJSONException("Impossible character after a '\\' in a string");
                    }
                    escape = false;
                    continue;
                }
                if(c == '\\'){
                    if(inString) {
                        escape = true;
                        continue;
                    }
                    else {
                        throw new MalformedJSONException("Awkward character : \\");
                    }
                }

                boolean inStringHere = false;
                if(c == '"'){
                    if(inString){
                        inString = false;
                        tokens.add(new JSONToken<>(JSONToken.STRING, currentString.toString()));
                        currentString = new StringBuilder();
                        needComma = true;
                        continue;
                    }
                    else{
                        inString = true;
                        inStringHere = true;
                    }
                }

                if(inString && !inStringHere){
                    currentString.append(c);
                }

                if(inStringHere || inString){
                    continue;
                }

                /*
                 * Dead code, became useless
                 *
                if(c == '\n'){
                    continue;
                }
                 */

                /*
                 * Handle numbers
                 */
                if(c == '-' || c == '+'){
                    if(inNumber) {
                        if(hasBeenExponent){
                            currentNumber.append(c);
                            hasBeenExponent = false;
                            continue;
                        }
                        throw new MalformedJSONException("There a negative sign in a number");
                    }
                    currentNumber.append(c);
                    inNumber = true;
                    hasBeenRelative = true;
                    continue;
                }
                else if(Character.isDigit(c)){
                    hasBeenRelative = false;
                    hasBeenExponent = false;
                    currentNumber.append(c);
                    if(!inNumber)
                        inNumber = true;
                    continue;
                }
                else if(inNumber){
                    if(c == '.'){
                        if(hasPoint){
                            throw new MalformedJSONException("There are two points in the number representation");
                        }
                        else if(hasExponent){
                            throw new MalformedJSONException("There is an exponent before a point");
                        }
                        else if(hasBeenRelative){
                            throw new MalformedJSONException("There is a point after the relative sign");
                        }
                        else{
                            hasPoint = true;
                            currentNumber.append(".");
                            hasBeenExponent = false;
                            hasBeenRelative = false;
                            continue;
                        }
                    }
                    else if(c == 'e' || c == 'E'){
                        if(hasExponent){
                            throw new MalformedJSONException("There are two exponent in the number representation");
                        }
                        else if(hasBeenRelative){
                            throw new MalformedJSONException("There is an exponent after the negative sign");
                        }
                        else{
                            hasExponent = true;
                            hasBeenExponent = true;
                            hasBeenRelative = false;
                            currentNumber.append(c);
                            continue;
                        }
                    }
                    else{
                        final char endChar = currentNumber.charAt(currentNumber.length() - 1);
                        if(endChar == '.') {
                            throw new MalformedJSONException("Number ends with a dot");
                        }
                        else if(endChar == '+' || endChar == '-'){
                            throw new MalformedJSONException("Number ends with a relative sign");
                        }
                        else if(endChar == 'e' || endChar == 'E'){
                            throw new MalformedJSONException("Number end with a exponent");
                        }
                        inNumber = false;
                        hasPoint = false;
                        hasBeenRelative = false;
                        hasExponent = false;
                        hasBeenExponent = false;

                        /*
                         * Parse number
                         */

                        final String argNumber = currentNumber.toString();
                        if(argNumber.contains(".")){
                            try{
                                double result = Double.parseDouble(argNumber);
                                if(result <= Float.MAX_VALUE)
                                    tokens.add(new JSONToken<>(JSONToken.FLOAT, (float)result));
                                else
                                    tokens.add(new JSONToken<>(JSONToken.DOUBLE, result));
                            }catch (NumberFormatException e){
                                throw new MalformedJSONException("Wrong number : " + argNumber);
                            }
                        }
                        else{
                            try{
                                long result = Long.parseLong(argNumber);
                                if(result <= Integer.MAX_VALUE)
                                    tokens.add(new JSONToken<>(JSONToken.INT, (int)result));
                                else
                                    tokens.add(new JSONToken<>(JSONToken.LONG, result));
                            }catch (NumberFormatException e){
                                throw new MalformedJSONException("Wrong number : " + argNumber);
                            }
                        }
                        if (
                                !(
                                        (c == ',') ||
                                        (c == '}') ||
                                        (c == ']')
                                )
                        ){
                            throw new MalformedJSONException("There is an awkward character after a number: "+c);
                        }
                        currentNumber = new StringBuilder();
                        if(c == ',')
                            continue;
                    }
                }

                /*
                 * Handle Boolean and null
                 */
                if(Character.isAlphabetic(c)){
                    if(!inBoolean)
                        inBoolean = true;
                    currentBoolean.append(c);
                    if(currentBoolean.length() == 4){
                        if(currentBoolean.toString().equals("true")){
                            tokens.add(new JSONToken<>(JSONToken.BOOLEAN, true));
                            currentBoolean = new StringBuilder();
                            inBoolean = false;
                            needComma = true;
                        }
                        else if(currentBoolean.toString().equals("null")){
                            tokens.add(new JSONToken<>(JSONToken.NULL, null));
                            currentBoolean = new StringBuilder();
                            inBoolean = false;
                            needComma = true;
                        }

                    }
                    else if(currentBoolean.length() == 5){
                        if(currentBoolean.toString().equals("false")){
                            currentBoolean = new StringBuilder();
                            inBoolean = false;
                            tokens.add(new JSONToken<>(JSONToken.BOOLEAN, false));
                            needComma = true;
                        }
                        else{
                            throw new MalformedJSONException("Awkward value : "+ currentBoolean);
                        }
                    }
                    continue;
                }

                /*
                 * Handle nodes
                 */
                if(c == '{'){
                    tokens.add(new JSONToken<>(JSONToken.BEGIN_OBJECT, null));
                }

                else if(c == '}'){
                    tokens.add(new JSONToken<>(JSONToken.END_OBJECT, null));
                    needComma = true;
                }

                else if(c == '['){
                    tokens.add(new JSONToken<>(JSONToken.BEGIN_ARRAY, null));
                }

                else if(c == ']'){
                    tokens.add(new JSONToken<>(JSONToken.END_ARRAY, null));
                    needComma = true;
                }

                else{
                    throw new MalformedJSONException("Awkward character : " + c);
                }

            }
            return tokens;
        } catch (IOException ignored) {}
        return new ArrayList<>();
    }

    private static JSONNode createNode(Queue<JSONToken<?>> tokens) {

        JSONNode node = null;

        while(!tokens.isEmpty()){
            final JSONToken<?> token = tokens.peek();
            final JSONToken.JSONTokenType<?> tokenType = token.getType();

            if(tokenType == JSONToken.KEY){
                tokens.poll();
                if(!(node instanceof JSONObject)){
                    throw new MalformedJSONException("There is key which was not in an object");
                }
                else {
                    ((JSONObject) node).set((String) token.getValue(), createNode(tokens));
                    continue;
                }
            }
            else if(tokenType == JSONToken.BEGIN_OBJECT){
                if(node == null)
                    node = new JSONObject();
                else if(node instanceof JSONObject){
                    throw new MalformedJSONException("There is an object without a key");
                }
                else{
                    ((JSONArray)node).add(createNode(tokens));
                    continue;
                }
            }
            else if(tokenType == JSONToken.BEGIN_ARRAY){
                if(node == null)
                    node = new JSONArray();
                else if(node instanceof JSONObject){
                    throw new MalformedJSONException("There is an array in an object without key");
                }
                else{
                    ((JSONArray)node).add(createNode(tokens));
                    continue;
                }
            }
            else if(
                    token.getType() == JSONToken.STRING ||
                            token.getType() == JSONToken.INT ||
                            token.getType() == JSONToken.LONG ||
                            token.getType() == JSONToken.FLOAT ||
                            token.getType() == JSONToken.DOUBLE ||
                            token.getType() == JSONToken.BOOLEAN ||
                            token.getType() == JSONToken.NULL
            ){
                if(node == null) {
                    tokens.poll();
                    return new JSONValue<>(token.getValue());
                }
                else if(node instanceof JSONObject){
                    throw new MalformedJSONException("Value inside an object without key");
                }
                else {
                    ((JSONArray)node).add(new JSONValue<>(token.getValue()));
                }
            }
            else if(tokenType == JSONToken.END_OBJECT){
                tokens.poll();
                if(node instanceof JSONObject)
                    return node;
                else{
                    throw new MalformedJSONException("Try to end an object which was not started");
                }
            }
            else if(tokenType == JSONToken.END_ARRAY){
                tokens.poll();
                if(node instanceof JSONArray)
                    return node;
                else{
                    throw new MalformedJSONException("Try to end an array which was not started");
                }
            }

            tokens.poll();
        }

        return node;

    }

    public static JSONNode fromJSON(String json){
        return fromJSON(json.toCharArray());
    }

    public static JSONNode fromJSON(InputStream inputStream){
        return fromJSON(new InputStreamReader(inputStream));
    }

    public static JSONNode fromJSON(char[] characters){
        return fromJSON(new CharArrayReader(characters));
    }

    public static JSONNode fromJSON(Reader reader){
        return createNode(new LinkedList<>(decode(reader)));
    }

    // jdk.nashorn.internal.parser.JSONParser#quote
    public static String quote(String value) {
        final StringBuilder product = new StringBuilder();
        product.append("\"");

        for(char ch : value.toCharArray()) {
            switch(ch) {
                case '\b':
                    product.append("\\b");
                    break;
                case '\t':
                    product.append("\\t");
                    break;
                case '\n':
                    product.append("\\n");
                    break;
                case '\f':
                    product.append("\\f");
                    break;
                case '\r':
                    product.append("\\r");
                    break;
                case '"':
                    product.append("\\\"");
                    break;
                case '\\':
                    product.append("\\\\");
                    break;
                default:
                    if (ch < ' ') {

                        final StringBuilder sb = new StringBuilder();
                        sb.append("\\u");
                        String hex = Integer.toHexString(ch);

                        for(int i = hex.length(); i < 4; ++i) {
                            sb.append('0');
                        }
                        sb.append(hex);

                        product.append(sb.toString());
                    } else {
                        product.append(ch);
                    }
            }
        }

        product.append("\"");
        return product.toString();
    }

}
