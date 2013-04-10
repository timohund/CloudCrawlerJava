package cloudcrawler.domain.crawler;

import cloudcrawler.domain.crawler.contentparser.XHTMLContentParser;
import cloudcrawler.domain.crawler.robotstxt.RobotsTxtService;
import cloudcrawler.system.charset.converter.ConversionService;
import cloudcrawler.system.http.HttpService;
import cloudcrawler.system.stream.SizeValidator;
import cloudcrawler.system.uri.URIUnifier;
import org.junit.Before;

/**
 *
 */
public class ServiceTest {

    protected Service service;

    protected HttpService httpServiceMock;

    protected URIUnifier uriUnifier;

    protected XHTMLContentParser xHTMLParserMock;

    protected RobotsTxtService robotsTxtServiceMock;

    protected ConversionService conversionServiceMock;

    protected SizeValidator sizeValidatorMock;

    @Before
    public void setUp() {
        service = new Service(httpServiceMock, uriUnifier, xHTMLParserMock, robotsTxtServiceMock, conversionServiceMock, sizeValidatorMock);
    }

    public void serviceIsNotReturningAnyResultsWhenHeadIsIndicatingMpeg() {

    }

    public void serviceIsNotReturningAnyResultsWhenGetIsIndicatingPdf() {

    }

    public void serviceIsNotCrawlingPageWhenRejectedByRobotsTxtService() {

    }
}
