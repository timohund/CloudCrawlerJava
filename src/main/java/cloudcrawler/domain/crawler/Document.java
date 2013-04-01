package cloudcrawler.domain.crawler;


import java.net.URI;
import java.util.HashMap;

/**
 * A crawling document represent a document that
 * is crawled by the crawler.
 */
public class Document {

    protected String title;

    protected URI uri;

    protected String content;

    protected String mimeType;

    protected int crawCount = 0;

    /**
     * Crawling countdown is used to craw pages not directly.
     * It will be set and decreased and when it's 0 the page will be crawled.
     */
    protected int crawlingCountdown = 0;

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
        this.incomingLinks.put(link.getTargetUri().toString(),link);
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

    /**
     * @return int
     */
    public int getCrawlCount() {
        return this.crawCount;
    }

    public int getCrawlingCountdown() {
        return crawlingCountdown;
    }

    public void setCrawlingCountdown(int crawlingCountdown) {
        this.crawlingCountdown = crawlingCountdown;
    }

    public void decrementCrawlingCountdown() {
        this.crawlingCountdown--;
    }
}
