package org.cloudcrawler.domain.crawler;

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
public class DocumentMergerTest {


    @Test
    public void canMergeSameDomainDocument() throws IOException, URISyntaxException, InterruptedException {

        Document document1 = new Document();
        document1.setCrawlingState(Document.CRAWLING_STATE_CRAWLED);
        document1.setUri(new URI("http://www.admin-wissen.de/"));

        Link link1 = new Link();
        link1.setSourceUri(new URI("http://www.facebook.com/"));
        document1.addIncomingLink(link1);

        Link link2 = new Link();
        link2.setSourceUri(new URI("http://www.amazon.de"));
        document1.addIncomingLink(link2);

            //crawled existing document
        Document document2 = new Document();
        document2.incrementCrawCount();
        document2.setUri(new URI("http://www.admin-wissen.de/"));

        Link link3 = new Link();
        link3.setSourceUri(new URI("http://www.heise.de/"));
        document2.addIncomingLink(link3);
        document2.incrementLinkAnalyzeCount();


        DocumentMerger merger = new DocumentMerger();
        merger.merge("http://www.admin-wissen.de/",document1);
        merger.merge("http://www.admin-wissen.de/",document2);

        Assert.assertEquals(1, merger.getResult().size());
            //all incoming links should be merged
        Assert.assertEquals(3,merger.getResult().get("http://www.admin-wissen.de/").getIncomingLinks().size());
            //and the crawling document should still be the master
        Assert.assertTrue(merger.getResult().get("http://www.admin-wissen.de/").getCrawlCount() > 0);
    }
}
