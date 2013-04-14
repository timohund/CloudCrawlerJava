package cloudcrawler.domain.crawler.message;


/**
 * Message class that contains a crawled document as
 * subject. This message is send be the crawling mapper to the reducer
 * to handle crawled documents.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */

import cloudcrawler.domain.crawler.pagerank.InheritedPageRank;

public class InheritPageRankMessage extends Message {

    protected InheritedPageRank attachment;

    public InheritedPageRank getAttachment() {
        return this.attachment;
    }

    public void setAttachment(InheritedPageRank attachment) {
        this.attachmentClassname = attachment.getClass().getCanonicalName();
        this.attachment = attachment;
    }
}