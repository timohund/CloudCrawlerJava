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
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    protected int counter = 0;

    /**
     * @param indexer
     */
    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }

    /**
     * @return Indexer
     */
    public Indexer getIndexer() {
       return indexer;
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
            this.indexer.prepare();
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
            context.progress();

            this.initialize(context.getConfiguration());
            Message message = this.messageManager.wakeup(value.toString());

            if(!message.getAttachmentClassname().endsWith("Document")) {
                System.out.println("Can not handle message with class " + message.getAttachmentClassname());
                return;
            }

            DocumentMessage currentDocumentCrawlMessage = (DocumentMessage) message;
            Document        crawled = currentDocumentCrawlMessage.getAttachment();

                //keep the message
            postMessage(key,currentDocumentCrawlMessage,context);

            context.progress();


//            if(crawled.getCrawlingState() == Document.CRAWLING_STATE_CRAWLED) {
                //and index it
                indexer.index(crawled);

                counter++;
                if(counter % 1000 == 0) {
                    indexer.commit();
                    //small sleep after index
                    Thread.sleep(2000);
                } else {
                    Thread.sleep(100);
                }

                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                System.out.println("Indexing new document at:" + dateFormat.format(date));
//          }
        } catch (Exception e) {
            System.out.println(e.getMessage());

            StackTraceElement[] stackTraces = e.getStackTrace();

            for(StackTraceElement stackTrace : stackTraces) {
                System.out.println(stackTrace.getFileName()+'#'+stackTrace.getLineNumber());
            }


            e.printStackTrace();
        }
    }

    public void cleanup(Context context) {
        try {
            indexer.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());

            StackTraceElement[] stackTraces = e.getStackTrace();

            for(StackTraceElement stackTrace : stackTraces) {
                System.out.println(stackTrace.getFileName()+'#'+stackTrace.getLineNumber());
            }

            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
