package com.juicer.core;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author SkyJourney
 */
public class JuicerTask {

    private URL url;

    private String next;

    private boolean isFinished;

    public JuicerTask() {
    }

    public JuicerTask(URL url) {
        this.url = url;
    }

    public JuicerTask(String url){
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("The input URL is malformed.");
        }
    }

    public JuicerTask(URL url, String next, boolean isFinished) {
        this.url = url;
        this.next = next;
        this.isFinished = isFinished;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("The input URL is malformed.");
        }
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }
}
