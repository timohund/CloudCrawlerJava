package org.cloudcrawler.system.uri;


import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

public class URIUnifier {

    public URIUnifier() {

    }
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

        String path = result.getRawPath();
        if(path == null || path.equals("")) {
            URIBuilder builder = new URIBuilder(result);
            builder.setPath("/");
            result = builder.build();
        }

        if(result.getHost() == null) {
           return null;
        }

            //make at leaste scheme and hostname lowercase
        URIBuilder builder = new URIBuilder(result);
        builder.setScheme(result.getScheme().toLowerCase());
        builder.setHost(result.getHost().toLowerCase());

        return builder.build();
    }
}
