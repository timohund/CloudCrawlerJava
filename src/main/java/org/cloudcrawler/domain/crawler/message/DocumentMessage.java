package org.cloudcrawler.domain.crawler.message;

/**
 * Message class that contains a crawled document as
 * subject. This message is send be the crawling mapper to the reducer
 * to handle crawled documents.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */

import org.cloudcrawler.domain.crawler.Document;

public class DocumentMessage extends Message {

    protected Document attachment;

    public Document getAttachment() {
        return this.attachment;
    }


    public void setAttachment(Document attachment) {
        this.attachmentClassname = attachment.getClass().getCanonicalName();
        this.attachment = attachment;
    }
}
