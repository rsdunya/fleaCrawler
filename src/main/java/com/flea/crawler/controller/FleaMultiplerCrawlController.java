package com.flea.crawler.controller;

import com.flea.crawler.FleaCrawler;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * Created by bilgi on 5/24/2015.
 */
public class FleaMultiplerCrawlController implements FleaCrawlController {
    public void start(String[] args) throws Exception {

           /*
                 * crawlStorageFolder is a folder where intermediate crawl data is
                 * stored.
                 */
        String crawlStorageFolder = args[0];

        CrawlConfig config1 = new CrawlConfig();
        CrawlConfig config2 = new CrawlConfig();
        CrawlConfig turkishConfig = new CrawlConfig();


                /*
                 * The two crawlers should have different storage folders for their
                 * intermediate data
                 */
        config1.setCrawlStorageFolder(crawlStorageFolder + "/crawler1");
        config2.setCrawlStorageFolder(crawlStorageFolder + "/crawler2");
        turkishConfig.setCrawlStorageFolder(crawlStorageFolder + "/turkish1");


        config1.setPolitenessDelay(50);
        config2.setPolitenessDelay(50);
        turkishConfig.setPolitenessDelay(50);
        config1.setMaxPagesToFetch(-1);
        config2.setMaxPagesToFetch(-1);
        turkishConfig.setMaxPagesToFetch(-1);
        config1.setMaxDepthOfCrawling(-1);
        config2.setMaxDepthOfCrawling(-1);
        config1.setResumableCrawling(false);
        config2.setResumableCrawling(true);
        turkishConfig.setResumableCrawling(true);
        turkishConfig.setMaxDepthOfCrawling(-1);
        config1.setUserAgentString("Googlebot/2.1 (+http://www.google.com/bot.html)");
        config2.setUserAgentString("Googlebot/2.1 (+http://www.google.com/bot.html)");
        turkishConfig.setUserAgentString("Googlebot/2.1 (+http://www.google.com/bot.html)");

                /*
                 * We will use different PageFetchers for the two crawlers.
                 */
        PageFetcher pageFetcher1 = new PageFetcher(config1);
        PageFetcher pageFetcher2 = new PageFetcher(config2);
        PageFetcher turkishPageFetcher = new PageFetcher(turkishConfig);
                /*
                 * We will use the same RobotstxtServer for both of the crawlers.
                 */
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher1);

        CrawlController controller1 = new CrawlController(config1, pageFetcher1, robotstxtServer);
        CrawlController controller2 = new CrawlController(config2, pageFetcher2, robotstxtServer);
        CrawlController turkishCrawlController = new CrawlController(turkishConfig, turkishPageFetcher, robotstxtServer);
        String[] crawler1Domains = new String[]{"http://youtube.com"};
        controller1.addSeed("http://www.youtube.com/");
        
        // TODO seed Chinese, Indian, Turkish, Arabic, Spanish, German
        controller1.setCustomData(crawler1Domains);
                /*
                 * The first crawler will have 5 cuncurrent threads and the second
                 * crawler will have 7 threads.
                 */
        controller1.startNonBlocking(FleaCrawler.class, 5);
        System.out.println("Crawler 1 is finished.");
    }
}
