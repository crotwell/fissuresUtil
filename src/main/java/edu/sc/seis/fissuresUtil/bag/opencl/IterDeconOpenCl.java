package edu.sc.seis.fissuresUtil.bag.opencl;

import static org.bridj.Pointer.allocateFloats;
import static org.bridj.Pointer.allocateInts;

import java.io.IOException;
import java.nio.ByteOrder;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.opencl.LocalSize;
import com.nativelibs4java.opencl.util.OpenCLType;
import com.nativelibs4java.opencl.util.ParallelMath;
import com.nativelibs4java.opencl.util.ReductionUtils;
import com.nativelibs4java.opencl.util.fft.FloatFFTPow2;
import com.nativelibs4java.util.IOUtils;

import edu.sc.seis.fissuresUtil.bag.IterDecon;
import edu.sc.seis.fissuresUtil.bag.IterDeconResult;


/**
 * 
 * OpenCL reworking of the Java version of the fortran iterdecon by Chuck Ammon
 * http://eqseis.geosc.psu.edu/~cammon/HTML/RftnDocs/rftn01.html
 * 
 * Also see 
 * Ligorria, J., and C. Ammon, Iterative deconvolution and receiver-function estimation, 
 * Bull., Seis. Soc. Am., 89 (5), 1395-1400, 1999. 
 *
 */

public class IterDeconOpenCl {
    
    public IterDeconOpenCl (int maxBumps,
                      boolean useAbsVal,
                      float tol,
                      float gwidthFactor) throws IOException {
        this.maxBumps = maxBumps;
        this.useAbsVal = useAbsVal;
        this.tol = tol;
        this.gwidthFactor = gwidthFactor;
        context = JavaCL.createBestContext();
        queue = context.createDefaultQueue();
        setUpKernels(context);
    }

    public FloatArrayResult makeCLBuffer(float[] data) {
        ByteOrder byteOrder = context.getByteOrder();
        int n = nextPowerTwo(data.length);
        Pointer<Float> dataCl = allocateFloats(n).order(byteOrder);
        dataCl.setFloats(data);
        CLBuffer<Float> dataClBuf = context.createBuffer(CLMem.Usage.Input, dataCl, true);
        return new FloatArrayResult(dataClBuf);
    }

    public IntArrayResult makeCLBuffer(int[] data) {
        ByteOrder byteOrder = context.getByteOrder();
        int n = nextPowerTwo(data.length);
        Pointer<Integer> dataCl = allocateInts(n).order(byteOrder);
        dataCl.setInts(data);
        CLBuffer<Integer> dataClBuf = context.createBuffer(CLMem.Usage.Input, dataCl, true);
        return new IntArrayResult(dataClBuf);
    }
    
