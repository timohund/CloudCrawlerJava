package cloudcrawler.domain;


import java.util.HashMap;

public class CrawlDocument {

    protected String url;

    protected String content;

    protected String mimeType;

    protected int crawCount = 0;

    protected int linkAnalyzeCount = 0;

    protected HashMap<String,String> incomingLinks = new HashMap<String, String>();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public void setIncomingLinks(HashMap<String, String> incomingLinks) {
        this.incomingLinks = incomingLinks;
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
