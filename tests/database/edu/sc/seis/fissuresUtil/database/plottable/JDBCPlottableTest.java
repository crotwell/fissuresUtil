package edu.sc.seis.fissuresUtil.database.plottable;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.database.JDBCTest;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannelId;

/**
 * @author crotwell Created on Sep 23, 2004
 */
public class JDBCPlottableTest extends JDBCTest {

    public void setUp() throws SQLException {
        data = createFullDayPlottable();
        plottDb = new JDBCPlottable();
    }

    public static PlottableChunk createFullDayPlottable() {
        return new PlottableChunk(FULL_DAY, START, SPS, CHAN_ID);
    }

    public void testPutThenGet() throws SQLException, IOException {
        plottDb.put(new PlottableChunk[] {data});
        PlottableChunk[] out = plottDb.get(data.getTimeRange(),
                                           data.getChannel(),
                                           data.getSamplesPerSecond());
        assertEquals(data, out[0]);
    }

    public void testPutTwoDaysGetOne() throws SQLException, IOException {
        PlottableChunk secondDay = new PlottableChunk(data.getData(),
                                                      START.add(ONE_DAY),
                                                      SPS,
                                                      CHAN_ID);
        plottDb.put(new PlottableChunk[] {data, secondDay});
        MicroSecondDate halfPastFirstDay = START.add((TimeInterval)ONE_DAY.divideBy(2));
        MicroSecondTimeRange halfFirstToHalfSecond = new MicroSecondTimeRange(halfPastFirstDay,
                                                                              ONE_DAY);
        PlottableChunk[] out = plottDb.get(halfFirstToHalfSecond,
                                           data.getChannel(),
                                           data.getSamplesPerSecond());
        int halfLength = data.getData().x_coor.length / 2;
        int[] x = new int[halfLength];
        int[] y = new int[halfLength];
        for(int i = 0; i < halfLength; i++) {
            x[i] = (i + halfLength) / 2;
        }
        System.arraycopy(data.getData().y_coor, halfLength, y, 0, halfLength);
        PlottableChunk secondHalfFirstDay = new PlottableChunk(new Plottable(x,
                                                                             y),
                                                               halfPastFirstDay,
                                                               SPS,
                                                               CHAN_ID);
        assertEquals(secondHalfFirstDay, out[0]);
    }

    public void testUpdate() throws SQLException, IOException {
        for(int i = 1; i < 10; i++) {
            plottDb.put(breakIntoPieces(data, i));
        }
        assertEquals(data, plottDb.get(data.getTimeRange(),
                                       data.getChannel(),
                                       data.getSamplesPerSecond())[0]);
    }

    private static PlottableChunk[] breakIntoPieces(PlottableChunk original,
                                                    int numPieces) {
        PlottableChunk[] pieces = new PlottableChunk[numPieces];
        TimeInterval pieceSize = (TimeInterval)original.getTimeRange()
                .getInterval()
                .divideBy(numPieces);
        for(int i = 0; i < pieces.length; i++) {
            pieces[i] = makeSubPlottable(original, original.getBeginTime()
                    .add((TimeInterval)pieceSize.multiplyBy(i)), pieceSize);
        }
        return pieces;
    }

    private static PlottableChunk makeSubPlottable(PlottableChunk orig,
                                                   MicroSecondDate startTime,
                                                   TimeInterval length) {
        double offset = startTime.subtract(orig.getBeginTime())
                .convertTo(UnitImpl.SECOND).value;
        int startSample = (int)Math.floor(offset * SPS);
        int numPoints = (int)Math.floor(length.convertTo(UnitImpl.SECOND).value
                * SPS);
        Plottable subPlott = new Plottable(copy(orig.getData().x_coor,
                                                startSample,
                                                numPoints),
                                           copy(orig.getData().y_coor,
                                                startSample,
                                                numPoints));
        return new PlottableChunk(subPlott, startTime, SPS, orig.getChannel());
    }

    private static int[] copy(int[] orig, int startPoint, int numPoints) {
        int[] copy = new int[numPoints];
        System.arraycopy(orig, startPoint, copy, 0, numPoints);
        return copy;
    }

    public static final TimeInterval ONE_DAY = new TimeInterval(1, UnitImpl.DAY);

    public static final double SECONDS_IN_DAY = ONE_DAY.convertTo(UnitImpl.SECOND)
            .getValue();

    public static final int PIXELS = 6000;

    public static final double SPS = PIXELS / SECONDS_IN_DAY * 2;

    private static final Time START_TIME = new Time("20000101T000000.000Z", 0);

    private static final MicroSecondDate START = new MicroSecondDate(START_TIME);

    public static final ChannelId CHAN_ID = MockChannelId.createVerticalChanId();

    private static Plottable FULL_DAY = null;
    static {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%C{1}.%M - %m\n")));
        MicroSecondDate end = START.add(ONE_DAY);
        LocalSeismogramImpl seis = SimplePlotUtil.createSpike(START,
                                                              ONE_DAY,
                                                              757);
        MicroSecondTimeRange fullRange = new MicroSecondTimeRange(START, end);
        try {
            int[][] coords = SimplePlotUtil.compressXvalues(seis,
                                                            fullRange,
                                                            PIXELS);
            FULL_DAY = new Plottable(coords[0], coords[1]);
        } catch(CodecException e) {
            e.printStackTrace();
        }
    }

    private PlottableChunk data;

    private JDBCPlottable plottDb;
}