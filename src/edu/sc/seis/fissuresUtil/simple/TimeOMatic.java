package edu.sc.seis.fissuresUtil.simple;

import java.util.HashMap;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;


/**
 * @author crotwell
 * Created on May 9, 2005
 */
public class TimeOMatic {

    /**
     *
     */
    public TimeOMatic() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public static void start() {
        Thread t = Thread.currentThread();
        // don't use ClockUtil.now here since we only need relative times
        times.put(t, new MicroSecondDate());
    }
    
    public static TimeInterval time() {
        Thread t = Thread.currentThread();
        MicroSecondDate then = (MicroSecondDate)times.get(t);
        if (then == null) { start(); return new TimeInterval(0, UnitImpl.SECOND);}
        MicroSecondDate now = new MicroSecondDate();
        times.put(t, now);
        return now.subtract(then);
    }
    
    public static void print(String msg) {
        System.out.println("["+Thread.currentThread().getName()+"] "+msg+": "+time().convertTo(UnitImpl.SECOND));
    }
    
    private static HashMap times = new HashMap();
}
