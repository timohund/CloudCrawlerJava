package cloudcrawler.system.charset.conversion;

import cloudcrawler.AbstractTest;
import cloudcrawler.system.charset.converter.ConversionResult;
import cloudcrawler.system.charset.converter.ConversionService;
import com.ibm.icu.text.CharsetDetector;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;


@RunWith(Parameterized.class)
public class SimpleConversionServiceTest extends AbstractTest {

    protected String fixtureName = "";

    protected boolean conversionExpected = false;

    protected ConversionService converter;

    public SimpleConversionServiceTest(String fixtureName, Boolean conversionExpected) throws URISyntaxException {
        this.fixtureName = fixtureName;
        this.conversionExpected = conversionExpected;

        this.converter = new ConversionService(new CharsetDetector());
    }


    @Parameterized.Parameters
    public static Collection charsetConversionTestFiles() {
        return Arrays.asList(new Object[][]{
            {"iso8859_1.txt", true},
            {"utf8.txt", false},
            {"windows-1250.txt", true},
        });
    }

    @Test
    public void simpleConversionTest() throws IOException {
        File file               = new File("src/test/fixtures/files/simpletest/"+this.fixtureName);
        InputStream is          = new FileInputStream(file);
        ConversionResult result = this.converter.convertToUTF8(is);

        Assert.assertEquals(this.conversionExpected,result.getWasConverted());
        Assert.assertEquals("Straßenüberquerung!",result.getContent());
    }

}