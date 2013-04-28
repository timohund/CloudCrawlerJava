package org.cloudcrawler.system.stream;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * This validator can be used to validate the size of an input stream against
 * a number of bytes.
 *
 * @author Timo Schmidt <timo.schmidt@gmx.net>
 */
public class SizeValidator {

    /**
     * This method can be used to check if a stream is larger then a certain size
     * of bytes.
     *
     * @param bis
     * @param limit
     * @return
     * @throws IOException
     */
    public boolean isValid(BufferedInputStream bis, int limit) throws IOException {
        bis.mark(limit+1);
        int size = 0;

        for (;;) {
            int rsz = bis.read();
            size++;
                //end is reached?
            if(rsz <= 0) {  break;  }
                //limit is reached?
            if(size > limit) {  return false; }
        }

        bis.reset();
        return true;
    }
}
