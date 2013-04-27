package org.cloudcrawler.domain.ioc;

import org.cloudcrawler.domain.crawler.robotstxt.cache.Cache;
import org.cloudcrawler.domain.crawler.schedule.CrawlingScheduleStrategy;
import org.cloudcrawler.domain.crawler.schedule.FixedAmountPerRunStrategy;
import org.cloudcrawler.domain.indexer.Indexer;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.hadoop.conf.Configuration;


/**
 * Mapping file used bei guice to map
 * interfaces to concrete classes.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */
public class CloudCrawlerModule implements Module {


    protected Configuration configuration;

    /**
     * @param configuration
     */
    public CloudCrawlerModule(Configuration configuration) {
        this.configuration = configuration;

    }

    @Override
    public void configure(Binder binder) {
        try {
            binder.bind(CrawlingScheduleStrategy.class).to(FixedAmountPerRunStrategy.class);
            binder.bind(Cache.class).to(getRobotsTxtCacheClass());
            binder.bind(Indexer.class).to(getIndexerClass());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return
     * @throws ClassNotFoundException
     */
    protected Class getRobotsTxtCacheClass() throws ClassNotFoundException {
        String className = configuration.get("robotstxt.cache.class","org.cloudcrawler.domain.crawler.robotstxt.cache.NullCache");
        return (Class<? extends Cache>) Class.forName(className);
    }

    /**
     * @return
     * @throws ClassNotFoundException
     */
    protected Class getIndexerClass() throws ClassNotFoundException {
        String className = configuration.get("indexer.index.class","org.cloudcrawler.domain.indexer.solr.SolrIndexer");
        return (Class<? extends Indexer>) Class.forName(className);
    }

    /**
     * @return Injector
     */
    public static Injector getConfiguredInjector(Configuration configuration) {
        CloudCrawlerModule module = new CloudCrawlerModule(configuration);
        return Guice.createInjector(module);
    }
}
