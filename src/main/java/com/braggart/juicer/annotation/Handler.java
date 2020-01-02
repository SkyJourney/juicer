package com.braggart.juicer.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Handler {
    String value() default "";
    String parent() default "";
}
