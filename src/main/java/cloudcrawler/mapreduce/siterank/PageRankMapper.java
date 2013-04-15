package cloudcrawler.mapreduce.siterank;

import cloudcrawler.domain.crawler.Document;
import cloudcrawler.domain.crawler.Link;
import cloudcrawler.domain.crawler.contentparser.XHTMLContentParser;
import cloudcrawler.domain.crawler.message.DocumentMessage;
import cloudcrawler.domain.crawler.message.InheritPageRankMessage;
import cloudcrawler.domain.crawler.message.Message;
import cloudcrawler.domain.crawler.message.MessagePersistenceManager;
import cloudcrawler.domain.crawler.pagerank.InheritedPageRank;
import cloudcrawler.ioc.CloudCrawlerModule;
import cloudcrawler.mapreduce.AbstractMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.hadoop.io.Text;

import java.io.IOException;
import java.util.Vector;

/**
 * The page rank mapper is producing pagerank messages. It inherits his
 * own page rank to linked documents by sending messages to the linked websites.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */
public class PageRankMapper extends AbstractMapper {

    protected Injector injector;

    protected XHTMLContentParser xhtmlContentParser;

    public PageRankMapper() {
        //since the Crawling mapper is instanciated in hadoop
        //we inject the dependecies by our own
        injector = Guice.createInjector(new CloudCrawlerModule());
        this.setXhtmlContentParser(injector.getInstance(XHTMLContentParser.class));
        this.setMessageManager(injector.getInstance(MessagePersistenceManager.class));
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
            Message message = this.messageManager.wakeup(value.toString());

            if(!message.getAttachmentClassname().equals(Document.class.getCanonicalName())) {
                System.out.println("Can not handle messge with class "+message.getAttachmentClassname());
                return;
            }

            DocumentMessage currentDocumentCrawlMessage = (DocumentMessage) message;
            Document        crawled = currentDocumentCrawlMessage.getAttachment();

            if(crawled == null) {
                return;
            }

            if(crawled.getCrawlingState() != Document.CRAWLING_STATE_CRAWLED) {
                    //we can only evaluate outgoing links for crawled documents
                postMessage(key,currentDocumentCrawlMessage,context);
                return;
            }

            double inheritableRank = 1.0;
            int rankAnalyzeCount = (int) crawled.getRankAnalyzeCount();

            if(rankAnalyzeCount > 0) {
                inheritableRank = (double) crawled.getRank();
            }

            xhtmlContentParser.initialize(crawled.getUri(), crawled.getContent(), crawled.getMimeType());
            Vector<Link> links = xhtmlContentParser.getOutgoingLinks(true);

            double rankToInherit = 0.0;
            if(links.size() > 0) {
                rankToInherit = inheritableRank / links.size();
            }

            System.out.println("Processing: "+crawled.getUri().toString()+" inheriting pagepage "+rankToInherit+" to "+links.size()+" documents");

            for(Link link : links){
                InheritPageRankMessage pageRankMessage = new InheritPageRankMessage();
                InheritedPageRank rank = new InheritedPageRank();
                rank.setRank(rankToInherit);
                pageRankMessage.setAttachment(rank);
                pageRankMessage.setTargetUri(link.getTargetUri());

                Text pageRankKey = new Text(link.getTargetUri().toString());
                postMessage(pageRankKey,pageRankMessage,context);
            }

                //we increment the link analyze count and set the rank to 0.0
                //since all ranks (incoming and outgoing should be reflected in message that will be
                // combined in the reducer)
            crawled.incrementRankAnalyzeCount();
            crawled.setRank(0.0);

                //write back the crawled document
            postMessage(key,currentDocumentCrawlMessage,context);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
