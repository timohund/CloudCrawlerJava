package cloudcrawler.mapreduce;

import cloudcrawler.domain.CrawlDocument;
import cloudcrawler.system.http.HttpService;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

public class CrawlMapper extends Mapper<Text, Text, Text, Text> {

    private Text content = new Text();

    private HttpService httpService = new HttpService();

    private Gson gson = new Gson();

    /**
     *
     * @param key
     * @param value
     * @param context
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        StringTokenizer itr = new StringTokenizer(value.toString(),",");
        while (itr.hasMoreTokens()) {
            content.set(itr.nextToken());

            try {

                if(content.toString().trim() == "") {
                    URI uri = new URI(key.toString());

                    HttpResponse headResponse   = httpService.getUrlWithHead(uri);
                    Header header               = headResponse.getLastHeader(new String("Content-Type"));

                    if(header.getValue().contains(new String("text/html"))) {
                        //do the real request
                        HttpResponse getResponse    = httpService.getUriWithGet(uri);
                        String website              = getResponse.getEntity().getContent().toString();

                        CrawlDocument crawled       = new CrawlDocument();
                        crawled.setContent(website);
                        crawled.setMimeType(getResponse.getEntity().getContentType().getValue());
                        crawled.setUrl(uri.toString());

                        String json = gson.toJson(crawled);
                        content = new Text( Base64.encodeBase64(json.getBytes()) );
                    }
                }


                context.write(key, content);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
     }
}
