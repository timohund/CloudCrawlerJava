package cloudcrawler.mapreduce;

import cloudcrawler.domain.CrawlDocument;
import cloudcrawler.domain.contentparser.XHTMLContentParser;
import cloudcrawler.system.http.HttpService;
import cloudcrawler.system.uri.URIUnifier;
import com.google.gson.Gson;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Vector;

public class CrawlMapper extends Mapper<Text, Text, Text, Text> {

    private HttpService httpService = new HttpService();

    private Gson gson = new Gson();

    private URIUnifier uriUni = new URIUnifier();

    /**
     * @param key
     * @param value
     * @param context
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        try {
            URI uri = new URI(key.toString());
            CrawlDocument crawled = new CrawlDocument();

                //reconstitute the object or assign the uri
            if (value.toString().trim().equals("") && !key.toString().trim().equals("")) {
                crawled.setUrl(uri.toString());
            } else {
                crawled = gson.fromJson(value.toString(),CrawlDocument.class);
            }

            if(crawled != null && crawled.getCrawlCount() == 0) {
                HttpResponse headResponse = httpService.getUrlWithHead(uri);
                Header header = headResponse.getLastHeader(new String("Content-Type"));
                Boolean isHtml = header.getValue().contains(new String("text/html"));
                    //close connection
                EntityUtils.consume(headResponse.getEntity());

                if (isHtml) {
                    //do the real request
                    HttpResponse getResponse = httpService.getUriWithGet(uri);

                    StringWriter writer = new StringWriter();
                    IOUtils.copy(getResponse.getEntity().getContent(), writer);
                    String website = writer.toString();

                    crawled.setContent(website);
                    crawled.setMimeType(getResponse.getEntity().getContentType().getValue());
                    crawled.incrementCrawCount();

                    this.prepareLinkedDocuments(crawled,uri,context);

                    EntityUtils.consume(getResponse.getEntity());
                }
            }

            String json = gson.toJson(crawled);
            value = new Text(json.toString());

            context.write(key, value);

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

    protected void prepareLinkedDocuments(CrawlDocument crawlDocument,URI sourceUri, Context context) throws XPathExpressionException, URISyntaxException, ParserConfigurationException, SAXException, IOException, InterruptedException {
        int analyzeCount = crawlDocument.getLinkAnalyzeCount();
        if(analyzeCount == 0 ) {
            XHTMLContentParser xHTMLParser = new XHTMLContentParser();
            xHTMLParser.initialize(sourceUri,crawlDocument.getContent(),crawlDocument.getMimeType());
            URI baseURI = xHTMLParser.getBaseHrefUri();
            Vector<URI> uris = xHTMLParser.getExternalLinkUris();

            for(URI linkUri : uris) {
                if(!(linkUri == null) && linkUri.toString().contains(".de") ) {

                    //todo remove query and fragment here, make it more flexible
                    URI storedUri       = this.uriUni.unifiy(linkUri,sourceUri,baseURI);
                    URIBuilder builder  = new URIBuilder(storedUri);
                    builder.setQuery(null);
                    builder.setFragment(null);
                    storedUri           = builder.build();

                    System.out.println("Following Link "+storedUri);

                    //create a new document from the followed link
                    CrawlDocument linkDocument = new CrawlDocument();
                    linkDocument.addIncomingLink(sourceUri.toString());
                    linkDocument.setUrl(storedUri.toString());

                    String json = gson.toJson(linkDocument);
                    Text value  = new Text(json.toString());

                    context.write(new Text(storedUri.toString()), value);
                }
            }

            crawlDocument.incrementLinkAnalyzeCount();
            crawlDocument.setContent("");
        }
    }

}