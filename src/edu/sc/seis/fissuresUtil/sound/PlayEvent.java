package edu.sc.seis.fissuresUtil.sound;

import edu.iris.Fissures.model.TimeInterval;


public class PlayEvent{

	private Object source;
	private TimeInterval interval;

	public PlayEvent(Object src, TimeInterval timeInterval){
		source = src;
		interval = timeInterval;
	}

	public TimeInterval getTimeInterval(){
		return interval;
	}

	public Object getSource(){
		return source;
	}
}
