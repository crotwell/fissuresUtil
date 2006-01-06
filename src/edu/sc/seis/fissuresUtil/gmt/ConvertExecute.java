package edu.sc.seis.fissuresUtil.gmt;

import java.io.File;
import java.io.IOException;

/**
 * @author oliverpa Created on Jan 21, 2005
 */
public class ConvertExecute {

    public static int convert(File infile, File outfile, String options)
            throws IOException, InterruptedException {
        return convert(infile.getCanonicalPath(), outfile.getCanonicalPath(), options);
    }
    
    public static int convert(String infile, String outfile, String options)
    throws IOException, InterruptedException {
        String command = "convert " + options + " " + infile + " " + outfile;
        return GenericCommandExecute.execute(command);
    }

    public static void main(String[] args) {
        try {
            convert("world.ps", "world.png", "-antialias -rotate 90");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}