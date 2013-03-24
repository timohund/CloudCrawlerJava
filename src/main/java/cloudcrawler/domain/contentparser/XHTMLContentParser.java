package cloudcrawler.domain.contentparser;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;


/**
 * Accessor class to retrieve content from an xhtml document.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */
public class XHTMLContentParser extends XMLContentParser {

    protected Document domDocument;

    protected XPathFactory xPathFactory;

    @Override
    protected void afterInitialize() throws ParserConfigurationException, IOException, SAXException {
        DOMParser parser    = new DOMParser();

        parser.setProperty("http://cyberneko.org/html/properties/default-encoding", "UTF-8");
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        parser.setProperty("http://cyberneko.org/html/properties/names/attrs","lower");

        parser.setFeature("http://xml.org/sax/features/validation", false);
        parser.setFeature("http://cyberneko.org/html/features/balance-tags", false);

        InputSource is = new InputSource();
        is.setSystemId(sourceUri.toString());

        InputStream stream = new ByteArrayInputStream(this.sourceContent.getBytes("UTF-8"));
        is.setByteStream(stream);

        parser.parse(is);

        domDocument = parser.getDocument();
        xPathFactory = XPathFactory.newInstance();
    }

    /**
     * Retrieves the external links of an html document.
     *
     * @return
     * @throws XPathExpressionException
     * @throws URISyntaxException
     */
    public Vector<URI> getExternalLinkUris() throws XPathExpressionException, URISyntaxException {
        Vector<URI> linkCollection = new Vector<URI>() ;

        XPath xpath = xPathFactory.newXPath();
        XPathExpression ex = xpath.compile("//A");
        Object result = ex.evaluate(this.domDocument, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        for (int i = 0; i < nodes.getLength(); i++) {
            Node aNode = nodes.item(i);
            URI aHrefUri = getUriFromHrefNode(aNode);

            linkCollection.addElement(aHrefUri);
        }

        return linkCollection;
    }


    /**
     * Returns the base href of the html document.
     *
     * @return
     * @throws XPathExpressionException
     * @throws URISyntaxException
     */
    public URI getBaseHrefUri() throws XPathExpressionException, URISyntaxException {
        URI baseUri = new URI("");
        XPath xpath = xPathFactory.newXPath();
        XPathExpression ex = xpath.compile("//BASE");
        Object result = ex.evaluate(this.domDocument, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;

        if(nodes.getLength() > 0) {
            Node firstNode = nodes.item(0);
            baseUri = this.getUriFromHrefNode(firstNode);
        }

        return baseUri;
    }

    /**
     * Helper method to extract the uri from a node with an href attribute.
     *
     * @param aNode
     * @return
     * @throws URISyntaxException
     */
    private URI getUriFromHrefNode(Node aNode) throws URISyntaxException {
        URI aHref = new URI("");
        try {
            Node aHrefAttribute = aNode.getAttributes().getNamedItem("href");
            String aHrefString = aHrefAttribute.getNodeValue().toString();
            aHref = new URI(aHrefString);
        } catch (NullPointerException e) {
            //can happen
        } catch (URISyntaxException e) {
            //can happen
        }
        return aHref;
    }
}
