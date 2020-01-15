package com.juicer;

import com.juicer.core.JuicerCollector;
import com.juicer.core.JuicerHandlerFactory;

import java.util.concurrent.ForkJoinPool;

public class JuicerActuator extends JuicerCollector {

    public JuicerActuator(JuicerHandlerFactory juicerHandlerFactory) {
        super(juicerHandlerFactory);
    }

    public JuicerActuator(JuicerHandlerFactory juicerHandlerFactory, ForkJoinPool forkJoinPool) {
        super(juicerHandlerFactory, forkJoinPool);
    }


}
