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

    // fix this as seismogram is now an interface
    
//     public static DataSet toFissures(MiniSeedRead seed, String name)
//         throws SeedFormatException {
//         Seismogram seis;
//         String chanID;
//         SeedRecord sr;
//         ArrayList channel;
//         HashMap allChannels = new HashMap();
//         //loop until we catch an EOF
//         while (true) {
//             try {
//                 sr = seed.getNextRecord();
//                 System.out.println("DATA RECORD:"+(DataRecord)sr);
//                 seis = FissuresConvert.toFissures((DataRecord)sr);
//                 chanID = seis.getChannelID().local_name;
//                 if (allChannels.containsKey(chanID)) {
//                     channel = (ArrayList)allChannels.get(chanID);
//                     channel.add(seis);
//                 } else {
//                     channel = new ArrayList();
//                     channel.add(seis);
//                     allChannels.put(chanID, channel);
//                 }
//                 System.out.println(seis.getBeginTime()+" "+seis.getMinValue()+"  "+seis.getMaxValue());
//             } catch (IOException e) {
//                 // IOException means we are at the end of the file/stream
//                 // so break out of the accululation while loop
//                 break;
//             }
//         }

//         Set keys = allChannels.keySet();
//         Iterator it = keys.iterator();
 
//         ArrayList allSeismograms = new ArrayList();

//         // iterate to join seismogram segments
//         while (it.hasNext()) {
//             channel = (ArrayList)allChannels.get(it.next());
//             DataGrouper.combineSeismograms(channel);
//             allSeismograms.addAll(channel);
//         }
//         Seismogram[] temp = new Seismogram[0]; // gives type for toArray
//         return DataGrouper.createDataSet((Seismogram[])allSeismograms.toArray(temp),
//                                          name);
//     }


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

} // FissuresConvert
