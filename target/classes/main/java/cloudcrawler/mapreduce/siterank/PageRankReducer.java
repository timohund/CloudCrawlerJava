package cloudcrawler.mapreduce.siterank;

import cloudcrawler.domain.crawler.message.DocumentMessage;
import cloudcrawler.domain.crawler.message.Message;
import cloudcrawler.domain.crawler.message.MessagePersistenceManager;
import cloudcrawler.domain.crawler.pagerank.PageRankMerger;
import cloudcrawler.ioc.CloudCrawlerModule;
import com.google.gson.JsonIOException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 14.04.13
 * Time: 10:08
 * To change this template use File | Settings | File Templates.
 */
public class PageRankReducer  extends Reducer<Text, Text, Text, Text> {

    protected Injector injector;

    protected MessagePersistenceManager messageManager;

    protected PageRankMerger pageRankMerger;

    public PageRankReducer() {
        injector = Guice.createInjector(new CloudCrawlerModule());
        this.setMessageManager(injector.getInstance(MessagePersistenceManager.class));
        this.setPageRankMerger(injector.getInstance(PageRankMerger.class));
    }

    public void setMessageManager(MessagePersistenceManager messageManager) {
        this.messageManager = messageManager;
    }

    public void setPageRankMerger(PageRankMerger pageRankMerger) {
        this.pageRankMerger = pageRankMerger;
    }

    /**
     * @param key
     * @param values
     * @param context
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        try {
            this.pageRankMerger.reset();
            for (Text val : values) {
                String json = val.toString();

                try {
                    Message message = messageManager.wakeup(json);
                    this.pageRankMerger.merge(message);
                } catch (JsonIOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    System.out.println("Error indexing "+key+" "+json+" ");
                    e.printStackTrace();
                }

                context.progress();
            }

            DocumentMessage mergedDocument = pageRankMerger.getResult();
            if(mergedDocument == null) {
                System.out.println("Can not merge document "+key.toString());
                return;
            }

            if(mergedDocument.getAttachment().getRank() > 10.0) {
                System.out.println("High page rank document "+mergedDocument.getAttachment().getRank()+" "+mergedDocument.getAttachment().getUri().toString());
            }

            String documentMessageJson = " "+messageManager.sleep(mergedDocument);
            context.write(key, new Text(documentMessageJson));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}