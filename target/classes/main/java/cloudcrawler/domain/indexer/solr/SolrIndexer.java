package cloudcrawler.domain.indexer.solr;

import cloudcrawler.domain.crawler.Document;
import cloudcrawler.domain.crawler.Link;
import cloudcrawler.domain.indexer.Indexer;
import cloudcrawler.system.configuration.ConfigurationManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

/**
 * Implementation to indexer into a solr server.
 *
 * @author timo-schmidt@gmx.net
 */
@Singleton
public class SolrIndexer implements Indexer {

    SolrServer solrServer;

    Collection<SolrInputDocument> indexedDocs = null;

    int addWhenDocCountReached = 10;

    int docCount = 0;

    /**
     * @param configurationManager
     */
    @Inject
    public SolrIndexer(ConfigurationManager configurationManager) throws MalformedURLException {
        String hostname     = configurationManager.getFromConfiguration("indexer.solr.hostname","127.0.0.1");
        String port         = configurationManager.getFromConfiguration("indexer.solr.port","8080");
        String corename     = configurationManager.getFromConfiguration("indexer.solr.corename","cloudcrawler");

        String url          = "http://"+hostname+":"+port+"/solr/"+corename;
        solrServer          = new HttpSolrServer(url);

        this.reset();
    }

    protected void reset() {
        indexedDocs = new ArrayList<SolrInputDocument>();
        docCount = 0;
    }

    public void flush() throws IOException, SolrServerException {
        solrServer.deleteByQuery("*:*");
        solrServer.commit();
    }

    @Override
    public void index(Document document) throws Exception {
        SolrInputDocument solrDocument = new SolrInputDocument();

        solrDocument.setField("id", UUID.randomUUID());
        solrDocument.setField("url",document.getUri().toString());

            //@todo find a smarter way to get text and evaluate jsoup for general extraction
        String content = document.getContent();
        solrDocument.setField("content", Jsoup.parse(content).text());
        solrDocument.setField("linktrust_d",document.getLinkTrust());

        HashMap<String,Link> links = document.getIncomingLinks();

        Iterator iterator = links.keySet().iterator();
        while(iterator.hasNext()) {
            String linkKey = iterator.next().toString();
            Link linkToAdd = links.get(linkKey);
            solrDocument.addField("incominglinkurls_sm",linkToAdd.getSourceUri().toString());
            solrDocument.addField("incominglinktexts_tm",linkToAdd.getText());
        }

        indexedDocs.add(solrDocument);
        docCount++;

        if(docCount >= addWhenDocCountReached) {
            addDocumentsToSolr();
        }
    }

    /**
     * @throws Exception
     */
    public void commit() throws Exception {
            //add maybe missing document to solr
        addDocumentsToSolr();
            //and the do commit on the solr server
        solrServer.commit();
    }

    /**
     * @throws Exception
     */
    private void addDocumentsToSolr() throws Exception {
        try {
            solrServer.add(indexedDocs);
            this.reset();
        } catch (Exception e) {
            //second change
            try {
                solrServer.add(indexedDocs);
                this.reset();
            } catch (Exception eInner) {
                throw eInner;
            }
        }
    }

    public void finalize() throws Exception {
        this.commit();
    }
}

