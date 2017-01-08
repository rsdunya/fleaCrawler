package com.flea.crawler;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by bilgi on 5/24/2015.
 */
public class FleaApplicationContext implements ApplicationContextAware {
    public static ApplicationContext context;

    public static ApplicationContext getContext() {
        return context;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext == null) {
            this.context = new ClassPathXmlApplicationContext("fleaCrawlerContext.xml");
        } else {
            this.context = applicationContext;
        }
    }
}
