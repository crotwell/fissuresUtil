package edu.sc.seis.fissuresUtil.gmt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

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
        //logger.debug("executing command: " + command);
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < points.length; i++) {
            for(int j = 0; j < points[i].length; j++) {
                buf.append(points[i][j] + "");
                if(j < points[i].length - 1) {
                    buf.append(" ");
                } else {
                    buf.append("\n");
                }
            }
        }
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        GenericCommandExecute.execute(command,
                                      new StringReader(buf.toString()),
                                      baOutputStream,
                                      System.err);
        StringTokenizer tok = new StringTokenizer(baOutputStream.toString());
        int[][] pixelLocs = new int[points.length][2];
        try {
            for(int i = 0; i < points.length; i++) {
                String xString = tok.nextToken();
                logger.debug("pixelLocs[" + i + "][0] will be " + xString);
                String yString = tok.nextToken();
                logger.debug("pixelLocs[" + i + "][1] will be " + yString);
                pixelLocs[i][0] = (int)Double.parseDouble(xString);
                pixelLocs[i][1] = (int)Double.parseDouble(yString);
            }
            return pixelLocs;
        } catch(NoSuchElementException e) {
            GlobalExceptionHandler.handle("problem translating points.  input was\n"
                    + buf.toString());
        }
        throw new IOException("there was a problem using mapproject");
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

    private static Logger logger = Logger.getLogger(MapProjectExecute.class);
}