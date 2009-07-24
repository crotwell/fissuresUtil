package edu.sc.seis.fissuresUtil.display;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import edu.sc.seis.fissuresUtil.display.borders.AmpBorder;
import edu.sc.seis.fissuresUtil.display.borders.UnchangingTitleProvider;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class StationWindowDisplay extends VerticalSeismogramDisplay{
    public void add(DataSetSeismogram[] dss) {
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
            }else{
                current = new BasicSeismogramDisplay(tc, new RMeanAmpConfig());
                AmpBorder ab = (AmpBorder)current.get(BorderedDisplay.CENTER_LEFT);
                ab.add(new UnchangingTitleProvider(stationCode));
                current.setParentDisplay(this);
                getCenter().add(current);
                stationDisplay.put(stationCode, current);
            }
            current.add(curSeis);
        }
        setBorders();
    }

    public void clear(){
        stationDisplay = new HashMap();
        super.clear();
    }

    private Map stationDisplay = new HashMap();
}
