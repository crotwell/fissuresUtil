package edu.sc.seis.fissuresUtil.mseed;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.UnitBase;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfRealTimeCollector.DataChunk;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.Property;
import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesType;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.iris.dmc.seedcodec.B1000Types;
import edu.sc.seis.fissuresUtil.database.DataCenterUtil;
import edu.sc.seis.seisFile.mseed.Blockette;
import edu.sc.seis.seisFile.mseed.Blockette100;
import edu.sc.seis.seisFile.mseed.Blockette1000;
import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataHeader;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;

/**
 * FissuresConvert.java
 * 
 * 
 * Created: Fri Oct 15 09:09:32 1999
 * 
 * @author Philip Crotwell
 * @version
 */
public class FissuresConvert {

    private FissuresConvert() {}

    public static DataRecord[] toMSeed(LocalSeismogram seis) throws SeedFormatException {
        return toMSeed(seis, 1);
    }

    public static DataRecord[] toMSeed(LocalSeismogram seis, int seqStart) throws SeedFormatException {
        LinkedList<DataRecord> outRecords = new LinkedList<DataRecord>();
        MicroSecondDate start = new MicroSecondDate(seis.begin_time);
        if (seis.data.discriminator().equals(TimeSeriesType.TYPE_ENCODED)) {
            // encoded data
            EncodedData[] eData = seis.data.encoded_values();
            outRecords = toMSeed(eData, seis.channel_id, start, (SamplingImpl)seis.sampling_info, seqStart);
        } else if (seis.data.discriminator().equals(TimeSeriesType.TYPE_FLOAT)) {
            try {
                // for float, 64 bytes = 4 bytes * 16 samples, so each edata
                // holds 62*16 samples
                EncodedData[] eData = new EncodedData[(int)Math.ceil(seis.num_points * 4.0f / (62 * 64))];
                float[] data = seis.get_as_floats();
                for (int i = 0; i < eData.length; i++) {
                    byte[] dataBytes = new byte[62 * 64];
                    int j;
                    for (j = 0; j + (62 * 16 * i) < data.length && j < 62 * 16; j++) {
                        int val = Float.floatToIntBits(data[j + (62 * 16 * i)]);
                        dataBytes[4 * j] = (byte)((val & 0xff000000) >> 24);
                        dataBytes[4 * j + 1] = (byte)((val & 0x00ff0000) >> 16);
                        dataBytes[4 * j + 2] = (byte)((val & 0x0000ff00) >> 8);
                        dataBytes[4 * j + 3] = (byte)((val & 0x000000ff));
                    }
                    if (j == 0) {
                        throw new SeedFormatException("try to put 0 float samples into an encodedData object j=" + j
                                + " i=" + i + " seis.num_ppoints=" + seis.num_points);
                    }
                    eData[i] = new EncodedData((short)B1000Types.FLOAT, dataBytes, j, false);
                }
                outRecords = toMSeed(eData, seis.channel_id, start, (SamplingImpl)seis.sampling_info, seqStart);
            } catch(FissuresException e) {
                // this shouldn't ever happen as we already checked the type
                throw new SeedFormatException("Problem getting float data", e);
            }
        } else {
            // not encoded
            throw new SeedFormatException("Can only handle EncodedData now, type=" + seis.data.discriminator().value());
            // int samples = seis.num_points;
            // while ( samples > 0 ) {
            // DataHeader header = new DataHeader(seqStart++, 'D', false);
            // ChannelId chan = seis.channel_id;
            // header.setStationIdentifier(chan.station_code);
            // header.setLocationIdentifier(chan.site_code);
            // header.setChannelIdentifier(chan.channel_code);
            // header.setNetworkCode(chan.network_id.network_code);
            // header.setStartTime(start);
            //
            // Blockette1000 b1000 = new Blockette1000();
            //
            // // b1000.setEncodeingFormat((byte)seis.);
            // DataRecord dr = new DataRecord(header);
            // } // end of while ()
        }
        return outRecords.toArray(new DataRecord[0]);
    }

    public static DataRecord[] toMSeed(DataChunk chunk) throws SeedFormatException {
        LinkedList<DataRecord> outRecords;
        if (chunk.data.discriminator().equals(TimeSeriesType.TYPE_ENCODED)) {
            outRecords = toMSeed(chunk.data.encoded_values(),
                                 chunk.channel,
                                 new MicroSecondDate(chunk.begin_time),
                                 DataCenterUtil.getSampling(chunk),
                                 chunk.seq_num);
        } else {
            throw new SeedFormatException("Can only handle EncodedData now");
        }
        return outRecords.toArray(new DataRecord[0]);
    }

