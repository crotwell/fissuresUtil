package edu.sc.seis.fissuresUtil.sac;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

/**
 * FissuresToSac.java
 *
 *
 * Created: Wed Apr 10 10:52:00 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class FissuresToSac {

    /**
     * Creates a SacTimeSeries object from a LocalSeismogram. Headers in the SAC
     * object are filled in as much as possible, with the notable exception of event
     * information and station location and channel orientation.
     *
     * @param seis the <code>LocalSeismogramImpl</code> with the data
     * @return a <code>SacTimeSeries</code> with data and headers filled
     */
    public static SacTimeSeries getSAC(LocalSeismogramImpl seis)
        throws CodecException {
        SacTimeSeries sac = new SacTimeSeries();
        float[] floatSamps;
        try {
            if (seis.can_convert_to_long()) {
                int[] idata = seis.get_as_longs();
                floatSamps = new float[idata.length];
                for (int i=0; i<idata.length; i++) {
                    floatSamps[i] = idata[i];
                }
            } else {
                floatSamps = seis.get_as_floats();
            } // end of else
        } catch (FissuresException e) {
            if (e.getCause() instanceof CodecException) {
                throw (CodecException)e.getCause();
            } else {
                throw new CodecException(e.the_error.error_description);
            }
        }
        sac.y = floatSamps;

        sac.npts = sac.y.length;
        sac.b = 0;
        SamplingImpl samp = (SamplingImpl)seis.sampling_info;
        QuantityImpl period = samp.getPeriod();
        period = period.convertTo(UnitImpl.SECOND);
        float f = (float)period.get_value();
        sac.e = sac.npts * f;
        sac.iftype = sac.ITIME;
        sac.leven = sac.TRUE;
        sac.delta = f;
        sac.idep = sac.IUNKN;

        UnitImpl yUnit = (UnitImpl)seis.y_unit;
        QuantityImpl min = (QuantityImpl)seis.getMinValue();
        sac.depmin = (float)min.convertTo(yUnit).value;
        QuantityImpl max = (QuantityImpl)seis.getMaxValue();
        sac.depmax = (float)max.convertTo(yUnit).value;
        QuantityImpl mean = (QuantityImpl)seis.getMeanValue();
        sac.depmen = (float)mean.convertTo(yUnit).value;

        GregorianCalendar cal =
            new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.setTime(new MicroSecondDate(seis.begin_time));
        sac.nzyear = cal.get(Calendar.YEAR);
        sac.nzjday = cal.get(Calendar.DAY_OF_YEAR);
        sac.nzhour = cal.get(Calendar.HOUR_OF_DAY);
        sac.nzmin = cal.get(Calendar.MINUTE);
        sac.nzsec = cal.get(Calendar.SECOND);
        sac.nzmsec = cal.get(Calendar.MILLISECOND);

        sac.knetwk = seis.channel_id.network_id.network_code;
        sac.kstnm = seis.channel_id.station_code;
        if ( ! seis.channel_id.site_code.equals("  ")) {
            sac.kcmpnm = seis.channel_id.site_code+seis.channel_id.channel_code;
        } else {
            sac.kcmpnm = seis.channel_id.channel_code;
        } // end of else
        sac.khole = seis.channel_id.site_code;

        return sac;
    }

    /**
     * Creates a SacTimeSeries object from a LocalSeismogram. Headers in the SAC
     * object are filled in as much as possible, with the notable exception of event
     * information.
     *
     * @param seis a <code>LocalSeismogramImpl</code> value
     * @param channel a <code>Channel</code> value
     * @return a <code>SacTimeSeries</code> value
     */
    public static SacTimeSeries getSAC(LocalSeismogramImpl seis,
                                       Channel channel)
        throws CodecException {
        SacTimeSeries sac = getSAC(seis);
        addChannel(sac, channel);
        return sac;
    }

    /**
     * Creates a SacTimeSeries object from a LocalSeismogram. Headers in the SAC
     * object are filled in as much as possible, with the notable exception of station
     * location and channel orientation information.
     *
     * @param seis a <code>LocalSeismogramImpl</code> value
     * @param origin an <code>Origin</code> value
     * @return a <code>SacTimeSeries</code> value
     */
    public static SacTimeSeries getSAC(LocalSeismogramImpl seis,
                                       Origin origin)
        throws CodecException {
        SacTimeSeries sac = getSAC(seis);
        addOrigin(sac, origin);
        return sac;
    }


    /**
     * Creates a SacTimeSeries object from a LocalSeismogram. Headers in the SAC
     * object are filled in as much as possible.
     *
     * @param seis a <code>LocalSeismogramImpl</code> value
     * @param channel a <code>Channel</code> value
     * @param origin an <code>Origin</code> value
     * @return a <code>SacTimeSeries</code> value
     */
    public static SacTimeSeries getSAC(LocalSeismogramImpl seis,
                                       Channel channel,
                                       Origin origin)
        throws CodecException {
        SacTimeSeries sac = getSAC(seis);
        addChannel(sac, channel);
        addOrigin(sac, origin);
        return sac;
    }

    /**
     * Adds the Channel information, including station location and channel
     * orientation to the sac object.
     *
     * @param sac a <code>SacTimeSeries</code> object to be modified
     * @param channel a <code>Channel</code>
     */
    public static void addChannel(SacTimeSeries sac, Channel channel) {
        sac.stla = (float)channel.my_site.my_location.latitude;
        sac.stlo = (float)channel.my_site.my_location.longitude;
        QuantityImpl z = (QuantityImpl)channel.my_site.my_location.elevation;
        sac.stel = (float)z.convertTo(UnitImpl.METER).value;
        z = (QuantityImpl)channel.my_site.my_location.depth;
        sac.stdp = (float)z.convertTo(UnitImpl.METER).value;

        sac.cmpaz = channel.an_orientation.azimuth;
        // sac vert. is 0, fissures and seed vert. is -90
        // sac hor. is 90, fissures and seed hor. is 0
        sac.cmpinc = 90+channel.an_orientation.dip;
    }

    /**
     * Adds origin informtion to the sac object, including the o marker.
     *
     * @param sac a <code>SacTimeSeries</code> object to be modified
     * @param origin an <code>Origin</code> value
     */
    public static void addOrigin(SacTimeSeries sac, Origin origin) {
        sac.evla = origin.my_location.latitude;
        sac.evlo = origin.my_location.longitude;
        QuantityImpl z = (QuantityImpl)origin.my_location.elevation;
        sac.evel = (float)z.convertTo(UnitImpl.METER).value;
        z = (QuantityImpl)origin.my_location.depth;
        sac.evdp = (float)z.convertTo(UnitImpl.METER).value;

        ISOTime isoTime = new ISOTime(sac.nzyear,
                                      sac.nzjday,
                                      sac.nzhour,
                                      sac.nzmin,
                                      sac.nzsec+sac.nzmsec/1000f);
        MicroSecondDate beginTime = isoTime.getDate();
        MicroSecondDate originTime = new MicroSecondDate(origin.origin_time);

        TimeInterval sacOMarker = (TimeInterval)originTime.subtract(beginTime);
        sacOMarker = (TimeInterval)sacOMarker.convertTo(UnitImpl.SECOND);
        sac.o = (float)sacOMarker.value;
    }

}// FissuresToSac