    public IterDeconResult process(float[] numerator,
                                   float[] denominator,
                                   float delta) throws ZeroPowerException {
        numerator = makePowerTwo(numerator);
        denominator = makePowerTwo(denominator);

        FloatArrayResult numeratorClBuf = makeCLBuffer(numerator);
        FloatArrayResult denominatorClBuf = makeCLBuffer(denominator);
        
        // Now begin the cross-correlation procedure
        // Put the filter in the signals
         
        FloatArrayResult numeratorGauss  = gaussianFilter(numeratorClBuf, gwidthFactor, delta);
        FloatArrayResult denominatorGauss  = gaussianFilter(denominatorClBuf, gwidthFactor, delta);

        // compute the power in the "numerator" for error scaling
        FloatResult powerNumerator = power(numeratorGauss);
        FloatResult powerDemoninator = power(denominatorGauss);
        float powerNumeratorFlt = powerNumerator.getAfterWait();
        float powerDemoninatorFlt = powerDemoninator.getAfterWait();
        if (powerNumeratorFlt == 0 || powerDemoninatorFlt == 0) {
            throw new ZeroPowerException("Power of numerator and denominator must be non-zero: num="+powerNumeratorFlt+" denom="+powerDemoninatorFlt);
        }
        FloatArrayResult residual = numeratorGauss;
        FloatArrayResult ampsClBuf = new FloatArrayResult(context.createBuffer(CLMem.Usage.InputOutput, Float.class, maxBumps));
        IntArrayResult shiftsClBuf = new IntArrayResult(context.createBuffer(CLMem.Usage.InputOutput, Integer.class, maxBumps));
        
        float improvement = 100;
        int bump;
        int n = (int)denominatorClBuf.getSize();
        FloatArrayResult predicted = makeCLBuffer(new float[n]);
        float prevPower = powerNumeratorFlt;
        for ( bump=0; bump < maxBumps && improvement > tol ; bump++) {

            // correlate the signals
            FloatArrayResult correlateNormResult = correlateNorm(residual, denominatorGauss);

            //  find the peak in the correlation
            if (useAbsVal) {
                CLEvent maxSpikeEvent = calcMaxSpike(correlateNormResult, ampsClBuf, shiftsClBuf, bump, delta);
                //need maxSpikeEvent to go with shifts and amps
                ampsClBuf = new FloatArrayResult(ampsClBuf.getResult(), maxSpikeEvent);
                shiftsClBuf = new IntArrayResult(shiftsClBuf.getResult(), maxSpikeEvent);
            } else {
                throw new RuntimeException("only useAbsValue supported now");
                //shifts[bump] = getMaxIndex(corr);
            } // end of else
            
            System.out.println("calc max bump of "+ampsClBuf.getAfterWait(queue)[bump]+"  at "+shiftsClBuf.getAfterWait(queue)[bump]);

            
            predicted = buildDecon(ampsClBuf, shiftsClBuf, bump, n, gwidthFactor, delta);
            FloatArrayResult predConvolve;
            if (useNativeFFT) {
                throw new RuntimeException("NativeFFT not implemented");
                //predConvolve = NativeFFT.convolve(predicted, denominator, dt);
            } else {
                predConvolve = convolve(predicted, denominatorClBuf, delta);
            }

            residual = getResidual(numeratorGauss, predConvolve);
            float residualPower = power(residual).getAfterWait();
            improvement = 100*(prevPower-residualPower)/powerNumeratorFlt;
            prevPower = residualPower;
 
        } // end of for (int bump=0; bump < maxBumps; bump++)

        
        IterDeconResult result = new IterDeconResult(maxBumps,
                                                     useAbsVal,
                                                     tol,
                                                     gwidthFactor,
                                                     numerator,
                                                     denominator,
                                                     delta,
                                                     ampsClBuf.getAfterWait(queue),
                                                     shiftsClBuf.getAfterWait(queue),
                                                     residual.getAfterWait(queue),
                                                     predicted.getAfterWait(queue),
                                                     new float[0][0], //corrSave,
                                                     buildSpikes(ampsClBuf, shiftsClBuf, bump, n).getAfterWait(queue),
                                                     prevPower,
                                                     powerNumeratorFlt,
                                                     bump);
        return result;
    }

    void setUpKernels(CLContext context) throws IOException {
        System.getProperties().setProperty("javacl.debug", "true");
        String src = IOUtils.readText(IterDeconOpenCl.class.getResource("IterDecon.cl"));
        CLProgram program = context.createProgram(src);
        sqrFloatsKernel = program.createKernel("sqr_floats");
        gaussianFilterKernel = program.createKernel("gaussianFilter");
        correlateKernel = program.createKernel("correlate");
        convolveKernel = program.createKernel("convolve");
        indexReduceAbsMax = program.createKernel("indexReduceAbsMax");
        phaseShift = program.createKernel("phase_shift");
        fft = new FloatFFTPow2(context);
        ParallelMath pmath = new ParallelMath(queue);
        scalarDiv = program.createKernel("scalar_div");
        scalarMul = program.createKernel("scalar_mul");
        floats_to_complex = program.createKernel("floats_to_complex");
        complex_to_floats = program.createKernel("complex_to_floats");
        shortenFFT  = program.createKernel("shortenFFT");
        lengthenFFT  = program.createKernel("lengthenFFT");
       // buildSpikes  = program.createKernel("buildSpikes");
        subtract_floats  = program.createKernel("subtract_floats");
    }

