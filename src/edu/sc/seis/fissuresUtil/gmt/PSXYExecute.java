package edu.sc.seis.fissuresUtil.gmt;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author oliverpa Created on Jan 14, 2005
 */
public class PSXYExecute {

    public static void open(String psFilename, String projection, String region)
            throws InterruptedException, IOException {
        String command = "psxy /dev/null -V -J" + projection + " -R" + region
                + " -K";
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

    public static void close(String psFilename, String projection, String region)
            throws InterruptedException, IOException {
        String command = "psxy /dev/null -V -J" + projection + " -R" + region
                + " -O";
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

    public static void addPoints(String psFilename,
                                 String projection,
                                 String region,
                                 String symbol,
                                 String fillColor,
                                 String outlineColor,
                                 double[][] points) throws IOException,
            InterruptedException {
        String command = "psxy -V -J" + projection + " -R" + region + " -S"
                + symbol + " -O -K";
        if(fillColor != null) {
            command += " -G" + fillColor;
        }
        if(outlineColor != null) {
            command += " -W" + outlineColor;
        }
        StringBuffer buff = new StringBuffer();
        for(int i = 0; i < points.length; i++) {
            for(int j = 0; j < points[i].length; j++) {
                buff.append(points[i][j] + "");
                if(j < points[i].length - 1) {
                    buff.append(" ");
                } else {
                    buff.append("\n");
                }
            }
        }
        FileOutputStream fos = new FileOutputStream(psFilename, true);
        try {
            GenericCommandExecute.execute(command,
                                          new StringReader(buff.toString()),
                                          fos,
                                          System.err);
        } finally {
            fos.close();
        }
    }

    public static void main(String[] args) {
        try {
            open("world.ps",
                      "Kf166/10i",
                      "-14/346/-90/90");
            double[][] points = { {-180, 90},
                                 {-135, 67.5},
                                 {-90, 45},
                                 {-45, 22.5},
                                 {0, 0}};
            addPoints("world.ps",
                      "Kf166/10i",
                      "-14/346/-90/90",
                      "t0.4",
                      "0/0/255",
                      "5/255",
                      points);
            double[][] morePoints = { {45, -22.5, 0.7},
                                     {90, -45, 0.5},
                                     {135, -67.5, 0.4},
                                     {180, -90, 1.0}};
            addPoints("world.ps",
                      "Kf166/10i",
                      "-14/346/-90/90",
                      "ci",
                      null,
                      "12/255/0/0",
                      morePoints);
            close("world.ps",
                 "Kf166/10i",
                 "-14/346/-90/90");
        } catch(Exception e) {
            e.printStackTrace();
        }
    } //private static Logger logger = Logger.getLogger(PSXYExecute.class);
}