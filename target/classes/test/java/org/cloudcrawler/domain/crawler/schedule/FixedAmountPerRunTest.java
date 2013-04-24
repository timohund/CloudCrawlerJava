package org.cloudcrawler.domain.crawler.schedule;


import org.cloudcrawler.domain.crawler.Document;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * The foreign links first strategy should schedule
 * links that point to different second level domains
 * before links that point to the same second level domain.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */
public class FixedAmountPerRunTest {


    FixedAmountPerRunStrategy strategy;

    @Before
    public void setUp() {
        strategy = new FixedAmountPerRunStrategy();
    }

    @Test
    public void canScheduleMultipleTargetsOnSameDomainLater() throws URISyntaxException {
        strategy.setPagesPerRun(3);
            //first different domain should be scheduled in the next slot
        int res1 = strategy.getNextCrawlingState(new URI("http://www.heise.de/test1.html"));
        Assert.assertEquals(Document.CRAWLING_STATE_SCHEDULED,res1);
            //same for the next 4 links on the same second level domain
        int res2 = strategy.getNextCrawlingState(new URI("http://www.heise.de/test2.html"));
        Assert.assertEquals(Document.CRAWLING_STATE_SCHEDULED,res2);
        int res3 = strategy.getNextCrawlingState(new URI("http://www.heise.de/test3.html"));
        Assert.assertEquals(Document.CRAWLING_STATE_SCHEDULED,res3);

            //the next page on the same second level domain should be scheduled for a later slot
        int res6 = strategy.getNextCrawlingState(new URI("http://www.heise.de/test6.html"));
        Assert.assertEquals(Document.CRAWLING_STATE_WAITING,res6);

            //but a page on another second level domain should still be in the next slot
        int res7 = strategy.getNextCrawlingState(new URI("http://www.amazon.de/test.html"));
        Assert.assertEquals(Document.CRAWLING_STATE_SCHEDULED,res7);
    }

}
