package edu.sc.seis.fissuresUtil.bag.opencl;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLEvent;


public class FloatResult extends CLEventResult {

    public FloatResult(Pointer<Float> result, CLEvent... eventsToWaitFor) {
        super(eventsToWaitFor);
        this.result = result;
    }
    
    
    public Pointer<Float> getResult() {
        return result;
    }

    public float getAfterWait() {
        CLEvent.waitFor(getEventsToWaitFor());
        return getResult().get(0);
    }
    
    Pointer<Float> result;
}
