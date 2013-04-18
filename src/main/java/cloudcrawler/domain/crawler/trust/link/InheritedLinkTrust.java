package cloudcrawler.domain.crawler.trust.link;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 14.04.13
 * Time: 10:43
 * To change this template use File | Settings | File Templates.
 */
public class InheritedLinkTrust {

    protected double linkTrust = 0.0;

    public double getLinkTrust() {
        return linkTrust;
    }

    public void setLinkTrust(double rank) {
        this.linkTrust = rank;
    }
}
