package cloudcrawler.system.uri;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 29.03.13
 * Time: 14:14
 * To change this template use File | Settings | File Templates.
 */
public class URIHelper {

    public static String getSecondLevelHostName(URI uri) {
        if(uri.toString().equals("")) {
            return "";
        }
        String hostName = uri.getHost();
        int lastDot = hostName.lastIndexOf(".");
        int secondLast = hostName.lastIndexOf(".",lastDot-1);

        if(secondLast > 0) {
            return hostName.substring(secondLast+1);
        } else {
            return hostName;
        }

    }
}
