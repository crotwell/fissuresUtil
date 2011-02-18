package edu.sc.seis.fissuresUtil.display.registrar;

import java.util.Iterator;

import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;

public class LayoutEvent{
    public LayoutEvent(LayoutData[] organizedSeismograms, UnitRangeImpl distance){
        this.data = organizedSeismograms;
        if(distance == null){
            throw new IllegalArgumentException("distance can not be null");
        }
        this.distance = distance;
        name = "not empty event";
    }

    public LayoutData getItemAt(int index){
        return data[index];
    }

    public UnitRangeImpl getDistance(){ return distance; }

    public Iterator iterator(){
        return new Iterator(){

            public void remove() {
                throw new UnsupportedOperationException("cannot remove from a layout event iterator");
            }

            public boolean hasNext() {
                if(index < data.length){
                    return true;
                }
                return false;
            }

            public Object next() {
                return data[index++];
            }
            private int index = 0;
        };
    }

    public String toString(){ return name; }

    public String name = "EMPTY EVENT";

    public static final UnitRangeImpl ONE_DEGREE = new UnitRangeImpl(0,1,UnitImpl.DEGREE);

    public static final LayoutEvent EMPTY_EVENT = new LayoutEvent(new LayoutData[0],
                                                                 ONE_DEGREE);

    private LayoutData[] data;

    private UnitRangeImpl distance = ONE_DEGREE;
}

