package cloudcrawler.domain.crawler;

import cloudcrawler.domain.contentparser.XHTMLContentParser;
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


    protected Vector<CrawlingDocument> prepareLinkedDocuments(Vector<CrawlingDocument> result, CrawlingDocument crawlingDocument) throws XPathExpressionException, URISyntaxException, ParserConfigurationException, SAXException, IOException, InterruptedException {
        int analyzeCount = crawlingDocument.getLinkAnalyzeCount();
        if(analyzeCount == 0 ) {
            xHTMLParser.initialize(crawlingDocument.getUri(), crawlingDocument.getContent(), crawlingDocument.getMimeType());
            URI baseURI = xHTMLParser.getBaseHrefUri();
            Vector<URI> uris = xHTMLParser.getExternalLinkUris();

            for(URI linkUri : uris) {
                if(!(linkUri == null) && linkUri.toString().contains(".de") ) {

                    //todo remove query and fragment here, make it more flexible
                    URI storedUri       = this.uriUni.unifiy(linkUri,crawlingDocument.getUri(),baseURI);
                    URIBuilder builder  = new URIBuilder(storedUri);
                    builder.setQuery(null);
                    builder.setFragment(null);
                    storedUri           = builder.build();

                    System.out.println("Following Link "+storedUri);

                    //create a new document from the followed link
                    CrawlingDocument linkDocument = new CrawlingDocument();
                    linkDocument.addIncomingLink(crawlingDocument.getUri().toString());
                    linkDocument.setUri(storedUri);

                    result.add(linkDocument);
                }
            }

            crawlingDocument.incrementLinkAnalyzeCount();
            crawlingDocument.setContent("");
        }

        return result;
    }
}
