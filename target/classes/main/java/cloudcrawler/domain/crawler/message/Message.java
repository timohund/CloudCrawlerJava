package cloudcrawler.domain.crawler.message;

/**
 * When something in the scope a a document should be
 * done the mapper should send a message with a subject
 * object to the targetUri. The reducer is responsible
 * to read and handle the messages.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */

import java.net.URI;

public class Message {

    protected URI targetUri;

    protected String attachmentClassname;

    public URI getTargetUri() {
        return targetUri;
    }

    public void setTargetUri(URI targetUri) {
        this.targetUri = targetUri;
    }

    public String getAttachmentClassname() {
        return attachmentClassname;
    }
}