    public static LinkedList<DataRecord> toMSeed(EncodedData[] eData,
                                                 ChannelId channel_id,
                                                 MicroSecondDate start,
                                                 SamplingImpl sampling_info,
                                                 int seqStart) throws SeedFormatException {
        LinkedList<DataRecord> list = new LinkedList<DataRecord>();
        DataHeader header;
        Blockette1000 b1000;
        Blockette100 b100;
        for (int i = 0; i < eData.length; i++) {
            header = new DataHeader(seqStart++, 'D', false);
            b1000 = new Blockette1000();
            b100 = new Blockette100();
            if (eData[i].values.length + header.getSize() + b1000.getSize() + b100.getSize() < RECORD_SIZE) {
                // ok to use Blockette100 for sampling
            } else if (eData[i].values.length + header.getSize() + b1000.getSize() < RECORD_SIZE) {
                // will fit without Blockette100
                b100 = null;
            } else {
                throw new SeedFormatException("Can't fit data into record "
                        + (eData[i].values.length + header.getSize() + b1000.getSize() + b100.getSize()) + " "
                        + eData[i].values.length + " " + (header.getSize() + b1000.getSize() + b100.getSize()));
            } // end of else
              // can fit into one record
            header.setStationIdentifier(channel_id.station_code);
            header.setLocationIdentifier(channel_id.site_code);
            header.setChannelIdentifier(channel_id.channel_code);
            header.setNetworkCode(channel_id.network_id.network_code);
            header.setStartBtime(getBtime(start));
            header.setNumSamples((short)eData[i].num_points);
            TimeInterval sampPeriod = sampling_info.getPeriod();
            start = start.add((TimeInterval)sampPeriod.multiplyBy(eData[i].num_points));
            short[] multiAndFactor = calcSeedMultipilerFactor(sampling_info);
            header.setSampleRateFactor(multiAndFactor[0]);
            header.setSampleRateMultiplier(multiAndFactor[1]);
            b1000.setEncodingFormat((byte)eData[i].compression);
            if (eData[i].byte_order) {
                // seed uses oposite convention
                b1000.setWordOrder((byte)0);
            } else {
                b1000.setWordOrder((byte)1);
            } // end of else
            b1000.setDataRecordLength(RECORD_SIZE_POWER);
            DataRecord dr = new DataRecord(header);
            dr.addBlockette(b1000);
            QuantityImpl hertz = sampling_info.getFrequency().convertTo(UnitImpl.HERTZ);
            if (b100 != null) {
                b100.setActualSampleRate((float)hertz.getValue());
                dr.addBlockette(b100);
            }
            dr.setData(eData[i].values);
            list.add(dr);
        } // end of for ()
        return list;
    }

    public static short[] calcSeedMultipilerFactor(SamplingImpl sampling) {
        TimeInterval sampPeriod = sampling.getPeriod();
        double sps = 1 / sampPeriod.convertTo(UnitImpl.SECOND).getValue();
        if (sps >= 1) {
            // don't get too close to the max for a short, use ceil as neg
            int divisor = (int)Math.ceil((Short.MIN_VALUE + 2) / sps);
            // don't get too close to the max for a short
            if (divisor < Short.MIN_VALUE + 2) {
                divisor = Short.MIN_VALUE + 2;
            }
            int factor = (int)Math.round(-1 * sps * divisor);
            return new short[] {(short)factor, (short)divisor};
        } else {
            // don't get too close to the max for a short, use ceil as neg
            int factor = -1 * (int)Math.round(Math.floor(1.0 * sps * (Short.MAX_VALUE - 2)) / sps);
            // don't get too close to the max for a short
            if (factor > Short.MAX_VALUE - 2) {
                factor = Short.MAX_VALUE - 2;
            }
            int divisor = (int)Math.round(-1 * factor * sps);
            return new short[] {(short)factor, (short)divisor};
        }
    }

    public static LocalSeismogramImpl toFissures(String filename) throws SeedFormatException, IOException,
            FissuresException {
        List<DataRecord> data = new ArrayList<DataRecord>();
        DataInput dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
        try {
            while (true) {
                SeedRecord sr = SeedRecord.read(dis, 4096);
                if (sr instanceof DataRecord) {
                    data.add((DataRecord)sr);
                }
            }
        } catch(EOFException e) {}
        return toFissures(data.toArray(new DataRecord[0]));
    }

    /**
     * assume all records from same channel and in time order with no
     * gaps/overlaps.
     */
    public static LocalSeismogramImpl toFissures(DataRecord[] seed) throws SeedFormatException, FissuresException {
        LocalSeismogramImpl seis = toFissures(seed[0]);
        // System.out.println("AFTER FIRST TO FISSURES: " +
        // seis.getBeginTime());
        for (int i = 1; i < seed.length; i++) {
            append(seis, seed[i]);
        }
        // System.out.println("AFTER APPEND: " + seis.getBeginTime());
        return seis;
    }

