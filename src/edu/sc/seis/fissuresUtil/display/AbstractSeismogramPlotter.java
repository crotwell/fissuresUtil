package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import java.awt.Shape;
import java.awt.Dimension;

/**
 * AbstractSeismogramPlotter.java
 *
 *
 * Created: Mon Jul  1 11:44:46 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public abstract class AbstractSeismogramPlotter implements Plotter{
    
    public DataSetSeismogram getSeismogram(){ return seismogram; }

    public void setVisibility(boolean b){ visible = b;  }

    public void toggleVisibility(){ 
	setVisibility(!visible);
    } 

    public boolean getVisibility(){ return visible; }

    protected DataSetSeismogram seismogram;

    protected boolean visible = true;
    
}// AbstractSeismogramPlotter
