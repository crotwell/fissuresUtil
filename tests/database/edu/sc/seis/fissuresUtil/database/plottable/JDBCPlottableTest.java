package edu.sc.seis.fissuresUtil.database.plottable;

import java.io.IOException;
import java.sql.SQLException;
import junit.framework.TestCase;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannelId;

/**
 * @author crotwell Created on Sep 23, 2004
 */
public class JDBCPlottableTest extends TestCase {

    public PlottableChunk createFullDayPlottable() throws CodecException {
        MicroSecondDate end = START.add(ONE_DAY);
        LocalSeismogramImpl seis = SimplePlotUtil.createSpike(START, ONE_DAY);
        MicroSecondTimeRange fullRange = new MicroSecondTimeRange(START, end);
        int[][] coords = SimplePlotUtil.compressXvalues(seis, fullRange, PIXELS);
        Plottable plottable = new Plottable(coords[0], coords[1]);
        return new PlottableChunk(plottable, START, SPS, CHAN_ID);
    }

    public void testPut() throws SQLException, IOException, CodecException {
        PlottableChunk data = createFullDayPlottable();
        JDBCPlottable jdbcPlot = new JDBCPlottable();
        jdbcPlot.put(new PlottableChunk[] {data});
        MicroSecondTimeRange range = new MicroSecondTimeRange(data.getStartTime(),
                                                              data.getStartTime()
                                                                      .add(ONE_DAY));
        PlottableChunk[] out = jdbcPlot.get(range,
                                            data.getChannel(),
                                            data.getSamplesPerSecond());
        assertEquals(data, out[0]);
    }

    public static final TimeInterval ONE_DAY = new TimeInterval(1, UnitImpl.DAY);

    public static final double SECONDS_IN_DAY = ONE_DAY.convertTo(UnitImpl.SECOND)
            .getValue();

    public static final int PIXELS = 6000;

    public static final double SPS = PIXELS / SECONDS_IN_DAY;

    private static final Time START_TIME = new Time("19991231T235959.000Z", 0);

    private static final MicroSecondDate START = new MicroSecondDate(START_TIME);

    public static final ChannelId CHAN_ID = MockChannelId.createVerticalChanId();
}