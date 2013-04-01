package cloudcrawler.domain.crawler.robotstxt.cache;

/**
 * The memcache Cache implementation
 */

import com.google.inject.Singleton;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;

import java.io.IOException;

@Singleton
public class MemCache implements Cache {

    private static final String NAMESPACE= "robotstxt";

    MemcachedClient memCacheClient;

    public MemCache() throws IOException {
        memCacheClient = new MemcachedClient(
            new BinaryConnectionFactory(),
            AddrUtil.getAddresses("127.0.0.1:11211")
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
