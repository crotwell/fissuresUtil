package edu.sc.seis.fissuresUtil.database.seismogram;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.sc.seis.fissuresUtil.mseed.FissuresConvert;
import edu.sc.seis.fissuresUtil.rt130.RT130FileHandler;
import edu.sc.seis.fissuresUtil.rt130.RT130FileHandlerFlag;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;
import edu.sc.seis.fissuresUtil.simple.Initializer;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.MiniSeedRead;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

public class RT130ReportGenerator {

    public static void main(String[] args) throws FissuresException,
            IOException, SeedFormatException, ParseException {
        Properties props = Initializer.loadProperties(args);
        PropertyConfigurator.configure(props);
        boolean finished = false;
        String baseFileSystemLocation = props.getProperty(BASE_FILE_SYSTEM_LOCATION);
        RT130FileHandlerFlag scanMode = RT130FileHandlerFlag.SCAN;
        RT130FileHandlerFlag makeLogs = RT130FileHandlerFlag.MAKE_LOGS;
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-props")) {
                String propFileLocation = args[i + 1];
                File file = new File(propFileLocation);
                logger.debug("Properties file location: "
                        + file.getCanonicalPath());
                i++;
            } else if(args[i].equals("-hsql")) {
                String hsqlFileLocation = args[i + 1];
                File file = new File(hsqlFileLocation);
                logger.debug("HSQL properties file location: "
                        + file.getCanonicalPath());
                i++;
            } else if(args[i].equals("-f")) {
                baseFileSystemLocation = args[i + 1];
                logger.debug("Using alternative data location: "
                        + baseFileSystemLocation);
                i++;
            } else if(args[i].equals("-full")) {
                scanMode = RT130FileHandlerFlag.FULL;
                logger.debug("Full processing of RT130 data: ON");
            } else if(args[i].equals("-nologs")) {
                makeLogs = RT130FileHandlerFlag.NO_LOGS;
                logger.debug("Log creation: OFF");
            } else if(args[i].equals("-h") || args[i].equals("-help")) {
                printHelp();
                System.exit(0);
            } else {
                System.out.println("Incorrect arguments entered.");
                printHelp();
                System.exit(0);
            }
        }
        if(scanMode == RT130FileHandlerFlag.SCAN) {
            logger.debug("Scan processing of RT130 data: ON");
        }
        if(makeLogs == RT130FileHandlerFlag.MAKE_LOGS) {
            logger.debug("Log creation: ON");
        }
        File file = new File(baseFileSystemLocation);
        List flags = new LinkedList();
        flags.add(scanMode);
        flags.add(makeLogs);
        fileHandler = new RT130FileHandler(props, flags);
        if(file.isDirectory()) {
            finished = readEntireDirectory(file);
        } else if(file.isFile()) {
            finished = readSingleFile(baseFileSystemLocation);
        } else {
            logger.error("File: " + file
                    + " is not a file or a directory. This can"
                    + " be caused in Windows when the file path includes"
                    + " a Unix-style reference (soft or hard).");
        }
        if(finished) {
            System.out.println();
            System.out.println("Database population complete.");
            System.out.println();
            fileHandler.getReport().outputReport();
        } else {
            printHelp();
        }
    }

    private static boolean readSingleFile(String fileLoc) throws IOException,
            FissuresException, SeedFormatException, ParseException {
        boolean finished = false;
        StringTokenizer t;
        if(System.getProperty("os.name").startsWith("Windows")) {
            t = new StringTokenizer(fileLoc, "\\");
        } else {
            t = new StringTokenizer(fileLoc, "/");
        }
        String fileName = "";
        while(t.hasMoreTokens()) {
            fileName = t.nextToken();
        }
        if(fileName.length() == 18 && fileName.charAt(9) == '_') {
            if(fileHandler.getFlags().contains(RT130FileHandlerFlag.SCAN)) {
                finished = fileHandler.processSingleRefTekScan(fileLoc,
                                                               fileName);
            } else {
                finished = fileHandler.processSingleRefTekFull(fileLoc,
                                                               fileName);
            }
        } else if(fileName.endsWith(".mseed")) {
            finished = processMSeed(fileHandler.getReport(), fileLoc, fileName);
        } else if(fileName.endsWith(".sac")) {
            finished = processSac(fileHandler.getReport(),
                                  fileLoc,
                                  fileName,
                                  fileHandler.getProps());
        } else {
            if(fileName.equals("SOH.RT")) {
                logger.debug("Ignoring Ref Tek file: " + fileName);
            } else if(fileName.equals(".DS_Store") || fileName.equals("._501")
                    || fileName.equals("._504")) {
                logger.debug("Ignoring Mac OS X file: " + fileName);
            } else {
                fileHandler.getReport()
                        .addUnsupportedFileException(fileLoc,
                                                     fileName
                                                             + " can not be processed because it's file"
                                                             + " name is not formatted correctly, and therefore"
                                                             + " is assumed to be an invalid file format. If"
                                                             + " the data file format is valid (mini seed, sac, rt130)"
                                                             + " try renaming the file.");
                logger.debug(fileName
                        + " can not be processed because it's file"
                        + " name is not formatted correctly, and therefore"
                        + " is assumed to be an invalid file format. If"
                        + " the data file format is valid (mini seed, sac, rt130)"
                        + " try renaming the file.");
            }
        }
        return finished;
    }

    private static boolean readEntireDirectory(File baseDirectory)
            throws FissuresException, IOException, SeedFormatException,
            ParseException {
        File[] files = baseDirectory.listFiles();
        if(files == null) {
            throw new IOException("Unable to get listing of directory: "
                    + baseDirectory);
        }
        for(int i = 0; i < files.length; i++) {
            if(files[i].isDirectory()) {
                readEntireDirectory(files[i]);
            } else {
                readSingleFile(files[i].getCanonicalPath());
            }
        }
        return true;
    }

    private static void printHelp() {
        System.out.println();
        System.out.println("    The default SOD properties file is server.properties.");
        System.out.println("    The default database properties file is server.properties.");
        System.out.println();
        System.out.println("    -props   | Accepts alternate properties file.");
        System.out.println("    -hsql    | Accepts alternate database properties file.");
        System.out.println("    -f       | Accepts alternate data directory.");
        System.out.println("    -full    | Turn on full processing of RT130 data.");
        System.out.println("             | Scan processing of RT130 data is on by default.");
        System.out.println("             |   No other types of data can be processed using scan method.");
        System.out.println("    -nologs  | Do not produce master SOH file log.");
        System.out.println();
        System.out.println();
        System.out.println("Program finished before database population was completed.");
        System.out.println();
    }

    private static boolean processSac(RT130Report report,
                                      String fileLoc,
                                      String fileName,
                                      Properties props) throws IOException,
            FissuresException {
        SacTimeSeries sacTime = new SacTimeSeries();
        try {
            sacTime.readHeader(new DataInputStream(new BufferedInputStream(new FileInputStream(fileLoc))));
        } catch(EOFException e) {
            report.addFileFormatException(fileLoc, fileName
                    + " seems to be an invalid sac file." + "\n"
                    + e.getMessage());
            logger.error(fileName + " seems to be an invalid sac file." + "\n"
                    + e.getMessage());
            return false;
        } catch(FileNotFoundException e) {
            report.addFileFormatException(fileLoc, "Unable to find file "
                    + fileName + "\n" + e.getMessage());
            logger.error("Unable to find file " + fileName + "\n"
                    + e.getMessage());
            return false;
        }
        SeismogramAttrImpl seis = SacToFissures.getSeismogramAttr(sacTime);
        Channel chan = SacToFissures.getChannel(sacTime);
        chan = PopulationProperties.fix(chan, props);
        report.addSacSeismogram(chan, seis.getBeginTime(), seis.getEndTime());
        return true;
    }

    private static boolean processMSeed(RT130Report report,
                                        String fileLoc,
                                        String fileName) throws IOException,
            SeedFormatException, FissuresException {
        MiniSeedRead mseedRead = null;
        try {
            mseedRead = new MiniSeedRead(new DataInputStream(new BufferedInputStream(new FileInputStream(fileLoc))));
        } catch(EOFException e) {
            report.addFileFormatException(fileLoc, fileName
                    + " seems to be an invalid mseed file." + "\n"
                    + e.getMessage());
            logger.error(fileName + " seems to be an invalid mseed file."
                    + "\n" + e.getMessage());
            return false;
        } catch(FileNotFoundException e) {
            report.addFileFormatException(fileLoc, "Unable to find file "
                    + fileName + "\n" + e.getMessage());
            logger.error("Unable to find file " + fileName + "\n"
                    + e.getMessage());
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
        report.addMSeedSeismogram(seis.channel_id,
                                  seis.getBeginTime(),
                                  seis.getEndTime());
        return true;
    }

    public static final String BASE_FILE_SYSTEM_LOCATION = "seismogramDir";

    private static RT130FileHandler fileHandler;

    private static final Logger logger = Logger.getLogger(RT130ReportGenerator.class);
}
