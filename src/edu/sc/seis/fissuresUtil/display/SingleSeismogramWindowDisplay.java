package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.Registrar;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
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
    /**
     * Creates a <code>SingleSeismogramWindowDisplay</code> without a parent
     *
     */
    public SingleSeismogramWindowDisplay(){
        this(null);
    }

    /**
     * Creates a <code>SingleSeismogramWindowDisplay</code> with the passed in parent controlling it
     *
     * @param parent the VSD that controls this VSD
     */
    public SingleSeismogramWindowDisplay(VerticalSeismogramDisplay parent){
        super(parent);
    }

    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss){
        return addDisplay(dss, globalRegistrar, globalRegistrar);
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
        return addDisplay(dss, tc, globalRegistrar);
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
        return addDisplay(dss, globalRegistrar, ac);
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
        if(sorter.contains(dss)){
            return null;
        }
        BasicSeismogramDisplay disp;
        if(basicDisplays.size() == 0){
            disp = new BasicSeismogramDisplay(dss, tc, ac, this);
            super.add(disp);
            disp.addBottomTimeBorder();
            disp.addTopTimeBorder();
            basicDisplays.add(disp);
        }
        else{
            disp = (BasicSeismogramDisplay)basicDisplays.getFirst();
            disp.add(dss);
        }
        return disp;
    }

}// SingleSeismogramWindowDisplay
