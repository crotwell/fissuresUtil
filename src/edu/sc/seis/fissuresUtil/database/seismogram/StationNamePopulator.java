package edu.sc.seis.fissuresUtil.database.seismogram;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.BasicConfigurator;
import edu.sc.seis.fissuresUtil.database.ConnectionCreator;
import edu.sc.seis.fissuresUtil.simple.Initializer;

public class StationNamePopulator {

    public static void main(String[] args) throws SQLException, IOException {
        BasicConfigurator.configure();
        Properties props = Initializer.loadProperties(args);
        ConnectionCreator connCreator = new ConnectionCreator(props);
        Connection conn = connCreator.createConnection();
        JDBCSeismogramFiles jdbcSeisFile = new JDBCSeismogramFiles(conn);
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
        if(args.length > 0) {
            jdbcSeisFile.populateStationName();
        }
    }

    private static void printHelp() {
        System.out.println();
        System.out.println("    No arguments are needed.");
        System.out.println("    The default SOD properties file is server.properties.");
        System.out.println("    The default database properties file is server.properties.");
        System.out.println();
        System.out.println("    -props   | Accepts alternate SOD properties file");
        System.out.println("                Use the same prop file used for DB population");        
        System.out.println("    -v       | Turn verbose messages on");
        System.out.println();
        System.out.println();
        System.out.println("Program finished before database population was completed.");
        System.out.println();
    }
}