package edu.sc.seis.fissuresUtil.gmt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;
import edu.sc.seis.fissuresUtil.bag.StreamPump;

/**
 * @author oliverpa Created on Jan 19, 2005
 */
public class MapProjectExecute {

    public static int[][] forward(String projection,
                                  String region,
                                  double[][] points) throws IOException,
            InterruptedException {
        Runtime rt = Runtime.getRuntime();
        String command = "mapproject -Dp -J" + projection + " -R" + region;
        //System.out.println("executing gmt command: " + command);
        Process proc = rt.exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        StringWriter outputSW = new StringWriter();
        BufferedWriter writer = new BufferedWriter(outputSW);
        BufferedWriter procWriter = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        BufferedWriter errWriter = new BufferedWriter(new OutputStreamWriter(System.err));
        StreamPump pump = new StreamPump(reader, writer, false);
        StreamPump errPump = new StreamPump(errReader, errWriter, false);
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
        //System.out.println("command returned exit value " + exitVal);
        //System.out.println("processing output from gmt command");
        StringTokenizer tok = new StringTokenizer(outputSW.toString());
        int[][] pixelLocs = new int[points.length][2];
        for(int i = 0; i < points.length; i++) {
            pixelLocs[i][0] = (int)Double.parseDouble(tok.nextToken());
            pixelLocs[i][1] = (int)Double.parseDouble(tok.nextToken());
        }
        return pixelLocs;
    }

    public static void main(String[] args) {
        try {
            double[][] points = { {-180, 89},
                                 {-135, 67.5},
                                 {-90, 45},
                                 {-45, 22.5},
                                 {0, 0},
                                 {45, -22.5},
                                 {90, -45},
                                 {135, -67.5},
                                 {179.9, -89}};
            int[][] pixelLocs = forward("Kf166/10i", "-14/346/-90/90", points);
            System.out.println("in : out");
            for(int i = 0; i < pixelLocs.length; i++) {
                System.out.println("(" + points[i][0] + ", " + points[i][1]
                        + ')' + " : (" + pixelLocs[i][0] + ", "
                        + pixelLocs[i][1] + ')');
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    //private static Logger logger = Logger.getLogger(MapProjectExecute.class);
}