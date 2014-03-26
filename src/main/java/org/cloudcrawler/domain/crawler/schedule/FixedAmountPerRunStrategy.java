package org.cloudcrawler.domain.crawler.schedule;

import org.cloudcrawler.domain.crawler.Document;
import org.cloudcrawler.system.uri.URIHelper;

import java.net.URI;
import java.util.HashMap;

/**
 *
 * This strategy schedules a fixed amount of pages per second level domain
 * for one run
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net></timo-schmidt@gmx.net>
 */
public class FixedAmountPerRunStrategy implements CrawlingScheduleStrategy{

    protected HashMap<String,Integer> scheduleDomainItemCounts = new HashMap<String, Integer>();

    protected int pagesPerRun = 2;

    public int getPagesPerRun() {
        return pagesPerRun;
    }

    public void setPagesPerRun(int pagesPerRun) {
        this.pagesPerRun = pagesPerRun;
    }

    public int getNextCrawlingState(URI linkTargetUri) {
        int storedValue = 1;
        String linkSecondLevelHost = URIHelper.getSecondLevelHostName(linkTargetUri);

        if(scheduleDomainItemCounts.containsKey(linkSecondLevelHost)) {
            storedValue = scheduleDomainItemCounts.get(linkSecondLevelHost);
            storedValue++;
        }

        scheduleDomainItemCounts.put(linkSecondLevelHost,storedValue);

        if(storedValue <= pagesPerRun) {
            return Document.CRAWLING_STATE_SCHEDULED;
        } else {
            return Document.CRAWLING_STATE_WAITING;
        }
    }
}
