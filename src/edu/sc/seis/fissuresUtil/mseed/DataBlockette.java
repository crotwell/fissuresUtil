
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
        System.arraycopy(Utility.intToByteArray(getType()), 2, info, 0, 2);
    }

    public void write(DataOutputStream dos, short nextOffset) throws IOException {
        dos.write(toBytes(nextOffset));
    }

    public byte[] toBytes(short nextOffset) {
        System.arraycopy(Utility.intToByteArray(nextOffset), 2, info, 2, 2);
        return info;
    }

    public byte[] toBytes() {
        return toBytes((short)0);
    }

    protected byte[] info;

} // DataBlockette
