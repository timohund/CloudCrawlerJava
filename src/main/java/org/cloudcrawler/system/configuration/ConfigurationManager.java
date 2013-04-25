package org.cloudcrawler.system.configuration;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;


/**
 * The configuration manager is responsible to load the hadoop configuration
 * extend it with the crawler configuration stored in
 *
 * /org.cloudcrawler/configuration/org.cloudcrawler-site.xml
 *
 * and provide access to the configuration in an injectable singleton.
 *
 * This class is a singleton but can not be a guice singleton since it is
 * required before a module gets creates
 *
 * @author Timo Schmidt <timo.schmidt@gmx.net>
 */
public class ConfigurationManager {

    Configuration conf;

    private static ConfigurationManager instance = null;

    /**
     * @param loadBasicConfiguration
     */
    private ConfigurationManager(boolean loadBasicConfiguration) {
        this.setConfiguration(new Configuration(loadBasicConfiguration));
    }


    public void setConfiguration(Configuration conf) {
        this.conf = conf;
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
     * @param loadBasicConfiguration
     * @return ConfigurationManager
     */
    public static ConfigurationManager getInstance(boolean loadBasicConfiguration) {
        if(instance == null) {
            instance = new ConfigurationManager(loadBasicConfiguration);
        }

        return instance;
    }

    /**
     * Flushes the singleton instance in testing context.
     *
     * @return void
     */
    public static void flushInstance() {
       instance = null;
    }
}

