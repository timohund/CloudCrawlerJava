package org.cloudcrawler.system.charset.conversion;

import com.ibm.icu.text.CharsetDetector;
import junit.framework.Assert;
import org.cloudcrawler.AbstractTest;
import org.cloudcrawler.system.charset.converter.ConversionResult;
import org.cloudcrawler.system.charset.converter.ConversionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;


@RunWith(Parameterized.class)
public class HtmlConversionServiceTest extends AbstractTest {

    protected String fixtureName = "";

    protected boolean conversionExpected = false;

    protected ConversionService converter;

    public HtmlConversionServiceTest(String fixtureName, Boolean conversionExpected) throws URISyntaxException {
        this.fixtureName = fixtureName;
        this.conversionExpected = conversionExpected;

        this.converter = new ConversionService(new CharsetDetector());
    }


    @Parameterized.Parameters
    public static Collection charsetConversionTestFiles() {
        return Arrays.asList(new Object[][]{
            {"test_utf8.html", false},
        });
    }

    @Test
    public void htmlConversionTest() throws IOException {
        File file               = new File("src/test/fixtures/websites/"+this.fixtureName);
        InputStream is          = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(is);
        ConversionResult result = this.converter.convertToUTF8(bis);

        Assert.assertEquals(this.conversionExpected,result.getWasConverted());
        Assert.assertTrue(result.getContent().contains("Bücher zum Thema PHP"));

    }

}