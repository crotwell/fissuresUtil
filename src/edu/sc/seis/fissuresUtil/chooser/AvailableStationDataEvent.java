package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.IfNetwork.Station;

/**
 * AvailableStationDataEvent.java
 *
 * @author Created by Philip Oliver-Paull
 */
public class AvailableStationDataEvent{
    private Object source;
    private Station station;
    private int isUp;

    public static final int UP = 2;
    public static final int DOWN = 1;
    public static final int UNKNOWN = 0;

    public AvailableStationDataEvent(Object source, Station station, int isUp){
        this.source = source;
        this.station = station;
        this.isUp = isUp;
    }

    public Object getSource(){
        return source;
    }

    public Station getStation(){
        return station;
    }

    public boolean stationIsUp(){
        return isUp == UP;
    }

    public boolean isStatusKnown() {
        return isUp != 0;
    }

    public int getStationStatus() {
        return isUp;
    }
}

