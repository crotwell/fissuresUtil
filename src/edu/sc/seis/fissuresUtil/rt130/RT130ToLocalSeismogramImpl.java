package edu.sc.seis.fissuresUtil.rt130;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.model.MicroSecondDate;
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

/**
 * @author fenner Created on Jun 13, 2005
 */
public class RT130ToLocalSeismogramImpl {

    public RT130ToLocalSeismogramImpl(DataInput inputStream) {
        this.dataInputStream = inputStream;
        this.props = null;
        this.conn = null;
    }

    public RT130ToLocalSeismogramImpl(DataInput inputStream,
                                                 Connection conn,
                                                 Properties props) {
        this.dataInputStream = inputStream;
        this.props = props;
        this.conn = conn;
    }

    public void close() {
        this.dataInputStream = null;
    }

    public LocalSeismogramImpl[] readEntireDataFile()
            throws RT130FormatException, IOException {
        boolean done = false;
        List seismogramList = new ArrayList();
        PacketType nextPacket = new PacketType();
        PacketType header = new PacketType();
        Map seismogramData = new HashMap();
        try {
            nextPacket = new PacketType(dataInputStream);
        } catch(EOFException e) {
            System.err.println("End of file reached before Event Trailer Packet was read.");
            System.err.println("The file likely contains an incomplete seismogram.");
            System.err.println("Local seismogram creation was not disturbed.");
            done = true;
        }
        while(!done) {
            if(nextPacket.packetType.equals("DT")) {
                Integer i = new Integer(nextPacket.dP.channelNumber);
                if(!seismogramData.containsKey(i)) {
                    seismogramData.put(i, new PacketType(header));
                }
                TimeInterval lengthOfData = new TimeInterval(((double)nextPacket.dP.numberOfSamples / (double)((PacketType)seismogramData.get(i)).sample_rate),
                                                             UnitImpl.SECOND);
                nextPacket.end_time_of_last_packet = nextPacket.begin_time_of_first_packet.add(lengthOfData);
                append(seismogramData, i, nextPacket, seismogramList);
            } else if(nextPacket.packetType.equals("EH")) {
                seismogramData.put(new Integer(1),
                                   Append.appendEventHeaderPacket(new PacketType(),
                                                                  nextPacket));
                header = new PacketType(nextPacket);
            } else if(nextPacket.packetType.equals("ET")) {
                for(Integer j = new Integer(0); seismogramData.containsKey(j); j = new Integer(j.intValue() + 1)) {
                    seismogramData.put(j,
                                       Append.appendEventTrailerPacket((PacketType)seismogramData.get(j),
                                                                       nextPacket));
                    seismogramList.add(makeSeismogram((PacketType)seismogramData.get(j), false));
                    resetSeismogramData((PacketType)seismogramData.get(j), nextPacket);
                }
                done = true;
            } else if(nextPacket.packetType.equals("AD")) {
                System.err.println("The given data file contains an unexpected Auxiliary Data Parameter Packet.");
                System.err.println("More than likely you are reading the State-of-Health file.");
                return null;
            } else if(nextPacket.packetType.equals("CD")) {
                System.err.println("The given data file contains an unexpected Calibration Parameter Packet.");
                System.err.println("More than likely you are reading the State-of-Health file.");
                return null;
            } else if(nextPacket.packetType.equals("DS")) {
                System.err.println("The given data file contains an unexpected Data Stream Parameter Packet.");
                System.err.println("More than likely you are reading the State-of-Health file.");
                return null;
            } else if(nextPacket.packetType.equals("OM")) {
                System.err.println("The given data file contains an unexpected Operating Mode Parameter Packet.");
                System.err.println("More than likely you are reading the State-of-Health file.");
                return null;
            } else if(nextPacket.packetType.equals("SH")) {
                System.err.println("The given data file contains an unexpected State-Of-Health Packet.");
                System.err.println("More than likely you are reading the State-of-Health file.");
                return null;
            } else if(nextPacket.packetType.equals("SC")) {
                System.err.println("The given data file contains an unexpected Station/Channel Parameter Packet.");
                System.err.println("More than likely you are reading the State-of-Health file.");
                return null;
            } else {
                System.err.println("The first two bytes of the Packet Header were not formatted");
                System.err.println("correctly, and do not refer to a valid Packet Type.");
                throw new RT130FormatException();
            }
            if(!done) {
                try {
                    nextPacket = new PacketType(dataInputStream);
                } catch(EOFException e) {
                    System.err.println("End of file reached before Event Trailer Packet was read.");
                    System.err.println("The file likely contains an incomplete seismogram.");
                    System.err.println("Local seismogram creation was not disturbed.");
                    done = true;
                }
            }
        }
        return (LocalSeismogramImpl[])seismogramList.toArray(new LocalSeismogramImpl[seismogramList.size()]);
    }

    private boolean seismogramIsContinuos(PacketType seismogramData,
                                          PacketType dataPacket) {
        boolean value = false;
        double tolerance = 1.1;
        TimeInterval sampleGapWithTolerance = new TimeInterval(((double)1 / (double)seismogramData.sample_rate)
                                                                       * tolerance,
                                                               UnitImpl.SECOND);
//         System.out.println("---------------------------- sampleGap: " +
//         sampleGapWithTolerance.toString());
//        System.out.println("------------------------------- seismogramData.end_time_of_last_packet: "
//                + seismogramData.end_time_of_last_packet.toString());
//        System.out.println("-------------------------------- dataPacket.begin_time_of_first_packet: "
//                + dataPacket.begin_time_of_first_packet.toString());
        if(seismogramData.end_time_of_last_packet.difference(dataPacket.begin_time_of_first_packet)
                .lessThan(sampleGapWithTolerance)) {
            value = true;
        }
        return value;
    }

