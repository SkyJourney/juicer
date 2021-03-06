package top.icdat.juicer.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author SkyJourney
 */
public class RuntimeStorage {

    private Map<String , Map<URL, JuicerTask>> juicerTaskQueue;

    private Map<String, List<JuicerData>> juicerResultStorage;

    private Map<String, String> juicerChain;

    private RuntimeStorage() {
        juicerTaskQueue = new ConcurrentHashMap<>();
        juicerResultStorage = new ConcurrentHashMap<>();
        juicerChain = new ConcurrentHashMap<>();
    }

    public Map<String, Map<URL, JuicerTask>> getJuicerTaskQueue() {
        return juicerTaskQueue;
    }

    public Map<String, List<JuicerData>> getJuicerResultStorage() {
        return juicerResultStorage;
    }

    public Map<String, String> getJuicerChain() {
        return juicerChain;
    }

    private static class SingletonHolder {
        private static final RuntimeStorage INSTANCE = new RuntimeStorage();
    }

    public static RuntimeStorage getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void putJuicerChain(String former, String latter) {
        juicerChain.put(former, latter);
    }

    public String getJuicerChain(String former) {
        return juicerChain.get(former);
    }

    public Map<URL, JuicerTask> addHandlerTaskQueue(String handler) {
        juicerTaskQueue.put(handler, new ConcurrentHashMap<>(16));
        return getHandlerTaskQueue(handler);
    }

    public Map<URL, JuicerTask> getHandlerTaskQueue(String handler) {
        return juicerTaskQueue.get(handler);
    }

    public void removeHandlerTaskQueue(String handler) {
        juicerTaskQueue.remove(handler);
    }

    public void putJuicerTask(String handler, URL url, JuicerTask juicerTask) {
        getHandlerTaskQueue(handler).put(url, juicerTask);
    }

    public JuicerTask getJuicerTask(String handler, URL url) {
        return getHandlerTaskQueue(handler).get(url);
    }

    public void putJuicerData(String handler, JuicerData juicerData){
        juicerResultStorage.get(handler).add(juicerData);
    }

    public List<JuicerData> addJuicerResult(String handler){
        juicerResultStorage.put(handler, new ArrayList<>());
        return getJuicerResult(handler);
    }

    public List<JuicerData> getJuicerResult(String handler){
        return juicerResultStorage.get(handler);
    }

    public void removeJuicerResult(String handler) {
        if (isResultExist(handler)) {
            juicerResultStorage.remove(handler);
        }
    }

    public boolean isResultExist(String handler) {
        return juicerResultStorage.get(handler)!=null;
    }

}
