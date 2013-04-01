package cloudcrawler.system.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.URI;

public class HttpService {

    String userAgent = "Ranktacle Robot";

    HttpClient client;

    public HttpService() {
        client = new DefaultHttpClient() ;
        HttpParams httpParam = client.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParam, 10 * 1000);
        HttpConnectionParams.setSoTimeout(httpParam, 10 * 1000);

        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
    }

    /**
     *
     * @param uri
     * @return
     * @throws java.io.IOException
     */
    public HttpResponse getUrlWithHead(URI uri) throws IOException {
        HttpHead headRequest;

        headRequest = new HttpHead();
        headRequest.setHeader("Referer","http://www.ranktacle.com/");
        headRequest.setURI(uri);

        return client.execute(headRequest);
    }

    /**
     *
     * @param uri
     * @return HttpResponse
     * @throws java.io.IOException
     */
    public HttpResponse getUriWithGet(URI uri) throws IOException {
        HttpGet getRequest;

        getRequest = new HttpGet();
        getRequest.setHeader("Referer","http://www.ranktacle.com/");

        getRequest.setURI(uri);

        return client.execute(getRequest);
    }
}
