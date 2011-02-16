/**
 * PickZoneDisplay.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.display;

import java.awt.Color;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class PickZoneDisplay extends VerticalSeismogramDisplay {

    public void add(DataSetSeismogram[] dss) {
        for(int i = 0; i < dss.length; i++) {
            add(dss[i]);
        }
    }

    public void add(DataSetSeismogram seis) {
        for(int i = 0; i < getCenter().getComponentCount(); i++) {
            BasicSeismogramDisplay cur = (BasicSeismogramDisplay)getCenter().getComponent(i);
            if(cur.contains(seis)) {
                continue;
            }
            DataSetSeismogram[] contained = cur.getSeismograms();
            for(int j = 0; j < contained.length; j++) {
                if(DisplayUtils.areFriends(contained[j], seis)) {
                    DataSetSeismogram[] newSeis = {seis};
                    cur.add(newSeis);
                    break;
                }
            }
        }
    }

    public void add(DataSetSeismogram[] seis, TimeConfig tc, Color color) {
        Color untransparent = new NamedColor(color.getRed(),
                                             color.getBlue(),
                                             color.getGreen(),
                                             255,
                                             color.toString());
        BasicSeismogramDisplay disp = new BasicSeismogramDisplay(tc,
                                                                 new RMeanAmpConfig(),
                                                                 untransparent);
        disp.setParentDisplay(this);
        disp.add(seis, untransparent);
        getCenter().add(disp);
        revalidate();
        setBorders();
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PickZoneDisplay.class);
}
