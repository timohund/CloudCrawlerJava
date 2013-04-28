package org.cloudcrawler.domain.validator;

import org.apache.hadoop.conf.Configuration;
import org.easymock.EasyMock;
import org.eclipse.jdt.internal.core.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.easymock.EasyMock.*;

/**
 * Testcase to check if URIs can be validated against configured
 * allow patterns.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */
public class URIValidatorTest {

    @Test
    public void canValidate() throws URISyntaxException {
        URI     allowedUri      = new URI("http://www.test.de/foo");

        Configuration confMock = EasyMock.createMock(Configuration.class);
        expect(confMock.get("urivalidator.scheme.allowpattern",".*")).andReturn(".*");
        expect(confMock.get("urivalidator.host.allowpattern",".*")).andReturn(".*de.*");
        expect(confMock.get("urivalidator.path.allowpattern",".*")).andReturn(".*");
        expect(confMock.get("urivalidator.query.allowpattern",".*")).andReturn("");
        expect(confMock.get("urivalidator.fragment.allowpattern",".*")).andReturn("");

        replay(confMock);

        URIValidator validator = new URIValidator(confMock);
        boolean isValid = validator.isValid(allowedUri);
        Assert.isTrue(isValid);

        verify(confMock);
    }
}
