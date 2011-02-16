package edu.sc.seis.fissuresUtil.display.registrar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
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

public class BasicTimeConfig implements TimeConfig{
    public BasicTimeConfig(){}

    /**
     * Creates a new <code>BasicTimeConfig</code> instance.  The display interval is initialized to be the same as the seismogram
     * being passed
     * @param seismo the initial seismogram
     */
    public BasicTimeConfig(DataSetSeismogram[] seismos){
        add(seismos);
    }

    /**
     * <code>add</code> adds a seismogram to the config
     *
     * @param seismo the seismogram to be added
     */
    public void add(DataSetSeismogram[] seismos){
        for(int i = 0; i < seismos.length; i++){
            if(!contains(seismos[i])){
                MicroSecondTimeRange seisTime = getInitialTime(seismos[i]);
                if(seisTime != null){
                    if(time == null){
                        time = seisTime;
                        initialTime = time;
                    }
                    seismoTimes.put(seismos[i], getInitialTime(seismos[i]));
                }
            }
        }
        seismograms = null;
        fireTimeEvent();
    }

    /*this method is used in the addition of seismograms to determine the initial
     *time
     */
    protected MicroSecondTimeRange getInitialTime(DataSetSeismogram seis){
        if(time == null){
            if(seis != null){
                time = new MicroSecondTimeRange(seis.getRequestFilter());
                initialTime = time;
            }
        }
        return time;
    }

    /**
     * <code>remove</code> removes a seismogram from this object
     *
     * @param seismo the seismogram to be removed
     */
    public void remove(DataSetSeismogram[] seismos){
        boolean someRemoved = false;
        for(int i = 0; i < seismos.length; i++){
            if(seismoTimes.remove(seismos[i]) != null){
                someRemoved = true;
            }
        }
        if(someRemoved){
            seismograms = null;
            if(seismoTimes.size() == 0){
                time = null;
                return;
            }
            fireTimeEvent();
        }
    }

    public void clear(){
        remove(getSeismograms());
    }

    /**
     * <code>contains</code> checks the receptacle for the presence of seismo
     *
     * @param seismo the seismogram whose presence is to be tested
     * @return true if the receptacle contains seismo, false otherwise
     */
    public boolean contains(DataSetSeismogram seismo){
        if(seismoTimes.containsKey(seismo)){
            return true;
        }
        return false;
    }

    public DataSetSeismogram[] getSeismograms(){
        if(seismograms == null){
            seismograms = (DataSetSeismogram[])seismoTimes.keySet().toArray(new DataSetSeismogram[seismoTimes.size()]);
        }
        return seismograms;
    }

    /**
     * <code>reset</code> causes the interval of the config to be set to the total time interval of one of the seismograms
     * and the rest of the seismograms to be set as if they were just added based on that interval
     */
    public void reset(){
        time = null;
        shift = 0;
        scale = 1;
        reset(getSeismograms());
    }

    /**
     * <code>reset</code> causes this seismogram to be set around the current shift and interval
     *
     * @param seismo a <code>DataSetSeismogram</code> to be reset
     */
    public void reset(DataSetSeismogram[] seismos){
        remove(seismos);
        add(seismos);
    }

    public void shaleTime(double shift, double scale){
        shaleTime(shift, scale, getSeismograms());
    }

    public void shaleTime(double shift, double scale, DataSetSeismogram[] seismos){
        this.shift += shift * this.scale;
        this.scale *= scale;
        if(time != null){
            time = time.shale(shift, scale);
            for(int i = 0; i < seismos.length; i++){
                MicroSecondTimeRange seisTime = (MicroSecondTimeRange)seismoTimes.get(seismos[i]);
                seismoTimes.put(seismos[i], seisTime.shale(shift, scale));
            }
            fireTimeEvent();
        }
    }

    public double getShift() {
        return shift;
    }

    public double getScale() {
        return scale;
    }


    public TimeEvent fireTimeEvent(){
        if(seismoTimes.size() == 0 && time != null){
            return fireTimeEvent(new EmptyTimeEvent(time));
        }
        DataSetSeismogram[] seismos = getSeismograms();
        MicroSecondTimeRange[] times = new MicroSecondTimeRange[seismos.length];
        for(int i = 0; i < seismos.length; i++){
            times[i] = (MicroSecondTimeRange)seismoTimes.get(seismos[i]);
        }
        return fireTimeEvent(new TimeEvent(seismos, times, time));
    }

    protected TimeEvent fireTimeEvent(TimeEvent event){
        Iterator f = listeners.iterator();
        while(f.hasNext()){
            ((TimeListener)f.next()).updateTime(event);
        }
        return event;
    }

    /**
     * @return   a MicroSecondTimeRange that covers the current generic time
     * range of this TimeConfig
     *
     */
    public MicroSecondTimeRange getTime() {
        if(time != null){
            return time;
        }else{
            return DisplayUtils.ZERO_TIME;
        }
    }

    /**
     * @param    seis                a  DataSetSeismogram a time is desired for
     *
     * @return   a MicroSecondTimeRange describing the current time of the given
     * seismogram in the time config
     *
     */
    public MicroSecondTimeRange getTime(DataSetSeismogram seis) {
        return (MicroSecondTimeRange)seismoTimes.get(seis);
    }

    public void addListener(TimeListener listener){
        if(listener != null){
            listeners.add(listener);
            fireTimeEvent();
        }
    }

    public void removeListener(TimeListener listener){
        listeners.remove(listener);
    }

    protected TimeInterval getInterval(DataSetSeismogram seismo){
        return new MicroSecondTimeRange(seismo.getRequestFilter()).getInterval();
    }

    /**
     * <code>listeners</code> contains all of the listeners registered with this config
     */
    private Set listeners = new HashSet();

    /**
     * <code>seismoTimes</code> contains all of the seismograms held by this config with their current TimeRange
     *
     */
    protected Map seismoTimes = new HashMap();

    protected DataSetSeismogram[] seismograms;

    protected double shift;

    protected double scale = 1;

    protected MicroSecondTimeRange time, initialTime;

    private static Logger logger = LoggerFactory.getLogger(BasicTimeConfig.class.getName());
}// BasicTimeConfig
