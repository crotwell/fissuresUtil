package edu.sc.seis.fissuresUtil.display;

import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;

/**
 * PlotCompressIterator.java
 *
 *
 * Created: Fri Oct 11 15:58:16 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class PlotCompressIterator extends SeismogramShapeIterator{
    public PlotCompressIterator(int[][] plot, int startIndex, int endIndex, AffineTransform at){
	super.plot = plot;
	super.startIndex = startIndex;
	super.currentIndex = startIndex;
	super.endIndex = endIndex; 
	super.at = at;
    }   

    
    public int currentSegment(float[] coordinates){
	int i = 0;
	if(min){
	    i = 1;
	    currentIndex--;
	}
	min = !min;
	coordinates[0] = currentIndex;
	coordinates[1] = plot[i][currentIndex];
	if(at != null){
	    at.transform(coordinates, 0, coordinates, 0, 1);
	}
	if(currentIndex == startIndex){
	    return SEG_MOVETO;
	}else{
	    return SEG_LINETO;
	}	
    }

    public int currentSegment(double[] coordinates){
	int i = 0;
	if(min){
	    i = 1;
	    currentIndex--;
	}
	min = !min;
	coordinates[0] = currentIndex;
	coordinates[1] = plot[i][currentIndex];
	if(at != null){
        at.transform(coordinates, 0, coordinates, 0, 1);
    }
	if(currentIndex == startIndex){
	    return SEG_MOVETO;
	}else{
	    return SEG_LINETO;
	}	
    }

    private boolean min = false; 
}// PlotCompressIterator
