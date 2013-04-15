package cloudcrawler.mapreduce.crawler;

import cloudcrawler.domain.crawler.Document;
import cloudcrawler.domain.crawler.Service;
import cloudcrawler.domain.crawler.message.DocumentMessage;
import cloudcrawler.domain.crawler.message.MessagePersistenceManager;
import cloudcrawler.ioc.CloudCrawlerModule;
import cloudcrawler.mapreduce.AbstractMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.net.URI;
import java.util.Vector;

public class CrawlingMapper extends AbstractMapper {

    protected Service crawlingService;

    protected Injector injector;

    public CrawlingMapper() {
            //since the Crawling mapper is instanciated in hadoop
            //we inject the dependecies by our own
        injector = Guice.createInjector(new CloudCrawlerModule());
        this.setMessageManager(injector.getInstance(MessagePersistenceManager.class));
        this.setCrawlingService(injector.getInstance(Service.class));
    }

    public void setCrawlingService(Service crawlingService) {
        this.crawlingService = crawlingService;
    }

    /**
     * @param messageManager
     * @param service
     */
    public CrawlingMapper(MessagePersistenceManager messageManager, Service service) {
        this.setMessageManager(messageManager);
        this.setCrawlingService(service);
    }

    /**
     *
     * @param message
     * @param key
     * @param context
     * @param e
     * @throws IOException
     * @throws InterruptedException
     */
    protected void handleDocumentException(DocumentMessage message, Text key, Context context, Exception e) throws IOException, InterruptedException {
        message.getAttachment().incrementErrorCount();
        message.getAttachment().setErrorMessage(e.getMessage());
        String json                 = messageManager.sleep(message);
        Text crawlingResultValue    = new Text(json.toString());
        context.write(key, crawlingResultValue);
    }

    /**
     *
     * @param key
     * @param value
     * @param context
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        try {
               //when the key is empty we skip the row
            if(key.toString().trim() == "") {   return;  }
                //create or retrieve the crawling document from json
            DocumentMessage currentDocumentCrawlMessage = new DocumentMessage();
            Document crawled = new Document();
            currentDocumentCrawlMessage.setAttachment(crawled);

                //reconstitute the object or assign the uri
            if (value.toString().trim().equals("") ) {
                    //new document from the input file without json are allways scheduled directly
                crawled.setCrawlingState(Document.CRAWLING_STATE_SCHEDULED);
            } else {
                currentDocumentCrawlMessage = (DocumentMessage) messageManager.wakeup(value.toString());
                crawled = currentDocumentCrawlMessage.getAttachment();
            }

            try {
                URI uri = new URI(key.toString());
                crawled.setUri(uri);

                if(crawled == null) {
                    return;
                }

                if(crawled.getCrawlCount() > 0 && crawled.getErrorCount() >= 2) {
                    //repost the document
                    postMessage(key, currentDocumentCrawlMessage, context);
                    return;
                }

                if(crawled.getCrawlingState() != Document.CRAWLING_STATE_SCHEDULED) {
                    //repost the document
                    postMessage(key, currentDocumentCrawlMessage, context);
                    return;
                }

                Vector<Document> crawlingResults = crawlingService.crawlAndFollowLinks(crawled);

                for(Document crawlingResult : crawlingResults) {
                    DocumentMessage linkTargetCrawlingMessage = new DocumentMessage();
                    linkTargetCrawlingMessage.setAttachment(crawlingResult);
                    linkTargetCrawlingMessage.setTargetUri(crawlingResult.getUri());

                    Text crawlingResultKey = new Text(crawlingResult.getUri().toString());
                    postMessage(crawlingResultKey, linkTargetCrawlingMessage, context);
                 }
            } catch (Exception e) {
                if(currentDocumentCrawlMessage != null) {
                    currentDocumentCrawlMessage.getAttachment().setCrawlingState(Document.CRAWLING_STATE_ERROR);
                    handleDocumentException(currentDocumentCrawlMessage, key, context, e);
                    e.printStackTrace();
                } else {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}