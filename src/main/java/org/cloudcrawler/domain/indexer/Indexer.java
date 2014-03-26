package org.cloudcrawler.domain.indexer;

import org.cloudcrawler.domain.crawler.Document;

public interface Indexer {

    public void prepare();

    public void flush() throws Exception;

    public void index(Document document) throws Exception;

    public void commit() throws Exception;
}
