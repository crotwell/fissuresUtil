package edu.sc.seis.fissuresUtil.database.problem;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import com.csvreader.CsvReader;
import edu.iris.Fissures.Time;
import edu.sc.seis.fissuresUtil.database.ConnectionCreator;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.JDBCTime;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;

public class JDBCProblemComponentTimeRange extends JDBCTable {

    public JDBCProblemComponentTimeRange() throws SQLException {
        this(new ConnectionCreator(new String[0]).createConnection());
    }

    public JDBCProblemComponentTimeRange(Connection conn) throws SQLException {
        super("problemcomponenttimerange", conn);
        problemTable = new JDBCProblem(conn);
        componentTable = new JDBCProblemComponent(conn);
        timeTable = new JDBCTime(conn);
        seq = new JDBCSequence(conn, "problemcomponenttimerange_seq");
        TableSetup.setup(this,
                         "edu/sc/seis/fissuresUtil/database/problem/problemchannels.vm");
    }

    public ProblemComponentTimeRange getProblemComponentTimeRange(int dbId)
            throws SQLException, NotFound {
        get.setInt(1, dbId);
        return extract(get.executeQuery())[0];
    }

    public ProblemComponentTimeRange[] getAll() throws SQLException, NotFound {
        return extract(getAll.executeQuery());
    }

    private ProblemComponentTimeRange[] extract(ResultSet rs)
            throws SQLException, NotFound {
        List comptrs = new ArrayList();
        int problemId = -1;
        int componentId = -1;
        int startId = -1;
        int endId = -1;
        while(rs.next()) {
            problemId = rs.getInt("problem_id");
            componentId = rs.getInt("component_id");
            try {
                startId = rs.getInt("start_id");
            } catch(SQLException e) {
                logger.info("no start_id in database entry");
            }
            try {
                endId = rs.getInt("end_id");
            } catch(SQLException e) {
                logger.info("no end_id in database entry");
            }
            comptrs.add(new ProblemComponentTimeRange(problemTable.getProblem(problemId),
                                                      componentTable.getName(componentId),
                                                      (startId != -1 ? timeTable.get(startId)
                                                              : null),
                                                      (endId != -1 ? timeTable.get(endId)
                                                              : null)));
        }
        return (ProblemComponentTimeRange[])comptrs.toArray(new ProblemComponentTimeRange[0]);
    }

    public int put(ProblemComponentTimeRange comptr) throws SQLException {
        return put(comptr.getProblem().getStationCode(),
                   comptr.getProblem().getType(),
                   comptr.getProblem().getStatus(),
                   comptr.getComponent(),
                   comptr.getStart(),
                   comptr.getEnd());
    }

    public int[] put(String stationCode,
                     String problemType,
                     String status,
                     String components,
                     String startPasscalTime,
                     String endPasscalTime) throws SQLException {
        Calendar cal = ProblemComponentTimeRange.getCalendarInstance();
        Time startTime = ProblemComponentTimeRange.getTime(startPasscalTime,
                                                           cal);
        Time endTime = ProblemComponentTimeRange.getTime(endPasscalTime, cal);
        String[] split = splitComponents(components);
        int[] ids = new int[split.length];
        for(int i = 0; i < split.length; i++) {
            ids[i] = put(stationCode,
                         problemType,
                         status,
                         split[i],
                         startTime,
                         endTime);
        }
        return ids;
    }

    private int put(String stationCode,
                    String problemType,
                    String status,
                    String component,
                    Time startTime,
                    Time endTime) throws SQLException {
        int problemId = problemTable.put(stationCode, problemType, status);
        int componentId = componentTable.put(component);
        int startId = timeTable.put(startTime);
        int endId = timeTable.put(endTime);
        getDbId.setInt(1, problemId);
        getDbId.setInt(2, componentId);
        getDbId.setInt(3, startId);
        getDbId.setInt(4, endId);
        ResultSet rs = getDbId.executeQuery();
        if(rs.next()) {
            return rs.getInt("comptr_id");
        } else {
            int id = seq.next();
            put.setInt(1, id);
            put.setInt(2, problemId);
            put.setInt(3, componentId);
            put.setInt(4, startId);
            put.setInt(5, endId);
            put.executeUpdate();
            return id;
        }
    }

    public void importProblems(InputStream in) throws IOException, SQLException {
        CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(in)));
        reader.readHeaders();
        while(reader.readRecord()) {
            put(reader.get("station"),
                reader.get("type"),
                reader.get("status"),
                reader.get("components"),
                reader.get("start"),
                reader.get("end"));
        }
    }

    private static String[] splitComponents(String components) {
        StringTokenizer tok = new StringTokenizer(components, ":");
        List compList = new ArrayList();
        while(tok.hasMoreTokens()) {
            compList.add(tok.nextToken());
        }
        return (String[])compList.toArray(new String[0]);
    }

    public static void main(String[] args) throws SQLException, IOException,
            NotFound {
        JDBCProblemComponentTimeRange comptrTable = new JDBCProblemComponentTimeRange();
        comptrTable.importProblems(new BufferedInputStream(new FileInputStream(args[0])));
        ProblemComponentTimeRange[] comptrs = comptrTable.getAll();
        for(int i = 0; i < comptrs.length; i++) {
            System.out.println(comptrs[i]);
        }
    }

    protected PreparedStatement put, get, getAll, getDbId;

    private JDBCSequence seq;

    private JDBCProblem problemTable;

    private JDBCProblemComponent componentTable;

    private JDBCTime timeTable;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JDBCProblemComponentTimeRange.class);
}
