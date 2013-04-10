package cloudcrawler.mapreduce.crawler;

import cloudcrawler.domain.crawler.Document;
import cloudcrawler.domain.crawler.Service;
import cloudcrawler.domain.crawler.message.DocumentMessage;
import cloudcrawler.ioc.CloudCrawlerModule;
import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.net.URI;
import java.util.Vector;

public class CrawlingMapper extends Mapper<Text, Text, Text, Text> {

    protected Gson gson;

    protected Service crawlingService;

    protected Injector injector;

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public void setCrawlingService(Service crawlingService) {
        this.crawlingService = crawlingService;
    }

    public CrawlingMapper() {
            //since the Crawling mapper is instanciated in hadoop
            //we inject the dependecies by our own
        injector = Guice.createInjector(new CloudCrawlerModule());

        this.setGson(injector.getInstance(Gson.class));
        this.setCrawlingService(injector.getInstance(Service.class));
    }

    /**
     *
     * @param gson
     * @param service
     */
    public CrawlingMapper(Gson gson, Service service) {
        this.setGson(gson);
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

        String json                 = gson.toJson(message);
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
                //no key norhing todo
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
                currentDocumentCrawlMessage = gson.fromJson(value.toString(),DocumentMessage.class);
                crawled = currentDocumentCrawlMessage.getAttachment();
            }


            try {
                URI uri = new URI(key.toString());
                crawled.setUri(uri);

                if(crawled != null) {
                    if(crawled.getCrawlCount() == 0 && crawled.getErrorCount() < 2) {
                        if(crawled.getCrawlingState()== Document.CRAWLING_STATE_SCHEDULED) {
                            //Thread.sleep(100);
                            Vector<Document> crawlingResults = crawlingService.crawlAndFollowLinks(crawled);

                            for(Document crawlingResult : crawlingResults) {
                                DocumentMessage linkTargetCrwalingMessage = new DocumentMessage();
                                linkTargetCrwalingMessage.setAttachment(crawlingResult);
                                linkTargetCrwalingMessage.setTargetUri(crawlingResult.getUri());

                                String json = gson.toJson(linkTargetCrwalingMessage);

                                Text crawlingResultKey = new Text(crawlingResult.getUri().toString());
                                Text crawlingResultValue = new Text(json.toString());

                                context.write(crawlingResultKey, crawlingResultValue);
                            }
                        } else {
                                //repost the document
                            String json = gson.toJson(currentDocumentCrawlMessage);
                            Text crawlingResultValue = new Text(json.toString());
                            context.write(key,crawlingResultValue);
                        }
                    } else {
                           //repost the document
                        String json = gson.toJson(currentDocumentCrawlMessage);
                        Text crawlingResultValue = new Text(json.toString());
                        context.write(key,crawlingResultValue);
                    }
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