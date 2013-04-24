package org.cloudcrawler.domain.crawler;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 30.03.13
 * Time: 11:09
 * To change this template use File | Settings | File Templates.
 */
public class Link {

    URI targetUri;

    URI sourceUri;

    String text = "";

    public URI getTargetUri() {
        return targetUri;
    }

    public void setTargetUri(URI targetUri) {
        this.targetUri = targetUri;
    }

    public URI getSourceUri() {
        return sourceUri;
    }

    public void setSourceUri(URI sourceUri) {
        this.sourceUri = sourceUri;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}