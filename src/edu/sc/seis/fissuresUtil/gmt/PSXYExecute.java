package edu.sc.seis.fissuresUtil.gmt;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * @author oliverpa Created on Jan 14, 2005
 */
public class PSXYExecute {

    public static void addPoints(String psFilename,
                                 String projection,
                                 String region,
                                 String symbol,
                                 String fill) throws IOException,
            InterruptedException {
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec("/sw/bin/psxy -J" + projection + " -R" + region
                + " -S" + symbol + " -G" + fill + " -O -K >> " + psFilename);
        // I think it's the output stream I'm supposed to be getting. The
        // javadoc for getOutputStream seemed to suggest this.  Either that,
        // or it just horribly confused me.
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
        writer.write("90 37");
        writer.close();
        rt.wait();
    }

    public static void main(String[] args) {
        try {
            addPoints("/Volumes/heff/oliverpa/gmtTest/world.ps",
                      "Kf166/10i",
                      "-14/346/-90/90",
                      "t1.0",
                      "0/0/255");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}