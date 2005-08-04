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
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.ConnectionCreator;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.mseed.FissuresConvert;
import edu.sc.seis.fissuresUtil.rt130.PacketType;
import edu.sc.seis.fissuresUtil.rt130.RT130FormatException;
import edu.sc.seis.fissuresUtil.rt130.RT130FileReader;
import edu.sc.seis.fissuresUtil.rt130.RT130ToLocalSeismogram;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;
import edu.sc.seis.fissuresUtil.simple.Initializer;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.MiniSeedRead;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

public class PopulateDatabaseFromDirectory {

    public static void main(String[] args) throws FissuresException,
            IOException, SeedFormatException, SQLException, NotFound {
        BasicConfigurator.configure();
        Properties props = Initializer.loadProperties(args);
        boolean verbose = false;
        boolean finished = false;
        for(int i = 1; i < args.length; i++) {
            if(args[i].equals("-v")) {
                verbose = true;
            }
        }
        if(args.length > 0) {
            ConnectionCreator connCreator = new ConnectionCreator(props);
            Connection conn = connCreator.createConnection();
            System.out.println("Database location: " + connCreator.getUrl());
            String fileLoc = args[args.length - 1];
            File file = new File(fileLoc);
            if(file.isDirectory()) {
                finished = readEntireDirectory(fileLoc,
                                               verbose,
                                               conn,
                                               props,
                                               fileLoc);
            } else {
                File seismogramFile = new File(fileLoc);
                File dataStream = new File(seismogramFile.getParent());
                File unitId = new File(dataStream.getParent());
                finished = readSingleFile(fileLoc,
                                          verbose,
                                          conn,
                                          props,
                                          unitId.getAbsolutePath());
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
                                          Properties props,
                                          String absoluteBaseDirectory)
            throws IOException, FissuresException, SeedFormatException,
            SQLException, NotFound {
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
            finished = processRefTek(jdbcSeisFile,
                                     conn,
                                     fileLoc,
                                     fileName,
                                     verbose,
                                     props,
                                     absoluteBaseDirectory);
        } else if(fileName.equals("SOH.RT")) {
            if(verbose) {
                System.out.println("Ignoring State of Health file " + fileName
                        + ".");
            }
        } else {
            if(verbose) {
                System.out.println(fileName
                        + " can not be processed because it's file"
                        + " name is not formatted correctly, and therefore"
                        + " is assumed to be an invalid file format. If"
                        + " the data file format is valid (mini seed, sac, rt130)"
                        + " try renaming the file.");
            }
        }
        return finished;
    }

    private static boolean readEntireDirectory(String baseDirectory,
                                               boolean verbose,
                                               Connection conn,
                                               Properties props,
                                               String absoluteBaseDirectory)
            throws FissuresException, IOException, SeedFormatException,
            SQLException, NotFound {
        File[] files = new File(baseDirectory).listFiles();
        for(int i = 0; i < files.length; i++) {
            if(files[i].isDirectory()) {
                readEntireDirectory(baseDirectory + files[i].getName() + "/",
                                    verbose,
                                    conn,
                                    props,
                                    absoluteBaseDirectory);
            } else {
                readSingleFile(files[i].getAbsolutePath(),
                               verbose,
                               conn,
                               props,
                               absoluteBaseDirectory);
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
        jdbcSeisFile.saveSeismogramToDatabase(SacToFissures.getChannel(sacTime),
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
            DataRecord dr = mseedRead.getNextRecord();
            list.add(dr);
        } catch(EOFException e) {
            // must be all
        }
        LocalSeismogramImpl seis = FissuresConvert.toFissures((DataRecord[])list.toArray(new DataRecord[0]));
        jdbcSeisFile.saveSeismogramToDatabase(seis.channel_id,
                                              seis,
                                              fileLoc,
                                              SeismogramFileTypes.MSEED);
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
                                         Properties props,
                                         String absoluteBaseDirectory)
            throws IOException, SQLException, NotFound {
        if(props == null || conn == null) {
            if(verbose) {
                System.out.println("No props file was specified.");
                System.out.println("The channel IDs created will not be correct.");
            }
        }
        RT130FileReader toSeismogramDataPackets = new RT130FileReader(fileLoc, false);
        PacketType[] seismogramDataPacketArray = null;
        try {
            seismogramDataPacketArray = toSeismogramDataPackets.processRT130Data();
        } catch(RT130FormatException e) {
            System.err.println(fileName + " seems to be an invalid rt130 file.");
            return false;
        }
        RT130ToLocalSeismogram toSeismogram = new RT130ToLocalSeismogram(conn, props);
        LocalSeismogramImpl[] seismogramArray = toSeismogram.ConvertRT130ToLocalSeismogram(seismogramDataPacketArray);
        Channel[] channel = toSeismogram.getChannels();
         
        // Check database for channels that match (with lat/long buffer)
        // If channel exists, use it. If not, use new channel.
        for(int i = 0; i < seismogramArray.length; i++) {
            Channel closeChannel = jdbcSeisFile.findCloseChannel(channel[i], new QuantityImpl(1, UnitImpl.KILOMETER));
            if(closeChannel == null){
                System.out.println("New station code: " + channel[i].my_site.my_station.get_code());
                jdbcSeisFile.saveSeismogramToDatabase(channel[i],
                                               seismogramArray[i],
                                               fileLoc,
                                               SeismogramFileTypes.RT_130);
            } else {
                jdbcSeisFile.setChannelBeginTimeToEarliest(closeChannel, channel[i]);
                System.out.println("Existing station code : " + closeChannel.my_site.my_station.get_code());
                jdbcSeisFile.saveSeismogramToDatabase(closeChannel,
                                                      seismogramArray[i],
                                                      fileLoc,
                                                      SeismogramFileTypes.RT_130);
            }
        }
        if(verbose) {
            System.out.println("RT130 file " + fileName
                    + " added to the database.");
        }
        return true;
    }
}
