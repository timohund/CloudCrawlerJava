package org.cloudcrawler.controller.mapreduce.trust;

import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.cloudcrawler.controller.mapreduce.AbstractMapper;
import org.cloudcrawler.domain.crawler.Document;
import org.cloudcrawler.domain.crawler.Link;
import org.cloudcrawler.domain.crawler.contentparser.XHTMLContentParser;
import org.cloudcrawler.domain.crawler.message.DocumentMessage;
import org.cloudcrawler.domain.crawler.message.InheritLinkTrustMessage;
import org.cloudcrawler.domain.crawler.message.Message;
import org.cloudcrawler.domain.crawler.message.MessagePersistenceManager;
import org.cloudcrawler.domain.crawler.trust.link.InheritedLinkTrust;
import org.cloudcrawler.domain.ioc.CloudCrawlerModule;
import org.cloudcrawler.system.uri.URIValidator;

import java.io.IOException;
import java.util.Vector;

/**
 * The page linkTrust mapper is producing pagerank messages. It inherits his
 * own page linkTrust to linked documents by sending messages to the linked websites.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 *
 */
public class LinkTrustMapper extends AbstractMapper {

    protected Injector injector;

    protected XHTMLContentParser xhtmlContentParser;

    protected URIValidator uriValidator;

    public void initialize(Configuration configuration) {
        if(this.injector == null) {
            this.injector = CloudCrawlerModule.getConfiguredInjector(configuration);
        }

        if(this.xhtmlContentParser == null) {
            this.setXhtmlContentParser(injector.getInstance(XHTMLContentParser.class));
        }

        if(this.messageManager == null) {
            this.setMessageManager(injector.getInstance(MessagePersistenceManager.class));
        }

        if(this.uriValidator == null) {
            this.setUriValidator(injector.getInstance(URIValidator.class));
        }
    }

    public void setUriValidator(URIValidator uriValidator) {
        this.uriValidator = uriValidator;
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
            this.initialize(context.getConfiguration());
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
                inheritableRank = (double) crawled.getLinkTrust();
            }

            xhtmlContentParser.initialize(crawled.getUri(), crawled.getContent(), crawled.getMimeType());
            Vector<Link> links = xhtmlContentParser.getOutgoingLinks(true);
            Vector<Link> allowedLinks = new Vector<Link>();

            for(Link link : links){
                //only inherit link trust to allowed uris
                if(this.uriValidator.isValid(link.getTargetUri())) {
                    allowedLinks.add(link);
                }
            }

            double rankToInherit = 0.0;
            if(links.size() > 0) {
                rankToInherit = inheritableRank / allowedLinks.size();
            }

            System.out.println("Processing: "+crawled.getUri().toString()+" inheriting pagepage "+rankToInherit+" to "+allowedLinks.size()+" documents");

            for(Link link : allowedLinks){
                InheritLinkTrustMessage pageRankMessage = new InheritLinkTrustMessage();
                InheritedLinkTrust rank = new InheritedLinkTrust();
                rank.setLinkTrust(rankToInherit);
                pageRankMessage.setAttachment(rank);
                pageRankMessage.setTargetUri(link.getTargetUri());

                Text pageRankKey = new Text(link.getTargetUri().toString());
                postMessage(pageRankKey,pageRankMessage,context);
            }

                //we increment the link analyze count and set the linkTrust to 0.0
                //since all ranks (incoming and outgoing should be reflected in message that will be
                // combined in the reducer)
            crawled.incrementRankAnalyzeCount();
            crawled.setLinkTrust(0.0);

                //write back the crawled document
            postMessage(key,currentDocumentCrawlMessage,context);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
