package com.flea.crawler.model;

import com.flea.crawler.visitor.Visitor;
import com.flea.database.indexer.IndexerVars;
import com.flea.language.FleaLanguageDetector;
import com.flea.language.FleaLanguageDetectorImpl;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;

/**
 * Created by bilgi on 9/7/15.
 */
public class WebPage extends WebResource {
    private final String TYPE = "webpage";
    private String bodyText;
    private String content;
    private String title;
    private Set<String> outgoingLinks = new LinkedHashSet<>();
    private List<WebImage> images = new LinkedList<>();
    private FleaLanguageDetector languageDetector;

    public WebPage(String url, long sessionId) {
        super(url, sessionId);
        languageDetector = new FleaLanguageDetectorImpl();
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<String> getOutgoingLinks() {
        return outgoingLinks;
    }

    public void setOutgoingLinks(Set<String> outgoingLinks) {
        this.outgoingLinks = outgoingLinks;
    }

    public List<WebImage> getImages() {
        return images;
    }

    public void setImages(List<WebImage> images) {
        this.images = images;
    }

    @Override
    public Map<String, Object> parseContent() {
        asMap.put(IndexerVars.SESSION_ID, sessionId);
        asMap.put("url", getUrl());
        asMap.put("type", TYPE);
        asMap.put("links", outgoingLinks);
        asMap.put("images", imagesAsMap());
        return asMap;
    }

    public Map<String, Map<String, Object>> imagesAsMap() {
        Map<String, Map<String, Object>> imageMap = new HashMap<>();
        for (WebImage image : images) {
            Map<String, Object> pairs = new HashMap<>();
            if (image.getAlt() != null && !image.getAlt().isEmpty()) {
                pairs.put("alt", image.getAlt());
            }
            pairs.put("url", image.getSrc());
            pairs.put("type", WebImage.TYPE);
            imageMap.put(image.getSrc(), pairs);
        }
        return imageMap;
    }

    @Override
    public boolean accept(Visitor v) {
        if (uri == null || uri.getHost() == null) {
            logger.info(id + " - Malformed WebResource: " + uri.toASCIIString());
            return false;
        }

        logger.info(id + " Visiting: " + uri.toString());
        v.sendOutboundHttpRequest(this);
        if (this.getStatus() != HttpResponseStatus.NOT_FOUND.code()) {
            String url = uri.toString();
            logger.info("Begin Parsing content for: " + url);
            Set<String> outgoingLinks = getOutgoingLinks();
            Map<String, Object> parsedContent = parseContent();

            if (parsedContent.containsKey("links")) {
                v.queuePages(outgoingLinks);
            }
            logger.info("Finished parsing content for: " + url);
            return true;
        }
        return false;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> asMap() {
        asMap.put(IndexerVars.SESSION_ID, sessionId);
        asMap.put("url", getUrl());
        asMap.put("type", TYPE);
        asMap.put("links", outgoingLinks);
        asMap.put("images", imagesAsMap());
        asMap.put("_id", DigestUtils.md5Hex(uri.toString()));
        asMap.put("url", uri.toString());
        asMap.put("url_not_analyzed", uri.toString());
        asMap.put("domain", uri.getHost());
        asMap.put("path", uri.getPath());
        asMap.put("rank", 0.0d);
        asMap.put("content", content);

        if (languageDetector != null) {
            try {
                asMap.put("language", languageDetector.detectLanguage(bodyText));
            } catch (Exception e) {
                asMap.put("language", "en");
            }
        } else {
            asMap.put("language", "en");
        }

        asMap.put("body", bodyText);
        asMap.put("links", outgoingLinks);
        asMap.put("title", title);
        asMap.put("lastUpdated", new Date());
        return asMap;
    }
}
