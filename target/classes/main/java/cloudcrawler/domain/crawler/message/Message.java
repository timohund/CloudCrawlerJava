package cloudcrawler.domain.crawler.message;

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