    private LocalSeismogramImpl makeSeismogram(PacketType seismogramData, boolean breakInData) {
        if(breakInData){
            System.out.println("The data collecting unit stopped recording data for a period of time longer than allowed.");
            System.out.println("A new seismogram will be created to hold the rest of the data.");
//        System.out.println("      Number of samples for channel "
//                + seismogramData.channel_number + ": "
//                + seismogramData.number_of_samples);
//        System.out.println("      Begin time of seismogram for channel "
//                + seismogramData.channel_number + ": "
//                + seismogramData.begin_time_of_seismogram);
        }
        Time beginTime = seismogramData.begin_time_of_seismogram.getFissuresTime();
        int numPoints = seismogramData.number_of_samples;
        SamplingImpl sampling = new SamplingImpl(seismogramData.sample_rate,
                                                 new TimeInterval(1,
                                                                  UnitImpl.SECOND));
        ChannelId channelId;
        if(props == null || conn == null) {
            channelId = new ChannelId(MockNetworkId.createNetworkID(),
                                      seismogramData.unitIdNumber,
                                      "" + seismogramData.data_stream_number,
                                      "BH" + seismogramData.channel_number,
                                      beginTime);
        } else {
            Channel channel = createChannel(seismogramData, sampling);
            try {
                JDBCChannel jdbcChannel = new JDBCChannel(conn);
                jdbcChannel.put(channel);
            } catch(SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            channelId = channel.get_id();
        }
        String id = channelId.toString();
        TimeSeriesDataSel timeSeriesDataSel = new TimeSeriesDataSel();
        System.out.println("encoded_data.length : " + seismogramData.encoded_data.length);
        timeSeriesDataSel.encoded_values(seismogramData.encoded_data);
        return new LocalSeismogramImpl(id,
                                       beginTime,
                                       numPoints,
                                       sampling,
                                       UnitImpl.COUNT,
                                       channelId,
                                       timeSeriesDataSel);
    }

    private void resetSeismogramData(PacketType seismogramData,
                                     PacketType nextPacket) {
        seismogramData.begin_time_of_seismogram = nextPacket.time;
        seismogramData.end_time_of_last_packet = nextPacket.time;
        seismogramData.number_of_samples = 0;
        seismogramData.encoded_data = new EncodedData[0];
    }

    private void append(Map seismogramData,
                        Integer i,
                        PacketType nextPacket,
                        List seismogramList) {
        if(seismogramIsContinuos((PacketType)seismogramData.get(i), nextPacket)) {
            seismogramData.put(i,
                               Append.appendDataPacket((PacketType)seismogramData.get(i),
                                                       nextPacket));
        } else {
            seismogramList.add(makeSeismogram((PacketType)seismogramData.get(i), true));
            resetSeismogramData((PacketType)seismogramData.get(i), nextPacket);
            append(seismogramData, i, nextPacket, seismogramList);
        }
    }

    private Channel createChannel(PacketType seismogramData, Sampling sampling) {
        String stationCode = props.getProperty(STATION_NAME + "."
                + seismogramData.unitIdNumber);
        String networkIdString = props.getProperty(NETWORK_ID);
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        MicroSecondDate msd = null;
        try {
            msd = new MicroSecondDate(simpleDate.parse(props.getProperty(NETWORK_BEGIN_TIME)));
        } catch(ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Time networkBeginTime = msd.getFissuresTime();
        NetworkId networkId = new NetworkId(networkIdString, networkBeginTime);
        ChannelId channelId = new ChannelId(networkId, stationCode, "00", "BH"
                + seismogramData.channel_number, networkBeginTime);
        TimeRange effectiveTime = new TimeRange(networkBeginTime,
                                                TimeUtils.timeUnknown);
        SiteId siteId = new SiteId(networkId,
                                   stationCode,
                                   "00",
                                   networkBeginTime);
        StationId stationId = new StationId(networkId,
                                            stationCode,
                                            networkBeginTime);
        QuantityImpl elevation = new QuantityImpl(0, UnitImpl.METER);
        QuantityImpl depth = elevation;
        Location location = new Location(0,
                                         0,
                                         elevation,
                                         depth,
                                         LocationType.from_int(0));
        NetworkAttrImpl networkAttr = new NetworkAttrImpl(networkId,
                                                          "",
                                                          "",
                                                          "",
                                                          effectiveTime);
        StationImpl station = new StationImpl(stationId,
                                              "",
                                              location,
                                              effectiveTime,
                                              "",
                                              "",
                                              "",
                                              networkAttr);
        SiteImpl site = new SiteImpl(siteId,
                                     location,
                                     effectiveTime,
                                     station,
                                     "");
        Channel channel = new ChannelImpl(channelId,
                                          "",
                                          new Orientation(0, -90),
                                          sampling,
                                          effectiveTime,
                                          site);
        return channel;
    }

    private final String NETWORK_ID = "network.networkId";

    private final String STATION_NAME = "station.stationName";

    private final String NETWORK_BEGIN_TIME = "network.beginTime";

    private Connection conn;

    private Properties props;

    private DataInput dataInputStream;
}
