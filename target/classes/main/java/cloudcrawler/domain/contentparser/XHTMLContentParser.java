package cloudcrawler.domain.contentparser;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;


/**
 * Accessor class to retrieve content from an xhtml document.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */
public class XHTMLContentParser extends XMLContentParser {

    /**
     * Retrieves the external links of an html document.
     *
     * @return
     * @throws XPathExpressionException
     * @throws URISyntaxException
     */
    public Vector<URI> getExternalLinkUris() throws XPathExpressionException, URISyntaxException {
        Vector<URI> linkCollection      = new Vector<URI>() ;

        XPath xpath                     = xPathFactory.newXPath();
        XPathExpression ex              = xpath.compile("//a");
        Object result                   = ex.evaluate(this.domDocument, XPathConstants.NODESET);
        NodeList nodes                  = (NodeList) result;

        for (int i = 0; i < nodes.getLength(); i++) {
            Node aNode          = nodes.item(i);
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
        URI baseUri                     = new URI("");
        XPath xpath                     = xPathFactory.newXPath();
        XPathExpression ex              = xpath.compile("//base");
        Object result                   = ex.evaluate(this.domDocument, XPathConstants.NODESET);
        NodeList nodes                  = (NodeList) result;

        if(nodes.getLength() > 0) {
            Node firstNode  = nodes.item(0);
            baseUri         = this.getUriFromHrefNode(firstNode);
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
        Node aHrefAttribute = aNode.getAttributes().getNamedItem("href");
        String aHref        = aHrefAttribute.getNodeValue().toString();
        return new URI(aHref);
    }
}
