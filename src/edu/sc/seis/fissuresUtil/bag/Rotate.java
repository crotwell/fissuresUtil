package edu.sc.seis.fissuresUtil.bag;

/**
 * Rotate.java
 *
 *
 * Created: Sun Dec 15 13:43:21 2002
 *
 * @author Philip Crotwell
 * @version $Id: Rotate.java 3013 2002-12-15 18:45:49Z crotwell $
 */
public class Rotate implements LocalMotionVectorFunction {

    public Rotate() {
	
    } // Rotate constructor
    
    public LocalMotionVector apply(LocalmotionVector vec) {
	return vec;
    }

} // Rotate
