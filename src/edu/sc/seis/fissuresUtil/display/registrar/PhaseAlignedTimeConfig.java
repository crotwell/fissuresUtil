/**
 * PhaseAlignedTimeConfig.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display.registrar;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_Time;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class PhaseAlignedTimeConfig extends RelativeTimeConfig{
    public MicroSecondTimeRange getInitialTime(DataSetSeismogram seis){
        DataSet ds = seis.getDataSet();
        Origin seisOrigin;
        EventAccessOperations eao = ds.getEvent();
        if(eao != null){
            try {
                seisOrigin = eao.get_preferred_origin();
            }catch (NoPreferredOrigin e) {
                if (eao.get_origins().length > 0) {
                    seisOrigin = eao.get_origins()[0];
                } else {
                    return super.getInitialTime(seis);
                }
            }
            Channel chan = ds.getChannel(seis.getRequestFilter().channel_id);
            if (chan == null) {
                return super.getInitialTime(seis);
            }
            Location stationLoc = chan.my_site.my_station.my_location;
            MicroSecondDate phaseTime;
            try {
                 phaseTime = calculate(seisOrigin, stationLoc);
            } catch (TauModelException e) {
                return super.getInitialTime(seis);
            }
            TimeInterval interval;
            if(initialTime != null){
                interval = initialTime.getInterval();
            }else{
                MicroSecondDate seisEndTime = new MicroSecondDate(seis.getRequestFilter().end_time);
                interval = new TimeInterval(phaseTime, seisEndTime);
            }
            // shift right by 10% so that P is not at left edge of screen
            TimeInterval tenthInterval = new TimeInterval(interval.get_value()*0.1, interval.getUnit());
            phaseTime = phaseTime.subtract(tenthInterval);
            interval = interval.add(tenthInterval);
            
            MicroSecondTimeRange originRange = new MicroSecondTimeRange(phaseTime, interval);
            return originRange.shale(shift, scale);
        }
        return super.getInitialTime(seis);
    }

    public synchronized MicroSecondDate calculate(Origin origin, Location station) throws TauModelException {
        QuantityImpl depth = (QuantityImpl)origin.my_location.depth;
        depth = depth.convertTo(UnitImpl.KILOMETER);
        taup.setSourceDepth(depth.getValue());
        double degrees = SphericalCoords.distance(station.latitude, station.longitude,
                                   origin.my_location.latitude, origin.my_location.longitude);
        taup.calculate(degrees);
        Arrival[] arrivals = taup.getArrivals();
        MicroSecondDate out =  new MicroSecondDate(origin.origin_time);
        if (arrivals.length > 0) {
            out = out.add(new TimeInterval(arrivals[0].getTime(), UnitImpl.SECOND));
        }
        return out;
    }

    /**
     * Sets Taup
     *
     * @param    Taup                a  TauP_Time
     */
    public synchronized  void setTauP(TauP_Time taup) {
        this.taup = taup;
    }

    /**
     * Returns Taup
     *
     * @return    a  TauP_Time
     */
    public synchronized TauP_Time getTauP() {
        return taup;
    }

     TauP_Time taup = null;
}

