package edu.sc.seis.fissuresUtil.gmt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.apache.log4j.Logger;
import edu.sc.seis.fissuresUtil.bag.StreamPump;

/**
 * @author oliverpa Created on Jan 14, 2005
 */
public class PSXYExecute {

    public static void addPoints(String psFilename,
                                 String projection,
                                 String region,
                                 String symbol,
                                 String fill,
                                 double[][] points) throws IOException,
            InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String command = "psxy -V -J" + projection + " -R" + region + " -S"
                + symbol + " -G" + fill + " -O -K";
        logger.debug("executing gmt command: " + command);
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
            procWriter.write(points[i][0] + " " + points[i][1] + '\n');
        }
        procWriter.close();
        int exitVal = proc.waitFor();
        //waiting for finish of StreamPump runs
        synchronized(pump) {}
        synchronized(errPump) {}
        logger.debug("command returned exit value " + exitVal);
    }

    public static void main(String[] args) {
        try {
            double[][] points = { {-180, 90},
                                 {-135, 67.5},
                                 {-90, 45},
                                 {-45, 22.5},
                                 {0, 0},
                                 {45, -22.5},
                                 {90, -45},
                                 {135, -67.5},
                                 {180, -90}};
            addPoints("/Volumes/heff/oliverpa/gmtTest/world.ps",
                      "Kf166/10i",
                      "-14/346/-90/90",
                      "t0.4",
                      "0/0/255",
                      points);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static Logger logger = Logger.getLogger(PSXYExecute.class);
}