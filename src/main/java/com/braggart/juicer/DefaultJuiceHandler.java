package com.braggart.juicer;

import com.braggart.juicer.core.JuicerData;
import com.braggart.juicer.core.JuicerHandler;
import com.braggart.juicer.core.JuicerSource;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

public class DefaultJuiceHandler implements JuicerHandler {
    @Override
    public JuicerSource getUrls(JuicerData juicerData) {
        return new JuicerSource();
    }

    @Override
    public JuicerData parse(Connection.Response response, Document document, String html) {
        return new JuicerData();
    }
}
