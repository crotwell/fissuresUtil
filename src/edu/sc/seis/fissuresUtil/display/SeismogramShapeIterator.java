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

public abstract class SeismogramShapeIterator implements PathIterator {
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

    protected int[][] plot;

    protected int startIndex, endIndex, currentIndex;

    protected AffineTransform at;

}// SeismogramShapeIterator
