package cloudcrawler.mapreduce;

import cloudcrawler.mapreduce.crawler.CrawlingMapper;
import cloudcrawler.mapreduce.crawler.CrawlingReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * MapReduce job that is taking the url list
 * from the first argument, crawls them and attaches
 * the link structure to the crawled document
 * and persists it.
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net>
 */
public class Main {

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String action = args[0];

        if(action.equals("crawl")) {
            Job job = new Job(conf, "cloudcrawler");
            job.setJarByClass(Main.class);
            job.setMapperClass(CrawlingMapper.class);
            job.setNumReduceTasks(5);
            job.setReducerClass(CrawlingReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            job.setInputFormatClass(KeyValueTextInputFormat.class);

            FileInputFormat.addInputPath(job, new Path(args[1]));
            FileOutputFormat.setOutputPath(job, new Path(args[2]));

            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }
    }
}

