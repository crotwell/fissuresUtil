package edu.sc.seis.fissuresUtil.display.registrar;
import org.apache.log4j.Logger;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SeismogramContainer;
import edu.sc.seis.fissuresUtil.display.SeismogramContainerFactory;
import edu.sc.seis.fissuresUtil.display.SeismogramContainerListener;
import edu.sc.seis.fissuresUtil.display.SeismogramIterator;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * AmpConfigData encapsulates the data for a particular seismogram in a particular
 * AmpConfig.  It contains convenience methods for updating ampranges, keeping
 * both a clean and shaled range for a seismogram, and storing the seismogram
 * point indices over which the currently stored amp range was calculated.
 * Created: Tue Sep  3 09:37:12 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class AmpConfigData implements SeismogramContainerListener{

    public AmpConfigData (DataSetSeismogram seismo,  AmpConfig parent) {
        this.parent = parent;
        this.container = SeismogramContainerFactory.create(this, seismo);
    }

    public void updateData() {
        newData = true;
        parent.fireAmpEvent();
    }

    public SeismogramIterator getIterator(){
        return container.getIterator(timeRange);
    }

    public boolean hasNewData(){ return newData; }

    public DataSetSeismogram getDSS(){ return container.getDataSetSeismogram();}

    /**
     * <code>setRange</code> updates the data with a new clean range
     * and invalidates the old shaled range if the new range is different
     * than the old range
     * @param newRange the new clean range
     * @return true if the new clean range is different than the old clean
     * range
     */
    public boolean setRange(UnitRangeImpl newRange){
        if(cleanRange != null && cleanRange.equals(newRange)){
            return false;
        }
        cleanRange = newRange;
        shaledRange = null;
        newData = false;
        return true;
    }

    /**
     * <code>shale</code> shales the currently held clean range by the
     * shift and scale passed in addition to any shift or scale already held
     * by the Data
     * @param shift additional shift for this range
     * @param scale additional shale for this range
     * @return the newly shaled range.
     */
    public UnitRangeImpl shale(double shift, double scale){
        return shale(shift, scale, cleanRange);
    }

    /**
     * Sets the clean range to be the passed range, and then shales it by
     * the values given in additon to any alredy held shift and scale.
     *
     * @param shift additional shift
     * @param scale additional shale
     * @param range the new clean range
     * @return the shaled clean range
     */
    public UnitRangeImpl shale(double shift, double scale, UnitRangeImpl range){
        cleanRange = range;
        addShift(shift);
        addScale(scale);
        shaledRange = DisplayUtils.getShaledRange(range, this.shift, this.scale);
        return shaledRange;
    }

    /**
     * <code>getRange</code> returns the current range shaled by the
     * current scale and shift
     * @return the shaled range
     */
    public UnitRangeImpl getRange(){
        if(shaledRange == null){
            shaledRange = DisplayUtils.getShaledRange(cleanRange, this.shift, this.scale);
        }
        return shaledRange;
    }

    /**
     * <code>getTime</code> is an accessor method for the time
     * this range describes
     *
     * @return the time this range is calculated over
     */
    public MicroSecondTimeRange getTime(){ return timeRange; }

    /**
     * <code>setTime</code> sets the time for this range
     *
     * @param newRange the new time range
     * @return true if the new time is different than the old one
     */
    public boolean setTime(MicroSecondTimeRange newRange){
        if(newRange.equals(timeRange) || newRange == DisplayUtils.ONE_TIME){
            return false;
        }
        timeRange = newRange;
        return true;
    }

    /**
     *
     * @return the amount the clean range is shifted to get the shaled range
     */
    public double getShift(){ return shift; }

    /**
     * <code>setShift</code> clears out the current shift and makes it equal
     * to the newShift
     * @param newShift the new shift for the data
     */
    public void setShift(double newShift){
        shift = newShift;
        shaledRange = null;
    }

    /**
     * <code>addShift</code> adds the new shift to the current shift scaled
     * by the amount the data is already scaled
     * @param newShift the amount of shift to be added in percentage of display shifted
     * @return the amount the current shift is after being modified by the newShift
     */
    public double addShift(double newShift){
        shift += newShift * scale;
        shaledRange = null;
        return shift;
    }

    public double getScale(){ return scale; }

    /**
     * <code>setScale</code> clears any existing scale and sets it to the new scale
     *
     * @param newScale the new scale value
     */
    public void setScale(double newScale){
        scale = newScale;
        shaledRange = null;
    }

    /**
     * <code>addScale</code> adds this scale to the existing scale after scaling it
     * by the existing scale
     * @param newScale a double describing the percentage of the current display the new scale
     * will equal
     * @return the current scale value for the amp config data after modification
     */
    public double addScale(double newScale){
        scale += newScale * scale;
        shaledRange = null;
        return scale;
    }

    /**
     * sets the shift to 0, scale to 1 and clears the calculation indices
     *
     */
    public void reset(){
        shift = 0;
        scale = 1;
        shaledRange = cleanRange;
    }

    public String toString(){
        return "AmpConfigData for " + container.getDataSetSeismogram().getName();
    }

    public static DataSetSeismogram[] getSeismograms(AmpConfigData[] ampData){
        DataSetSeismogram[] seisArray = new DataSetSeismogram[ampData.length];
        for (int i = 0; i < ampData.length; i++){
            seisArray[i] = ampData[i].getDSS();
        }
        return seisArray;
    }

    private SeismogramContainer container;

    private UnitRangeImpl cleanRange = DisplayUtils.ONE_RANGE;

    private UnitRangeImpl shaledRange;

    private MicroSecondTimeRange timeRange = DisplayUtils.ZERO_TIME;

    private double shift = 0;

    private double scale = 1;

    private AmpConfig parent;

    private boolean newData = false;

    private static Logger logger = Logger.getLogger(AmpConfigData.class);
}// AmpConfigData
