package edu.sc.seis.fissuresUtil.display.registrar;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * @author danala Created on Jan 20, 2005 Creates a custom layout for use in REV
 */
public class CustomLayOutConfig extends BasicLayoutConfig {

    public CustomLayOutConfig(double minDistance, double maxDistance,
            double percentSeisHeight) {
        super();
        this.percent_offset = percentSeisHeight / 100.0;
        double axisCorrection = (maxDistance - minDistance) * percent_offset;
        this.minDistance = minDistance - axisCorrection;
        this.maxDistance = maxDistance + axisCorrection;
    }

    public LayoutEvent getLayout() {
        return lastEvent;
    }

    public void setSwapAxes(boolean swapAxes) {
        this.swapAxes = swapAxes;
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
                double centerPercentage = 0;
                double cpVal = (endDist - curDist) / totalDistance;
                if(swapAxes) {
                    centerPercentage = 1 - cpVal;
                } else {
                    centerPercentage = cpVal;
                }
                data[i] = new LayoutData(cur,
                                         centerPercentage - percent_offset,
                                         centerPercentage + percent_offset);
            }
            UnitRangeImpl range = new UnitRangeImpl(startDist,
                                                    endDist,
                                                    UnitImpl.DEGREE);
            lastEvent = new LayoutEvent(data, range);
            return lastEvent;
        }
        lastEvent = LayoutEvent.EMPTY_EVENT;
        return lastEvent;
    }

    private LayoutEvent lastEvent = LayoutEvent.EMPTY_EVENT;

    private double percent_offset;

    private double minDistance;

    private boolean swapAxes = false;

    private double maxDistance;
}