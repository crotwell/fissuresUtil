/**
 * LongShortTrigger.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

public class LongShortTrigger {
    public LongShortTrigger(LocalSeismogramImpl seis, int index, float value, float sta, float lta) {
        this(seis, index, value,
             seis.getBeginTime().add((TimeInterval)seis.getSampling().getPeriod().multiplyBy(index)),
            sta, lta);
    }

    public LongShortTrigger(LocalSeismogramImpl seis, int index, float value,
                            MicroSecondDate when,
                           float sta, float lta){
        this.seis = seis;
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
     * Returns the seismogram associated with the trigger.
     *
     * @return    a  LocalSeismogramImpl
     */
    public LocalSeismogramImpl getSeis() {
        return seis;
    }

    public float getSTA() { return sta; }

    public float getLTA() { return lta; }

    private LocalSeismogramImpl seis;

    private MicroSecondDate when;

    private float value;

    private int index;

    private float sta;

    private float lta;
}


