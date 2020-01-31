package top.icdat.juicer.util;

import top.icdat.juicer.core.Headers;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


/**
 * 该类是一个基于Jsoup的静态工具类，封装了获取HTTP响应的基本功能。提供默认请求头，也可以自定义
 * 请求头。该类对于复杂机制的网站爬取性能较弱。
 * 获取响应有两种返回类型，一种是完整的响应信息Response，一种是网页数据Document，可以根据请求
 * 内容切换使用。
 *
 * @author SkyJourney
 * @since 1.0
 */
public class DocumentHelper {

    private DocumentHelper(){}

    public static Headers getSampleHeaders(){
        return Headers.getInstance().defaultHeaders();
    }


    public static Document getDocument(String url, Headers headers) throws Exception {
        return getResponse(url,headers).parse();
    }

    public static Document getDocument(String url) throws Exception {
        return getResponse(url).parse();
    }

    public static Connection.Response getResponse(String url, Headers headers) throws Exception {
        if(headers == null){
            return getResponse(url);
        } else {
            return Jsoup.connect(url)
                    .headers(headers)
                    .ignoreContentType(true)
                    .method(Connection.Method.GET)
                    .execute();
        }
    }

    public static Connection.Response getResponse(String url) throws Exception {
        return Jsoup.connect(url)
                .headers(getSampleHeaders())
                .ignoreContentType(true)
                .method(Connection.Method.GET)
                .execute();
    }
}
