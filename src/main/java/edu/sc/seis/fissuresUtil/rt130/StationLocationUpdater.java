package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.Location;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCLocation;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.simple.Initializer;

public class StationLocationUpdater {

    public static void update(Connection conn, Map stationToLocationId)
            throws SQLException {
        Statement stmt = conn.createStatement();
        Iterator it = stationToLocationId.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String code = (String)entry.getKey();
            Object locId = entry.getValue();
            int stationsUpdated = stmt.executeUpdate("UPDATE station SET loc_id = "
                    + locId + " WHERE sta_code = '" + code + "';");
            int siteUpdated = stmt.executeUpdate("UPDATE site SET loc_id = "
                    + locId
                    + " WHERE site_id IN ( select site_id FROM site JOIN station on (station.sta_id = site.sta_id) WHERE station.sta_code = '"
                    + code + "' );");
            logger.debug(stationsUpdated + " station updates and "
                    + siteUpdated + " site updates");
        }
    }

    /**
     * 
     * If the stationToLocation map contains a location not in the db, that
     * location is inserted and the station code entry in the returned map
     * points to that location id. If the location is already in the db, but the
     * station has a different location id, the map contains that station code
     * and the correct location id. If the station already has the correct id in
     * the db, it isn't in the returned map at all.
     * 
     * @return - A map of station code to its correct station id.
     * @throws SQLException
     * @throws NotFound
     */
    public static Map getIncorrectLocations(Connection conn,
                                            Map stationToLocation)
            throws SQLException, NotFound {
        Map stationToCorrectLocId = new HashMap();
        JDBCLocation locationTable = new JDBCLocation(conn);
        Iterator it = stationToLocation.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            Location loc = (Location)entry.getValue();
            int locId = locationTable.put(loc);
            String code = (String)entry.getKey();
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT loc_id FROM station WHERE sta_code = '"
                            + code + "';");
            while(rs.next()) {
                int staLocId = rs.getInt("loc_id");
                if(staLocId != locId) {
                    stationToCorrectLocId.put(code, new Integer(locId));
                    Location incorrectLocation = locationTable.get(staLocId);
                    logger.info(code + "'s location "
                            + XYReader.toString(incorrectLocation)
                            + " being replaced by " + XYReader.toString(loc));
                    break;
                }
            }
        }
        return stationToCorrectLocId;
    }

    public static void main(String[] args) throws FileNotFoundException,
            IOException, SQLException, NotFound {
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals(("--dry-run")) || args[i].equals("-n")) {
                dryRun = true;
                logger.info(args[i] + " specified so no changes being made.");
            }
        }
        Properties props = Initializer.loadProperties(args);
        PropertyConfigurator.configure(props);
        String xyFileLoc = props.getProperty("stationLocationUpdater.xyfile");
        Map stationLocations = XYReader.read(new BufferedReader(new FileReader(xyFileLoc)));
        ConnMgr.installDbProperties(props, args);
        Connection conn = ConnMgr.createConnection();
        Map incorrects = getIncorrectLocations(conn, stationLocations);
        if(!dryRun) {
            update(conn, incorrects);
        }
    }

    private static boolean dryRun = false;

    private static final Logger logger = LoggerFactory.getLogger(StationLocationUpdater.class);
}
