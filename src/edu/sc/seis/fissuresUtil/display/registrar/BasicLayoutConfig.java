package edu.sc.seis.fissuresUtil.display.registrar;
import java.util.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import javax.swing.JOptionPane;



public class BasicLayoutConfig implements LayoutConfig{
    public BasicLayoutConfig(){}

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
                QuantityImpl dist = calculateDistance(seismos[i]);
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
        if(seis.size() > 0){
            noDistDialog();
        }else{
            unableToDisplayDialog();
        }

    }

    private void noDistDialog(){
        JOptionPane.showMessageDialog(null,
                                      "Some of the seismograms added to the record section have no distances in their data set so they will not be displayed",
                                      "Unable to Display some Seismograms",
                                      JOptionPane.WARNING_MESSAGE);
    }

    public void unableToDisplayDialog(){
        JOptionPane.showMessageDialog(null,
                                      "All of the seismograms added to the record section have no distances in their data set so it can not be displayed",
                                      "Unable to display any seismograms",
                                      JOptionPane.WARNING_MESSAGE);
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
            orderedSeis.add(0, seis[0]);
            double minDistBetween = Double.POSITIVE_INFINITY;
            for (int i = 1; i < seis.length; i++){
                DataSetSeismogram curSeis = seis[i];
                double curSeisDelt = ((QuantityImpl)distanceMap.get(curSeis)).getValue();
                ListIterator orIt = orderedSeis.listIterator();
                boolean added = false;
                while(orIt.hasNext()){
                    DataSetSeismogram orSeis = (DataSetSeismogram)orIt.next();
                    double orSeisDelt = ((QuantityImpl)distanceMap.get(orSeis)).getValue();
                    if(curSeisDelt < orSeisDelt){
                        orIt.previous();
                        orIt.add(curSeis);
                        added = true;
                        break;
                    }
                    double distDiff = Math.abs(orSeisDelt - curSeisDelt);
                    if(distDiff != 0 && distDiff < minDistBetween){
                        minDistBetween = distDiff;
                    }
                }
                if(!added){
                    orderedSeis.add(curSeis);
                }
            }
            if(minDistBetween == Double.POSITIVE_INFINITY){//if minDistBetween hasn't changed, there is only one seis
                LayoutData[] data = { new LayoutData(seis[0], 0.0, 1.0)};
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

    public static QuantityImpl calculateDistance(DataSetSeismogram seis){
        EventAccessOperations event = seis.getDataSet().getEvent();
        ChannelId chanId = seis.getRequestFilter().channel_id;
        Channel seismoChannel = seis.getDataSet().getChannel(chanId);
        if(seismoChannel != null){
            Site seisSite = seismoChannel.my_site;
            Location seisLoc =  seisSite.my_location;
            Location eventLoc;
            try{
                eventLoc = event.get_preferred_origin().my_location;
            }catch(NoPreferredOrigin e){//if no preferred origin, just use the first
                eventLoc = event.get_origins()[0].my_location;
            }
            DistAz distAz = new DistAz(seisLoc.latitude, seisLoc.longitude,
                                       eventLoc.latitude, eventLoc.longitude);
            return new QuantityImpl(distAz.delta, UnitImpl.DEGREE);
        }
        return null;
    }

    private Map distanceMap = new HashMap();

    private List listeners = new ArrayList();

    private List seis =  new ArrayList();

    private double scale = 1;
}
