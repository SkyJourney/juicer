package com.braggart.juicer;

import com.braggart.juicer.core.JuicerData;
import com.braggart.juicer.core.JuicerHandler;
import com.braggart.juicer.core.JuicerSource;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.List;

public class DefaultJuiceHandler implements JuicerHandler {
    @Override
    public JuicerSource getUrls(JuicerData juicerData) {
        return new JuicerSource();
    }

    @Override
    public JuicerData parse(Connection.Response response, Document document, String html) {
        return new JuicerData();
    }

    @Override
    public boolean hasParent() {
        return false;
    }

    @Override
    public String getParent() {
        return null;
    }
}
