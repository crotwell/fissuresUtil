package edu.sc.seis.fissuresUtil.gmt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
//import org.apache.log4j.Logger;
import edu.sc.seis.fissuresUtil.bag.StreamPump;

/**
 * @author oliverpa Created on Jan 14, 2005
 */
public class PSXYExecute {

    public static void addPoints(String psFilename,
                                 String projection,
                                 String region,
                                 String symbol,
                                 String fillColor,
                                 String outlineColor,
                                 double[][] points) throws IOException,
            InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String command = "psxy -V -J" + projection + " -R" + region + " -S"
                + symbol + " -O -K";
        if(fillColor != null) {
            command += " -G" + fillColor;
        }
        if(outlineColor != null) {
            command += " -W" + outlineColor;
        }
        System.out.println("executing gmt command: " + command);
        Process proc = rt.exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(psFilename,
                                                                                               true)));
        BufferedWriter procWriter = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        BufferedWriter errWriter = new BufferedWriter(new OutputStreamWriter(System.err));
        StreamPump pump = new StreamPump(reader, writer);
        StreamPump errPump = new StreamPump(errReader, errWriter);
        pump.start();
        errPump.start();
        for(int i = 0; i < points.length; i++) {
            for(int j = 0; j < points[i].length; j++) {
                procWriter.write(points[i][j] + "");
                if(j < points[i].length - 1) {
                    procWriter.write(" ");
                } else {
                    procWriter.write("\n");
                }
            }
        }
        procWriter.close();
        int exitVal = proc.waitFor();
        //waiting for finish of StreamPump runs
        synchronized(pump) {}
        synchronized(errPump) {}
        System.out.println("command returned exit value " + exitVal);
    }

public static void main(String[] args) {
        try {
            double[][] points = { {-180, 90},
                                 {-135, 67.5},
                                 {-90, 45},
                                 {-45, 22.5},
                                 {0, 0}};
            addPoints("/Volumes/heff/oliverpa/gmtTest/world.ps",
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
            addPoints("/Volumes/heff/oliverpa/gmtTest/world.ps",
                      "Kf166/10i",
                      "-14/346/-90/90",
                      "ci",
                      null,
                      "12/255/0/0",
                      morePoints);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }    //private static Logger logger = Logger.getLogger(PSXYExecute.class);
}