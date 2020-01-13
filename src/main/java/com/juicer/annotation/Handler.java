package com.juicer.annotation;

import java.lang.annotation.*;

/**
 * 用于标注handler实例，类注解。其中有两个属性：{@link Handler#value()}值为handler实例的名称，若无设定则以
 * handler类的全小写字符串作为handler名；{@link Handler#parent()}可以设定该handler的链式上级，用于实现自动
 * 链式抓取。<br>
 * handler实例要求含有标有{@link com.juicer.annotation.Href}和{@link com.juicer.annotation.Parser}
 * 注解的两个方法，否则在调用handler时会抛出无方法异常。<br>
 * Used to mark handler instances. TYPE annotation. There are two properties:
 * {@link Handler#value()} is the name of the handler instance. If it is not set, the lowercase string
 * of the handler class is used as the handler name; {@link Handler#parent()} can set the chained
 * superior of the handler for automatic chained crawling.<br>
 * The handler instance requires two methods marked with {@link com.juicer.annotation.Href} and
 * {@link com.juicer.annotation.Parser} annotations, otherwise a RuntimeException said have no method
 * would be thrown when the handler is called.
 *
 * @author SkyJourney
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Handler {
    /**
     * 标记handler实例的名称，未设定时为空字符串，此时该实例会被识别其类名的全小写作为实例名。.<br>
     * Mark the name of the handler instance. If it is not set, it is an empty string.
     * At this time, the instance will be recognized by its lowercase class name as the instance name.
     * @return handler实例的名称 <br>The name of the handler instance.
     */
    String value() default "";

    /**
     * 设定该handler实例的链式上级，通过名称直接指定。<br>
     * Set the chained superior of this handler instance, and specify it directly by name.
     * @return 链式上级handler的名称 <br>The name of chained superior handler name
     */
    String parent() default "";
}
