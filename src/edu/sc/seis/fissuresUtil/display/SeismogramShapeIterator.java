package edu.sc.seis.fissuresUtil.display;

import java.awt.geom.PathIterator;

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
    public SeismogramShapeIterator(int[][] plot, int startIndex, int endIndex){
	this.plot = plot;
	this.startIndex = startIndex;
	currentIndex = startIndex;
	this.endIndex = endIndex;    
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
	if(currentIndex == startIndex){
	    return SEG_MOVETO;
	}else{
	    return SEG_LINETO;
	}	
    }

    protected boolean min = false;

    protected int[][] plot;

    protected int startIndex, endIndex, currentIndex;

}// SeismogramShapeIterator
