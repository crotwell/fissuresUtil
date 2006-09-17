package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.seismogram.PopulationProperties;

public class RT130ToLocalSeismogram {

    public RT130ToLocalSeismogram() {}

    public static RT130ToLocalSeismogram create(Properties props)
            throws FileNotFoundException, IOException {
        PropParser pp = new PropParser(props);
        NCFile ncFile = new NCFile(pp.getPath(NCFile.NC_FILE_LOC));
        logger.debug("NC file location: " + ncFile.getCanonicalPath());
        String xyFileLoc = pp.getPath(XYReader.XY_FILE_LOC);
        logger.debug("XY file location: " + xyFileLoc);
        Map stationLocations = XYReader.read(new BufferedReader(new FileReader(xyFileLoc)));
        NetworkAttr attr = PopulationProperties.getNetworkAttr(props);
        NCReader reader = new NCReader(attr, stationLocations);
        reader.load(new FileInputStream(pp.getPath(NCFile.NC_FILE_LOC)));
        Map dataStreamToSampleRate = makeDataStreamToSampleRate(props, pp);
        return new RT130ToLocalSeismogram(new DASChannelCreator(attr,
                                                                null,
                                                                reader.getSites()),
                                          dataStreamToSampleRate);
    }

    public static Map makeDataStreamToSampleRate(Properties props, PropParser pp) {
        Map dataStreamToSampleRate = new HashMap();
        for(int i = 1; i < 7; i++) {
            if(props.containsKey(DATA_STREAM + i)) {
                dataStreamToSampleRate.put(new Integer(i - 1),
                                           new Integer(pp.getInt(DATA_STREAM
                                                   + i)));
            }
        }
        return dataStreamToSampleRate;
    }

    public RT130ToLocalSeismogram(DASChannelCreator chanCreator,
                                  Map dataStreamToSampleRate) {
        this.chanCreator = chanCreator;
        this.dataStreamToSampleRate = dataStreamToSampleRate;
    }

    public LocalSeismogramImpl[] convert(PacketType[] seismogramDataPacket) {
        LocalSeismogramImpl[] seismogramDataArray = new LocalSeismogramImpl[seismogramDataPacket.length];
        channel = null;
        for(int i = 0; i < seismogramDataPacket.length; i++) {
            seismogramDataArray[i] = convert(seismogramDataPacket[i], i);
        }
        return seismogramDataArray;
    }

    private LocalSeismogramImpl convert(PacketType seismogramData, int i) {
        int numPoints = seismogramData.number_of_samples;
        if(seismogramData.sample_rate == 0) {
            logger.debug("A sample rate of 0 samples per second was detected for data stream number "
                    + seismogramData.data_stream_number + ".");
            Integer dataStream = new Integer(seismogramData.data_stream_number);
            if(dataStreamToSampleRate.containsKey(dataStream)) {
                seismogramData.sample_rate = ((Integer)dataStreamToSampleRate.get(dataStream)).intValue();
                logger.debug("The sample rate of " + seismogramData.sample_rate
                        + " was found in the props file, and will be used.");
            } else {
                logger.error("The props file does not contain a sample rate for this "
                        + "data stream, and can not be used to correct the problem.");
            }
        }
        SamplingImpl sampling = new SamplingImpl(seismogramData.sample_rate,
                                                 new TimeInterval(1,
                                                                  UnitImpl.SECOND));
        if(channel == null) {
            channel = createChannel(seismogramData, sampling);
        }
        ChannelId channelId;
        channelId = channel[i].get_id();
        String id = channelId.toString();
        TimeSeriesDataSel timeSeriesDataSel = new TimeSeriesDataSel();
        timeSeriesDataSel.encoded_values(seismogramData.encoded_data);
        MicroSecondDate beginTimeOfSeismogram = LeapSecondApplier.applyLeapSecondCorrection(seismogramData.unitIdNumber,
                                                                                            seismogramData.begin_time_of_seismogram);
        return new LocalSeismogramImpl(id,
                                       beginTimeOfSeismogram.getFissuresTime(),
                                       numPoints,
                                       sampling,
                                       UnitImpl.COUNT,
                                       channelId,
                                       timeSeriesDataSel);
    }

    private Channel[] createChannel(PacketType seismogramData, Sampling sampling) {
        return chanCreator.create(seismogramData.unitIdNumber,
                                  seismogramData.begin_time_of_seismogram,
                                  "" + seismogramData.data_stream_number,
                                  seismogramData.sample_rate);
    }

    public Channel[] getChannels() {
        return channel;
    }

    private Channel[] channel;

    public static final String DATA_STREAM = "dataStream.";

    private Map dataStreamToSampleRate;

    private DASChannelCreator chanCreator;

    private static final Logger logger = Logger.getLogger(RT130ToLocalSeismogram.class);
}
