package cloudcrawler.domain.contentparser;

import cloudcrawler.AbstractTest;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
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
public class XHTMLContentParserTest extends AbstractTest {

    @Test
    public void simpleTest() throws SAXException, ParserConfigurationException, IOException, XPathExpressionException, URISyntaxException {
        String testContent = new String(
                "<!DOCTYPE html>"+
                "<html>" +
                "   <head>" +
                "       <title>simpletest</title>" +
                "       <base href=\"http://www.google.de/\"/>" +
                "  </head>" +
                "   <body>" +
                "       <a href=\"http://www.google.de/foo.html\">bla</a>    "+
                "   </body>"+
                "</html>");

        URI pageUri     = new URI("http://www.google.de/");
        String mimeType = new String("text/html");

        XHTMLContentParser parser = new XHTMLContentParser();
   //     parser.setDomParser(new DOMParser());
        parser.initialize(pageUri,testContent,mimeType);

            //can we get the base href uri?
        Assert.assertEquals("http://www.google.de/", parser.getBaseHrefUri().toString());

            //can we extract the links?
        Assert.assertEquals("http://www.google.de/foo.html",parser.getExternalLinkUris().get(0).toString());
    }



    @Test
    public void xHTMLExtractionTest() throws IOException, URISyntaxException, ParserConfigurationException, SAXException, XPathExpressionException {
        File file = new File("src/test/fixtures/admin-wissen.de.html");
        String content = FileUtils.readFileToString(file);

        URI pageUri     = new URI("http://www.admin-wissen.de/");
        String mimeType = new String("text/html");

        XHTMLContentParser parser = new XHTMLContentParser();
        parser.initialize(pageUri,content,mimeType);

        String baseUrl  = parser.getBaseHrefUri().toString();

        Assert.assertEquals("http://www.admin-wissen.de/",baseUrl);
        Assert.assertTrue(parser.getExternalLinkUris().size() > 0);
    }

    @Test
    public void xHTMLExtractionTestMs() throws IOException, URISyntaxException, ParserConfigurationException, SAXException, XPathExpressionException {
        File file = new File("src/test/fixtures/admin-wissen.de.html");
        String content = FileUtils.readFileToString(file);

        URI pageUri     = new URI("http://www.microsoft.de/");
        String mimeType = new String("text/html");

        XHTMLContentParser parser = new XHTMLContentParser();
        parser.initialize(pageUri,content,mimeType);

        Assert.assertTrue(parser.getExternalLinkUris().size() > 0);
    }
}
