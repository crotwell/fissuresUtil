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
    
    public abstract Shape draw(Dimension size, TimeSnapshot imageState);

    public LocalSeismogram getSeismogram(){ return seismogram; }

    public  AmpConfigRegistrar getAmpConfig(){ return ampConfig; }
    
    public void setVisibility(boolean b){ visible = b; }

    public void toggleVisibility(){ visible = !visible; } 

    protected LocalSeismogram seismogram;

    protected AmpConfigRegistrar ampConfig;

    protected boolean visible = true;
    
}// AbstractSeismogramPlotter
