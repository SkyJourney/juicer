package com.braggart.juicer.util;

import com.braggart.juicer.core.Headers;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author SkyJourney
 */
public class JsoupDocumentHelper {

    private static Headers headers = new Headers();

    public static Headers getSampleHeaders(){
        Headers headers = new Headers();
        headers.put("Accept", "*/*");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("Cache-Control", "max-age=0");
        headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36");
        return headers;
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
