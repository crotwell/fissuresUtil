
package edu.sc.seis.fissuresUtil.mseed;

/**
 * DataRecord.java
 *
 *
 * Created: Thu Apr  8 13:52:27 1999
 *
 * @author Philip Crotwell
 * @version
 */
import java.io.*;

public class DataRecord extends SeedRecord
    implements Serializable {
    
    public DataRecord(DataHeader header) {
	super(header);
    }
        
    public void addBlockette(Blockette b)
    throws SeedFormatException {
	if (b instanceof DataBlockette) {
	    super.addBlockette(b);
	} else if (b instanceof BlocketteUnknown) {
            System.out.println("BlockettUnknown added: "+b.getType());

	} else {
	    throw new SeedFormatException(
		   "Cannot add non-data blockettes to a DataRecord "+
		   b.getType());
	}
    }

    /** returns the data from this data header unparsed, is as a byte array
     *  in the format from blockette 1000. The return type is byte[], 
     *  so the caller must decode the data based on its format.
    */
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
	this.data = data;
    }

    public int getDataSize() {
        return data.length;
    }

    public DataHeader getHeader() {
	return (DataHeader)header;
    }

    public void write(DataOutputStream dos) throws IOException {
        getHeader().write(dos);
        Blockette[] blockettes = getBlockettes();
        int blockettesSize = 0;
        for ( int i=0; i<blockettes.length; i++) {
            dos.write(blockettes[i].toBytes());
            blockettesSize += blockettes[i].getSize();
        } // end of for ()
        dos.write(data);
        int remainBytes = RECORD_SIZE - header.getSize()
            - blockettesSize - data.length;
        for ( int i=0; i<remainBytes;i++) {
            dos.write(ZERO_BYTE);
        } // end of for ()
    
    }

    public static DataRecord read(DataInputStream inStream)
	throws IOException, SeedFormatException {
	ControlHeader header = ControlHeader.read(inStream);

	if (header instanceof DataHeader) {
	    return readDataRecord(inStream,
				(DataHeader)header);
	} else {
	    throw new SeedFormatException("Found a control header in a miniseed file");
	}
    }

    protected static DataRecord readDataRecord(DataInputStream inStream,
					       DataHeader header)
    throws IOException, SeedFormatException {

/*
	Assert.isTrue(header.getDataBlocketteOffset()>= header.getSize(),
		      "Offset to first blockette must be larger than the header size");
*/
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

    public String toString() {
	String s = "Data "+super.toString();
	s += "\n"+data.length+" bytes of data read.";
	return s;
    }

    protected byte[] data;

    byte ZERO_BYTE = 0;

    int RECORD_SIZE = 4096;

} // DataRecord
