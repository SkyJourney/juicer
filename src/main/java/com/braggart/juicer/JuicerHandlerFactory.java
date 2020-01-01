package com.braggart.juicer;

import com.braggart.juicer.annotation.Handler;
import com.braggart.juicer.annotation.Href;
import com.braggart.juicer.annotation.Parser;
import com.braggart.juicer.core.JuicerData;
import com.braggart.juicer.core.JuicerHandler;
import com.braggart.juicer.core.JuicerSource;
import com.braggart.juicer.util.ClassScanner;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JuicerHandlerFactory {

    private static Map<String,JuicerHandler> juicerHandlerMap = new HashMap<>();

    public JuicerHandlerFactory(String basePackage) {
        initHandlerClasses(basePackage);
    }

    public JuicerHandlerFactory(List<String> basePackages) {
        basePackages.forEach(this::initHandlerClasses);
    }

    public Map<String, JuicerHandler> getJuicerHandlerMap() {
        return juicerHandlerMap;
    }

    public JuicerHandler getJuicerHandler(String key){
        return juicerHandlerMap.get(key);
    }

    private void initHandlerClasses(String basePackage){
        List<Class<?>> classes = ClassScanner.getClasses(basePackage);
        juicerHandlerMap.putAll(
                classes.parallelStream()
                        .filter(isHandler)
                        .collect(Collectors.toMap(findHandlerName, juicerHandlerImpl))
        );
    }

    private Predicate<Class<?>> isHandler = clz ->  clz.getAnnotation(Handler.class)!=null;

    private Function<Class<?>,String> findHandlerName = clz -> {
        Handler handler = clz.getAnnotation(Handler.class);
        String value = handler.value();
        if (value.equals("")){
            return clz.getSimpleName().toLowerCase();
        } else {
            return value;
        }
    };

    private Function<Class<?>,JuicerHandler> juicerHandlerImpl = clz -> {
        Method[] methods = clz.getMethods();
        Method hrefMethod = getAnnotationMethod(methods, Href.class);
        Method parserMethod = getAnnotationMethod(methods, Parser.class);
        try {
            return new JuicerHandler() {
                Object handlerImpl = clz.getConstructor().newInstance();

                @Override
                public JuicerData parse(Connection.Response response, Document document, String html) {
                    JuicerData juicerData = null;
                    try {
                        if(parserMethod!=null){
                            juicerData = (JuicerData)parserMethod.invoke(handlerImpl, getRequiredParameter(getParameterType(parserMethod),document,html));
                            juicerData.put("_source", document.location());
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return juicerData;
                }

                @Override
                public JuicerSource getUrls(JuicerData juicerData) {
                    JuicerSource urls = null;
                    if(juicerData==null){
                        juicerData = new JuicerData();
                    }
                    try {
                        if(hrefMethod!=null){
                            urls = (JuicerSource)hrefMethod.invoke(handlerImpl,
                                    getRequiredParameter(
                                            getParameterType(hrefMethod),juicerData)
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return urls;
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    };

    private <A extends Annotation> Method getAnnotationMethod(Method[] methods, Class<A> annotationClass){
        Method[] methods1 = Stream.of(methods).filter(method -> method.getAnnotation(annotationClass)!=null).toArray(Method[]::new);
        if(methods1.length==0){
            return null;
        } else if(methods1.length>1){
            throw new RuntimeException("More than 1 Method for ["+annotationClass.getName()+"]");
        } else {
            return methods1[0];
        }
    }

    private Class<?>[] getParameterType(Method method){
        Parameter[] parameters = method.getParameters();
        return Stream.of(parameters).map(Parameter::getType).toArray(Class<?>[]::new);
    }

    private Object[] getRequiredParameter(Class<?>[] classes, Object...objects){
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
