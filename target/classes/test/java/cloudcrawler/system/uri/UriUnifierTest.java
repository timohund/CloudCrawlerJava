package cloudcrawler.system.uri;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class UriUnifierTest {

    private String href;

    private String itemHref;

    private String baseHref;

    private String expectedResultHref;

    private URIUnifier uriUnifier = new URIUnifier();

    public UriUnifierTest(String href, String itemHref, String baseHref, String expectedResultHref) {
        this.href = href;
        this.itemHref = itemHref;
        this.baseHref = baseHref;
        this.expectedResultHref = expectedResultHref;
    }

    @Parameterized.Parameters
    public static Collection urisToUnify() {
        return Arrays.asList(new Object[][]{
                {"http://www.google.de/", "http://www.google.de/", "http://www.google.de/", "http://www.google.de/"},
                {"./one.html", "http://www.test.de/", "", "http://www.test.de/one.html"},
                {"one.html", "http://www.test.de/", "", "http://www.test.de/one.html"},
                {"../one.html", "http://www.test.de/test/", "", "http://www.test.de/one.html"},
                {"./one.html", "http://www.test.de/foo/", "", "http://www.test.de/foo/one.html"},
                {"one.html", "http://www.test.de/foo/bar/cola", "http://www.test.de/foo/", "http://www.test.de/foo/one.html"},
                {"//www.heise.de/foo?bar=bar@test", "http://www.test.de/foo/bar/cola", "", "http://www.heise.de/foo?bar=bar@test"},
                {"http://www.heise.de","","","http://www.heise.de/"},
                {"/one.html","http://www.test.de/foo/bar/d;p?q","","http://www.test.de/one.html"},
                {"/one.html","http://www.test.de/foo/bar/cola","http://www.test.de/foo/","http://www.test.de/one.html"},
                {"/one.html","Http://wwW.test.de/foo/bar/cola","hTTp://WwW.test.de/foo/","http://www.test.de/one.html"},

        });
    }

    @Test
    public void canNormalize() throws URISyntaxException {
        URI hrefUri = new URI(this.href);
        URI itemUri = new URI(this.itemHref);
        URI baseUri = new URI(this.baseHref);

        URI result  = uriUnifier.unifiy(hrefUri,itemUri,baseUri);
        Assert.assertEquals(this.expectedResultHref, result.toString());
    }
}
