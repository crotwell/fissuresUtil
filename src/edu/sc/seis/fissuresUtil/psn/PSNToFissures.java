/**
 * PSNToFissures.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.psn;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.psn.*;
import java.sql.Timestamp;

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

        return new LocalSeismogramImpl(seisId,
                                       time,
                                       evRec.getFixedHeader().getSampleCount(),
                                       new SamplingImpl(1,
                                                        new TimeInterval(1.0/evRec.getFixedHeader().getSampleRate(),
                                                                         UnitImpl.SECOND)
                                                       ),
                                       UnitImpl.COUNT,
                                       channelId,
                                       data);
    }

    public static MicroSecondDate microSecondDateFromPSN(PSNDateTime time){
        Timestamp timeStamp = new Timestamp(time.getYear(),
                                            time.getMonth(),
                                            time.getDay(),
                                            time.getHour(),
                                            time.getMinute(),
                                            time.getSecond(),
                                            time.getNanosec());
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

}

