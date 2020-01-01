package com.braggart.juicer.test;


import com.braggart.juicer.annotation.Handler;
import com.braggart.juicer.annotation.Href;
import com.braggart.juicer.annotation.Parser;
import com.braggart.juicer.core.JuicerData;
import com.braggart.juicer.core.JuicerSource;

import javax.swing.text.Document;

@Handler("test")
public class TestHandler {

    @Href
    public JuicerSource a() {
        JuicerSource urls = new JuicerSource();
        urls.add("http://www.baidu.com");
        return urls;
    }

    @Parser
    public JuicerData b(Document document){
        JuicerData juicerData = new JuicerData();
        juicerData.put("TOP", "TOP");
        return juicerData;
    }

}
