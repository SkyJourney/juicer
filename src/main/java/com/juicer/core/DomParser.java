package com.juicer.core;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

/**
 * @author SkyJourney
 */
@FunctionalInterface
public interface DomParser {
    /**
     * 解析响应内容。有三种类型的参数，可以任意选择其中有效的类型进行解析，
     * 将需要的数据封装为{@link JuicerData}对象返回。
     * @param juicerData {@link JuicerData}对象，用于多级传递的数据对象
     * @param response {@link org.jsoup.Connection.Response}类型完整的响应信息
     * @param document Jsoup提供的网页内容{@link org.jsoup.nodes.Document}封装
     * @param html {@link String}字符串的网页内容
     * @return 将存有解析后的数据返回为 {@link JuicerData}对象
     */
    JuicerData parse(JuicerData juicerData, Connection.Response response, Document document, String html);
}
