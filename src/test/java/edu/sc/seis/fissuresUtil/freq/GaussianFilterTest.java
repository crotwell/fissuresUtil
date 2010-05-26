/*
 * Created on Mar 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package edu.sc.seis.fissuresUtil.freq;

import junit.framework.TestCase;

/**
 * @author joanna
 *
 * Software developed for IRIS Data Management Center.
 */
public class GaussianFilterTest extends TestCase {
	
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

	
	public void testApplyNarrow() {
		
		GaussianFilter gloFil = 
			new GaussianFilter( new SeisGramText(), 14.0, 2.0);
		Cmplx[] fftdata = Cmplx.fft(vals);
		double dt = 1.0/n;
		Cmplx[] copyData = new Cmplx[fftdata.length];
		System.arraycopy( fftdata, 0, copyData, 0, fftdata.length);
		Cmplx[] filt = gloFil.apply(dt, copyData);
		Cmplx[] Pbyy = new Cmplx[filt.length/8];
		Cmplx[] Pyy = new Cmplx[fftdata.length/8];
		for ( int i = 1; i < 10; i++ ) {
			Pbyy[i] = Cmplx.mul( filt[i], filt[i].conjg() );
			Pyy[i] = Cmplx.mul( fftdata[i], fftdata[i].conjg() );
			//System.out.println(Pbyy[i].real() + " orig: "+Pyy[i].real());
			assertTrue( Pbyy[i].real() < Pyy[i].real());
		}
	}

	public void testApplyWide() {
		
		GaussianFilter gloFil = 
			new GaussianFilter( new SeisGramText(), 40.0, 2.0);
		Cmplx[] fftdata = Cmplx.fft(vals);
		double dt = 1.0/n;
		Cmplx[] copyData = new Cmplx[fftdata.length];
		System.arraycopy( fftdata, 0, copyData, 0, fftdata.length);
		Cmplx[] filt = gloFil.apply(dt, copyData);
		Cmplx[] Pbyy = new Cmplx[filt.length/8];
		Cmplx[] Pyy = new Cmplx[fftdata.length/8];
		// tests based on observation, not solid understanding of
		// how a Gaussian filter performs
		for ( int i = 1; i < 38; i++ ) {
			Pbyy[i] = Cmplx.mul( filt[i], filt[i].conjg() );
			Pyy[i] = Cmplx.mul( fftdata[i], fftdata[i].conjg() );
			//System.out.println(i + " " + Pbyy[i].real() + " orig: "+Pyy[i].real());
			assertTrue( Pbyy[i].real() < Pyy[i].real());
		}
		for ( int i = 70; i < fftdata.length/8; i++ ) {
			Pbyy[i] = Cmplx.mul( filt[i], filt[i].conjg() );
			Pyy[i] = Cmplx.mul( fftdata[i], fftdata[i].conjg() );
			//System.out.println(i + " " + Pbyy[i].real() + " orig: "+Pyy[i].real());
			assertTrue( Pbyy[i].real() < Pyy[i].real());
		}
	}

}
