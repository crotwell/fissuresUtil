package edu.sc.seis.fissuresUtil.mseed;

import java.util.*;
import java.io.*;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesType;

/**
 * FissuresConvert.java
 *
 *
 * Created: Fri Oct 15 09:09:32 1999
 *
 * @author Philip Crotwell
 * @version
 */

public class FissuresConvert  {

    public FissuresConvert() {

    }

    public DataRecord[] toMSeed(LocalSeismogram seis)
        throws SeedFormatException {
        return toMSeed(seis, 1);
    }

    public DataRecord[] toMSeed(LocalSeismogram seis, int seqStart)
        throws SeedFormatException {
        LinkedList outRecords = new LinkedList();
        MicroSecondDate start = new MicroSecondDate(seis.begin_time);
        if ( seis.data.discriminator().equals(TimeSeriesType.TYPE_ENCODED) ) {
            // encoded data
            DataHeader header;
            Blockette1000 b1000;
            EncodedData[] eData = seis.data.encoded_values();
            for ( int i=0; i< eData.length; i++) {
                header = new DataHeader(seqStart++, 'D', false);
                b1000 = new Blockette1000();
                if ( eData[i].values.length + header.getSize() + b1000.getSize() < RECORD_SIZE ) {
                    // can fit into one record
                    ChannelId chan = seis.channel_id;
                    header.setStationIdentifier(chan.station_code);
                    header.setLocationIdentifier(chan.site_code);
                    header.setChannelIdentifier(chan.channel_code);
                    header.setNetworkCode(chan.network_id.network_code);
                    TimeInterval sampPeriod =
                        ((SamplingImpl)seis.sampling_info).getPeriod();
                    header.setStartTime(start);
                    header.setNumSamples((short)eData[i].num_points);
                    start = start.add((TimeInterval)sampPeriod.multiplyBy(eData[i].num_points));

                    // >0 so samples/second
                    // mul by 500 to preserve more digits, 20 sps => 10000
                    // 100
                    // this may not be the best in all cases, but is a
                    // reasonable guess
                    header.setSampleRateFactor((short)(1/sampPeriod.convertTo(UnitImpl.SECOND).getValue()*100));
                    header.setSampleRateMultiplier((short) -100);

                    b1000.setEncodingFormat((byte)eData[i].compression);
                    if ( eData[i].byte_order ) {
                        // seed uses oposite convention
                        b1000.setWordOrder( (byte)0 );
                    } else {
                        b1000.setWordOrder( (byte)1 );
                    } // end of else

                    b1000.setDataRecordLength( RECORD_SIZE_POWER);
                    DataRecord dr = new DataRecord(header);
                    dr.addBlockette(b1000);
                    dr.setData(eData[i].values);
                    outRecords.add(dr);
                } else {
                    throw new SeedFormatException("Can't fit data into record"+
                                                      (eData[i].values.length + header.getSize() + b1000.getSize())+" "+
                                                      eData[i].values.length +" "+ header.getSize() + b1000.getSize());
                } // end of else

            } // end of for ()

        } else {
            // not encoded
            int samples = seis.num_points;
            while ( samples > 0 ) {
                DataHeader header = new DataHeader(seqStart++, 'D', false);
                ChannelId chan = seis.channel_id;
                header.setStationIdentifier(chan.station_code);
                header.setLocationIdentifier(chan.site_code);
                header.setChannelIdentifier(chan.channel_code);
                header.setNetworkCode(chan.network_id.network_code);
                header.setStartTime(start);

                Blockette1000 b1000 = new Blockette1000();

                //  b1000.setEncodeingFormat((byte)seis.);
                DataRecord dr = new DataRecord(header);
            } // end of while ()
        }
        return (DataRecord[])outRecords.toArray(new DataRecord[0]);
    }

    public static LocalSeismogram toFissures(DataRecord seed)
        throws SeedFormatException {


        DataHeader header = seed.getHeader();

        edu.iris.Fissures.Time time =
            new edu.iris.Fissures.Time(header.getISOStartTime(),
                                       -1);
        // the network id isn't correct, but network start is not stored
        // in miniseed
        ChannelId channelId  =
            new ChannelId(new NetworkId(header.getNetworkCode().trim(),
                                        time),
                          header.getStationIdentifier().trim(),
                          header.getLocationIdentifier().trim(),
                          header.getChannelIdentifier().trim(),
                          time);
        String seisId = channelId.network_id.network_code+":"
            +channelId.station_code+":"
            +channelId.site_code+":"
            +channelId.channel_code+":"
            +header.getISOStartTime();
        Property[] props = new Property[1];
        props[0] = new Property("Name", seisId);

        int numPerSampling;
        TimeInterval timeInterval;
        if (header.getSampleRateFactor() > 0) {
            numPerSampling = header.getSampleRateFactor();
            timeInterval = new TimeInterval(1, UnitImpl.SECOND);
            if (header.getSampleRateMultiplier() > 0) {
                numPerSampling *= header.getSampleRateMultiplier();
            } else {
                timeInterval =
                    (TimeInterval)timeInterval.multiplyBy(-1 *
                                                              header.getSampleRateMultiplier());
            }
        } else {
            numPerSampling = 1;
            timeInterval =
                new TimeInterval(-1 * header.getSampleRateFactor(),
                                 UnitImpl.SECOND);
            if (header.getSampleRateMultiplier() > 0) {
                numPerSampling *= header.getSampleRateMultiplier();
            } else {
                timeInterval =
                    (TimeInterval)timeInterval.multiplyBy(-1 *
                                                              header.getSampleRateMultiplier());
            }
        }

        SamplingImpl sampling =
            new SamplingImpl(numPerSampling,
                             timeInterval);
        TimeSeriesDataSel bits = convertData(seed);

        return new LocalSeismogramImpl(seisId,
                                       props,
                                       time,
                                       header.getNumSamples(),
                                       sampling,
                                       UnitImpl.COUNT,
                                       channelId,
                                       new edu.iris.Fissures.IfParameterMgr.ParameterRef[0],
                                       new QuantityImpl[0],
                                       new SamplingImpl[0],
                                       bits);
    }

    public static TimeSeriesDataSel convertData(DataRecord seed)
        throws SeedFormatException {
        Blockette[] allBs = seed.getBlockettes(1000);
        if (allBs.length == 0) {
            throw new SeedFormatException("No blockette 1000s in the volume.");
        } else if (allBs.length > 1) {
            throw new SeedFormatException(
                "Multiple blockette 1000s in the volume. "+
                    allBs.length);
        }
        Blockette1000 b1000 = (Blockette1000)allBs[0];

        EncodedData eData =
            new EncodedData(b1000.getEncodingFormat(),
                            seed.getData(),
                            seed.getHeader().getNumSamples(),
                            ! b1000.isBigEndian());
        EncodedData[] eArray = new EncodedData[1];
        eArray[0] = eData;
        TimeSeriesDataSel bits = new TimeSeriesDataSel();
        bits.encoded_values(eArray);
        return bits;
    }

    public static SeismogramAttrImpl convertAttributes(DataRecord seed)
        throws SeedFormatException {
        // wasteful as this does the data as well...
        return (SeismogramAttrImpl)toFissures(seed);
    }

    byte RECORD_SIZE_POWER = 12;

    int RECORD_SIZE = (int)Math.pow(2, RECORD_SIZE_POWER);

} // FissuresConvert
