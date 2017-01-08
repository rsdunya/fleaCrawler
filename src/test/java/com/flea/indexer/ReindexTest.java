package com.flea.indexer;

import com.flea.database.indexer.FleaIndexer;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bilgi on 7/31/2015.
 */
public class ReindexTest {

    FleaIndexer fleaIndexer;

    {
        ApplicationContext context =
                new ClassPathXmlApplicationContext("fleaCrawlerContext.xml");
        fleaIndexer= (FleaIndexer) context.getBean("fleaIndexer");
    }

    @Test
    public void reindex(){
        DigestUtils.md5Hex("google.com");
        Map<String, Object> a = new HashMap<String, Object>();
        a.put("a", "a");
        a.put("b", "b");
        a.put("c", "c");
        a.put("d", "d");
        String id = fleaIndexer.putIndex(a);
        a = new HashMap<String, Object>();
        a.put("_id", id);
        a.put("a", "d");
        a.put("b", "e");
        a.put("c", "f");
        a.put("d", "g");
        fleaIndexer.putIndex(a);
    }
}
