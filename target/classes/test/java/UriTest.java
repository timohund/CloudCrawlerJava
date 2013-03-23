import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class UriTest {

    private final String href;

    private final String itemHref;

    private final String baseHref;

    private final String expectedResultHref;

    public UriTest(String href, String itemHref, String baseHref, String expectedResultHref) {
        this.href = href;
        this.itemHref = itemHref;
        this.baseHref = baseHref;
        this.expectedResultHref = expectedResultHref;
    }

    @Parameterized.Parameters
    public static Collection primeNumbers() {
        return Arrays.asList(new Object[][]{
                {"http://www.google.de/", "http://www.google.de/", "http://www.google.de/", "http://www.google.de/"},
                {"./one.html", "http://www.test.de/", "", "http://www.test.de/one.html"},
                {"one.html", "http://www.test.de/", "", "http://www.test.de/one.html"},
                {"../one.html", "http://www.test.de/test/", "", "http://www.test.de/one.html"},
                {"./one.html", "http://www.test.de/foo/", "", "http://www.test.de/foo/one.html"},
                {"one.html", "http://www.test.de/foo/bar/cola", "http://www.test.de/foo/", "http://www.test.de/foo/one.html"},
                {"//www.heise.de/foo?bar=bar@test", "http://www.test.de/foo/bar/cola", "", "http://www.heise.de/foo?bar=bar@test"}
        });
    }

    @Test
    public void canNormalize() throws URISyntaxException {

        URI result = new URI("");
        URI hrefUri = new URI(this.href);

        if (hrefUri.isAbsolute()) {
            if (itemHref.trim().isEmpty()) {
                if (this.baseHref.trim().isEmpty()) {
                } else {
                    hrefUri.resolve(this.baseHref);
                }
            } else {
                hrefUri.resolve(this.itemHref);
            }
            result = hrefUri;
        } else {
            if (this.baseHref.trim().isEmpty()) {
                if (itemHref.trim().isEmpty()) {
                } else {
                    result = new URI(this.itemHref);
                    result = result.resolve(hrefUri);
                }
            } else {
                result = new URI(this.baseHref);
                result = result.resolve(hrefUri);
            }
        }

        result = result.normalize();

        Assert.assertEquals(this.expectedResultHref, result.toString());

    }
}
