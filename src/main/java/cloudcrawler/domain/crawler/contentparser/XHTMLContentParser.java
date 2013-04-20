package cloudcrawler.domain.crawler.contentparser;

import cloudcrawler.domain.crawler.Link;
import cloudcrawler.system.uri.URIHelper;
import cloudcrawler.system.uri.URIUnifier;
import com.google.inject.Inject;
import nu.validator.htmlparser.common.Heuristics;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
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
public class XHTMLContentParser {

    protected Document document;

    protected String secondLevelHostName = "";

    protected URIUnifier uriUni;

    protected URI   sourceUri;

    protected String sourceContent;

    protected String mimeType;

    @Inject
    public void setURIUnifier(URIUnifier uriUni) {
        this.uriUni = uriUni;
    }

    /**
     * @param sourceUri
     * @param sourceContent
     * @param mimeType
     */
    public void initialize(URI sourceUri, String sourceContent, String mimeType) throws IOException, SAXException, ParserConfigurationException {
        this.sourceUri = sourceUri;
        this.sourceContent = sourceContent;
        this.mimeType = mimeType;

        this.afterInitialize();
    }

    protected void afterInitialize() throws ParserConfigurationException, IOException, SAXException {
        InputStream stream = IOUtils.toInputStream(this.sourceContent);

        // Use the TagSoup parser to build an XOM document from HTML
        HtmlDocumentBuilder docBuilder = new HtmlDocumentBuilder();

        docBuilder.setReportingDoctype(true);
        docBuilder.setHtml4ModeCompatibleWithXhtml1Schemata(true);
        docBuilder.setHeuristics(Heuristics.NONE);

        InputSource in = new InputSource(stream);
        in.setEncoding("UTF-8");

        secondLevelHostName = URIHelper.getSecondLevelHostName(this.sourceUri);

        document = docBuilder.parse(in);
    }

    /**
     * Retrieves the external links of an html document.
     *
     * @return
     * @throws XPathExpressionException
     * @throws URISyntaxException
     */
    public Vector<Link> getOutgoingLinks(boolean onlyOtherDomains) throws XPathExpressionException, URISyntaxException {
        Vector<Link> linkCollection = new Vector<Link>() ;

        NodeList nodes = document.getElementsByTagName("a");
        URI baseUri = this.getBaseHrefUri();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node aNode = nodes.item(i);
            URI aHrefUri   = getUriFromHrefNode(aNode);
            URI unifiedUri = this.getUnifiedFilteredUri(aHrefUri,baseUri);

            if(unifiedUri == null) {
                continue;
            }

            if(onlyOtherDomains) {
                    //when the link points to the same second level domain, we skip it
                String targetSecondLevelHost = URIHelper.getSecondLevelHostName(unifiedUri);
                if(targetSecondLevelHost.equals(secondLevelHostName)) {
                    continue;
                }
            }

            String linkText = aNode.getTextContent().trim();
            Link link = new Link();

            link.setTargetUri(unifiedUri);
            link.setSourceUri(this.sourceUri);

            if(linkText.length() > 140) {
                link.setText(new String(linkText.substring(0,140)));
            } else {
                link.setText(linkText);
            }

            linkCollection.addElement(link);
        }

        return linkCollection;
    }

    protected URI getUnifiedFilteredUri(URI aHrefUri, URI baseUri) throws URISyntaxException {
        URI unifiedUri = this.uriUni.unifiy(aHrefUri, this.sourceUri, baseUri);

        if(unifiedUri == null) {
            return null;
        }

        //todo remove query and fragment here, make it more flexible
        URIBuilder builder = new URIBuilder(unifiedUri);
        builder.setQuery(null);
        builder.setFragment(null);
        unifiedUri = builder.build();

        return unifiedUri;
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

    /**
     * Returns the title of the document.
     *
     * @return
     */
    public String getTitle() {
        String title = new String("");

        NodeList titleNodes = document.getElementsByTagName("title");
        if(titleNodes.getLength() > 0) {
            try {
                Node firstNode = titleNodes.item(0);
                title = firstNode.getTextContent().trim();
            } catch(NullPointerException e) {

            }
        }


        return title;
    }
}
