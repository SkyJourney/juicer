package test;

import com.juicer.JuicerActuator;
import com.juicer.annotation.EnableDataPersistence;
import com.juicer.annotation.HandlerScan;
import com.juicer.annotation.JuicerConfiguration;
import com.juicer.core.RuntimeStorage;

import java.util.concurrent.ExecutionException;

@JuicerConfiguration
@EnableDataPersistence
@HandlerScan(basePackages = "com.juicer.test")
public class JuicerTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        JuicerActuator juicerActuator = JuicerActuator.getActuatorByConfiguration(JuicerTest.class);
        juicerActuator.refreshDataFromHandler("test3").forEach(System.out::println);

    }
}
