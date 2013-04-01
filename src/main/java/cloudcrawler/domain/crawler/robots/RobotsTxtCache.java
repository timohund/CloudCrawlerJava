package cloudcrawler.domain.crawler.robots;

import com.google.inject.Singleton;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;

@Singleton
public class RobotsTxtCache {

    private static final String NAMESPACE= "robotstxt";

    private static MemcachedClient[] m = null;

    public RobotsTxtCache() {
        try {
            m= new MemcachedClient[21];
            for (int i = 0; i <= 20; i ++) {
                MemcachedClient c =  new MemcachedClient(
                        new BinaryConnectionFactory(),
                        AddrUtil.getAddresses("127.0.0.1:11211"));
                m[i] = c;
            }
        } catch (Exception e) {

        }
    }

    public void set(String key, int ttl, final Object o) {
        getCache().set(NAMESPACE + key, ttl, o);
    }

    public Object get(String key) {
        Object o = getCache().get(NAMESPACE + key);
        if(o == null) {
            System.out.println("Cache MISS for KEY: " + key);
        } else {
            System.out.println("Cache HIT for KEY: " + key);
        }
        return o;
    }

    public Object delete(String key) {
        return getCache().delete(NAMESPACE + key);
    }

    public MemcachedClient getCache() {
        MemcachedClient c= null;
        try {
            int i = (int) (Math.random()* 20);
            c = m[i];
        } catch(Exception e) {

        }
        return c;
    }
}
