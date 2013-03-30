package cloudcrawler.domain.crawler.contentparser;

import cloudcrawler.AbstractTest;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;


@RunWith(Parameterized.class)
public class XHTMLContentParserTest extends AbstractTest {

    String fixtureName;

    URI fixtureUri;

    String expectedBaseHref;

    public XHTMLContentParserTest(String fixtureName, String fixtureUri, String expectedBaseHref) throws URISyntaxException {
        this.fixtureName = fixtureName;
        this.fixtureUri = new URI(fixtureUri);
        this.expectedBaseHref = expectedBaseHref;
    }


    @Parameterized.Parameters
    public static Collection urisToUnify() {
        return Arrays.asList(new Object[][]{
                {"simpleFixture.html", "", "http://www.google.de/"},
                {"admin-wissen.de.html", "http://www.admin-wissen.de/", "http://www.admin-wissen.de/"},
                {"microsoft.de.html", "http://www.microsoft.de/", ""},
                {"aldi.de.html", "http://www.aldi.de/", ""},


        });
    }

    @Test
    public void xHTMLExtractionTest() throws IOException, URISyntaxException, ParserConfigurationException, SAXException, XPathExpressionException {
        File file = new File("src/test/fixtures/"+this.fixtureName);
        String content = FileUtils.readFileToString(file);

        String mimeType = new String("text/html");

        XHTMLContentParser parser = new XHTMLContentParser();
        parser.initialize(this.fixtureUri,content,mimeType);

        String baseUrl  = parser.getBaseHrefUri().toString();

        Assert.assertEquals(this.expectedBaseHref,baseUrl);
        Assert.assertTrue(parser.getExternalLinkUris().size() > 0);
    }
}
