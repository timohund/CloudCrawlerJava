package cloudcrawler.ioc;

import cloudcrawler.domain.crawler.schedule.CrawlingScheduleStrategy;
import cloudcrawler.domain.crawler.schedule.ForeignLinksFirstStrategy;
import com.google.inject.Binder;
import com.google.inject.Module;


/**
 * Mapping file used bei guice to map
 * interfaces to concrete classes.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */
public class CloudCrawlerModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(CrawlingScheduleStrategy.class).to(ForeignLinksFirstStrategy.class);
    }
}
