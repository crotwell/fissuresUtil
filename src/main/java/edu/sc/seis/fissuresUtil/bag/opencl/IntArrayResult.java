package edu.sc.seis.fissuresUtil.bag.opencl;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLQueue;


public class IntArrayResult  extends CLEventResult {

    public IntArrayResult(CLBuffer<Integer> result, CLEvent... eventsToWaitFor) {
        super(eventsToWaitFor);
        this.result = result;
    }

    public CLBuffer<Integer> getResult() {
        return result;
    }

    public int[] getAfterWait(CLQueue queue) {
        Pointer<Integer> outPtr =  result.read(queue, getEventsToWaitFor());
        return outPtr.getInts();
    }
    
    CLBuffer<Integer> result;
}

