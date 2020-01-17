package com.juicer;

import com.juicer.annotation.Handler;
import com.juicer.annotation.Href;
import com.juicer.annotation.Parser;
import com.juicer.core.*;
import com.juicer.util.ClassScanner;
import com.juicer.util.JuicerReflectHelper;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 该工厂类是注册Handler的核心组件，通过包名扫描自动添加Handler到工厂中，注解方法被加载为
 * {@link com.juicer.core.JuicerHandler} 接口的实现。工厂提供动态添加Handler的方法
 * addJuicerHandler，利用函数式编程添加自定义Handler。 <br>
 * This factory class is the core component for registering Handlers. Handlers
 * are automatically added to the factory through package name scanning, and
 * annotation methods are loaded as implementations of the
 * {@link com.juicer.core.JuicerHandler} interface. The factory provides the method
 * addJuicerHandler that dynamically adds Handlers, and uses functional
 * programming to add custom Handlers. <br>
 * @author SkyJourney
 * @since 1.0
 */
public class JuicerHandlerFactory {


    /**
     * 私有的线程安全Map对象，用来存储被注册的Handler。通过方法
     * {@link JuicerHandlerFactory #getJuicerHandler}获取对应对象。键名由
     * 注解属性决定，若未设定默认为全小写类名。
     */
    private static Map<String, JuicerHandler> juicerHandlerMap = new ConcurrentHashMap<>();


    /**
     * 默认空构造，只用来生成对象，不加载任何Handler
     */
    public JuicerHandlerFactory() {
    }

    public JuicerHandlerFactory(String basePackage) {
        initHandlerClasses(basePackage);
    }

    public JuicerHandlerFactory(List<String> basePackages) {
        basePackages.forEach(this::initHandlerClasses);
    }

    public JuicerHandlerFactory(String...basePackages) {
        Arrays.asList(basePackages).forEach(this::initHandlerClasses);
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

    private static Predicate<Class<?>> isHandler = clz ->  clz.getAnnotation(Handler.class)!=null;

    private static Function<Class<?>,String> findHandlerName = clz -> {
        Handler handler = clz.getAnnotation(Handler.class);
        String value = handler.value();
        if ("".equals(value)){
            return clz.getSimpleName().toLowerCase();
        } else {
            return value;
        }
    };

    private static Function<Class<?>,JuicerHandler> juicerHandlerImpl = clz -> {
        Method[] methods = clz.getMethods();
        Handler handler = clz.getAnnotation(Handler.class);
        Method hrefMethod = JuicerReflectHelper.getAnnotationMethod(methods, Href.class);
        Method parserMethod = JuicerReflectHelper.getAnnotationMethod(methods, Parser.class);
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
                    return !"".equals(previous);
                }

                @Override
                public JuicerData parse(JuicerData juicerData, Connection.Response response, Document document, String html) {
                    try {
                        if(parserMethod!=null){
                            if(juicerData==null){
                                juicerData = new JuicerData();
                            }
                            JuicerData juicerData1 = (JuicerData)parserMethod.invoke(
                                    handlerImpl,
                                    JuicerReflectHelper.getRequiredParameter(
                                            JuicerReflectHelper.getParameterType(parserMethod)
                                            ,juicerData
                                            ,response
                                            ,document
                                            ,html
                                    )
                            );
                            if(juicerData1 != null){
                                String source = juicerData1.getString("_source");
                                if (source!=null) {
                                    juicerData1.put("_preUrl", source);
                                }
                                juicerData1.put("_source", document.location());
                                return juicerData1;
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
                                    JuicerReflectHelper.getRequiredParameter(
                                            JuicerReflectHelper.getParameterType(hrefMethod)
                                            ,juicerData
                                    )
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

    public void addJuicerHandler(String handlerName, HrefSupplier hrefSupplier, DomParser domParser){
        addJuicerHandler(handlerName, "", hrefSupplier, domParser);
    }

    public void addJuicerHandler(String handlerName, String parentName, HrefSupplier hrefSupplier, DomParser domParser){
        juicerHandlerMap.put(handlerName, new JuicerHandler() {

            private String previous = parentName;

            @Override
            public boolean hasParent() {
                return !"".equals(previous);
            }

            @Override
            public String getParent() {
                return previous;
            }

            @Override
            public JuicerData parse(JuicerData juicerData,Connection.Response response, Document document, String html) {
                Objects.requireNonNull(domParser);
                JuicerData juicerData1 =domParser.parse(juicerData ,response, document, html);
                juicerData1.put("_source", document.location());
                return juicerData1;
            }

            @Override
            public JuicerSource getUrls(JuicerData juicerData) {
                Objects.requireNonNull(hrefSupplier);
                return hrefSupplier.getUrls(juicerData);
            }
        });
    }

}
