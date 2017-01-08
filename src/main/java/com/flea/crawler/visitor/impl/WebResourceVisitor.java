package com.flea.crawler.visitor.impl;

import com.flea.crawler.model.WebPage;
import com.flea.crawler.model.WebResource;
import com.flea.crawler.util.URLUtils;
import com.flea.crawler.visitor.Visitor;
import com.flea.database.graph.FleaGraphDBManager;
import com.flea.database.indexer.FleaIndexer;
import com.flea.search.searcher.FleaSearcher;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISet;
import com.sun.istack.internal.logging.Logger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by bilgi on 8/9/15.
 */
public class WebResourceVisitor<T extends WebPage> implements Visitor, ApplicationContextAware {

    private Logger logger = Logger.getLogger(WebResourceVisitor.class);
    private long updateInterval = 604800000; // 7 days
    private Bootstrap clientBootStrap;
    private ISet<String> sharedPageQueue;
    private Set<String> pageQueue = new ConcurrentSkipListSet<>();
    private Visitor visitor;
    private UUID uid = UUID.randomUUID();
    private long sessionId;
    private WebPage webResource;
    private boolean busy;
    private FleaIndexer indexer;
    private FleaSearcher searcher;
    private FleaGraphDBManager graphDBManager;

    private Random randomGenerator = new Random();

    public WebResourceVisitor(long sessionId) {
        this.sessionId = sessionId;
        this.visitor = this;
    }

    public WebResource getWebResource() {
        return webResource;
    }

    public void setWebResource(WebResource webResource) {
        this.webResource = (T) webResource;
    }

    public void sendOutboutHttpRequest(String url) {
        sendOutboundHttpRequest(createWebResource(url));
    }

    public void sendOutboundHttpRequest(WebResource webResource) {
        InetSocketAddress address = new InetSocketAddress(webResource.getUri().getHost(), webResource.getPort());
        try {
            // Make the connection attempt.
            ChannelFuture connect = clientBootStrap.connect(address.getAddress(), webResource.getPort());
            Channel ch = connect.sync().channel();

            // Prepare the HTTP request.
            FullHttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.GET, webResource.getUri().getRawPath());
            request.headers().set(HttpHeaders.Names.HOST, webResource.getUri().getHost());
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            request.headers().set(HttpHeaders.Names.USER_AGENT, "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");

            // Send the HTTP request.
            ch.writeAndFlush(request);

            // Wait for the server to close the connection.
            ch.closeFuture().sync();
            logger.info("Request sent");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Shut down executor threads to exit.
//            clientBootStrap.group().shutdownGracefully();
        }
    }

    public Bootstrap initializeChannel() {
        Config cfg = new Config();
        cfg.setProperty("hazelcast.logging.type", "none");
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
        sharedPageQueue = instance.getSet("sharedPageQueue");

        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap clientBootStrap = new Bootstrap();
        clientBootStrap.group(group)
                .channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new HttpClientCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(1048576));
                ch.pipeline().addLast(new NettyClientHandler(visitor));
            }
        });
        return clientBootStrap;
    }

    public WebResource createWebResource(String url) {
        webResource = new WebPage(URLUtils.cleanUrl(url), sessionId);
        return webResource;
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    @Override
    public void queuePages(Set<String> pages) {
        sharedPageQueue.addAll(pages);
        pageQueue.addAll(pages);
    }

    @Override
    public boolean isBusy() {
        return busy;
    }

    @Override
    public void createEdgeBatch(String url, Map<String, Object> additionalParams) {
        /*TODO change this to actual session id - IndexerVars.CRAWL_SESSION -*/
        graphDBManager.createEdgeBatch(url, additionalParams);
    }

    @Override
    public void queue(String href) {
        webResource.getOutgoingLinks().add(href);
    }

    @Override
    public WebResource visit(WebResource webResource) {
        if (webResource.accept(this)) {
            Map<String, Object> webResourceAsMap = webResource.asMap();
            indexer.putIndex(webResourceAsMap);
            visitor.createEdgeBatch(webResource.getUrl(), webResourceAsMap);
        }
        return webResource;
    }

    @Override
    public void run() {
        clientBootStrap = initializeChannel();
        if (webResource != null) {
            busy = true;
            visit(webResource);
            busy = false;
        }

        while (true) {
            for (String page : pageQueue) {
                setWebResource(new WebPage(page, sessionId));
                visit(webResource);
                pageQueue.remove(page);
            }
            if (sharedPageQueue.size() > 0) {
                int random = randomGenerator.nextInt(sharedPageQueue.size());
                String page = (String) sharedPageQueue.toArray()[random];
                sharedPageQueue.remove(page);
                setWebResource(new WebPage(page, sessionId));
                visit(webResource);
            }
        }
    }

    public static void main(String[] args) {
        String url = "www.rsdunya.com:80";
        WebResourceVisitor webResourceVisitor = new WebResourceVisitor(1l);
        webResourceVisitor.visit(webResourceVisitor.createWebResource(url));
    }


    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        indexer = (FleaIndexer) context.getBean("fleaIndexer");
        searcher = (FleaSearcher) context.getBean("fleaSearcher");
        graphDBManager = (FleaGraphDBManager) context.getBean("fleaGraphDBManager");
    }
}