package edu.sc.seis.fissuresUtil.display;

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
     * @param mouseForwarder the object every contained BSD forwards its mouse events to
     * @param motionForwarder the object every contained BSD forwards its mouse motion events to
     */
    public SingleSeismogramWindowDisplay(MouseForwarder mouseForwarder, MouseMotionForwarder motionForwarder){
	this(mouseForwarder, motionForwarder, null);
    }

    /**
     * Creates a <code>SingleSeismogramWindowDisplay</code> with the passed in parent controlling it
     *
     * @param mouseForwarder the object every contained BSD forwards its mouse events to
     * @param motionForwarder the object every contained BSD forwards its mouse motion events to
     * @param parent the VSD that controls this VSD
     */
    public SingleSeismogramWindowDisplay(MouseForwarder mouseForwarder, MouseMotionForwarder motionForwarder, 
				     VerticalSeismogramDisplay parent){
	super(mouseForwarder, motionForwarder, parent);
    }
    
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss){
	return addDisplay(dss, DisplayUtils.getSeismogramNames(dss));
    }

    /**
     * adds the seismograms to the main display with an individual RMeanAmpConfig and the global time registrar for
     * BSDs in this VSD and adds it to the the VSD
     *
     * @param dss the seismograms for the new BSD
     * @param name the BSD's name
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, String[] names){
	return addDisplay(dss, globalRegistrar, globalRegistrar, names);
    }

    /**
     *  adds the seismograms to the main display with an individual RMeanAmpConfig and the passed in TImeConfig
     * and adds it to the display
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @param name the BSD's name
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc, String[] names){
	return addDisplay(dss, tc, globalRegistrar, names);
    }
    
     /**
     *  adds the seismogram to the main display with the passed in amp config and the global TImeConfig
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
     * adds the seismograms to the main display with the passed in amp and time configs and adds it to
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
	    System.out.println("THE GLOBAL REGISTRAR WAS NULL, BUT NOW IT IS BEING CREATED");
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
	if(sorter.contains(names)){
	    return null;
	}
	BasicSeismogramDisplay disp;
	if(basicDisplays.size() == 0){
	    disp = new BasicSeismogramDisplay(dss, tc, ac, names, this);
	    super.add(disp);
	    disp.addMouseMotionListener(motionForwarder);
	    disp.addMouseListener(mouseForwarder);
	    disp.addBottomTimeBorder();
	    disp.addTopTimeBorder();
	    basicDisplays.add(disp);
	}
	else{
	    disp = (BasicSeismogramDisplay)basicDisplays.getFirst();
	    disp.add(dss, names);
	}
	return disp;
    }
        
}// SingleSeismogramWindowDisplay
