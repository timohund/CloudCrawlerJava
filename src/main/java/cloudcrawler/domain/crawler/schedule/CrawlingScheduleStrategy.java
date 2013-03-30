package cloudcrawler.domain.crawler.schedule;

import java.net.URI;

/**
 *
 */
public interface CrawlingScheduleStrategy {

    public void setCurrentPageUri(URI currentPageUri);

    public int getCrawlingCountDown(URI linkTargetUri);
}
