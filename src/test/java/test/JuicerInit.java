package test;

import top.icdat.juicer.JuicerActuator;
import top.icdat.juicer.annotation.EnableDataPersistence;
import top.icdat.juicer.annotation.EnableInterruptResume;
import top.icdat.juicer.annotation.HandlerScan;
import top.icdat.juicer.annotation.JuicerConfiguration;

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
//        JuicerActuator juicerActuator = JuicerActuator.getActuatorByConfiguration(JuicerInit.class, new ForkJoinPool(8));
    }

}
