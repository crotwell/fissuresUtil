package edu.sc.seis.fissuresUtil.gmt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import edu.sc.seis.fissuresUtil.bag.StreamPump;

/**
 * @author oliverpa Created on Jan 21, 2005
 */
public class GenericCommandExecute {

    public static void execute(String command) throws InterruptedException,
            IOException {
        Runtime rt = Runtime.getRuntime();
        System.out.println("executing command: " + command);
        Process proc = rt.exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        BufferedWriter errWriter = new BufferedWriter(new OutputStreamWriter(System.err));
        StreamPump pump = new StreamPump(reader, writer);
        StreamPump errPump = new StreamPump(errReader, errWriter);
        pump.start();
        errPump.start();
        int exitVal = proc.waitFor();
        //waiting for finish of StreamPump runs
        synchronized(pump) {}
        synchronized(errPump) {}
        System.out.println("command returned exit value " + exitVal);
    }

    public static void main(String[] args) {
        try {
            if(args.length > 0) {
                execute(args[0]);
            } else {
                execute("ls /bin");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}