    protected FloatArrayResult correlateNorm(FloatArrayResult residual, FloatArrayResult denominatorGaussClBuf) {
        int size = (int)residual.getSize();
        FloatResult zeroLag = power(denominatorGaussClBuf);
        
        FloatArrayResult residualFFT = forwardFFT(residual);
        FloatArrayResult denomFFT = forwardFFT(denominatorGaussClBuf);
        
        CLBuffer<Float> correlationFFTBuf = context.createBuffer(CLMem.Usage.InputOutput, Float.class, size);
        correlateKernel.setArgs(residualFFT.getResult(), denomFFT.getResult(), correlationFFTBuf, new int[] {(int)size});
        
        CLEvent correlateEvent = correlateKernel.enqueueNDRange(queue, new int[] {(int)size/2}, CLEventResult.combineEvents(residualFFT.getEventsToWaitFor(), denomFFT.getEventsToWaitFor()));
        FloatArrayResult correlationFFT = new FloatArrayResult(correlationFFTBuf, correlateEvent);
        FloatArrayResult outFFT = inverseFFT(correlationFFT);

        CLBuffer<Float> clBufOut = context.createBuffer(CLMem.Usage.InputOutput, Float.class, size);
        scalarDiv.setArgs(outFFT.getResult(), zeroLag.getAfterWait(), clBufOut, size);
        CLEvent normEvent = scalarDiv.enqueueNDRange(queue, new int[] {(int)clBufOut.getElementCount()}, CLEventResult.combineEvents(zeroLag.getEventsToWaitFor(), outFFT.getEventsToWaitFor()));
        return new FloatArrayResult(clBufOut, normEvent);
        
    }
    
    public FloatArrayResult convolve(FloatArrayResult x, FloatArrayResult y, float delta) {
        int size = (int)x.getSize();
        
        FloatArrayResult xFFT = forwardFFT(x);
        FloatArrayResult yFFT = forwardFFT(y);
        
        CLBuffer<Float> convolveFFTBuf = context.createBuffer(CLMem.Usage.InputOutput, Float.class, size);
        convolveKernel.setArgs(xFFT.getResult(), yFFT.getResult(), convolveFFTBuf, new int[] {(int)size});
        
        CLEvent correlateEvent = convolveKernel.enqueueNDRange(queue, new int[] {(int)size/2}, CLEventResult.combineEvents(xFFT.getEventsToWaitFor(), yFFT.getEventsToWaitFor()));
        FloatArrayResult correlationFFT = new FloatArrayResult(convolveFFTBuf, correlateEvent);
        FloatArrayResult outFFT = inverseFFT(correlationFFT);

        CLBuffer<Float> clBufOut = context.createBuffer(CLMem.Usage.InputOutput, Float.class, size);
        scalarMul.setArgs(outFFT.getResult(), delta, clBufOut, size);
        CLEvent normEvent = scalarMul.enqueueNDRange(queue, new int[] {(int)clBufOut.getElementCount()}, outFFT.getEventsToWaitFor());
        return new FloatArrayResult(clBufOut, normEvent);
    }

    public FloatArrayResult buildSpikes(FloatArrayResult ampsClBuf, IntArrayResult shiftsClBuf, int bump, int n) {
        
        // fall back to regualr IterDecon on cpu
        float[] amps = ampsClBuf.getAfterWait(queue);
        int[] shifts = shiftsClBuf.getAfterWait(queue);
        return makeCLBuffer(IterDecon.buildSpikes(amps, shifts, n));
        
        /*CLBuffer<Float> clBufOut = context.createBuffer(CLMem.Usage.InputOutput, Float.class, n);
        buildSpikes.setArgs(ampsClBuf.getResult(), shiftsClBuf.getResult(), clBufOut, n, bump, LocalSize.ofFloatArray(buildSpikes.getWorkGroupSize().get(queue.getDevice())));
        CLEvent buildSpikesEvent = buildSpikes.enqueueNDRange(queue, new int[] {(int)clBufOut.getElementCount()}, CLEventResult.combineEvents(ampsClBuf.getEventsToWaitFor(),  shiftsClBuf.getEventsToWaitFor()));
        return new FloatArrayResult(clBufOut, buildSpikesEvent);*/
    }

    public FloatArrayResult buildDecon(FloatArrayResult ampsClBuf, IntArrayResult shiftsClBuf, int bump, int n, float gwidthFactor, float dt) {
        return gaussianFilter(buildSpikes(ampsClBuf, shiftsClBuf, bump, n), gwidthFactor, dt);
    }

    /** returns the residual, ie x-y */
    public FloatArrayResult getResidual(FloatArrayResult x, FloatArrayResult y) {
        CLBuffer<Float> clBufOut = context.createBuffer(CLMem.Usage.InputOutput, Float.class, x.getSize());
        subtract_floats.setArgs(x.getResult(), y.getResult(), clBufOut, x.getSize());
        CLEvent subtractEvent = subtract_floats.enqueueNDRange(queue, new int[] {(int)x.getSize()}, CLEventResult.combineEvents(x.getEventsToWaitFor(),  y.getEventsToWaitFor()));
        return new FloatArrayResult(clBufOut, subtractEvent);
    }

