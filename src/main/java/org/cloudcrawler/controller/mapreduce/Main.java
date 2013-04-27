package org.cloudcrawler.controller.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.cloudcrawler.controller.mapreduce.crawler.CrawlingMapper;
import org.cloudcrawler.controller.mapreduce.crawler.CrawlingReducer;
import org.cloudcrawler.controller.mapreduce.indexer.IndexerMapper;
import org.cloudcrawler.controller.mapreduce.trust.LinkTrustMapper;
import org.cloudcrawler.controller.mapreduce.trust.LinkTrustReducer;

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
            Job job = new Job(conf, "org.cloudcrawler - crawling");
            job.setJarByClass(Main.class);
            job.setMapperClass(CrawlingMapper.class);
            job.setReducerClass(CrawlingReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            job.setInputFormatClass(KeyValueTextInputFormat.class);
            job.setNumReduceTasks(15);

            FileInputFormat.addInputPath(job, new Path(args[1]));
            FileOutputFormat.setOutputPath(job, new Path(args[2]));
            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }

        if(action.equals("linktrust")) {
            Job job = new Job(conf, "org.cloudcrawler - linktrust");
            job.setJarByClass(Main.class);
            job.setMapperClass(LinkTrustMapper.class);
            job.setReducerClass(LinkTrustReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);

            job.setInputFormatClass(KeyValueTextInputFormat.class);
            job.setNumReduceTasks(5);

            FileInputFormat.addInputPath(job, new Path(args[1]));
            FileOutputFormat.setOutputPath(job, new Path(args[2]));
            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }

        if(action.equals("index")) {
            Job job = new Job(conf, "org.cloudcrawler - index");
            job.setJarByClass(Main.class);
            job.setMapperClass(IndexerMapper.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);

            job.setInputFormatClass(KeyValueTextInputFormat.class);
            job.setNumReduceTasks(5);

            FileInputFormat.addInputPath(job, new Path(args[1]));
            FileOutputFormat.setOutputPath(job, new Path(args[2]));
            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }
    }

}
