/**
 * PickZoneDisplay.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.Color;



public class PickZoneDisplay extends VerticalSeismogramDisplay{

    public void add(DataSetSeismogram[] dss) {
        for (int i = 0; i < dss.length; i++) {
            add(dss[i]);
        }
    }

    public void add(DataSetSeismogram seis){
        for (int i = 0; i < cp.getComponentCount(); i++) {
            BasicSeismogramDisplay cur = (BasicSeismogramDisplay)cp.getComponent(i);
            if(cur.contains(seis)) {
                continue;
            }else{
                DataSetSeismogram[] contained = cur.getSeismograms();
                for (int j = 0; j < contained.length; j++) {
                    if(DisplayUtils.areFriends(contained[j], seis)){
                        DataSetSeismogram[] newSeis = { seis};
                        cur.add(newSeis);
                        break;
                    }
                }
            }
        }
    }

    public void add(DataSetSeismogram[] seis, TimeConfig tc, Color color){
        Color untransparent = new NamedColor(color.getRed(),
                                             color.getBlue(),
                                             color.getGreen(),
                                             255,
                                             color.toString());
        BasicSeismogramDisplay disp = new BasicSeismogramDisplay(tc, new RMeanAmpConfig(), untransparent);
        disp.setParentDisplay(this);
        disp.add(seis, untransparent);
        getCenter().add(disp);
        revalidate();
        setBorders();
    }


    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PickZoneDisplay.class);

}
