package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.UnitRangeImpl;

/**
 * AmpConfigData.java
 *
 *
 * Created: Tue Sep  3 09:37:12 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class AmpConfigData {
    public AmpConfigData (DataSetSeismogram seismo, UnitRangeImpl ampRange, MicroSecondTimeRange timeRange, double shift, double scale){
	this.seismo = seismo;
	this.cleanRange = ampRange;
	this.timeRange = timeRange;
	this.shift = shift;
	this.scale = scale;
	needShale = true;
    }

    public DataSetSeismogram getSeismogram(){ return seismo; }

    public UnitRangeImpl getCleanRange(){ return cleanRange; }
    
    public boolean setCleanRange(UnitRangeImpl newRange){ 
	if(cleanRange != null && cleanRange.equals(newRange)){
	    return false;
	}
	cleanRange = newRange;
	needShale = true;
	return true;
    }

    public UnitRangeImpl shale(double shift, double scale){ return shale(shift, scale, cleanRange); }

    public UnitRangeImpl shale(double shift, double scale, UnitRangeImpl range){
	cleanRange = range;
	addShift(shift);
	addScale(scale);
	shaledRange = DisplayUtils.getShaledRange(range, this.shift, this.scale);
	needShale = false;
	return shaledRange;
    }

    public UnitRangeImpl getShaledRange(){ 
	if(needShale){
	    shaledRange = DisplayUtils.getShaledRange(cleanRange, this.shift, this.scale);
	    needShale = false;
	}
	return shaledRange; 
    }

    public UnitRangeImpl getShaledOverRange(double fullRange){
	UnitRangeImpl range = getShaledRange();
	double middle = range.getMaxValue() - (range.getMaxValue() - range.getMinValue())/2;
	range.max_value = middle+fullRange/2;
	range.min_value = middle-fullRange/2;
	return range;
    }
	
	  
    
    public MicroSecondTimeRange getTime(){ return timeRange; }

    public boolean setTime(MicroSecondTimeRange newRange){ 
	if(newRange.equals(timeRange)){
	    return false;
	}
	timeRange = newRange;
	return true;
    }

    public double getShift(){ return shift; }

    public void setShift(double newShift){ 
	shift = newShift; 
	needShale = true;
    }

    public double addShift(double newShift){
	shift += newShift * scale;
	needShale = true;
	return shift;
    }

    public double getScale(){ return scale; }

    public void setScale(double newScale){ 
	scale = newScale;
	needShale = true; 
    }

    public double addScale(double newScale){ 
	scale += newScale * scale;
	needShale = true;
	return scale;
    }

    public void reset(){
	shift = 0;
	scale = 1;
	needShale = false;
	indexSet = false;
	shaledRange = cleanRange;
    }

    public boolean indexSet(){
	return indexSet;
    }

    public void setCalcIndex(int[] indices){
	calcIndices = indices;
	indexSet = true;
    }

    public int getBeginIndex(){
	return calcIndices[0];
    }

    public int getEndIndex(){
	return calcIndices[1];
    }
	
    private DataSetSeismogram seismo;
    
    private UnitRangeImpl cleanRange, shaledRange;

    private MicroSecondTimeRange timeRange;

    private double shift;

    private double scale;

    private int[] calcIndices;

    private boolean needShale;
    
    private boolean indexSet = false;
}// AmpConfigData
