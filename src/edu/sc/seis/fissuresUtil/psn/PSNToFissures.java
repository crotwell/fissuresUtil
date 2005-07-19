/**
 * PSNToFissures.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.psn;

import java.sql.Timestamp;
import java.util.GregorianCalendar;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.FlinnEngdahlType;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.FlinnEngdahlRegionImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.seisFile.psn.*;

public class PSNToFissures{

    public static LocalSeismogramImpl[] getSeismograms(PSNDataFile psnData) throws FissuresException{
        PSNEventRecord[] evRecs = psnData.getEventRecords();
        LocalSeismogramImpl[] seismos = new LocalSeismogramImpl[evRecs.length];
        for (int i = 0; i < evRecs.length; i++) {
            seismos[i] = getSeismogram(evRecs[i]);
        }
        return seismos;
    }

    public static LocalSeismogramImpl getSeismogram(PSNEventRecord evRec) throws FissuresException{
        MicroSecondDate beginTime = microSecondDateFromPSN(evRec.getFixedHeader().getDateTime());
        Time time = beginTime.getFissuresTime();

        TimeSeriesDataSel data = new TimeSeriesDataSel();
        if (evRec.isSampleDataShort()){
            data.sht_values(evRec.getSampleDataShort());
        }
        else if(evRec.isSampleDataInt()){
            data.int_values(evRec.getSampleDataInt());
        }
        else if(evRec.isSampleDataFloat()){
            data.flt_values(evRec.getSampleDataFloat());
        }
        else if(evRec.isSampleDataDouble()){
            data.dbl_values(evRec.getSampleDataDouble());
        }
        else throw new FissuresException();

        ChannelId channelId = getChannelId(evRec);

        String eventName = "   ";
        if (evRec.getVariableHeader().hasComment()){
            eventName = evRec.getVariableHeader().getComment();
        }

        String seisId = channelId.network_id.network_code + ":" +
            channelId.station_code + ":" +
            channelId.site_code + ":" +
            channelId.channel_code + ":" +
            time.date_time;

        LocalSeismogramImpl lsi = new LocalSeismogramImpl(seisId,
                                                          time,
                                                          evRec.getFixedHeader().getSampleCount(),
                                                          new SamplingImpl(1,
                                                                           new TimeInterval(1.0/evRec.getFixedHeader().getSampleRate(),
                                                                                            UnitImpl.SECOND)
                                                                          ),
                                                          UnitImpl.COUNT,
                                                          channelId,
                                                          data);
        return lsi;
    }

    public static MicroSecondDate microSecondDateFromPSN(PSNDateTime time){
        GregorianCalendar gc = new GregorianCalendar((int)time.getYear(),
                                                         (int)time.getMonth() - 1,
                                                         (int)time.getDay(),
                                                         (int)time.getHour(),
                                                         (int)time.getMinute(),
                                                         (int)time.getSecond());
        Timestamp timeStamp = new Timestamp(gc.getTimeInMillis());
        timeStamp.setNanos(time.getNanosec());
        MicroSecondDate msd = new MicroSecondDate(timeStamp);
        return msd;
    }

    public static ChannelId getChannelId(PSNEventRecord evRec){
        return getChannelId(evRec, "  ");
    }

    public static ChannelId getChannelId(PSNEventRecord evRec, String siteCode){
        MicroSecondDate msd = microSecondDateFromPSN(evRec.getFixedHeader().getDateTime());

        PSNHeader header = evRec.getFixedHeader();

        String netCode = header.getSensorNetwork();
        String stationCode = header.getSensorName();
        String channelCode = header.getChannelId();
        if (channelCode.length() == 5){
            siteCode = channelCode.substring(0,2);
            channelCode = channelCode.substring(2,5);
        }
        Time begin = msd.getFissuresTime();

        NetworkId netId = new NetworkId(netCode, begin);
        ChannelId id = new ChannelId(netId,
                                     stationCode,
                                     siteCode,
                                     channelCode,
                                     begin);
        return id;
    }

    public static Channel getChannel(PSNEventRecord evRec){
        ChannelId channelId = getChannelId(evRec);
        PSNHeader header = evRec.getFixedHeader();

        float stationElevation = (float)header.getSensorElevation();
        if (stationElevation == -12345.0f){
            stationElevation = 0;
        }

        Location loc = new Location((float)header.getSensorLat(),
                                        (float)header.getSensorLong(),
                                    new QuantityImpl(stationElevation, UnitImpl.METER),
                                    new QuantityImpl(0, UnitImpl.METER),
                                    LocationType.GEOGRAPHIC);

        Orientation orient = new Orientation((float)header.getCompAz(), (float)(header.getCompIncident() - 90));
        SamplingImpl samp = new SamplingImpl(1, new TimeInterval((1/header.getSampleRate()), UnitImpl.SECOND));
        TimeRange effective = new TimeRange(channelId.network_id.begin_time,
                                            new Time(edu.iris.Fissures.TIME_UNKNOWN.value, 0));
        NetworkAttr netAttr = new NetworkAttrImpl(channelId.network_id,
                                                  channelId.network_id.network_code,
                                                  "",
                                                  "",
                                                  effective);
        StationId stationId = new StationId(channelId.network_id,
                                            channelId.station_code,
                                            channelId.network_id.begin_time);
        Station station = new StationImpl(stationId,
                                          channelId.station_code,
                                          loc,
                                          effective,
                                          "",
                                          "",
                                          "from psn",
                                          netAttr);
        SiteId siteId = new SiteId(channelId.network_id,
                                   channelId.station_code,
                                   channelId.site_code,
                                   channelId.network_id.begin_time);
        Site site = new SiteImpl(siteId,
                                 loc,
                                 effective,
                                 station,
                                 "from psn");
        return new ChannelImpl(channelId,
                               channelId.channel_code,
                               orient,
                               samp,
                               effective,
                               site);

    }

    public static EventAccessOperations getEvent(PSNDataFile psnData){
        int numRecs = psnData.getEventRecords().length;
        for (int i = 0; (i < numRecs); i++) {
            PSNVariableHeader varHeader = psnData.getEventRecords()[i].getVariableHeader();
            if (varHeader.hasEventInfo()){
                PSNEventInfo evInfo = varHeader.getEventInfo()[0];
                MicroSecondDate originTime = microSecondDateFromPSN(evInfo.getTime());

                EventAttr attr = new EventAttrImpl("PSN Event");
                attr.region = new FlinnEngdahlRegionImpl(FlinnEngdahlType.GEOGRAPHIC_REGION, 0);
                Origin[] origins = new Origin[1];
                Location loc;

                loc = new Location((float)evInfo.getLat(),
                                       (float)evInfo.getLon(),
                                   new QuantityImpl(0, UnitImpl.METER),
                                   new QuantityImpl(evInfo.getDepthKM(), UnitImpl.KILOMETER),
                                   LocationType.GEOGRAPHIC);

                origins[0] = new OriginImpl("genid:" +
                                                Math.round(Math.random()*Integer.MAX_VALUE),
                                            "",
                                            "",
                                            originTime.getFissuresTime(),
                                            loc,
                                            new Magnitude[0],
                                            new ParameterRef[0]);
                EventAccessOperations evo = new CacheEvent(attr, origins, origins[0]);
                return evo;
            }
        }
        return null;
    }

}

