package cloudcrawler.domain.crawler;


import java.net.URI;
import java.util.HashMap;

/**
 * A crawling document represent a document that
 * is crawled by the crawler.
 */
public class CrawlingDocument {

    protected URI uri;

    protected String content;

    protected String mimeType;

    protected int crawCount = 0;

    protected int linkAnalyzeCount = 0;

    protected HashMap<String,String> incomingLinks = new HashMap<String, String>();

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

    public HashMap<String, String> getIncomingLinks() {
        return incomingLinks;
    }

    public void addIncomingLink(String url) {
        this.incomingLinks.put(url,url);
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
}
