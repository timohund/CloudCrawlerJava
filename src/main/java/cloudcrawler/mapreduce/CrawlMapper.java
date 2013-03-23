package cloudcrawler.mapreduce;

import cloudcrawler.system.http.HttpService;
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
                URI uri = new URI(key.toString());

                HttpResponse headResponse   = httpService.getUrlWithHead(uri);
                Header header               = headResponse.getLastHeader(new String("Content-Type"));

                if(header.getValue().contains(new String("text/html"))) {
                    //do the real request
                    HttpResponse getResponse    = httpService.getUriWithGet(uri);
                    String website              = getResponse.getEntity().getContent().toString();
                    content                     = new Text(website);
                }


                context.write(key, content);

            } catch (URISyntaxException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
     }
}
