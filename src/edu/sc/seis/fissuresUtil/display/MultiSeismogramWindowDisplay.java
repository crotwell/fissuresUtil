package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.Registrar;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * MultiSeismogramWindowDisplay displays every seismogram added to it
 * in a new Basic Seismogram Display
 *
 *
 * Created: Mon Nov 11 16:01:35 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class MultiSeismogramWindowDisplay extends VerticalSeismogramDisplay {
    /**
     * Creates a <code>MultiSeismogramWindowDisplay</code> without a parent
     *
     * @param mouseForwarder the object every contained BSD forwards its mouse events to
     * @param motionForwarder the object every contained BSD forwards its mouse motion events to
     */
    public MultiSeismogramWindowDisplay(MouseForwarder mouseForwarder, MouseMotionForwarder motionForwarder){
    this(mouseForwarder, motionForwarder, null);
    }

    /**
     * Creates a <code>MultiSeismogramWindowDisplay</code> with a parent
     *
     * @param mouseForwarder the object every contained BSD forwards its mouse events to
     * @param motionForwarder the object every contained BSD forwards its mouse motion events to
     * @param parent the VSD that controls this VSD
     */
    public MultiSeismogramWindowDisplay(MouseForwarder mouseForwarder, MouseMotionForwarder motionForwarder,
                     VerticalSeismogramDisplay parent){
    super(mouseForwarder, motionForwarder, parent);
    }

    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss){
    return addDisplay(dss, globalRegistrar, new RMeanAmpConfig(dss));
    }

    /**
     * creates a new BSD with an individual RMeanAmpConfig and the passed in TImeConfig
     * and adds it to the display
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @param name the BSD's name
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc){
    return addDisplay(dss, tc, new RMeanAmpConfig(dss));
    }

     /**
     * creates a new BSD with the passed in amp config and the global TImeConfig
     * and adds it to the display
     *
     * @param dss the seismograms for the new BSD
     * @param ac the amp config for the new BSD
     * @param name the BSD's name
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, AmpConfig ac){
    return addDisplay(dss, globalRegistrar, ac);
    }

     /**
     * creates a new BSD with the passed in amp and time configs and adds it to
     * the display
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @param ac the amp config for the new BSD
     * @param name the BSD's name
     * @return the created BSD
     * @return a <code>BasicSeismogramDisplay</code> value
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc, AmpConfig ac){
    if(tc == globalRegistrar && globalRegistrar == null){
        globalRegistrar = new Registrar(dss);
        tc = globalRegistrar;
    }
    if(sorter.contains(dss)){
        return null;
    }
    BasicSeismogramDisplay disp = null;
    for(int i = 0; i < dss.length; i++){
        DataSetSeismogram[] seismos = { dss[i] };
        disp = new BasicSeismogramDisplay(seismos, tc, ac, this);
        int j = sorter.sort(seismos);
        super.add(disp, j);
        disp.addMouseMotionListener(motionForwarder);
        disp.addMouseListener(mouseForwarder);
        if(basicDisplays.size() > 0){
        ((BasicSeismogramDisplay)basicDisplays.getLast()).removeBottomTimeBorder();
        ((BasicSeismogramDisplay)basicDisplays.getFirst()).removeTopTimeBorder();
        }
        basicDisplays.add(j, disp);
        ((BasicSeismogramDisplay)basicDisplays.getLast()).addBottomTimeBorder();
        ((BasicSeismogramDisplay)basicDisplays.getFirst()).addTopTimeBorder();
    }
    return disp;
    }

}// MultiSeismogramWindowDisplay
