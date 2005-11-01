/**
 * StdAuxillaryDataNames.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.xml;

public interface StdAuxillaryDataNames
{
    public static final String prefix = StdAuxillaryDataNames.class.getName();

    public static final String RESPONSE = prefix+"/RESPONSE";

    public static final String NETWORK_BEGIN = prefix+"/NETWORK_BEGIN";
    
    public static final String CHANNEL_BEGIN = prefix+"/CHANNEL_BEGIN";
	
	public static final String PICK_FLAG = prefix+"/PICK_FLAG";
    
    public static final String APPROVED = prefix+"/APPROVED";
    
    public static final String S_TO_N = prefix+"/StoN";
}

