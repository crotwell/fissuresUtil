package edu.sc.seis.fissuresUtil.sac;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.IfSeismogramDC.SeismogramAttr;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.ISOTime;
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
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

/**
 * SacToFissures.java Created: Thu Mar 2 13:48:26 2000
 * 
 * @author Philip Crotwell
 * @version
 */
public class SacToFissures {

    public SacToFissures() {}
    
    public static LocalSeismogramImpl getSeismogram(File sacFile) throws FileNotFoundException, IOException, FissuresException {
        SacTimeSeries sac = new SacTimeSeries();
        sac.read(sacFile);
        return getSeismogram(sac);
    }
    
    public static LocalSeismogramImpl getSeismogram(InputStream in) throws IOException, FissuresException {
        DataInputStream dis;
        if (in instanceof DataInputStream) {
            dis = (DataInputStream)in;
        } else {
            dis = new DataInputStream(in);
        }
        SacTimeSeries sac = new SacTimeSeries();
        sac.read(dis);
        return getSeismogram(sac);
    }

    /**
     * Gets a LocalSeismogram. The data comes from the sac file, while the
     * SeismogramAttr comes from attr. A check is made on the beginTime,
     * numPoints and sampling and the sac file is considered correct for these
     * three.
     */
    public static LocalSeismogramImpl getSeismogram(SacTimeSeries sac,
                                                    SeismogramAttr attr)
            throws FissuresException {
        LocalSeismogramImpl seis = new LocalSeismogramImpl(attr, sac.y);
        if(seis.getNumPoints() != sac.npts) {
            seis.num_points = sac.npts;
        } // end of if (seis.getNumPoints() != sac.npts)
        Sampling samp = seis.getSampling();
        TimeInterval period = ((SamplingImpl)samp).getPeriod();
        if(sac.delta != 0) {
            double error = (period.convertTo(UnitImpl.SECOND).getValue() - sac.delta)
                    / sac.delta;
            if(error > 0.01) {
                seis.sampling_info = new SamplingImpl(1,
                                                      new TimeInterval(sac.delta,
                                                                       UnitImpl.SECOND));
            } // end of if (error > 0.01)
        } // end of if (samp.getPeriod().getValue() != sac.delta)
        if(sac.b != -12345) {
            MicroSecondDate beginTime = getSeismogramBeginTime(sac);
            double error = seis.getBeginTime()
                    .subtract(beginTime)
                    .divideBy(period)
                    .getValue();
            if(Math.abs(error) > 0.01) {
                seis.begin_time = beginTime.getFissuresTime();
            } // end of if (error > 0.01)
        } // end of if (sac.b != -12345)
        return seis;
    }

    public static LocalSeismogramImpl getSeismogram(SacTimeSeries sac)
            throws FissuresException {
        TimeSeriesDataSel data = new TimeSeriesDataSel();
        data.flt_values(sac.y);
        return new LocalSeismogramImpl(getSeismogramAttr(sac), data);
    }
    
    public static SeismogramAttrImpl getSeismogramAttr(SacTimeSeries sac)
    throws FissuresException {
        MicroSecondDate beginTime = getSeismogramBeginTime(sac);
        edu.iris.Fissures.Time time = beginTime.getFissuresTime();
        ChannelId chanId = getChannelId(sac);
        String evtName = "   ";
        if(!sac.kevnm.equals(sac.STRING16_UNDEF)) {
            evtName += sac.kevnm.trim() + " ";
        }
        if(sac.evla != sac.FLOAT_UNDEF && sac.evlo != sac.FLOAT_UNDEF
                && sac.evdp != sac.FLOAT_UNDEF) {
            evtName += "lat: " + sac.evla + " lon: " + sac.evlo + " depth: "
                    + (sac.evdp / 1000) + " km";
        }
        if(sac.gcarc != sac.FLOAT_UNDEF) {
            DecimalFormat df = new DecimalFormat("##0.#");
            evtName += "  " + df.format(sac.gcarc) + " deg.";
        }
        if(sac.az != sac.FLOAT_UNDEF) {
            DecimalFormat df = new DecimalFormat("##0.#");
            evtName += "  az " + df.format(sac.az) + " deg.";
        }
        // seis id can be anything, so set to net:sta:site:chan:begin
        String seisId = chanId.network_id.network_code + ":"
                + chanId.station_code + ":" + chanId.site_code + ":"
                + chanId.channel_code + ":" + time.date_time;
        return new SeismogramAttrImpl(seisId,
                                       time,
                                       sac.npts,
                                       new SamplingImpl(1,
                                                        new TimeInterval(sac.delta,
                                                                         UnitImpl.SECOND)),
                                       UnitImpl.COUNT,
                                       chanId);
    }

    public static ChannelId getChannelId(SacTimeSeries sac) {
        if(!sac.khole.equals(sac.STRING8_UNDEF)
                && sac.khole.trim().length() == 2) { return getChannelId(sac,
                                                                         sac.khole.trim()); }
        return getChannelId(sac, "  ");
    }

