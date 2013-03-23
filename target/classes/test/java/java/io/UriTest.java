package java.io;


import junit.framework.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class UriTest {

    @Test
    public void canNormalize() {

        try {
            URI inputResult      = new URI(new String("http://www.google.de/foo/bar/../test.html"));
            URI expectedResult   = new URI(new String("http://www.google.de/foo/test.html"));

            Assert.assertEquals(inputResult.toString(), expectedResult.toString());

        } catch (URISyntaxException e) {

        }
    }
}
