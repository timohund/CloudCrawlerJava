package cloudcrawler.mapreduce;

/**
 * Testcases to test the crawling mapper functionality.
 *
 * @author Timo Schmidt <timo.schmidt@gmx.net>
 */

import cloudcrawler.domain.crawler.Document;
import cloudcrawler.domain.crawler.Service;
import cloudcrawler.domain.crawler.message.DocumentMessage;
import com.google.gson.Gson;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.easymock.EasyMock.*;

public class CrawlingMapperTest {

    CrawlingMapper mapper;

    Mapper.Context contextMock;

    Gson gson;

    Service crawlingServiceMock;

    @Before
    public void setUp() {
        contextMock         = EasyMock.createMock(Mapper.Context.class);
        gson                = new Gson();
        crawlingServiceMock = EasyMock.createMock(Service.class);

        mapper = new CrawlingMapper(gson, crawlingServiceMock);
    }

    @Test
    public void documentCrawledWithErrorGetsIncrementedErrorCount() throws Exception {
            //only key and no json should trigger the fetching process of a document

            DocumentMessage expectedOutputMessage   = new DocumentMessage();
            Document document                       = new Document();
            expectedOutputMessage.setAttachment(document);
            document.incrementErrorCount();
            document.setUri(new URI("http://www.heise.de/"));
            document.setErrorMessage("test exception");

            Text keyOut     = new Text(document.getUri().toString());
            Text valueOut   = new Text(gson.toJson(expectedOutputMessage));
            contextMock.write(keyOut, valueOut);

                //we expect that the crawling service is triggered to crawl one document
                //and follow the links
            expect(crawlingServiceMock.crawlAndFollowLinks(isA(Document.class))).andThrow(new Exception(
                "test exception"
            ));

        replay(crawlingServiceMock);
        replay(contextMock);

            Text key     = new Text(document.getUri().toString());
            Text value   = new Text();
            mapper.map(key,value, contextMock);

        verify(crawlingServiceMock);
        verify(contextMock);
    }

    @Test
    public void crawlingCountdownIsDecrementedForUnCrawledDocument() {

    }

    @Test
    public void crawlingDocumentWithCrawCountDownZeroWillBeCrawled() {

    }
}
