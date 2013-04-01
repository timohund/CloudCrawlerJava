package cloudcrawler.domain.crawler.robots;

import cloudcrawler.system.http.HttpService;
import com.google.inject.Inject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;

import java.io.StringWriter;
import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 01.04.13
 * Time: 12:19
 * To change this template use File | Settings | File Templates.
 */
public class RobotsTxtService {


    protected HttpService httpService;

    protected SimpleRobotRulesParser robotsTxtParser;

    protected RobotsTxtCache cache;

    @Inject
    RobotsTxtService(HttpService httpService, SimpleRobotRulesParser robotsTxtParser, RobotsTxtCache cache) {
        this.httpService = httpService;
        this.robotsTxtParser = robotsTxtParser;
        this.cache = cache;
    }

    public boolean isAllowedUri(URI uri, String userAgent) throws Exception {

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(uri.getScheme());
        uriBuilder.setHost(uri.getHost());
        uriBuilder.setUserInfo(uri.getUserInfo());
        uriBuilder.setPath("/robots.txt");

        URI robotsTxtUri        = uriBuilder.build();
        BaseRobotRules rules    = (BaseRobotRules) cache.get(robotsTxtUri.toString());

        if(rules == null) {
            HttpResponse response = httpService.getUriWithGet(robotsTxtUri);

            try {

                // HACK! DANGER! Some sites will redirect the request to the top-level domain
                // page, without returning a 404. So look for a response which has a redirect,
                // and the fetched content is not plain text, and assume it's one of these...
                // which is the same as not having a robots.txt file.

                String contentType = response.getEntity().getContentType().getValue();
                boolean isPlainText = (contentType != null) && (contentType.startsWith("text/plain"));

                if (response.getStatusLine().getStatusCode() == 404 || !isPlainText) {
                    rules =  robotsTxtParser.failedFetch(HttpStatus.SC_GONE);
                } else {
                    StringWriter writer = new StringWriter();

                    IOUtils.copy(response.getEntity().getContent(), writer);

                    rules = robotsTxtParser.parseContent(
                            uri.toString(),
                            writer.toString().getBytes(),
                            response.getEntity().getContentType().getValue(),
                            userAgent);

                }
            } catch (Exception e) {
                EntityUtils.consume(response.getEntity());
                throw e;
            }

            EntityUtils.consume(response.getEntity());
            cache.set(robotsTxtUri.toString(),60*60*24,rules);
        }

        return rules.isAllowed(uri.toString());
    }
}
