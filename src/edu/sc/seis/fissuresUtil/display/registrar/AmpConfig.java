package edu.sc.seis.fissuresUtil.display.registrar;

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * AmpConfigs calculate the amp ranges for a given set of seismograms based on a certain rule.
 * See BasicAmpConfig or RMeanAmpConfig for implementations of a couple rules.
 * Created: Tue Aug 27 14:48:03 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public interface AmpConfig extends DataSetSeismogramReceptacle, TimeListener{
    /**
     * <code>shaleAmp</code> shifts then scales all the seismograms in the config.
     *
     * First, the shift value is used to move the amp range by <code>shift</code>*amp range.
     * Then the scale value is used to adjust the high amp value so that the new amp range is
     * scale*amp range.
     *
     * So, if shift = 1 and scale = 2, the shift is applied and the new low end on the amp range is equal to the
     * old high value.  Then the range itself is modified to be two times its original size.  The new
     * range starts at the old low end and is twice as wide
     *
     * An adjustment of shift = 0, scale = 1 will leave the seismograms unchanged.  A scale value must
     * be greater than 0, or it will reduce the range to nothing.
     *
     * @param shift the percentage of current amp range by which the seismograms
     * will be shifted
     * @param scale the percentage of the current amp range that will exist after
     * the high end is adjusted
     */
    public void shaleAmp(double shift, double scale);

    /**
     * <code>shaleAmp</code> performs a shaleAmp on the seismograms in the array
     */
    public void shaleAmp(double shift, double scale, DataSetSeismogram[] seismos);

    /**
     * <code>addListener</code> causes listener to receive updates when this
     * AmpConfig changes
     *
     */
    public void addListener(AmpListener listener);

    /**
     * <code>removeListener</code> causes listener to no longer receive updates from
     * this AmpConfig
     *
     */
    public void removeListener(AmpListener listener);

    /**
     * <code>fireAmpEvent</code> makes the AmpConfig create a new AmpEvent
     * reflecting its current internal state and send it to all AmpListeners
     */
    public void fireAmpEvent();

    public AmpEvent calculate();

    public AmpConfigData getAmpData(DataSetSeismogram seis);

    public AmpConfigData[] getAmpData();

    public UnitRangeImpl getAmp();

    public UnitRangeImpl getAmp(DataSetSeismogram seis);
}// AmpConfig

