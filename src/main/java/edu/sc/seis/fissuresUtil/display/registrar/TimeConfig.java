package edu.sc.seis.fissuresUtil.display.registrar;

import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * TimeConfigs calculate the time ranges for a given set of seismograms
 *
 *
 * Created: Tue Aug 27 14:45:38 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public interface TimeConfig extends DataSetSeismogramReceptacle {
    /**
     * <code>shaleTime</code> shifts then scales all the seismograms in the config.
     *
     * First, the shift value is used to move the begin time by <code>shift</code>*timeWidth.
     * Then the scale value is used to adjust the end time so that the new timeWidth is
     * scale*timeWidth.
     *
     * So, if shift = 1 and scale = 2, the shift is applied and the new begin time is equal to the
     * old end time.  Then the time width is modified to be two times its original size.  The new
     * time starts at the old begin time and lasts twice as long.
     *
     * An adjustment of shift = 0, scale = 1 will leave the seismograms unchanged.  A scale value must
     * be greater than 0, or it will reduce the time width to nothing.
     *
     * @param shift the percentage of current time width by which the seismograms
     * will be shifted
     * @param scale the percentage of the current time width that will exist after
     * the end time is moved
     */
    public void shaleTime(double shift, double scale);

    /**
     * <code>shaleTime</code> performs a shale on the seismograms in the array
     */
    public void shaleTime(double shift, double scale, DataSetSeismogram[] seismos);

    /**
     * <code>add</code> adds the listener to the group of objects that are
     * updated when this time config changes
     * @param listener a <code>TimeEventListener</code> that will be updated
     * as this config changes
     */
    public void addListener(TimeListener listener);

    /**
     * <code>remove</code> removes listener from the update group
     *
     * @param listener a <code>TimeEventListener</code> that will no longer
     * receive updates from this config
     */
    public void removeListener(TimeListener listener);

    /**
     * <code>fireTimeEvent</code> causes the config to prepare a time event and fire it
     * to all of its <code>TimeEvent</code> objects
     *
     * @return the ConfigEvent fired
     */
    public TimeEvent fireTimeEvent();

    /**
     * @return   a MicroSecondTimeRange that covers the current generic time
     * range of this TimeConfig
     *
     */
    public MicroSecondTimeRange getTime();


    /**
     * @param    seis                a  DataSetSeismogram a time is desired for
     *
     * @return   a MicroSecondTimeRange describing the current time of the given
     * seismogram in the time config
     *
     */
    public MicroSecondTimeRange getTime(DataSetSeismogram seis);

    public double getShift();

    public double getScale();
}// TimeConfig
