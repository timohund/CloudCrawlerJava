package cloudcrawler.domain.crawler.robotstxt.parser;

import cloudcrawler.domain.crawler.robotstxt.rules.BaseRobotRules;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

/**
 * Testcase to check if the robots txt can be parsed as expected.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */
public class SimpleRobotRulesParserTest {

    protected SimpleRobotRulesParser parser;

    @Before
    public void setUp() {
        parser = new SimpleRobotRulesParser();
    }

    @Test
    public void testParser() throws UnsupportedEncodingException {
        String url          = new String("http://www.test.de/foobar/");
        String content      = new String(
                                    "User-agent: Slurp\n" +
                                    "Disallow: /\n"
                                );

            //Slurp should be rejected
        BaseRobotRules rules = parser.parseContent(url,content.getBytes("UTF-8"),"text/plain","Slurp");
        Assert.assertFalse(rules.isAllowed(url));

            //But erich should be allowed
        BaseRobotRules rules2 = parser.parseContent(url,content.getBytes("UTF-8"),"text/plain","Erich");
        Assert.assertTrue(rules2.isAllowed(url));
    }
}
