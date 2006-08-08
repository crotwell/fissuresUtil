package edu.sc.seis.fissuresUtil.database.plottable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.time.RangeTool;
import edu.sc.seis.fissuresUtil.time.ReduceTool;

public class JDBCPlottable extends JDBCTable {

    public JDBCPlottable(Connection conn, String dbType) throws SQLException {
        super("plottable", conn);
        chanTable = new JDBCChannel(conn);
        Context ctx = new VelocityContext();
        ctx.put(dbType, "true");
        TableSetup.setup(getTableName(),
                         conn,
                         this,
                         "edu/sc/seis/fissuresUtil/database/props/plottable/default.props",
                         ctx);
    }

    public void put(PlottableChunk[] chunks) throws SQLException, IOException {
        MicroSecondTimeRange stuffInDB = getDroppingRange(chunks);
        PlottableChunk[] dbChunks = get(stuffInDB,
                                        chunks[0].getChannel(),
                                        chunks[0].getPixelsPerDay());
        logger.debug("got " + dbChunks.length
                + " chunks from stuff that was already in the database");
        logger.debug("combining chunks from database with new chunks");
        PlottableChunk[] everything = new PlottableChunk[chunks.length
                + dbChunks.length];
        System.arraycopy(dbChunks, 0, everything, 0, dbChunks.length);
        System.arraycopy(chunks, 0, everything, dbChunks.length, chunks.length);
        // scrutinizeEverything(everything, "unmerged");
        logger.debug("Merging " + everything.length + " chunks");
        everything = ReduceTool.merge(everything);
        // scrutinizeEverything(everything, "merged");
        logger.debug("Breaking "
                + everything.length
                + " remaining chunks after merge into seperate chunks based on day");
        everything = breakIntoDays(everything);
        // scrutinizeEverything(everything, "split into days");
        logger.debug("Adding " + everything.length + " chunks split on days");
        logger.debug("Dropping data within time range of " + stuffInDB);
        int rowsDropped = drop(stuffInDB,
                               chunks[0].getChannel(),
                               chunks[0].getPixelsPerDay());
        logger.debug("Dropped " + rowsDropped
                + " rows of stuff that new data covered");
        for(int i = 0; i < everything.length; i++) {
            logger.debug("putting chunk " + i + ": " + everything[i]);
            int stmtIndex = 1;
            PlottableChunk chunk = everything[i];
            synchronized(put) {
                try {
                    put.setInt(stmtIndex++, chanTable.put(chunk.getChannel()));
                    put.setInt(stmtIndex++, chunk.getPixelsPerDay());
                    put.setTimestamp(stmtIndex++, chunk.getBeginTime()
                            .getTimestamp());
                    put.setTimestamp(stmtIndex++, chunk.getEndTime()
                            .getTimestamp());
                    int[] y = chunk.getData().y_coor;
                    put.setInt(stmtIndex++, y.length / 2);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(out);
                    for(int k = 0; k < y.length; k++) {
                        dos.writeInt(y[k]);
                    }
                    put.setBytes(stmtIndex++, out.toByteArray());
                    put.executeUpdate();
                } catch(SQLException ex) {
                    logger.warn("problem with sql query: " + put);
                    SQLException newEx = new SQLException(ex.getMessage()
                            + ". problematic chunk: " + chunk);
                    newEx.setStackTrace(ex.getStackTrace());
                    throw newEx;
                }
            }
        }
    }

    /*
     * use this for debugging various processing steps of plottable chunks in
     * put()
     */
    private static void scrutinizeEverything(PlottableChunk[] everything,
                                             String whatsBeenDone) {
        logger.debug("everything[] " + whatsBeenDone + ":");
        for(int i = 0; i < everything.length; i++) {
            logger.debug(everything[i]);
            Plottable plot = everything[i].getData();
            try {
                for(int j = 0; j < plot.y_coor.length; j += 2) {
                    if(plot.y_coor[j] == 0 || plot.y_coor[j + 1] == 0) {
                        logger.debug(j / 2 + ": " + plot.y_coor[j] + " "
                                + plot.y_coor[j + 1]);
                    }
                }
            } catch(Exception e) {
                logger.debug("something weird happened.");
                logger.debug(e.getMessage());
            }
        }
        logger.debug("end everything[] " + whatsBeenDone);
    }

    private static MicroSecondTimeRange getDroppingRange(PlottableChunk[] chunks) {
        MicroSecondTimeRange stuffInDB = RangeTool.getFullTime(chunks);
        MicroSecondDate startTime = PlottableChunk.stripToDay(stuffInDB.getBeginTime());
        MicroSecondDate strippedEnd = PlottableChunk.stripToDay(stuffInDB.getEndTime());
        if(!strippedEnd.equals(stuffInDB.getEndTime())) {
            strippedEnd = strippedEnd.add(PlottableChunk.ONE_DAY);
        }
        return new MicroSecondTimeRange(startTime, strippedEnd);
    }

    private PlottableChunk[] breakIntoDays(PlottableChunk[] everything) {
        List results = new ArrayList();
        for(int i = 0; i < everything.length; i++) {
            PlottableChunk[] days = everything[i].breakIntoDays();
            for(int j = 0; j < days.length; j++) {
                results.add(days[j]);
            }
        }
        return (PlottableChunk[])results.toArray(new PlottableChunk[0]);
    }

