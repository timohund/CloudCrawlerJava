package cloudcrawler.mapreduce.siterank;

import cloudcrawler.domain.crawler.Document;
import cloudcrawler.domain.crawler.Link;
import cloudcrawler.domain.crawler.contentparser.XHTMLContentParser;
import cloudcrawler.domain.crawler.message.DocumentMessage;
import cloudcrawler.domain.crawler.message.InheritPageRankMessage;
import cloudcrawler.domain.crawler.pagerank.InheritedPageRank;
import cloudcrawler.ioc.CloudCrawlerModule;
import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 14.04.13
 * Time: 10:08
 * To change this template use File | Settings | File Templates.
 */
public class PageRankMapper extends Mapper<Text, Text, Text, Text> {

    protected Gson gson;

    protected Injector injector;

    protected XHTMLContentParser xhtmlContentParser;


    public PageRankMapper() {
        //since the Crawling mapper is instanciated in hadoop
        //we inject the dependecies by our own
        injector = Guice.createInjector(new CloudCrawlerModule());
        this.setGson(injector.getInstance(Gson.class));
        this.setXhtmlContentParser(injector.getInstance(XHTMLContentParser.class));
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public Injector getInjector() {
        return injector;
    }

    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public XHTMLContentParser getXhtmlContentParser() {
        return xhtmlContentParser;
    }

    public void setXhtmlContentParser(XHTMLContentParser xhtmlContentParser) {
        this.xhtmlContentParser = xhtmlContentParser;
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
            DocumentMessage currentDocumentCrawlMessage = gson.fromJson(value.toString(),DocumentMessage.class);
            Document crawled = currentDocumentCrawlMessage.getAttachment();

            if(crawled == null) {
                return;
            }

            if(crawled.getCrawlingState() != Document.CRAWLING_STATE_CRAWLED) {
                String json = gson.toJson(currentDocumentCrawlMessage);
                Text crawlingResultValue = new Text(json.toString());
                context.write(key,crawlingResultValue);
                return;
            }

            double inheritableRank = 1.0;
            int rankAnalyzeCount = (int) crawled.getRankAnalyzeCount();

            if(rankAnalyzeCount > 0) {
                inheritableRank = (double) crawled.getRank();
            }

            xhtmlContentParser.initialize(crawled.getUri(), crawled.getContent(), crawled.getMimeType());
            Vector<Link> links = xhtmlContentParser.getOutgoingLinks(true);
            double rankToInherit = inheritableRank / links.size();

            System.out.println("Processing: "+crawled.getUri().toString()+" inheriting pagepage "+rankToInherit+" to "+links.size()+" documents");

            for(Link link : links){
                InheritPageRankMessage pageRankMessage = new InheritPageRankMessage();
                InheritedPageRank rank = new InheritedPageRank();
                rank.setRank(rankToInherit);
                pageRankMessage.setAttachment(rank);
                pageRankMessage.setTargetUri(link.getTargetUri());

                Text pageRankKey = new Text(link.getTargetUri().toString());
                String json = gson.toJson(pageRankMessage);
                Text pageRankMessageValue = new Text(json.toString());
                context.write(pageRankKey,pageRankMessageValue);
            }


                //we increment the link analyze count and set the rank to 0.0
                //since all ranks (incoming and outgoing should be reflected in message that will be
                // combined in the reducer)
            crawled.incrementRankAnalyzeCount();
            crawled.setRank(0.0);

                //write back the crawled document
            String json = gson.toJson(currentDocumentCrawlMessage);
            Text crawlingResultValue = new Text(json.toString());
            context.write(key,crawlingResultValue);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
}
