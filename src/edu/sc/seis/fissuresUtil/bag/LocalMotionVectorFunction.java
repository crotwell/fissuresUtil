package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.SeismogramDC.LocalMotionVector;

/**
 * LocalMotionVectorFunction.java
 *
 *
 * Created: Sun Dec 15 13:38:39 2002
 *
 * @author Philip Crotwell
 * @version $Id: LocalMotionVectorFunction.java 3013 2002-12-15 18:45:49Z crotwell $
 */
public interface LocalMotionVectorFunction {

    public LocalMotionVector apply(LocalMotionVector vec);

} // LocalMotionVectorFunction
