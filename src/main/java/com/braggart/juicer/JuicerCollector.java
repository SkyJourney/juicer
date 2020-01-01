package com.braggart.juicer;

import com.braggart.juicer.core.Headers;
import com.braggart.juicer.core.JuicerData;
import com.braggart.juicer.core.JuicerHandler;
import com.braggart.juicer.util.JsoupDocumentHelper;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JuicerCollector {

    private JuicerHandlerFactory juicerHandlerFactory;

    public JuicerHandlerFactory getJuicerHandlerFactory() {
        return juicerHandlerFactory;
    }

    public JuicerCollector(JuicerHandlerFactory juicerHandlerFactory) {
        this.juicerHandlerFactory = juicerHandlerFactory;
    }

    public List<JuicerData> getDataFromHandler(String handlerBean){
        return getDataFromHandler(handlerBean, null, null);
    }

    public List<JuicerData> getDataFromHandler(String handlerBean, Headers headers){
        return getDataFromHandler(handlerBean, headers, null);
    }

    public List<JuicerData> getDataFromHandler(String handlerBean, JuicerData juicerData){
        Headers headers = JsoupDocumentHelper.getSampleHeaders();
        return getDataFromHandler(handlerBean, headers, juicerData);
    }

    public List<JuicerData> getDataFromHandler(String handlerBean, Headers headers,JuicerData juicerData){
        JuicerHandler juicerHandler = juicerHandlerFactory.getJuicerHandler(handlerBean);
        if(juicerData!=null && juicerData.get("_source")!=null){
            headers.put("referer", (String)juicerData.get("_source"));
        }
        Stream<URL> stream = juicerHandler.getUrls(juicerData).stream();
        return stream.map(URL::toExternalForm)
                .map(url -> {
                    try {
                        return JsoupDocumentHelper.getResponse(url, headers);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Cannot get response.");
                    }
                }).filter(Objects::nonNull)
                .map(response -> {
                    Document document;
                    try {
                        document = response.parse();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Cannot get document.");
                    }
                    assert document != null;
                    String html = document.html();
                    return juicerHandler.parse(response, document, html);
                }).collect(Collectors.toList());
    }
}
