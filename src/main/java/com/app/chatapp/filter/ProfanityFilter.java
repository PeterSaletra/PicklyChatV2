package com.app.chatapp.filter;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ProfanityFilter {
    private static final String apiKey = "[5092d3a8b925b1c6a90e6791fdf9f1bd]";
    private static final String url = "https://api.moderatecontent.com/text/?";
    private static char replace = '*';
    private static ProfanityFilter filter = new ProfanityFilter();
    private ProfanityFilter(){}

    public static ProfanityFilter getInstance(){
        return filter;
    }

    public static String filterMessage(String message) {
        JSONObject reqJSON = new JSONObject();
        try{
            reqJSON.put("msg", URLEncoder.encode(message, "UTF-8"));
            reqJSON.put("key",  URLEncoder.encode(apiKey, "UTF-8"));
            reqJSON.put("replace", replace);
            String urlParameters = getUrlParams(reqJSON);
            URI uri = new URI(url + urlParameters);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            JSONParser parser = new JSONParser();
            JSONObject resJSON = (JSONObject) parser.parse(response.toString());
            return resJSON.get("clean").toString();

        }catch (UnsupportedEncodingException err){
            err.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    private static String getUrlParams(JSONObject json){
        StringBuilder urlParameters = new StringBuilder();
        ArrayList<String> keys = new ArrayList<>();
        keys.addAll(json.keySet());
        Iterator<String> iter = keys.iterator();
        while(iter.hasNext()){
            String key = iter.next();
            urlParameters.append(key + "=" + json.get(key) + (iter.hasNext() ? "&" : ""));
        }
        return urlParameters.toString();
    }
}
