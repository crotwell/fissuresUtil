package edu.sc.seis.fissuresUtil.sound;

import java.io.*;
import edu.sc.seis.fissuresUtil.mseed.Utility;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

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
				OutputStream out) {
	/*
	out.write("RIFF");
	int size = 0;

	writeLittleEndian(out, size);
	out.write("WAVE");

	// write fmt subchunk
	out.write("fmt ");
	writeLittleEndian(out, 16);
	writeLittleEndian(out, (short)1); // linear quantization, PCM
	writeLittleEndian(out, (short)1); // mono, 1 channel
	writeLittleEndian(out, samp); // sample rate
	writeLittleEndian(out, samp*2); // byte rate, 2 bytes per sample
	writeLittleEndian(out, (short)2); // block align
	writeLittleEndian(out, (short)16); // bits per sample

	// write data subchunk
	out.write("data");
	writeLittleEndian(out, num_points*2); // subchunk2 size

	int[] data = seis.get_as_longs();
	for ( int i=0; i<seis.getNumPoints(); i++) {
	    writeLittleEndian(out, (short)data[i]);	    
	} // end of for ()
	*/
    }

    protected static void writeLittleEndian(OutputStream out, int value) 
	throws IOException {
	byte[] tmpBytes;
	tmpBytes = Utility.intToByteArray(value);
	out.write(tmpBytes[0]);
	out.write(tmpBytes[1]);
	out.write(tmpBytes[2]);
	out.write(tmpBytes[3]);
    }

    protected static void writeLittleEndian(OutputStream out, short value) 
	throws IOException {
	byte[] tmpBytes;
	tmpBytes = Utility.intToByteArray(value);
	out.write(tmpBytes[0]);
	out.write(tmpBytes[1]);
    }

} // FissuresToWAV
