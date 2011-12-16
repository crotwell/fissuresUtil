package edu.sc.seis.fissuresUtil.display.drawable;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.DataInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

public class SeismogramShapeTest extends TestCase {

    public void testExpansionPlot() throws IOException, FissuresException,
            CodecException {
        BasicConfigurator.configure();
        SacTimeSeries timeSeries = new SacTimeSeries();
        timeSeries.read(new DataInputStream(SeismogramShapeTest.class.getClassLoader()
                .getResourceAsStream("edu/sc/seis/fissuresUtil/display/drawable/recfunctest")));
        LocalSeismogramImpl seis = SacToFissures.getSeismogram(timeSeries);
        SeismogramShape ss = new SeismogramShape(null,
                                                 new MemoryDataSetSeismogram(seis));
        MicroSecondTimeRange tr = new MicroSecondTimeRange(seis.getBeginTime(),
                                                           new TimeInterval(2,
                                                                            UnitImpl.MINUTE));

        checkMinAndMaxInPathIterator(seis, ss, tr, new Dimension(4000, 400));
        checkMinAndMaxInPathIterator(seis, ss, tr, new Dimension(700, 400));
        checkMinAndMaxInPathIterator(seis, ss, tr, new Dimension(100, 400));
    }

    private void checkMinAndMaxInPathIterator(LocalSeismogramImpl seis, SeismogramShape ss, MicroSecondTimeRange tr, Dimension dimension) throws CodecException {
        for(int j = 0; j < 9; j++) {
            ss.update(tr.shift(.01 * j),
                      seis.getAmplitudeRange(),
                      dimension);
            PathIterator pit = ss.getPathIterator(new AffineTransform());
            float[] coords = new float[6];
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;
            while(!pit.isDone()) {
                pit.currentSegment(coords);
                if(coords[1] < min) {
                    min = coords[1];
                }
                if(coords[1] > max) {
                    max = coords[1];
                }
                pit.next();
            }
            assertEquals(0, min, .1);
            assertEquals(400, max, .1);
        }
    }
}
