package cloudcrawler.domain.crawler.schedule;

import java.net.URI;

/**
 *
 */
public interface CrawlingScheduleStrategy {

    public int getNextCrawlingState(URI linkTargetUri);
}
