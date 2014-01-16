package org.cloudcrawler.domain.indexer.elasticsearch;

import com.google.gson.Gson;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;
import org.cloudcrawler.domain.crawler.Document;
import org.cloudcrawler.domain.crawler.contentparser.XHTMLContentParser;
import org.cloudcrawler.domain.indexer.Indexer;
import org.cloudcrawler.system.configuration.ConfigurationReader;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.MalformedURLException;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 23.04.13
 * Time: 21:11
 * To change this template use File | Settings | File Templates.
 */
public class ElasticSearchIndexer implements Indexer {

    Client client;

    String index;

    Gson gson;

    /**
     * @param configurationReader
     */
    @Inject
    public ElasticSearchIndexer(ConfigurationReader configurationReader, XHTMLContentParser parser, Gson gson) throws MalformedURLException {
        Configuration configuration = configurationReader.getConfiguration();
        String hostname  = configuration.get("indexer.elasticsearch.hostname","localhost");
        Integer port     = configuration.getInt("indexer.elasticsearch.port", 9300);
        this.index       = configuration.get("indexer.elasticsearch.index","cloudcrawler");

        System.out.println(hostname+":"+port);

        Settings settings   = ImmutableSettings.settingsBuilder().put("cluster.name", "cloudcrawler").build();
        this.client         = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(hostname,port));
        this.gson           = gson;
    }

    @Override
    public void flush() throws Exception {
        DeleteByQueryRequest req = new DeleteByQueryRequest();
        req.query("*");
        client.deleteByQuery(req);
    }

    @Override
    public void index(Document document) throws Exception {
        try {
            IndexResponse response = client.prepareIndex(index,"document").setSource(gson.toJson(document)).execute().actionGet();
            System.out.println(response.getHeaders().toString());
        } catch (Exception e) {

            System.out.println(e.getMessage());
        }

    }

    @Override
    public void commit() throws Exception {

    }
}
