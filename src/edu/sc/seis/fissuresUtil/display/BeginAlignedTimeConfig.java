package edu.sc.seis.fissuresUtil.display;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * BeginAlignedTimeConfig synchronizes all the seismograms it holds around their initial times. It gets the first added 
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
    public BeginAlignedTimeConfig(DataSetSeismogram[] seismos){
		add(seismos);
    }
    
    /**
	 * <code>add</code> adds a seismogram to the config
	 *
	 * @param seismo the seismogram to be added
	 */
    public void add(DataSetSeismogram[] seismos){
		super.add(seismos);
		if(interval == null){
			interval = getInterval(seismos[0]);
		}
		for(int i = 0; i < seismos.length; i++){
			if(!contains(seismos[i])){
				MicroSecondTimeRange current = MicroSecondTimeRange.createTimeRangeFromFilter(seismos[i].getRequestFilter());
			}
		}
		fireTimeEvent();
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
		return super.fireTimeEvent(new TimeEvent(seismos, times));
	}
	
	private TimeInterval interval;
	
}// BeginAlignedTimeConfig
