package org.cloudcrawler.system.charset.converter;

import com.google.inject.Inject;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * RobotsTxtService class to convert an input stream to an utf8 output string.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */
public class ConversionService {

    protected CharsetDetector charsetDetector;

    @Inject
    public ConversionService(CharsetDetector charsetDetector) {
        this.charsetDetector = charsetDetector;
    }

    public ConversionResult convertToUTF8(BufferedInputStream bis) throws IOException {
        ConversionResult result     = new ConversionResult();


        charsetDetector.setText(bis);

        CharsetMatch match  = charsetDetector.detect();
        String detectedCharset = match.getName();

        if (!detectedCharset.equals("UTF-8")) {
            byte[] bytes = IOUtils.toByteArray(bis);
            byte[] utf8 = new String(bytes, match.getName()).getBytes("UTF-8");
            result.setContent(new String(utf8, "UTF-8"));
            result.setWasConverted(true);
        } else {
            result.setContent(IOUtils.toString(bis));
        }

        return result;
    }
}
