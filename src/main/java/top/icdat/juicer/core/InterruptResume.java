package top.icdat.juicer.core;


/**
 * @author SkyJourney
 */
public interface InterruptResume {

    void resume(String path);

    void rescue(RuntimeStorage runtimeStorage);
}
