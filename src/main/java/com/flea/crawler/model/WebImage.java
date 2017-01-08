package com.flea.crawler.model;

/**
 * Created by bilgi on 9/7/15.
 */
public class WebImage extends WebResource {

    public static String TYPE = "image";

    private String alt;

    private String src;

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
