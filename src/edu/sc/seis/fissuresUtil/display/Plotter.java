package edu.sc.seis.fissuresUtil.display;

import java.awt.Shape;
import java.awt.Dimension;

/**
 * Plotters are objects to be put in the main display of a SeismogramDisplay.  
 * 
 * Created: Fri Jun  7 10:27:49 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version 0.1
 * @see BasicPlotter, SeismogramPlotter
 */

public interface Plotter {
    /**
     * This method takes the object to be drawn and returns a shape represetnting
     * it
     *
     */
    public Shape draw(Dimension size);

    public void toggleVisibility();
    
}// Plotter
