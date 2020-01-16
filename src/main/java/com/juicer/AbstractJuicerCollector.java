package com.juicer;

import com.juicer.JuicerHandlerFactory;
import com.juicer.core.*;
import com.juicer.util.DocumentHelper;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author SkyJourney
 */
public abstract class AbstractJuicerCollector {

    private JuicerHandlerFactory juicerHandlerFactory;

    private RuntimeStorage runtimeStorage = RuntimeStorage.getInstance();

    public RuntimeStorage getRuntimeStorage() {
        return runtimeStorage;
    }

    private ForkJoinPool forkJoinPool;

    public JuicerHandlerFactory getJuicerHandlerFactory() {
        return juicerHandlerFactory;
    }

    public ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }

    public void setJuicerHandlerFactory(JuicerHandlerFactory juicerHandlerFactory) {
        this.juicerHandlerFactory = juicerHandlerFactory;
    }

    public void setForkJoinPool(ForkJoinPool forkJoinPool) {
        this.forkJoinPool = forkJoinPool;
    }

    public AbstractJuicerCollector() {
    }

    public AbstractJuicerCollector(JuicerHandlerFactory juicerHandlerFactory, ForkJoinPool forkJoinPool) {
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
        if(runtimeStorage.isResultExist(handlerBean)){
            return runtimeStorage.getJuicerResult(handlerBean);
        }
        JuicerHandler juicerHandler = juicerHandlerFactory.getJuicerHandler(handlerBean);
        Stream<Map.Entry<JuicerData,URL>> stream;
        Function<JuicerData, Stream<Map.Entry<JuicerData,URL>>> getUrlsFromParent = preData -> {
            if(preData!=null && preData.get("_source")!=null){
                headers.put("referer", (String)preData.get("_source"));
            }
            runtimeStorage.addHandlerTaskQueue(handlerBean);
            return juicerHandler.getUrls(preData)
                    .stream()
                    .map(url -> {
                        runtimeStorage.putJuicerTask(handlerBean
                                , url
                                , new JuicerTask(url
                                        , runtimeStorage.getJuicerChain(handlerBean)
                                        , false
                                )
                        );
                        return new AbstractMap.SimpleEntry<>(preData, url);
                    });
        };
        if(juicerHandler.hasParent()
                && juicerData==null){
            runtimeStorage.putJuicerChain(juicerHandler.getParent(), handlerBean);
            stream = this.getData(juicerHandler.getParent(),headers)
                    .parallelStream()
                    .flatMap(getUrlsFromParent);
        } else {
            runtimeStorage.addHandlerTaskQueue(handlerBean);
            stream = juicerHandler.getUrls(juicerData)
                    .parallelStream()
                    .map(url -> {
                        runtimeStorage.putJuicerTask(handlerBean
                                , url
                                , new JuicerTask(url
                                        , runtimeStorage.getJuicerChain(handlerBean)
                                        , false
                                )
                        );
                        System.out.println(runtimeStorage.getJuicerTaskQueue());
                        return new AbstractMap.SimpleEntry<>(juicerData, url);
                    });
        }
        if(juicerData!=null && juicerData.get("_source")!=null){
            headers.put("referer", (String)juicerData.get("_source"));
        }
        runtimeStorage.addJuicerResult(handlerBean);
        stream.map(entry -> {
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
            runtimeStorage.getJuicerTask(handlerBean, entry.getValue()).setFinished(true);
            return resultData;
        }).forEach(juicerData1 -> runtimeStorage.putJuicerData(handlerBean, juicerData1));
        runtimeStorage.removeHandlerTaskQueue(handlerBean);
        return runtimeStorage.getJuicerResult(handlerBean);
    }
}
