package cloudcrawler.domain.crawler.schedule;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 30.03.13
 * Time: 10:04
 * To change this template use File | Settings | File Templates.
 */
public class EverythingDirectlyStrategy implements CrawlingScheduleStrategy {

    @Override
    public void setCurrentPageUri(URI currentPageUri) {}

    @Override
    public int getCrawlingCountDown(URI linkTargetUri) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
