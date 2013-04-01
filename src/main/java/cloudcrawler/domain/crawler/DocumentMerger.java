package cloudcrawler.domain.crawler;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 29.03.13
 * Time: 12:45
 * To change this template use File | Settings | File Templates.
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
                    // weiterverabeitung der Werte...
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
