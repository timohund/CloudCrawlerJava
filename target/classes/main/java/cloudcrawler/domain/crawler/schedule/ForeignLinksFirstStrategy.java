package cloudcrawler.domain.crawler.schedule;

import cloudcrawler.system.uri.URIHelper;

import java.net.URI;
import java.util.HashMap;
import java.util.Random;

/**
 * This strategy should schedule links on the same page with
 * a high countdown and links to another second level domain
 * imediatly
 *
 * @author Timo Schmidt <timo-schmidt@gmx.net></timo-schmidt@gmx.net>
 */
public class ForeignLinksFirstStrategy implements CrawlingScheduleStrategy{

    protected URI currentPageUri;

    protected String currentSecondLevelHost;

    protected HashMap<String,Integer> lastCrawlingCountDowns = new HashMap<String, Integer>();

    protected int onPageLowestCrawlCountDown = 10;

    protected int onPageHighestCrawlCountDown = 30;

    @Override
    public void setCurrentPageUri(URI currentPageUri) {
        currentPageUri = currentPageUri   ;
        currentSecondLevelHost = URIHelper.getSecondLevelHostName(currentPageUri);
    }

    public int getCrawlingCountDown(URI linkTargetUri) {
        int result = 0;

        String linkSecondLevelHost = URIHelper.getSecondLevelHostName(linkTargetUri);

        if(currentSecondLevelHost.equals(linkSecondLevelHost)) {
            //the link is on the same host, so we schedule it with a high
            //crawling countdown
            Random rand = new Random();
            int randomNum = rand.nextInt(
                    this.onPageHighestCrawlCountDown - this.onPageLowestCrawlCountDown + 1)
                    + this.onPageLowestCrawlCountDown;
            result = randomNum;
        } else {
            // the link is on a different host, so we check how many
            // site for this domain are scheduled and return a low value
            if(lastCrawlingCountDowns.containsKey(linkSecondLevelHost)) {
                int lastCrawlingCountDown = lastCrawlingCountDowns.get(linkSecondLevelHost);
                lastCrawlingCountDown++;
                lastCrawlingCountDowns.remove(linkSecondLevelHost);
                lastCrawlingCountDowns.put(linkSecondLevelHost, lastCrawlingCountDown);

                    //5 links are allowed
                result = (int) Math.ceil(lastCrawlingCountDown / 5);
               // System.out.println("Assigning "+result+" "+linkSecondLevelHost);

            } else {
                result = 0;
                lastCrawlingCountDowns.put(linkSecondLevelHost, result);
            }
        }

        return result;
    }
}
