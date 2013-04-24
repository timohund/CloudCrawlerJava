package org.cloudcrawler.domain.crawler;

import java.util.HashMap;
import java.util.Iterator;

/**
 * During the mapping process a new document is created for every found link.
 *
 * Because of this it is possible that multiple documents for the same url
 * exist after the mapping process.
 *
 * To avoid that we have multiple documents for the same url in the end
 * the reducer process is responsible to merge multiple documents for the same url.
 *
 * The following rules are applied:
 *
 * When we have a document that was allready crawled, it is used as the "master" document.
 * Only one document per url should exist in the state crawled.
 *
 * All incoming Links from every document are merged into the master document to
 * keep the incoming link information.
 *
 * Finally the merger has one master document with all incoming links, also from the slave documents.
 *
 * @author Timo Schmidt <timo.schmidt@gmx.net>
 */
public class DocumentMerger {

    protected HashMap<String,Document> result = new HashMap<String, Document>();

    public DocumentMerger() {
        this.reset();
    }

    public void reset() {
        result = new HashMap<String, Document>();
    }

    /**
     * Determines the master document (the one which was crawled)
     * and merges new created slave documents.
     *
     * @param key
     * @param document
     */
    public void merge(String key, Document document) {
        Document masterDocument = document;
        if(result.containsKey(key)) {
            Document slaveDocument;
            Document currentStoredResult = result.get(key);

            if(currentStoredResult != null && currentStoredResult.getCrawlCount() > 0) {
                masterDocument = currentStoredResult;
                slaveDocument = document;
            } else {
                slaveDocument = currentStoredResult;
            }

            HashMap<String,Link> links =  slaveDocument.getIncomingLinks();
            if(links != null) {
                Iterator iterator = links.keySet().iterator();
                while(iterator.hasNext()) {
                    String linkKey = iterator.next().toString();
                    Link linkToAdd = links.get(linkKey);
                    masterDocument.addIncomingLink(linkToAdd);
                }
            }
        }

        result.remove(key);
        result.put(key, masterDocument);
    }

    public HashMap<String, Document> getResult() {
        return result;
    }
}
