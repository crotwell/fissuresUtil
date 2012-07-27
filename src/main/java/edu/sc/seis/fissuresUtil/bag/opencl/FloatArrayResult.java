package edu.sc.seis.fissuresUtil.bag.opencl;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;

public class FloatArrayResult extends CLEventResult {

    public FloatArrayResult(CLBuffer<Float> result, CLEvent... eventsToWaitFor) {
        super(eventsToWaitFor);
        this.result = result;
    }

    public CLBuffer<Float> getResult() {
        return result;
    }
    
    public long getSize() {
        return result.getElementCount();
    }

    public float[] getAfterWait(CLQueue queue) {
        Pointer<Float> outPtr =  result.read(queue, getEventsToWaitFor());
        return outPtr.getFloats();
    }
    
    CLBuffer<Float> result;
}
