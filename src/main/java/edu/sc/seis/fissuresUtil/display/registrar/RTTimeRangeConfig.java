package edu.sc.seis.fissuresUtil.display.registrar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
/**
 * RTTimeRangeConfig.java
 *
 *
 * Created: Mon Jun  3 15:47:31 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class RTTimeRangeConfig implements TimeConfig, TimeListener{
    public RTTimeRangeConfig(TimeConfig internalConfig){
        this(internalConfig, DEFAULT_REFRESH);
    }

    public RTTimeRangeConfig(TimeConfig internalConfig, TimeInterval update){
        this(internalConfig, update, 1);
    }

    public RTTimeRangeConfig(TimeConfig internalConfig, TimeInterval update, float speed){
        this.update = update;
        this.speed = speed;
        setInternalConfig(internalConfig);
    }

    public void setInternalConfig(TimeConfig config){
        if(internalTimeConfig != null){
            internalTimeConfig.removeListener(this);
            add(config.getSeismograms());
        }
        internalTimeConfig = config;
        time = config.fireTimeEvent();
        config.addListener(this);
    }
    
    public TimeConfig getInternalConfig(){ return internalTimeConfig; }

    public void add(DataSetSeismogram[] seismos){
        internalTimeConfig.add(seismos);
    }

    public void updateTime(TimeEvent event) {
        time = event;
        synchronized(listeners){
            Iterator it = listeners.iterator();
            while(it.hasNext()){
                ((TimeListener)it.next()).updateTime(event);
            }
        }
    }

    public void startTimer() {
        if (timer == null) {
            timer =
                new Timer((int)update.convertTo(UnitImpl.MILLISECOND).value,
                          new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (speed != 0 && lastDate != null) {
                                MicroSecondDate now = ClockUtil.now();
                                TimeInterval timeInterval = new TimeInterval(lastDate, now);
                                lastDate = now;
                                shaleTime(timeInterval.divideBy(time.getTime().getInterval()).getValue() * speed, 1);
                                //logger.debug("Timer: updateTimeSyncListeners()  speed="+speed);
                            } else{
                                lastDate = ClockUtil.now();
                            }
                        }
                    });
            timer.setCoalesce(true);
            timer.start();
        }
    }

    public void stopTimer(){
        if(timer != null){
            timer.stop();
            timer = null;
        }
    }

    public void reset(){
        speed = 1;
        stopTimer();
        internalTimeConfig.reset();
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public void addListener(TimeListener listener) {
        synchronized(listeners){
            if(listener != null){
                listeners.add(listener);
            }
        }
    }

    public void removeListener(TimeListener listener) {
        synchronized(listeners){
            listeners.remove(listener);
        }
    }

    public void shaleTime(double shift, double scale, DataSetSeismogram[] seismos) {
        internalTimeConfig.shaleTime(shift, scale, seismos);
    }

    public void shaleTime(double shift, double scale) {
        internalTimeConfig.shaleTime(shift, scale);
    }

    public void remove(DataSetSeismogram[] seismos) {
        internalTimeConfig.remove(seismos);
    }

    public double getShift() {
        return internalTimeConfig.getShift();
    }

    public double getScale() {
        return internalTimeConfig.getScale();
    }


    public void reset(DataSetSeismogram[] seismos) {
        internalTimeConfig.reset(seismos);
    }

    public boolean contains(DataSetSeismogram seismo) {
        return internalTimeConfig.contains(seismo);
    }

    public TimeEvent fireTimeEvent() {
        return internalTimeConfig.fireTimeEvent();
    }


    /**
     * @return   a MicroSecondTimeRange that covers the current generic time
     * range of this TimeConfig
     *
     */
    public MicroSecondTimeRange getTime() {
        return internalTimeConfig.getTime();
    }

    /**
     * @param    seis                a  DataSetSeismogram a time is desired for
     *
     * @return   a MicroSecondTimeRange describing the current time of the given
     * seismogram in the time config
     *
     */
    public MicroSecondTimeRange getTime(DataSetSeismogram seis) {
        return internalTimeConfig.getTime();
    }

    public DataSetSeismogram[] getSeismograms() {
        return internalTimeConfig.getSeismograms();
    }

    public void clear() {
        internalTimeConfig.removeListener(this);
        internalTimeConfig.clear();
        internalTimeConfig.addListener(this);
    }

    private List listeners = Collections.synchronizedList(new ArrayList());

    private TimeConfig internalTimeConfig;

    private TimeInterval update;

    public static final TimeInterval serverTimeOffset =
        ClockUtil.getTimeOffset();

    private MicroSecondDate lastDate;

    private TimeEvent time;

    private float speed = 1;

    /** Timers are used for realTime update of the Seismograms **/
    protected Timer timer;

    protected TimeInterval width;

    private static Logger logger = LoggerFactory.getLogger(RTTimeRangeConfig.class);
    public static TimeInterval DEFAULT_REFRESH = new TimeInterval(.125, UnitImpl.SECOND);

    private static TimeInterval THREE_MINUTES = new TimeInterval(3, UnitImpl.MINUTE);

}// RTTimeRangeConfig
