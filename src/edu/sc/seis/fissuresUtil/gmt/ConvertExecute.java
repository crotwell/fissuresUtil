package edu.sc.seis.fissuresUtil.gmt;

import java.io.IOException;


/**
 * @author oliverpa
 * Created on Jan 21, 2005
 */
public class ConvertExecute {
    
    public static void convert(String infile, String outfile, String options)
            throws IOException, InterruptedException{
        String command = "convert " + options + " " + infile + " " + outfile;
        GenericCommandExecute.execute(command);
    }

    public static void main(String[] args) {
        try {
            convert("world.ps", "world.png", "-antialias -rotate 90");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