    private static int getPixels(int pixelsPerDay, MicroSecondTimeRange tr) {
        TimeInterval inter = tr.getInterval();
        inter = (TimeInterval)inter.convertTo(UnitImpl.DAY);
        double samples = pixelsPerDay * inter.getValue();
        return (int)Math.floor(samples);
    }

    public static int[] fill(MicroSecondTimeRange fullRange,
                             int[] y,
                             PlottableChunk chunk) {
        MicroSecondDate rowBeginTime = chunk.getBeginTime();
        int offsetIntoRequestSamples = SimplePlotUtil.getPixel(y.length / 2,
                                                               fullRange,
                                                               rowBeginTime) * 2;
        int[] dataY = chunk.getData().y_coor;
        int numSamples = dataY.length;
        int firstSampleForRequest = 0;
        if(offsetIntoRequestSamples < 0) {
            firstSampleForRequest = -1 * offsetIntoRequestSamples;
        }
        int lastSampleForRequest = numSamples;
        if(offsetIntoRequestSamples + numSamples > y.length) {
            lastSampleForRequest = y.length - offsetIntoRequestSamples;
        }
        for(int i = firstSampleForRequest; i < lastSampleForRequest; i++) {
            y[i + offsetIntoRequestSamples] = dataY[i];
        }
        return y;
    }

    public int drop(MicroSecondTimeRange requestRange,
                    ChannelId id,
                    int samplesPerDay) throws SQLException {
        int chanDbId;
        try {
            chanDbId = chanTable.getDBId(id);
        } catch(NotFound e) {
            logger.info("Channel " + ChannelIdUtil.toStringNoDates(id)
                    + " not found");
            return 0;
        }
        synchronized(drop) {
            drop.setTimestamp(1, requestRange.getEndTime().getTimestamp());
            drop.setTimestamp(2, requestRange.getBeginTime().getTimestamp());
            drop.setInt(3, chanDbId);
            drop.setDouble(4, samplesPerDay);
            return drop.executeUpdate();
        }
    }

    public PlottableChunk[] get(MicroSecondTimeRange requestRange,
                                ChannelId id,
                                int pixelsPerDay) throws SQLException,
            IOException {
        int chanDbId;
        try {
            chanDbId = chanTable.getDBId(id);
        } catch(NotFound e) {
            logger.info("Channel " + ChannelIdUtil.toStringNoDates(id)
                    + " not found");
            return new PlottableChunk[0];
        }
        int index = 1;
        ResultSet rs;
        List chunks = new ArrayList();
        int requestPixels = getPixels(pixelsPerDay, requestRange);
        synchronized(get) {
            get.setTimestamp(index++, requestRange.getEndTime().getTimestamp());
            get.setTimestamp(index++, requestRange.getBeginTime()
                    .getTimestamp());
            get.setInt(index++, chanDbId);
            get.setInt(index++, pixelsPerDay);
            rs = get.executeQuery();
            logger.debug("Request made for " + requestPixels + " from "
                    + requestRange + " at " + pixelsPerDay + "ppd");
            while(rs.next()) {
                Timestamp ts = rs.getTimestamp("start_time");
                MicroSecondDate rowBeginTime = new MicroSecondDate(ts);
                int offsetIntoRequestPixels = SimplePlotUtil.getPixel(requestPixels,
                                                                      requestRange,
                                                                      rowBeginTime);
                int numPixels = rs.getInt("pixel_count");
                int firstPixelForRequest = 0;
                if(offsetIntoRequestPixels < 0) {
                    // This db row has data starting before the request, start
                    // at
                    // pertinent point
                    firstPixelForRequest = -1 * offsetIntoRequestPixels;
                }
                int lastPixelForRequest = numPixels;
                if(offsetIntoRequestPixels + numPixels > requestPixels) {
                    // This row has more data than was requested in it, only get
                    // enough to fill the request
                    lastPixelForRequest = requestPixels
                            - offsetIntoRequestPixels;
                }
                int pixelsUsed = lastPixelForRequest - firstPixelForRequest;
                int[] x = new int[pixelsUsed * 2];
                int[] y = new int[pixelsUsed * 2];
                byte[] dataBytes = rs.getBytes("data");
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dataBytes));
                for(int i = 0; i < firstPixelForRequest; i++) {
                    dis.readInt();
                    dis.readInt();
                }
                for(int i = 0; i < pixelsUsed * 2; i++) {
                    // x[i] = firstPixelForRequest + i / 2;
                    x[i] = firstPixelForRequest + offsetIntoRequestPixels + i
                            / 2;
                    y[i] = dis.readInt();
                }
                if(x.length > 0) {
                    logger.debug("x[0]: " + x[0]);
                } else {
                    logger.debug("ZERO LENGTH ARRAY!!!");
                }
                Plottable p = new Plottable(x, y);
                PlottableChunk pc = new PlottableChunk(p,
                                                       PlottableChunk.getPixel(rowBeginTime,
                                                                               pixelsPerDay)
                                                               + firstPixelForRequest,
                                                       PlottableChunk.getJDay(rowBeginTime),
                                                       PlottableChunk.getYear(rowBeginTime),
                                                       pixelsPerDay,
                                                       id);
                chunks.add(pc);
                logger.debug("Returning " + pc + " from chunk starting at "
                        + rowBeginTime);
            }
        }
        return (PlottableChunk[])chunks.toArray(new PlottableChunk[chunks.size()]);
    }

    private PreparedStatement put, get, drop;

    private JDBCChannel chanTable;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JDBCPlottable.class);
}