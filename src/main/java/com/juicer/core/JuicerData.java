package com.juicer.core;

import java.util.HashMap;
import java.util.Map;

public class JuicerData extends HashMap<String,Object> {

    public static JuicerData getInstance(){
        return new JuicerData();
    }

    public JuicerData addData(String key,String value){
        this.put(key, value);
        return this;
    }

    public JuicerData addDatas(Map<String, Object> datas){
        this.putAll(datas);
        return this;
    }

}
