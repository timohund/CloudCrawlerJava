package cloudcrawler.mapreduce;

import cloudcrawler.domain.crawler.message.Message;
import cloudcrawler.domain.crawler.message.MessagePersistenceManager;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

abstract public class AbstractMapper  extends Mapper<Text, Text, Text, Text> {

    protected MessagePersistenceManager messageManager;


    public void setMessageManager(MessagePersistenceManager messageManager) {
        this.messageManager = messageManager;
    }

    protected void postMessage(Text key, Message message, Context context) throws IOException, InterruptedException {
        String json = messageManager.sleep(message);
        Text messageJson = new Text(json.toString());
        context.write(key,messageJson);
    }
}
