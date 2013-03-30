package cloudcrawler.domain.crawler.message;

import cloudcrawler.domain.crawler.Document;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 30.03.13
 * Time: 08:26
 * To change this template use File | Settings | File Templates.
 */
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
