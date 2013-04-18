package cloudcrawler.mapreduce.trust;

import cloudcrawler.domain.crawler.message.DocumentMessage;
import cloudcrawler.domain.crawler.message.Message;
import cloudcrawler.domain.crawler.message.MessagePersistenceManager;
import cloudcrawler.domain.crawler.trust.link.LinkTrustMerger;
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
public class LinkTrustReducer extends Reducer<Text, Text, Text, Text> {

    protected Injector injector;

    protected MessagePersistenceManager messageManager;

    protected LinkTrustMerger linkTrustMerger;

    public LinkTrustReducer() {
        injector = Guice.createInjector(new CloudCrawlerModule());
        this.setMessageManager(injector.getInstance(MessagePersistenceManager.class));
        this.setLinkTrustMerger(injector.getInstance(LinkTrustMerger.class));
    }

    public void setMessageManager(MessagePersistenceManager messageManager) {
        this.messageManager = messageManager;
    }

    public void setLinkTrustMerger(LinkTrustMerger linkTrustMerger) {
        this.linkTrustMerger = linkTrustMerger;
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
            this.linkTrustMerger.reset();
            for (Text val : values) {
                String json = val.toString();

                try {
                    Message message = messageManager.wakeup(json);
                    this.linkTrustMerger.merge(message);
                } catch (JsonIOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    System.out.println("Error indexing "+key+" "+json+" ");
                    e.printStackTrace();
                }

                context.progress();
            }

            DocumentMessage mergedDocument = linkTrustMerger.getResult();
            if(mergedDocument == null) {
                System.out.println("Can not merge document "+key.toString());
                return;
            }

            if(mergedDocument.getAttachment().getLinkTrust() > 10.0) {
                System.out.println("High page linkTrust document "+mergedDocument.getAttachment().getLinkTrust()+" "+mergedDocument.getAttachment().getUri().toString());
            }

            String documentMessageJson = " "+messageManager.sleep(mergedDocument);
            context.write(key, new Text(documentMessageJson));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}