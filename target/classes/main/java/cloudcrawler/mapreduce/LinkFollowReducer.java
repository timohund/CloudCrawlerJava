package cloudcrawler.mapreduce;

import cloudcrawler.domain.CrawlDocument;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.tools.ant.filters.StringInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;

public class LinkFollowReducer extends Reducer<Text, Text, Text, Text> {

    private Text result = new Text();

    private Gson gson = new Gson();

    protected CrawlDocument merge(CrawlDocument crawlDocument) {
       return crawlDocument;
    }

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

        for (Text val : values) {

            try {
                String json = Base64.decodeBase64(val.toString()).toString();
                CrawlDocument crawlDocument = gson.fromJson(json,CrawlDocument.class);

                if(crawlDocument.getLinkAnalyzeCount() == 0) {
                    String content = crawlDocument.getContent();
                    InputStream is = new StringInputStream(content);

                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = null;

                    dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(is);

                    XPathFactory xPathFactory   = XPathFactory.newInstance();
                    XPath xpath                 = xPathFactory.newXPath();

                    XPathExpression ex          = xpath.compile("//a");
                    Object result               = ex.evaluate(doc);

                    NodeList nodes              = (NodeList) result;
                    for (int i = 0; i < nodes.getLength(); i++) {

                        Node aNode          = nodes.item(i);
                        Node aHrefAttribute = aNode.getAttributes().getNamedItem("href");
                        String aHref        = aHrefAttribute.getNodeValue().toString();
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

        context.write(key, result);
    }
}
