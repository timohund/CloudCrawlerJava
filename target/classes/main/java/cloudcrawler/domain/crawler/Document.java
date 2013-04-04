package cloudcrawler.domain.crawler;


import java.net.URI;
import java.util.HashMap;

/**
 * A crawling document represent a document that
 * is crawled by the crawler.
 */
public class Document {

    public static final int CRAWLING_STATE_WAITING      = 0;
    public static final int CRAWLING_STATE_SCHEDULED    = 1;
    public static final int CRAWLING_STATE_CRAWLED      = 2;

    protected String title;

    protected URI uri;

    protected String content;

    protected String mimeType;

    protected int crawCount = 0;

    protected int errorCount = 0;

    protected String errorMessage;

    protected int crawlingState = CRAWLING_STATE_WAITING;

    protected int linkAnalyzeCount = 0;

    protected HashMap<String,Link> incomingLinks = new HashMap<String, Link>();

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public HashMap<String, Link> getIncomingLinks() {
        return incomingLinks;
    }

    public void addIncomingLink(Link link) {
        this.incomingLinks.put(link.getSourceUri().toString(),link);
    }

    public void incrementLinkAnalyzeCount() {
        this.linkAnalyzeCount++;
    }

    public int getLinkAnalyzeCount() {
        return this.linkAnalyzeCount;
    }

    public void incrementCrawCount() {
        this.crawCount++;
    }

    public int getCrawlCount() {
        return this.crawCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void incrementErrorCount() {
        this.errorCount++;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setCrawlingState(int crawlingState) {
        this.crawlingState = crawlingState;
    }

    public int getCrawlingState() {
        return crawlingState;
    }
}
