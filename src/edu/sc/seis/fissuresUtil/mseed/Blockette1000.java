
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
    
    public Blockette1000(byte[] info) {
	super(info);
	
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
    public byte getEncodingFormat() {return info[0];}
    
    /**
       * Set the value of encodingFormat.
       * @param v  Value to assign to encodingFormat.
       */
    public void setEncodingFormat(byte  v) {
	info[0] = v;
    }
    
    /**
       * Get the value of wordOrder.
       * @return Value of wordOrder.
       */
    public byte getWordOrder() {
	return info[1];
    }
    
    /**
       * Set the value of wordOrder.
       * @param v  Value to assign to wordOrder.
       */
    public void setWordOrder(byte  v) {
	info[1] = v;
    }

    public boolean isBigEndian() {
        if (info[1] == 1) {
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
	return info[2];
    }
    
    /**
       * Get the value of dataRecordLengthByte.
       * @return Value of dataRecordLengthByte.
       */
    public int getDataRecordLength() {
	if (info[2] < 31) {
	    return (0x01 << info[2]);
	} else {
	    throw new RuntimeException("Data Record Length exceeds size of int");
	}
    }
    
    /**
       * Set the value of dataRecordLength.
       * @param v  Value to assign to dataRecordLength.
       */
    public void setDataRecordLength(byte  v) {
	info[2] = v;
    }
    
    /**
       * Get the value of reserved.
       * @return Value of reserved.
       */
    public byte getReserved() {return info[3];}
    
    /**
       * Set the value of reserved.
       * @param v  Value to assign to reserved.
       */
    public void setReserved(byte  v) {info[3] = v;}
    
    public String toString() {
	return super.toString()+"  format="+getEncodingFormat();
    }

} // Blockette1000
