package cloudcrawler.domain.crawler;

import cloudcrawler.domain.crawler.contentparser.XHTMLContentParser;
import cloudcrawler.domain.crawler.robotstxt.RobotsTxtService;
import cloudcrawler.system.charset.converter.ConversionService;
import cloudcrawler.system.http.HttpService;
import cloudcrawler.system.stream.SizeValidator;
import cloudcrawler.system.uri.URIUnifier;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Vector;

import static org.easymock.EasyMock.*;

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
        httpServiceMock         = EasyMock.createMock(HttpService.class);
        uriUnifier              = EasyMock.createMock(URIUnifier.class);
        xHTMLParserMock         = EasyMock.createMock(XHTMLContentParser.class);
        robotsTxtServiceMock    = EasyMock.createMock(RobotsTxtService.class);
        conversionServiceMock   = EasyMock.createMock(ConversionService.class);
        sizeValidatorMock       = EasyMock.createMock(SizeValidator.class);

        service = new Service(httpServiceMock, uriUnifier, xHTMLParserMock, robotsTxtServiceMock, conversionServiceMock, sizeValidatorMock);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void serviceIsNotReturningAnyResultsWhenHeadIsIndicatingMpeg() throws Exception {
        Document document = new Document();
        document.setUri(new URI("http://www.google.de/"));

            //when the head request is indicating an mpeg there should never be a get request triggered
        expect(httpServiceMock.getUriWithGet(isA(URI.class))).andThrow(new AssertionFailedError()).anyTimes();
            //since we do not want to test the robots txt service here we mock it with allways allow
        expect(robotsTxtServiceMock.isAllowedUri(isA(URI.class))).andReturn(true).anyTimes();

        HttpResponse httpResponseMock   = EasyMock.createMock(HttpResponse.class);
        Header httpHeaderMock           = EasyMock.createMock(Header.class);

        expect(httpServiceMock.getUrlWithHead(isA(URI.class))).andReturn(httpResponseMock);
        expect(httpResponseMock.getLastHeader(isA(String.class))).andReturn(httpHeaderMock);
        expect(httpHeaderMock.getValue()).andReturn("mpeg");
        expect(httpServiceMock.close(isA(HttpResponse.class))).andReturn(true);
        expect(httpServiceMock.reset()).andReturn(true);

        replay(httpServiceMock);
        replay(robotsTxtServiceMock);
        replay(httpResponseMock);
        replay(httpHeaderMock);

        Vector<Document> documents =  service.crawlAndFollowLinks(document);

        verify(httpServiceMock);
        verify(robotsTxtServiceMock);
        verify(httpResponseMock);
        verify(httpHeaderMock);

        //we expect an empty collection will be return since an unallowed mimetype was requested
        Assert.assertEquals(documents.size(),0);
    }

    public void serviceIsNotReturningAnyResultsWhenGetIsIndicatingPdf() {

    }

    public void serviceIsNotCrawlingPageWhenRejectedByRobotsTxtService() {

    }

    public void serviceIsResettingHttpClientWhenExceptionIsThrown() {

    }
}
