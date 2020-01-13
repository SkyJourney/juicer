package com.juicer.core;

/**
 * 提供请求地址的接口，设定为函数式接口可以方便的用lambda表达式动态提供方法内容。
 * 该接口会根据handler实例中的 {@link com.juicer.annotation.Href} 注解方法生成实现
 * 方法。<br>
 * An interface for supplying request addresses, set as a FunctionalInterface that
 * can conveniently provide method content dynamically with lambda expressions.
 * This interface generates an implementation method based on the
 * {@link com.juicer.annotation.Href} annotation method in the handler instance.
 * @author SkyJourney
 * @since 1.0
 */
@FunctionalInterface
public interface HrefSupplier {
    /**
     * 为handler实例提供请求地址集，封装为 {@link JuicerSource} 对象返回。<br>
     * This method is used to supply request addresses which would be consumed by
     * {@link com.juicer.annotation.Parser} method in handler instance.
     * @param juicerData 传入{@link JuicerData}类型的数据，可用于生成请求地址，该对象
     *                   可直接从链式上级传入<br>
     *                   Pass in data of type {@link JuicerData}, which can be used
     *                   to generate request addresses of the handler instance. This
     *                   object can be passed in directly from the chained superior.
     * @return  请求地址集以 {@link JuicerSource} 对象的形式返回。<br>The request address
     * set is returned as a {@link JuicerSource} object.
     */
    JuicerSource getUrls(JuicerData juicerData);
}
