package com.juicer;

import com.juicer.core.Headers;
import com.juicer.core.JuicerData;
import com.juicer.core.JuicerHandler;
import com.juicer.util.JsoupDocumentHelper;
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
        Headers headers = JsoupDocumentHelper.getSampleHeaders();
        return getDataFromHandler(handlerBean, headers, null);
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
        Stream<URL> stream;
        if(juicerHandler.hasParent() && juicerData==null){
            stream = this.getDataFromHandler(juicerHandler.getParent(),headers).parallelStream()
                    .flatMap(preData -> {
                        if(preData.get("_source")!=null){
                            headers.put("referer", (String)preData.get("_source"));
                        }
                        return juicerHandler.getUrls(preData).stream();
                    });
        } else {
            stream = juicerHandler.getUrls(juicerData).parallelStream();
        }
        if(juicerData!=null && juicerData.get("_source")!=null){
            headers.put("referer", (String)juicerData.get("_source"));
        }
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
                    Objects.requireNonNull(document);
                    String html = document.html();
                    return juicerHandler.parse(response, document, html);
                }).collect(Collectors.toList());
    }
}
