package org.cloudcrawler.system.uri;

import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;
import org.cloudcrawler.system.configuration.ConfigurationReader;

import java.net.URI;

/**
 * This validator is used to check if an URI is allowed for
 * crawling
 *
 * @author Timo Schmidt
 */
public class URIValidator {

    protected String schemeAllowPattern = ".*";
    protected String hostAllowPattern = ".*";
    protected String pathAllowPattern = ".*";
    protected String queryAllowPattern = ".*";
    protected String fragmentAllowPattern = ".*";

    @Inject
    public URIValidator(ConfigurationReader configurationReader) {
        Configuration configuration = configurationReader.getConfiguration();
        this.schemeAllowPattern     = configuration.get("urivalidator.scheme.allowpattern",".*");
        this.hostAllowPattern       = configuration.get("urivalidator.host.allowpattern",".*");
        this.pathAllowPattern       = configuration.get("urivalidator.path.allowpattern",".*");
        this.queryAllowPattern      = configuration.get("urivalidator.query.allowpattern",".*");
        this.fragmentAllowPattern   = configuration.get("urivalidator.fragment.allowpattern",".*");
    }

    public boolean isValid(URI uri) {
        boolean isAllowedScheme     = matchesPattern(uri.getScheme(), schemeAllowPattern);
        boolean isAllowedHost       = matchesPattern(uri.getHost(), hostAllowPattern);
        boolean isAllowedPath       = matchesPattern(uri.getPath(), pathAllowPattern);
        boolean isAllowedQuery      = matchesPattern(uri.getQuery(), queryAllowPattern);
        boolean isAllowedFragment   = matchesPattern(uri.getFragment(), fragmentAllowPattern);

        return isAllowedScheme && isAllowedHost && isAllowedPath && isAllowedQuery && isAllowedFragment;
    }

    /**
     *
     * @param subject
     * @param pattern
     * @return
     */
    protected boolean matchesPattern(String subject, String pattern) {
        if(subject == null) {
            subject = new String("");
        }

        return subject.matches(pattern);
    }
}
