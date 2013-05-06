package org.cloudcrawler.domain.crawler;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.cloudcrawler.domain.crawler.contentparser.XHTMLContentParser;
import org.cloudcrawler.domain.crawler.robotstxt.RobotsTxtService;
import org.cloudcrawler.system.charset.converter.ConversionService;
import org.cloudcrawler.system.http.HttpService;
import org.cloudcrawler.system.stream.SizeValidator;
import org.cloudcrawler.system.uri.URIValidator;
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

    protected XHTMLContentParser xHTMLParserMock;

    protected RobotsTxtService robotsTxtServiceMock;

    protected ConversionService conversionServiceMock;

    protected SizeValidator sizeValidatorMock;

    protected URIValidator uriValidatorMock;

    @Before
    public void setUp() {
        httpServiceMock         = EasyMock.createMock(HttpService.class);
        xHTMLParserMock         = EasyMock.createMock(XHTMLContentParser.class);
        robotsTxtServiceMock    = EasyMock.createMock(RobotsTxtService.class);
        conversionServiceMock   = EasyMock.createMock(ConversionService.class);
        sizeValidatorMock       = EasyMock.createMock(SizeValidator.class);
        uriValidatorMock        = EasyMock.createMock(URIValidator.class);

        service = new Service(httpServiceMock, xHTMLParserMock, robotsTxtServiceMock, conversionServiceMock, sizeValidatorMock, uriValidatorMock);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void serviceIsNotReturningAnyResultsWhenHeadIsIndicatingMpeg() throws Exception {
        Document document = new Document();
        document.setUri(new URI("http://www.google.de/"));

        expect(uriValidatorMock.isValid(isA(URI.class))).andReturn(true).anyTimes();

        //when the head request is indicating an mpeg there should never be a get request triggered
        expect(httpServiceMock.get(isA(URI.class))).andThrow(new AssertionFailedError()).anyTimes();
            //since we do not want to test the robots txt service here we mock it with allways allow
        expect(robotsTxtServiceMock.isAllowedUri(isA(URI.class))).andReturn(true).anyTimes();


        HttpResponse httpResponseMock   = EasyMock.createMock(HttpResponse.class);
        Header httpHeaderMock           = EasyMock.createMock(Header.class);

        expect(httpServiceMock.head(isA(URI.class))).andReturn(httpResponseMock);
        expect(httpResponseMock.getLastHeader(isA(String.class))).andReturn(httpHeaderMock);
        expect(httpHeaderMock.getValue()).andReturn("mpeg");
        expect(httpServiceMock.close(isA(HttpResponse.class))).andReturn(true);
        expect(httpServiceMock.reset()).andReturn(true);

        replay(uriValidatorMock);
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

    /**
     * This Testcase should check that the document will not we crawled
     * When:
     *      * The head is indicating correct mimetype
     *      * But the concrete get request is returing a forbidden mimetyoe
     * @throws Exception
     */
    @Test
    public void serviceIsNotReturningAnyResultsWhenGetIsIndicatingPdf() throws Exception {
        Document document = new Document();
        document.setUri(new URI("http://www.google.de/"));

        expect(uriValidatorMock.isValid(isA(URI.class))).andReturn(true).anyTimes();

        //since we do not want to test the robots txt service here we mock it with allways allow
        expect(robotsTxtServiceMock.isAllowedUri(isA(URI.class))).andReturn(true).anyTimes();

        HttpResponse httpResponseMock   = EasyMock.createMock(HttpResponse.class);
        Header httpHeaderMock           = EasyMock.createMock(Header.class);

        expect(httpServiceMock.head(isA(URI.class))).andReturn(httpResponseMock);
        expect(httpResponseMock.getLastHeader(isA(String.class))).andReturn(httpHeaderMock);
        expect(httpHeaderMock.getValue()).andReturn("text/html");
        expect(httpServiceMock.close(isA(HttpResponse.class))).andReturn(true);

            //now the get request
        expect(httpServiceMock.get(isA(URI.class))).andReturn(httpResponseMock);
        expect(httpResponseMock.getLastHeader(isA(String.class))).andReturn(httpHeaderMock);
        expect(httpHeaderMock.getValue()).andReturn("appclication/pdf");
        expect(httpServiceMock.reset()).andReturn(true);

        replay(uriValidatorMock);
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

    public void serviceIsNotCrawlingPageWhenRejectedByRobotsTxtService() {

    }

    public void serviceIsResettingHttpClientWhenExceptionIsThrown() {

    }
}
