package com.braggart;

import static org.junit.Assert.assertTrue;

import com.braggart.juicer.JuicerCollector;
import com.braggart.juicer.JuicerHandlerFactory;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    public static void main(String[] args) {
        JuicerHandlerFactory juicerHandlerFactory = new JuicerHandlerFactory("com.braggart.test");
        JuicerCollector juicerCollector = new JuicerCollector(juicerHandlerFactory);
        System.out.println(juicerCollector.getDataFromHandler("test"));
    }
}
