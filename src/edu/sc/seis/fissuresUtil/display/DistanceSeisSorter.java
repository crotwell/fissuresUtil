package edu.sc.seis.fissuresUtil.display;

/**
 * DistanceSeisSorter.java
 *
 * @author Created by Charlie Groves
 */

import edu.sc.seis.fissuresUtil.display.registrar.BasicLayoutConfig;
import edu.sc.seis.fissuresUtil.display.registrar.LayoutEvent;
import edu.sc.seis.fissuresUtil.display.registrar.LayoutListener;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.Iterator;

public class DistanceSeisSorter extends SeismogramSorter implements LayoutListener{
    public DistanceSeisSorter(){
        layoutConfig.addListener(this);
    }

    public void updateLayout(LayoutEvent e) {}

    public int sort(DataSetSeismogram seismogram){
        DataSetSeismogram[] dss = {seismogram};
        layoutConfig.add(dss);
        Iterator it = layoutConfig.generateLayoutEvent().iterator();
        int i = 0;
        while(it.hasNext()){
            if(it.next().toString().equals(seismogram.toString())){
                return i;
            }
            i++;
        }
        return 0;
    }

    public boolean remove(DataSetSeismogram seismogram){
        if(layoutConfig.contains(seismogram)){
            DataSetSeismogram[] dss = {seismogram};
            layoutConfig.remove(dss);
            return true;
        }
        return false;
    }

    private BasicLayoutConfig layoutConfig = new BasicLayoutConfig();

}

