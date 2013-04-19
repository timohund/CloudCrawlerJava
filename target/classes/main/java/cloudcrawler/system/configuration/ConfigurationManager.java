package cloudcrawler.system.configuration;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;


/**
 * The configuration manager is responsible to load the hadoop configuration
 * extend it with the crawler configuration stored in
 *
 * /cloudcrawler/configuration/cloudcrawler-site.xml
 *
 * and provide access to the configuration in an injectable singleton.
 *
 * This class is a singlton but can not be a guice singleton since it is
 * required before a module gets creates
 *
 * @author Timo Schmidt <timo.schmidt@gmx.net>
 */
public class ConfigurationManager {

    Configuration conf;

    private static ConfigurationManager instance = null;

    private ConfigurationManager() {
        conf = new Configuration();
    }

    /**
     * @param additionalConfiguration
     * @return
     * @throws IOException
     */
    public boolean loadAdditionalConfiguration(String additionalConfiguration) throws IOException {
        try {
            Path path = new Path(additionalConfiguration);
            FileSystem fs = FileSystem.get(conf);
            if(fs.exists(path)) {
                conf.addResource(path);
                conf.reloadConfiguration();
                System.out.println(conf.get("robotstxt.cache.class", "cloudcrawler.domain.crawler.robotstxt.cache.NullCache"));

                return true;
            }
        } catch (Exception e) {
            System.out.println("Could not initialize file system");
        }
        return false;
    }

    public Configuration getConfiguration() {
        return conf;
    }

    /**
     * @param name
     * @param defaultValue
     * @return
     */
    public String getFromConfiguration(String name, String defaultValue) {
        return conf.get(name, defaultValue);
    }

    /**
     * @param name
     * @return
     */
    public String getFromConfiguration(String name) {
        return conf.get(name);
    }

    /**
     * @return ConfigurationManager
     */
    public static ConfigurationManager getInstance() {
        if(instance == null) {
            instance = new ConfigurationManager();
        }

        return instance;
    }
}