    public CLEvent calcMaxSpike(FloatArrayResult corrClBuf, FloatArrayResult ampsClBuf, IntArrayResult shiftsClBuf, int bump, float delta, CLEvent... waitForEvent) {
        int workGroupSize = (int)Math.min(128, indexReduceAbsMax.getWorkGroupSize().get(queue.getDevice()).intValue());
        int globalWorkSize = (int)Math.min(queue.getDevice().getMaxComputeUnits()*4, workGroupSize);
        LocalSize sharedMemSize = LocalSize.ofFloatArray(globalWorkSize/workGroupSize);
        LocalSize sharedMemIndexSize = LocalSize.ofIntArray(globalWorkSize/workGroupSize);
        // first iteration gets us down to globalWorkSize elements
        indexReduceAbsMax.setArgs(corrClBuf.getResult(),
                               (int)corrClBuf.getSize()/2, // only do first half to avoid neg lag
                               sharedMemSize,
                               sharedMemIndexSize,
                               ampsClBuf.getResult(),
                               shiftsClBuf.getResult(),
                               bump,
                               delta);
        CLEvent maxBumpEvent = indexReduceAbsMax.enqueueNDRange(queue, new int[] {globalWorkSize}, new int[] {workGroupSize}, waitForEvent);
        return maxBumpEvent;
    }
    
    public static int getAbsMaxIndex(float[] data) {
        int minIndex = getMinIndex(data);
        int maxIndex = getMaxIndex(data);
        if (Math.abs(data[minIndex]) > Math.abs(data[maxIndex])) {
            return minIndex;
        } // end of if (Math.abs(data[minIndex]) > Math.abs(data[maxIndex]))
        return maxIndex;
    }

    public static final int getMinIndex(float[] data) {
        int index = 0;
        for (int i=1; i<data.length/2; i++) {
            if (data[i] < data[index]) {
                index = i;
            }
        }
        return index;
    }

    public static final int getMaxIndex(float[] data) {
        int index = 0;
        for (int i=1; i<data.length/2; i++) {
            if (data[i] > data[index]) {
                index = i;
            }
        }
        return index;
    }


    public final FloatResult power(FloatArrayResult clBuf, CLEvent... eventsToWaitFor) {
        int n = (int)clBuf.getSize();
        Pointer<Float> result = allocateFloats(1).order(context.getByteOrder());
        CLBuffer<Float> clBufTmp = context.createBuffer(CLMem.Usage.InputOutput, Float.class, n);
        sqrFloatsKernel.setArgs(clBuf.getResult(), clBufTmp, n);
        CLEvent numSqrEvent = sqrFloatsKernel.enqueueNDRange(queue, new int[] {n}, eventsToWaitFor);
        ReductionUtils.Reductor<Float> reductor = ReductionUtils.createReductor(context, 
                                                                                ReductionUtils.Operation.Add, 
                                                                                OpenCLType.Float, 
                                                                                1);
        CLEvent reduceNumEvent = reductor.reduce(queue, clBufTmp, n, result, 2, numSqrEvent);
        return new FloatResult(result, reduceNumEvent);
        
    }

    /** convolve a function with a unit-area Gaussian filter.
     *   G(w) = exp(-w^2 / (4 a^2))
     *  The 1D gaussian is: f(x) = 1/(2*PI*sigma) e^(-x^2/(q * sigma^2))
     *  and the impluse response is: g(x) = 1/(2*PI)e^(-sigma^2 * u^2 / 2)
     *
     * If gwidthFactor is zero, does not filter.
     */
    public FloatArrayResult gaussianFilter(FloatArrayResult  x,
                                           float gwidthFactor,
                                           float dt) {
        int realSize = (int)x.getSize();
        FloatArrayResult forwardFFT = forwardFFT(x);
        CLBuffer<Float> clBufTmp = context.createBuffer(CLMem.Usage.InputOutput, Float.class, realSize);
        gaussianFilterKernel.setArgs(forwardFFT.getResult(), clBufTmp, realSize, gwidthFactor, dt);
        CLEvent gaussianFilterEvent = gaussianFilterKernel.enqueueNDRange(queue, new int[] {realSize/2}, forwardFFT.eventsToWaitFor);
        return inverseFFT(new FloatArrayResult(clBufTmp, gaussianFilterEvent));
    }
    
