package edu.sc.seis.fissuresUtil.bag.opencl;

import com.nativelibs4java.opencl.CLEvent;

public abstract class CLEventResult {

    public CLEventResult(CLEvent... eventsToWaitFor) {
        this.eventsToWaitFor = eventsToWaitFor;
    }

    public CLEvent[] getEventsToWaitFor() {
        return eventsToWaitFor;
    }

    CLEvent[] eventsToWaitFor;

    public static CLEvent[] combineEvents(CLEventResult a, CLEventResult b) {
        return combineEvents(a.getEventsToWaitFor(), b.getEventsToWaitFor());
    }
    
    public static CLEvent[] combineEvents(CLEvent[] a, CLEvent... b) {
        CLEvent[] out = new CLEvent[a.length+b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
}
