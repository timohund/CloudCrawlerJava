package cloudcrawler.mapreduce;

import cloudcrawler.domain.CrawlDocument;
import cloudcrawler.domain.contentparser.XHTMLContentParser;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

public class LinkFollowReducer extends Reducer<Text, Text, Text, Text> {

    private Text result = new Text();

    private Gson gson = new Gson();

    protected CrawlDocument merge(CrawlDocument crawlDocument) {
       return crawlDocument;
    }

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

        try {
            URI sourceURI = new URI(key.toString());

            for (Text val : values) {
                try {
                    String json = Base64.decodeBase64(val.toString()).toString();
                    CrawlDocument crawlDocument = gson.fromJson(json,CrawlDocument.class);

                    if(crawlDocument.getLinkAnalyzeCount() == 0) {
                        String content = crawlDocument.getContent();

                        XHTMLContentParser xHTMLParser = new XHTMLContentParser();
                        xHTMLParser.initialize(sourceURI,crawlDocument.getContent(),crawlDocument.getMimeType());
                        Vector<URI> uris = xHTMLParser.getExternalLinkUris();

                        for(URI linkUri : uris) {

                        }
                        this.merge(crawlDocument);
                    } else {
                        this.merge(crawlDocument);
                    }

                } catch (ParserConfigurationException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (SAXException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (XPathExpressionException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                context.progress();
            }
        }catch (URISyntaxException e) {
            e.printStackTrace();
        }

        context.write(key, result);
    }
}
