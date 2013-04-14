package cloudcrawler.domain.crawler.pagerank;

import cloudcrawler.domain.crawler.Document;
import cloudcrawler.domain.crawler.message.DocumentMessage;
import cloudcrawler.domain.crawler.message.InheritPageRankMessage;
import cloudcrawler.domain.crawler.message.Message;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 14.04.13
 * Time: 11:55
 * To change this template use File | Settings | File Templates.
 */
public class PageRankMerger {

    protected DocumentMessage targetDocumentMessage = null;

    protected double incomingPageRankSum = 0.0;

    protected int incomingPageRankCount = 0;

    public PageRankMerger() {
        this.reset();
    }

    public void reset() {
        this.targetDocumentMessage = null;
        this.incomingPageRankSum = 0.0;
        this.incomingPageRankCount = 0;
    }

    public void merge(Message message) throws DuplicateTargetDocumentException, UnexpectedMessageException {
        if(message.getAttachmentClassname().equals(Document.class.getCanonicalName())) {
            DocumentMessage docMessage = (DocumentMessage) message;
            if(this.targetDocumentMessage != null) {
                throw new DuplicateTargetDocumentException();
            }
            this.targetDocumentMessage = docMessage;
        } else if(message.getAttachmentClassname().equals(InheritedPageRank.class.getCanonicalName())){
            InheritPageRankMessage rankMessage = (InheritPageRankMessage) message;

            this.incomingPageRankCount += rankMessage.getAttachment().getRank();
            this.incomingPageRankCount++;
        } else {
            throw new UnexpectedMessageException();
        }

        if(this.targetDocumentMessage != null) {
            this.targetDocumentMessage.getAttachment().setRank(this.incomingPageRankSum);
        }
    }

    public DocumentMessage getResult() {
        return this.targetDocumentMessage;
    }
}
