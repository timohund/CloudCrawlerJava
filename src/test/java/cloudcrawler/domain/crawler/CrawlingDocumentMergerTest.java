package cloudcrawler.domain.crawler;

import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 29.03.13
 * Time: 10:37
 * To change this template use File | Settings | File Templates.
 */
public class CrawlingDocumentMergerTest {


    @Test
    public void canMergeSameDomainDocument() throws IOException, URISyntaxException, InterruptedException {

        CrawlingDocument document1 = new CrawlingDocument();
        document1.setCrawlingCountdown(0);
        document1.setUri(new URI("http://www.admin-wissen.de/"));
        document1.addIncomingLink("http://www.facebook.com/");
        document1.addIncomingLink("http://www.amazon.de");

            //crawled existing document
        CrawlingDocument document2 = new CrawlingDocument();
        document2.incrementCrawCount();
        document2.setUri(new URI("http://www.admin-wissen.de/"));
        document2.addIncomingLink("http://www.heise.de/");
        document2.incrementLinkAnalyzeCount();


        CrawlingDocumentMerger merger = new CrawlingDocumentMerger();
        merger.merge("http://www.admin-wissen.de/",document1);
        merger.merge("http://www.admin-wissen.de/",document2);

        Assert.assertEquals(1, merger.getResult().size());
            //all incoming links should be merged
        Assert.assertEquals(3,merger.getResult().get("http://www.admin-wissen.de/").getIncomingLinks().size());
            //and the crawling document should still be the master
        Assert.assertTrue(merger.getResult().get("http://www.admin-wissen.de/").getCrawlCount() > 0);
    }
}
