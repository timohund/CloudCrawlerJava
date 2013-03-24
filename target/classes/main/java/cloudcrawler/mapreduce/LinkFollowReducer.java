package cloudcrawler.mapreduce;

import cloudcrawler.domain.CrawlDocument;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;

public class LinkFollowReducer extends Reducer<Text, Text, Text, Text> {

    private Gson gson = new Gson();

    private HashMap<String,CrawlDocument> result = new HashMap<String, CrawlDocument>();

    /**
     *
     * @param key
     * @param crawlDocument
     */
    protected void merge(Text key, CrawlDocument crawlDocument) {
        CrawlDocument masterDocument = crawlDocument;
        if(result.containsKey(key.toString())) {
            CrawlDocument slaveDocument;

            CrawlDocument currentStoredResult = result.get(key.toString());
            if(currentStoredResult != null) {
               masterDocument = currentStoredResult;
               slaveDocument = crawlDocument;
            } else {
               slaveDocument = currentStoredResult;
            }


            HashMap<String,String> links =  slaveDocument.getIncomingLinks();

            if(links != null) {
                Iterator iterator = links.keySet().iterator();
                while(iterator.hasNext()) {
                    String linkKey = iterator.next().toString();
                    String linkToAdd = links.get(linkKey).toString();
                    // weiterverabeitung der Werte...
                    masterDocument.addIncomingLink(linkToAdd);
                }
            }
        }

        result.remove(key.toString());

        result.put(key.toString(), masterDocument);

    }

    protected void emitAll(Context context) throws IOException, InterruptedException {
        Iterator it = this.result.keySet().iterator();
        while(it.hasNext()) {
            String url = it.next().toString();
            CrawlDocument document = this.result.get(url);
            String documentJson = " "+gson.toJson(document);
            context.write(new Text(url),new Text(documentJson));
        }

        context.setStatus("done");
    }

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

        try {
            this.result = new HashMap<String, CrawlDocument>();
            URI sourceURI = new URI(key.toString());

            for (Text val : values) {
                String json = val.toString();

                try {
                    CrawlDocument crawlDocument = gson.fromJson(json,CrawlDocument.class);
                    this.merge(key,crawlDocument);

                } catch (JsonIOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    System.out.println("Error indexing "+key+" "+json+" ");
                    e.printStackTrace();
                }

                context.progress();
            }

            this.emitAll(context);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
