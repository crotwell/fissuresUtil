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
import java.util.List;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

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
        put = conn.prepareStatement("INSERT INTO plottable (channel_id, "
                + "samples_per_second, start_time, sample_count, data) "
                + "VALUES (?, ?, ?, ?, ?)");
        get = conn.prepareStatement("SELECT * FROM plottable where "
                + "start_time BETWEEN ? AND ? and channel_id = ? and "
                + "samples_per_second = ?");
    }

    public void put(PlottableChunk[] chunks) throws SQLException, IOException {
        for(int i = 0; i < chunks.length; i++) {
            int stmtIndex = 1;
            PlottableChunk chunk = chunks[i];
            put.setInt(stmtIndex++, chanTable.put(chunk.getChannel()));
            put.setDouble(stmtIndex++, chunk.getSamplesPerSecond());
            put.setTimestamp(stmtIndex++, chunk.getStartTime().getTimestamp());
            int[] x = chunk.getData().x_coor;
            int[] y = chunk.getData().y_coor;
            put.setInt(stmtIndex++, x.length);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            for(int k = 0; k < x.length; k++) {
                dos.writeInt(x[k]);
                dos.writeInt(y[k]);
            }
            put.setBytes(stmtIndex++, out.toByteArray());
            put.executeUpdate();
        }
    }

    public PlottableChunk[] get(MicroSecondTimeRange range,
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
        get.setTimestamp(1, range.getBeginTime().getTimestamp());
        get.setTimestamp(2, range.getEndTime().getTimestamp());
        get.setInt(3, chanDbId);
        get.setDouble(4, samplesPerSecond);
        ResultSet rs = get.executeQuery();
        List chunks = new ArrayList();
        while(rs.next()) {
            Timestamp ts = rs.getTimestamp("start_time");
            MicroSecondDate beginTime = new MicroSecondDate(ts);
            int numSamples = rs.getInt("sample_count");
            int[] x = new int[numSamples];
            int[] y = new int[numSamples];
            byte[] dataBytes = rs.getBytes("data");
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dataBytes));
            for(int i = 0; i < y.length; i++) {
                x[i] = dis.readInt();
                y[i] = dis.readInt();
            }
            Plottable p = new Plottable(x, y);
            chunks.add(new PlottableChunk(p, beginTime, samplesPerSecond, id));
        }
        return (PlottableChunk[])chunks.toArray(new PlottableChunk[chunks.size()]);
    }

    private PreparedStatement put, get;

    private JDBCChannel chanTable;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JDBCPlottable.class);
}