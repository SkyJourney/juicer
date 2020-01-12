package com.juicer.core;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

/**
 * 解析网页内容的接口，设定为函数式接口可以方便的用lambda表达式动态提供方法内容。
 * 该接口会根据handler实例中的 {@link com.juicer.annotation.Parser} 注解方法生成实现
 * 方法。<br/>
 * An interface for parsing web page content, set as a FunctionalInterface that
 * can conveniently provide method content dynamically with lambda expressions.
 * This interface generates an implementation method based on the
 * {@link com.juicer.annotation.Parser} annotation method in the handler instance.
 * @author SkyJourney
 * @since 1.0
 */
@FunctionalInterface
public interface DomParser {
    /**
     * 解析响应内容。有三种类型的参数，可以任意选择其中有效的类型进行解析，将需要的数据封装为
     * {@link JuicerData} 对象返回。<br/>
     * This method is used to parse web page content which is gotten from the URL that the
     * {@link com.juicer.annotation.Href} method provides in the handler instance. There are
     * three types of parameters, which can be arbitrarily selected for parsing. Return a
     * {@link com.juicer.core.JuicerData} object with the parsed data.
     * @param juicerData {@link JuicerData} 对象，从链式上级传递下来的数据对象 <br/>
     *                   {@link JuicerData} data Object from chained superior handler.
     * @param response {@link org.jsoup.Connection.Response} 类型完整的响应信息 <br/>
     *                 {@link org.jsoup.Connection.Response} object for complete response information.
     * @param document Jsoup提供的网页内容 {@link org.jsoup.nodes.Document} 封装 <br/>
     *                 The web page content with {@link org.jsoup.nodes.Document} object which is
     *                 easy to parse.
     * @param html {@link String}字符串的网页内容 <br/>
     *                 The whole page html with {@link String} type.
     * @return 将存有解析后的数据返回为 {@link JuicerData} 对象 <br/>
     * Returns the parsed data as a {@link JuicerData} object
     */
    JuicerData parse(JuicerData juicerData, Connection.Response response, Document document, String html);
}
