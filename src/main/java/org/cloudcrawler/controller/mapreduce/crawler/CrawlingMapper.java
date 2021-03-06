package org.cloudcrawler.controller.mapreduce.crawler;

import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.cloudcrawler.controller.mapreduce.AbstractMapper;
import org.cloudcrawler.domain.crawler.Document;
import org.cloudcrawler.domain.crawler.Service;
import org.cloudcrawler.domain.crawler.message.DocumentMessage;
import org.cloudcrawler.domain.crawler.message.MessagePersistenceManager;
import org.cloudcrawler.domain.ioc.CloudCrawlerModule;

import java.io.IOException;
import java.net.URI;
import java.util.Vector;

public class CrawlingMapper extends AbstractMapper {

    protected Service crawlingService;

    protected Injector injector;


    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public void setCrawlingService(Service crawlingService) {
        this.crawlingService = crawlingService;
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

    protected void initialize(Configuration configuration) {
        if(this.injector == null) {
            this.injector = CloudCrawlerModule.getConfiguredInjector(configuration);
        }

        if(this.messageManager == null) {
            this.setMessageManager(injector.getInstance(MessagePersistenceManager.class));
        }

        if(this.crawlingService == null) {
            this.setCrawlingService(injector.getInstance(Service.class));
        }
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
            this.initialize(context.getConfiguration());
               //when the key is empty we skip the row
            if(key.toString().trim() == "") {   return;  }
                //create or retrieve the crawling document from json
            DocumentMessage currentDocumentCrawlMessage = getOrCreateCrawlMessage(value);
            Document crawled = currentDocumentCrawlMessage.getAttachment();

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

                Vector<Document> crawlingResults = crawlingService.crawlAndFollowLinks(crawled, false);

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

    private DocumentMessage getOrCreateCrawlMessage(Text value) {
        DocumentMessage currentDocumentCrawlMessage = new DocumentMessage();
        if (value.toString().trim().equals("") ) {
            //no value present => create document
            Document crawlDocument = new Document();
            crawlDocument.setCrawlingState(Document.CRAWLING_STATE_SCHEDULED);
            currentDocumentCrawlMessage.setAttachment(crawlDocument);
        } else {
            //reconstitute the object or assign the uri
            currentDocumentCrawlMessage = (DocumentMessage) messageManager.wakeup(value.toString());
        }

        return currentDocumentCrawlMessage;
    }
}