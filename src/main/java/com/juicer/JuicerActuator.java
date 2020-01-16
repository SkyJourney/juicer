package com.juicer;

import com.juicer.annotation.EnableDataPersistence;
import com.juicer.annotation.EnableInterruptResume;
import com.juicer.annotation.HandlerScan;
import com.juicer.annotation.JuicerConfiguration;
import com.juicer.core.InterruptResume;
import com.juicer.core.RuntimeStorage;
import com.juicer.util.PropertiesUtils;

import java.util.Properties;
import java.util.concurrent.ForkJoinPool;

/**
 * @author SkyJourney
 */
public class JuicerActuator extends AbstractJuicerCollector implements InterruptResume {

    private static final Properties DEFAULT_INTERRUPT_SETTINGS;
    private static final String INTERRUPT_SAVE_ALLOW = "juicer.interrupt.save.allow";
    private static final String DATA_SAVE_PATH = "juicer.data.save.path";
    private static final String DATA_PERSISTENCE = "juicer.data.persistence.allow";
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

    public JuicerActuator(JuicerHandlerFactory juicerHandlerFactory, String propertiesPath) {
        this(juicerHandlerFactory,ForkJoinPool.commonPool(),propertiesPath);
    }

    public JuicerActuator(JuicerHandlerFactory juicerHandlerFactory, ForkJoinPool forkJoinPool, String propertiesPath) {
        super(juicerHandlerFactory, forkJoinPool);
        juicerInterruptSettings = new Properties(DEFAULT_INTERRUPT_SETTINGS);
        PropertiesUtils.putProperties(juicerInterruptSettings,PropertiesUtils.read(propertiesPath));
        init();
    }

    public JuicerActuator(Class<?> clz) {
        this(clz, ForkJoinPool.commonPool());
    }

    public JuicerActuator(Class<?> clz, ForkJoinPool forkJoinPool) {
        super();
        setForkJoinPool(forkJoinPool);
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

    }

    @Override
    public void rescue(RuntimeStorage runtimeStorage) {

    }
}
