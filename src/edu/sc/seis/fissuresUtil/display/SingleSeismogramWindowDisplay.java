package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.IndividualizedAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.ArrayList;
import java.util.List;

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

    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss){
        return addDisplay(dss, tc, ac);
    }

    /**
     *  adds the seismograms to the main display with an individual RMeanAmpConfig and the passed in TImeConfig
     * and adds it to the display
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc){
        return addDisplay(dss, tc, ac);
    }

    /**
     *  adds the seismogram to the main display with the passed in amp config and the global TImeConfig
     * and adds it to the display
     *
     * @param dss the seismograms for the new BSD
     * @param ac the amp config for the new BSD
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, AmpConfig ac){
        return addDisplay(dss, tc, ac);
    }

    /**
     * adds the seismograms to the main display with the passed in amp and time configs and adds it to
     * the display
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @param ac the amp config for the new BSD
     * @return the created BSD
     * @return a <code>BasicSeismogramDisplay</code> value
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc, AmpConfig ac){
        List toAdd = new ArrayList();
        for (int i = 0; i < dss.length; i++){
            if(!contains(dss[i])){
                toAdd.add(dss[i]);
            }
        }
        BasicSeismogramDisplay disp;
        DataSetSeismogram[] newSeis = new DataSetSeismogram[toAdd.size()];
        toAdd.toArray(newSeis);
        if(basicDisplays.size() == 0){
            disp = new BasicSeismogramDisplay(tc, ac, this);
            disp.add(newSeis);
            super.add(disp);
            disp.addBottomTimeBorder();
            disp.addTopTimeBorder();
            basicDisplays.add(disp);
        }
        else{
            disp = (BasicSeismogramDisplay)basicDisplays.getFirst();
            disp.add(newSeis);
        }
        return disp;
    }

    public void setIndividualizedAmpConfig(AmpConfig ac){
        this.ac = new IndividualizedAmpConfig(ac);
        if(basicDisplays.size() != 0){
            BasicSeismogramDisplay  disp = (BasicSeismogramDisplay)basicDisplays.getFirst();
            disp.setAmpConfig(this.ac);
        }
    }

}// SingleSeismogramWindowDisplay
