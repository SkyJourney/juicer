package com.juicer;

import com.juicer.annotation.Handler;
import com.juicer.annotation.Href;
import com.juicer.annotation.Parser;
import com.juicer.core.*;
import com.juicer.util.ClassScanner;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JuicerHandlerFactory {

    private static Map<String, JuicerHandler> juicerHandlerMap = new ConcurrentHashMap<>();

    public JuicerHandlerFactory(String basePackage) {
        initHandlerClasses(basePackage);
    }

    public JuicerHandlerFactory(List<String> basePackages) {
        basePackages.forEach(this::initHandlerClasses);
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
        Handler handler = clz.getAnnotation(Handler.class);
        Method hrefMethod = getAnnotationMethod(methods, Href.class);
        Method parserMethod = getAnnotationMethod(methods, Parser.class);
        try {
            return new JuicerHandler() {

                private Object handlerImpl = clz.getConstructor().newInstance();

                private String previous = handler.parent();

                @Override
                public String getParent() {
                    return previous;
                }

                @Override
                public boolean hasParent() {
                    return !previous.equals("");
                }

                @Override
                public JuicerData parse(Connection.Response response, Document document, String html) {
                    try {
                        if(parserMethod!=null){
                            JuicerData juicerData = (JuicerData)parserMethod.invoke(handlerImpl, getRequiredParameter(getParameterType(parserMethod),document,html));
                            if(juicerData != null){
                                juicerData.put("_source", document.location());
                                return juicerData;
                            } else {
                                return null;
                            }
                        } else {
                            throw new RuntimeException("Your handler ["+clz.getName()+"] has no method for parser.");
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Cannot call the parser method correctly. " +
                                "Please check the return type, the parameter type and access permission of this method.");
                    }
                }

                @Override
                public JuicerSource getUrls(JuicerData juicerData) {
                    if(juicerData==null){
                        juicerData = new JuicerData();
                    }
                    try {
                        if(hrefMethod!=null){
                            JuicerSource juicerSource =  (JuicerSource)hrefMethod.invoke(handlerImpl,
                                    getRequiredParameter(
                                            getParameterType(hrefMethod),juicerData)
                            );
                            if(juicerSource!=null){
                                return juicerSource;
                            } else {
                                throw new  RuntimeException("@Href method cannot return null.");
                            }
                        } else {
                            throw new RuntimeException("Your handler ["+clz.getName()+"] has no method for getting urls.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Cannot call the getUrls method correctly. " +
                                "Please check the return type, the parameter type and access permission of this method.");
                    }
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot implement the handler for class ["+clz.getName()+"]");
        }
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

    public void addJuicerHandler(String handlerName, HrefSupplier hrefSupplier, DomParser domParser){
        addJuicerHandler(handlerName, null, hrefSupplier, domParser);
    }

    public void addJuicerHandler(String handlerName, String parentName, HrefSupplier hrefSupplier, DomParser domParser){
        juicerHandlerMap.put(handlerName, new JuicerHandler() {
            @Override
            public boolean hasParent() {
                return parentName!=null;
            }

            @Override
            public String getParent() {
                return parentName;
            }

            @Override
            public JuicerData parse(Connection.Response response, Document document, String html) {
                Objects.requireNonNull(domParser);
                JuicerData juicerData =domParser.parse(response, document, html);
                juicerData.put("_source", document.location());
                return juicerData;
            }

            @Override
            public JuicerSource getUrls(JuicerData juicerData) {
                Objects.requireNonNull(hrefSupplier);
                return hrefSupplier.getUrls(juicerData);
            }
        });
    }

}
