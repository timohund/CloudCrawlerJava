package org.cloudcrawler.domain.crawler.trust.link;

import org.cloudcrawler.domain.crawler.Document;
import org.cloudcrawler.domain.crawler.message.DocumentMessage;
import org.cloudcrawler.domain.crawler.message.InheritLinkTrustMessage;
import org.cloudcrawler.domain.crawler.message.Message;

/**
 * The link trust merger is responsible to merge all incoming trust messages
 * for a single link.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */
public class LinkTrustMerger {

    /**
     * Holds the target document after merging.
     */
    protected DocumentMessage targetDocumentMessage = null;

    protected double incomingLinkTrust = 0.0;

    protected int incomingLinkTrustCount = 0;

    public LinkTrustMerger() {
        this.reset();
    }

    public void reset() {
        this.targetDocumentMessage = null;
        this.incomingLinkTrust = 0.0;
        this.incomingLinkTrustCount = 0;
    }

    public void merge(Message message) throws DuplicateTargetDocumentException, UnexpectedMessageException {
        if(message.getAttachmentClassname().equals(Document.class.getCanonicalName())) {
            DocumentMessage docMessage = (DocumentMessage) message;
            if(this.targetDocumentMessage != null) {
                throw new DuplicateTargetDocumentException();
            }
            this.targetDocumentMessage = docMessage;
        } else if(message.getAttachmentClassname().equals(InheritedLinkTrust.class.getCanonicalName())){
            InheritLinkTrustMessage rankMessage = (InheritLinkTrustMessage) message;

            this.incomingLinkTrust += rankMessage.getAttachment().getLinkTrust();
            this.incomingLinkTrustCount++;
        } else {
            throw new UnexpectedMessageException();
        }

        if(this.targetDocumentMessage != null) {
            this.targetDocumentMessage.getAttachment().setLinkTrust(this.incomingLinkTrust);
        }
    }

    public DocumentMessage getResult() {
        return this.targetDocumentMessage;
    }
}
