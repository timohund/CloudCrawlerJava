package cloudcrawler.mapreduce;

import cloudcrawler.domain.CrawlDocument;
import cloudcrawler.system.http.HttpService;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

public class CrawlMapper extends Mapper<Text, Text, Text, Text> {

    private HttpService httpService = new HttpService();

    private Gson gson = new Gson();

    /**
     * @param key
     * @param value
     * @param context
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        try {

            if (value.toString().trim().equals("")) {
                URI uri = new URI(key.toString());

                HttpResponse headResponse = httpService.getUrlWithHead(uri);
                Header header = headResponse.getLastHeader(new String("Content-Type"));
                Boolean isHtml = header.getValue().contains(new String("text/html"));
                    //close connection
                EntityUtils.consume(headResponse.getEntity());

                if (isHtml) {
                    //do the real request
                    HttpResponse getResponse = httpService.getUriWithGet(uri);

                    StringWriter writer = new StringWriter();
                    IOUtils.copy(getResponse.getEntity().getContent(), writer);
                    String website = writer.toString();


                    CrawlDocument crawled = new CrawlDocument();
                    crawled.setContent(website);
                    crawled.setMimeType(getResponse.getEntity().getContentType().getValue());
                    crawled.setUrl(uri.toString());

                    EntityUtils.consume(getResponse.getEntity());
                    String json = gson.toJson(crawled);
                    value = new Text(json.toString());
                }
            }

            context.write(key, value);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }
}