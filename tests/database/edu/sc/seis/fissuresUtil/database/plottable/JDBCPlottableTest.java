package edu.sc.seis.fissuresUtil.database.plottable;

import java.util.Properties;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.mockFissures.MockFERegion;
import edu.sc.seis.fissuresUtil.mockFissures.MockLocation;
import junit.framework.TestCase;


/**
 * @author crotwell
 * Created on Sep 23, 2004
 */
public class JDBCPlottableTest extends TestCase {

    public Plottable createPlottable() throws CodecException {
        LocalSeismogramImpl seis = SimplePlotUtil.createTestData();

        edu.iris.Fissures.Time time =
            new edu.iris.Fissures.Time("19991231T235959.000Z",
                                       -1);
        MicroSecondDate begin = new MicroSecondDate(time);
        MicroSecondDate end = begin.add(new TimeInterval(1, UnitImpl.DAY));
        MicroSecondTimeRange timeRange = new MicroSecondTimeRange(begin, end);
        int[][] coOrdinates;
        coOrdinates =
            SimplePlotUtil.compressXvalues(seis,
                                           timeRange,
                                           new java.awt.Dimension(pixel_size_width,pixel_size_height));


        Plottable plottable = new Plottable(coOrdinates[0], coOrdinates[1]);
        return plottable;
   }

    public void testPut() throws CodecException {
        Plottable plottable = createPlottable();
        JDBCPlottable jdbcPlot = new JDBCPlottable(ConnMgr.createConnection(),
                                                   new Properties());
        int dbid = jdbcPlot.put(plottable);
        Plottable out = jdbcPlot.get(dbid);
    }
    
    public void testGetStatus() {}

    public void testGet() {}
    
    int pixel_size_width=6000;
    int pixel_size_height=0; // doesn't matter, not used
    
}
