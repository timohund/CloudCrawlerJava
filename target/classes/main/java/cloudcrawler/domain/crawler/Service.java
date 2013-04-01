package cloudcrawler.domain.crawler;

import cloudcrawler.domain.crawler.contentparser.XHTMLContentParser;
import cloudcrawler.domain.crawler.schedule.CrawlingScheduleStrategy;
import cloudcrawler.system.http.HttpService;
import cloudcrawler.system.uri.URIUnifier;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

/**
 * The crawling service is responsible to create
 * crawled object for a given URI.
 *
 * It returns a collection of documents.
 * This collection contains the requested document
 * with all crawling information and Crawling documents for all
 * linked documents which are marked as uncrawled
 *
 *
 */
public class Service {

    protected HttpService httpService;

    protected URIUnifier uriUni;

    protected XHTMLContentParser xHTMLParser;

    protected CrawlingScheduleStrategy schedulingStrategy;

    protected cloudcrawler.domain.crawler.robotstxt.Service robotsTxtService;

    @Inject
    public Service(HttpService httpService, URIUnifier uriUnifier, XHTMLContentParser xHTMLParser, CrawlingScheduleStrategy schedulingStrategy, cloudcrawler.domain.crawler.robotstxt.Service robotsTxtService) {
        this.httpService = httpService;
        this.uriUni = uriUnifier;
        this.xHTMLParser = xHTMLParser;
        this.schedulingStrategy = schedulingStrategy;
        this.robotsTxtService = robotsTxtService;
    }

    public Vector<Document> crawlAndFollowLinks(Document toCrawl) throws Exception {
        Vector<Document> results = new Vector<Document>();

        HttpResponse headResponse = httpService.getUrlWithHead(toCrawl.getUri());
        Header header = headResponse.getLastHeader(new String("Content-Type"));
        Boolean isHtml = header.getValue().contains(new String("text/html"));
        //close connection
        EntityUtils.consume(headResponse.getEntity());

        if (isHtml) {
            //do the real request
            System.out.println("Crawling "+toCrawl.getUri().toString());

            if(this.robotsTxtService.isAllowedUri(toCrawl.getUri(),"")) {
                HttpResponse getResponse = httpService.getUriWithGet(toCrawl.getUri());

                StringWriter writer = new StringWriter();
                IOUtils.copy(getResponse.getEntity().getContent(), writer);
                String website = writer.toString();

                toCrawl.setContent(website);
                toCrawl.setMimeType(getResponse.getEntity().getContentType().getValue());
                toCrawl.incrementCrawCount();

                results.add(toCrawl);
                results = this.prepareLinkedDocuments(results, toCrawl);

                EntityUtils.consume(getResponse.getEntity());
            } else {
              System.out.println("Crawl blocked by robotstxt txt");
            }
        }

        return results;
    }


    protected Vector<Document> prepareLinkedDocuments(Vector<Document> result, Document document)  {
        int analyzeCount = document.getLinkAnalyzeCount();
        if(analyzeCount == 0 ) {
            try {
                xHTMLParser.initialize(document.getUri(), document.getContent(), document.getMimeType());
                URI baseURI = xHTMLParser.getBaseHrefUri();
                Vector<Link> links = xHTMLParser.getExternalLinkUris();
                this.schedulingStrategy.setCurrentPageUri(document.getUri());

                for(Link link : links) {
                    if(!(link == null) && link.getTargetUri().toString().contains(".de/") ) {

                        //todo remove query and fragment here, make it more flexible
                        URI unifiedUri       = this.uriUni.unifiy(link.getTargetUri(), document.getUri(),baseURI);
                        link.setTargetUri(unifiedUri);

                        URIBuilder builder  = new URIBuilder(unifiedUri);
                        builder.setQuery(null);
                        builder.setFragment(null);
                        unifiedUri           = builder.build();


                        //create a new document from the followed link
                        Document linkDocument = new Document();

                        linkDocument.addIncomingLink(link);
                        linkDocument.setUri(unifiedUri);

                        int crawlCountDown = this.schedulingStrategy.getCrawlingCountDown(unifiedUri);
                        linkDocument.setCrawlingCountdown(crawlCountDown);

                        result.add(linkDocument);
                    }
                }

                document.incrementLinkAnalyzeCount();
                document.setContent("");

            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SAXException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ParserConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (XPathExpressionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (URISyntaxException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        return result;
    }
}
