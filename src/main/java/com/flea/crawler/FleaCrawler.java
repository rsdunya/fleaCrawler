package com.flea.crawler;

import com.flea.database.graph.FleaGraphDBManager;
import com.flea.database.indexer.FleaIndexer;
import com.flea.language.FleaLanguageDetector;
import com.flea.models.SearchResult;
import com.flea.search.searcher.FleaSearcher;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by bilgi on 5/21/2015.
 */
public class FleaCrawler extends WebCrawler {

    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png)$");

    /**
     * You should implement this function to specify whether the given url
     * should be crawled or not (based on your crawling logic).
     */
    FleaIndexer indexer;
    FleaSearcher searcher;
    FleaLanguageDetector languageDetector;
    FleaGraphDBManager graphDBManager;

    {
        ApplicationContext context =
                new ClassPathXmlApplicationContext("fleaCrawlerContext.xml");
        languageDetector = (FleaLanguageDetector) context.getBean("fleaLanguageDetector");
        indexer = (FleaIndexer) context.getBean("fleaIndexer");
        searcher = (FleaSearcher) context.getBean("fleaSearcher");
        graphDBManager = (FleaGraphDBManager) context.getBean("fleaGraphDBManager");
    }
//    {
//        languageDetector = (FleaLanguageDetector)FleaApplicationContext.context.getBean("fleaLanguageDetector");
//        indexer = (FleaIndexer)FleaApplicationContext.context.getBean("fleaIndexer");
//        searcher = (FleaSearcher)FleaApplicationContext.context.getBean("fleaSearcher");
//        graphDBManager = (FleaGraphDBManager)FleaApplicationContext.context.getBean("fleaGraphDBManager");
//    }

    long updateInterval = 604800000; // 7 days

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        // TODO we'll need to also scan robots.txt for page update frequency.

        return true;
    }

    /**
     * This function is called when a page is fetched and ready to be processed
     * by your program.
     */
    @Override
    public void visit(Page page) {
        Header[] responseHeaders = page.getFetchResponseHeaders();
        if (responseHeaders != null) {
            logger.debug("Response headers:");
            for (Header header : responseHeaders) {
                logger.debug("\t{}: {}", header.getName(), header.getValue());
            }
        }

        indexDocument(page);
        logger.debug("=============");
    }

    private Map<String, Object> indexDocument(Page page) {
        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        String domain = page.getWebURL().getDomain();
        String path = page.getWebURL().getPath();
        String subDomain = page.getWebURL().getSubDomain();
        String parentUrl = page.getWebURL().getParentUrl();
        String anchor = page.getWebURL().getAnchor();
        String id = DigestUtils.md5Hex(url);

        logger.debug("Docid: {}", docid);
        logger.info("URL: {}", url);
        logger.debug("Domain: '{}'", domain);
        logger.debug("Sub-domain: '{}'", subDomain);
        logger.debug("Path: '{}'", path);
        logger.debug("Parent page: {}", parentUrl);
        logger.debug("Anchor text: {}", anchor);

        Map<String, Object> doc = new HashMap<String, Object>();
        doc.put("_id", id);
        doc.put("url", url);
        doc.put("url_not_analyzed", url);
        doc.put("domain", domain);
        doc.put("path", path);
        doc.put("subDomain", subDomain);
        doc.put("parentUrl", parentUrl);
        doc.put("anchor", anchor);
        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            logger.debug("Text length: {}", text.length());
            logger.debug("Html length: {}", html.length());
            logger.debug("Number of outgoing links: {}", links.size());

            // TODO change for testing
            if (languageDetector != null) {
                doc.put("language", languageDetector.detectLanguage(htmlParseData.getText()));
            } else {
                doc.put("language", "en");
            }

            doc.put("body", htmlParseData.getText());
            doc.put("links", htmlParseData.getOutgoingUrls());
            doc.put("title", buildTitle(htmlParseData));
        }

        SearchResult searchResult = searcher.searchExactMatch(page.getWebURL().getURL(), "url_not_analyzed", null, 1);
        if (searchResult != null && searchResult.getUrl() != null && url != null && page.getWebURL().getURL() != null &&
                searchResult.getUrl().equalsIgnoreCase(page.getWebURL().getURL())) {
            doc.put("id", searchResult.getId());
            if (searchResult.getLastUpdated() != null) {
                if ((System.currentTimeMillis() - updateInterval) < searchResult.getLastUpdated().getTime()) {
                    doc.put("updateExistingPage", true);
                }
            }
        }

        doc.put("rank", 0.0d);
        doc.put("lastUpdated", new Date());
        doc.put("indexDocId", indexer.putIndex(doc));
        graphDBManager.createOrUpdatePage(doc, null);
        return doc;
    }

    private String buildTitle(HtmlParseData htmlParseData) {
        StringBuilder title = new StringBuilder(htmlParseData.getTitle().trim());
        // TODO smart analyze desc maybe to include in search link
//        if(htmlParseData.getMetaTags() != null && htmlParseData.getMetaTags().size()>0 && htmlParseData.getMetaTags().containsKey("description")){
//            title.append(" - " + (String)htmlParseData.getMetaTags().get("description"));
//        }

        return title.toString();
    }
}

