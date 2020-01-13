package com.juicer.annotation;

import com.juicer.core.JuicerData;

import java.lang.annotation.*;

/**
 * 指定Handler实例的URL地址提供方法，对应{@link com.juicer.core.HrefSupplier}接口中的
 * {@link com.juicer.core.HrefSupplier#getUrls(JuicerData)}方法。一个handler实例只允
 * 许一个该注解标记的方法。<br>
 * Specify the URL supplier method of the Handler instance, corresponding to the
 * {@link com.juicer.core.HrefSupplier#getUrls(JuicerData)} method in the
 * {@link com.juicer.core.HrefSupplier} interface. A handler instance allows only
 * one method marked by this annotation.
 * @author SkyJourney
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Href {
}
