package cloudcrawler.domain.crawler;

import cloudcrawler.domain.contentparser.XHTMLContentParser;
import cloudcrawler.system.http.HttpService;
import cloudcrawler.system.uri.URIHelper;
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
import java.util.Random;
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
public class CrawlingService {

    protected HttpService httpService;

    protected URIUnifier uriUni;

    protected XHTMLContentParser xHTMLParser;

    @Inject
    public CrawlingService(HttpService httpService, URIUnifier uriUnifier, XHTMLContentParser xHTMLParser) {
        this.httpService = httpService;
        this.uriUni = uriUnifier;
        this.xHTMLParser = xHTMLParser;
    }

    public Vector<CrawlingDocument> crawlAndFollowLinks(CrawlingDocument toCrawl) throws IOException, InterruptedException, ParserConfigurationException, SAXException, XPathExpressionException, URISyntaxException {
        Vector<CrawlingDocument> results = new Vector<CrawlingDocument>();

        HttpResponse headResponse = httpService.getUrlWithHead(toCrawl.getUri());
        Header header = headResponse.getLastHeader(new String("Content-Type"));
        Boolean isHtml = header.getValue().contains(new String("text/html"));
        //close connection
        EntityUtils.consume(headResponse.getEntity());

        if (isHtml) {
            //do the real request
            System.out.println("Crawling "+toCrawl.getUri().toString());
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
        }

        return results;
    }


    protected Vector<CrawlingDocument> prepareLinkedDocuments(Vector<CrawlingDocument> result, CrawlingDocument crawlingDocument)  {
        int analyzeCount = crawlingDocument.getLinkAnalyzeCount();
        if(analyzeCount == 0 ) {
            try {
                xHTMLParser.initialize(crawlingDocument.getUri(), crawlingDocument.getContent(), crawlingDocument.getMimeType());
                URI baseURI = xHTMLParser.getBaseHrefUri();
                Vector<URI> uris = xHTMLParser.getExternalLinkUris();

                for(URI linkUri : uris) {
                    if(!(linkUri == null) && linkUri.toString().contains(".de/") ) {

                        //todo remove query and fragment here, make it more flexible
                        URI unifiedUri       = this.uriUni.unifiy(linkUri,crawlingDocument.getUri(),baseURI);
                        URIBuilder builder  = new URIBuilder(unifiedUri);
                        builder.setQuery(null);
                        builder.setFragment(null);
                        unifiedUri           = builder.build();


                        //create a new document from the followed link
                        CrawlingDocument linkDocument = new CrawlingDocument();


                        String currentSecondLevelHost = URIHelper.getSecondLevelHostName(crawlingDocument.getUri());
                        String linkSecondLevelHost = URIHelper.getSecondLevelHostName(unifiedUri);

                        if(currentSecondLevelHost.equals(linkSecondLevelHost)) {
                            Random rand = new Random();
                            //int randomNum = rand.nextInt(max - min + 1) + min;
                            int randomNum = rand.nextInt(30 - 10 + 1) + 10;
                      //      System.out.println(randomNum);
                            linkDocument.setCrawlingCountdown(randomNum);
                        } else {
                        //    System.out.println(unifiedUri.getHost()+" "+crawlingDocument.getUri().getHost());
                       //     System.out.println("External Link "+unifiedUri);
                        }

                        linkDocument.addIncomingLink(crawlingDocument.getUri().toString());
                        linkDocument.setUri(unifiedUri);

                        result.add(linkDocument);
                    }
                }

                crawlingDocument.incrementLinkAnalyzeCount();
                crawlingDocument.setContent("");

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
