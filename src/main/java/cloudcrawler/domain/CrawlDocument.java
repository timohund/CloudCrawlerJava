package cloudcrawler.domain;


import java.util.Map;

public class CrawlDocument {

    protected String url;

    protected String content;

    protected String mimeType;

    protected Map<String,String> incomingLinks;

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

    public Map<String, String> getIncomingLinks() {
        return incomingLinks;
    }

    public void setIncomingLinks(Map<String, String> incomingLinks) {
        this.incomingLinks = incomingLinks;
    }

    public void addIncomingLink(String url) {
        this.incomingLinks.put(url,url);
    }
}
