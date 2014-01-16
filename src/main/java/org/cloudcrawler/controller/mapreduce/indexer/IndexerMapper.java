package org.cloudcrawler.controller.mapreduce.indexer;

import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.cloudcrawler.controller.mapreduce.AbstractMapper;
import org.cloudcrawler.domain.crawler.Document;
import org.cloudcrawler.domain.crawler.message.DocumentMessage;
import org.cloudcrawler.domain.crawler.message.Message;
import org.cloudcrawler.domain.crawler.message.MessagePersistenceManager;
import org.cloudcrawler.domain.indexer.Indexer;
import org.cloudcrawler.domain.ioc.CloudCrawlerModule;

import java.io.IOException;

/**
 * The page linkTrust mapper is producing pagerank messages. It inherits his
 * own page linkTrust to linked documents by sending messages to the linked websites.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 *
 */
public class IndexerMapper extends AbstractMapper {

    protected Indexer indexer;

    protected Injector injector;

    /**
     * @param indexer
     */
    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }

    public void initialize(Configuration configuration) throws Exception{
        if(this.injector == null) {
            this.injector = CloudCrawlerModule.getConfiguredInjector(configuration);
        }

        if(this.messageManager == null) {
            this.setMessageManager(injector.getInstance(MessagePersistenceManager.class));
        }

        if(this.indexer == null) {
            this.setIndexer(injector.getInstance(Indexer.class));
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
            Message message = this.messageManager.wakeup(value.toString());

            if(!message.getAttachmentClassname().endsWith("Document")) {
                System.out.println("Can not handle message with class "+message.getAttachmentClassname());
                return;
            }

            DocumentMessage currentDocumentCrawlMessage = (DocumentMessage) message;
            Document        crawled = currentDocumentCrawlMessage.getAttachment();

                //keep the message
            postMessage(key,currentDocumentCrawlMessage,context);

            if(crawled.getCrawlingState() == Document.CRAWLING_STATE_CRAWLED) {
                //and index it
                indexer.index(crawled);
                indexer.commit();
                //small sleep after index
                Thread.sleep(500);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
