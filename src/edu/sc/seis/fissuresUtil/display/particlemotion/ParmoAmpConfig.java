package edu.sc.seis.fissuresUtil.display.particlemotion;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfigData;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.BasicAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.BasicAmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.LazyAmpEvent;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * @author hedx Created on Jul 26, 2005
 */
public class ParmoAmpConfig extends BasicAmpConfig {
    public ParmoAmpConfig(){
        System.out.println("we are in parmoampconfig");
    }
    public void add(DataSetSeismogram[] seismos) {
        boolean someAdded = false;
        synchronized(ampData) {
            if(isEven(seismos.length)) {
                for(int i = 0; i < seismos.length - 1; i += 2) {
                    SeisContainer cont = new SeisContainer(seismos[i],
                                                           seismos[i + 1]);
                    if(!contains(cont)) {
                        seisData.add(cont);
                        ampData.add(new AmpConfigData(seismos[i], this));
                        ampData.add(new AmpConfigData(seismos[i + 1], this));
                        someAdded = true;
                    }
                }
            }
            if(someAdded) {
                dataArray = null;
                ampEvent = new LazyAmpEvent(this);
                seisArray = null;
            }
        }
        if(someAdded) {
            fireAmpEvent();
        }
    }

    public AmpEvent calculate() {
        System.out.println("we are in calculate");
        boolean changed = false;
        AmpConfigData[] ad = getUniqueAmpData();

        for(int i = 0; i < ad.length; i++) {
            if(ad[i] != null && //checks for the seismogram being removed
                                // between getSeismograms and here
                    ad[i].setTime(getTime(ad[i].getDSS()))) {//checks for the
                                                             // time update
                                                             // equaling the old
                                                             // time
                if(setAmpRange(ad[i])) { //checks if the new time changes the
                                         // amp range
                    changed = true;// only generates a new amp event if the amp
                                   // ranges have changed
                }
            } else if(ad[i] != null && ad[i].hasNewData()) {
                setAmpRange(ad[i]);
                changed = true;
            }
        }
        
        
        for(int i = 0; i<ad.length; i++){
            Iterator iter = ampData.iterator();
            while(iter.hasNext()){
                AmpConfigData data  = (AmpConfigData)iter.next();
                if(areTheyEqual(data, ad[i]) && !data.getRange().equals(ad[i].getRange())){
                    data.setRange(ad[i].getRange());
                }
            }
        }
        if(changed || ampEvent instanceof LazyAmpEvent) {
            ampEvent = recalculate();
        }
        return ampEvent;
    }

    private boolean areTheyEqual(AmpConfigData data1, AmpConfigData data2) {
        return data1.toString().equals(data2.toString());
    }
    


    private void addData(AmpConfigData newData, LinkedList newList) {
        //first we determine if the list contains the data. 
        boolean add = true;
        Iterator iter = newList.iterator();
        while(iter.hasNext()) {
            AmpConfigData temp = (AmpConfigData)iter.next();
            if(areTheyEqual(temp, newData)) {
                add = false;
                break;
            }
        }
        if(add){
            newList.add(newData);
        }
    }

    public AmpEvent recalculate() {
        System.out.println("we are in recalculate");
        AmpConfigData[] ad = getAmpData();
        System.out.println("ad length is: " + ad.length);
        System.out.println("the contents in ad: ");
        for(int i = 0; i<ad.length; i++){
            System.out.println(ad[i]);
        }
        System.out.println("their ranges: ");
        for(int i = 0; i<ad.length; i++){
            System.out.println(ad[i].getRange());
        }
        if(isEven(ad.length)) {
            UnitRangeImpl[] amps = new UnitRangeImpl[ad.length];
            int index = 0;
            for(int i = 0; i < ad.length - 1; i += 2) {
                /*UnitRangeImpl currentHorz = ad[i].getRange();
                UnitRangeImpl currentVert = ad[i + 1].getRange();
                double min = currentVert.getMinValue();
                double max = currentVert.getMaxValue();
                double minRatio = currentHorz.getMinValue() / min;
                double maxRatio = currentHorz.getMaxValue() / max;
                UnitRangeImpl vertRange = new UnitRangeImpl(min,
                                                            max,
                                                            UnitImpl.COUNT);
                UnitRangeImpl horzRange = new UnitRangeImpl(min * minRatio, max
                        * maxRatio, UnitImpl.COUNT);
                amps[index++] = horzRange;
                amps[index++] = vertRange;*/
                
                UnitRangeImpl currentHorz = ad[i].getRange();
                UnitRangeImpl currentVert = ad[i + 1].getRange();
//                System.out.println("horz range is: " + currentHorz);
//                System.out.println("vert range is: " + currentVert);
                double vMin = currentVert.getMinValue();
                double vMax = currentVert.getMaxValue();
                double hMin = currentHorz.getMinValue();
                double hMax = currentHorz.getMaxValue();
                UnitRangeImpl vertRange = new UnitRangeImpl(vMin,
                                                            vMax,
                                                            UnitImpl.COUNT);
                UnitRangeImpl horzRange = new UnitRangeImpl(hMin,
                                                            hMax,
                                                            UnitImpl.COUNT);
                amps[index++] = horzRange;
                amps[index++] = vertRange;
            }
            UnitRangeImpl genericRange = DisplayUtils.ONE_RANGE;
            return new BasicAmpEvent(getSeismograms(ad), amps, genericRange);
        }
        return null;
    }

    private AmpConfigData[] getUniqueAmpData() {
        synchronized(ampData) {
            Iterator iter = ampData.iterator();
            LinkedList newList = new LinkedList();
            while(iter.hasNext()) {
                AmpConfigData tempData = (AmpConfigData)iter.next();
                addData(tempData, newList);
            }
            AmpConfigData[] newArray = new AmpConfigData[newList.size()];
            for(int i = 0; i < newArray.length; i++) {
                newArray[i] = (AmpConfigData)newList.get(i);
            }
            return newArray;
        }
    }

    private boolean isEven(int n) {
        if(n % 2 == 0) {
            return true;
        }
        return false;
    }

    private boolean contains(SeisContainer container) {
        Iterator iter = seisData.iterator();
        while(iter.hasNext()) {
            SeisContainer seis = (SeisContainer)iter.next();
            if(container.equals(seis)) {
                return true;
            }
        }
        return false;
    }

    private List seisData = Collections.synchronizedList(new LinkedList());
}
