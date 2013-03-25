package cloudcrawler.mapreduce;

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
public class Crawler {

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = new Job(conf, "cloudcrawler");
        job.setJarByClass(Crawler.class);
        job.setMapperClass(CrawlingMapper.class);
        job.setNumReduceTasks(5);
        job.setReducerClass(CrawlingReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setInputFormatClass(KeyValueTextInputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

