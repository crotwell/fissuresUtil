package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;


public class Arithmatic {
    
    public static LocalSeismogramImpl mul(LocalSeismogramImpl seis, float val) throws FissuresException {
        float[] data = seis.get_as_floats();
        float[] out = new float[data.length];
        for(int i = 0; i < data.length; i++) {
            out[i] = data[i]*val;
        }
        return new LocalSeismogramImpl(seis, out);
    }
    
    public static LocalSeismogramImpl div(LocalSeismogramImpl seis, float val) throws FissuresException {
        return mul(seis, 1/val);
    }
}
