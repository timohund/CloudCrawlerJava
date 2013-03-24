package cloudcrawler.mapreduce;

import cloudcrawler.domain.CrawlDocument;
import cloudcrawler.domain.contentparser.XHTMLContentParser;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URI;
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
                    String json = val.toString();
                    CrawlDocument crawlDocument = gson.fromJson(json,CrawlDocument.class);

                    result = new Text(crawlDocument.getContent());
                    int analyzeCount = crawlDocument.getLinkAnalyzeCount();
                    if(analyzeCount == 0) {
                        String content = crawlDocument.getContent();

                        XHTMLContentParser xHTMLParser = new XHTMLContentParser();
                        xHTMLParser.initialize(sourceURI,crawlDocument.getContent(),crawlDocument.getMimeType());
                        Vector<URI> uris = xHTMLParser.getExternalLinkUris();

                        for(URI linkUri : uris) {

                        }
                        context.write(key, new Text(gson.toJson(result)));

                   //     this.merge(crawlDocument);
                    } else {
                   //     this.merge(crawlDocument);
                    }

                } catch (JsonIOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (SAXParseException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (XPathExpressionException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                context.progress();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }


    }
}
