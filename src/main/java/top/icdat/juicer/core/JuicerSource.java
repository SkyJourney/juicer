package top.icdat.juicer.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JuicerSource extends ArrayList<URL> {

    public boolean add(String url) {
        try {
            return super.add(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("The input URL is malformed.");
        }

    }

    public String getString(int index) {
        return super.get(index).toExternalForm();
    }

    public static JuicerSource getInstance(){
        return new JuicerSource();
    }

    public JuicerSource addUrl(String url){
        this.add(url);
        return this;
    }

    public JuicerSource addUrl(URL url){
        this.add(url);
        return this;
    }

    public JuicerSource addUrls(String...urls){
        this.addAll(Stream.of(urls).map(url -> {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                throw new RuntimeException("The input URL is malformed.");
            }
        }).collect(Collectors.toList()));
        return this;
    }

    public JuicerSource addUrls(URL...urls){
        Collections.addAll(this, urls);
        return this;
    }

    public JuicerSource addUrls(List<URL> urls){
        this.addAll(urls);
        return this;
    }

}
