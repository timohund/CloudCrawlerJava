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
public class CrawlingDocumentMerger {

    protected HashMap<String,CrawlingDocument> result = new HashMap<String, CrawlingDocument>();

    public CrawlingDocumentMerger() {
        this.reset();
    }

    public void reset() {
        result = new HashMap<String, CrawlingDocument>();
    }

    /**
     *
     * @param key
     * @param crawlingDocument
     */
    public void merge(String key, CrawlingDocument crawlingDocument) {
        CrawlingDocument masterDocument = crawlingDocument;
        if(result.containsKey(key)) {
            CrawlingDocument slaveDocument;
            CrawlingDocument currentStoredResult = result.get(key);

            if(currentStoredResult != null && currentStoredResult.getCrawlCount() > 0) {
                masterDocument = currentStoredResult;
                slaveDocument = crawlingDocument;
            } else {
                slaveDocument = currentStoredResult;
            }

            HashMap<String,String> links =  slaveDocument.getIncomingLinks();

            if(links != null) {
                Iterator iterator = links.keySet().iterator();
                while(iterator.hasNext()) {
                    String linkKey = iterator.next().toString();
                    String linkToAdd = links.get(linkKey).toString();
                    // weiterverabeitung der Werte...
                    masterDocument.addIncomingLink(linkToAdd);
                }
            }
        }

        result.remove(key);
        result.put(key, masterDocument);
    }

    public HashMap<String, CrawlingDocument> getResult() {
        return result;
    }
}
