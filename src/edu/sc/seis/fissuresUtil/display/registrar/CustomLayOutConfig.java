package edu.sc.seis.fissuresUtil.display.registrar;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;


/**
 * @author danala
 * Created on Jan 20, 2005
 */
public class CustomLayOutConfig extends BasicLayoutConfig{
    
public CustomLayOutConfig(double minDistance, double maxDistance){
    super();
    this.minDistance = minDistance;
    this.maxDistance = maxDistance;
}

public LayoutEvent getLayout(){ return lastEvent; }

public synchronized LayoutEvent generateLayoutEvent(){
    
    DataSetSeismogram[] seis = getSeismograms();
    if(seis.length > 0){
        double startDist =  minDistance;
        double endDist = maxDistance;
        double totalDistance = endDist - startDist;
        double percentageOffset = 0.1;
        LayoutData[] data = new LayoutData[seis.length];
        for (int i = 0; i < data.length; i++){
            DataSetSeismogram cur = seis[i];
            double curDist = ((QuantityImpl)valueMap.get(cur)).getValue();
            double centerPercentage = (curDist - startDist)/totalDistance;
            data[i] = new LayoutData(cur,
                                     centerPercentage - percentageOffset,
                                     centerPercentage + percentageOffset);

        }
        UnitRangeImpl range = new UnitRangeImpl(startDist, endDist, UnitImpl.DEGREE);
        lastEvent = new  LayoutEvent(data, range);
        return lastEvent;
    }
    lastEvent = LayoutEvent.EMPTY_EVENT;
    return lastEvent;
}
private LayoutEvent lastEvent= LayoutEvent.EMPTY_EVENT;
private double minDistance;
private double maxDistance;



}
