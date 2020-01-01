package com.braggart.juicer.core;

import java.net.URL;
import java.util.List;

/**
 * @author SkyJourney
 */
@FunctionalInterface
public interface HrefSupplier {
    /**
     * Supply the collection of URL
     * @return
     */
    List<URL> getUrls(JuicerData juicerData);
}
