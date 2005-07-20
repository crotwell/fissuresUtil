package edu.sc.seis.fissuresUtil.database.seismogram;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.log4j.BasicConfigurator;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.mseed.FissuresConvert;
import edu.sc.seis.fissuresUtil.rt130.RT130FormatException;
import edu.sc.seis.fissuresUtil.rt130.RT130ToLocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;
import edu.sc.seis.fissuresUtil.simple.Initializer;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.MiniSeedRead;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

public class PopulateDatabaseFromDirectory {

    public static void main(String[] args) throws FissuresException,
            IOException, SeedFormatException, SQLException {
        BasicConfigurator.configure();
        Properties props = Initializer.loadProperties(args);
        ConnMgr.installDbProperties(props, args);
        boolean verbose = false;
        boolean finished = false;
        for(int i = 1; i < args.length; i++) {
            if(args[i].equals("-v")) {
                verbose = true;
            }
        }
        if(args.length > 0) {
            Connection conn = null;
            try {
                conn = ConnMgr.createConnection();
            } catch(SQLException e) {
                System.err.println("Error creating connection.");
                e.printStackTrace();
            }
            String fileLoc = args[args.length - 1];
            File file = new File(fileLoc);
            if(file.isDirectory()) {
                finished = readEntireDirectory(fileLoc, verbose, conn, props);
            } else {
                finished = readSingleFile(fileLoc, verbose, conn, props);
            }
        }
        if(finished) {
            System.out.println();
            System.out.println("Database population complete.");
            System.out.println();
        } else {
            printHelp();
        }
    }

    private static boolean readSingleFile(String fileLoc,
                                          boolean verbose,
                                          Connection conn,
                                          Properties props) throws IOException,
            FissuresException, SeedFormatException, SQLException {
        boolean finished = false;
        StringTokenizer t = new StringTokenizer(fileLoc, "/");
        String fileName = "";
        JDBCSeismogramFiles jdbcSeisFile = new JDBCSeismogramFiles(conn);
        while(t.hasMoreTokens()) {
            fileName = t.nextToken();
        }
        if(fileName.endsWith(".sac")) {
            finished = processSac(jdbcSeisFile, fileLoc, fileName, verbose);
        } else if(fileName.endsWith(".mseed")) {
            finished = processMSeed(jdbcSeisFile, fileLoc, fileName, verbose);
        } else if(fileName.length() == 18 && fileName.charAt(9) == '_') {
            finished = processRefTek(jdbcSeisFile, conn, fileLoc, fileName, verbose, props);
        } else {
            if(verbose) {
                System.out.println(fileName + " is not a valid file.");
            }
        }
        return finished;
    }

    private static boolean readEntireDirectory(String baseDirectory,
                                               boolean verbose,
                                               Connection conn,
                                               Properties props)
            throws FissuresException, IOException, SeedFormatException,
            SQLException {
        File[] files = new File(baseDirectory).listFiles();
        for(int i = 0; i < files.length; i++) {
            if(files[i].isDirectory()) {
                readEntireDirectory(baseDirectory + files[i].getName() + "/",
                                    verbose,
                                    conn, props);
            } else {
                readSingleFile(files[i].getAbsolutePath(), verbose, conn, props);
            }
        }
        return true;
    }

