package cloudcrawler.mapreduce;

import cloudcrawler.domain.crawler.Document;
import cloudcrawler.domain.crawler.Service;
import cloudcrawler.domain.crawler.message.DocumentMessage;
import cloudcrawler.ioc.CloudCrawlerModule;
import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.http.conn.HttpHostConnectException;
import org.xml.sax.SAXException;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
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


                //reconstitute the object or assign the uri
            if (!value.toString().trim().equals("") ) {
                currentDocumentCrawlMessage = gson.fromJson(value.toString(),DocumentMessage.class);
                crawled = currentDocumentCrawlMessage.getAttachment();
            }


            URI uri = new URI(key.toString());
            crawled.setUri(uri);

            if(crawled != null) {
                if(crawled.getCrawlCount() == 0 ) {
                    if(crawled.getCrawlingCountdown() == 0) {
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
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            System.out.println("Error crawling " + key + " connection timmed out");
        } catch (HttpHostConnectException e) {
            System.out.println("Connection refuised " + key);
        } catch (SocketTimeoutException e) {
            System.out.println("Socket timeout during " + key);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host "+key);
        } catch (SSLPeerUnverifiedException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();

            //todo
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unknown error");
        }
    }


}