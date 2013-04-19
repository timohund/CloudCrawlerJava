package cloudcrawler.domain.ioc;

import cloudcrawler.domain.crawler.robotstxt.cache.Cache;
import cloudcrawler.domain.crawler.schedule.CrawlingScheduleStrategy;
import cloudcrawler.domain.crawler.schedule.FixedAmountPerRunStrategy;
import cloudcrawler.domain.indexer.Indexer;
import cloudcrawler.system.configuration.ConfigurationManager;
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

    protected ConfigurationManager configurationManager;

    protected Configuration configuration;

    /**
     * @param configurationManager
     */
    public CloudCrawlerModule(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
        configuration = configurationManager.getConfiguration();
    }

    @Override
    public void configure(Binder binder) {
        try {
            binder.bind(CrawlingScheduleStrategy.class).to(FixedAmountPerRunStrategy.class);
            binder.bind(Cache.class).to(getRobotsTxtCacheClass());
            binder.bind(ConfigurationManager.class).toInstance(ConfigurationManager.getInstance());
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
        String className = configuration.get("robotstxt.cache.class","cloudcrawler.domain.crawler.robotstxt.cache.NullCache");
        return (Class<? extends Cache>) Class.forName(className);
    }

    /**
     * @return
     * @throws ClassNotFoundException
     */
    protected Class getIndexerClass() throws ClassNotFoundException {
        String className = configuration.get("indexer.indexer.class","cloudcrawler.domain.indexer.solr.SolrIndexer");
        return (Class<? extends Indexer>) Class.forName(className);
    }

    /**
     * @return Injector
     */
    public static Injector getConfiguredInjector() {
        CloudCrawlerModule module = new CloudCrawlerModule(ConfigurationManager.getInstance());
        return Guice.createInjector(module);
    }
}