    /**
     * assume all records from same channel and in time order with no
     * gaps/overlaps. Specifying a default compression and byte order. This
     * should only be used in cases where the miniseed records are older than
     * the Blockette 1000 SEED specification and where the compression and byte
     * order are known from outside sources. Per the SEED specification, valid
     * miniseed MUST have a blockette 1000 and so this method exists only for
     * reading older data.
     * 
     * @param defaultCompression
     *            compression to use if there is no blockette 1000, See the SEED
     *            specification for blockette 1000 for valid compression types.
     * @param defaultByteOrder
     *            byte order to use if there is no blockette 1000. 0 indicates
     *            little-endian order and a 1 indicates big-endian.
     */
    public static LocalSeismogramImpl toFissures(DataRecord[] seed, byte defaultCompression, byte defaultByteOrder)
            throws SeedFormatException, FissuresException {
        DataRecord[] seedCopy = new DataRecord[seed.length];
        for (int i = 0; i < seed.length; i++) {
            if (seed[i].getBlockettes(1000).length == 0) {
                seedCopy[i] = new DataRecord(seed[i]);
                Blockette1000 fakeB1000 = new Blockette1000();
                fakeB1000.setEncodingFormat(defaultCompression);
                fakeB1000.setWordOrder(defaultByteOrder);
                fakeB1000.setDataRecordLength((byte)30);// should be huge and we
                                                        // will never write this
                                                        // out
                seedCopy[i].setRecordSize(8184); // make this bug enough for the
                                                 // extra blockette
                seedCopy[i].addBlockette(fakeB1000);
            } else {
                seedCopy[i] = seed[i];
            }
        }
        return toFissures(seedCopy);
    }

    /**
     * assume all records from same channel and in time order with no
     * gaps/overlaps.
     */
    public static LocalSeismogramImpl append(LocalSeismogramImpl seis, DataRecord[] seed) throws SeedFormatException,
            FissuresException {
        for (int i = 0; i < seed.length; i++) {
            append(seis, seed[i]);
        }
        return seis;
    }

    /**
     * assume all records from same channel and in time order with no
     * gaps/overlaps.
     */
    public static LocalSeismogramImpl append(LocalSeismogramImpl seis, DataRecord seed) throws SeedFormatException,
            FissuresException {
        TimeSeriesDataSel bits = convertData(seed);
        EncodedData[] edata = bits.encoded_values();
        for (int j = 0; j < edata.length; j++) {
            if (edata[j] == null) {
                System.err.println("encoded data is null " + j);
                System.exit(1);
            }
            seis.append_encoded(edata[j]);
        }
        return seis;
    }

    public static LocalSeismogramImpl toFissures(DataRecord seed) throws SeedFormatException {
        DataHeader header = seed.getHeader();
        String isoTime = getISOTime(header.getStartBtime());
        // the network id isn't correct, but network start is not stored
        // in miniseed
        ChannelId channelId = new ChannelId(new NetworkId(header.getNetworkCode().trim(), new Time(isoTime, -1)),
                                            header.getStationIdentifier().trim(),
                                            header.getLocationIdentifier().trim(),
                                            header.getChannelIdentifier().trim(),
                                            new Time(isoTime, -1));
        String seisId = channelId.network_id.network_code + ":" + channelId.station_code + ":" + channelId.site_code
                + ":" + channelId.channel_code + ":" + getISOTime(header.getStartBtime());
        Property[] props = new Property[1];
        props[0] = new Property("Name", seisId);
        SamplingImpl sampling = convertSampleRate(seed);
        TimeSeriesDataSel bits = convertData(seed);
        return new LocalSeismogramImpl(seisId,
                                       props,
                                       new Time(isoTime, -1),
                                       header.getNumSamples(),
                                       sampling,
                                       UnitImpl.COUNT,
                                       channelId,
                                       new edu.iris.Fissures.IfParameterMgr.ParameterRef[0],
                                       new QuantityImpl[0],
                                       new SamplingImpl[0],
                                       bits);
    }

    public static SamplingImpl convertSampleRate(DataRecord seed) {
        SamplingImpl sampling;
        Blockette[] blocketts = seed.getBlockettes(100);
        int numPerSampling;
        TimeInterval timeInterval;
        if (blocketts.length != 0) {
            Blockette100 b100 = (Blockette100)blocketts[0];
            float f = b100.getActualSampleRate();
            numPerSampling = 1;
            timeInterval = new TimeInterval(1 / f, UnitImpl.SECOND);
            sampling = new SamplingImpl(numPerSampling, timeInterval);
        } else {
            DataHeader header = seed.getHeader();
            sampling = convertSampleRate(header.getSampleRateMultiplier(), header.getSampleRateFactor());
        }
        return sampling;
    }

