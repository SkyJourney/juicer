package com.juicer.util;

import com.juicer.core.Headers;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author SkyJourney
 */
public class JsoupDocumentHelper {
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
