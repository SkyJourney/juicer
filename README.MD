![image](https://github.com/SkyJourney/juicer/raw/master/resources/logo.png)
# Juicer - 专注网页数据解析 Focus on your PARSER
**Juicer** 是一个简易的基于 [_Jsoup_][jsoup] 和注解的爬虫模块，用于快速开发网页数据解析。
你可以用注解快速完成网页数据解析。该模块的目的是为了让开发者专注于其解析方法，让模块完成相关的抓取网页和收集操作。
该模块用lambda表达式和Stream API写成，因此需要JDK 1.8以上版本。  
提供数据保存和中断继续的功能，支持独立线程池。

**Juicer** is a simple module based on [_Jsoup_][jsoup] for scraping and parsing data from web pages.  
You can easily use annotations to crawl data from web pages. The purpose of this module is to focus on your parse methods and let the module do the crawl and collection.  
This module was written with lambda expressions and Stream API, so your JDK version must be 1.8 or above.  
Provide data save and interrupt resume features, support independent thread pool.

## 需求 Requirement
JDK 1.8+

## 使用说明 Instruction
**Juicer** 需要两部分，一部分是工厂对象和执行器对象的配置，一部分是需要自己实现的handler类。

**Juicer** needs two parts to run correctly. One part is the configuration of `JuicerHandlerFactory` Object and `JuicerActuator` Object. Another part is the handler classes which user need to write.
### 配置初始化 Configuration Initialization  
**Juicer** 支持多种方式进行配置初始化。配置选项有如下：
- 扫描handler类的包位置
- 开启数据储存
- 开启中断续爬
- 数据存储位置

默认无配置状态下中断续爬和数据储存功能为关闭，包名无配置则不扫描。  
支持注解和配置文件两种形式配置Juicer。  
配置文件形式需要手动设定`JuicerHandlerFactory`扫描包的位置。配置文件有如下三个选项：
```properties
#开启中断续爬，默认为false
juicer.data.interrupt-save=false
#开启数据存储，默认为false
juicer.data.persistence=false
#设置数据存储位置，默认为./JuicerSaveData.jdt
juicer.data.save-path=./JuicerSaveData.jdt
```
将配置文件保存为juicer.properties并存放在resources目录下，juicer将自动读取该文件。若存在其他位置可以通过`JuicerActuator`的构造器设置配置文件位置。  
对象初始化如下：
```java
public class JuicerInit {

    public static void main(String[] args) {
        //构造器参数为包名的String数组，可以扫描多个包位置中的handler
        JuicerHandlerFactory juicerHandlerFactory = new JuicerHandlerFactory("com.juicer.handler");
        //默认构造器，默认载入resources目录中的juicer.properties文件，不存在则使用默认配置
        JuicerActuator juicerActuator = new JuicerActuator(juicerHandlerFactory);
        //从设定位置载入配置文件
        JuicerActuator juicerActuator = new JuicerActuator(juicerHandlerFactory, "config/juicer.properties");
        //带独立线程池的执行对象，可以设定配置文件位置
        ForkJoinPool forkJoinPool = Executors.newWorkStealingPool(8);
        JuicerActuator juicerActuator = new JuicerActuator(juicerHandlerFactory, forkJoinPool);
        JuicerActuator juicerActuator = new JuicerActuator(juicerHandlerFactory, forkJoinPool,"config/juicer.properties");
    }

}
```

也可以通过注解进行配置：
```java
//标注为Juicer配置类，可通过参数修改保存数据的位置
@JuicerConfiguration(savePath = "./JuicerData.jdt")
//设定扫描handler类位置，可以设定多个包位置
@HandlerScan(basePackages = "com.juicer.handler")
//开启数据存储
@EnableDataPersistence
//开启中断续爬
@EnableInterruptResume
public class JuicerInit {

    public static void main(String[] args) {
        //载入注解配置类获取执行对象，可以设定独立线程池
        JuicerActuator juicerActuator = JuicerActuator.getActuatorByConfiguration(JuicerInit.class);
        JuicerActuator juicerActuator = JuicerActuator.getActuatorByConfiguration(JuicerInit.class, new ForkJoinPool(8));
    }

}
```
初始化过程可以直接注入spring框架，只需要生成对应的java bean即可，在业务层注入`JuicerActuator`对象即可调用框架。  

### Handler类  Handler Classes
handler类的标识注解为`@Handler`，其中有两个必须的核心方法，分别需要用`@Href`和`@Parser`标记。`@Href`方法返回`JuicerSource`对象，其为被`@Parser`解析的URL集合。`@Parser`方法解析`JuicerSource`中的URL内容，生成`JuicerData`对象存储需要的解析数据。
```java
//若无值设定，则handler的名称为该类的全小写
//Handler注解可以设定parent属性指定链式上级，可以直接执行最后一级handler
//框架会自动执行链上所有的父级handler。
@Handler("test")
public class TestHandler {

    @Href
    //可以选择传入JuicerData对象，若设定链式上级则为上级解析的数据对象，
    //若没有设定则为空数据。
    public JuicerSource a(JuicerData juicerData) {
        return JuicerSource.getInstance().addUrl("http://www.4399.com");
    }

    @Parser
    //可以选择传入多种类型的对象，详情参见com.juicer.core.DomParser接口
    public JuicerData b(Document document) throws Exception {
//        Thread.sleep(5000);
        return JuicerData.getInstance().addData("records", "records");
    }
}
```
### 获取解析数据 Obtain Parsed Data
执行`JuicerActuator`对象的`getDataFromHandler`方法获取解析数据，参数即为handler的名称。执行`refreshDataFromHandler`方法刷新解析数据，用于定时业务会非常实用。
```java
public class JuicerTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //构造器参数为包名的String数组，可以扫描多个包位置中的handler
        JuicerHandlerFactory juicerHandlerFactory = new JuicerHandlerFactory("com.juicer.handler");
        //默认构造器，默认载入resources目录中的juicer.properties文件，不存在则使用默认配置
        JuicerActuator juicerActuator = new JuicerActuator(juicerHandlerFactory);
        juicerActuator.getDataFromHandler("test").forEach(System.out::println);
        juicerActuator.refreshDataFromHandler("test").forEach(System.out::println);
    }
}
```

[jsoup]:https://github.com/jhy/jsoup/
