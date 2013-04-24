package org.cloudcrawler.system.charset.converter;

/**
 *
 *
 */
public class ConversionResult {

    protected boolean wasConverted = false;

    protected String content = "";

    public boolean getWasConverted() {
        return wasConverted;
    }

    public void setWasConverted(boolean wasConverted) {
        this.wasConverted = wasConverted;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
