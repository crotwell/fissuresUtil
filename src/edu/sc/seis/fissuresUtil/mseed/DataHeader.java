//  DataHeader.java
//
//  Container class for SEED Fixed Section Data Header information
//
//  Started by Philip Crotwell, FISSURES, USC
//  Modified 8/9/2000 by Robert Casey, IRIS DMC

package edu.sc.seis.fissuresUtil.mseed;

import java.io.*;
import java.text.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.model.MicroSecondDate;

/**
   Container class for SEED Fixed Section Data Header information.

   @author Philip Crotwell, FISSURES, USC<br>
   Robert Casey, IRIS DMC
   @version 08/28/2000
*/
public class DataHeader extends ControlHeader {

    protected byte[] stationIdentifier = new byte[5];

    protected byte[] locationIdentifier = new byte[2];

    protected byte[] channelIdentifier = new byte[3];

    protected byte[] networkCode = new byte[2];

    protected byte[] startTime = new byte[10];

    protected int numSamples;

    protected int sampleRateFactor;

    protected int sampleRateMultiplier;

    protected byte activityFlags;

    protected byte ioClockFlags;

    protected byte dataQualityFlags;

    protected byte numBlockettes;

    protected byte[] timeCorrection = new byte[5];

    protected int dataOffset;

    protected int dataBlocketteOffset;

    /**
       creates a DataHeader object with listed sequence number, type code,
       and continuation code boolean.

       @param sequenceNum sequence number of the record represented by this object.
       @param typeCode character representing the type of record represented
       by this object
       @param continuationCode true if this record is flagged as a continuation
       from its previous SEED record
     */
    public  DataHeader(int sequenceNum,
               char typeCode, boolean continuationCode) {
    super(sequenceNum, typeCode, continuationCode);
    }

    /**
       Instantiate an object of this class and read an FSDH byte
       stream into it, parsing the contents
       into the instance variables of this object, which represent the
       individual FSDH fields.<br>
       Note, first 8 bytes are assumed to already have been read.

       @param in SEED data stream offset 8 bytes from beginning of record.

       @param sequenceNum 6 digit ascii sequence tag at the beginning of

       SEED record.
       @param typeCode character representing the type of record being read

       @param continuationCode true if this record is flagged as a continuation
       from its previous SEED record.

       @return an object of this class with fields filled from 'in' parameter

       @throws IOException
       @throws SeedFormatException
    */
    public static DataHeader read(DataInput in,
                  int sequenceNum,
                  char typeCode,
                  boolean continuationCode)
    throws IOException, SeedFormatException {

    byte[] buf = new byte[40];
    in.readFully(buf);
    DataHeader data = new DataHeader(sequenceNum,
                     typeCode,
                     continuationCode);
    data.read(buf,0);
    return data;
    }


    /** test whether the data being read needs to be byte-swapped
        look for bogus year value to determine this */
    private boolean flagByteSwap () {
    int year = Utility.uBytesToInt
        (startTime[0], startTime[1], false);
    if ( (year < 1960) || (year > 2050) ) return true;
    else return false;
    }


