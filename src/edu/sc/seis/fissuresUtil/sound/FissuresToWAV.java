package edu.sc.seis.fissuresUtil.sound;

import java.io.*;
import edu.sc.seis.fissuresUtil.mseed.Utility;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.UnitImpl;
import org.apache.log4j.*;

/**
 * FissuresToWAV.java
 * @see http://ccrma-www.stanford.edu/CCRMA/Courses/422/projects/WaveFormat/
 *
 *
 * Created: Wed Feb 19 15:35:06 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class FissuresToWAV {
    public FissuresToWAV() {
	
    } // FissuresToWAV constructor
    
    public static void writeWAV(LocalSeismogramImpl seis,
				int speedUp,
				DataOutput out) throws IOException {
	
	out.writeBytes("RIFF");
	int size = 36 + 2*seis.getNumPoints();

	writeLittleEndian(out, size);
	out.writeBytes("WAVE");

	// write fmt subchunk
	out.writeBytes("fmt ");
	writeLittleEndian(out, 16);
	writeLittleEndian(out, (short)1); // linear quantization, PCM
	writeLittleEndian(out, (short)1); // mono, 1 channel
	SamplingImpl sampling = (SamplingImpl)seis.getSampling();
	QuantityImpl freq = sampling.getFrequency();
	freq = freq.convertTo(UnitImpl.HERTZ);
	int samp = (int)(freq.getValue() * speedUp);
	writeLittleEndian(out, samp); // sample rate
	writeLittleEndian(out, samp*2); // byte rate, 2 bytes per sample
	writeLittleEndian(out, (short)2); // block align
	writeLittleEndian(out, (short)16); // bits per sample

	// write data subchunk
	out.writeBytes("data");
	writeLittleEndian(out, seis.getNumPoints()*2); // subchunk2 size

	int[] data = seis.get_as_longs();
	for ( int i=0; i<seis.getNumPoints(); i++) {
	    writeLittleEndian(out, (short)data[i]);	    
	} // end of for ()
	
    }

    protected static void writeLittleEndian(DataOutput out, int value) 
	throws IOException {
	byte[] tmpBytes;
	tmpBytes = Utility.intToByteArray(value);
	out.write(tmpBytes[3]);
	out.write(tmpBytes[2]);
	out.write(tmpBytes[1]);
	out.write(tmpBytes[0]);
    }

    protected static void writeLittleEndian(DataOutput out, short value) 
	throws IOException {
	byte[] tmpBytes;
	tmpBytes = Utility.intToByteArray((int)value);
	out.write(tmpBytes[3]);
	out.write(tmpBytes[2]);
    }
    static Category logger = 
	Category.getInstance(FissuresToWAV.class.getName());

} // FissuresToWAV
