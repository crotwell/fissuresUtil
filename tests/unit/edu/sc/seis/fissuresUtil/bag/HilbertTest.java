package edu.sc.seis.fissuresUtil.bag;

import java.io.DataInputStream;
import java.io.IOException;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.freq.Cmplx;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;
import junit.framework.TestCase;


/**
 * @author crotwell
 * Created on Apr 26, 2005
 */
public class HilbertTest extends TestCase {

    public void testImpluseResponse() throws Exception {
        Hilbert hilbert = new Hilbert();
        LocalSeismogramImpl testSeis = SimplePlotUtil.createDelta();
        LocalSeismogramImpl hilbertSeis = hilbert.apply(testSeis);
        
        Cmplx[] c = Cmplx.fft(testSeis.get_as_floats());
        Cmplx[] h = Cmplx.fft(hilbertSeis.get_as_floats());
        
        for(int i = 0; i < hilbertSeis.num_points && i < 10; i++) {
            assertEquals(hilbertSeis.get_as_floats()[i], (i%2==0 || i==0 ? 0 : (2/(Math.PI*i))), 0.0001);
        }
    }
    
    public void testAnalyticSignal() throws FissuresException {
        Hilbert hilbert = new Hilbert();
        LocalSeismogramImpl testSeis = SimplePlotUtil.createDelta();
        Cmplx[] c = Cmplx.fft(testSeis.get_as_floats());
        Cmplx[] a = hilbert.analyticSignal(testSeis);
        for(int i = 0; i < a.length; i++) {
            assertEquals(testSeis.get_as_floats()[i], a[i].r, 0.001);
        }
        LocalSeismogramImpl hilbertSeis = hilbert.apply(testSeis);
        for(int i = 0; i < hilbertSeis.get_as_floats().length; i++) {
            assertEquals(hilbertSeis.get_as_floats()[i], a[i].i, 0.001);
        }
    }
    
    public void testVsSAC() throws IOException, FissuresException {
        DataInputStream in =
            new DataInputStream(this.getClass().getClassLoader().getResourceAsStream("edu/sc/seis/fissuresUtil/bag/delta.sac"));
        SacTimeSeries deltaSAC = new SacTimeSeries();
        deltaSAC.read(in);
        in.close();
        in =
            new DataInputStream(this.getClass().getClassLoader().getResourceAsStream("edu/sc/seis/fissuresUtil/bag/hilbert_delta.sac"));
        SacTimeSeries hilbertSAC = new SacTimeSeries();
        hilbertSAC.read(in);
        in.close();
        LocalSeismogramImpl delta = SacToFissures.getSeismogram(deltaSAC);
        LocalSeismogramImpl hilbert = SacToFissures.getSeismogram(hilbertSAC);
        LocalSeismogramImpl fisHilbert = (new Hilbert()).apply(delta);
        for(int i = 0; i < fisHilbert.get_as_floats().length; i++) {
            assertEquals(i+" ", hilbert.get_as_floats()[i], fisHilbert.get_as_floats()[i], 0.01);
        }
    }
}