    /**
       populates this object with Fixed Section Data Header info.
       this routine modified to include byte offset, should the station identifier
       start at a byte offset (such as 8 from the beginning of a data record).

       @param buf data buffer containing FSDH information
       @param offset byte offset to begin reading buf
    */
    protected void read(byte[] buf, int offset) {
    System.arraycopy(buf, offset+0, stationIdentifier, 0, stationIdentifier.length);
    System.arraycopy(buf, offset+5, locationIdentifier, 0, locationIdentifier.length);
    System.arraycopy(buf, offset+7, channelIdentifier, 0, channelIdentifier.length);
    System.arraycopy(buf, offset+10, networkCode, 0, networkCode.length);
    System.arraycopy(buf, offset+12, startTime, 0, startTime.length);
    boolean byteSwapFlag = flagByteSwap();
    numSamples = Utility.uBytesToInt
        (buf[offset+22], buf[offset+23], byteSwapFlag);
    sampleRateFactor = Utility.bytesToInt
        (buf[offset+24], buf[offset+25], byteSwapFlag);
    sampleRateMultiplier = Utility.bytesToInt
        (buf[offset+26], buf[offset+27], byteSwapFlag);
    activityFlags = buf[offset+28];
    ioClockFlags = buf[offset+29];
    dataQualityFlags = buf[offset+30];
    numBlockettes = buf[offset+31];
    System.arraycopy(buf, offset+32, timeCorrection, 0, timeCorrection.length);
    dataOffset = Utility.uBytesToInt
        (buf[offset+36], buf[offset+37], byteSwapFlag);
    dataBlocketteOffset = Utility.uBytesToInt
        (buf[offset+38], buf[offset+39], byteSwapFlag);
    }

    /**
       write DataHeader contents to a DataOutput stream

       @param dh DataHeader to read values from
       @param dos DataOutput stream to write to
     */
    protected void write(DataOutput dos) throws IOException
    {
    super.write(dos);

    dos.write(Utility.pad(getStationIdentifier().getBytes("ASCII"),
                  5,
                  (byte)32));
    dos.write(Utility.pad(getLocationIdentifier().getBytes("ASCII"),
                  2,
                  (byte)32));
    dos.write(Utility.pad(getChannelIdentifier().getBytes("ASCII"),
                  3,
                  (byte)32));
    dos.write(Utility.pad(getNetworkCode().getBytes("ASCII"),
                  2,
                  (byte)32));
    dos.write(Utility.pad(getStartTime().getBytes("ASCII"),
                  10,
                  (byte)32));
    dos.write(getNumSamples());
    dos.write(getSampleRateFactor());
    dos.write(getSampleRateMultiplier());
    dos.write(getActivityFlags());
    dos.write(getIOClockFlags());
    dos.write(getDataQualityFlags());
    dos.write(getNumBlockettes());
    dos.write(getTimeCorrection());
    dos.write(getDataOffset());
    dos.write(getDataBlocketteOffset());
    }

    public short getSize() { return 48;}

    /**
       * Get the value of stationIdentifier.
       * @return Value of stationIdentifier.
       */
    public String getStationIdentifier() {return new String(stationIdentifier);}

