package edu.sc.seis.fissuresUtil.display;
import java.util.ArrayList;
import java.util.List;

import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.IndividualizedAmpConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * SingleSeismogramWindowDisplay displays every seismogram added to it
 * in a single BasicSeismogramDisplay
 *
 *
 * Created: Mon Nov 11 16:00:52 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class SingleSeismogramWindowDisplay extends VerticalSeismogramDisplay {
    public void add(DataSetSeismogram[] dss) {
        List toAdd = new ArrayList();
        for (int i = 0; i < dss.length; i++){
            if(!contains(dss[i])){
                toAdd.add(dss[i]);
            }
        }
        BasicSeismogramDisplay disp;
        DataSetSeismogram[] newSeis = new DataSetSeismogram[toAdd.size()];
        toAdd.toArray(newSeis);
        if(getCenter().getComponentCount() == 0){
            disp = new BasicSeismogramDisplay(tc, ac);
            disp.setParentDisplay(this);
            disp.add(newSeis);
            getCenter().add(disp);
        }
        else{
            disp = (BasicSeismogramDisplay)getCenter().getComponent(0);
            disp.add(newSeis);
        }
        setBorders();
    }

    public void setIndividualizedAmpConfig(AmpConfig ac){
        this.ac = new IndividualizedAmpConfig(ac);
        if(getCenter().getComponentCount() != 0){
            BasicSeismogramDisplay  disp = (BasicSeismogramDisplay)getCenter().getComponent(0);
            disp.setAmpConfig(this.ac);
        }
    }

}// SingleSeismogramWindowDisplay
