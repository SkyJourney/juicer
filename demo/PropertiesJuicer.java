package test;

import com.juicer.JuicerActuator;
import com.juicer.annotation.EnableDataPersistence;
import com.juicer.annotation.HandlerScan;
import com.juicer.annotation.JuicerConfiguration;
import com.juicer.core.RuntimeStorage;

import java.util.concurrent.ExecutionException;

public class JuicerTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        JuicerHandlerFactory juicerHandlerFactory = new JuicerHandlerFactory("com.juicer.test");
        JuicerActuator juicerActuator = new JuicerActuator(juicerHandlerFactory,"juicer.proerties");
        juicerActuator.refreshDataFromHandler("test3").forEach(System.out::println);

    }
}
