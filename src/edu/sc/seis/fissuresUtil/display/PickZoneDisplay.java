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
        // TODO
    }


    /**
     * adds the seismograms to the VSD with the passed amp config
     * @param dss the seismograms to be added
     * @param ac the amp config for the new seismograms
     * @return the BSD the seismograms were added to
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, AmpConfig ac) {
        return addDisplay(dss);
    }

    /**
     * adds the given seismograms to the VSD with their seismogram names as suggestions
     *
     *
     * @param dss the seismograms to be added
     * @return the BSD the seismograms were added to
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss) {
        for (int i = 0; i < dss.length; i++) {
            add(dss[i]);
        }
        return (BasicSeismogramDisplay)cp.getComponent(0);
    }

    /**
     * adds the seismograms to the VSD with the passed timeConfig
     *
     * @param dss the seismograms to be added
     * @param tc the time config for the new seismograms
     * @return the BSD the seismograms were added to
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc) {
        return addDisplay(dss);
    }

    /**
     * adds the seismograms to the VSD with the passed timeConfig and ampConfig
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @param ac the amp config for the new BSD
     * @return the BSD the seismograms were added to
     *
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc, AmpConfig ac) {
        return addDisplay(dss);
    }

    public BasicSeismogramDisplay add(DataSetSeismogram[] seis, TimeConfig tc, Color color){
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
        return disp;
    }

    public BasicSeismogramDisplay add(DataSetSeismogram seis){
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
                        return cur;
                    }
                }
            }
        }
        return null;
    }
}
