package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.Registrar;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



/**
 * StationWindowDisplay.java
 *
 * @author Created by Charlie Groves
 */

public class StationWindowDisplay extends VerticalSeismogramDisplay{
    public StationWindowDisplay(){ this(null); }

    public StationWindowDisplay(VerticalSeismogramDisplay parent){
        super(parent);
    }

    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss) {
        return addDisplay(dss, globalRegistrar, globalRegistrar);
    }

    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, AmpConfig ac) {
        return addDisplay(dss, globalRegistrar, ac);
    }

    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc) {
        return addDisplay(dss, tc, globalRegistrar);
    }

    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc, AmpConfig ac) {
        if(tc == globalRegistrar && globalRegistrar == null){
            boolean setAC = false;
            if(ac == globalRegistrar){
                setAC = true;
            }
            globalRegistrar = new Registrar(dss);
            if(setAC){
                ac = globalRegistrar;
            }
            tc = globalRegistrar;
        }
        Map stationDss = new HashMap();
        for (int i = 0; i < dss.length; i++){
            String stationCode = dss[i].getRequestFilter().channel_id.station_code;
            if(stationDss.containsKey(stationCode)){
                ((List)stationDss.get(stationCode)).add(dss[i]);
            }else{
                List newList = new ArrayList();
                newList.add(dss[i]);
                stationDss.put(stationCode, newList);
            }
        }
        Iterator it = stationDss.keySet().iterator();
        BasicSeismogramDisplay current = null;
        while(it.hasNext()){
            String stationCode = (String)it.next();
            DataSetSeismogram[] curSeis = (DataSetSeismogram[])((List)stationDss.get(stationCode)).toArray(new DataSetSeismogram[0]);
            if(stationDisplay.containsKey(stationCode)){
                current = (BasicSeismogramDisplay)stationDisplay.get(stationCode);
                current.add(curSeis);
            }else{
                current = new BasicSeismogramDisplay(curSeis, tc, ac, this);
                super.add(current);
                basicDisplays.add(current);
                current.addBottomTimeBorder();
                current.addTopTimeBorder();
                stationDisplay.put(stationCode, current);
            }
        }
        return current;
    }


    private Map stationDisplay = new HashMap();
}

