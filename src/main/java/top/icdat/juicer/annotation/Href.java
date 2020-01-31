package top.icdat.juicer.annotation;

import top.icdat.juicer.core.JuicerData;
import top.icdat.juicer.core.HrefSupplier;

import java.lang.annotation.*;

/**
 * 指定Handler实例的URL地址提供方法，对应{@link HrefSupplier}接口中的
 * {@link HrefSupplier#getUrls(JuicerData)}方法。一个handler实例只允
 * 许一个该注解标记的方法。<br>
 * Specify the URL supplier method of the Handler instance, corresponding to the
 * {@link HrefSupplier#getUrls(JuicerData)} method in the
 * {@link HrefSupplier} interface. A handler instance allows only
 * one method marked by this annotation.
 * @author SkyJourney
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Href {
}
