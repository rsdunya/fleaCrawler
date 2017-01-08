package com.flea.crawler;

import com.flea.config.ConfigVars;
import com.flea.crawler.model.WebPage;
import com.flea.crawler.visitor.Visitor;
import com.flea.crawler.visitor.impl.WebResourceVisitor;
import com.flea.database.graph.FleaGraphDBManager;
import com.flea.database.indexer.FleaIndexer;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.ISet;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by bilgi on 5/24/2015.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.flea"})
@Configuration
@EnableAutoConfiguration
@ImportResource("classpath:fleaCrawlerContext.xml")
public class StartCrawler{

    @Autowired
    private FleaGraphDBManager fleaGraphDBManager;

    @Autowired
    private FleaIndexer fleaIndexer;

    private static final Logger logger = LoggerFactory.getLogger(StartCrawler.class);
    private static String configPath;
    private long sessionID;
    private ExecutorService threadPool;
    private ISet<String> sharedPageQueue;
    private IList<String> activeVisitors;
    private List<Future<?>> futures = new ArrayList<>();
    private String hazelCastHost;
    private String name;

    private int hazelCastPort;
    private int threadMax = 5;
    private int visitorCount = 0;
    private String[] seeds;
    private Random randomGenerator = new Random();
    private static ApplicationContext context;

    public StartCrawler() {
        parseConfig(configPath);
        synchronized (StartCrawler.class) {
            if (threadPool == null) {
                threadPool = Executors.newFixedThreadPool(threadMax);
            }
        }
        initialize();
    }

    public static void main(String[] args) {
        if(args == null || args.length < 1){
            System.out.println("Please provide a config file");
            System.out.println("Usage: crawler /path/to/config");
            return;
        }
        ConfigVars.fleaConfigPath = configPath = args[0];
        context = SpringApplication.run(StartCrawler.class, args);
        StartCrawler startCrawler = context.getBean(StartCrawler.class);
        startCrawler.startVisitors();
        logger.info("Finished Crawling");
    }

    private void parseConfig(String filename) {
        PropertiesConfiguration configs = null;
        try {
            configs = new PropertiesConfiguration(filename);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        assert configs != null;
        configs.setReloadingStrategy(new FileChangedReloadingStrategy());
        name = configs.getString("name");
        threadMax = configs.getInt("threadMax");
        hazelCastHost = configs.getString("hazelCast.hostname");
        hazelCastPort = configs.getInt("hazelCast.port");
        seeds = configs.getStringArray("seeds");
        name=configs.getString("name");
        sessionID = configs.getLong("sessionId");
    }

    private void initialize() {
        Config cfg = new Config();
        cfg.getNetworkConfig().setPublicAddress(hazelCastHost);
        cfg.getNetworkConfig().setPort(hazelCastPort);
        cfg.setProperty("hazelcast.logging.type", "none");
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
        sharedPageQueue = instance.getSet("sharedPageQueue");
        activeVisitors = instance.getList("activeVisitors");
    }

    public void startVisitors() {
        // kick of crawlers based on initial seeds
        initializeSeeds();

        // poll the sharedPageQueue
        while (true) {
            assignAvailableVisitor();
        }
    }

    private void assignAvailableVisitor() {
        if (visitorCount < threadMax) {
            if (sharedPageQueue.size() > 0) {
                int random = randomGenerator.nextInt(sharedPageQueue.size());
                String page = (String) sharedPageQueue.toArray()[random];
                sharedPageQueue.remove(page);
                Visitor visitor = createVisitor(sessionID);
                visitor.setWebResource(new WebPage(page, sessionID));
                futures.add(threadPool.submit(visitor));
                ++visitorCount;
            }
        }
    }

    private void initializeSeeds() {
        // Check frontier db for seeds from previous session.
        Set<String> queuedUrls = fleaGraphDBManager.getQueuedUrls(sessionID);
        for(String url: queuedUrls){
            sharedPageQueue.add(url);
        }

        if (seeds != null && seeds.length > 0) {
            Collections.addAll(sharedPageQueue, seeds);
        }
    }

    private Visitor createVisitor(long sessionID) {
        WebResourceVisitor<WebPage> webResourceVisitor = new WebResourceVisitor<>(sessionID);
        webResourceVisitor.setApplicationContext(context);
        activeVisitors.add(webResourceVisitor.getUid().toString());
        return webResourceVisitor;
    }
}