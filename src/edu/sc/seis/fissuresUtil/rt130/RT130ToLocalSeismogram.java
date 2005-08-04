package edu.sc.seis.fissuresUtil.rt130;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.TimeUtils;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkId;

public class RT130ToLocalSeismogram {
    
    public RT130ToLocalSeismogram(){
        this.conn = null;
        this.props = null;
    }

    public RT130ToLocalSeismogram(Connection conn, Properties props) {
        this.conn = conn;
        this.props = props;
    }

    public LocalSeismogramImpl[] ConvertRT130ToLocalSeismogram(PacketType[] seismogramDataPacket){
        LocalSeismogramImpl[] seismogramDataArray = new LocalSeismogramImpl[seismogramDataPacket.length];
        this.channel = new Channel[seismogramDataPacket.length];
        for(int i = 0; i < seismogramDataPacket.length; i++){
            seismogramDataArray[i] = ConvertRT130ToLocalSeismogram(seismogramDataPacket[i], i);
        }
        return seismogramDataArray;
    }
    
    public LocalSeismogramImpl ConvertRT130ToLocalSeismogram(PacketType seismogramData, int i) {
        Time mockBeginTimeOfChannel = seismogramData.begin_time_from_state_of_health_file.getFissuresTime();
        int numPoints = seismogramData.number_of_samples;
        SamplingImpl sampling = new SamplingImpl(seismogramData.sample_rate,
                                                 new TimeInterval(1,
                                                                  UnitImpl.SECOND));
        ChannelId channelId;
        if(props == null || conn == null) {
            channelId = new ChannelId(MockNetworkId.createNetworkID(),
                                      seismogramData.unitIdNumber,
                                      "" + seismogramData.data_stream_number,
                                      "BH" + seismogramData.channel_name[seismogramData.channel_number],
                                      mockBeginTimeOfChannel);
        } else {
            this.channel[i] = createChannel(seismogramData, sampling);
            channelId = channel[i].get_id();
        }
        String id = channelId.toString();
        TimeSeriesDataSel timeSeriesDataSel = new TimeSeriesDataSel();
        timeSeriesDataSel.encoded_values(seismogramData.encoded_data);
        return new LocalSeismogramImpl(id,
                                       seismogramData.begin_time_of_seismogram.getFissuresTime(),
                                       numPoints,
                                       sampling,
                                       UnitImpl.COUNT,
                                       channelId,
                                       timeSeriesDataSel);
    }

    private Channel createChannel(PacketType seismogramData, Sampling sampling) {
        String stationCode = props.getProperty(STATION_NAME + "."
                + seismogramData.unitIdNumber);
        if(stationCode == null){
            stationCode = seismogramData.unitIdNumber;
        }
//        String networkIdString = props.getProperty(NETWORK_ID);
        String networkIdString = "XX";
//        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//        MicroSecondDate msd = null;
//        try {
//            msd = new MicroSecondDate(simpleDate.parse(props.getProperty(NETWORK_BEGIN_TIME)));
//        } catch(ParseException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        Time networkBeginTime = msd.getFissuresTime();
        ISOTime isoTime = new ISOTime(1970, 0, 0, 0, 0);
        Time networkBeginTime = isoTime.getDate().getFissuresTime();
        Time channelBeginTime = seismogramData.begin_time_from_state_of_health_file.getFissuresTime();
        NetworkId networkId = new NetworkId(networkIdString, networkBeginTime);
        ChannelId channelId = new ChannelId(networkId, stationCode, "00", "BH"
                + seismogramData.channel_name[seismogramData.channel_number], channelBeginTime);
        TimeRange effectiveNetworkTime = new TimeRange(networkBeginTime,
                                                TimeUtils.timeUnknown);
        TimeRange effectiveChannelTime = new TimeRange(channelBeginTime,
                                                         TimeUtils.timeUnknown);
        SiteId siteId = new SiteId(networkId,
                                   stationCode,
                                   "00",
                                   channelBeginTime);
        StationId stationId = new StationId(networkId,
                                            stationCode,
                                            channelBeginTime);
        QuantityImpl elevation = new QuantityImpl(0, UnitImpl.METER);
        QuantityImpl depth = elevation;
        Location location = new Location(seismogramData.latitude_,
                                         seismogramData.longitude_,
                                         elevation,
                                         depth,
                                         LocationType.from_int(0));
        NetworkAttrImpl networkAttr = new NetworkAttrImpl(networkId,
                                                          "",
                                                          "",
                                                          "",
                                                          effectiveNetworkTime);
        StationImpl station = new StationImpl(stationId,
                                              "",
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
                                          new Orientation(0, -90),
                                          sampling,
                                          effectiveChannelTime,
                                          site);
        return newChannel;
    }
    
    public Channel[] getChannels(){
        return this.channel;
    }
    
    private Channel[] channel;

    private final String NETWORK_ID = "network.networkId";

    private final String STATION_NAME = "station.stationName";

    private final String NETWORK_BEGIN_TIME = "network.beginTime";

    private Connection conn;

    private Properties props;
}
