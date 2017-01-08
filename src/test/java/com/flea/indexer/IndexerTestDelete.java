package com.flea.indexer;

import com.flea.database.indexer.FleaIndexer;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by bilgi on 7/31/2015.
 */
public class IndexerTestDelete {

    FleaIndexer fleaIndexer;

    {
        ApplicationContext context =
                new ClassPathXmlApplicationContext("fleaCrawlerContext.xml");
        fleaIndexer= (FleaIndexer) context.getBean("fleaIndexer");
    }

    @Test
    public void deleteDoc(){
        fleaIndexer.delete("AU7g2KzA1BtWXq1mVAgC");
    }
}
