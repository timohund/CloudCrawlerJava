package org.cloudcrawler.controller.mapreduce.crawler;

import com.google.gson.JsonIOException;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.cloudcrawler.domain.crawler.Document;
import org.cloudcrawler.domain.crawler.DocumentMerger;
import org.cloudcrawler.domain.crawler.message.DocumentMessage;
import org.cloudcrawler.domain.crawler.message.Message;
import org.cloudcrawler.domain.crawler.message.MessagePersistenceManager;
import org.cloudcrawler.domain.crawler.schedule.CrawlingScheduleStrategy;
import org.cloudcrawler.domain.ioc.CloudCrawlerModule;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class CrawlingReducer extends Reducer<Text, Text, Text, Text> {

    protected Injector injector;

    protected DocumentMerger merger;

    protected CrawlingScheduleStrategy crawlingScheduler;

    protected MessagePersistenceManager messageManager;

    public void setMessageManager(MessagePersistenceManager messageManager) {
        this.messageManager = messageManager;
    }

    public void setMerger(DocumentMerger merger) {
        this.merger = merger;
    }

    public void setCrawlingScheduler(CrawlingScheduleStrategy crawlingScheduler) {
        this.crawlingScheduler = crawlingScheduler;
    }

    protected void initialize(Configuration configuration) {
        if(this.injector == null) {
            this.injector = CloudCrawlerModule.getConfiguredInjector(configuration);
        }

        if(this.messageManager == null) {
            this.setMessageManager(injector.getInstance(MessagePersistenceManager.class));
        }

        if(this.merger == null) {
            this.setMerger(injector.getInstance(DocumentMerger.class));
        }

        if(this.crawlingScheduler == null) {
            this.setCrawlingScheduler(injector.getInstance(CrawlingScheduleStrategy.class));
        }
    }

    /**
     * @param key
     * @param values
     * @param context
     * @throws IOException
     * @throws InterruptedException
     */
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        try {
            this.initialize(context.getConfiguration());
            this.merger.reset();

            for (Text val : values) {
                String json = val.toString();

                try {
                    Message message = messageManager.wakeup(json);
                    DocumentMessage documentMessage = (DocumentMessage) message;
                    Document document = documentMessage.getAttachment();
                    merger.merge(key.toString(), document);
                } catch (JsonIOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    System.out.println("Error indexing "+key+" "+json+" ");
                    e.printStackTrace();
                }

                context.progress();
            }

            this.emitAll(this.merger.getResult(),context);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void emitAll(HashMap<String,Document> mergeResult,Context context) throws Exception {
        Iterator it = mergeResult.keySet().iterator();

        while(it.hasNext()) {
            String url = it.next().toString();
            Document document = mergeResult.get(url);

            if(document.getCrawlingState() == Document.CRAWLING_STATE_SCHEDULED) {
                throw new Exception("An document should never leave the crawling mapper in a scheduled state");
            } else if(document.getCrawlingState() == Document.CRAWLING_STATE_WAITING) {
                int nextState = crawlingScheduler.getNextCrawlingState(document.getUri());
                document.setCrawlingState(nextState);
            }

            DocumentMessage documentMessage = new DocumentMessage();
            documentMessage.setAttachment(document);
            String documentMessageJson = " "+messageManager.sleep(documentMessage);
            context.write(new Text(url),new Text(documentMessageJson));
        }

        context.setStatus("done");
    }
}
