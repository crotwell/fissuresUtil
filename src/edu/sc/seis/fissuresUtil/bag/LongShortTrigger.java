/**
 * LongShortTrigger.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.bag;

import java.io.Serializable;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

public class LongShortTrigger implements Serializable {

    /** This uses the seismogram to get the channelId, seismogramId and trigger time, but does
     *  not retain a references to the seismogram to avoid memory leaks. */
    public LongShortTrigger(LocalSeismogramImpl seis, int index, float value, float sta, float lta) {
        this(seis.getChannelID(),
             seis.get_id(),
             index,
             value,
             seis.getBeginTime().add((TimeInterval)seis.getSampling().getPeriod().multiplyBy(index)),
             sta,
             lta);
    }

    public LongShortTrigger(ChannelId channelId,
                            String seisId,
                            int index,
                            float value,
                            MicroSecondDate when,
                            float sta,
                            float lta){
        this.seisId = seisId;
        this.channelId = channelId;
        this.index = index;
        this.when = when;
        this.value = value;
        this.sta = sta;
        this.lta = lta;
    }

    /**
     * Returns Index
     *
     * @return    an int
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns short average / long average trigger value
     *
     * @return    a  float
     */
    public float getValue() {
        return value;
    }

    /**
     * Returns time the trigger occurred.
     *
     * @return    a  MicroSecondDate
     */
    public MicroSecondDate getWhen() {
        return when;
    }

    /**
     * Returns the seismogram id associated with the trigger.
     *
     * @return    a  LocalSeismogram id
     */
    public String getSeisId() {
        return seisId;
    }

    /**
     * Returns the channel id associated with the trigger.
     *
     * @return    a  ChannelId
     */
    public String getChannelId() {
        return seisId;
    }

    public float getSTA() { return sta; }

    public float getLTA() { return lta; }

    private String seisId;

    private ChannelId channelId;

    private MicroSecondDate when;

    private float value;

    private int index;

    private float sta;

    private float lta;
}