    /**
       * Set the value of stationIdentifier.
       * @param v  Value to assign to stationIdentifier.
       */
        public void setStationIdentifier(String  v) {
        int requriedBytes = 5; //REFER SEED Format
        try{
        this.stationIdentifier = Utility.pad(v.getBytes("ASCII"),5, (byte)32);
        }catch( java.io.UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }


    /**
       * Get the value of locationIdentifier.
       * @return Value of locationIdentifier.
       */
    public String getLocationIdentifier() {return new String(locationIdentifier);}

    /**
       * Set the value of locationIdentifier.
       * @param v  Value to assign to locationIdentifier.
       */
        public void setLocationIdentifier(String  v) {
         int requiredBytes = 2; //REFER SEED Format
         try{
         this.locationIdentifier =
         Utility.pad(v.getBytes("ASCII"),requiredBytes,(byte)32);
         }catch( java.io.UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }


    /**
       * Get the value of channelIdentifier.
       * @return Value of channelIdentifier.
       */
    public String getChannelIdentifier() {

    return new String(channelIdentifier);}

    /**
       * Set the value of channelIdentifier.
       * @param v  Value to assign to channelIdentifier.
       */
       public void setChannelIdentifier(String  v) {
       int requiredBytes = 3; //REFER SEED Format
       try{
       this.channelIdentifier = Utility.pad(v.getBytes("ASCII"),requiredBytes,(byte)32);
       }catch( java.io.UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
       }

    /**
       * Get the value of networkCode.
       * @return Value of networkCode.
       */
    public String getNetworkCode() {return new String(networkCode);}

    /**
       * Set the value of networkCode.
       * @param v  Value to assign to networkCode.
       */
       public void setNetworkCode(String  v) {
       int requiredBytes = 2;//REFER SEED FORMAT
       byte paddingByte = (byte)32;
       try{
       this.networkCode = Utility.pad(v.getBytes("ASCII"),requiredBytes,paddingByte);
       }catch( java.io.UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
       }


    // extract SEED time structure from the startTime byte vector, splitting into
    // meaningful elements of year, day, time
    // Btime is an inner class
    private Btime getStartBtime() {
        Btime bTime = new Btime();
        boolean byteSwapFlag = flagByteSwap();
    bTime.year = Utility.uBytesToInt
        (startTime[0], startTime[1], byteSwapFlag);
    bTime.jday = Utility.uBytesToInt
        (startTime[2], startTime[3], byteSwapFlag);
    bTime.hour = startTime[4] & 0xff;
    bTime.min = startTime[5] & 0xff;
    bTime.sec = startTime[6] & 0xff;
    // startTime[7] is unused (alignment)
    bTime.tenthMilli = Utility.uBytesToInt
        (startTime[8], startTime[9], byteSwapFlag);
    return bTime;
    }


    /**
       get the sample rate.  derived from sample rate factor
       and the sample rate multiplier

       @return sample rate
    */
    public float getSampleRate () {
        double factor = (double) getSampleRateFactor();
    double multiplier = (double) getSampleRateMultiplier();
    float sampleRate = (float) 10000.0;  // default (impossible) value;
    if ((factor * multiplier) != 0.0) {  // in the case of log records
        sampleRate = (float) (java.lang.Math.pow
                  (java.lang.Math.abs(factor),
                   (factor/java.lang.Math.abs(factor)))
                  * java.lang.Math.pow
                  (java.lang.Math.abs(multiplier),
                   (multiplier/java.lang.Math.abs(multiplier))));
    }
    return sampleRate;
    }

    // convert contents of Btime structure to the number of
    // ten thousandths of seconds it represents within that year
    private double ttConvert (Btime bTime) {
    double tenThousandths = bTime.jday * 864000000.0;
    tenThousandths += bTime.hour * 36000000.0;
    tenThousandths += bTime.min * 600000.0;
    tenThousandths += bTime.sec * 10000.0;
    tenThousandths += bTime.tenthMilli;
    return tenThousandths;
    }


    // take the Btime structure and forward-project a new time that is
    // the specified number of ten thousandths of seconds ahead
    private Btime projectTime (Btime bTime, double tenThousandths) {
        int offset = 0;  // leap year offset
    // check to see if this is a leap year we are starting on
    boolean is_leap = bTime.year % 4 == 0 &&
        bTime.year % 100 != 0 ||
        bTime.year % 400 == 0;
        if (is_leap) offset = 1;
        // convert bTime to tenths of seconds in the current year, then
        // add that value to the incremental time value tenThousandths
        tenThousandths += ttConvert(bTime);
        // now increment year if it crosses the year boundary
    if ((tenThousandths) >= (366+offset)*864000000.0) {
        bTime.year++;
        tenThousandths -= (365+offset)*864000000.0;
    }
    // increment day
    bTime.jday = (int) (tenThousandths / 864000000.0);
    tenThousandths -= (double) bTime.jday * 864000000.0;
    // increment hour
    bTime.hour = (int) (tenThousandths / 36000000.0);
    tenThousandths -= (double) bTime.hour * 36000000.0;
    // increment minutes
    bTime.min = (int) (tenThousandths / 600000.0);
    tenThousandths -= (double) bTime.min * 600000.0;
    // increment seconds
    bTime.sec = (int) (tenThousandths / 10000.0);
    tenThousandths -= (double) bTime.sec * 10000.0;
    // set tenth seconds
    bTime.tenthMilli = (int) tenThousandths;

        // return the resultant value
    return bTime;
    }


    // return a Btime structure containing the derived end time for this record
    private Btime getEndBtime () {
      Btime startBtime = getStartBtime();
      // get the number of ten thousandths of seconds of data
      double numTenThousandths =
          (((double) getNumSamples() / getSampleRate()) * 10000.0);
      // return the time structure projected by the number of ten thousandths of seconds
      return projectTime(startBtime,numTenThousandths);
    }


    /**
       * Get the value of startTime.
       * @return Value of startTime.
       */
    public String getStartTime() {
    // get time structure
    Btime startStruct = getStartBtime();
    // zero padding format of output numbers
        DecimalFormat twoZero = new DecimalFormat("00");
        DecimalFormat threeZero = new DecimalFormat("000");
        DecimalFormat fourZero = new DecimalFormat("0000");
    // return string in standard jday format
    return new String(fourZero.format(startStruct.year) + "," +
                          threeZero.format(startStruct.jday) + "," +
              twoZero.format(startStruct.hour) + ":" +
              twoZero.format(startStruct.min) + ":" +
              twoZero.format(startStruct.sec) + "." +
              fourZero.format(startStruct.tenthMilli) );
    }

    /**
       get the value of end time.  derived from Start time,
       sample rate, and number of samples.

       @return the value of end time
    */
    public String getEndTime() {
    // get time structure
    Btime endStruct = getEndBtime();
    // zero padding format of output numbers
        DecimalFormat twoZero = new DecimalFormat("00");
        DecimalFormat threeZero = new DecimalFormat("000");
        DecimalFormat fourZero = new DecimalFormat("0000");
    // return string in standard jday format
    return new String(fourZero.format(endStruct.year) + "," +
                          threeZero.format(endStruct.jday) + "," +
              twoZero.format(endStruct.hour) + ":" +
              twoZero.format(endStruct.min) + ":" +
              twoZero.format(endStruct.sec) + "." +
              fourZero.format(endStruct.tenthMilli) );
    }


    /**
       get the value of start time in ISO format

       @return the value of start time in ISO format
    */
    public String getISOStartTime() {
        // get time structure
        Btime startStruct = getStartBtime();
        float fSecond = startStruct.sec + startStruct.tenthMilli / 10000f;
        return edu.iris.Fissures.model.ISOTime.getISOString(startStruct.year,
                                                            startStruct.jday,
                                                            startStruct.hour,
                                                            startStruct.min,
                                                            fSecond);
    }

    /**
       * Set the value of startTime.
       * @param v  Value to assign to startTime.
       */
        public void setStartTime(String  v) {
        byte[] startTimeBytes = new byte[10];
        try{
        byte[]  vBytes = v.getBytes("ASCII");
        if(vBytes.length < 10)
        {
            startTimeBytes = Utility.pad(v.getBytes("ASCII"),10, (byte)0);
        }
         }catch( java.io.UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        this.startTime = startTimeBytes;
    }

    public void setStartTime(MicroSecondDate date) {
        String t = seedDate.format(date);
        int millis =(int) (date.getMicroSeconds() % 1000000) / 100;
        setStartTime( t + millis);

    }

    static java.text.SimpleDateFormat seedDate =
        new java.text.SimpleDateFormat("yyyy,DDD,HH,mm,ss.");


    /**
       * Get the value of numSamples.
       * @return Value of numSamples.
       */
    public int getNumSamples() {return numSamples;}

    /**
       * Set the value of numSamples.
       * @param v  Value to assign to numSamples.
       */
      public void setNumSamples(short  v) {

      this.numSamples = v;
      }

    /**
       * Get the value of sampleRateFactor.
       * @return Value of sampleRateFactor.
       */
    public int getSampleRateFactor() {return sampleRateFactor;}

    /**
       * Set the value of sampleRateFactor.
       * @param v  Value to assign to sampleRateFactor.
       */
    public void setSampleRateFactor(short  v) {

    this.sampleRateFactor = v;}

    /**
       * Get the value of sampleRateMultiplier.
       * @return Value of sampleRateMultiplier.
       */
    public int getSampleRateMultiplier() {return sampleRateMultiplier;}

    /**
       * Set the value of sampleRateMultiplier.
       * @param v  Value to assign to sampleRateMultiplier.
       */
      public void setSampleRateMultiplier(short  v) {this.sampleRateMultiplier = v;}

    /**
       * Get the value of activityFlags.
       * @return Value of activityFlags.
       */
    public byte getActivityFlags() {return activityFlags;}

    /**
       * Set the value of activityFlags.
       * @param v  Value to assign to activityFlags.
       */
       public void setActivityFlags(byte  v) {this.activityFlags = v;}

    /**
       * Get the value of IOClockFlags.
       * @return Value of IOClockFlags.
       */
    public byte getIOClockFlags() {return ioClockFlags;}

    /**
       * Set the value of IOClockFlags.
       * @param v  Value to assign to IOClockFlags.
       */
       public void setIOClockFlags(byte  v) {this.ioClockFlags = v;}

    /**
       * Get the value of dataQualityFlags.
       * @return Value of dataQualityFlags.
       */
    public byte getDataQualityFlags() {return dataQualityFlags;}

    /**
       * Set the value of dataQualityFlags.
       * @param v  Value to assign to dataQualityFlags.
       */
      public void setDataQualityFlags(byte  v) {this.dataQualityFlags = v;}

    /**
       * Get the value of numBlockettes.
       * @return Value of numBlockettes.
       */
    public byte getNumBlockettes() {return numBlockettes;}

    /**
       * Set the value of numBlockettes.
       * @param v  Value to assign to numBlockettes.
       */
       public void setNumBlockettes(byte  v) {this.numBlockettes = v;}

    /**
       * Get the value of timeCorrection.
       * @return Value of timeCorrection.
       */
    public byte[] getTimeCorrection() {return timeCorrection;}

    /**
       * Set the value of timeCorrection.
       * @param v  Value to assign to timeCorrection.
       */
      public void setTimeCorrection(byte[]  v) {
      byte[] timeCorrectionBytes = new byte[4];
      if(v.length != 4)
          {
          if(v.length < 4)
             timeCorrectionBytes =  Utility.pad(v, 4, (byte)0);
                  else
              timeCorrectionBytes = Utility.format(v,v.length-4, v.length);
          this.timeCorrection = timeCorrectionBytes;
          }
      else
          {
          this.timeCorrection = v;
          }
            }

    /**
       * Get the value of dataOffset.
       * @return Value of dataOffset.
       */
    public int getDataOffset() {return dataOffset;}

    /**
       * Set the value of dataOffset.
       * @param v  Value to assign to dataOffset.
       */
        public void setDataOffset(short  v) {this.dataOffset = v;}

    /**
       * Get the value of dataBlocketteOffset.
       * @return Value of dataBlocketteOffset.
       */
    public int getDataBlocketteOffset() {return dataBlocketteOffset;}

    /**
       * Set the value of dataBlocketteOffset.
       * @param v  Value to assign to dataBlocketteOffset.
       */
      public void setDataBlocketteOffset(short  v) {this.dataBlocketteOffset = v;}

    /**
       Present a default string representation of the contents of this object

       @return formatted string of object contents
     */
    public String toString() {
    String s = super.toString()+" ";
    s += " "+getStationIdentifier()+"."+
        getLocationIdentifier()+"."+
        getChannelIdentifier()+"."+
        getNetworkCode()+"."+
        getStartTime()+"  "+
            getNumBlockettes()+" "+
        getDataOffset()+" "+
        getDataBlocketteOffset();
    return s;
    }
}
// DataHeader.java
