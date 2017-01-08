package com.flea.indexer;

import com.flea.crawler.FleaCrawler;
import com.flea.crawler.controller.FleaCrawlControllerImpl;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.ParseException;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.NotAllowedContentException;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by bilgi on 7/30/2015.
 */
public class IndexerTestPut {

    FleaCrawler fleaCrawler = new FleaCrawler();
    FleaCrawlControllerImpl controller = new FleaCrawlControllerImpl();
    Parser parser;

    @Test
    public void testPutIndex() throws NotAllowedContentException, ParseException {
        parser = new Parser(getConfig());
        String path = new File(Paths.get("").toAbsolutePath().toString() +"/src/test/resources/html/").toString() +"/";
        for(int i =0;  i < 1; ++i) {
            for (String s : new File(Paths.get("").toAbsolutePath().toString() + "/src/test/resources/html/").list()) {
                String context = new File(path).toURI().toString();
                String fileName = new File(path + s).toString();

                WebURL webUrl = new WebURL();
                HtmlParseData parseData = new HtmlParseData();
                byte[] contentData = null;
                try {
                    contentData = Files.readAllBytes(Paths.get(fileName));
                    parseData.setHtml(new String(contentData, "UTF-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                webUrl.setURL("http://bilgitest.com:8080/"+s);
                Page p = new Page(webUrl);
                p.setContentData(contentData);
                parser.parse(p,"http://bilgitest.com:8080/"+s);
//                p.setParseData(parseData);
                fleaCrawler.visit(p);
            }
            try {
                Thread.sleep(100l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public CrawlConfig getConfig(){
//        int numberOfCrawlers = Integer.parseInt(args[1]);
        CrawlConfig config = new CrawlConfig();
//        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setPolitenessDelay(1000);
        config.setMaxDepthOfCrawling(2);
        config.setMaxPagesToFetch(1000);
        config.setIncludeBinaryContentInCrawling(false);
        config.setResumableCrawling(false);
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        return config;
    }
}
