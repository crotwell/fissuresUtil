package edu.sc.seis.fissuresUtil.gmt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author oliverpa Created on Mar 18, 2005
 */
public class PSBasemapExecute {

    public static void createBasemap(String psFilename,
                                     String projection,
                                     String region,
                                     String paperType,
                                     String fill,
                                     boolean isPortrait,
                                     int xOffset, 
                                     int yOffset) throws InterruptedException, IOException {
        String orient = "";
        if (isPortrait) {
            orient = " -P";
        }
        String command = "psbasemap -V -J" + projection + " -R" + region
                + " --PAPER_MEDIA=" + paperType + " -G" + fill + orient + " -X" + xOffset + "p -Y" + yOffset + "p -K";
        FileOutputStream fos = new FileOutputStream(psFilename, true);
        try {
            GenericCommandExecute.execute(command,
                                          new StringReader(""),
                                          fos,
                                          System.err);
        } finally {
            fos.close();
        }
    }
}