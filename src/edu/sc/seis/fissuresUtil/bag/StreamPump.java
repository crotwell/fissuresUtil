/**
 * StreamPump takes an input stream, and an optional output writer, and reads
 * from the input stream and writes it to the output. It does this in a separate
 * thread, useful also for gobbling the stuff in an input stream that you do not
 * really care about, but need to empty to prevent the streams buffers from
 * filling.
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.fissuresUtil.bag;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class StreamPump extends Thread {

    public StreamPump(InputStream in) {
        this(new BufferedInputStream(in), null);
    }

    public StreamPump(BufferedReader in) {
        this(in, null, false);
    }

    public StreamPump(InputStream in, BufferedWriter out) {
        this(new BufferedReader(new InputStreamReader(in)), out, false);
    }

    public StreamPump(BufferedReader in, BufferedWriter out, boolean closeOnExit) {
        this.in = in;
        this.out = out;
        this.closeOnExit = closeOnExit;
    }

    public synchronized void run() {
        try {
            String s;
            while((s = in.readLine()) != null) {
                if(out != null) {
                    out.write(s);
                    out.newLine();
                }
            }
            if(out != null) {
                out.flush();
            }
        } catch(Throwable e) {
            GlobalExceptionHandler.handle(e);
        } finally {
            if(out != null && closeOnExit) {
                try {
                    out.close();
                } catch(IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        hasCompleted = true;
    }

    public boolean hasCompleted() {
        return hasCompleted;
    }

    private BufferedReader in;

    private BufferedWriter out;

    private boolean hasCompleted = false, closeOnExit;
}