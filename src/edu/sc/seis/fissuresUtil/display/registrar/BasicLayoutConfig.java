package edu.sc.seis.fissuresUtil.display.registrar;
import java.util.*;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import javax.swing.JOptionPane;



public class BasicLayoutConfig implements LayoutConfig{
    public BasicLayoutConfig(){ }
    
    public BasicLayoutConfig(DataSetSeismogram[] seismos){
        add(seismos);
    }
    
    public synchronized void addListener(LayoutListener listener) {
        listeners.add(listener);
        fireLayoutEvent();
    }
    
    public synchronized void removeListener(LayoutListener listener) {
        listeners.remove(listener);
    }
    
    public void fireLayoutEvent(){
        fireLayoutEvent(generateLayoutEvent());
    }
    
    private synchronized void fireLayoutEvent(LayoutEvent event) {
        Iterator it = listeners.iterator();
        while(it.hasNext()){
            ((LayoutListener)it.next()).updateLayout(event);
        }
    }
    
    /*
     *Checks the given seismograms for already being in the config, and if not,
     *adds them and fires a new LayoutEvent with them
     */
    public synchronized void add(DataSetSeismogram[] seismos) {
        List noDist = new ArrayList();
        boolean someAdded = false;
        for (int i = 0; i < seismos.length; i++){
            if(!(distanceMap.containsKey(seismos[i]))){
                QuantityImpl dist = DisplayUtils.calculateDistance(seismos[i]);
                if(dist == null){
                    noDist.add(seismos[i]);
                }else{
                    seis.add(seismos[i]);
                    distanceMap.put(seismos[i], dist);
                    someAdded = true;
                }
            }
        }
        if(someAdded){
            fireLayoutEvent();
        }
    }
    
    /**
     *Attempts to remove all given seismograms from this Config.  If any are
     * removed, a layout event is fired
     */
    public synchronized void remove(DataSetSeismogram[] seismos) {
        for (int i = 0; i < seismos.length; i++){
            if(distanceMap.containsKey(seismos[i])){
                distanceMap.remove(seismos[i]);
            }
        }
        boolean someRemoved = false;
        for (int i = 0; i < seismos.length; i++){
            if(seis.remove(seismos[i])){
                someRemoved = true;
            }
        }
        if(someRemoved){
            fireLayoutEvent();
        }
    }
    
    public synchronized boolean contains(DataSetSeismogram seismo) {
        if(seis.contains(seismo)){
            return true;
        }
        return false;
    }
    
    public synchronized void clear() {
        if(seis.size() > 0){
            seis.clear();
            fireLayoutEvent();
        }
    }
    
    public DataSetSeismogram[] getSeismograms() {
        return (DataSetSeismogram[])seis.toArray(new DataSetSeismogram[seis.size()]);
    }
    
    public void reset() {
        DataSetSeismogram[] seismograms = getSeismograms();
        seis.clear();
        add(seismograms);
    }
    
    public void reset(DataSetSeismogram[] seismos) {
        List reset = new ArrayList();
        for (int i = 0; i < seismos.length; i++){
            if(contains(seismos[i])){
                seis.remove(seismos[i]);
                reset.add(seismos[i]);
            }
        }
        add((DataSetSeismogram[])reset.toArray(new DataSetSeismogram[reset.size()]));
    }
    
    public double getScale() {
        return scale;
    }
    
    /**
     *sets the amount by which every seismogram in the layout is being scaled
     *@param scale - the factor by which the seismogram height is multiplied
     */
    public void setScale(double scale) {
        if(this.scale != scale){
            this.scale = scale;
            fireLayoutEvent();
        }
    }
    
    public synchronized LayoutEvent generateLayoutEvent(){
        DataSetSeismogram[] seis = getSeismograms();
        if(seis.length > 0){
            List orderedSeis = new ArrayList(seis.length);
            orderedSeis.add(seis[0]);
            double minDistBetween = Double.POSITIVE_INFINITY;
            for (int i = 1; i < seis.length; i++){
                DataSetSeismogram curSeis = seis[i];
                double curSeisDelt = ((QuantityImpl)distanceMap.get(curSeis)).getValue();
                ListIterator orIt = orderedSeis.listIterator();
                boolean added = false;
                while(orIt.hasNext() && !added){
                    DataSetSeismogram orSeis = (DataSetSeismogram)orIt.next();
                    double orSeisDelt = ((QuantityImpl)distanceMap.get(orSeis)).getValue();
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
                return new LayoutEvent(data, LayoutEvent.ONE_DEGREE);
            }
            double offset = minDistBetween * scale/2;
            double startDist =  ((QuantityImpl)distanceMap.get(orderedSeis.get(0))).getValue() - offset;
            double endDist = ((QuantityImpl)distanceMap.get(orderedSeis.get(orderedSeis.size() - 1))).getValue() + offset;
            double totalDistance = endDist - startDist;
            double percentageOffset = offset/totalDistance;
            LayoutData[] data = new LayoutData[seis.length];
            for (int i = 0; i < data.length; i++){
                DataSetSeismogram cur = (DataSetSeismogram)orderedSeis.get(i);
                double curDist = ((QuantityImpl)distanceMap.get(cur)).getValue();
                double centerPercentage = (curDist - startDist)/totalDistance;
                data[i] = new LayoutData(cur,
                                         centerPercentage - percentageOffset,
                                         centerPercentage + percentageOffset);
                
            }
            UnitRangeImpl range = new UnitRangeImpl(startDist, endDist, UnitImpl.DEGREE);
            return new LayoutEvent(data, range);
        }
        return LayoutEvent.EMPTY_EVENT;
    }
    
    private Map distanceMap = new HashMap();
    
    private Set listeners = new HashSet();
    
    private List seis =  new ArrayList();
    
    private double scale = 1;
}
