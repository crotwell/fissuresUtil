package edu.sc.seis.fissuresUtil.display.registrar;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;


/**
 * @author danala
 * Created on Jan 20, 2005
 */
public class CustomLayOutConfig extends BasicLayoutConfig{
    
public CustomLayOutConfig(double minDistance, double maxDistance,DataSetSeismogram[] dataSeis){
    super(dataSeis);
    this.minDistance = minDistance;
    this.maxDistance = maxDistance;
}

public LayoutEvent getLayout(){ return lastEvent; }

public synchronized LayoutEvent generateLayoutEvent(){
    
    DataSetSeismogram[] seis = getSeismograms();
    if(seis.length > 0){
        List orderedSeis = new ArrayList(seis.length);
        orderedSeis.add(seis[0]);
        double minDistBetween = Double.POSITIVE_INFINITY;
        for (int i = 1; i < seis.length; i++){
            DataSetSeismogram curSeis = seis[i];
            double curSeisDelt = ((QuantityImpl)valueMap.get(curSeis)).getValue();
            ListIterator orIt = orderedSeis.listIterator();
            boolean added = false;
            while(orIt.hasNext() && !added){
                DataSetSeismogram orSeis = (DataSetSeismogram)orIt.next();
                double orSeisDelt = ((QuantityImpl)valueMap.get(orSeis)).getValue();
                double distDiff = orSeisDelt - curSeisDelt;
                if(distDiff > 0){
                    orIt.previous();
                    orIt.add(curSeis);
                    added = true;
                }
                if(distDiff != 0 && Math.abs(distDiff) < minDistBetween){
                    minDistBetween = Math.abs(distDiff);
                }
            }
            if(!added){
                orderedSeis.add(curSeis);
            }
        }
        if(minDistBetween == Double.POSITIVE_INFINITY){//if minDistBetween hasn't changed, all the seis are at one place
            LayoutData[] data = new LayoutData[seis.length];
            for (int i = 0; i < data.length; i++){
                data[i] = new LayoutData(seis[i], 0.0, 1.0);
            }
            double dist = ((QuantityImpl)valueMap.get(orderedSeis.get(0))).getValue();
            UnitRangeImpl range = new UnitRangeImpl(dist - 2, dist + 2, UnitImpl.DEGREE);
            lastEvent = new LayoutEvent(data, range);
            return lastEvent;
        }
        double offset = minDistBetween * getScale()/2;
        double startDist =  minDistance;
        double endDist = maxDistance;
        double totalDistance = endDist - startDist;
        double percentageOffset = offset/totalDistance;
        LayoutData[] data = new LayoutData[seis.length];
        for (int i = 0; i < data.length; i++){
            DataSetSeismogram cur = (DataSetSeismogram)orderedSeis.get(i);
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
