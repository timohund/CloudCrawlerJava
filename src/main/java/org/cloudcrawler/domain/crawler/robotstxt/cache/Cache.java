package org.cloudcrawler.domain.crawler.robotstxt.cache;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 01.04.13
 * Time: 17:16
 * To change this template use File | Settings | File Templates.
 */
public interface Cache {
    void set(String key, int ttl, Object o);

    Object get(String key);

    Object delete(String key);
}
