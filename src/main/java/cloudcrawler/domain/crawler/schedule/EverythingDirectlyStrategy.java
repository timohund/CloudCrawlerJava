package cloudcrawler.domain.crawler.schedule;

import cloudcrawler.domain.crawler.Document;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 30.03.13
 * Time: 10:04
 * To change this template use File | Settings | File Templates.
 */
public class EverythingDirectlyStrategy implements CrawlingScheduleStrategy {

    @Override
    public int getNextCrawlingState(URI linkTargetUri) {
        return Document.CRAWLING_STATE_SCHEDULED;
    }
}
