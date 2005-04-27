package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.freq.Cmplx;


/**
 * See http://www.mers.byu.edu/docs/reports/MERS9505.pdf for info on the hilbert transform.
 * 
 * @author crotwell
 * Created on Apr 25, 2005
 */
public class Hilbert implements LocalSeismogramFunction {

    public Hilbert() {
    }

    public LocalSeismogramImpl apply(LocalSeismogramImpl seis) throws FissuresException {
        Cmplx[] c = Cmplx.fft(seis.get_as_floats());
        for(int i = 0; i < c.length/2; i++) {
            double tmp = c[i].i;
            c[i].i = c[i].r;
            c[i].r = -tmp;
        }
        for(int i = c.length/2; i < c.length; i++) {
            double tmp = c[i].i;
            c[i].i = -c[i].r;
            c[i].r = tmp;
        }
        return new LocalSeismogramImpl(seis, Cmplx.fftInverse(c, seis.getNumPoints()));
    }
    
    public Cmplx[] analyticSignal(LocalSeismogramImpl seis) throws FissuresException {
        float[] seisData = seis.get_as_floats();
        LocalSeismogramImpl hilbert = apply(seis);
        float[] hilbertData = hilbert.get_as_floats();
        Cmplx[] out = new Cmplx[seis.getNumPoints()];
        for(int i = 0; i < out.length; i++) {
            out[i] = new Cmplx(seisData[i], hilbertData[i]);
        }
        return out;
    }
    
    public double[] unwrapPhase(Cmplx[] data) {
        double[] out = new double[data.length];
        int wraps = 0;
        double a = data[0].phs();
        out[0] = a;
        double b = data[1].phs();
        out[1] = b;
        double c;
        for(int i = 2; i < out.length; i++) {
            c = data[i].phs();
            if (2*b-a > Math.PI && c < 0) {
                // unwrap up
                wraps++;
            } else if (2*b-a < -Math.PI && c > 0) {
                // unwrap down
                wraps--;
            }
            out[i] = c + wraps*2*Math.PI;
            a = b;
            b = c;
        }
        return out;
    }
}
