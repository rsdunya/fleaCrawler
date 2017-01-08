package com.flea.crawler.model;

import com.flea.crawler.visitor.Visitor;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

/**
 * Created by bilgi on 8/23/15.
 */
public class PageQueueEntry implements Serializable {
    private UUID sessionID;
    private UUID visitorID;
    private WebResource webResource;
    private Set<String> outgoingLinks;
    private Visitor visitor;

    public PageQueueEntry(WebResource webResource, UUID sessionID, Visitor visitor) {
        this.webResource = webResource;
        this.sessionID = sessionID;
        this.visitor = visitor;
    }

    public UUID getSessionID() {
        return sessionID;
    }

    public void setSessionID(UUID sessionID) {
        this.sessionID = sessionID;
    }

    public Visitor getVisitor() {
        return visitor;
    }

    public void setVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    public WebResource getWebResource() {
        return webResource;
    }

    public void setWebResource(WebResource webResource) {
        this.webResource = webResource;
    }

    public Set<String> getOutgoingLinks() {
        return outgoingLinks;
    }

    public void setOutgoingLinks(Set<String> outgoingLinks) {
        this.outgoingLinks = outgoingLinks;
    }
}
