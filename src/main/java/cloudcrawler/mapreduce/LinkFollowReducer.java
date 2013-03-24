package cloudcrawler.mapreduce;

import cloudcrawler.domain.CrawlDocument;
import cloudcrawler.domain.contentparser.XHTMLContentParser;
import cloudcrawler.system.uri.URIUnifier;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.http.client.utils.URIBuilder;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class LinkFollowReducer extends Reducer<Text, Text, Text, Text> {

    private Gson gson = new Gson();

    private HashMap<String,CrawlDocument> result = new HashMap<String, CrawlDocument>();

    private URIUnifier uriUni = new URIUnifier();

    /**
     *
     * @param key
     * @param crawlDocument
     */
    protected void merge(Text key, CrawlDocument crawlDocument) {
        CrawlDocument masterDocument = crawlDocument;
        if(result.containsKey(key.toString())) {
            CrawlDocument slaveDocument;

            CrawlDocument currentStoredResult = result.get(key.toString());
            if(currentStoredResult != null) {
               masterDocument = currentStoredResult;
               slaveDocument = crawlDocument;
            } else {
               slaveDocument = currentStoredResult;
            }


            HashMap<String,String> links =  slaveDocument.getIncomingLinks();

            if(links != null) {
                Iterator iterator = links.keySet().iterator();
                while(iterator.hasNext()) {
                    String linkKey = iterator.next().toString();
                    String linkToAdd = links.get(linkKey).toString();
                    // weiterverabeitung der Werte...
                    masterDocument.addIncomingLink(linkToAdd);
                }
            }
        }

        result.put(key.toString(), masterDocument);

    }

    protected void emitAll(Context context) throws IOException, InterruptedException {
        Iterator it = this.result.keySet().iterator();
        while(it.hasNext()) {
            String url = it.next().toString();
            CrawlDocument document = this.result.get(url);
            String documentJson = " "+gson.toJson(document);
            context.write(new Text(url),new Text(documentJson));
        }

        context.setStatus("done");
    }

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

        try {
            URI sourceURI = new URI(key.toString());

            for (Text val : values) {
                String json = val.toString();

                try {
                    CrawlDocument crawlDocument = gson.fromJson(json,CrawlDocument.class);

                    int analyzeCount = crawlDocument.getLinkAnalyzeCount();
                    if(analyzeCount == 0 ) {
                        String content = crawlDocument.getContent();

                        XHTMLContentParser xHTMLParser = new XHTMLContentParser();
                        xHTMLParser.initialize(sourceURI,crawlDocument.getContent(),crawlDocument.getMimeType());
                        URI baseURI = xHTMLParser.getBaseHrefUri();
                        Vector<URI> uris = xHTMLParser.getExternalLinkUris();

                        for(URI linkUri : uris) {
                            if(!(linkUri == null) && linkUri.toString().contains(".de") ) {

                                    //todo remove query and fragment here, make it more flexible
                                URI storedUri       = this.uriUni.unifiy(linkUri,sourceURI,baseURI);
                                URIBuilder builder  = new URIBuilder(storedUri);
                                builder.setQuery("");
                                builder.setFragment("");
                                storedUri           = builder.build();

                                System.out.println("Following Link "+storedUri);

                                crawlDocument.addIncomingLink(storedUri.toString());

                                    //create a new document from the followed link
                                CrawlDocument linkDocument = new CrawlDocument();
                                linkDocument.setUrl(storedUri.toString());
                                this.merge(new Text(storedUri.toString()),linkDocument);
                            }
                        }

                            //we don't need to content anymore
                        crawlDocument.setContent("");

                    }

                    this.merge(key,crawlDocument);

                } catch (JsonIOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (SAXParseException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (XPathExpressionException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (NullPointerException e) {
                    System.out.println("Error indexing "+key+" "+json+" ");
                    e.printStackTrace();
                }

                context.progress();
            }

            this.emitAll(context);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
