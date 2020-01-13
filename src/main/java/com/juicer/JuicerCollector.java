package com.juicer;

import com.juicer.core.Headers;
import com.juicer.core.JuicerData;
import com.juicer.core.JuicerHandler;
import com.juicer.core.JuicerTask;
import com.juicer.util.DocumentHelper;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author SkyJourney
 */
public class JuicerCollector {

    private JuicerHandlerFactory juicerHandlerFactory;

    private Map<String ,Map<URL, JuicerTask>> juicerTaskQueue = new ConcurrentHashMap<>();

    private Map<String, List<JuicerData>> juicerResultStorage = new ConcurrentHashMap<>();

    private Map<String, String> juicerChain = new ConcurrentHashMap<>();

    private ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    public JuicerHandlerFactory getJuicerHandlerFactory() {
        return juicerHandlerFactory;
    }

    public ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }

    public JuicerCollector(JuicerHandlerFactory juicerHandlerFactory) {
        this.juicerHandlerFactory = juicerHandlerFactory;
    }

    public JuicerCollector(JuicerHandlerFactory juicerHandlerFactory, ForkJoinPool forkJoinPool) {
        this.juicerHandlerFactory = juicerHandlerFactory;
        this.forkJoinPool = forkJoinPool;
    }

    public List<JuicerData> getDataFromHandler(String handlerBean) throws ExecutionException, InterruptedException {
        return forkJoinPool.submit(()->getData(handlerBean)).get();
    }

    private List<JuicerData> getData(String handlerBean){
        Headers headers = DocumentHelper.getSampleHeaders();
        return getData(handlerBean, headers, null);
    }

    public List<JuicerData> getDataFromHandler(String handlerBean, Headers headers) throws ExecutionException, InterruptedException {
        return forkJoinPool.submit(()->getData(handlerBean,headers)).get();
    }

    private List<JuicerData> getData(String handlerBean, Headers headers){
        return getData(handlerBean, headers, null);
    }

    public List<JuicerData> getDataFromHandler(String handlerBean,JuicerData juicerData) throws ExecutionException, InterruptedException {
        return forkJoinPool.submit(()->getData(handlerBean,juicerData)).get();
    }

    private List<JuicerData> getData(String handlerBean, JuicerData juicerData){
        Headers headers = DocumentHelper.getSampleHeaders();
        return getData(handlerBean, headers, juicerData);
    }

    public List<JuicerData> getDataFromHandler(String handlerBean, Headers headers, JuicerData juicerData) throws ExecutionException, InterruptedException {
        return forkJoinPool.submit(()->getData(handlerBean,headers,juicerData)).get();
    }

    private List<JuicerData> getData(String handlerBean, Headers headers, JuicerData juicerData){
        JuicerHandler juicerHandler = juicerHandlerFactory.getJuicerHandler(handlerBean);
        Stream<Map.Entry<JuicerData,URL>> stream;
        Function<JuicerData, Stream<Map.Entry<JuicerData,URL>>> getUrlsFromParent = preData -> {
            if(preData!=null && preData.get("_source")!=null){
                headers.put("referer", (String)preData.get("_source"));
            }
            juicerTaskQueue.put(handlerBean, new ConcurrentHashMap<>(16));
            return juicerHandler.getUrls(preData)
                    .stream()
                    .map(url -> {
                        juicerTaskQueue.get(handlerBean)
                                .put(url, new JuicerTask(url
                                        , juicerChain.get(handlerBean)
                                        , false));
                        return new AbstractMap.SimpleEntry<>(preData, url);
                    });
        };
        if(juicerHandler.hasParent()
                && juicerData==null){
            juicerChain.put(juicerHandler.getParent(), handlerBean);
            stream = this.getData(juicerHandler.getParent(),headers)
                    .parallelStream()
                    .flatMap(getUrlsFromParent);
        } else {
            juicerTaskQueue.put(handlerBean, new ConcurrentHashMap<>(16));
            stream = juicerHandler.getUrls(juicerData)
                    .parallelStream()
                    .map(url -> {
                        juicerTaskQueue.get(handlerBean)
                                .put(url, new JuicerTask(url
                                        , juicerChain.get(handlerBean)
                                        , false));
                        return new AbstractMap.SimpleEntry<>(juicerData, url);
                    });
        }
        if(juicerData!=null && juicerData.get("_source")!=null){
            headers.put("referer", (String)juicerData.get("_source"));
        }
        juicerResultStorage.put(handlerBean, stream
                .map(entry -> {
                    Connection.Response response;
                    try {
                        response = DocumentHelper.getResponse(entry.getValue().toExternalForm(), headers);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Cannot get response.");
                    }
                    Objects.requireNonNull(response);
                    Document document;
                    try {
                        document = response.parse();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Cannot get document.");
                    }
                    Objects.requireNonNull(document);
                    String html = document.html();
                    JuicerData resultData;
                    if(entry.getKey()!=null){
                        resultData = juicerHandler.parse(JuicerData.getInstance().addData(entry.getKey()), response, document, html);
                    } else {
                        resultData = juicerHandler.parse(JuicerData.getInstance(), response, document, html);
                    }
                    juicerTaskQueue.get(handlerBean).get(entry.getValue()).setFinished(true);
                    return resultData;
                }).collect(Collectors.toList())
        );
        juicerTaskQueue.remove(handlerBean);
        return juicerResultStorage.get(handlerBean);
    }
}
