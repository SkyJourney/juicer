package com.juicer;

import com.juicer.core.Headers;
import com.juicer.core.JuicerData;
import com.juicer.core.JuicerHandler;
import com.juicer.util.DocumentHelper;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author SkyJourney
 */
public class JuicerCollector {

    private JuicerHandlerFactory juicerHandlerFactory;

    public JuicerHandlerFactory getJuicerHandlerFactory() {
        return juicerHandlerFactory;
    }

    public JuicerCollector(JuicerHandlerFactory juicerHandlerFactory) {
        this.juicerHandlerFactory = juicerHandlerFactory;
    }

    public List<JuicerData> getDataFromHandler(String handlerBean){
        Headers headers = DocumentHelper.getSampleHeaders();
        return getDataFromHandler(null, handlerBean, headers, null);
    }

    public List<JuicerData> getDataFromHandler(JuicerData transferData, String handlerBean){
        Headers headers = DocumentHelper.getSampleHeaders();
        return getDataFromHandler(transferData, handlerBean, headers, null);
    }

    public List<JuicerData> getDataFromHandler(String handlerBean, Headers headers){
        return getDataFromHandler(null, handlerBean, headers, null);
    }

    public List<JuicerData> getDataFromHandler(JuicerData transferData, String handlerBean, Headers headers){
        return getDataFromHandler(transferData, handlerBean, headers, null);
    }

    public List<JuicerData> getDataFromHandler(String handlerBean, JuicerData juicerData){
        Headers headers = DocumentHelper.getSampleHeaders();
        return getDataFromHandler(null, handlerBean, headers, juicerData);
    }

    public List<JuicerData> getDataFromHandler(JuicerData transferData, String handlerBean, JuicerData juicerData){
        Headers headers = DocumentHelper.getSampleHeaders();
        return getDataFromHandler(transferData, handlerBean, headers, juicerData);
    }

    public List<JuicerData> getDataFromHandler(JuicerData transferData, String handlerBean, Headers headers,JuicerData juicerData){
        JuicerHandler juicerHandler = juicerHandlerFactory.getJuicerHandler(handlerBean);
        Stream<URL> stream;
        Function<JuicerData, Stream<URL>> getUrlsFromParent = preData -> {
            if(preData!=null && preData.get("_source")!=null){
                headers.put("referer", (String)preData.get("_source"));
            }
            return juicerHandler.getUrls(preData).stream();
        };

        if(transferData != null){
            if(juicerHandler.hasParent() && juicerData==null){
                stream = this.getDataFromHandler(transferData,juicerHandler.getParent(),headers).parallelStream()
                        .flatMap(getUrlsFromParent);
            } else {
                stream = juicerHandler.getUrls(juicerData).parallelStream();
            }
        } else {
            if(juicerHandler.hasParent() && juicerData==null){
                stream = this.getDataFromHandler(juicerHandler.getParent(),headers).parallelStream()
                        .flatMap(getUrlsFromParent);
            } else {
                stream = juicerHandler.getUrls(juicerData).parallelStream();
            }
        }
        if(juicerData!=null && juicerData.get("_source")!=null){
            headers.put("referer", (String)juicerData.get("_source"));
        }
        return stream.map(URL::toExternalForm)
                .map(url -> {
                    try {
                        return DocumentHelper.getResponse(url, headers);
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
                    return juicerHandler.parse(transferData, response, document, html);
                }).collect(Collectors.toList());
    }

}
