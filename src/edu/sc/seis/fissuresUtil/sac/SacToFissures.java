
package edu.sc.seis.fissuresUtil.sac;

import java.text.DecimalFormat;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfParameterMgr.*;
import edu.iris.Fissures.IfTimeSeries.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;
//import edu.iris.Fissures.display.EventTemp;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;

/**
 * SacToFissures.java
 *
 *
 * Created: Thu Mar  2 13:48:26 2000
 *
 * @author Philip Crotwell
 * @version
 */

public class SacToFissures  {
    
    public SacToFissures() {
        
    }

    public static LocalSeismogramImpl getSeismogram(SacTimeSeries sac) 
        throws FissuresException {
        ISOTime isoTime = new ISOTime(sac.nzyear,
                                      sac.nzjday,
                                      sac.nzhour,
                                      sac.nzmin,
                                      sac.nzsec+sac.nzmsec/1000f);
        MicroSecondDate beginTime = isoTime.getDate();
        TimeInterval sacBMarker = new TimeInterval(sac.b, UnitImpl.SECOND);
        beginTime = beginTime.add(sacBMarker);
        edu.iris.Fissures.Time time = 
                 new edu.iris.Fissures.Time(
                     ISOTime.getISOString(beginTime), 
                     -1);
        TimeSeriesDataSel data = new TimeSeriesDataSel();
        data.flt_values(sac.y);

        ChannelId chanId = getChannelId(sac);

        String evtName = "   ";
        if ( ! sac.kevnm.equals(sac.STRING16_UNDEF)) {
            evtName+= sac.kevnm.trim()+" ";
        }
        if (sac.evla != sac.FLOAT_UNDEF &&
            sac.evlo != sac.FLOAT_UNDEF &&
            sac.evdp != sac.FLOAT_UNDEF) {
            evtName+= "lat: "+sac.evla+" lon: "+sac.evlo+" depth: "+(sac.evdp/1000)+" km";
        }
        if (sac.gcarc != sac.FLOAT_UNDEF) {
            DecimalFormat df = new DecimalFormat("##0.#");
            evtName += "  "+df.format(sac.gcarc)+" deg.";
        }
        if (sac.az != sac.FLOAT_UNDEF) {
            DecimalFormat df = new DecimalFormat("##0.#");
            evtName += "  az "+df.format(sac.az)+" deg.";            
        }
        // seis id can be anything, so set to net:sta:site:chan:begin
        String seisId = chanId.network_id.network_code+":"+
            chanId.station_code+":"+
            chanId.site_code+":"+
            chanId.channel_code+":"+
            time.date_time;
        return new LocalSeismogramImpl(seisId,
                                       time,
                                       sac.npts,
                                       new SamplingImpl(1,
                                                        new TimeInterval(sac.delta, UnitImpl.SECOND)),
                                       UnitImpl.COUNT,
                                       chanId,
                                       data);
    }

    public static ChannelId getChannelId(SacTimeSeries sac) {
        return getChannelId(sac, "  ");
    }

    public static ChannelId getChannelId(SacTimeSeries sac, 
                                         String siteCode) {
        String isoTime = ISOTime.getISOString(sac.nzyear,
                                              sac.nzjday,
                                              sac.nzhour,
                                              sac.nzmin,
                                              sac.nzsec+sac.nzmsec/1000f);
        String netCode = "XX";
        if ( ! sac.knetwk.trim().equals("-12345")) {
            netCode = sac.knetwk.trim();
        }
        String staCode = "XXXXX";
        if ( ! sac.kstnm.trim().equals("-12345")) {
            staCode = sac.kstnm.trim();
        }
        String chanCode = "XXX";
        if ( ! sac.kcmpnm.trim().equals("-12345")) {
            chanCode = sac.kcmpnm.trim();
        }
        Time begin = new edu.iris.Fissures.Time(isoTime, 
                                                -1);
        NetworkId netId = new NetworkId(netCode,
                                        begin);
        ChannelId id = new ChannelId(netId,
                                     staCode,
                                     siteCode,
                                     chanCode,
                                     begin);
        return id;
    }

