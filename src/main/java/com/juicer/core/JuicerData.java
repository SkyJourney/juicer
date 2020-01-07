package com.juicer.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author SkyJourney
 * @since 1.0
 */
public class JuicerData extends HashMap<String,Object> {

    public static JuicerData getInstance(){
        return new JuicerData();
    }

    public JuicerData addData(String key,Object value){
        this.put(key, value);
        return this;
    }

    public JuicerData addData(Map<String, Object> data){
        this.putAll(data);
        return this;
    }

    public String getString(String key){
        return this.get(key).toString();
    }

}
