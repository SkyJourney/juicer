package com.braggart.juicer;

import com.braggart.juicer.core.JuicerData;
import com.braggart.juicer.core.JuicerHandler;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.List;

public class DefaultJuiceHandler implements JuicerHandler {
    @Override
    public List<URL> getUrls(JuicerData juicerData) {
        return null;
    }

    @Override
    public JuicerData parse(Connection.Response response, Document document, String html) {
        return null;
    }
}
