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
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_Time;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class PhaseAlignedTimeConfig extends RelativeTimeConfig {

    public PhaseAlignedTimeConfig() {
        this("ttp");
    }

    public PhaseAlignedTimeConfig(String phaseName) {
        super();
        this.phaseName = phaseName;
    }

    public MicroSecondTimeRange getInitialTime(DataSetSeismogram seis) {
        DataSet ds = seis.getDataSet();
        Origin seisOrigin;
        EventAccessOperations eao = ds.getEvent();
        if(eao != null) {
            try {
                seisOrigin = eao.get_preferred_origin();
            } catch(NoPreferredOrigin e) {
                if(eao.get_origins().length > 0) {
                    seisOrigin = eao.get_origins()[0];
                } else {
                    return super.getInitialTime(seis);
                }
            }
            Channel chan = ds.getChannel(seis.getRequestFilter().channel_id);
            if(chan == null) { return super.getInitialTime(seis); }
            Location stationLoc = chan.my_site.my_station.my_location;
            MicroSecondDate phaseTime;
            try {
                phaseTime = calculate(seisOrigin, stationLoc);
            } catch(TauModelException e) {
                return super.getInitialTime(seis);
            }
            TimeInterval interval;
            if(initialTime != null) {
                interval = initialTime.getInterval();
            } else {
                MicroSecondDate seisEndTime = new MicroSecondDate(seis.getRequestFilter().end_time);
                interval = new TimeInterval(phaseTime, seisEndTime);
            }
            MicroSecondTimeRange originRange = new MicroSecondTimeRange(phaseTime,
                                                                        interval);
            return originRange.shale(shift, scale);
        }
        return super.getInitialTime(seis);
    }

    protected void shaleInitialTime() {
        // shift right by 10% so that P is not at left edge of screen
        shaleTime(-0.1, 1.1);
    }

    public synchronized MicroSecondDate calculate(Origin origin,
                                                  Location station)
            throws TauModelException {
        TauPUtil util = TauPUtil.getTauPUtil();
        Arrival[] arrivals = util.calcTravelTimes(station,
                                                  origin,
                                                  new String[] {phaseName});
        MicroSecondDate out = new MicroSecondDate(origin.origin_time);
        if(arrivals.length > 0) {
            out = out.add(new TimeInterval(arrivals[0].getTime(),
                                           UnitImpl.SECOND));
        }
        return out;
    }

    public synchronized String getPhaseName() {
        return phaseName;
    }

    public synchronized void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getTypeOfRelativity() {
        return "Time since the " + getPhaseName() + " arrival";
    }

    TauP_Time taup = null;

    String phaseName;
}