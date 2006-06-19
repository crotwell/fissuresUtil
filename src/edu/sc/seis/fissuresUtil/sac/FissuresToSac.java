package edu.sc.seis.fissuresUtil.sac;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Filter;
import edu.iris.Fissures.IfNetwork.FilterType;
import edu.iris.Fissures.IfNetwork.PoleZeroFilter;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.IfNetwork.TransferType;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.cache.InstrumentationLoader;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.freq.Cmplx;
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
        sac.iztype = sac.IB;
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

        setKZTime(sac, new MicroSecondDate(seis.begin_time));
        
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
        if (channel != null) {
            addChannel(sac, channel);
        }
        if (origin != null) {
            addOrigin(sac, origin);
        }
        if (origin != null && channel != null) {
            DistAz distAz = new DistAz(channel, origin);
            sac.gcarc = (float)distAz.getDelta();
        }
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
        setKZTime(sac, originTime);
        TimeInterval sacBMarker = (TimeInterval)beginTime.subtract(originTime);
        sacBMarker = (TimeInterval)sacBMarker.convertTo(UnitImpl.SECOND);
        sac.b = (float)sacBMarker.value;
        sac.o = 0;
        if (origin.magnitudes.length > 0) {
            sac.mag = origin.magnitudes[0].value;
        }
    }
    
    public static void setKZTime(SacTimeSeries sac, MicroSecondDate date) {
        Calendar cal = ClockUtil.getGMTCalendar();
        cal.setTime(date);
        sac.nzyear = cal.get(Calendar.YEAR);
        sac.nzjday = cal.get(Calendar.DAY_OF_YEAR);
        sac.nzhour = cal.get(Calendar.HOUR_OF_DAY);
        sac.nzmin = cal.get(Calendar.MINUTE);
        sac.nzsec = cal.get(Calendar.SECOND);
        sac.nzmsec = cal.get(Calendar.MILLISECOND);
    }
    
    public static SacPoleZero getPoleZero(Response response) throws InvalidResponse {
        InstrumentationLoader.repairResponse(response);
        if ( ! InstrumentationLoader.isValid(response)) {
            throw new IllegalArgumentException("response is not valid");
        }
        Stage stage = response.stages[0];
        Filter filter = stage.filters[0];
        if(filter.discriminator().value() != FilterType._POLEZERO) {
            throw new IllegalArgumentException("Unexpected response type "
                    + filter.discriminator().value());
        }
        PoleZeroFilter pz = filter.pole_zero_filter();
        int gamma = 0;
        UnitImpl unit = (UnitImpl)stage.input_units;
        if(unit.isConvertableTo(UnitImpl.METER_PER_SECOND)) {
            gamma = 1;
        } else if(unit.isConvertableTo(UnitImpl.METER_PER_SECOND_PER_SECOND)) {
            gamma = 2;
        }
        int num_zeros = pz.zeros.length + gamma;
        double mulFactor = 1;
        if(stage.type == TransferType.ANALOG) {
            mulFactor = 2 * Math.PI;
        }
        Cmplx[] zeros = SacPoleZero.initCmplx(num_zeros);
        for(int i = 0; i < pz.zeros.length; i++) {
            zeros[i] = new Cmplx(pz.zeros[i].real * mulFactor,
                                 pz.zeros[i].imaginary * mulFactor);
        }
        Cmplx[] poles = SacPoleZero.initCmplx(pz.poles.length);
        for(int i = 0; i < pz.poles.length; i++) {
            poles[i] = new Cmplx(pz.poles[i].real * mulFactor,
                                 pz.poles[i].imaginary * mulFactor);
        }
        float constant = stage.the_normalization[0].ao_normalization_factor;
        double sd = response.the_sensitivity.sensitivity_factor;
        double fs = response.the_sensitivity.frequency;
        sd *= Math.pow(2 * Math.PI * fs, gamma);
        double A0 = stage.the_normalization[0].ao_normalization_factor;
        double fn = stage.the_normalization[0].normalization_freq;
        A0 = A0 / Math.pow(2 * Math.PI * fn, gamma);
        if(stage.type == TransferType.ANALOG) {
            A0 *= Math.pow(2 * Math.PI, pz.poles.length - pz.zeros.length);
        }
        if(poles.length == 0 && zeros.length == 0) {
            constant = (float)(sd * A0);
        } else {
            constant = (float)(sd * calc_A0(poles, zeros, fs));
        }
        return new SacPoleZero(poles, zeros, constant);
    }

    private static double calc_A0(Cmplx[] poles, Cmplx[] zeros, double ref_freq) {
        int i;
        Cmplx numer, denom, f0, hold;
        double a0;
        f0 = new Cmplx(0, 2 * Math.PI * ref_freq);
        hold = zeros[0];
        denom = Cmplx.sub(f0, hold);
        for(i = 1; i < zeros.length; i++) {
            hold = zeros[i];
            denom = Cmplx.mul(denom, Cmplx.sub(f0, hold));
        }
        hold = poles[0];
        numer = Cmplx.sub(f0, hold);
        for(i = 1; i < poles.length; i++) {
            hold = poles[i];
            numer = Cmplx.mul(numer, Cmplx.sub(f0, hold));
        }
        a0 = Cmplx.div(numer, denom).mag();
        return a0;
    }

}// FissuresToSac
