package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.TimeUtils;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.seismogram.PopulationProperties;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkId;

public class RT130ToLocalSeismogram {

    public RT130ToLocalSeismogram() {
        this.ncFile = null;
        this.stationLocations = null;
    }

    public static RT130ToLocalSeismogram create(Properties props)
            throws FileNotFoundException, IOException {
        PropParser pp = new PropParser(props);
        NCFile ncFile = new NCFile(pp.getPath(NCFile.NC_FILE_LOC));
        logger.debug("NC file location: " + ncFile.getCanonicalPath());
        String xyFileLoc = pp.getPath(XYReader.XY_FILE_LOC);
        logger.debug("XY file location: " + xyFileLoc);
        Map stationLocations = XYReader.read(new BufferedReader(new FileReader(xyFileLoc)));
        Map dataStreamToSampleRate = new HashMap();
        for(int i = 1; i < 7; i++) {
            if(props.containsKey(DATA_STREAM + i)) {
                dataStreamToSampleRate.put(new Integer(i - 1),
                                           new Integer(pp.getInt(DATA_STREAM
                                                   + i)));
            }
        }
        NetworkAttr attr = PopulationProperties.getNetworkAttr(props);
        return new RT130ToLocalSeismogram(ncFile,
                                          stationLocations,
                                          dataStreamToSampleRate,
                                          attr);
    }

    public RT130ToLocalSeismogram(NCFile ncFile,
                                  Map stationLocations,
                                  Map dataStreamToSampleRate,
                                  NetworkAttr attr) {
        this.ncFile = ncFile;
        this.stationLocations = stationLocations;
        this.dataStreamToSampleRate = dataStreamToSampleRate;
        this.networkAttr = attr;
    }

    public LocalSeismogramImpl[] ConvertRT130ToLocalSeismogram(PacketType[] seismogramDataPacket) {
        LocalSeismogramImpl[] seismogramDataArray = new LocalSeismogramImpl[seismogramDataPacket.length];
        this.channel = new Channel[seismogramDataPacket.length];
        for(int i = 0; i < seismogramDataPacket.length; i++) {
            seismogramDataArray[i] = ConvertRT130ToLocalSeismogram(seismogramDataPacket[i],
                                                                   i);
        }
        return seismogramDataArray;
    }

    public LocalSeismogramImpl ConvertRT130ToLocalSeismogram(PacketType seismogramData,
                                                             int i) {
        Time mockBeginTimeOfChannel = seismogramData.begin_time_from_state_of_health_file.getFissuresTime();
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
        ChannelId channelId;
        if(ncFile == null) {
            channelId = new ChannelId(MockNetworkId.createNetworkID(),
                                      seismogramData.unitIdNumber,
                                      "" + seismogramData.data_stream_number,
                                      "BH"
                                              + seismogramData.channel_name[seismogramData.channel_number],
                                      mockBeginTimeOfChannel);
        } else {
            this.channel[i] = createChannel(seismogramData, sampling);
            channelId = channel[i].get_id();
        }
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

    private Channel createChannel(PacketType seismogramData, Sampling sampling) {
        String stationCode = ncFile.getUnitName(seismogramData.begin_time_from_state_of_health_file,
                                                seismogramData.unitIdNumber);
        Time networkBeginTime = ncFile.network_begin_time.getFissuresTime();
        Time channelBeginTime = seismogramData.begin_time_from_state_of_health_file.getFissuresTime();
        networkAttr.get_id().begin_time = networkBeginTime;
        String tempCode = "B";
        if(seismogramData.sample_rate < 10) {
            tempCode = "L";
        }
        ChannelId channelId = new ChannelId(networkAttr.get_id(),
                                            stationCode,
                                            "00",
                                            tempCode
                                                    + "H"
                                                    + seismogramData.channel_name[seismogramData.channel_number],
                                            channelBeginTime);
        TimeRange effectiveChannelTime = new TimeRange(channelBeginTime,
                                                       TimeUtils.timeUnknown);
        SiteId siteId = new SiteId(networkAttr.get_id(),
                                   stationCode,
                                   "00",
                                   channelBeginTime);
        StationId stationId = new StationId(networkAttr.get_id(),
                                            stationCode,
                                            channelBeginTime);
        Location location = new Location(0,
                                         0,
                                         new QuantityImpl(0, UnitImpl.METER),
                                         new QuantityImpl(0, UnitImpl.METER),
                                         LocationType.GEOGRAPHIC);
        if(stationLocations.containsKey(stationCode)) {
            location = (Location)stationLocations.get(stationCode);
        } else {
            logger.error("XY file did not contain a location for unit "
                    + stationCode
                    + ".\n"
                    + "The location used for the unit will be a lat/long of 0/0.");
        }
        StationImpl station = new StationImpl(stationId,
                                              stationCode,
                                              location,
                                              effectiveChannelTime,
                                              "",
                                              "",
                                              "",
                                              networkAttr);
        SiteImpl site = new SiteImpl(siteId,
                                     location,
                                     effectiveChannelTime,
                                     station,
                                     "");
        Channel newChannel = new ChannelImpl(channelId,
                                             "",
                                             new Orientation(0, 0),
                                             sampling,
                                             effectiveChannelTime,
                                             site);
        if(channelId.channel_code.endsWith("N")) {
            newChannel.an_orientation = new Orientation(0, 0);
        } else if(channelId.channel_code.endsWith("E")) {
            newChannel.an_orientation = new Orientation(90, 0);
        } else if(channelId.channel_code.endsWith("Z")) {
            newChannel.an_orientation = new Orientation(0, -90);
        }
        return newChannel;
    }

    public Channel[] getChannels() {
        return channel;
    }

    private Channel[] channel;

    private NetworkAttr networkAttr;

    public static final String DATA_STREAM = "dataStream.";

    private NCFile ncFile;

    private Map stationLocations, dataStreamToSampleRate;

    private static final Logger logger = Logger.getLogger(RT130ToLocalSeismogram.class);
}
