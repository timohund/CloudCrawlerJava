package org.cloudcrawler.system.http;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;

public class HttpService {

    String userAgent = "Ernst 2.0";

    HttpClient client;

    Injector injector;

    @Inject
    public HttpService(Injector injector) {
        this.injector = injector;
        this.init();
    }

    /**
     * ReInitializes the httpService
     * @return void
     */
    protected void init() {
        client = this.injector.getInstance(DefaultHttpClient.class);

        HttpParams httpParam = client.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParam, 10 * 1000);
        HttpConnectionParams.setSoTimeout(httpParam, 10 * 1000);

        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
    }

    /**
     * Closes the current connection and prepares the HttpService for a new request.
     * @return void
     */
    public boolean reset() {
        client.getConnectionManager().shutdown();
        this.init();
        return true;
    }

    /**
     * Perform a head request.
     *
     * @param uri
     * @return
     * @throws java.io.IOException
     */
    public HttpResponse head(URI uri) throws IOException {
        HttpHead headRequest;

        headRequest = new HttpHead();
      //  headRequest.setHeader("Referer","http://www.ranktacle.com/");
        headRequest.setURI(uri);

        return client.execute(headRequest);
    }

    /**
     * Perform a get request
     *
     * @param uri
     * @return HttpResponse
     * @throws java.io.IOException
     */
    public HttpResponse get(URI uri) throws IOException {
        HttpGet getRequest;

        getRequest = new HttpGet();
        //getRequest.setHeader("Referer","http://www.ranktacle.com/");

        getRequest.setURI(uri);

        return client.execute(getRequest);
    }

    /**
     * Closes the connection for a response.
     *
     * @param response
     */
    public boolean close(HttpResponse response) throws IOException {
        //close connection
        EntityUtils.consume(response.getEntity());

        return true;
    }

    /**
     * Return the useragent of the crawler.
     *
     * @return
     */
    public String getUserAgent() {
        return userAgent;
    }
}
