package edu.sc.seis.fissuresUtil.display.registrar;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.registrar.RTTimeRangeConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class RTTimeRangeConfig extends BasicTimeConfig{

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
        super(seismos);
        this.update = update;
        this.speed = speed;
    }

    public void add(DataSetSeismogram[] seismos){
        if(time == null){
            MicroSecondDate startTime = new MicroSecondDate(seismos[0].getRequestFilter().start_time);
            MicroSecondDate endTime = new MicroSecondDate(seismos[0].getRequestFilter().end_time);
            endTime = endTime.subtract(THREE_MINUTES).add(serverTimeOffset);
            time = new MicroSecondTimeRange(startTime, endTime);
        }
        super.add(seismos);
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
                                shaleTime(timeInterval.divideBy(time.getInterval()).getValue() * speed, 1);
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
        super.reset();
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }



    private TimeInterval update;

    public static final TimeInterval serverTimeOffset =
        ClockUtil.getTimeOffset();

    private MicroSecondDate lastDate;

    private float speed = 1;

    /** Timers are used for realTime update of the Seismograms **/
    protected Timer timer;

    protected TimeInterval width;

    private static Logger logger = Logger.getLogger(RTTimeRangeConfig.class);

    private static TimeInterval THREE_MINUTES = new TimeInterval(3, UnitImpl.MINUTE);

}// RTTimeRangeConfig
