package cloudcrawler.domain.crawler.message;


/**
 * Message class that contains a crawled document as
 * subject. This message is send be the crawling mapper to the reducer
 * to handle crawled documents.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */

import cloudcrawler.domain.crawler.trust.link.InheritedLinkTrust;

public class InheritLinkTrustMessage extends Message {

    protected InheritedLinkTrust attachment;

    public InheritedLinkTrust getAttachment() {
        return this.attachment;
    }

    public void setAttachment(InheritedLinkTrust attachment) {
        this.attachmentClassname = attachment.getClass().getCanonicalName();
        this.attachment = attachment;
    }
}