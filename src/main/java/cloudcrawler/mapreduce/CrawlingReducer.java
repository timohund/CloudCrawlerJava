package cloudcrawler.mapreduce;

import cloudcrawler.domain.crawler.Document;
import cloudcrawler.domain.crawler.DocumentMerger;
import cloudcrawler.domain.crawler.message.DocumentMessage;
import cloudcrawler.ioc.CloudCrawlerModule;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class CrawlingReducer extends Reducer<Text, Text, Text, Text> {

    protected Gson gson = new Gson();

    protected Injector injector;

    protected DocumentMerger merger;

    public CrawlingReducer() {
        injector = Guice.createInjector(new CloudCrawlerModule());
        this.setGson(injector.getInstance(Gson.class));
        this.setMerger(injector.getInstance(DocumentMerger.class));
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public void setMerger(DocumentMerger merger) {
        this.merger = merger;
    }

    /**
     * @param key
     * @param values
     * @param context
     * @throws IOException
     * @throws InterruptedException
     */
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

        try {

            this.merger.reset();

            for (Text val : values) {
                String json = val.toString();

                try {
                    DocumentMessage message = gson.fromJson(json, DocumentMessage.class);
                    Document document = message.getAttachment();
                    merger.merge(key.toString(), document);

                } catch (JsonIOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    System.out.println("Error indexing "+key+" "+json+" ");
                    e.printStackTrace();
                }

                context.progress();
            }


            this.emitAll(this.merger.getResult(),context);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void emitAll(HashMap<String,Document> mergeResult,Context context) throws IOException, InterruptedException {
        Iterator it = mergeResult.keySet().iterator();
        while(it.hasNext()) {
            String url = it.next().toString();
            Document document = mergeResult.get(url);
            DocumentMessage documentMessage = new DocumentMessage();
            documentMessage.setAttachment(document);
            String documentMessageJson = " "+gson.toJson(documentMessage);
            context.write(new Text(url),new Text(documentMessageJson));
        }

        context.setStatus("done");
    }
}
