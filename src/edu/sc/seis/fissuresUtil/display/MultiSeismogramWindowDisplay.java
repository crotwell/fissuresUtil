package edu.sc.seis.fissuresUtil.display;

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
	return addDisplay(dss, DisplayUtils.getSeismogramNames(dss));
    }

    /**
     * creates a new BSD with an individual RMeanAmpConfig and the global time registrar for
     * BSDs in this VSD and adds it to the the VSD
     *
     * @param dss the seismograms for the new BSD
     * @param name the BSD's name
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, String[] names){
	return addDisplay(dss, globalRegistrar, new RMeanAmpConfig(dss), names);
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
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc, String[] names){
	return addDisplay(dss, tc, new RMeanAmpConfig(dss), names);
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
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, AmpConfig ac, String[] names){
	return addDisplay(dss, globalRegistrar, ac, names);
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
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc, AmpConfig ac, String[] names){
	if(tc == globalRegistrar && globalRegistrar == null){
	    globalRegistrar = new Registrar(dss);
	    tc = globalRegistrar;
	}
	if(sorter.contains(names)){
	    return null;
	}
	BasicSeismogramDisplay disp = null;
	for(int i = 0; i < dss.length; i++){
	    DataSetSeismogram[] seismos = { dss[i] };
	    String[] subNames = {names[i]};
	    disp = new BasicSeismogramDisplay(seismos, tc, ac, subNames, this);
	    int j = sorter.sort(seismos, subNames);
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
