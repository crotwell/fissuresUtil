package edu.sc.seis.fissuresUtil.database.seismogram;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
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
        RT130FileHandlerFlag scanMode = RT130FileHandlerFlag.SCAN;
        int i;
        for(i = 0; i < args.length; i++) {
            if(args[i].equals("-props")) {
                i++;//Handled by Initializer.loadProperties
            } else if(args[i].equals("-full")) {
                scanMode = RT130FileHandlerFlag.FULL;
            } else if(args[i].equals("-scan")) {
                scanMode = RT130FileHandlerFlag.SCAN;
            } else if(args[i].equals("-h") || args[i].equals("-help")) {
                printHelp();
            } else if(args[i].equals("-progress")) {
                showProgress = true;
            } else if(args[i].startsWith("-")) {
                System.out.println("Don't understand argument '" + args[i]
                        + "'");
                printHelp();
            } else {
                break;// Not a flag so it must be files to read
            }
        }
        logger.debug("RT130 mode: " + scanMode);
        List files = new ArrayList();
        for(int j = 0; j + i < args.length; j++) {
            files.add(new File(args[i + j]));
        }
        if(showProgress) {
            Iterator it = files.iterator();
            while(it.hasNext()) {
                File file = (File)it.next();
                logger.info("Counting the number of total files in " + file);
                if(file.isDirectory()) {
                    finished = countEntireDirectory(file);
                } else if(file.isFile()) {
                    numFilesTotal++;
                }
            }
            System.out.print(decFormat.format(0));
        }
        List flags = new LinkedList();
        flags.add(scanMode);
        fileHandler = new RT130FileHandler(props, flags);
        Iterator it = files.iterator();
        while(it.hasNext()) {
            File file = (File)it.next();
            if(file.isDirectory()) {
                finished = readEntireDirectory(file);
            } else if(file.isFile()) {
                finished = readSingleFile(file);
            } else {
                logger.error("File: " + file
                        + " is not a file or a directory. This can"
                        + " be caused in Windows when the file path includes"
                        + " a Unix-style reference (soft or hard).");
            }
        }
        if(finished) {
            if(showProgress){
            System.out.print("\b\b\b\b\b\b\b" + decFormat.format(1));
            }
            System.out.println();
            System.out.println("Report generation complete.");
            System.out.println();
            fileHandler.getReport().outputReport();
        } else {
            printHelp();
        }
    }

    private static boolean readSingleFile(File file) throws IOException,
            FissuresException, SeedFormatException, ParseException {
        String fileLoc = file.getCanonicalPath();
        if(showProgress) {
            double percentage = numFilesRead / (double)numFilesTotal;
            System.out.print("\b\b\b\b\b\b\b");
            System.out.print(decFormat.format(percentage));
        }
        numFilesRead++;
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
            // HACK
            if(fileLoc.endsWith("/data/SNEP/Tar_Extracts/May_Service_2006/930F_2005_273/2005345/930F/1/160620270_00FFAAAB")) {
                logger.debug("Skipping Ben's favorite file");
                return true;
            }
            // END HACK
            if(fileHandler.getFlags().contains(RT130FileHandlerFlag.SCAN)) {
                return fileHandler.scan(fileLoc, fileName);
            } else {
                return fileHandler.read(fileLoc, fileName);
            }
        } else if(fileName.endsWith(".mseed")) {
            return processMSeed(fileHandler.getReport(), fileLoc, fileName);
        } else if(fileName.endsWith(".sac")) {
            return processSac(fileHandler.getReport(),
                              fileLoc,
                              fileName,
                              fileHandler.getProps());
        }
        if(fileName.equals("SOH.RT") || fileName.equals("soh.rt")) {
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
            logger.debug(fileName + " can not be processed because it's file"
                    + " name is not formatted correctly, and therefore"
                    + " is assumed to be an invalid file format. If"
                    + " the data file format is valid (mini seed, sac, rt130)"
                    + " try renaming the file.");
        }
        return false;
    }

    private static boolean countEntireDirectory(File baseDirectory)
            throws FissuresException, IOException, SeedFormatException,
            ParseException {
        File[] files = baseDirectory.listFiles();
        if(files == null) {
            throw new IOException("Unable to get listing of directory: "
                    + baseDirectory);
        }
        for(int i = 0; i < files.length; i++) {
            if(files[i].isDirectory()) {
                countEntireDirectory(files[i]);
            } else {
                numFilesTotal++;
            }
        }
        return true;
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
                readSingleFile(files[i]);
            }
        }
        return true;
    }

    private static void printHelp() {
        System.out.println();
        System.out.println("    The default SOD properties file is server.properties.");
        System.out.println("    The default database properties file is server.properties.");
        System.out.println();
        System.out.println("    -props     | Accepts alternate properties file.");
        System.out.println("    -hsql      | Accepts alternate database properties file.");
        System.out.println("    -f         | Accepts alternate data directory.");
        System.out.println("    -full      | Turn on full processing of RT130 data.");
        System.out.println("    -scan      | Turn on scan processing of RT130 data.");
        System.out.println("               |   Scan processing of RT130 data is on by default.");
        System.out.println("               |   No other types of data can be processed using scan method.");
        System.out.println("    -help      | Displays this message.");
        System.out.println("    -h         | Displays this message.");
        System.out.println("    -progress  | Show percentage complete.");
        System.out.println();
        System.out.println();
        System.out.println("Program finished before the report was created.");
        System.out.println();
        System.exit(0);
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

    private static DecimalFormat decFormat = new DecimalFormat("000.00%");

    private static boolean showProgress = false;

    private static long numFilesTotal = 0, numFilesRead = 0;

    public static final String BASE_FILE_SYSTEM_LOCATION = "seismogramDir";

    private static RT130FileHandler fileHandler;

    private static final Logger logger = Logger.getLogger(RT130ReportGenerator.class);
}
