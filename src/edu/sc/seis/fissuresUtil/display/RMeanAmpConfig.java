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

    protected AmpEvent recalculateAmp(){
        //System.out.println("RMean recalculation");
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

    protected boolean setAmpRange(DataSetSeismogram seismo){
        //System.out.println("RMean SetAmpRange");
        AmpConfigData data = (AmpConfigData)ampData.get(seismo);

        if ( data.getSeismograms().length == 0) {
            return data.setCleanRange(DisplayUtils.ZERO_RANGE);
        } // end of if ()

        LocalSeismogramImpl[] seismograms = data.getSeismograms();
        double points = 0;
        double meanTotal = 0;
        double[] minMaxAll = new double[2];
        minMaxAll[0] = Double.POSITIVE_INFINITY;
        minMaxAll[1] = Double.NEGATIVE_INFINITY;
        boolean dataInRange = false;
        UnitImpl seisUnit = seismograms[0].getUnit();
        for(int i = 0; i < seismograms.length; i++){
            LocalSeismogramImpl seis = seismograms[i];
            int[] seisIndex = DisplayUtils.getSeisPoints(seis, data.getTime());
            if(seisIndex[1] < 0 || seisIndex[0] >= seis.getNumPoints()) {
                //no data points in window, don't calculate range
                break;
            }
            dataInRange = true;
            if(seisIndex[0] < 0){
                seisIndex[0] = 0;
            }
            if(seisIndex[1] >= seis.getNumPoints()){
                seisIndex[1] = seis.getNumPoints() -1;
            }
            double[] minMaxMean =
                data.getStatistics(seis).minMaxMean(seisIndex[0], seisIndex[1]);
            if(minMaxMean[0] < minMaxAll[0]){
                minMaxAll[0] = minMaxMean[0];
            }
            if(minMaxMean[1] > minMaxAll[1]){
                minMaxAll[1] = minMaxMean[1];
            }
            points += seisIndex[1] - seisIndex[0];
            meanTotal += minMaxMean[2]*(seisIndex[1] - seisIndex[0]);
        }
        if(dataInRange){
            double mean = meanTotal/points;
            double meanDiff;
            double maxToMeanDiff = Math.abs(mean - minMaxAll[1]);
            double minToMeanDiff = Math.abs(mean - minMaxAll[0]);
            if(maxToMeanDiff > minToMeanDiff){
                meanDiff = maxToMeanDiff;
            }else{
                meanDiff = minToMeanDiff;
            }
            double min = mean - meanDiff;
            double max = mean + meanDiff;
            return data.setCleanRange(new UnitRangeImpl(min, max, seisUnit));
        }else{
            return data.setCleanRange(DisplayUtils.ZERO_RANGE);
        }
    }

    private UnitRangeImpl setRange(UnitRangeImpl currRange, double range){
        double middle = currRange.getMaxValue() - (currRange.getMaxValue() - currRange.getMinValue())/2;
        return new UnitRangeImpl(middle - range/2, middle + range/2, currRange.getUnit());
    }

    private static Category logger = Category.getInstance(RMeanAmpConfig.class.getName());

}// RMeanAmpConfig

