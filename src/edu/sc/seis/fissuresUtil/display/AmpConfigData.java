package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.bag.Statistics;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.List;
import java.util.Map;

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

public class AmpConfigData {

    /**
     * Creates a new <code>AmpConfigData</code> object
     *
     * @param seismo the seismogram being held by this AmpCOnfigData
     * @param cleanRange the unshaled range for this AmpConfigData
     * @param timeRange the time range that AmpRange represents
     * @param shift the amount to shift this amp range to reach its shaled range
     * @param scale the amount to scale this amp range to reach its shaled range
     */
    public AmpConfigData (DataSetSeismogram seismo, UnitRangeImpl cleanRange, MicroSecondTimeRange timeRange,
			  double shift, double scale){
	if ( cleanRange == null) {
	    // not sure if this is right or not, but I think that a 
	    // null cleanRange should not be allowed.
	    throw new IllegalArgumentException("CleanRange cannot be null");
	} // end of if ()
	
	this.seismo = seismo;
	this.cleanRange = cleanRange;
	this.timeRange = timeRange;
	this.shift = shift;
	this.scale = scale;
	shaledRange = null;

	
    }

    public LocalSeismogramImpl[] getSeismograms() {
	return (LocalSeismogramImpl[])seismograms.toArray(new LocalSeismogramImpl[0]);
    }
	
    public void addSeismograms(LocalSeismogramImpl[] seismos){
	for(int i = 0; i < seismos.length; i++){
	    seismograms.add(seismos[i]);
	}
	newData = true;
    }
	
    public boolean hasNewData(){ return newData; }
	
    public DataSetSeismogram getDSS(){ return seismo; }
	
    /**
     * <code>getCleanRange</code> returns the range before shaling
     *
     * @return a range unmodified by shaling
     */
    public UnitRangeImpl getCleanRange(){ return cleanRange; }
    
    /**
     * <code>setCleanRange</code> updates the data with a new clean range
     * and invalidates the old shaled range if the new range is different
     * than the old range
     * @param newRange the new clean range
     * @return true if the new clean range is different than the old clean
     * range
     */
    public boolean setCleanRange(UnitRangeImpl newRange){
	if(cleanRange != null && cleanRange.equals(newRange)){
	    return false;
	}
	cleanRange = newRange;
	shaledRange = null;
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
    public UnitRangeImpl shale(double shift, double scale){ return shale(shift, scale, cleanRange); }
	
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
     * <code>getShaledRange</code> returns the current range shaled by the
     * current scale and shift
     * @return the shaled range
     */
    public UnitRangeImpl getShaledRange(){
	// use tmpRange in case another thread sets shaledRange to null
	UnitRangeImpl tmpRange = shaledRange;
	if(tmpRange == null){
	    tmpRange = DisplayUtils.getShaledRange(cleanRange, this.shift, this.scale);
	    shaledRange = tmpRange;
	}
	return tmpRange;
    }
	
    /**
     * <code>getShaledOverRange</code> returns this data's shaled range
     * stretched or shrunk to equal the size of the range passed while
     * keeping the center at the same spot.
     * @param fullRange the size of the range to cover
     * @return the shaled range over the fullRange
     */
    public UnitRangeImpl getShaledOverRange(double fullRange){
	UnitRangeImpl range = getShaledRange();
	double middle = range.getMaxValue() - (range.getMaxValue() - range.getMinValue())/2;
	range.max_value = middle+fullRange/2;
	range.min_value = middle-fullRange/2;
	return range;
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
	if(newRange.equals(timeRange)){
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
	indexSet = false;
	shaledRange = cleanRange;
    }
	
    /**
     * <code>indexSet</code> is to check if the indices have been set for this config
     *
     * @return true if the indices have been set
     */
    public boolean indexSet(){
	return indexSet;
    }
	
    /**
     * <code>setCalcIndex</code> sets the seismogram data indices this AmpConfigData is
     * calculated over
     * @param indices a 2 element <code>int[]</code> containing the start and end indices
     */
    public void setCalcIndex(int[] indices){
	calcIndices = indices;
	indexSet = true;
    }
	
    /**
     * <code>getBeginIndex</code> returns the first index
     *
     * @return the first index for the seismogram range calculated here
     */
    public int getBeginIndex(){
	return calcIndices[0];
    }
	
    /**
     * <code>getEndIndex</code> returns the last index
     *
     * @return the last index for the range calculate here
     */
    public int getEndIndex(){
	return calcIndices[1];
    }

    public Statistics getStatistics(LocalSeismogramImpl lseis) {
	Statistics stat;
	if ( (stat = (Statistics)statCache.get(lseis)) != null) {
	    return stat;
	}
	stat = new Statistics(lseis);
	statCache.put(lseis, stat);
	return stat;
    }
	
    private DataSetSeismogram seismo;
    
    private UnitRangeImpl cleanRange, shaledRange;
	
    private MicroSecondTimeRange timeRange;
	
    private double shift;
	
    private double scale;
	
    private int[] calcIndices;
	
    private boolean newData;
    
    private boolean indexSet = false;
	
    private List seismograms = new ArrayList();
	
    private Map statCache = new WeakHashMap();
}// AmpConfigData
