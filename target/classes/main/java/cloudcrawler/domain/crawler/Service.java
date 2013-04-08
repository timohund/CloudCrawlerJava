package cloudcrawler.domain.crawler;

import cloudcrawler.domain.crawler.contentparser.XHTMLContentParser;
import cloudcrawler.domain.crawler.schedule.CrawlingScheduleStrategy;
import cloudcrawler.system.charset.converter.ConversionResult;
import cloudcrawler.system.charset.converter.ConversionService;
import cloudcrawler.system.http.HttpService;
import cloudcrawler.system.uri.URIUnifier;
import com.google.inject.Inject;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The crawling service is responsible to create
 * crawled object for a given URI.
 * <p/>
 * It returns a collection of documents.
 * This collection contains the requested document
 * with all crawling information and Crawling documents for all
 * linked documents which are marked as uncrawled
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */
public class Service {

    protected HttpService httpService;

    protected URIUnifier uriUni;

    protected XHTMLContentParser xHTMLParser;

    protected cloudcrawler.domain.crawler.robotstxt.Service robotsTxtService;

    protected ConversionService utf8ConversionService;

    @Inject
    public Service( HttpService httpService,
                    URIUnifier uriUnifier,
                    XHTMLContentParser xHTMLParser,
                    CrawlingScheduleStrategy schedulingStrategy,
                    cloudcrawler.domain.crawler.robotstxt.Service robotsTxtService,
                    ConversionService utf8ConversionService) {
        this.httpService = httpService;
        this.uriUni = uriUnifier;
        this.xHTMLParser = xHTMLParser;
        this.robotsTxtService = robotsTxtService;
        this.utf8ConversionService = utf8ConversionService;
    }

    /**
     * This method is used to fetch the content of a crawling document
     * and creating new documents for linked documents.
     *
     * @param toCrawl
     * @return
     * @throws Exception
     */
    public Vector<Document> crawlAndFollowLinks(Document toCrawl) throws Exception {
        Vector<Document> results = new Vector<Document>();

        if (    getRequestIsUnAllowedByRobotsTxt(toCrawl) ||
                getHeadRequestIndicatesUnAllowedContentType(toCrawl) ) {
            return results;
        }

        HttpResponse getResponse    = httpService.getUriWithGet(toCrawl.getUri());
        String getMimeType          = getResponse.getEntity().getContentType().getValue();

        if (    getGetRequestIndicatesUnAllowedContentType(getMimeType) ||
                getGetRequestIndicatesUnAllowedContentLength(getResponse) ) {
            return results;
        }

        // All checks passed, do the download
        System.out.println("Crawling " + toCrawl.getUri().toString());
        toCrawl.incrementCrawCount();
        toCrawl.setCrawlingState(Document.CRAWLING_STATE_CRAWLED);

        InputStream is= getResponse.getEntity().getContent();
        String website = convertWebsiteInputStreamToUtf8String(is);
        toCrawl.setContent(website);
        toCrawl.setMimeType(getMimeType);
        results.add(toCrawl);
        results = this.prepareLinkedDocuments(results, toCrawl);
        EntityUtils.consume(getResponse.getEntity());

        return results;
    }

    /**
     * This method is checking if the request is blocked by the robots txt.
     *
     * @param toCrawl
     * @return
     * @throws Exception
     */
    private boolean getRequestIsUnAllowedByRobotsTxt(Document toCrawl) throws Exception {
        boolean isBlocked = true;
        isBlocked = !this.robotsTxtService.isAllowedUri(toCrawl.getUri());
        if(isBlocked) {
            System.out.println("Crawl blocked by robotstxt txt");
        }

        return isBlocked;
    }

    /**
     * This method is used to check if the getResponse indicates a to large content size.
     *
     * @param getResponse
     * @return boolean
     */
    private boolean getGetRequestIndicatesUnAllowedContentLength(HttpResponse getResponse) {
        Vector<Document> results;
        Long contentLength = getResponse.getEntity().getContentLength();
        int contentSizeLimit = 1024 * 1024;
        if (contentLength > contentSizeLimit) {
            System.out.println("Skipping to large file " + contentLength);
            this.httpService.reset();
            return true;
        }
        return false;
    }

    /**
     * This method is used to check if the get request indicates an unallowed content type.
     *
     * @param getMimeType
     * @return boolean
     */
    private boolean getGetRequestIndicatesUnAllowedContentType(String getMimeType) {
        Vector<Document> results;
        Boolean isGetIndicatingHtml = getMimeType.contains(new String("text/html"));
        if (!isGetIndicatingHtml) {
            System.out.println("No html response indicated in get");
            this.httpService.reset();
            return true;
        }
        return false;
    }

    /**
     * This method is used to check if the head request in advanced allready indicates that
     * the mimeType of the response is unallowed.
     *
     * @param toCrawl
     * @return boolean
     * @throws IOException
     */
    private boolean getHeadRequestIndicatesUnAllowedContentType(Document toCrawl) throws IOException {
        HttpResponse headResponse = httpService.getUrlWithHead(toCrawl.getUri());
        Header header = headResponse.getLastHeader(new String("Content-Type"));
        Boolean isHeadIndicatingHtml = header.getValue().contains(new String("text/html"));
        //close connection
        EntityUtils.consume(headResponse.getEntity());
        if (!isHeadIndicatingHtml) {
            System.out.println("No html response indicated by head");
            this.httpService.reset();
            return true;
        }
        return false;
    }


    /**
     * Converts the input stream of the website into utf8.
     *
     * @param is
     * @return
     * @throws IOException
     */
    private String convertWebsiteInputStreamToUtf8String(InputStream is) throws IOException {
        ConversionResult result = utf8ConversionService.convertToUTF8(is);
        String website          = result.getContent();

        if(result.getWasConverted()) {
            Pattern regex = Pattern.compile("(<meta.*?charset=[^\"']+(\"|')[^>]*>)", Pattern.CASE_INSENSITIVE);
            Matcher regexMatcher = regex.matcher(website);
            website = regexMatcher.replaceAll("");
        }

        return website;
    }

    /**
     * This method is used to extract the links from the input documents and
     * create new documents for every linked element.
     *
     * @param result
     * @param document
     * @return
     */
    protected Vector<Document> prepareLinkedDocuments(Vector<Document> result, Document document) {
        int analyzeCount = document.getLinkAnalyzeCount();

        if (analyzeCount != 0) {
            return result;
        }

        try {
            xHTMLParser.initialize(document.getUri(), document.getContent(), document.getMimeType());
            URI baseURI = xHTMLParser.getBaseHrefUri();
            Vector<Link> links = xHTMLParser.getExternalLinkUris();

            for (Link link : links) {
                //todo make the link filter configureable
                if (!(link == null) && link.getTargetUri().toString().contains(".de/")) {

                    //todo remove query and fragment here, make it more flexible
                    URI unifiedUri = this.uriUni.unifiy(link.getTargetUri(), document.getUri(), baseURI);
                    link.setTargetUri(unifiedUri);
                    link.setSourceUri(document.getUri());

                    URIBuilder builder = new URIBuilder(unifiedUri);
                    builder.setQuery(null);
                    builder.setFragment(null);
                    unifiedUri = builder.build();

                    //create a new document from the followed link
                    Document linkDocument = new Document();

                    linkDocument.addIncomingLink(link);
                    linkDocument.setUri(unifiedUri);
                    linkDocument.setCrawlingState(Document.CRAWLING_STATE_WAITING);

                    result.add(linkDocument);
                }
            }

            document.incrementLinkAnalyzeCount();

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

        return result;
    }
}
