package org.cloudcrawler.system.configuration;

import com.google.inject.Singleton;
import org.apache.hadoop.conf.Configuration;

/**
 * Created with IntelliJ IDEA.
 * User: timoschmidt
 * Date: 18.12.13
 * Time: 19:50
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class ConfigurationReader {

    protected Configuration configuration;

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
