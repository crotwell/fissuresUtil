package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedInputStream;
//import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
//import java.util.Calendar;
import java.util.Properties;
//import java.util.TimeZone;
import org.apache.log4j.BasicConfigurator;
//import edu.iris.Fissures.model.ISOTime;
//import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.simple.Initializer;

public class SimpleFileReader {

    public static void main(String[] args) throws IOException, RT130FormatException {
        BasicConfigurator.configure();
        Properties props = Initializer.loadProperties(args);
        ConnMgr.installDbProperties(props, args);
        boolean one = false;
        boolean two = false;
        boolean four = false;
        boolean eight = false;
        boolean sixteen = false;
        boolean thirtyTwo = false;
        boolean sixtyFour = false;
        boolean oneHundredTwentyEight = false;
        boolean twoHundredFiftySix = false;
        for(int i = 1; i < args.length; i++) {
            if(args[i].equals("1")) {
                one = true;
            } else if(args[i].equals("2")) {
                two = true;
            } else if(args[i].equals("4")) {
                four = true;
            } else if(args[i].equals("8")) {
                eight = true;
            } else if(args[i].equals("16")) {
                sixteen = true;
            } else if(args[i].equals("32")) {
                thirtyTwo = true;
            } else if(args[i].equals("64")) {
                sixtyFour = true;
            } else if(args[i].equals("128")) {
                oneHundredTwentyEight = true;
            } else if(args[i].equals("256")) {
                twoHundredFiftySix = true;
            }
        }
        if(args.length > 0) {
            String fileLoc = args[args.length - 1];
            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fileLoc))));
//                // Packet Type
//                String packetType = new String(readBytes(dis, 2));
//                System.out.println("Packet Type: " + packetType);
//                // Experiment Number
//                int experimentNumber = BCDRead.toInt(readBytes(dis, 1));
//                System.out.println("Experiement Number: " + experimentNumber);
//                // Year
//                int year = BCDRead.toInt(readBytes(dis, 1));
//                System.out.println("Year: " + year);
//                // Unit ID Number
//                String unitIdNumber = HexRead.toString(readBytes(dis, 2));
//                System.out.println("Unit ID Number: " + unitIdNumber);
//                // Time
//                String timeString = BCDRead.toString(readBytes(dis, 6));
//                System.out.println("Time: " + timeString);
//                MicroSecondDate time = stringToMicroSecondDate(timeString,
//                                                               (year + 2000));
//                System.out.println("Micro Second Date Time: " + time.toString());
//                // Byte Count
//                int byteCount = BCDRead.toInt(readBytes(dis, 2));
//                System.out.println("Byte Count: " + byteCount);
//                // Packet Sequence
//                int packetSequence = BCDRead.toInt(readBytes(dis, 2));
//                System.out.println("Packet Sequence: " + packetSequence);
//                
//                
//                
//                dis.skipBytes(1008);
//                
//                
//                // Packet Type
//                packetType = new String(readBytes(dis, 2));
//                System.out.println("Packet Type: " + packetType);
//                // Experiment Number
//                experimentNumber = BCDRead.toInt(readBytes(dis, 1));
//                System.out.println("Experiement Number: " + experimentNumber);
//                // Year
//                year = BCDRead.toInt(readBytes(dis, 1));
//                System.out.println("Year: " + year);
//                // Unit ID Number
//                unitIdNumber = HexRead.toString(readBytes(dis, 2));
//                System.out.println("Unit ID Number: " + unitIdNumber);
//                // Time
//                timeString = BCDRead.toString(readBytes(dis, 6));
//                System.out.println("Time: " + timeString);
//                time = stringToMicroSecondDate(timeString,
//                                                               (year + 2000));
//                System.out.println("Micro Second Date Time: " + time.toString());
//                // Byte Count
//                byteCount = BCDRead.toInt(readBytes(dis, 2));
//                System.out.println("Byte Count: " + byteCount);
//                // Packet Sequence
//                packetSequence = BCDRead.toInt(readBytes(dis, 2));
//                System.out.println("Packet Sequence: " + packetSequence);
//                
//                // Event Number
//                int eventNumber = BCDRead.toInt(readBytes(dis, 2)); 
//                //System.out.println("    Event Number: " + eventNumber);
//                
//                // Data Stream Number
//                int dataStreamNumber = BCDRead.toInt(readBytes(dis, 1));
//                //System.out.println("    Data Stream Number: " + dataStreamNumber);
//                
//                // Channel Number
//                int channelNumber = BCDRead.toInt(readBytes(dis, 1)); 
//                //System.out.println("    Channel Number: " + channelNumber);
//                
//                // Number Of Samples
//                int numberOfSamples = BCDRead.toInt(readBytes(dis, 2));
//                System.out.println("    Number Of Samples: " + numberOfSamples);
//                
//                // Flags
//                byte[] flagsArray = readBytes(dis, 1);
//                byte flags = flagsArray[0];
//                //System.out.println("    Flags: " + Integer.toBinaryString(flags));
//                
//                // Data Format
//                String dataFormat = HexRead.toString(readBytes(dis, 1));
//                //System.out.println("    Data Format: " + dataFormat);
                

                dis.skipBytes(1048);
                
                
                
                
                 int count = 0;
                 int data = 0;
                 while(count <= 1000) {
                 if(one) {
                 System.out.println();
                 } else if(two) {
                 if(count % 2 == 0) {
                 System.out.println();
                 }
                 } else if(four) {
                 if(count % 4 == 0) {
                 System.out.println();
                 }
                 } else if(eight) {
                 if(count % 8 == 0) {
                 System.out.println();
                 }
                 } else if(sixteen) {
                 if(count % 16 == 0) {
                 System.out.println();
                 }
                 } else if(thirtyTwo) {
                 if(count % 32 == 0) {
                 System.out.println();
                 }
                 } else if(sixtyFour) {
                 if(count % 64 == 0) {
                 System.out.println();
                 }
                 } else if(oneHundredTwentyEight) {
                 if(count % 128 == 0) {
                 System.out.println();
                 }
                 } else if(twoHundredFiftySix) {
                 if(count % 256 == 0) {
                 System.out.println();
                 }
                 }
                 data = dis.readUnsignedByte();
                 if(data < -99){
                 // Do nothing.
                 } else if(data < -9) {
                 System.out.print(" ");
                 } else if(data < 0) {
                 System.out.print("  ");
                 } else if(data < 10) {
                 System.out.print("   ");
                 } else if(data < 100) {
                 System.out.print("  ");
                 } else {
                 System.out.print(" ");
                 }
                 System.out.print(data);
                 System.out.print(" ");
                 count++;
                 }
            } catch(FileNotFoundException e) {
                System.out.println("Could not find file " + fileLoc);
            }
        }
    }

