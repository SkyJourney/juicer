package com.juicer.core;

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

    private RuntimeStorage runtimeStorage = RuntimeStorage.getInstance();

    public RuntimeStorage getRuntimeStorage() {
        return runtimeStorage;
    }

    private static final Properties DEFAULT_INTERRUPT_SETTINGS;

    static {
        DEFAULT_INTERRUPT_SETTINGS = new Properties();
        DEFAULT_INTERRUPT_SETTINGS.setProperty("juicer.interrupt.save.allow", "false");
        DEFAULT_INTERRUPT_SETTINGS.setProperty("juicer.interrupt.save.path", ".");
    }

    private Properties juicerInterruptSettings = new Properties(DEFAULT_INTERRUPT_SETTINGS);

    private ForkJoinPool forkJoinPool;

    public JuicerHandlerFactory getJuicerHandlerFactory() {
        return juicerHandlerFactory;
    }

    public ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }

    public JuicerCollector(JuicerHandlerFactory juicerHandlerFactory) {
        this(juicerHandlerFactory,ForkJoinPool.commonPool());
    }

    public JuicerCollector(JuicerHandlerFactory juicerHandlerFactory, ForkJoinPool forkJoinPool) {
        this.juicerHandlerFactory = juicerHandlerFactory;
        this.forkJoinPool = forkJoinPool;
        //init();
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
            runtimeStorage.getJuicerTaskQueue().put(handlerBean, new ConcurrentHashMap<>(16));
            return juicerHandler.getUrls(preData)
                    .stream()
                    .map(url -> {
                        runtimeStorage.getJuicerTaskQueue().get(handlerBean)
                                .put(url, new JuicerTask(url
                                        , runtimeStorage.getJuicerChain().get(handlerBean)
                                        , false));
                        return new AbstractMap.SimpleEntry<>(preData, url);
                    });
        };
        if(juicerHandler.hasParent()
                && juicerData==null){
            runtimeStorage.getJuicerChain().put(juicerHandler.getParent(), handlerBean);
            stream = this.getData(juicerHandler.getParent(),headers)
                    .parallelStream()
                    .flatMap(getUrlsFromParent);
        } else {
            runtimeStorage.getJuicerTaskQueue().put(handlerBean, new ConcurrentHashMap<>(16));
            stream = juicerHandler.getUrls(juicerData)
                    .parallelStream()
                    .map(url -> {
                        runtimeStorage.getJuicerTaskQueue().get(handlerBean)
                                .put(url, new JuicerTask(url
                                        , runtimeStorage.getJuicerChain().get(handlerBean)
                                        , false));
                        return new AbstractMap.SimpleEntry<>(juicerData, url);
                    });
        }
        if(juicerData!=null && juicerData.get("_source")!=null){
            headers.put("referer", (String)juicerData.get("_source"));
        }
        runtimeStorage.getJuicerResultStorage().put(handlerBean, stream
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
                    runtimeStorage.getJuicerTaskQueue().get(handlerBean).get(entry.getValue()).setFinished(true);
                    return resultData;
                }).collect(Collectors.toList())
        );
        runtimeStorage.getJuicerTaskQueue().remove(handlerBean);
        return runtimeStorage.getJuicerResultStorage().get(handlerBean);
    }
}
