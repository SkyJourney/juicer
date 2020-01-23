package com.juicer;

import com.alibaba.fastjson.JSON;
import com.juicer.annotation.EnableDataPersistence;
import com.juicer.annotation.EnableInterruptResume;
import com.juicer.annotation.HandlerScan;
import com.juicer.annotation.JuicerConfiguration;
import com.juicer.core.*;
import com.juicer.util.DocumentHelper;
import com.juicer.util.PropertiesUtils;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

/**
 * @author SkyJourney
 */
public class JuicerActuator extends AbstractJuicerCollector implements InterruptResume {

    private static final Properties DEFAULT_INTERRUPT_SETTINGS;
    private static final String INTERRUPT_SAVE_ALLOW = "juicer.data.interrupt-save";
    private static final String DATA_SAVE_PATH = "juicer.data.save-path";
    private static final String DATA_PERSISTENCE = "juicer.data.persistence";
    private static final String TRUE = "true";
    private static final String FALSE = "false";


    static {
        DEFAULT_INTERRUPT_SETTINGS = new Properties();
        DEFAULT_INTERRUPT_SETTINGS.setProperty(INTERRUPT_SAVE_ALLOW, FALSE);
        DEFAULT_INTERRUPT_SETTINGS.setProperty(DATA_SAVE_PATH, "./JuicerSaveData.jdt");
        DEFAULT_INTERRUPT_SETTINGS.setProperty(DATA_PERSISTENCE, FALSE);
    }

    private Properties juicerInterruptSettings;

    public Properties getJuicerInterruptSettings() {
        return juicerInterruptSettings;
    }

    public JuicerActuator(JuicerHandlerFactory juicerHandlerFactory) {
        this(juicerHandlerFactory,ForkJoinPool.commonPool(),"juicer.properties");
    }

    public JuicerActuator(JuicerHandlerFactory juicerHandlerFactory, ExecutorService threadPool) {
        this(juicerHandlerFactory,threadPool,"juicer.properties");
    }

    public JuicerActuator(JuicerHandlerFactory juicerHandlerFactory, String propertiesPath) {
        this(juicerHandlerFactory,ForkJoinPool.commonPool(),propertiesPath);
    }

    public JuicerActuator(JuicerHandlerFactory juicerHandlerFactory, ExecutorService threadPool, String propertiesPath) {
        super(juicerHandlerFactory, threadPool);
        juicerInterruptSettings = new Properties(DEFAULT_INTERRUPT_SETTINGS);
        PropertiesUtils.putProperties(juicerInterruptSettings,PropertiesUtils.read(propertiesPath));
        init();
    }

    public JuicerActuator(JuicerHandlerFactory juicerHandlerFactory, Properties properties) {
        this(juicerHandlerFactory,ForkJoinPool.commonPool(),properties);
    }

    public JuicerActuator(JuicerHandlerFactory juicerHandlerFactory, ExecutorService threadPool, Properties properties) {
        super(juicerHandlerFactory, threadPool);
        juicerInterruptSettings = new Properties(DEFAULT_INTERRUPT_SETTINGS);
        PropertiesUtils.putProperties(juicerInterruptSettings,properties);
        init();
    }

    public static JuicerActuator getActuatorByConfiguration(Class<?> clz) {
        return new JuicerActuator(clz, ForkJoinPool.commonPool());
    }

    public static JuicerActuator getActuatorByConfiguration(Class<?> clz, ExecutorService threadPool) {
        return new JuicerActuator(clz, threadPool);
    }

    private JuicerActuator(Class<?> clz, ExecutorService threadPool) {
        super();
        setThreadPool(threadPool);
        juicerInterruptSettings = new Properties(DEFAULT_INTERRUPT_SETTINGS);
        JuicerConfiguration juicerConfiguration = clz.getAnnotation(JuicerConfiguration.class);
        if (juicerConfiguration!=null) {
            juicerInterruptSettings.setProperty(DATA_SAVE_PATH, juicerConfiguration.savePath());
            EnableInterruptResume enableInterruptResume = clz.getAnnotation(EnableInterruptResume.class);
            EnableDataPersistence enableDataPersistence = clz.getAnnotation(EnableDataPersistence.class);
            HandlerScan handlerScan = clz.getAnnotation(HandlerScan.class);
            if (handlerScan!=null) {
                setJuicerHandlerFactory(new JuicerHandlerFactory(handlerScan.basePackages()));
            } else {
                setJuicerHandlerFactory(new JuicerHandlerFactory());
            }
            if (enableInterruptResume!=null) {
                juicerInterruptSettings.setProperty(INTERRUPT_SAVE_ALLOW, String.valueOf(enableInterruptResume.enable()));
            }
            if (enableDataPersistence!=null) {
                juicerInterruptSettings.setProperty(DATA_PERSISTENCE, String.valueOf(enableDataPersistence.enable()));
            }
        }
        init();
    }

    public void init(){
        if(TRUE.equals(juicerInterruptSettings.getProperty(INTERRUPT_SAVE_ALLOW))
                || TRUE.equals(juicerInterruptSettings.getProperty(DATA_PERSISTENCE))) {
            resume(juicerInterruptSettings.getProperty(DATA_SAVE_PATH));
            Runtime.getRuntime().addShutdownHook(new Thread(() -> rescue(getRuntimeStorage())));
        }
    }

