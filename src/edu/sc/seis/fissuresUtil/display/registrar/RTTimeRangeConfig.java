package edu.sc.seis.fissuresUtil.display.registrar;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Timer;
import org.apache.log4j.Logger;
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
    public RTTimeRangeConfig(DataSetSeismogram[] seismos){
        this(seismos, new TimeInterval(.125, UnitImpl.SECOND));
    }

    public RTTimeRangeConfig (DataSetSeismogram[] seismos,
                              TimeInterval update){
        this(seismos, update, 1);
    }

    public RTTimeRangeConfig(DataSetSeismogram[] seismos,
                             TimeInterval update,
                             float speed){
        this(seismos, update, speed, new BasicTimeConfig());
    }

    public RTTimeRangeConfig(DataSetSeismogram[] seismos, TimeInterval update,
                             float speed, TimeConfig internalConfig){
        this.update = update;
        this.speed = speed;
        setInternalConfig(internalConfig);
        add(seismos);
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

    public void add(DataSetSeismogram[] seismos){
        internalTimeConfig.add(seismos);
    }

    public void updateTime(TimeEvent event) {
        time = event;
        Iterator it = listeners.iterator();
        while(it.hasNext()){
            ((TimeListener)it.next()).updateTime(event);
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
        listeners.add(listener);
    }

    public void removeListener(TimeListener listener) {
        listeners.remove(listener);
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

    public void reset(DataSetSeismogram[] seismos) {
        internalTimeConfig.reset(seismos);
    }

    public boolean contains(DataSetSeismogram seismo) {
        return internalTimeConfig.contains(seismo);
    }

    public TimeEvent fireTimeEvent() {
        return internalTimeConfig.fireTimeEvent();
    }

    public DataSetSeismogram[] getSeismograms() {
        return internalTimeConfig.getSeismograms();
    }

    public void clear() {
        internalTimeConfig.removeListener(this);
        internalTimeConfig.clear();
        internalTimeConfig.addListener(this);
    }

    private List listeners = new ArrayList();

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

    private static Logger logger = Logger.getLogger(RTTimeRangeConfig.class);

    private static TimeInterval THREE_MINUTES = new TimeInterval(3, UnitImpl.MINUTE);

}// RTTimeRangeConfig
