package edu.sc.seis.fissuresUtil.time;

import junit.framework.TestCase;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

public class ReduceToolCutOverlapTest extends TestCase {

    public void testEmpty() throws FissuresException {
        assertEquals(0,
                     ReduceTool.cutOverlap(new LocalSeismogramImpl[0]).length);
    }

    public void testSimpleOverlap() throws FissuresException {
        LocalSeismogramImpl[] overlaps = ReduceToolTest.createOverlapping();
        LocalSeismogramImpl[] result = ReduceTool.cutOverlap(overlaps);
        assertEquals(1, result.length);
        assertEquals(overlaps[0].getBeginTime(), result[0].getBeginTime());
        assertEquals(overlaps[0].num_points * 1.5, result[0].num_points, 1.0);
    }

    public void testContiguous() throws FissuresException {
        assertEquals(2,
                     ReduceTool.cutOverlap(ReduceToolTest.createContiguous()).length);
    }

    public void testEquals() throws FissuresException {
        assertEquals(1,
                     ReduceTool.cutOverlap(ReduceToolTest.createEqual()).length);
    }

    public void testComplexOverlap() throws FissuresException {
        LocalSeismogramImpl[] contig = ReduceToolTest.createContiguous();
        LocalSeismogramImpl[] overlap = ReduceToolTest.createOverlapping();
        LocalSeismogramImpl[] contigAndOverlap = new LocalSeismogramImpl[] {contig[0],
                                                                            contig[1],
                                                                            overlap[1]};
        assertEquals(1, ReduceTool.cutOverlap(contigAndOverlap).length);
    }
}
