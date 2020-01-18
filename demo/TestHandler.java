package test;


import com.juicer.annotation.Handler;
import com.juicer.annotation.Href;
import com.juicer.annotation.Parser;
import com.juicer.core.JuicerData;
import com.juicer.core.JuicerSource;
import org.jsoup.Connection;


@Handler("test")
public class TestHandler {

    @Href
    public JuicerSource a(JuicerData juicerData) {
        return JuicerSource.getInstance().addUrl("http://www.4399.com");
    }

    @Parser
    public JuicerData b(Connection.Response response) throws Exception {
//        Thread.sleep(5000);
        return JuicerData.getInstance().addData("records", "records");
    }

}
