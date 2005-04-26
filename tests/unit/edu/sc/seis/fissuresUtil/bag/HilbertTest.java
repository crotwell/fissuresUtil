package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.freq.Cmplx;
import junit.framework.TestCase;


/**
 * @author crotwell
 * Created on Apr 26, 2005
 */
public class HilbertTest extends TestCase {

    public void testHilbert() throws Exception {
        Hilbert hilbert = new Hilbert();
        LocalSeismogramImpl testSeis = SimplePlotUtil.createDelta();
        LocalSeismogramImpl hilbertSeis = hilbert.apply(testSeis);
        
        Cmplx[] c = Cmplx.fft(testSeis.get_as_floats());
        Cmplx[] h = Cmplx.fft(hilbertSeis.get_as_floats());
        
        for(int i = 0; i < hilbertSeis.num_points && i < 10; i++) {
            assertEquals(hilbertSeis.get_as_floats()[i], (i%2==0 || i==0 ? 0 : (2/(Math.PI*i))), 0.0001);
        }
    }
    
}
