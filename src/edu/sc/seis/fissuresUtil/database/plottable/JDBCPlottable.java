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
import edu.sc.seis.fissuresUtil.time.ReduceTool;

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
        //TODO get everything for start and end day as well, in case the db has
        // more
        PlottableChunk[] dbChunks = get(stuffInDB,
                                        chunks[0].getChannel(),
                                        chunks[0].getSamplesPerDay());
        PlottableChunk[] everything = new PlottableChunk[chunks.length
                + dbChunks.length];
        System.arraycopy(dbChunks, 0, everything, 0, dbChunks.length);
        System.arraycopy(chunks, 0, everything, dbChunks.length, chunks.length);
        logger.debug("Merging " + everything.length + " chunks");
        everything = ReduceTool.merge(everything);
        logger.debug("Breaking "
                + everything.length
                + " remaining chunks after merge into seperate chunks based on day");
        everything = breakIntoDays(everything);
        logger.debug("Adding " + everything.length + " chunks split on days");
        int rowsDropped = drop(stuffInDB,
                               chunks[0].getChannel(),
                               chunks[0].getSamplesPerDay());
        logger.debug("Dropped " + rowsDropped
                + " rows of stuff that new data covered");
        for(int i = 0; i < everything.length; i++) {
            logger.debug("Adding chunk " + i + ": " + everything[i]);
            int stmtIndex = 1;
            PlottableChunk chunk = everything[i];
            put.setInt(stmtIndex++, chanTable.put(chunk.getChannel()));
            put.setInt(stmtIndex++, chunk.getSamplesPerDay());
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

    public static Calendar makeCal() {
        return Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
    }

    private static int getSamples(int samplesPerDay, MicroSecondTimeRange tr) {
        TimeInterval inter = tr.getInterval();
        inter = (TimeInterval)inter.convertTo(UnitImpl.DAY);
        double samples = samplesPerDay * inter.getValue();
        return (int)Math.floor(samples);
    }

    public static int[] fill(MicroSecondTimeRange fullRange,
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
                    int samplesPerDay) throws SQLException {
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
        drop.setDouble(4, samplesPerDay);
        return drop.executeUpdate();
    }

    public PlottableChunk[] get(MicroSecondTimeRange requestRange,
                                ChannelId id,
                                int samplesPerDay) throws SQLException,
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
        get.setTimestamp(index++, requestRange.getEndTime().getTimestamp());
        get.setTimestamp(index++, requestRange.getBeginTime().getTimestamp());
        get.setInt(index++, chanDbId);
        get.setInt(index++, samplesPerDay);
        ResultSet rs = get.executeQuery();
        List chunks = new ArrayList();
        int requestSamples = getSamples(samplesPerDay, requestRange);
        logger.debug("Request made for " + requestSamples + " from "
                + requestRange + " at " + samplesPerDay);
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
                                                   PlottableChunk.getSample(rowBeginTime,
                                                                            samplesPerDay)
                                                           + firstSampleForRequest,
                                                   PlottableChunk.getJDay(rowBeginTime),
                                                   PlottableChunk.getYear(rowBeginTime),
                                                   samplesPerDay,
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