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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.time.RangeTool;

public class JDBCPlottable extends PlottableTable {

    public JDBCPlottable() throws SQLException {
        this(ConnMgr.createConnection());
    }

    public JDBCPlottable(Connection conn) throws SQLException {
        super("plottable", conn);
        chanTable = new JDBCChannel(conn);
        if(!DBUtil.tableExists("plottable", conn)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("plottable.create"));
        }
        prepareStatements();
    }
    
    public void put(PlottableChunk[] chunks) throws SQLException, IOException {
        MicroSecondTimeRange stuffInDB = RangeTool.getFullTime(chunks);
        //TODO get everything for start and end day as well, in case the db has more
        PlottableChunk[] dbChunks = get(stuffInDB,
                                        chunks[0].getChannel(),
                                        chunks[0].getSamplesPerSecond());
        PlottableChunk[] everything = new PlottableChunk[chunks.length
                + dbChunks.length];
        System.arraycopy(dbChunks, 0, everything, 0, dbChunks.length);
        System.arraycopy(chunks, 0, everything, dbChunks.length, chunks.length);
        logger.debug("Merging " + everything.length + " chunks");
        everything = merge(everything);
        logger.debug("Breaking "
                + everything.length
                + " remaining chunks after merge into seperate chunks based on day");
        everything = breakIntoDays(everything);
        logger.debug("Adding " + everything.length + " chunks split on days");
        int rowsDropped = drop(stuffInDB,
                               chunks[0].getChannel(),
                               chunks[0].getSamplesPerSecond());
        logger.debug("Dropped " + rowsDropped
                + " rows of stuff that new data covered");
        for(int i = 0; i < everything.length; i++) {
            logger.debug("Adding chunk " + i + ": " + everything[i]);
            int stmtIndex = 1;
            PlottableChunk chunk = everything[i];
            put.setInt(stmtIndex++, chanTable.put(chunk.getChannel()));
            put.setDouble(stmtIndex++, chunk.getSamplesPerSecond());
            put.setTimestamp(stmtIndex++, chunk.getBeginTime().getTimestamp());
            put.setTimestamp(stmtIndex++, chunk.getEndTime().getTimestamp());
            int[] y = chunk.getData().y_coor;
            put.setInt(stmtIndex++, y.length);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            for(int k = 0; k < y.length; k++) {
                dos.writeInt(y[k]);
            }
            put.setBytes(stmtIndex++, out.toByteArray());
            put.executeUpdate();
        }
    }

    private PlottableChunk[] breakIntoDays(PlottableChunk[] chunks) {
        List results = new ArrayList();
        Calendar curCal = makeCal();
        Calendar endCal = makeCal();
        for(int i = 0; i < chunks.length; i++) {
            int numBefore = results.size();
            PlottableChunk chunk = chunks[i];
            curCal.setTime(chunk.getBeginTime());
            endCal.setTime(chunk.getEndTime());
            while(!curCal.equals(endCal)) {
                PlottableChunk trimmed = trimToDay(curCal, chunk);
                results.add(trimmed);
                curCal.setTime(trimmed.getEndTime());
            }
            logger.debug("Broke " + chunk + " into "
                    + (results.size() - numBefore) + " pieces");
        }
        return (PlottableChunk[])results.toArray(new PlottableChunk[0]);
    }

    private PlottableChunk trimToDay(Calendar dayCal, PlottableChunk chunk) {
        MicroSecondDate beginTime = new MicroSecondDate(dayCal.getTime());
        Calendar chunkCal = makeCal();
        chunkCal.setTime(chunk.getBeginTime());
        if(chunkCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)) {
            beginTime = chunk.getBeginTime();
        }
        MicroSecondDate endTime = chunk.getEndTime();
        chunkCal.setTime(chunk.getEndTime());
        if(chunkCal.get(Calendar.DAY_OF_YEAR) != dayCal.get(Calendar.DAY_OF_YEAR)) {
            dayCal.add(Calendar.DAY_OF_YEAR, 1);
            endTime = new MicroSecondDate(dayCal.getTime());
        }
        MicroSecondTimeRange tr = new MicroSecondTimeRange(beginTime, endTime);
        return new PlottableChunk(new Plottable(null, fill(tr, chunk)),
                                  beginTime,
                                  chunk);
    }

    private static Calendar makeCal() {
        return Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
    }

    /**
     * Combines adjacent and overlapping chunks. Assumes that the given chunks
     * all belong to the same channel and have the same number of samples per
     * second
     */
    private PlottableChunk[] merge(PlottableChunk[] chunks) {
        chunks = (PlottableChunk[])chunks.clone();
        for(int i = 0; i < chunks.length; i++) {
            PlottableChunk chunk = chunks[i];
            for(int j = i + 1; j < chunks.length; j++) {
                PlottableChunk chunk2 = chunks[j];
                if(RangeTool.areContiguous(chunk, chunk2)
                        || RangeTool.areOverlapping(chunk, chunk2)) {
                    chunks[j] = merge(chunk, chunk2);
                    chunks[i] = null;
                    break;
                }
            }
        }
        List results = new ArrayList();
        for(int i = 0; i < chunks.length; i++) {
            if(chunks[i] != null) {
                results.add(chunks[i]);
            }
        }
        return (PlottableChunk[])results.toArray(new PlottableChunk[0]);
    }

    private PlottableChunk merge(PlottableChunk chunk, PlottableChunk chunk2) {
        MicroSecondTimeRange fullRange = new MicroSecondTimeRange(chunk.getTimeRange(),
                                                                  chunk2.getTimeRange());
        logger.debug("Merging " + chunk + " and " + chunk2 + " into "
                + fullRange);
        int samples = (int)Math.floor(chunk.getSamplesPerSecond()
                * fullRange.getInterval().convertTo(UnitImpl.SECOND).value);
        int[] y = new int[samples];
        fill(fullRange, y, chunk);
        fill(fullRange, y, chunk2);
        Plottable mergedData = new Plottable(null, y);
        return new PlottableChunk(mergedData,
                                  fullRange.getBeginTime(),
                                  chunk.getSamplesPerSecond(),
                                  chunk.getChannel());
    }

    private static int getSamples(double samplesPerSecond,
                                  MicroSecondTimeRange tr) {
        TimeInterval inter = tr.getInterval();
        inter = (TimeInterval)inter.convertTo(UnitImpl.SECOND);
        double samples = samplesPerSecond * inter.getValue();
        return (int)Math.floor(samples);
    }

    private static int[] fill(MicroSecondTimeRange tr, PlottableChunk chunk) {
        return fill(tr,
                    new int[getSamples(chunk.getSamplesPerSecond(), tr)],
                    chunk);
    }

    private static int[] fill(MicroSecondTimeRange fullRange,
                              int[] y,
                              PlottableChunk chunk) {
        MicroSecondDate rowBeginTime = chunk.getBeginTime();
        int offsetIntoRequestSamples = SimplePlotUtil.getPixel(y.length,
                                                               fullRange,
                                                               rowBeginTime);
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
                    double samplesPerSecond) throws SQLException {
        int chanDbId;
        try {
            chanDbId = chanTable.getDBId(id);
        } catch(NotFound e) {
            logger.info("Channel " + ChannelIdUtil.toStringNoDates(id)
                    + " not found");
            return 0;
        }
        drop.setTimestamp(1, requestRange.getEndTime().getTimestamp());
        drop.setTimestamp(2, requestRange.getBeginTime().getTimestamp());
        drop.setInt(3, chanDbId);
        drop.setDouble(4, samplesPerSecond);
        return drop.executeUpdate();
    }

    public PlottableChunk[] get(MicroSecondTimeRange requestRange,
                                ChannelId id,
                                double samplesPerSecond) throws SQLException,
            IOException {
        int chanDbId;
        try {
            chanDbId = chanTable.getDBId(id);
        } catch(NotFound e) {
            logger.info("Channel " + ChannelIdUtil.toStringNoDates(id)
                    + " not found");
            return new PlottableChunk[0];
        }
        get.setTimestamp(1, requestRange.getEndTime().getTimestamp());
        get.setTimestamp(2, requestRange.getBeginTime().getTimestamp());
        get.setInt(3, chanDbId);
        get.setDouble(4, samplesPerSecond);
        ResultSet rs = get.executeQuery();
        List chunks = new ArrayList();
        int requestSamples = getSamples(samplesPerSecond, requestRange);
        logger.debug("Request made for " + requestSamples + " from "
                + requestRange + " at " + samplesPerSecond);
        while(rs.next()) {
            Timestamp ts = rs.getTimestamp("start_time");
            MicroSecondDate rowBeginTime = new MicroSecondDate(ts);
            int offsetIntoRequestSamples = SimplePlotUtil.getPixel(requestSamples,
                                                                   requestRange,
                                                                   rowBeginTime);
            int numSamples = rs.getInt("sample_count");
            int firstSampleForRequest = 0;
            if(offsetIntoRequestSamples < 0) {
                //This db row has data starting before the request, start at
                // pertinent point
                firstSampleForRequest = -1 * offsetIntoRequestSamples;
            }
            int lastSampleForRequest = numSamples;
            if(offsetIntoRequestSamples + numSamples > requestSamples) {
                //This row has more data than was requested in it, only get
                // enough to fill the request
                lastSampleForRequest = requestSamples
                        - offsetIntoRequestSamples;
            }
            int samplesUsed = lastSampleForRequest - firstSampleForRequest;
            int[] x = new int[samplesUsed];
            int[] y = new int[samplesUsed];
            byte[] dataBytes = rs.getBytes("data");
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dataBytes));
            for(int i = 0; i < firstSampleForRequest; i++) {
                dis.readInt();
            }
            for(int i = 0; i < samplesUsed; i++) {
                x[i] = (firstSampleForRequest + i) / 2;
                y[i] = dis.readInt();
            }
            Plottable p = new Plottable(x, y);
            PlottableChunk pc = new PlottableChunk(p,
                                                   rowBeginTime.add(new TimeInterval(firstSampleForRequest
                                                                                             / samplesPerSecond,
                                                                                     UnitImpl.SECOND)),
                                                   samplesPerSecond,
                                                   id);
            chunks.add(pc);
            logger.debug("Returning " + pc + " from chunk starting at "
                    + rowBeginTime);
        }
        return (PlottableChunk[])chunks.toArray(new PlottableChunk[chunks.size()]);
    }

    private PreparedStatement put, get, drop;

    private JDBCChannel chanTable;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JDBCPlottable.class);
}