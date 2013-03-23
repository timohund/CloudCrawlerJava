package cloudcrawler.domain.contentparser;

import junit.framework.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 23.03.13
 * Time: 16:45
 * To change this template use File | Settings | File Templates.
 */
public class XHTMLContentParserTest {


    @Test
    public void simpleTest() throws URISyntaxException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        String testContent = new String(
                "<html>" +
                "   <head>" +
                "       <title>simpletest</title>" +
                "       <base href='http://www.google.de/'/>" +
                "  </head>" +
                "   <body>" +
                "       <a href='foo.html'>bla</a>    "+
                "   </body>"+
                "</html>");

        URI pageUri     = new URI("http://www.google.de/");
        String mimeType = new String("text/html");

        XHTMLContentParser parser = new XHTMLContentParser();
        parser.initialize(pageUri,testContent,mimeType);

            //can we get the base href uri?
        Assert.assertEquals("http://www.google.de/",parser.getBaseHrefUri().toString());

            //can we extract the links?
        Assert.assertEquals("foo.html",parser.getExternalLinkUris().get(0).toString());
    }
}
