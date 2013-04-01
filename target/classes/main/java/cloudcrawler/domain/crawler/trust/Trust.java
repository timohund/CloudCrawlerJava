package cloudcrawler.domain.crawler.trust;

/**
 *
 */
public class Trust {

    public static final String TRUST_DOMAIN_ECONOMY         = "economy";
    public static final String TRUST_DOMAIN_ETC             = "etc";
    public static final String TRUST_DOMAIN_FINANCE         = "finance";
    public static final String TRUST_DOMAIN_LAW             = "law";
    public static final String TRUST_DOMAIN_MEDICINE        = "medicine";
    public static final String TRUST_DOMAIN_PEOPLE          = "people";
    public static final String TRUST_DOMAIN_POLITICS        = "politics";
    public static final String TRUST_DOMAIN_SOCIAL          = "social";
    public static final String TRUST_DOMAIN_STATES          = "states";
    public static final String TRUST_DOMAIN_UNIVERSITIES    = "universities";


    /**
     * Holds the value for this trust.
     *
     * @double
     */
    protected double value;

    /**
     * Code of the trustdomain.
     *
     * @int
     */
    protected String domain;


    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

}

