package com.juicer.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.stream.Stream;

public class HandlerReflectHelper {

    public static <A extends Annotation> Method getAnnotationMethod(Method[] methods, Class<A> annotationClass){
        Method[] methods1 = Stream.of(methods).filter(method -> method.getAnnotation(annotationClass)!=null).toArray(Method[]::new);
        if(methods1.length==0){
            return null;
        } else if(methods1.length>1){
            throw new RuntimeException("More than 1 Method for ["+annotationClass.getName()+"]");
        } else {
            return methods1[0];
        }
    }

    public static Class<?>[] getParameterType(Method method){
        Parameter[] parameters = method.getParameters();
        return Stream.of(parameters).map(Parameter::getType).toArray(Class<?>[]::new);
    }

    public static Object[] getRequiredParameter(Class<?>[] classes, Object...objects){
        Object[] parameters = new Object[classes.length];
        if(classes.length>0){
            for(int i = 0;i<classes.length;i++){
                for (Object object : objects) {
                    if (object.getClass() == classes[i]) {
                        parameters[i] = object;
                    }
                }
            }
        }
        return parameters;
    }

}
