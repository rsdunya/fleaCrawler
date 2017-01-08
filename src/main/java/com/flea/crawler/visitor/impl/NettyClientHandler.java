package com.flea.crawler.visitor.impl;

import com.flea.crawler.model.WebImage;
import com.flea.crawler.model.WebPage;
import com.flea.crawler.model.WebResource;
import com.flea.crawler.util.URLUtils;
import com.flea.crawler.visitor.Visitor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * Created by bilgi on 8/23/15.
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    Visitor visitor;

    public NettyClientHandler(Visitor v) {
        visitor = v;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        String url = ctx.channel().remoteAddress().toString();
        url = url.substring(0, url.lastIndexOf("/")).replaceFirst("^(http://)", "");
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            HttpHeaders headers = response.headers();
            if (response.getStatus().code() == HttpResponseStatus.FOUND.code() ||
                    response.getStatus().code() == HttpResponseStatus.TEMPORARY_REDIRECT.code() ||
                    response.getStatus().code() == HttpResponseStatus.MOVED_PERMANENTLY.code() ||
                    response.getStatus().code() == HttpResponseStatus.SEE_OTHER.code()) {
                if (!headers.isEmpty() && headers.get("Location") != null) {
                    String movedToLocation = headers.get("Location");
                    visitor.sendOutboutHttpRequest(movedToLocation);
                    ctx.close();
                    return;
                }
            }
            if (response.getStatus().code() == HttpResponseStatus.NOT_FOUND.code()) {
                visitor.getWebResource().setStatus(HttpResponseStatus.NOT_FOUND.code());
                return;
            }
        }

        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            WebPage webResource = (WebPage) visitor.getWebResource();
            webResource.setContent(content.content().toString(CharsetUtil.UTF_8));
            Document jsoupDoc = Jsoup.parse(webResource.getContent() == null ? "" : webResource.getContent());
            webResource.setBodyText(jsoupDoc.body().text() == null ? "" : jsoupDoc.body().text());
            webResource.setOutgoingLinks(parseOutgoingLinks(webResource, jsoupDoc));
            webResource.setImages(parseImages(webResource, jsoupDoc));
            webResource.setTitle(jsoupDoc.title() == null ? "" : jsoupDoc.title());
            webResource.setContent(jsoupDoc.html());
        }
    }

    private Set<String> parseOutgoingLinks(WebResource webResource, Document jsoupDoc) {
        Elements elements = jsoupDoc.select("a[href]");
        Iterator<Element> iterator = elements.iterator();
        Set<String> tempLinks = new LinkedHashSet<>();
        Set<String> outgoingLinks = new LinkedHashSet<>();
        while (iterator.hasNext()) {
            Element next = iterator.next();
            String href = next.attr("abs:href");
            if (href != null && !href.isEmpty()) {
                outgoingLinks.add(href);
            }
        }
        return outgoingLinks;
    }

    public List<WebImage> parseImages(WebResource webResource, Document jsoupDoc) {
        Elements elements = jsoupDoc.select("img");
        List<WebImage> images = new LinkedList<>();
        for (Element e : elements) {
            String src = e.attr("src");
            String alt = e.attr("alt");
            WebImage imageElement = new WebImage();
            if (src != null && !src.isEmpty()) {
                src = URLUtils.getAbsoluteUrl(webResource.getUrl(), src);
                imageElement.setSrc(src);
            }

            if (alt != null && !alt.isEmpty()) {
                imageElement.setAlt(alt);
            }

            images.add(imageElement);
        }
        return images;
    }

    public List<String> parseMultimedia(Document jsoupDoc) {
        return new ArrayList<>();
    }
}
