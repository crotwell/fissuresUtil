package edu.sc.seis.fissuresUtil.display;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.Iterator;
import org.apache.log4j.Category;

/**
 * RMeanAmpConfig.java
 *
 *
 * Created: Thu Oct  3 09:46:23 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class RMeanAmpConfig extends BasicAmpConfig {
    public RMeanAmpConfig(DataSetSeismogram[] seismos){
        super(seismos);
    }

    protected AmpEvent calculateAmp(){
        Iterator e = ampData.keySet().iterator();
        boolean changed = false;
        while(e.hasNext()){
            AmpConfigData current = (AmpConfigData)ampData.get(e.next());
            //checks for the time update equaling the old time
            if(current.setTime(getTime(current.getDSS()))){
                //checks if the new time changes the amp range
                if(setAmpRange(current.getDSS())){
                    // only generates a new amp event if the amp ranges change
                    changed = true;
                }
            }else if(current.hasNewData()){
                setAmpRange(current.getDSS());
                    changed = true;
            }
        }
        if(changed || currentAmpEvent == null){
            recalculateAmp();
        }
        return currentAmpEvent;
    }

    protected AmpEvent recalculateAmp(){
        Iterator e = ampData.keySet().iterator();
        double range = Double.NEGATIVE_INFINITY;
        while(e.hasNext()){
            UnitRangeImpl current =
                ((AmpConfigData)ampData.get(e.next())).getShaledRange();
            if(current != null &&
               current.getMaxValue() - current.getMinValue() > range){
                range = current.getMaxValue() - current.getMinValue();
            }
        }
        DataSetSeismogram[] seismos = getSeismograms();
        UnitRangeImpl[] amps = new UnitRangeImpl[seismos.length];
        for(int i = 0; i < seismos.length; i++){
            amps[i] = setRange(((AmpConfigData)ampData.get(seismos[i])).getShaledRange(),range);
        }
        currentAmpEvent = new AmpEvent(seismos, amps);
        return currentAmpEvent;
    }

    private boolean setAmpRange(DataSetSeismogram seismo){
        AmpConfigData data = (AmpConfigData)ampData.get(seismo);

    if ( data.getSeismograms().length == 0) {
        return data.setCleanRange(DisplayUtils.ZERO_RANGE);
    } // end of if ()

        LocalSeismogramImpl seis = data.getSeismograms()[0];
        int[] seisIndex = DisplayUtils.getSeisPoints(seis, data.getTime());
        if(seisIndex[1] < 0 || seisIndex[0] >= seis.getNumPoints()) {
            //no data points in window, set range to 0
            data.setCalcIndex(seisIndex);
            return data.setCleanRange(DisplayUtils.ZERO_RANGE);
        }

        if(seisIndex[0] < 0){
            seisIndex[0] = 0;
        }
        if(seisIndex[1] >= seis.getNumPoints()){
            seisIndex[1] = seis.getNumPoints() -1;
        }
        double[] minMaxMean =
            data.getStatistics(seis).minMaxMean(seisIndex[0], seisIndex[1]);
        double meanDiff;
        double maxToMeanDiff = Math.abs(minMaxMean[2] - minMaxMean[1]);
        double minToMeanDiff = Math.abs(minMaxMean[2] - minMaxMean[0]);
        if(maxToMeanDiff > minToMeanDiff){
            meanDiff = maxToMeanDiff;
        }else{
            meanDiff = minToMeanDiff;
        }
        data.setCalcIndex(seisIndex);
        double min = minMaxMean[2] - meanDiff;
        double max = minMaxMean[2] + meanDiff;
        return data.setCleanRange(new UnitRangeImpl(min, max, UnitImpl.COUNT));
    }

    private UnitRangeImpl setRange(UnitRangeImpl currRange, double range){
        double middle = currRange.getMaxValue() - (currRange.getMaxValue() - currRange.getMinValue())/2;
        return new UnitRangeImpl(middle - range/2, middle + range/2, UnitImpl.COUNT);
    }

    private static Category logger = Category.getInstance(RMeanAmpConfig.class.getName());

}// RMeanAmpConfig
