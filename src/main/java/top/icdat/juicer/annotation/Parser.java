package top.icdat.juicer.annotation;

import top.icdat.juicer.core.DomParser;
import top.icdat.juicer.core.JuicerData;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.lang.annotation.*;

/**
 * 指定Handler实例的网页解析方法，对应{@link DomParser}接口中的
 * {@link DomParser#parse(JuicerData, Connection.Response, Document, String)}
 * 方法。一个handler实例只允许一个该注解标记的方法。<br>
 * Specify the parsing method for web page of the Handler instance, corresponding to the
 * {@link DomParser#parse(JuicerData, Connection.Response, Document, String)}
 * method in the {@link DomParser} interface. A handler instance allows only
 * one method marked by this annotation.
 * @author SkyJourney
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Parser {
}
