package edu.sc.seis.fissuresUtil.sound;

import edu.iris.Fissures.model.TimeInterval;
import javax.sound.sampled.Clip;


public class PlayEvent{

    private Object source;
    private TimeInterval interval;
    private Clip clip;

    public PlayEvent(Object src, TimeInterval timeInterval, Clip clip){
        source = src;
        interval = timeInterval;
        this.clip = clip;
    }

    public TimeInterval getTimeInterval(){
        return interval;
    }

    public Object getSource(){
        return source;
    }

    public Clip getClip(){ return clip; }
}
