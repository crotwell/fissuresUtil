package edu.sc.seis.fissuresUtil.sound;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.SeismogramIterator;
import edu.sc.seis.fissuresUtil.mseed.Utility;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import org.apache.log4j.Category;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;

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

	private int chunkSize, numChannels, sampleRate, speedUp, bitsPerSample,
		blockAlign, byteRate, subchunk2Size;
	private Clip clip;
	private SeismogramIterator iterator;

    public FissuresToWAV(SeismogramIterator iterator, int speedUp) {
		this.iterator = iterator;
		chunkSize = 36 + 2*iterator.getNumPoints();
		numChannels = 1;
		bitsPerSample = 16;
		blockAlign = numChannels * (bitsPerSample/8);
		subchunk2Size = iterator.getNumPoints() * blockAlign;
		setSpeedUp(speedUp);
    }

	public void writeWAV(DataOutput out) throws IOException {
		writeChunkData(out);
		writeWAVData(out);
    }

	public void play(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			writeWAVData(dos);
		}
		catch(IOException e){
			e.printStackTrace();
		}

		if (clip != null) clip.close();
		Clip clip = null;
		AudioFormat audioFormat = new AudioFormat(sampleRate, 16, 1, true, false);
		DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
		if (!AudioSystem.isLineSupported(info)) {
			System.out.println("Line not supported, apparently...");
		}
		// Obtain and open the line.
		try {
			clip = (Clip) AudioSystem.getLine(info);
			byte[] data = baos.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			AudioInputStream ais = new AudioInputStream(bais, audioFormat, data.length);
			//clip.open(audioFormat, data, 0, 100);
			try{
				clip.open(ais);
				clip.start();
			}
			catch(IOException e){
				e.printStackTrace();
			}
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
		}

		try{
			baos.close();
			dos.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	public void setSpeedUp(int newSpeed){
		speedUp = newSpeed;
		sampleRate = calculateSampleRate(iterator.getSampling(), newSpeed);
		byteRate = sampleRate * blockAlign;
	}

	private void writeChunkData(DataOutput out) throws IOException{
		out.writeBytes("RIFF"); //ChunkID

		//ChunkSize
		writeLittleEndian(out, chunkSize);

		out.writeBytes("WAVE"); //Format

		// write fmt subchunk
		out.writeBytes("fmt "); //Subchunk1ID
		writeLittleEndian(out, 16); //Subchunk1Size
		writeLittleEndian(out, (short)1); // Audioformat = linear quantization, PCM
		writeLittleEndian(out, (short)numChannels); // NumChannels
		writeLittleEndian(out, sampleRate); // SampleRate
		writeLittleEndian(out, byteRate); // byte rate
		writeLittleEndian(out, (short)blockAlign); // block align
		writeLittleEndian(out, (short)bitsPerSample); // bits per sample

		// write data subchunk
		out.writeBytes("data");
		writeLittleEndian(out, subchunk2Size); // subchunk2 size
	}

	private void writeWAVData(DataOutput out) throws IOException{

		//calculate maximum amplification factor to avoid either
		//clipping or dead quiet
		int amplification = (int)(24000.0/iterator.minMaxMean()[1]);

		while (iterator.hasNext()){
			try{
				QuantityImpl next = (QuantityImpl)iterator.next();
				writeLittleEndian(out, (short)(amplification * next.getValue()));
			}
			catch(NullPointerException e){
				writeLittleEndian(out, (short)0);
			}
			catch(ArrayIndexOutOfBoundsException e){
				writeLittleEndian(out, (short)0);
			}
		}
	}

	public static int calculateSampleRate(SamplingImpl sampling, int speedUp){
		System.out.println(sampling);
		QuantityImpl freq = sampling.getFrequency();
		freq = freq.convertTo(UnitImpl.HERTZ);
		return (int)(freq.getValue() * speedUp);
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
