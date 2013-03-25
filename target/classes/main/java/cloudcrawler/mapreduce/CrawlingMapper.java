package cloudcrawler.mapreduce;

import cloudcrawler.domain.crawler.CrawlingDocument;
import cloudcrawler.domain.crawler.CrawlingService;
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

    protected CrawlingService crawlingService;

    protected Injector injector;

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public void setCrawlingService(CrawlingService crawlingService) {
        this.crawlingService = crawlingService;
    }

    public CrawlingMapper() {
            //since the Crawling mapper is instanciated in hadoop
            //we inject the dependecies by our own
        injector = Guice.createInjector(new CloudCrawlerModule());

        this.setGson(injector.getInstance(Gson.class));
        this.setCrawlingService(injector.getInstance(CrawlingService.class));
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
            CrawlingDocument crawled = new CrawlingDocument();
                //reconstitute the object or assign the uri
            if (!value.toString().trim().equals("") ) {
                crawled = gson.fromJson(value.toString(),CrawlingDocument.class);
            }

            URI uri = new URI(key.toString());
            crawled.setUri(uri);

            if(crawled != null && crawled.getCrawlCount() == 0) {
                Vector<CrawlingDocument> crawlingResults = crawlingService.crawlAndFollowLinks(crawled);

                for(CrawlingDocument crawlingResult : crawlingResults) {
                    String json = gson.toJson(crawlingResult);
                    Text crawlingResultKey = new Text(crawlingResult.getUri().toString());
                    Text crawlingResultValue = new Text(json.toString());

                    context.write(crawlingResultKey, crawlingResultValue);
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
            //todo
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unknown error");
        }
    }


}