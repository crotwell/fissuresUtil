package edu.sc.seis.fissuresUtil.freq;

import junit.framework.TestCase;
// JUnitDoclet end import

/**
* Generated by JUnitDoclet, a tool provided by
* ObjectFab GmbH under LGPL.
* Please see www.junitdoclet.org, www.gnu.org
* and www.objectfab.de for informations about
* the tool, the licence and the authors.
*/


public class CmplxTest
// JUnitDoclet begin extends_implements
extends TestCase
// JUnitDoclet end extends_implements
{
  // JUnitDoclet begin class
    edu.sc.seis.fissuresUtil.freq.Cmplx cmplx = null;
  // JUnitDoclet end class

  public CmplxTest(String name) {
    // JUnitDoclet begin method RTrendTest
    super(name);
    // JUnitDoclet end method RTrendTest
  }

  public edu.sc.seis.fissuresUtil.freq.Cmplx createInstance() throws Exception {
    // JUnitDoclet begin method testcase.createInstance
      return new edu.sc.seis.fissuresUtil.freq.Cmplx();
    // JUnitDoclet end method testcase.createInstance
  }

  protected void setUp() throws Exception {
      // JUnitDoclet begin method testcase.setUp
      super.setUp();
      cmplx = createInstance();
      // JUnitDoclet end method testcase.setUp
  }

  protected void tearDown() throws Exception {
    // JUnitDoclet begin method testcase.tearDown
    cmplx = null;
    super.tearDown();
    // JUnitDoclet end method testcase.tearDown
  }

  public void testCorrelate() throws Exception {
    // JUnitDoclet begin method apply
      float[] inA = new float[30];
      float[] inB = new float[30];
      inA[5] = 1;
      inB[3] = 1;
      float[] out = Cmplx.correlate(inA, inB);

      assertEquals("float", 1.0f, out[2], 0.0000001);
    // JUnitDoclet end method apply
  } 
  
  public void testSignalCorrelate() throws Exception {
    // JUnitDoclet begin method apply
  	int n = 2000;
  	java.util.Random rand = new java.util.Random();
  	float[] sinWave = new float[n];
  	float[] cosWave = new float[n];
	for (int i=0; i < n; i++) {
 		sinWave[i] = (float) ( Math.sin( 2.0*Math.PI*i/20 ) );
 		cosWave[i] = (float) ( Math.cos( 2.0*Math.PI*i/20 ) );
	}     
	float[] corr = Cmplx.correlate( sinWave, cosWave );
	// Returns a negatively amplified sine wave
	float ampFactor = corr[5]/sinWave[5];
	for ( int i = 0; i < 20 ; i++) {
		// require a larger delta due to processing noise
		assertEquals( "unexpected value for correlation",
					  sinWave[i], corr[i]/ampFactor, 0.01);  
	}
  // JUnitDoclet end method apply
}

  public void testCorrelateTriangle() throws Exception {
    // JUnitDoclet begin method apply
      float[] inA = new float[30];
      float[] inB = new float[30];
      inA[5] = .5f;
      inA[6] = 1;
      inA[7] = .5f;
      inB[2] = .05f;
      inB[3] = .1f;
      inB[4] = .05f;
      float[] out = Cmplx.correlate(inA, inB);

      assertEquals("float", .15f, out[3], 0.0000001);
    // JUnitDoclet end method apply
  }

  public void testConvolve() throws Exception {
    // JUnitDoclet begin method apply
      float[] inA = new float[30];
      float[] inB = new float[30];
      inA[6] = 1;
      inB[3] = .1f;
      float[] out = Cmplx.convolve(inA, inB, 1);

      assertEquals("float", .1f, out[9], 0.0000001);
    // JUnitDoclet end method apply
  }

  public void testConvolveSpike() throws Exception {
    // JUnitDoclet begin method apply
      float[] inA = new float[30];
      float[] inB = new float[30];
      for ( int i=0; i<inA.length; i++) {
          inA[i] = 2;
      }
      inB[3] = .1f;
      float[] out = Cmplx.convolve(inA, inB, 1);

      assertEquals("float", .2f, out[9], 0.0000001);
      assertEquals("float", .2f, out[8], 0.0000001);
      assertEquals("float", .2f, out[10], 0.0000001);
    // JUnitDoclet end method apply
  }
  
  
  public void testFftSignal() {
    // JUnitDoclet begin method apply
  	java.util.Random rand = new java.util.Random();
  	int n = 1024;
	float[] vals_lo = new float[n];
	float[] vals_hi = new float[n];
	float[] vals_comb = new float[n];
	int loPeak = 76;
	int hiPeak = 19;
	for (int i=0; i < n; i++) {
		float ri = rand.nextFloat();
 		vals_lo[i] = (float) ( 0.25*Math.sin( 2.0*Math.PI*(i/loPeak) ) ) + 0.5f*ri;
 		vals_hi[i] = (float) ( 0.25*Math.sin( 2.0*Math.PI*(i/hiPeak) ) ) + 0.5f*ri;
 		vals_comb[i] = vals_lo[i] + vals_hi[i];
	}

	Cmplx[] fft_lo = Cmplx.fft( vals_lo );
	Cmplx[] fft_hi = Cmplx.fft( vals_hi );
	Cmplx[] fft_comb = Cmplx.fft( vals_comb );

	// test the transitivity of the resulting fft at least...
	for ( int i = 0; i < n/2; i++ ) {
		assertEquals( fft_comb[i].real(), fft_hi[i].real() + fft_lo[i].real(), 0.0001);
		assertEquals( fft_comb[i].imag(), fft_hi[i].imag() + fft_lo[i].imag(), 0.0001);
	}
	
	// JUnitDoclet end method apply
  }

  /**
  * JUnitDoclet moves marker to this method, if there is not match
  * for them in the regenerated code and if the marker is not empty.
  * This way, no test gets lost when regenerating after renaming.
  * Method testVault is supposed to be empty.
  */
  public void testVault() throws Exception {
    // JUnitDoclet begin method testcase.testVault
    // JUnitDoclet end method testcase.testVault
  }

  public static void main(String[] args) {
    // JUnitDoclet begin method testcase.main
    junit.textui.TestRunner.run(CmplxTest.class);
    // JUnitDoclet end method testcase.main
  }
}