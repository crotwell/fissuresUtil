
package edu.sc.seis.fissuresUtil.mseed;

/**
 * Blockette1000.java
 *
 *
 * Created: Fri Apr  2 14:51:42 1999
 *
 * @author Philip Crotwell
 * @version
 */

import java.io.*;

public class Blockette1000 extends  DataBlockette {

    public static final int B1000_SIZE = 8;
    
    public Blockette1000() {
        super(B1000_SIZE);
    }

    public Blockette1000(byte[] info) {
        super(info);
        if (info.length < B1000_SIZE) {
            throw new IllegalArgumentException("Blockette 1000 must have 8 bytes, but got "+info.length);
        }
    }

    public int getSize() {
        return 8;
    }

    public int getType() { return 1000; }

    public String getName() {
        return "Data Only SEED Blockette";
    }

    /**
     * Get the value of encodingFormat.
     * @return Value of encodingFormat.
     */
    public byte getEncodingFormat() {return info[4];}

    /**
     * Set the value of encodingFormat.
     * @param v  Value to assign to encodingFormat.
     */
    public void setEncodingFormat(byte  v) {
        info[4] = v;
    }

    /**
     * Get the value of wordOrder.
     * @return Value of wordOrder.
     */
    public byte getWordOrder() {
        return info[5];
    }

    /**
     * Set the value of wordOrder.
     * @param v  Value to assign to wordOrder.
     */
    public void setWordOrder(byte  v) {
        info[5] = v;
    }

    public boolean isBigEndian() {
        if (info[5] == 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isLittleEndian() {
        return ! isBigEndian();
    }
    /**
     * Get the value of dataRecordLengthByte.
     * @return Value of dataRecordLengthByte.
     */
    public byte getDataRecordLengthByte() {
        return info[6];
    }

    /**
     * Get the value of dataRecordLengthByte.
     * @return Value of dataRecordLengthByte.
     */
    public int getDataRecordLength() {
        if (getDataRecordLengthByte() < 31) {
            return (0x01 << getDataRecordLengthByte());
        } else {
            throw new RuntimeException("Data Record Length exceeds size of int");
        }
    }

    /**
     * Set the value of dataRecordLength.
     * @param v  Value to assign to dataRecordLength.
     */
    public void setDataRecordLength(byte  v) {
        info[6] = v;
    }

    /**
     * Get the value of reserved.
     * @return Value of reserved.
     */
    public byte getReserved() {return info[7];}

    /**
     * Set the value of reserved.
     * @param v  Value to assign to reserved.
     */
    public void setReserved(byte  v) {info[7] = v;}

    public void writeASCII(Writer out) throws IOException {
        out.write("Blockette1000 "+getEncodingFormat()+" "+getWordOrder()+" "+getDataRecordLengthByte());
    }

    public String toString() {
        return super.toString()+"  format="+getEncodingFormat();
    }

} // Blockette1000
