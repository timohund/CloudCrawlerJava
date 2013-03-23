package cloudcrawler.domain.contentparser;

import org.apache.tools.ant.filters.StringInputStream;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 23.03.13
 * Time: 17:07
 * To change this template use File | Settings | File Templates.
 */
public class XMLContentParser extends AbstractContentParser{

    protected Document domDocument;

    protected XPathFactory xPathFactory;

    @Override
    protected void afterInitialize() throws ParserConfigurationException, IOException, SAXException {
        InputStream is                      = new StringInputStream(this.sourceContent);
        DocumentBuilderFactory dbFactory    = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder            = null;

        dBuilder                            = dbFactory.newDocumentBuilder();
        domDocument                         = dBuilder.parse(is);
        xPathFactory                        = XPathFactory.newInstance();
    }
}