    private static void printHelp() {
        System.out.println();
        System.out.println("    The last argument must be a directory or file location.");
        System.out.println("    The default SOD properties file is server.properties.");
        System.out.println("    The default database properties file is server.properties.");
        System.out.println();
        System.out.println("    -props   | Accepts alternate SOD properties file");
        System.out.println("    -hsql    | Accepts alternate database properties file");
        System.out.println("    -v       | Turn verbose messages on");
        System.out.println();
        System.out.println("    Props file time format | yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        System.out.println();
        System.out.println("Program finished before database population was completed.");
        System.out.println();
    }

    private static boolean processSac(JDBCSeismogramFiles jdbcSeisFile,
                                      String fileLoc,
                                      String fileName,
                                      boolean verbose) throws IOException,
            FissuresException, SQLException {
        SacTimeSeries sacTime = new SacTimeSeries();
        try {
            sacTime.read(new DataInputStream(new BufferedInputStream(new FileInputStream(fileLoc))));
        } catch(EOFException e) {
            System.err.println(fileName + " seems to be an invalid sac file.");
            return false;
        } catch(FileNotFoundException e) {
            System.err.println("Unable to find file " + fileName);
            return false;
        }
        LocalSeismogramImpl seis = SacToFissures.getSeismogram(sacTime);
        System.out.println("CHANNEL " + ChannelIdUtil.toString(seis.channel_id)
                + " BEGIN TIME: " + seis.getBeginTime().toString());
        jdbcSeisFile.saveSeismogramToDatabase(seis.channel_id,
                                              seis,
                                              fileLoc,
                                              SeismogramFileTypes.SAC);
        if(verbose) {
            System.out.println("SAC file " + fileName
                    + " added to the database.");
        }
        return true;
    }

    private static boolean processMSeed(JDBCSeismogramFiles jdbcSeisFile,
                                        String fileLoc,
                                        String fileName,
                                        boolean verbose) throws IOException,
            SeedFormatException, FissuresException, SQLException {
        MiniSeedRead mseedRead = null;
        try {
            mseedRead = new MiniSeedRead(new DataInputStream(new BufferedInputStream(new FileInputStream(fileLoc))));
        } catch(EOFException e) {
            System.err.println(fileName
                    + " seems to  be an invalid mseed file.");
            return false;
        } catch(FileNotFoundException e) {
            System.err.println("Unable to find file " + fileName);
            return false;
        }
        LinkedList list = new LinkedList();
        try {
            while(true) {
                try{
                    DataRecord dr = mseedRead.getNextRecord();
                    list.add(dr);
                } catch(SeedFormatException e){
                    System.out.println("Format exception skipped");
                }
            }
        } catch(EOFException e) {
            // must be all
        }
        LocalSeismogramImpl seis = FissuresConvert.toFissures((DataRecord[])list.toArray(new DataRecord[0]));
        jdbcSeisFile.saveSeismogramToDatabase(seis.channel_id,
                                              seis,
                                              fileLoc,
                                              SeismogramFileTypes.MSEED);
        System.out.println("    Start time: " + seis.begin_time.date_time);
        System.out.println("    Number of points: " + seis.num_points);
        try {
            for(int i = 0; i < seis.num_points; i++){
                System.out.println("    Data point " + i + ": " + seis.getValueAt(i).value);
            }
        } catch(CodecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(verbose) {
            System.out.println("MSEED file " + fileName
                    + " added to the database.");
        }
        return true;
    }

    private static boolean processRefTek(JDBCSeismogramFiles jdbcSeisFile,
                                         Connection conn,
                                         String fileLoc,
                                         String fileName,
                                         boolean verbose,
                                         Properties props) throws IOException,
            SQLException {
        if(props == null || conn == null){
            if(verbose){
                System.out.println("No props file was specified.");
                System.out.println("The channel IDs created will not be correct.");
            }
        }
        File seismogramFile = new File(fileLoc);
        FileInputStream fis = null;
        fis = new FileInputStream(seismogramFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);
        RT130ToLocalSeismogramImpl toSeismogram = new RT130ToLocalSeismogramImpl(dis, conn, props);
        LocalSeismogramImpl[] seismogramArray = null;
        try {
            seismogramArray = toSeismogram.readEntireDataFile();
        } catch(RT130FormatException e) {
            System.err.println(fileName
                    + " seems to be an invalid rt130 file.");
            return false;
        }
        for(int i = 0; i < seismogramArray.length; i++) {
            jdbcSeisFile.saveSeismogramToDatabase(seismogramArray[i].channel_id,
                                                  seismogramArray[i],
                                                  fileLoc,
                                                  SeismogramFileTypes.RT_130);
            System.out.println("    Start time: " + seismogramArray[i].begin_time.date_time);
            System.out.println("    Number of points: " + seismogramArray[i].num_points);
            try {
                System.out.println("    Data 0: " + seismogramArray[i].getValueAt(0).value);
                System.out.println("    Data 1: " + seismogramArray[i].getValueAt(1).value);
                System.out.println("    Data 2: " + seismogramArray[i].getValueAt(2).value);
                System.out.println("    Data 3: " + seismogramArray[i].getValueAt(3).value);
                System.out.println("    Data 4: " + seismogramArray[i].getValueAt(4).value);
                System.out.println("    Data 5: " + seismogramArray[i].getValueAt(5).value);
                System.out.println("    Data 6: " + seismogramArray[i].getValueAt(6).value);
                System.out.println("    Data 7: " + seismogramArray[i].getValueAt(7).value);
                System.out.println("    Data 8: " + seismogramArray[i].getValueAt(8).value);
                System.out.println("    Data 9: " + seismogramArray[i].getValueAt(9).value);
            } catch(CodecException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
        if(verbose) {
            System.out.println("REF_TEK file " + fileName
                    + " added to the database.");
        }
        return true;
    }
}
