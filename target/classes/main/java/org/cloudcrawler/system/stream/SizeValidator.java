package org.cloudcrawler.system.stream;

import java.io.BufferedInputStream;
import java.io.IOException;

public class SizeValidator {


    public boolean validate(BufferedInputStream bis, int limit) throws IOException {
         bis.mark(limit+1);
        int size = 0;

        for (;;) {
            int rsz = bis.read();
            size++;
            if(rsz <= 0) {
               break;
            }

            if(size > limit) {
                return false;
            }
        }

        bis.reset();
        return true;
    }
}
