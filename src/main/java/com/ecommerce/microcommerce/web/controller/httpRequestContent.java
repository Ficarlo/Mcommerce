package com.ecommerce.microcommerce.web.controller;

import javafx.util.Pair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class httpRequestContent {

    //TODO: Voir si on récupère les paramètres d'entrées. Actuellement uniquement les paramètres de retour sont utilisés.
    private String urlSend;
    java.util.List<Pair<String,String>> parametersSend;
    List<Pair<String,String>> headersSend;
    String bodySend;

    private int status;
    private String body;
    private List<Pair<String,String>> headers;

    public httpRequestContent(int status, String body, List<Pair<String,String>> headers) {
        this.status = status;
        this.body = body;
        this.headers = headers;
    }

    public httpRequestContent() {
    }

    public httpRequestContent(boolean initHeader) {
        if(initHeader){
            headers = new ArrayList<>();
        }

    }

    public httpRequestContent(String urlSend, List<Pair<String, String>> headersSend, List<Pair<String, String>> parametersSend, String bodySend, boolean initHeader) {

        this.urlSend = urlSend;
        this.parametersSend = parametersSend;
        this.headersSend = headersSend;
        this.bodySend = bodySend;

        if(initHeader){
            headers = new ArrayList<>();
        }
    }

    public String getUrlSend() {
        return urlSend;
    }

    public void setUrlSend(String urlSend) {
        this.urlSend = urlSend;
    }

    public List<Pair<String, String>> getParametersSend() {
        return parametersSend;
    }

    public void setParametersSend(List<Pair<String, String>> parametersSend) {
        this.parametersSend = parametersSend;
    }

    public List<Pair<String, String>> getHeadersSend() {
        return headersSend;
    }

    public void setHeadersSend(List<Pair<String, String>> headersSend) {
        this.headersSend = headersSend;
    }

    public String getBodySend() {
        return bodySend;
    }

    public void setBodySend(String bodySend) {
        this.bodySend = bodySend;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<Pair<String,String>> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Pair<String,String>> headers) {
        this.headers = headers;
    }

    public void setHeaders( String key, String value) {
        this.headers.add(new Pair(key,value));
    }

    public void build() throws IOException {

        URL ur = new URL(urlSend);
        HttpURLConnection con = (HttpURLConnection) ur.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(30000);
        con.setReadTimeout(30000);

        //Ajout des headers
        if(!headersSend.isEmpty()){
            for (Pair<String, String> header : headersSend) {
                con.setRequestProperty(header.getKey(),header.getValue());
                System.out.println(header.getKey()+"/"+header.getValue());
            }
        }

        //Ajout des paramètres
        if(!parametersSend.isEmpty()){
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(getParamsString(parametersSend));
            out.flush();
            out.close();
        }

        int statusRet = con.getResponseCode();
        this.setStatus(statusRet);
        System.out.println("Url: "+urlSend+" Status:"+statusRet);


        //Récupération des headers
        for (Map.Entry<String, List<String>> entries : con.getHeaderFields().entrySet()) {
            String values = "";
            for (String value : entries.getValue()) {
                values += value + ",";
            }
            //System.out.println("Response"+ entries.getKey()+", "+values);
            this.setHeaders(entries.getKey(),values);

        }

        //récupération du body  https://stackoverflow.com/questions/25011927/how-to-get-response-body-using-httpurlconnection-when-code-other-than-2xx-is-re
        BufferedReader br = (status <400)? new BufferedReader(new InputStreamReader((con.getInputStream()))) :new BufferedReader(new InputStreamReader((con.getErrorStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }
        br.close();
        con.disconnect();

        this.setBody(sb.toString());
    }

    private static String getParamsString(List<Pair<String, String>> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();


        for (Pair<String, String> param : params) {
            result.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(param.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }
}