    @Override
    public void resume(String path) {
        File file = new File(path);
        if (file.exists()) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(new File(path));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.out.println("Cannot open file.");
            }
            readData(fileInputStream);
            continueTaskQueue();
        }


    }

    @Override
    public void rescue(RuntimeStorage runtimeStorage) {
        if (TRUE.equals(juicerInterruptSettings.getProperty(DATA_PERSISTENCE))) {
            saveData();
        } else {
            if (getRuntimeStorage().getJuicerTaskQueue().size()!=0) {
                saveData();
            }
        }
    }

    private void saveData() {
        SavedDataProto.SavedData savedData = getSavedData();
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(juicerInterruptSettings.getProperty(DATA_SAVE_PATH)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Cannot access file.");
        }
        try {
            Objects.requireNonNull(fileOutputStream);
            fileOutputStream.write(savedData.toByteArray());
            fileOutputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SavedDataProto.SavedData getSavedData() {
        RuntimeStorage runtimeStorage = getRuntimeStorage();
        SavedDataProto.SavedData.Builder builder = SavedDataProto.SavedData.newBuilder();
        builder.putAllJuicerChain(runtimeStorage.getJuicerChain());
        runtimeStorage.getJuicerResultStorage().forEach((handler,results) -> {
            SavedDataProto.SingleResult.Builder resultBuilder = SavedDataProto.SingleResult.newBuilder();
            results.forEach(juicerData -> resultBuilder.addResult(
                    JSON.toJSONString(juicerData)
            ));
            builder.putJuicerResultStorage(handler, resultBuilder.build());
        });
        runtimeStorage.getJuicerTaskQueue().forEach((handler,taskQueue) -> {
            SavedDataProto.SingleTask.Builder taskBuilder = SavedDataProto.SingleTask.newBuilder();
            taskQueue.forEach((url,juicerTask) -> taskBuilder.putTask(
                    url.toExternalForm()
                    , SavedDataProto.JuicerTask.newBuilder()
                            .setUrl(juicerTask.getUrl().toExternalForm())
                            .setNext(juicerTask.getNext()).setIsFinished(juicerTask.isFinished())
                            .build()
            ));
            builder.putJuicerTaskQueue(handler, taskBuilder.build());
        });
        return  builder.build();
    }

    private void readData(InputStream inputStream) {
        SavedDataProto.SavedData savedData = null;
        try {
            savedData = SavedDataProto.SavedData.parseFrom(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        RuntimeStorage runtimeStorage = getRuntimeStorage();
        Objects.requireNonNull(savedData);
        runtimeStorage.getJuicerChain().putAll(savedData.getJuicerChainMap());
        savedData.getJuicerTaskQueueMap().forEach((handler,singleTask) -> {
            Map<URL, JuicerTask> task = runtimeStorage.addHandlerTaskQueue(handler);
            singleTask.getTaskMap().forEach((url,juicerTask) -> {
                try {
                    task.put(new URL(url)
                            , new JuicerTask(
                                    new URL(juicerTask.getUrl())
                                    ,juicerTask.getNext()
                                    ,juicerTask.getIsFinished()
                            )
                    );
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            });
        });
        savedData.getJuicerResultStorageMap().forEach((handler,singleResult) -> {
            List<JuicerData> juicerDataList = runtimeStorage.addJuicerResult(handler);
            for(int i = 0;i<singleResult.getResultCount();i++) {
                juicerDataList.add(JSON.parseObject(singleResult.getResult(i), JuicerData.class));
            }
        });
    }

    private void continueTaskQueue() {
        Map<String, Map<URL, JuicerTask>> juicerTaskQueue = getRuntimeStorage().getJuicerTaskQueue();
        if (juicerTaskQueue.size()!=0) {
            for(String handler: juicerTaskQueue.keySet()) {
                Consumer<Map.Entry<URL,JuicerTask>> continueTask = entry -> {
                    Connection.Response response;
                    try {
                        response = DocumentHelper.getResponse(entry.getKey().toExternalForm(), Headers.getInstance().defaultHeaders());
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
                    JuicerData preData = null;
                    JuicerHandler juicerHandler = getJuicerHandlerFactory().getJuicerHandler(handler);
                    if (juicerHandler.hasParent()) {
                        for (JuicerData juicerData :getRuntimeStorage().getJuicerResult(juicerHandler.getParent())) {
                            if (juicerData.getString("_preUrl").equals(entry.getKey().toExternalForm())) {
                                preData = juicerData;
                            }
                        }
                        if (preData==null) {
                            preData = JuicerData.getInstance();
                        }
                    } else {
                        preData = JuicerData.getInstance();
                    }
                    JuicerData resultData = juicerHandler.parse(preData, response, document, html);
                    entry.getValue().setFinished(true);
                    if (!getRuntimeStorage().isResultExist(handler)) {
                        getRuntimeStorage().addJuicerResult(handler);
                    }
                    getRuntimeStorage().putJuicerData(handler, resultData);
                    if (entry.getValue().getNext()!=null) {
                        try {
                            getDataFromHandler(entry.getValue().getNext(), resultData);
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                            throw new RuntimeException("Error when juicing");
                        }
                    }
                };
                juicerTaskQueue.get(handler).entrySet()
                        .stream()
                        .filter(entry -> !entry.getValue().isFinished())
                        .forEach(continueTask);
                juicerTaskQueue.remove(handler);
            }
        }
    }
}
