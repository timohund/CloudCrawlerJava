package org.cloudcrawler.domain.indexer.elasticsearch;

import com.google.gson.Gson;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;
import org.cloudcrawler.domain.crawler.Document;
import org.cloudcrawler.domain.crawler.contentparser.XHTMLContentParser;
import org.cloudcrawler.domain.indexer.Indexer;
import org.cloudcrawler.system.configuration.ConfigurationReader;
import org.elasticsearch.action.admin.indices.optimize.OptimizeRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
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

    BulkRequestBuilder bulkRequest;

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

    public void prepare() {
        OptimizeRequest optimizeRequest = new OptimizeRequest();
        optimizeRequest.indices("cloudcrawler");
        this.client.admin().indices().optimize(optimizeRequest);

        bulkRequest = client.prepareBulk();
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
            bulkRequest.add(client.prepareIndex(index, "document").setRefresh(false).setSource(gson.toJson(document)));
        } catch (Exception e) {

            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void commit() throws Exception {
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
    }

}
