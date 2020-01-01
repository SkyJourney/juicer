package com.braggart.juicer.core;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

/**
 * @author SkyJourney
 */
@FunctionalInterface
public interface DomParser {
    /**
     * Supply the map of data from page.
     * @return JuicerData
     */
    JuicerData parse(Connection.Response response, Document document, String html);
}
