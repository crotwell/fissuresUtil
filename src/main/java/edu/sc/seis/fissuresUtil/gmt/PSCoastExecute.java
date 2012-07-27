package edu.sc.seis.fissuresUtil.gmt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

public class PSCoastExecute {

    public static void createMap(String psFilename,
                                 String projection,
                                 String region,
                                 String border,
                                 String fill,
                                 boolean isPortrait,
                                 int xOffset,
                                 int yOffset) throws InterruptedException,
            IOException {
        createMap(new File(psFilename),
                  projection,
                  region,
                  border,
                  fill,
                  isPortrait,
                  xOffset,
                  yOffset);
    }

    public static void createMap(File psFile,
                                 String projection,
                                 String region,
                                 String border,
                                 String fill,
                                 boolean isPortrait,
                                 float xOffset,
                                 float yOffset) throws InterruptedException,
            IOException {
        String orient = "";
        if(isPortrait) {
            orient = " -P";
        }
        String command = "pscoast -J" + projection + " -R" + region + " -G"
                + fill + orient + " -B" + border + " -X" + xOffset + "p -Y"
                + yOffset + "p -K";
        FileOutputStream fos = new FileOutputStream(psFile, true);
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
