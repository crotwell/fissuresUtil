package edu.sc.seis.fissuresUtil.display.registrar;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * @author danala Created on Jan 20, 2005 
 * Creates a custom layout for use in REV
 */
public class CustomLayOutConfig extends BasicLayoutConfig {

    public CustomLayOutConfig(double minDistance, double maxDistance) {
        super();
        double axisCorrection = (maxDistance - minDistance) * PERCENT_OFFSET;
        this.minDistance = minDistance - axisCorrection;
        this.maxDistance = maxDistance + axisCorrection;
    }

    public LayoutEvent getLayout() {
        return lastEvent;
    }

    public synchronized LayoutEvent generateLayoutEvent() {
        DataSetSeismogram[] seis = getSeismograms();
        if(seis.length > 0) {
            double startDist = minDistance;
            double endDist = maxDistance;
            double totalDistance = endDist - startDist;
            LayoutData[] data = new LayoutData[seis.length];
            for(int i = 0; i < data.length; i++) {
                DataSetSeismogram cur = seis[i];
                double curDist = ((QuantityImpl)valueMap.get(cur)).getValue();
                double centerPercentage = (endDist - curDist) / totalDistance;
                data[i] = new LayoutData(cur, centerPercentage
                        - PERCENT_OFFSET, centerPercentage + PERCENT_OFFSET);
            }
            UnitRangeImpl range = null;
            if(startDist > endDist) {
                range = new UnitRangeImpl(endDist, startDist, UnitImpl.DEGREE);
            } else {
                range = new UnitRangeImpl(startDist, endDist, UnitImpl.DEGREE);
            }
            lastEvent = new LayoutEvent(data, range);
            return lastEvent;
        }
        lastEvent = LayoutEvent.EMPTY_EVENT;
        return lastEvent;
    }

    private LayoutEvent lastEvent = LayoutEvent.EMPTY_EVENT;
    
    public static double PERCENT_OFFSET = 0.1;

    private double minDistance;

    private double maxDistance;
}