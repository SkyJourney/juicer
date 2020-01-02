package com.juicer.core;

/**
 * @author SkyJourney
 */
@FunctionalInterface
public interface HrefSupplier {
    /**
     * Supply the collection of URL
     * @return JuicerSource
     */
    JuicerSource getUrls(JuicerData juicerData);
}
