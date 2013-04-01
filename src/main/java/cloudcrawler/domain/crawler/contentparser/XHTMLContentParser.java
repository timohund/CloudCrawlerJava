package cloudcrawler.domain.crawler.contentparser;

import cloudcrawler.domain.crawler.Link;
import nu.validator.htmlparser.common.Heuristics;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
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

    protected Document document;



    @Override
    protected void afterInitialize() throws ParserConfigurationException, IOException, SAXException {
        InputStream stream = new ByteArrayInputStream(this.sourceContent.getBytes("UTF-8"));

        // Use the TagSoup parser to build an XOM document from HTML
        HtmlDocumentBuilder docBuilder = new HtmlDocumentBuilder();
        docBuilder.setReportingDoctype(true);
        docBuilder.setHtml4ModeCompatibleWithXhtml1Schemata(true);
        docBuilder.setHeuristics(Heuristics.ALL);
        document = docBuilder.parse(stream);
    }
    /**
     * Retrieves the external links of an html document.
     *
     * @return
     * @throws XPathExpressionException
     * @throws URISyntaxException
     */
    public Vector<Link> getExternalLinkUris() throws XPathExpressionException, URISyntaxException {
        Vector<Link> linkCollection = new Vector<Link>() ;

        NodeList nodes = document.getElementsByTagName("a");

        for (int i = 0; i < nodes.getLength(); i++) {
            Node aNode = nodes.item(i);
            URI aHrefUri    = getUriFromHrefNode(aNode);
            String linkText = aNode.getTextContent();
            Link link = new Link();

            link.setTargetUri(aHrefUri);

            if(linkText.length() > 140) {
                link.setText(new String(linkText.substring(0,140)));
            } else {
                link.setText(linkText);
            }

            linkCollection.addElement(link);
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
        NodeList nodes = document.getElementsByTagName("base");

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