    public static Channel getChannel(SacTimeSeries sac) {
        ChannelId chanId = getChannelId(sac);

        float stel = sac.stel;
        if (stel == -12345.0f) {
            stel = 0;
        } // end of if (stel == -12345.0f)
        float stdp = sac.stdp;
        if (stdp == -12345.0f) {
            stdp = 0;
        } // end of if (stdp == -12345.0f)
        
        Location loc = new Location(sac.stla,
                                    sac.stlo,
                                    new QuantityImpl(sac.stel, 
                                                     UnitImpl.METER),
                                    new QuantityImpl(sac.stdp, 
                                                     UnitImpl.METER),
                                    LocationType.GEOGRAPHIC);
        Orientation orient = new Orientation(sac.cmpaz, sac.cmpinc - 90);
                SamplingImpl samp = new SamplingImpl(1,
                                         new TimeInterval(sac.delta, 
                                                          UnitImpl.SECOND));
                TimeRange effective = new TimeRange(chanId.network_id.begin_time,
                                                    new Time(edu.iris.Fissures.TIME_UNKNOWN.value,
                                                             0));              
                
        NetworkAttr netAttr = 
            new NetworkAttrImpl(chanId.network_id, 
                                chanId.network_id.network_code,
                                "",
                                "",
                                effective);
        StationId staId = new StationId(chanId.network_id,
                                        chanId.station_code,
                                        chanId.network_id.begin_time);
        Station station = 
            new StationImpl(staId,
                            chanId.station_code,
                            loc,
                            effective,
                            "",
                            "",
                            "from sac",
                            netAttr);
        SiteId siteId = new SiteId(chanId.network_id,
                                   chanId.station_code,
                                   chanId.site_code,
                                   chanId.network_id.begin_time);
        Site site = new SiteImpl(siteId,
                                 loc,
                                 effective,
                                 station,
                                 "from sac");
        return new ChannelImpl(chanId,
                               chanId.channel_code,
                               orient,
                               samp,
                               effective,
                               site);
        
    }

    public static CacheEvent getEvent(SacTimeSeries sac) {
	if (sac.o != sac.FLOAT_UNDEF &&
            sac.evla != sac.FLOAT_UNDEF &&
            sac.evlo != sac.FLOAT_UNDEF &&
            sac.evdp != sac.FLOAT_UNDEF) {
	    
	    ISOTime isoTime = new ISOTime(sac.nzyear,
					  sac.nzjday,
					  sac.nzhour,
					  sac.nzmin,
					  sac.nzsec+sac.nzmsec/1000f);
	    MicroSecondDate beginTime = isoTime.getDate();
            System.out.println("SacToFissures "+beginTime);
	    TimeInterval sacOMarker = new TimeInterval(sac.o, UnitImpl.SECOND);
            beginTime = beginTime.add(sacOMarker);
            System.out.println("SacToFissuresB "+beginTime+"  "+sacOMarker);
            EventAttr attr = new EventAttrImpl("SAC Event");
            Origin[] origins = new Origin[1];
            Location loc;
	    if (sac.evdp > 1000) {
            loc = new Location(sac.evla, 
                               sac.evlo,
                               new QuantityImpl(0, UnitImpl.METER), 
                               new QuantityImpl(sac.evdp, UnitImpl.METER),
                               LocationType.GEOGRAPHIC);
        } else {
            loc = new Location(sac.evla, 
                               sac.evlo,
                               new QuantityImpl(0, UnitImpl.METER), 
                               new QuantityImpl(sac.evdp, UnitImpl.KILOMETER),
                               LocationType.GEOGRAPHIC);
        } // end of else
        origins[0] = new OriginImpl("genid:"+
                                   Math.round(Math.random()*Integer.MAX_VALUE),
                                    "",
                                    "",
                                    beginTime.getFissuresTime(),
                                    loc,
                                    new Magnitude[0],
                                    new ParameterRef[0]);
	    return new CacheEvent(attr, origins, origins[0]);
	} else {
	    return null;
	}
    }
    
} // SacToFissures
