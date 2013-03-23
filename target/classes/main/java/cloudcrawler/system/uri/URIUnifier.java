package cloudcrawler.system.uri;


import java.net.URI;
import java.net.URISyntaxException;

public class URIUnifier {

    /**
     * @param hrefUri
     * @param sourceUri
     * @param baseHrefUri
     * @return
     */
    public URI unifiy(URI hrefUri, URI sourceUri, URI baseHrefUri) throws URISyntaxException {
        URI result = new URI("");

        if (hrefUri.isAbsolute()) {
            if (sourceUri.toString().trim().isEmpty()) {
                if (baseHrefUri.toString().trim().isEmpty()) {
                } else {
                    hrefUri.resolve(baseHrefUri);
                }
            } else {
                hrefUri.resolve(sourceUri);
            }
            result = hrefUri;
        } else {
            if (baseHrefUri.toString().trim().isEmpty()) {
                if (sourceUri.toString().trim().isEmpty()) {
                } else {
                    result = sourceUri.resolve(hrefUri);
                }
            } else {
                result = baseHrefUri.resolve(hrefUri);
            }
        }

        result = result.normalize();

        return result;
    }
}
