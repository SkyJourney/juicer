package com.juicer;

import java.util.Properties;
import java.util.concurrent.ForkJoinPool;

public class JuicerActuator extends JuicerCollector {

    private static final Properties DEFAULT_INTERRUPT_SETTINGS;

    static {
        DEFAULT_INTERRUPT_SETTINGS = new Properties();
        DEFAULT_INTERRUPT_SETTINGS.setProperty("juicer.interrupt.save.allow", "false");
        DEFAULT_INTERRUPT_SETTINGS.setProperty("juicer.interrupt.save.path", ".");
    }

    private Properties juicerInterruptSettings;

    public Properties getJuicerInterruptSettings() {
        return juicerInterruptSettings;
    }

    public JuicerActuator(JuicerHandlerFactory juicerHandlerFactory) {
        this(juicerHandlerFactory,ForkJoinPool.commonPool());
    }

    public JuicerActuator(JuicerHandlerFactory juicerHandlerFactory, ForkJoinPool forkJoinPool) {
        super(juicerHandlerFactory, forkJoinPool);
        init();
    }

    private void init() {
        juicerInterruptSettings = new Properties(DEFAULT_INTERRUPT_SETTINGS);

    }


}
