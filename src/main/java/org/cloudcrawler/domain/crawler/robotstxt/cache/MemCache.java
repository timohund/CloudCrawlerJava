package org.cloudcrawler.domain.crawler.robotstxt.cache;

/**
 * The memcache Cache implementation
 *
 */

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;

@Singleton
public class MemCache implements Cache {

    private static final String NAMESPACE= "robotstxt";

    MemcachedClient memCacheClient;

    /**
     * @throws IOException
     */
    @Inject
    public MemCache(Configuration configuration) throws IOException {
        String host     = configuration.get("cache.memcache.hostname", "127.0.0.1");
        String port     = configuration.get("cache.memcache.port","11211");
        String address  = host+":"+port;

        memCacheClient  = new MemcachedClient(
            new BinaryConnectionFactory(),
            AddrUtil.getAddresses(address)
       );
    }

    @Override
    public void set(String key, int ttl, final Object o) {
        memCacheClient.set(NAMESPACE + key, ttl, o);
    }

    @Override
    public Object get(String key) {
        Object o = memCacheClient.get(NAMESPACE + key);
        if(o == null) {
            System.out.println("Cache MISS for KEY: " + key);
        } else {
            System.out.println("Cache HIT for KEY: " + key);
        }
        return o;
    }

    @Override
    public Object delete(String key) {
        return memCacheClient.delete(NAMESPACE + key);
    }

}
