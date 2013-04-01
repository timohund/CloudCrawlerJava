package cloudcrawler.domain.crawler.robotstxt;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Inspired by crawler commons
 *
 * @coauthor Timo Schmidt <timo-schmidt@gmx.net>
 */

import cloudcrawler.domain.crawler.robotstxt.cache.Cache;
import cloudcrawler.domain.crawler.robotstxt.parser.SimpleRobotRulesParser;
import cloudcrawler.domain.crawler.robotstxt.rules.BaseRobotRules;
import cloudcrawler.system.http.HttpService;
import com.google.inject.Inject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;

import java.io.StringWriter;
import java.net.URI;

public class Service {

    protected HttpService httpService;

    protected SimpleRobotRulesParser robotsTxtParser;

    protected Cache cache;

    @Inject
    Service(HttpService httpService, SimpleRobotRulesParser robotsTxtParser, Cache cache) {
        this.httpService = httpService;
        this.robotsTxtParser = robotsTxtParser;
        this.cache = cache;
    }

    /**
     * This method is used to evaluate if the provided uri is
     * allowed to be crawled against the robots.txt of the website.
     *
     * @param uri
     * @param userAgent
     * @return
     * @throws Exception
     */
    public boolean isAllowedUri(URI uri) throws Exception {
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
                // which is the same as not having a robotstxt.txt file.

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
                            httpService.getUserAgent());

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