    public static ChannelId getChannelId(SacTimeSeries sac, String siteCode) {
        MicroSecondDate nzTime = getNZTime(sac);
        Time fisTime = nzTime.getFissuresTime();
        String netCode = "XX";
        if(!sac.knetwk.trim().equals("-12345")) {
            netCode = sac.knetwk.trim().toUpperCase();
        }
        String staCode = "XXXXX";
        if(!sac.kstnm.trim().equals("-12345")) {
            staCode = sac.kstnm.trim().toUpperCase();
        }
        String chanCode = "XXX";
        if(!sac.kcmpnm.trim().equals("-12345")) {
            chanCode = sac.kcmpnm.trim().toUpperCase();
            if(chanCode.length() == 5) {
                // site code is first 2 chars of kcmpnm
                siteCode = chanCode.substring(0, 2);
                chanCode = chanCode.substring(2, 5);
            }
        }
        NetworkId netId = new NetworkId(netCode, fisTime);
        ChannelId id = new ChannelId(netId,
                                     staCode,
                                     siteCode,
                                     chanCode,
                                     fisTime);
        return id;
    }

    public static ChannelImpl getChannel(SacTimeSeries sac) {
        ChannelId chanId = getChannelId(sac);
        float stel = sac.stel;
        if(stel == -12345.0f) {
            stel = 0;
        } // end of if (stel == -12345.0f)
        float stdp = sac.stdp;
        if(stdp == -12345.0f) {
            stdp = 0;
        } // end of if (stdp == -12345.0f)
        Location loc = new Location(sac.stla,
                                    sac.stlo,
                                    new QuantityImpl(sac.stel, UnitImpl.METER),
                                    new QuantityImpl(sac.stdp, UnitImpl.METER),
                                    LocationType.GEOGRAPHIC);
        Orientation orient = new Orientation(sac.cmpaz, sac.cmpinc - 90);
        SamplingImpl samp = new SamplingImpl(1,
                                             new TimeInterval(sac.delta,
                                                              UnitImpl.SECOND));
        TimeRange effective = new TimeRange(chanId.network_id.begin_time,
                                            new Time(edu.iris.Fissures.TIME_UNKNOWN.value,
                                                     0));
        NetworkAttr netAttr = new NetworkAttrImpl(chanId.network_id,
                                                  chanId.network_id.network_code,
                                                  "",
                                                  "",
                                                  effective);
        StationId staId = new StationId(chanId.network_id,
                                        chanId.station_code,
                                        chanId.network_id.begin_time);
        Station station = new StationImpl(staId,
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
        Site site = new SiteImpl(siteId, loc, effective, station, "from sac");
        return new ChannelImpl(chanId,
                               chanId.channel_code,
                               orient,
                               samp,
                               effective,
                               site);
    }

    /**
     * calculates the reference (NZ) time from the sac headers NZYEAR, NZJDAY,
     * NZHOUR, NZMIN, NZSEC, NZMSEC. If any of these are UNDEF (-12345), then ClockUtil.wayPast
     */
    public static MicroSecondDate getNZTime(SacTimeSeries sac) {
        if (sac.nzyear == SacTimeSeries.INT_UNDEF ||
            sac.nzjday == SacTimeSeries.INT_UNDEF ||
            sac.nzhour == SacTimeSeries.INT_UNDEF ||
            sac.nzmin == SacTimeSeries.INT_UNDEF ||
            sac.nzsec == SacTimeSeries.INT_UNDEF ||
            sac.nzmsec == SacTimeSeries.INT_UNDEF) {
            return ClockUtil.wayPast();
        }
        ISOTime isoTime = new ISOTime(sac.nzyear,
                                      sac.nzjday,
                                      sac.nzhour,
                                      sac.nzmin,
                                      sac.nzsec + sac.nzmsec / 1000f);
        MicroSecondDate originTime = isoTime.getDate();
        return originTime;
    }

    /**
     * calculates the event origin time from the sac headers O, NZYEAR, NZJDAY,
     * NZHOUR, NZMIN, NZSEC, NZMSEC.
     */
    public static MicroSecondDate getEventOriginTime(SacTimeSeries sac) {
        MicroSecondDate originTime = getNZTime(sac);
        TimeInterval sacOMarker = new TimeInterval(sac.o, UnitImpl.SECOND);
        originTime = originTime.add(sacOMarker);
        return originTime;
    }

    /**
     * calculates the seismogram begin time from the sac headers B, NZYEAR,
     * NZJDAY, NZHOUR, NZMIN, NZSEC, NZMSEC.
     */
    public static MicroSecondDate getSeismogramBeginTime(SacTimeSeries sac) {
        MicroSecondDate bTime = getNZTime(sac);
        TimeInterval sacBMarker = new TimeInterval(sac.b, UnitImpl.SECOND);
        bTime = bTime.add(sacBMarker);
        return bTime;
    }

    public static CacheEvent getEvent(SacTimeSeries sac) {
        if(sac.o != sac.FLOAT_UNDEF && sac.evla != sac.FLOAT_UNDEF
                && sac.evlo != sac.FLOAT_UNDEF && sac.evdp != sac.FLOAT_UNDEF) {
            MicroSecondDate beginTime = getEventOriginTime(sac);
            EventAttr attr = new EventAttrImpl("SAC Event");
            OriginImpl[] origins = new OriginImpl[1];
            Location loc;
            if(sac.evdp > 1000) {
                loc = new Location(sac.evla,
                                   sac.evlo,
                                   new QuantityImpl(0, UnitImpl.METER),
                                   new QuantityImpl(sac.evdp, UnitImpl.METER),
                                   LocationType.GEOGRAPHIC);
            } else {
                loc = new Location(sac.evla,
                                   sac.evlo,
                                   new QuantityImpl(0, UnitImpl.METER),
                                   new QuantityImpl(sac.evdp,
                                                    UnitImpl.KILOMETER),
                                   LocationType.GEOGRAPHIC);
            } // end of else
            origins[0] = new OriginImpl("genid:"
                                                + Math.round(Math.random()
                                                        * Integer.MAX_VALUE),
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
