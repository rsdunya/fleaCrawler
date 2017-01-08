//package com.flea.database.indexer;
//
//import com.flea.crawler.FleaCrawler;
//import com.flea.database.graph.FleaOrientDBManager;
//import com.flea.search.searcher.ElasticSearcher;
//import com.tinkerpop.blueprints.CloseableIterable;
//import com.tinkerpop.blueprints.Vertex;
//import edu.uci.ics.crawler4j.crawler.Page;
//import edu.uci.ics.crawler4j.parser.HtmlParseData;
//import edu.uci.ics.crawler4j.url.WebURL;
//import org.junit.Test;
//
//import java.util.*;
//
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
///**
// * Created by bilgi on 5/30/2015.
// */
//public class IndexerTest {
//
//    ElasticIndexer indexer = new ElasticIndexer();
//    ElasticSearcher searcher = new ElasticSearcher();
//    FleaOrientDBManager manager = new FleaOrientDBManager();
//
//    FleaCrawler crawler = new FleaCrawler();
//    long updateInterval = 604800000; // 7 days
//
//    // should test the affects of duplicate docs on orientdb
//    // Store initial doc in elastic and orientdb
//    // Then create the duplicate in elastic, then update it in orientdb
//    // If the doc already existed, orient should keep the stored score(rank)
//    // else default all scores to 0
//    // result, duplicates are okay in elastic, orientdb should only contain the latest revision.
//    @Test
//    public void putDuplicateDocTest(){
//        generateTestPage(null);
//        generateTestPage(0.875);
//
//    }
//
//    private Map<String, Object> generateTestPage(Double score){
//        Page page = new Page(generateUrl());
//
//        // Here is an imaginary assumption that the ranker has run and changed the score from 0 to score.
//        if(score !=null){
//            Iterable<Vertex> vertices = manager.getVertexByField("url", page.getWebURL().getURL());
//            assertTrue(vertices.iterator().hasNext());
//            for (Vertex vertice : vertices) {
//                vertice.setProperty("score", score);
//            }
//        }
//
//        HtmlParseData parseData = new HtmlParseData();
//        parseData.setHtml("<html><body>TEST</body></html>");
//        page.setParseData(parseData);
//        crawler.visit(page);
//        CloseableIterable iterable = manager.getVertexByField("url", page.getWebURL().getURL());
//        assert iterable.iterator().hasNext() ==true;
//
//        return null;
//    }
//
//    public Set<WebURL> generateOutgoingUrls(){
//        WebURL url = new WebURL();
//        url.setPath("outgoingpath");
//        url.setURL("outgoingurl");
//        url.setAnchor("outgoinganchor");
//        url.setDocid(2);
//        url.setParentDocid(1);
//        url.setParentUrl("outgoingparenturl");
//        url.setTag("outgoingtag");
//        Set<WebURL> urls=new LinkedHashSet<>();
//        urls.add(url);
//        return urls;
//    }
//
//    public WebURL generateUrl(){
//        WebURL url = new WebURL();
//        url.setPath("path");
//        url.setURL("url");
//        url.setAnchor("anchor");
//        url.setDocid(1);
////        url.setParentDocid(1);
//        url.setParentUrl("parenturl");
//        url.setTag("tag");
//        return url;
//    }
//}
