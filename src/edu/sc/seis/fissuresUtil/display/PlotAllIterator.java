package edu.sc.seis.fissuresUtil.display;

import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;

/**
 * PlotAllIterator.java
 *
 *
 * Created: Fri Oct 11 14:35:11 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class PlotAllIterator extends SeismogramShapeIterator{
    public PlotAllIterator (int[][] points, AffineTransform at){
	super.plot = points;
	super.startIndex = 0;
	super.currentIndex = startIndex;
	super.endIndex = points.length; 
	super.at = at;
    }
    
    public int currentSegment(float[] coordinates){
	coordinates[0] = plot[currentIndex][0];
	coordinates[1] = plot[currentIndex][1];
	if(at != null){
	    at.transform(coordinates, 0, coordinates, 0, 1);
	}
	if(currentIndex == 0){
	    return SEG_MOVETO;
	}else{
	    return SEG_LINETO;
	}	
    }

    public int currentSegment(double[] coordinates){
	coordinates[0] = plot[currentIndex][0];
	coordinates[1] = plot[currentIndex][1];
	if(at != null){
	    at.transform(coordinates, 0, coordinates, 0, 1);
	}
	if(currentIndex == 0){
	    return SEG_MOVETO;
	}else{
	    return SEG_LINETO;
	}	
    }
}// PlotAllIterator
