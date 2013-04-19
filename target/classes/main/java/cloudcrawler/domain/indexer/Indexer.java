package cloudcrawler.domain.indexer;

import cloudcrawler.domain.crawler.Document;

public interface Indexer {

    public void flush() throws Exception;

    public void index(Document document) throws Exception;

    public void commit() throws Exception;
}
