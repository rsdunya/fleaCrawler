package com.flea.crawler.visitor;

import com.flea.crawler.model.WebResource;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by bilgi on 8/9/15.
 */
public interface Visitor extends Runnable, Serializable {

    public WebResource visit(WebResource webResource);

    public WebResource getWebResource();

    public void setWebResource(WebResource webResource);

    public void createEdgeBatch(String url, Map<String, Object> additionalParams);

    public void sendOutboutHttpRequest(String url);

    public void sendOutboundHttpRequest(WebResource webResource);

    public void queue(String href);

    public boolean isBusy();

    public void queuePages(Set<String> pages);
}
