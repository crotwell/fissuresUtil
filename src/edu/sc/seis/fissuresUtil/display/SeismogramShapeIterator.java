package edu.sc.seis.fissuresUtil.display;

import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;
/**
 * SeismogramShapeIterator.java
 *
 *
 * Created: Sun Jul 28 21:38:56 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class SeismogramShapeIterator implements PathIterator {
    public SeismogramShapeIterator(int[][] plot, int startIndex, int endIndex, AffineTransform at){
	this.plot = plot;
	this.startIndex = startIndex;
	currentIndex = startIndex;
	this.endIndex = endIndex; 
	this.at = at;
    }
 
    public void next(){
	currentIndex++;
    }

    public int getWindingRule(){
 	return WIND_NON_ZERO;
    }

    public boolean isDone(){
	if(currentIndex == endIndex){
	    return true;
	}
	return false;
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

    private int[][] plot;

    private int startIndex, endIndex, currentIndex;

    private AffineTransform at;

}// SeismogramShapeIterator
