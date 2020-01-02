package com.braggart.juicer.test;

import com.braggart.juicer.JuicerCollector;
import com.braggart.juicer.JuicerHandlerFactory;

public class JuicerTest {
    public static void main(String[] args) {
        JuicerHandlerFactory juicerHandlerFactory = new JuicerHandlerFactory("com.braggart.juicer.test");
        JuicerCollector juicerCollector = new JuicerCollector(juicerHandlerFactory);
        juicerCollector.getDataFromHandler("test");
    }
}
