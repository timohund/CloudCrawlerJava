package cloudcrawler.mapreduce;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class LinkFollowReducer extends Reducer<Text, Text, Text, Text> {
   private Text result=new Text();

    public void reduce(Text key,Iterable<Text>values, Context context) throws IOException,InterruptedException {

        for(Text val:values) {

            context.progress();
        }

        context.write(key,result);
    }
}
