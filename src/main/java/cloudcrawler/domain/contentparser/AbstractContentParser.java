package cloudcrawler.domain.contentparser;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;

abstract public class AbstractContentParser {

    protected URI   sourceUri;

    protected String sourceContent;

    protected String mimeType;


    /**
     * @param sourceUrl
     * @param sourceContent
     * @param mimeType
     */
    public void initialize(URI sourceUrl, String sourceContent, String mimeType) throws IOException, SAXException, ParserConfigurationException {
        this.sourceUri = sourceUri;
        this.sourceContent = sourceContent;
        this.mimeType = mimeType;

        this.afterInitialize();
    }

    protected abstract void afterInitialize() throws ParserConfigurationException, IOException, SAXException;

}