    /** Converts the full complex 2*n FFT into the length n format as used by OregonDSP. This makes it
     * easier to reuse code from the original IterDecon.
     */
    public FloatArrayResult shortenFFT(CLBuffer<Float> inCLBuffer, CLEvent... eventsToWaitFor) {
        CLBuffer<Float> shortenFFTVals = context.createBuffer(CLMem.Usage.InputOutput, Float.class, inCLBuffer.getElementCount()/2);
        shortenFFT.setArgs(inCLBuffer, shortenFFTVals, inCLBuffer.getElementCount()/2);
        CLEvent shortenFFTEvent = shortenFFT.enqueueNDRange(queue, new int[] {(int)inCLBuffer.getElementCount()/2});
        FloatArrayResult shortenFFTResult = new FloatArrayResult(shortenFFTVals, shortenFFTEvent);
        return shortenFFTResult;
    }

    /** Converts the length n format as used by OregonDSP into the full complex 2*n FFT for use by the OpenCL FFT
     * library. This makes it easier to reuse code from the original IterDecon.
     */
    public FloatArrayResult lengthenFFT(CLBuffer<Float> inCLBuffer, CLEvent... eventsToWaitFor) {
        long n = inCLBuffer.getElementCount();
        System.out.println("IterDeconOpenCL lengthenFFT length: "+n+" ");
        CLBuffer<Float> lengthenFFTVals = context.createBuffer(CLMem.Usage.InputOutput, Float.class, 2*n);
        lengthenFFT.setArgs(inCLBuffer, lengthenFFTVals, n);
        CLEvent lengthenFFTEvent = lengthenFFT.enqueueNDRange(queue, new int[] {(int)n/2}, eventsToWaitFor);
        FloatArrayResult lengthenFFTResult = new FloatArrayResult(lengthenFFTVals, lengthenFFTEvent);
        return lengthenFFTResult;
    }
    
    protected FloatArrayResult phaseShift(FloatArrayResult x, float shift, float dt) {
        int size = (int)x.getSize();
        
        FloatArrayResult xFFT = forwardFFT(x);
        
        CLBuffer<Float> shiftFFT = context.createBuffer(CLMem.Usage.InputOutput, Float.class, size);
        phaseShift.setArgs(xFFT.getResult(), size, shiftFFT, shift, dt);
        
        CLEvent phaseShiftEvent = phaseShift.enqueueNDRange(queue, new int[] {(int)size}, xFFT.getEventsToWaitFor());
        
        FloatArrayResult outFFT = inverseFFT(new FloatArrayResult(shiftFFT, phaseShiftEvent));
        
        CLBuffer<Float> clBufOut = context.createBuffer(CLMem.Usage.InputOutput, Float.class, size);
        complex_to_floats.setArgs(outFFT.result, clBufOut, size);
        CLEvent cmplxToFloatEvent = complex_to_floats.enqueueNDRange(queue, new int[] {size}, outFFT.getEventsToWaitFor());
        return new FloatArrayResult(clBufOut, cmplxToFloatEvent);
    }

    public FloatArrayResult forwardFFT(FloatArrayResult x) {
        return forwardFFT(x.getResult(), x.getEventsToWaitFor());
    }
    
    /** forward FFT with the result in the format returned by OregonDSP's fft. */
    public FloatArrayResult forwardFFT(CLBuffer<Float> x, CLEvent... eventsToWaitFor) {
        CLBuffer<Float> clBufRealComplex = context.createBuffer(CLMem.Usage.InputOutput, Float.class, 2*x.getElementCount());
        floats_to_complex.setArgs(x, clBufRealComplex, x.getElementCount());
        CLEvent denomRealToCmplxEvent = floats_to_complex.enqueueNDRange(queue, new int[] {(int)x.getElementCount()}, eventsToWaitFor);
        CLBuffer<Float> clBufDenomFFT = context.createBuffer(CLMem.Usage.InputOutput, Float.class, 2*x.getElementCount());
        CLEvent fftEvent = fft.transform(queue, clBufRealComplex, clBufDenomFFT, false, denomRealToCmplxEvent);
        
        return shortenFFT(clBufDenomFFT, fftEvent);
    }
    
