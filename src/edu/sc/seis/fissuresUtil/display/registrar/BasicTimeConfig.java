package edu.sc.seis.fissuresUtil.display.registrar;
import edu.sc.seis.fissuresUtil.display.*;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Category;
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
    /**
     * Creates a new <code>BasicTimeConfig</code> instance.  The display interval is initialized to be the same as the seismogram
     * being passed
     * @param seismo the initial seismogram
     */
    public BasicTimeConfig(DataSetSeismogram[] seismos){
        if(seismos == null || DisplayUtils.allNull(seismos)){
            throw new IllegalArgumentException("Seismograms passed into a time config must not be null");
        }
        add(seismos);
    }

    /**
     * <code>add</code> adds a seismogram to the config
     *
     * @param seismo the seismogram to be added
     */
    public void add(DataSetSeismogram[] seismos){
        if(time == null){
            time = new MicroSecondTimeRange(seismos[0].getRequestFilter());
        }
        for(int i = 0; i < seismos.length; i++){
            if(!contains(seismos[i])){
                seismoTimes.put(seismos[i], time);
            }
        }
        seismograms = null;
        fireTimeEvent();
    }

    /**
     * <code>remove</code> removes a seismogram from this object
     *
     * @param seismo the seismogram to be removed
     */
    public boolean remove(DataSetSeismogram[] seismos){
        boolean allRemoved = true;
        for(int i = 0; i < seismos.length; i++){
            if(seismoTimes.containsKey(seismos[i])){
                seismoTimes.remove(seismos[i]);
            }else{
                allRemoved = false;
            }
        }
        seismograms = null;
        //fireTimeEvent();
        return allRemoved;
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
        time = time.shale(shift, scale);
        for(int i = 0; i < seismos.length; i++){
            seismoTimes.put(seismos[i], time);
        }
        fireTimeEvent();
    }

    public TimeEvent fireTimeEvent(){
        if(seismoTimes.size() == 0 && time != null){
            return fireTimeEvent(new EmptyTimeEvent(time));
        }
        DataSetSeismogram[] seismos = getSeismograms();
        MicroSecondTimeRange[] times = new MicroSecondTimeRange[seismos.length];
        for(int i = 0; i < seismos.length; i++){
            times[i] = time;
        }
        return fireTimeEvent(new TimeEvent(seismos, times));
    }

    protected TimeEvent fireTimeEvent(TimeEvent event){
        Iterator f = listeners.iterator();
        while(f.hasNext()){
            ((TimeListener)f.next()).updateTime(event);
        }
        return event;
    }

    public void addListener(TimeListener listener){
        listeners.add(listener);
        fireTimeEvent();
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
    private List listeners = new ArrayList();

    /**
     * <code>seismoTimes</code> contains all of the seismograms held by this config with their current TimeRange
     *
     */
    protected Map seismoTimes = new HashMap();

    private DataSetSeismogram[] seismograms;

    protected double shift;

    protected double scale = 1;

    protected MicroSecondTimeRange time;

    private static Category logger = Category.getInstance(BasicTimeConfig.class.getName());
}// BasicTimeConfig
