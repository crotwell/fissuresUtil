
package edu.sc.seis.fissuresUtil.mseed;

import java.io.*;

/**
 * DataBlockette.java
 *
 *
 * Created: Thu Apr  8 12:40:56 1999
 *
 * @author Philip Crotwell
 * @version
 */
public abstract class DataBlockette extends Blockette
    implements Serializable {
    
    public DataBlockette(byte[] info) {
	this.info = info;
    }
    
    public DataBlockette(int size) {
	this.info = new byte[size];
    }

    public void write(DataOutputStream dos) throws IOException {
	dos.write(toBytes());
    }
    	
    public byte[] toBytes() {
	return info;
    }

    protected byte[] info;

} // DataBlockette
