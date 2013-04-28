package org.cloudcrawler.system.stream;

import org.cloudcrawler.AbstractTest;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;


@RunWith(Parameterized.class)
public class SizeValidatorTest extends AbstractTest {

    protected String fixtureName = "";

    protected boolean exceptionExpected = false;

    protected SizeValidator sizeValidator;

    public SizeValidatorTest(String fixtureName, Boolean exceptionExpected) throws URISyntaxException {
        this.fixtureName = fixtureName;
        this.exceptionExpected = exceptionExpected;

        this.sizeValidator = new SizeValidator();
    }


    @Parameterized.Parameters
    public static Collection charsetConversionTestFiles() {
        return Arrays.asList(new Object[][]{
                {"small.txt", true},
                {"large.txt", false},
        });
    }

    @Test
    public void simpleConversionTest() throws IOException {
        File file               = new File("src/test/fixtures/files/filesize/"+this.fixtureName);
        InputStream is          = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(is);
        boolean isValid         = this.sizeValidator.isValid(bis, 1024 * 1024);

        Assert.assertEquals(isValid,exceptionExpected);
    }

}