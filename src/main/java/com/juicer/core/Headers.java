package com.juicer.core;

import java.util.HashMap;
import java.util.Map;

public class Headers extends HashMap<String,String> {

    public static Headers getInstance(){
        return new Headers();
    }

    public Headers defaultHeaders(){
        this.put("Accept", "*/*");
        this.put("Connection", "Keep-Alive");
        this.put("Accept-Language", "en-US,en;q=0.8");
        this.put("Cache-Control", "max-age=0");
        this.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36");
        return this;
    }

    public Headers header(String key,String value){
        this.put(key, value);
        return this;
    }

    public Headers headers(Map<String, String> headers){
        this.putAll(headers);
        return this;
    }

}
