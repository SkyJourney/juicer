package com.juicer.core;

/**
 * @author SkyJourney
 */
@FunctionalInterface
public interface HrefSupplier {
    /**
     * Supply the collection of URL
     * @param juicerData 传入{@link JuicerData}类型的数据，用于生成下一级的URL
     * @return JuicerSource
     */
    JuicerSource getUrls(JuicerData juicerData);
}
