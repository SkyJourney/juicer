package test;


import com.juicer.annotation.Handler;
import com.juicer.annotation.Href;
import com.juicer.annotation.Parser;
import com.juicer.core.JuicerData;
import com.juicer.core.JuicerSource;


@Handler(value = "test2",parent = "test")
public class Test2Handler {

    @Href
    public JuicerSource a(JuicerData juicerData) {
        JuicerSource urls = new JuicerSource();
        urls.add("http://www.sina.com.cn");
        return urls;
    }

    @Parser
    public JuicerData b(JuicerData transferData){
        transferData.put("BOTTOM", "BOTTOM");
        return transferData;
    }

}
