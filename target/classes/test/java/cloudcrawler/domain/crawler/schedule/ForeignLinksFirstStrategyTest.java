package cloudcrawler.domain.crawler.schedule;


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
public class ForeignLinksFirstStrategyTest {


    ForeignLinksFirstStrategy strategy;

    @Before
    public void setUp() {
        strategy = new ForeignLinksFirstStrategy();
    }

    @Test
    public void canScheduleMultipleTargetsOnSameDomainLater() throws URISyntaxException {
        strategy.setCurrentPageUri(new URI("http://www.test.de/"));

            //first different domain should be scheduled in the next slot
        int res1 = strategy.getCrawlingCountDown(new URI("http://www.heise.de/test1.html"));
        Assert.assertEquals(0,res1);

            //same for the next 4 links on the same second level domain
        int res2 = strategy.getCrawlingCountDown(new URI("http://www.heise.de/test2.html"));
        Assert.assertEquals(0,res2);
        int res3 = strategy.getCrawlingCountDown(new URI("http://www.heise.de/test3.html"));
        Assert.assertEquals(0,res3);
        int res4 = strategy.getCrawlingCountDown(new URI("http://www.heise.de/test4.html"));
        Assert.assertEquals(0,res4);
        int res5 = strategy.getCrawlingCountDown(new URI("http://www.heise.de/test5.html"));
        Assert.assertEquals(0,res5);

            //the next page on the same second level domain should be scheduled for a later slot
        int res6 = strategy.getCrawlingCountDown(new URI("http://www.heise.de/test6.html"));
        Assert.assertEquals(1,res6);

            //but a page on another second level domain should still be in the next slot
        int res7 = strategy.getCrawlingCountDown(new URI("http://www.amazon.de/test.html"));
        Assert.assertEquals(0,res7);

            //a subpage of the current page uid should be scheduled later (in a least 10 slots)
        int res8 = strategy.getCrawlingCountDown(new URI("http://www.test.de/foo.html"));
        Assert.assertTrue(res8 > 10);
    }

}