    public static SamplingImpl convertSampleRate(int multi, int factor) {
        int numPerSampling;
        TimeInterval timeInterval;
        if (factor > 0) {
            numPerSampling = factor;
            timeInterval = new TimeInterval(1, UnitImpl.SECOND);
            if (multi > 0) {
                numPerSampling *= multi;
            } else {
                timeInterval = (TimeInterval)timeInterval.multiplyBy(-1 * multi);
            }
        } else {
            numPerSampling = 1;
            timeInterval = new TimeInterval(-1 * factor, UnitImpl.SECOND);
            if (multi > 0) {
                numPerSampling *= multi;
            } else {
                timeInterval = (TimeInterval)timeInterval.multiplyBy(-1 * multi);
            }
        }
        SamplingImpl sampling = new SamplingImpl(numPerSampling, timeInterval);
        return sampling;
    }

    public static TimeSeriesDataSel convertData(DataRecord seed) throws SeedFormatException {
        Blockette1000 b1000 = (Blockette1000)seed.getUniqueBlockette(1000);
        EncodedData eData = new EncodedData(b1000.getEncodingFormat(),
                                            seed.getData(),
                                            seed.getHeader().getNumSamples(),
                                            !b1000.isBigEndian());
        EncodedData[] eArray = new EncodedData[1];
        eArray[0] = eData;
        TimeSeriesDataSel bits = new TimeSeriesDataSel();
        bits.encoded_values(eArray);
        return bits;
    }

    public static SeismogramAttrImpl convertAttributes(DataRecord seed) throws SeedFormatException {
        // wasteful as this does the data as well...
        return toFissures(seed);
    }

    /**
     * get the value of start time in ISO format
     * 
     * @return the value of start time in ISO format
     */
    public static String getISOTime(Btime startStruct) {
        float fSecond = startStruct.sec + startStruct.tenthMilli / 10000f;
        return edu.iris.Fissures.model.ISOTime.getISOString(startStruct.year,
                                                            startStruct.jday,
                                                            startStruct.hour,
                                                            startStruct.min,
                                                            fSecond);
    }

    /**
     * get the value of start time in MicroSecondDate format
     * 
     * @return the value of start time in MicroSecondDate format
     */
    public MicroSecondDate getMicroSecondTime(Btime startStruct) {
        ISOTime iso = new ISOTime(startStruct.year,
                                  startStruct.jday,
                                  startStruct.hour,
                                  startStruct.min,
                                  startStruct.sec);
        MicroSecondDate d = iso.getDate().add(new TimeInterval(startStruct.tenthMilli, UnitImpl.TENTHMILLISECOND));
        return d;
    }

    public static Btime getBtime(MicroSecondDate date) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        Btime btime = new Btime();
        btime.tenthMilli = (int)(cal.get(Calendar.MILLISECOND) * 10 + (Math.round(date.getMicroSeconds() / 100.0)));
        btime.year = cal.get(Calendar.YEAR);
        btime.jday = cal.get(Calendar.DAY_OF_YEAR);
        btime.hour = cal.get(Calendar.HOUR_OF_DAY);
        btime.min = cal.get(Calendar.MINUTE);
        btime.sec = cal.get(Calendar.SECOND);
        return btime;
    }

    static final byte RECORD_SIZE_POWER = 12;

    static int RECORD_SIZE = (int)Math.pow(2, RECORD_SIZE_POWER);

    /**
     * Turns a UnitImpl into a byte array using Java serialization
     */
    public static byte[] toBytes(UnitImpl obj) {
        ByteArrayOutputStream byteHolder = new ByteArrayOutputStream();
        try {
            ObjectOutputStream fissuresWriter = new ObjectOutputStream(byteHolder);
            fissuresWriter.writeObject(obj);
            return byteHolder.toByteArray();
        } catch(IOException io) {
            throw new RuntimeException("Didn't think it was possible to get an IO exception dealing entirely with in memory streams",
                                       io);
        }
    }

    /**
     * Turns a byte array containing just a serialized UnitImpl object back into
     * an UnitImpl
     */
    public static UnitImpl fromBytes(byte[] bytes) throws IOException {
        UnitImpl impl;
        try {
            impl = (UnitImpl)new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
        } catch(ClassNotFoundException cnf) {
            throw new IllegalArgumentException("The serialized bytes passed to fromBytes must contain a serialized UnitImpl, instead it was a class we couldn't find: "
                    + cnf.getMessage());
        }
        singletonizeUnitBase(impl);
        return impl;
    }

    private static void singletonizeUnitBase(UnitImpl impl) {
        impl.the_unit_base = UnitBase.from_int(impl.the_unit_base.value());
        for (int i = 0; i < impl.elements.length; i++) {
            singletonizeUnitBase((UnitImpl)impl.elements[i]);
        }
    }
} // FissuresConvert
