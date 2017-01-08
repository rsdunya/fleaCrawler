package com.flea.crawler.model;

import com.flea.crawler.visitor.Visitable;
import com.flea.crawler.visitor.Visitor;
import com.sun.istack.internal.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bilgi on 8/9/15.
 * <p/>
 * TODO webresource is abstract and should be extended, webpage, txtdoc, pdfdoc, multimedia, image etc
 */
public abstract class WebResource implements Visitable, Serializable {

    protected Logger logger = Logger.getLogger(WebResource.class);
    protected URI uri;
    protected String id;
    protected int port;
    protected int status;
    protected Map<String, Object> asMap = new HashMap<>();
    protected long sessionId;
    protected String mime;

    public WebResource() {

    }

    public WebResource(String url) {
        url = url.trim().replaceAll(" ", "%20");
        try {
            uri = new URI(url);
            id = DigestUtils.md5Hex(url);
            int port = uri.getPort();
            String scheme = uri.getScheme();
            if (port == -1) {
                if ("http".equalsIgnoreCase(scheme)) {
                    this.port = 80;
                } else if ("https".equalsIgnoreCase(scheme)) {
                    this.port = 443;
                }
                if (port != -1 || scheme == null) {
                    if (scheme == null) {
                        scheme = "http";
                        this.port = 80;
                    }
                    URIBuilder builder = new URIBuilder("//" + url);
                    builder.setScheme(scheme);
                    uri = builder.build();
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public WebResource(String url, long sessionId) {
        this(url);
        this.sessionId = sessionId;
    }

    @Override
    public boolean accept(Visitor v) {
        v.visit(this);
        return true;
    }

    public abstract String getType();

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getUrl() {
        return uri.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, Object> parseContent() {
        return new HashMap<>();
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public Map<String, Object> getAsMap() {
        return asMap;
    }

    public void setAsMap(Map<String, Object> asMap) {
        this.asMap = asMap;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public Map<String, Object> asMap() {
        return new HashMap<>();
    }
}
