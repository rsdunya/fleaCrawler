package com.flea.crawler.visitor;

/**
 * Created by bilgi on 8/9/15.
 */
public interface Visitable {
    public boolean accept(Visitor v);
}
