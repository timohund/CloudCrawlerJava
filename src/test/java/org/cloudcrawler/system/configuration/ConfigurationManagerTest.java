package org.cloudcrawler.system.configuration;

import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Simple testcase to check if the configuration manager is working as expected
 */
public class ConfigurationManagerTest {

    /**
     * Check if an additional configuration can be loaded
     */
    @Test
    public void unConfiguredPropertyIsReturnedAsNull() {
        ConfigurationManager.flushInstance();
        ConfigurationManager cm = ConfigurationManager.getInstance(false);
        String empty            = cm.getFromConfiguration("foo");
        Assert.assertNull(empty);
    }

    /**
     * This testcases checks if the testconfiguration can be loaded from the filesystem
     */
    @Test
    public void canLoadTestConfiguration() throws IOException, URISyntaxException {
        ConfigurationManager.flushInstance();
        ConfigurationManager cm = ConfigurationManager.getInstance(true);
        cm.loadAdditionalConfiguration("src/test/fixtures/files/configuration/cloudcrawler-site.xml");
        Assert.assertEquals("bar",cm.getFromConfiguration("foo"));
    }
}
