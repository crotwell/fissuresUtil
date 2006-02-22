package edu.sc.seis.fissuresUtil.database.seismogram;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.BasicConfigurator;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.sc.seis.fissuresUtil.database.ConnectionCreator;
import edu.sc.seis.fissuresUtil.database.network.JDBCStation;
import edu.sc.seis.fissuresUtil.simple.Initializer;

public class StationCodeUpdater {

    public static void main(String[] args) throws SQLException, IOException {
        BasicConfigurator.configure();
        Properties props = Initializer.loadProperties(args);
        ConnectionCreator connCreator = new ConnectionCreator(props);
        Connection conn = connCreator.createConnection();
        JDBCSeismogramFiles jdbcSeisFile = new JDBCSeismogramFiles(conn);
        JDBCStation jdbcStation = new JDBCStation(conn);
        boolean verbose = false;
        for(int i = 1; i < args.length; i++) {
            if(args[i].equals("-h")) {
                printHelp();
            }
        }
        for(int i = 1; i < args.length; i++) {
            if(args[i].equals("-v")) {
                verbose = true;
                System.out.println();
                System.out.println("/---------------Station Code Updater---");
                System.out.println();
                System.out.println("Verbose messages: ON");
            }
        }
        for(int i = 1; i < args.length - 1; i++) {
            if(verbose) {
                if(args[i].equals("-props")) {
                    String propFileLocation = args[i + 1];
                    File file = new File(propFileLocation);
                    System.out.println("Properties file location: "
                            + file.getCanonicalPath());
                }
            }
        }
        if(verbose) {
            System.out.println();
            System.out.println("\\------------------------------------");
        }
        if(args.length > 4) {
            String oldName = args[args.length - 2];
            String newName = args[args.length - 1];
            if(verbose) {
                int numOld = numOfStationCodes(jdbcStation, oldName);
                System.out.println();
                System.out.println("Updating " + numOld + " instances of "
                        + oldName + " to " + newName + ".");
            }
            jdbcSeisFile.updateStationCode(oldName, newName);
            if(verbose) {
                int numNew = numOfStationCodes(jdbcStation, newName);
                System.out.println("Now " + numNew + " instances of " + newName
                        + ".");
                System.out.println();
                System.out.println("Update complete.");
            }
        } else {
            printHelp();
        }
    }

    private static int numOfStationCodes(JDBCStation jdbcStation,
                                         String stationCode)
            throws SQLException {
        StationId[] stationIds = jdbcStation.getAllStationIds();
        int numOfStationCode = 0;
        for(int i = 0; i < stationIds.length; i++) {
            if(stationIds[i].station_code.equals(stationCode)) {
                numOfStationCode++;
            }
        }
        return numOfStationCode;
    }

    private static void printHelp() {
        System.out.println();
        System.out.println("    The last argument must be a String representing the new name.");
        System.out.println("    The next to last argument must be a String representing the old name.");
        System.out.println("    The default database properties file is server.properties.");
        System.out.println();
        System.out.println("    -props   | Accepts alternate database properties file");
        System.out.println("                Use the same prop file used for DB population");
        System.out.println("    -v       | Turn verbose messages on");
        System.out.println();
        System.out.println();
        System.out.println("Program finished before database update was completed.");
        System.out.println();
    }
}