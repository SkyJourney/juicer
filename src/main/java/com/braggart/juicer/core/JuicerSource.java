package com.braggart.juicer.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class JuicerSource extends ArrayList<URL> {

    public boolean add(String url) {
        boolean success = false;
        try {
            success =  super.add(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return success;
    }

    public String getString(int index) {
        return super.get(index).toExternalForm();
    }
}
