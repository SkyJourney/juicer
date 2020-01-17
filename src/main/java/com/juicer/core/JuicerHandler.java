package com.juicer.core;

import com.juicer.JuicerHandlerFactory;

/**
 * 用于生成handler实例的接口，继承了 {@link com.juicer.core.HrefSupplier} 和
 * {@link com.juicer.core.DomParser} 两个接口，并要求了链式上级元素的判断和提取
 * 方法。其实例通过 {@link JuicerHandlerFactory} 生成。 <br>
 * The interface is used to generate the handler instance. it inherits the
 * two interfaces {@link com.juicer.core.HrefSupplier} and
 * {@link com.juicer.core.DomParser}, and requires the judgment method and
 * getter method of the chained superior element. Its instance is generated
 * by {@link JuicerHandlerFactory}.
 * @author SkyJourney
 * @since 1.0
 */
public interface JuicerHandler extends HrefSupplier, DomParser{
    /**
     * 判断handler实例是否有链式上级。 <br>
     * Determine if the handler instance has a chained superior.
     * @return 存在链式上级返回true，否则返回false。<br>
     * If there is a chain superior, it returns true, otherwise it returns false.
     */
    boolean hasParent();

    /**
     * 获取链式上级的名称。<br>
     * Get the name of the chained superior.
     * @return 链式上级的名称。<br> the name of the chained superior.
     */
    String getParent();

}
