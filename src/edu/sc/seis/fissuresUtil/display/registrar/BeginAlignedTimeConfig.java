package edu.sc.seis.fissuresUtil.display.registrar;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * BasicTimeConfig synchronizes all the seismograms it holds around their initial times. It gets the first added
 * seismogram's time interval and uses it to initialize the display interval of subsequently added seismograms.  Any time shifts or
 * interval adjustments that occur are recorded so that added seismograms will be the same distance from their begin times and displayed
 *  over the same amount of time.
 *
 * Created: Thu Aug 29 11:00:31 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class BeginAlignedTimeConfig extends BasicTimeConfig{
    /**
     * Creates a new <code>BeginAlignedTimeConfig</code> instance.  The display interval is initialized to be the same as the seismogram
     * being passed
     * @param seismo the initial seismogram
     */
    public BeginAlignedTimeConfig(){
    }

    public BeginAlignedTimeConfig(DataSetSeismogram[] seismos){
        super(seismos);
    }

    protected MicroSecondTimeRange getInitialTime(DataSetSeismogram seis){
        if(interval == null){
            interval = getInterval(seis);
        }
        MicroSecondTimeRange current = new MicroSecondTimeRange(seis.getRequestFilter());
        QuantityImpl intervalPercentage = current.getInterval().convertTo(UnitImpl.MICROSECOND).divideBy(interval.convertTo(UnitImpl.MICROSECOND));
        current = current.shale(0, intervalPercentage.getValue());
        return current.shale(shift, scale);
    }

    public void shaleTime(double shift, double scale, DataSetSeismogram[] seismos){
        this.shift += shift * this.scale;
        this.scale *= scale;
        interval = (TimeInterval)interval.multiplyBy(scale);
        for(int i = 0; i < seismos.length; i++){
            seismoTimes.put(seismos[i], ((MicroSecondTimeRange)seismoTimes.get(seismos[i])).shale(shift, scale));
        }
        fireTimeEvent();
    }

    public TimeEvent fireTimeEvent(){
        DataSetSeismogram[] seismos = getSeismograms();
        MicroSecondTimeRange[] times = new MicroSecondTimeRange[seismos.length];
        for(int i = 0; i < seismos.length; i++){
            times[i] = (MicroSecondTimeRange)seismoTimes.get(seismos[i]);
        }
        MicroSecondTimeRange epoch = new MicroSecondTimeRange(new MicroSecondDate(ONE_WEEK),
                                                              interval);
        return super.fireTimeEvent(new RelativeTimeEvent(seismos, times, epoch.shift(shift)));
    }

    private long ONE_WEEK = 1000 * 60 * 60 * 24 * 7;

    private TimeInterval interval;
}// BeginAlignedTimeConfig
