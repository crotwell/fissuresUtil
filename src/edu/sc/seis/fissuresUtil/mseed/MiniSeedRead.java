
package edu.sc.seis.fissuresUtil.mseed;

/**
 * MiniSeedRead.java
 *
 *
 * Created: Thu Apr  8 12:10:52 1999
 *
 * @author Philip Crotwell
 * @version
 */

import java.io.*;
import edu.iris.Fissures.utility.Assert;

public class MiniSeedRead  {
    
    public MiniSeedRead(DataInput inStream) 
	throws IOException {
	this.inStream = inStream;
    }

    public void close() throws IOException {
        inStream = null;
    }
    
    /** gets the next logical record int the seed volume. This may not
	exactly correspond to the logical record structure within the 
	volume as "continued" records will be concatinated to avoid 
	partial blockettes. */
    public SeedRecord getNextRecord() 
    throws SeedFormatException, IOException {
	ControlHeader header = ControlHeader.read(inStream);

	if (header instanceof DataHeader) {
	    return readDataRecord((DataHeader)header);
	} else {
throw new SeedFormatException("Found a control record in miniseed");
//	    return readControlRecord(header);

	}
    }

    
    
    /**
       * Get the value of readData.
       * @return Value of readData.
       */
    public boolean isReadData() {return readData;}
    
    /**
       * Set the value of readData.
       * @param v  Value to assign to readData.
       */
    public void setReadData(boolean  v) {this.readData = v;}
    
/*
    protected ControlRecord readControlRecord(ControlHeader header) 
    throws IOException {
	ControlRecord controlRec = new ControlRecord(header);

	// preload first blockette
	AsciiBlockette b = nextControlHeaderBlockette();
	
	

	return controlRec;
    }

    protected AsciiBlockette nextControlHeaderBlockette() 
	throws IOException {
	byte[] typeBytes = new byte[3];
	inStream.readFully(typeBytes);
	String typeString = new String(typeBytes);

	byte[] lengthBytes = new byte[4];
	inStream.readFully(lengthBytes);
	String lengthString = new String(lengthBytes);

	byte[] blocketteBytes = 
	    new byte[Integer.parseInt(lengthString)-7];
	inStream.readFully(blocketteBytes);

	return (AsciiBlockette)
	    Blockette.parseBlockette(Integer.valueOf(typeString
						     ).shortValue(), 
				     blocketteBytes);
    }
*/

    protected DataRecord readDataRecord(DataHeader header)
    throws IOException, SeedFormatException {
	Assert.isTrue(header.getDataBlocketteOffset()>= header.getSize(),
		      "Offset to first blockette must be larger than the header size");
	byte[] garbage = new byte[header.getDataBlocketteOffset()-
				 header.getSize()];

	DataRecord dataRec = new DataRecord(header);

        if (garbage.length != 0) {
            inStream.readFully(garbage);
        }

	byte[] blocketteBytes;
	int currOffset = header.getDataBlocketteOffset();
	int type, nextOffset;
 	for (int i=0; i< header.getNumBlockettes() ; i++) {
	    //get blockette type (first 2 bytes)
	    byte hibyte = inStream.readByte();
	    byte lowbyte = inStream.readByte();
	    type = Utility.uBytesToInt(hibyte, lowbyte, false);
            // System.out.println("Blockette type "+type);

	    hibyte = inStream.readByte();
	    lowbyte = inStream.readByte();
	    nextOffset = Utility.uBytesToInt(hibyte, lowbyte, false);

	    // account for the 4 bytes above
	    currOffset +=  4;

	    if (nextOffset != 0) {
		blocketteBytes = new byte[nextOffset - currOffset];
	    } else if (header.getDataOffset() > currOffset) {
		blocketteBytes = new byte[header.getDataOffset()-
					 currOffset];

	    } else {
		blocketteBytes = new byte[0];
	    }
	    inStream.readFully(blocketteBytes);
            if (nextOffset != 0) {
                currOffset = nextOffset;
            } else {
                currOffset += blocketteBytes.length;
            }

	    Blockette b = Blockette.parseBlockette(type, 
                                                   blocketteBytes);
	    dataRec.addBlockette(b);
	    
	    if (nextOffset == 0) {
		break;
	    }
	}

	Blockette[] allBs = dataRec.getBlockettes(1000);
	if (allBs.length == 0) {
	    // no data
	    throw new SeedFormatException("no blockette 1000");
	} else if (allBs.length > 1) {
	     throw new SeedFormatException(
			   "Multiple blockette 1000s in the volume. "+
			   allBs.length);
	}
        //	System.out.println("allBs.length="+allBs.length);
	Blockette1000 b1000 = (Blockette1000)allBs[0];
        // System.out.println(b1000);

        byte[] timeseries;
        if (header.getDataOffset() == 0) {
            // data record with no data, so gobble up the rest of the record
            timeseries  = new byte[b1000.getDataRecordLength() - currOffset];
        } else {
            timeseries = new byte[ b1000.getDataRecordLength() -
                                 header.getDataOffset() ];
        }
        // System.out.println("getDataRecordLength() = "+ b1000.getDataRecordLength());
        inStream.readFully(timeseries);
        dataRec.setData(timeseries);
	return dataRec;
    }

    protected DataInput inStream;

    protected int recordSize;

    protected boolean readData;

    public static void main(String[] args) {
        DataInputStream ls = null;
        try {
            System.out.println("open socket");

            if (args.length == 0) {
		//                ls = new DataInputStream(
		// edu.iris.Fissures.liss.LissSocket.open("anmo.iu.liss.org"));
            } else {
                ls = new DataInputStream( new BufferedInputStream(
              	   new FileInputStream(args[0])));
            }
	    MiniSeedRead rf = new MiniSeedRead(ls);
	    for (int i=0; i<10; i++) {
		SeedRecord sr = rf.getNextRecord();
		System.out.println(sr);
		if (sr instanceof DataRecord) {
//		    Object data = ((DataRecord)sr).getDecodedData();
//		    if (data instanceof int[]) {
// 			int[] intData = (int[])data;
// 			for (int j=0; j< intData.length; j++) {
// 			    System.out.println(intData[j]);
// 			}
//		    }
                }
	    }
 

        } catch (Exception e) {
           System.out.println(e);
	   e.printStackTrace();
           
        } finally {
            try {
                if (ls != null) ls.close();    
            } catch (Exception ee) {}
        }
    }

} // MiniSeedRead
