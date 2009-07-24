package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.IfSeismogramDC.LocalMotionVector;

/**
 * LocalMotionVectorFunction.java
 *
 *
 * Created: Sun Dec 15 13:38:39 2002
 *
 * @author Philip Crotwell
 * @version $Id: LocalMotionVectorFunction.java 3021 2002-12-16 18:48:10Z crotwell $
 */
public interface LocalMotionVectorFunction {

    public LocalMotionVector apply(LocalMotionVector vec);

} // LocalMotionVectorFunction