    /** inverse FFT with the input in the format of OregonDSP's fft. */
    public FloatArrayResult inverseFFT(FloatArrayResult xFFT) {
        int size = (int)xFFT.getSize();
        FloatArrayResult longFFT = lengthenFFT(xFFT.getResult(), xFFT.getEventsToWaitFor());
        CLBuffer<Float> clBufRealComplex = context.createBuffer(CLMem.Usage.InputOutput, Float.class, 2*size);
        System.out.println("before inverseFFT "+size+" to "+2*size);
        CLEvent fftEvent = fft.transform(queue, longFFT.getResult(), clBufRealComplex, true, longFFT.getEventsToWaitFor());
        
        CLBuffer<Float> clBufReal = context.createBuffer(CLMem.Usage.InputOutput, Float.class, size);
        complex_to_floats.setArgs(clBufRealComplex, clBufReal, size);
        CLEvent cmplxToRealEvent = complex_to_floats.enqueueNDRange(queue, new int[] {size}, fftEvent);
        
        return new FloatArrayResult(clBufReal, cmplxToRealEvent);
    }

    public static float[] makePowerTwo(float[] data) {
        float[] out = new float[nextPowerTwo(data.length)];
        System.arraycopy(data, 0, out, 0, data.length);
        return out;
    }

    public static float[] makePowerTwoTimesTwo(float[] data) {
        float[] out = new float[2*nextPowerTwo(data.length)];
        System.arraycopy(data, 0, out, 0, data.length);
        return out;
    }

    public static int nextPowerTwo(int n) {
        int i=1;
        while (i < n) {
            i*=2;
        }
        return i;
    }

    static boolean useNativeFFT = false;
    static boolean useOregonDSPFFT = false;
    
    // kernels
    protected CLKernel sqrFloatsKernel;
    protected CLKernel floats_to_complex;
    protected CLKernel complex_to_floats;
    protected CLKernel gaussianFilterKernel;
    protected CLKernel correlateKernel;
    protected CLKernel convolveKernel;
    protected CLKernel buildSpikes;
    protected CLKernel indexReduceAbsMax;
    protected CLKernel phaseShift;
    protected CLKernel subtract_floats;
    protected CLKernel shortenFFT;
    protected CLKernel lengthenFFT;
    FloatFFTPow2 fft;

    private CLKernel scalarDiv;
    private CLKernel scalarMul;
    
    protected int maxBumps;
    protected boolean useAbsVal;
    protected float tol;
    protected float gwidthFactor;
    protected CLContext context;
    protected CLQueue queue;
    
    public static void main(String[] args) throws Exception {
        IterDeconOpenCl iterDecon = new IterDeconOpenCl(1, true, 1,1);
        

        int n = 1024;
        float gwidth = 2.5f;
        float dt = 0.1f;
        float[] inData = new float[n];
        inData[0] = 1;
        
        System.out.println("After init opencl");
        FloatArrayResult inCLBuffer = iterDecon.makeCLBuffer(inData);
        System.out.println("before fft ");
        FloatArrayResult forwardFFT = iterDecon.forwardFFT(inCLBuffer);
        
        FloatArrayResult inverse = iterDecon.inverseFFT(forwardFFT);
        System.out.println("before read "+forwardFFT.getEventsToWaitFor()[0].getCommandExecutionStatus()+"  "+inverse.getEventsToWaitFor()[0].getCommandExecutionStatus());
        System.out.print("Inverse events: ");
        for (int i = 0; i < inverse.getEventsToWaitFor().length; i++) {
            System.out.print("  "+inverse.getEventsToWaitFor()[i].getCommandExecutionStatus());
        }
        System.out.println();
        float[] inverseFlts = inverse.getAfterWait(iterDecon.queue);
        //assertArrayEquals(inData, inverseFlts, 0.001f);
        
        /*
        int n = 1024;
        ByteOrder byteOrder = iterDecon.context.getByteOrder();
        Pointer<Float> aPtr = allocateFloats(n).order(byteOrder);
        for (int i = 0; i < n; i++) {
            aPtr.set(i, (float)(i%16)+i/((float)n));
        }
        CLBuffer<Float> a = iterDecon.context.createBuffer(Usage.InputOutput, aPtr);
        FloatArrayResult gauss= iterDecon.gaussianFilter(a, 2f, .1f);
        
        Pointer<Float>  amps = allocateFloats(n).order(byteOrder);
        Pointer<Integer> shifts = allocateInts(n).order(byteOrder);
        CLBuffer<Float> ampsClBuf = iterDecon.context.createBuffer(CLMem.Usage.InputOutput, Float.class, n);
        CLBuffer<Integer> shiftsClBuf = iterDecon.context.createBuffer(CLMem.Usage.InputOutput, Integer.class, n);
        
        iterDecon.calcMaxSpike(new FloatArrayResult(a), ampsClBuf, shiftsClBuf, n);*/
        System.out.println("Done");
    }
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(IterDeconOpenCl.class);

}// IterDecon

