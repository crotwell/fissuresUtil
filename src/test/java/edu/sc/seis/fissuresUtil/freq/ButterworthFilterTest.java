/*
 * Created on Feb 25, 2005
 *
 */
package edu.sc.seis.fissuresUtil.freq;

import junit.framework.TestCase;

/**
 * @author joanna
 *
 * Software developed for IRIS Data Management Center.
 */
public class ButterworthFilterTest extends TestCase {

	float[] vals; // time series values
	int n = 1024; // length of time series
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		vals = new float[n];
	  	java.util.Random rand = new java.util.Random();
		for (int i=0; i < n; i++) 
			vals[i] = rand.nextFloat() + (float) (Math.sin( 2.0*Math.PI*(i/200)) +
								Math.sin( 2.0*Math.PI*(i/14) ));
	}

	public void testApplyHigh() {
		
		ButterworthFilter hiFil = 
			new ButterworthFilter( 20, 100, 8);
		Cmplx[] fftdata = Cmplx.fft(vals);
		double dt = 1.0/n;
		Cmplx[] copyData = new Cmplx[fftdata.length];
		System.arraycopy( fftdata, 0, copyData, 0, fftdata.length);
		Cmplx[] filt = hiFil.apply(dt, copyData);
		Cmplx[] Pbyy = new Cmplx[filt.length/8];
		Cmplx[] Pyy = new Cmplx[fftdata.length/8];
		for ( int i = 0; i < 16; i++ ) {
			Pbyy[i] = Cmplx.mul( filt[i], filt[i].conjg() );
			Pyy[i] = Cmplx.mul( fftdata[i], fftdata[i].conjg() );
			assertTrue( Pbyy[i].real() < Pyy[i].real());
		}
	}
	
	public void testApplyLo() {
		
		ButterworthFilter loFil = 
			new ButterworthFilter( 0.1, 20, 8);
		Cmplx[] fftdata = Cmplx.fft(vals);
		double dt = 1.0/n;
		Cmplx[] copyData = new Cmplx[fftdata.length];
		System.arraycopy( fftdata, 0, copyData, 0, fftdata.length);
		Cmplx[] filt = loFil.apply(dt, copyData);
		Cmplx[] Pbyy = new Cmplx[filt.length/8];
		Cmplx[] Pyy = new Cmplx[fftdata.length/8];
		for ( int i = 21; i < fftdata.length/8; i++ ) {
			Pbyy[i] = Cmplx.mul( filt[i], filt[i].conjg() );
			Pyy[i] = Cmplx.mul( fftdata[i], fftdata[i].conjg() );
			//System.out.println(Pbyy[i].real() + " orig: "+Pyy[i].real());
			assertTrue( Pbyy[i].real() < Pyy[i].real());
		}
	}

}
