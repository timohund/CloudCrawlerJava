About
===========

:Author: Timo Schmidt <timo-schmidt@gmx.net>
:Description: Crawler based on mapreduce for Apache Hadoop
:Build status: |buildStatusIcon|

Cloudcrawler is java based crawler based on hadoop/mapreduces.
The goal was to get familar with apache hadoop.

Alternatives are:

* Apache Nutch
* Heretrix Crawler

Compile
===========

You can compile a single jar file with the following command:

    mvn compile assembly:single

The jar file is located in:

    target/org.cloudcrawler-jar-with-dependencies.jar


Crawl & Index
============
The crawler can be used the following way:

1. Create a source file with crawling start urls (one per line)
eg:

    http://www.heise.de/

    http://www.spiegel.de/

2. Copy the file to hdfs

    hadoop fs -copyFromLocal crawl.txt /cloudcrawler/crawl/start/

3. Now the crawling can be starte:

    hadoop org.cloudcrawler-jar-with-dependencies.jar crawl /cloudcrawler/crawl/start/ /cloudcrawler/crawl/out1/

The job can be repeated several times and the crawler will discover more and more pages

    hadoop org.cloudcrawler-jar-with-dependencies.jar crawl /cloudcrawler/crawl/out1/ /cloudcrawler/crawl/out2/

    hadoop org.cloudcrawler-jar-with-dependencies.jar crawl /cloudcrawler/crawl/out2/ /cloudcrawler/crawl/out3/

4. When the crawling is done, there are some other jobs that can be used one of the is the linktrust job. This job should be executed 3-4 times and the first input is the last output of the crawling process

    hadoop org.cloudcrawler-jar-with-dependencies.jar linktrust /cloudcrawler/crawl/out3/ /cloudcrawler/linktrust/out1/

5. The last process ist the indexing process. The indexing process can be used to write the documents to elasticsearch or solr

    hadoop org.cloudcrawler-jar-with-dependencies.jar index /cloudcrawler/linktrust/out1/ /cloudcrawler/index/out1/



.. |buildStatusIcon| image:: https://travis-ci.org/timoschmidt/CloudCrawlerJava.png?branch=master
   :alt: Build Status
   :target: https://travis-ci.org/timoschmidt/CloudCrawlerJava


