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
        return new PlottableChunk(FULL_DAY, 0, 1, 2000, PIXELS, CHAN_ID);
    }

    public void testPutThenGet() throws SQLException, IOException {
        plottDb.put(new PlottableChunk[] {data});
        PlottableChunk[] out = plottDb.get(data.getTimeRange(),
                                           data.getChannel(),
                                           data.getPixelsPerDay());
        assertEquals(data, out[0]);
    }

    public void testPutTwoDaysGetOne() throws SQLException, IOException {
        PlottableChunk secondDay = new PlottableChunk(data.getData(),
                                                      0,
                                                      2,
                                                      2000,
                                                      PIXELS,
                                                      CHAN_ID);
        plottDb.put(new PlottableChunk[] {data, secondDay});
        MicroSecondDate halfPastFirstDay = START.add((TimeInterval)ONE_DAY.divideBy(2));
        MicroSecondTimeRange halfFirstToHalfSecond = new MicroSecondTimeRange(halfPastFirstDay,
                                                                              ONE_DAY);
        PlottableChunk[] out = plottDb.get(halfFirstToHalfSecond,
                                           data.getChannel(),
                                           data.getPixelsPerDay());
        int halfLength = data.getData().x_coor.length / 2;
        int[] x = new int[halfLength];
        int[] y = new int[halfLength];
        for(int i = 0; i < halfLength; i++) {
            x[i] = (i + halfLength) / 2;
        }
        System.arraycopy(data.getData().y_coor, halfLength, y, 0, halfLength);
        PlottableChunk secondHalfFirstDay = new PlottableChunk(new Plottable(x,
                                                                             y),
                                                               halfLength,
                                                               1,
                                                               2000,
                                                               PIXELS,
                                                               CHAN_ID);
        assertEquals(secondHalfFirstDay, out[0]);
    }

    public void testUpdate() throws SQLException, IOException {
        for(int i = 1; i < 20; i += 3) {
            plottDb.put(breakIntoPieces(data, i));
        }
        assertEquals(data, plottDb.get(data.getTimeRange(),
                                       data.getChannel(),
                                       data.getPixelsPerDay())[0]);
    }

    public void testYearStraddlingData() throws CodecException, SQLException,
            IOException {
        Time start = new Time("19991231T120000.000Z", 0);
        MicroSecondDate startDate = new MicroSecondDate(start);
        Plottable p = makeDay(startDate);
        PlottableChunk chunk = new PlottableChunk(p,
                                                  PIXELS / 2,
                                                  startDate,
                                                  PIXELS,
                                                  CHAN_ID);
        plottDb.put(new PlottableChunk[] {chunk});
        PlottableChunk[] results = plottDb.get(chunk.getTimeRange(),
                                               chunk.getChannel(),
                                               chunk.getPixelsPerDay());
        assertEquals(chunk.getTimeRange().getBeginTime(),
                     results[0].getBeginTime());
        assertEquals(chunk.getTimeRange().getEndTime(), results[1].getEndTime());
    }

    private static PlottableChunk[] breakIntoPieces(PlottableChunk original,
                                                    int numPieces) {
        PlottableChunk[] pieces = new PlottableChunk[numPieces];
        double pieceSize = PIXELS / (double)numPieces;
        for(int i = 0; i < pieces.length; i++) {
            int startPixel = (int)Math.floor(i * pieceSize);
            int stopPixel = (int)Math.floor((i + 1) * pieceSize);
            if(i == pieces.length - 1) {
                stopPixel = PIXELS;
            }
            pieces[i] = makeSubPlottable(original, startPixel, stopPixel
                    - startPixel);
        }
        return pieces;
    }

    private static PlottableChunk makeSubPlottable(PlottableChunk orig,
                                                   int startPoint,
                                                   int numPixels) {
        Plottable subPlott = new Plottable(copy(orig.getData().x_coor,
                                                startPoint,
                                                numPixels * 2),
                                           copy(orig.getData().y_coor,
                                                startPoint,
                                                numPixels * 2));
        PlottableChunk chunk = new PlottableChunk(subPlott,
                                                  startPoint,
                                                  1,
                                                  2000,
                                                  PIXELS,
                                                  orig.getChannel());
        return chunk;
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

    public static final int SPD = PIXELS * 2;

    private static final Time START_TIME = new Time("20000101T000000.000Z", 0);

    private static final MicroSecondDate START = new MicroSecondDate(START_TIME);

    public static final ChannelId CHAN_ID = MockChannelId.createVerticalChanId();

    private static Plottable FULL_DAY = null;
    static {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%C{1}.%M - %m\n")));
        try {
            FULL_DAY = makeDay(START);
        } catch(CodecException e) {
            e.printStackTrace();
        }
    }

    public static Plottable makeDay(MicroSecondDate startTime)
            throws CodecException {
        MicroSecondDate end = startTime.add(ONE_DAY);
        LocalSeismogramImpl seis = SimplePlotUtil.createSpike(startTime,
                                                              ONE_DAY,
                                                              757,
                                                              CHAN_ID);
        MicroSecondTimeRange fullRange = new MicroSecondTimeRange(startTime,
                                                                  end);
        int[][] coords = SimplePlotUtil.makePlottable(seis, fullRange, SPD);
        return new Plottable(coords[0], coords[1]);
    }

    private PlottableChunk data;

    private JDBCPlottable plottDb;
}