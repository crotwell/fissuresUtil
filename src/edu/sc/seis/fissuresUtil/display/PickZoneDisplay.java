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

    /**
     * adds the seismograms to the VSD with the passed amp config
     * @param dss the seismograms to be added
     * @param ac the amp config for the new seismograms
     * @return the BSD the seismograms were added to
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, AmpConfig ac) {
        throw new UnsupportedOperationException("only add is supported on PickZoneDisplay");
    }

    /**
     * adds the given seismograms to the VSD with their seismogram names as suggestions
     *
     *
     * @param dss the seismograms to be added
     * @return the BSD the seismograms were added to
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss) {
        throw new UnsupportedOperationException("only add is supported on PickZoneDisplay");
    }

    /**
     * adds the seismograms to the VSD with the passed timeConfig
     *
     * @param dss the seismograms to be added
     * @param tc the time config for the new seismograms
     * @return the BSD the seismograms were added to
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc) {
        return addDisplay(dss, tc, new RMeanAmpConfig());
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
        throw new UnsupportedOperationException("only add is supported on PickZoneDisplay");
    }

    public BasicSeismogramDisplay add(DataSetSeismogram[] seis, TimeConfig tc, Color color){
        Color untransparent = new Color(color.getRed(),
                                       color.getGreen(),
                                       color.getBlue());
        BasicSeismogramDisplay newDisplay = new BasicSeismogramDisplay(tc, new RMeanAmpConfig(), this, untransparent);
        newDisplay.add(seis, untransparent);
        DataSetSeismogram[] componentSorted = DisplayUtils.getComponents(seis[0]);
        for (int i = 0; i < componentSorted.length; i++) {
            if(!componentSorted[i].equals(seis[0])){
                DataSetSeismogram[] adder = { componentSorted[i] };
                newDisplay.add(adder);
            }
        }
        seis[0].setName(seis[0].getName() + "." + color);
        newDisplay.removeTopTimeBorder();
        newDisplay.addBottomTimeBorder();
        add(newDisplay);
        basicDisplays.add(newDisplay);
        revalidate();
        return newDisplay;
    }

}

