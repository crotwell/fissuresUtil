package edu.sc.seis.fissuresUtil.simple;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * @author crotwell Created on May 9, 2005
 */
public class TimeOMatic {

    public static void setWriter(Writer writer) {
        if(writer instanceof BufferedWriter) {
            out = (BufferedWriter)writer;
        } else {
            out = new BufferedWriter(writer);
        }
    }

    public static void start() {
        Thread t = Thread.currentThread();
        // don't use ClockUtil.now here since we only need relative times
        times.put(t, new MicroSecondDate());
    }

    public static TimeInterval time() {
        Thread t = Thread.currentThread();
        MicroSecondDate then = (MicroSecondDate)times.get(t);
        if(then == null) {
            start();
            return new TimeInterval(0, UnitImpl.SECOND);
        }
        MicroSecondDate now = new MicroSecondDate();
        times.put(t, now);
        return now.subtract(then);
    }

    public static void print(String msg) {
        try {
            out.write("[" + Thread.currentThread().getName() + "] " + msg
                    + ": " + time().convertTo(UnitImpl.SECOND));
            out.newLine();
            out.flush();
        } catch(IOException e) {
            GlobalExceptionHandler.handle(e);
        }
    }

    static BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));

    private static HashMap times = new HashMap();
}
