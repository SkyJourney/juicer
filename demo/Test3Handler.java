package test;


import com.juicer.annotation.Handler;
import com.juicer.annotation.Href;
import com.juicer.annotation.Parser;
import com.juicer.core.JuicerData;
import com.juicer.core.JuicerSource;


@Handler(value = "test3",parent = "test2")
public class Test3Handler {

    @Href
    public JuicerSource a(JuicerData juicerData) {
        JuicerSource urls = new JuicerSource();
        urls.add("http://www.baidu.com");
        return urls;
    }

    @Parser
    public JuicerData b(JuicerData transferData){
        transferData.put("TOP", "TOP");
        return transferData;
    }

}
