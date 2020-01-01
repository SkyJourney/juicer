package com.braggart.juicer.test;


import com.braggart.juicer.annotation.Handler;
import com.braggart.juicer.annotation.Href;
import com.braggart.juicer.annotation.Parser;
import com.braggart.juicer.core.JuicerData;

import javax.swing.text.Document;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Handler("test")
public class TestHandler {

    @Href
    public List<URL> a() throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        urls.add(new URL("http://www.baidu.com"));
        return urls;
    }

    @Parser
    public JuicerData b(Document document){
        JuicerData juicerData = new JuicerData();
        juicerData.put("TOP", "TOP");
        return juicerData;
    }

}
