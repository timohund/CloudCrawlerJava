package cloudcrawler.domain.crawler;

import cloudcrawler.domain.crawler.contentparser.XHTMLContentParser;
import cloudcrawler.domain.crawler.schedule.CrawlingScheduleStrategy;
import cloudcrawler.system.http.HttpService;
import cloudcrawler.system.uri.URIUnifier;
import com.google.inject.Inject;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
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

    @Inject
    public Service(HttpService httpService, URIUnifier uriUnifier, XHTMLContentParser xHTMLParser, CrawlingScheduleStrategy schedulingStrategy, cloudcrawler.domain.crawler.robotstxt.Service robotsTxtService) {
        this.httpService = httpService;
        this.uriUni = uriUnifier;
        this.xHTMLParser = xHTMLParser;
        this.robotsTxtService = robotsTxtService;
    }

    public Vector<Document> crawlAndFollowLinks(Document toCrawl) throws Exception {
        Vector<Document> results = new Vector<Document>();

        if (!this.robotsTxtService.isAllowedUri(toCrawl.getUri())) {
            System.out.println("Crawl blocked by robotstxt txt");
            return results;
        }

        HttpResponse headResponse = httpService.getUrlWithHead(toCrawl.getUri());
        Header header = headResponse.getLastHeader(new String("Content-Type"));
        Boolean isHeadIndicatingHtml = header.getValue().contains(new String("text/html"));
        //close connection
        EntityUtils.consume(headResponse.getEntity());

        if (!isHeadIndicatingHtml) {
            System.out.println("No html response indicated by head");
            this.httpService.closeAllConnections();
            return results;
        }


        HttpResponse getResponse = httpService.getUriWithGet(toCrawl.getUri());
        String getMimeType = getResponse.getEntity().getContentType().getValue();
        Boolean isGetIndicatingHtml = getMimeType.contains(new String("text/html"));

        if (!isGetIndicatingHtml) {
            System.out.println("No html response indicated in get");
            this.httpService.closeAllConnections();
            return results;
        }

        Long contentLength      = getResponse.getEntity().getContentLength();
        int contentSizeLimit   = 1024 * 1024;

        if(contentLength > contentSizeLimit) {
            System.out.println("Skipping to large file "+contentLength);
            this.httpService.closeAllConnections();
            return results;
        }

        //do the real request
        System.out.println("Crawling " + toCrawl.getUri().toString());
        toCrawl.incrementCrawCount();
        toCrawl.setCrawlingState(Document.CRAWLING_STATE_CRAWLED);

        String website = getWebsiteUTF8Content(getResponse);

        toCrawl.setContent(website);
        toCrawl.setMimeType(getMimeType);
        results.add(toCrawl);
        results = this.prepareLinkedDocuments(results, toCrawl);
        EntityUtils.consume(getResponse.getEntity());

        return results;
    }

    private String getWebsiteUTF8Content(HttpResponse getResponse) throws IOException {
        BufferedInputStream is = new BufferedInputStream(getResponse.getEntity().getContent());
        CharsetDetector charsetDetector = new CharsetDetector();
        charsetDetector.setText(is);
        CharsetMatch match = charsetDetector.detect();
        String website = "";

        if (!match.getName().equals("UTF-8")) {
            byte[] bytes = IOUtils.toByteArray(is);
            byte[] utf8 = new String(bytes, match.getName()).getBytes("UTF-8");
            website = new String(utf8, "UTF-8");

            Pattern regex = Pattern.compile("(<meta.*?charset=[^\"']+(\"|')[^>]*>)", Pattern.CASE_INSENSITIVE);
            Matcher regexMatcher = regex.matcher(website);
            website = regexMatcher.replaceAll("");
        } else {
            website = IOUtils.toString(is);
        }
        return website;
    }


    protected Vector<Document> prepareLinkedDocuments(Vector<Document> result, Document document) {
        int analyzeCount = document.getLinkAnalyzeCount();
        if (analyzeCount == 0) {
            try {
                xHTMLParser.initialize(document.getUri(), document.getContent(), document.getMimeType());
                URI baseURI = xHTMLParser.getBaseHrefUri();
                Vector<Link> links = xHTMLParser.getExternalLinkUris();

                for (Link link : links) {
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
        }

        return result;
    }
}