//    private static byte[] readBytes(DataInput in, int numBytes)
//            throws IOException {
//        byte[] seqBytes = new byte[numBytes];
//        in.readFully(seqBytes);
//        return seqBytes;
//    }
//
//    private static MicroSecondDate stringToMicroSecondDate(String timeString,
//                                                    int yearInt)
//            throws RT130FormatException {
//        String fractionsOfSecond = "";
//        String seconds = "";
//        String minutes = "";
//        String hours = "";
//        String daysOfYearReversed = "";
//        if(timeString.length() >= 1) {
//            fractionsOfSecond = "" + timeString.charAt(timeString.length() - 3);
//            fractionsOfSecond = fractionsOfSecond
//                    + timeString.charAt(timeString.length() - 2);
//            fractionsOfSecond = fractionsOfSecond
//                    + timeString.charAt(timeString.length() - 1);
//        }
//        if(timeString.length() >= 5) {
//            seconds = "" + timeString.charAt(timeString.length() - 5);
//            seconds = seconds + timeString.charAt(timeString.length() - 4);
//        }
//        if(timeString.length() >= 7) {
//            minutes = "" + timeString.charAt(timeString.length() - 7);
//            minutes = minutes + timeString.charAt(timeString.length() - 6);
//        }
//        if(timeString.length() >= 9) {
//            hours = "" + timeString.charAt(timeString.length() - 9);
//            hours = hours + timeString.charAt(timeString.length() - 8);
//        }
//        if(timeString.length() >= 10) {
//            daysOfYearReversed = ""
//                    + timeString.charAt(timeString.length() - 10);
//        }
//        if(timeString.length() >= 11) {
//            daysOfYearReversed = daysOfYearReversed
//                    + timeString.charAt(timeString.length() - 11);
//        }
//        if(timeString.length() >= 12) {
//            daysOfYearReversed = daysOfYearReversed
//                    + timeString.charAt(timeString.length() - 12);
//        }
//        if(timeString.length() > 12 || timeString.length() < 9) {
//            System.err.println("Cannot read time field of Packet Header.");
//            throw new RT130FormatException();
//        }
//        String daysOfYear = "0";
//        for(int i = daysOfYearReversed.length() - 1; i >= 0; i--) {
//            daysOfYear = daysOfYear.concat("" + daysOfYearReversed.charAt(i));
//        }
//        int daysOfYearInt = Integer.valueOf(daysOfYear).intValue();
//        int hoursInt = Integer.valueOf(hours).intValue();
//        int minutesInt = Integer.valueOf(minutes).intValue();
//        seconds = seconds.concat(".");
//        seconds = seconds.concat(fractionsOfSecond);
//        float secondsInt = Float.valueOf(seconds).floatValue();
//          
//        ISOTime isoTime = new ISOTime(yearInt, daysOfYearInt, hoursInt, minutesInt, secondsInt);
//        return isoTime.getDate();
//    }
}
