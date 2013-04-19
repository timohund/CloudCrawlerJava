package cloudcrawler.domain.crawler.robotstxt.cache;

/**
 * Backend that does no caching at all.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */
public class NullCache implements Cache {

    @Override
    public void set(String key, int ttl, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object get(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object delete(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
