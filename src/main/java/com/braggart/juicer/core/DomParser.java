package com.braggart.juicer.core;

import org.jsoup.nodes.Document;

/**
 * @author SkyJourney
 */
@FunctionalInterface
public interface DomParser {
    /**
     * Supply the map of data from page.
     * @return
     */
    JuicerData parse(Document document, String html);
}
