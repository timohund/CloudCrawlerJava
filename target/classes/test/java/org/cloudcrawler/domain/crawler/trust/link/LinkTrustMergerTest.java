package org.cloudcrawler.domain.crawler.trust.link;

import org.cloudcrawler.domain.crawler.Document;
import org.cloudcrawler.domain.crawler.message.DocumentMessage;
import org.cloudcrawler.domain.crawler.message.InheritLinkTrustMessage;
import junit.framework.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public class LinkTrustMergerTest {

    @Test
    public void canMergeWhenTrustsMergedBeforeDocument() throws DuplicateTargetDocumentException, UnexpectedMessageException, URISyntaxException {
        InheritLinkTrustMessage message1    = new InheritLinkTrustMessage();
        InheritedLinkTrust trust1 = new InheritedLinkTrust();
        trust1.setLinkTrust(2.11);
        message1.setAttachment(trust1);

        InheritLinkTrustMessage message2    = new InheritLinkTrustMessage();
        InheritedLinkTrust trust2 = new InheritedLinkTrust();
        trust2.setLinkTrust(2.22);
        message2.setAttachment(trust2);

        Document foo                = new Document();
        foo.setUri(new URI("http://www.google.de/"));
        DocumentMessage message3    = new DocumentMessage();
        message3.setAttachment(foo);

        LinkTrustMerger linkTrustMerger     = new LinkTrustMerger();
        linkTrustMerger.merge(message1);
        linkTrustMerger.merge(message2);
        linkTrustMerger.merge(message3);

        Assert.assertEquals(linkTrustMerger.getResult().getAttachment().getLinkTrust(),4.33);
    }
}
