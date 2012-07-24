

/* Copies real values from a into even indices in b. Assumes out is twice the size of a and n is length of a. */
__kernel void floats_to_complex(__global const float* a, __global float* out, int n) 
{
    int i = get_global_id(0);
    if (i >= n)
        return;
    
    out[2*i] = a[i];
    out[2*i+1] = 0;
}

/* Copies real values from even indices of a into b. Assumes a is twice the size of out and n is length of out. */
__kernel void complex_to_floats(__global const float* a, __global float* out, int n) 
{
    int i = get_global_id(0);
    if (i >= n)
        return;
    
    out[i] = a[2*i];
}


__kernel void sqr_floats(__global const float* a, __global float* out, int n) 
{
    int i = get_global_id(0);
    if (i >= n)
        return;
    
    out[i] = a[i] * a[i];
}

__kernel void scalar_div(__global const float* a, float factor, __global float* out, int n) 
{
    int i = get_global_id(0);
    if (i >= n)
        return;
    
    out[i] = a[i] / factor;
}


/* applies a gaussian filter to the fft. Assumes n is the 1/2 the length of 
 fft, or the length of the original real array and fft data is in order of OregonDSP's fft.*/
__kernel void gaussianFilter(__global const float* fft, __global float* out, int n, float gwidthFactor, float dt, __global float* gaussVals) 
{
    int i = get_global_id(0);
    if (2*i >= n)
        return;
    
    float df = 1/(n * dt);
    float omega;
    float gauss;
    if (i == 0) {
        out[0] = fft[0];
        gaussVals[0] = 1;
        // only worry about n/2 val and zero is mul by 1
        omega = M_PI/dt;
        gauss = exp(-omega*omega / (4*gwidthFactor*gwidthFactor));
        out[n/2] = fft[n/2] * gauss;
    } else {
        omega = i*2*M_PI*df;
        gauss = exp(-omega*omega / (4*gwidthFactor*gwidthFactor));
        out[i] = fft[i] * gauss;
        out[n-i] = fft[n-i] * gauss;
        gaussVals[i] = gauss;
    }
}

/* shortens a full complex fft to the format and sign convention used in OregonDSP. Reals are in position i
 and imaginary are in position n-i. shortLen is the length of the shortFft array, which
 should be 1/2 the length of the fft array.*/
__kernel void shortenFFT(__global const float* fft, __global float* shortFft, int shortLen)
{
    int i = get_global_id(0);
    float f = fft[i];
    if (i < shortLen) {
        if (i == 1) {
            // handle special case of n/2 real val and 0th imag
            shortFft[shortLen/2] = fft[shortLen];
        } else if (i % 2 == 0) { 
            shortFft[i/2] = f;   
        } else {
            shortFft[shortLen-(i-1)/2] = -1*f;
        }
    }
}

/*
 
 */
__kernel void lengthenFFT(__global const float* fft, __global float* longFFT, int shortLen) {
    int i = get_global_id(0);
    if(i < shortLen/2) {
        float real = fft[i];
        if (i==0) {
            longFFT[0] = real;
            longFFT[1] = 0;
            longFFT[shortLen]=fft[shortLen/2];
            longFFT[shortLen+1]=0;
        } else {
            float imag = fft[shortLen-i];
            longFFT[2*i] = real;
            longFFT[2*i+1] = -1*imag;
            longFFT[2*shortLen-2*i]=real;
            longFFT[2*shortLen-2*i+1]=imag;
        }
    }
}

__kernel void correlate(__global const float* afft, __global const float* bfft, __global float* outfft, int n) 
{
    int i = get_global_id(0);
    if (i >= n/2)
        return;
    
    if (i == 0) {
        outfft[0] = afft[0]*bfft[0];
        outfft[n/2] = afft[n/2]*bfft[n/2];
    } else {
        // swap signs due to cong of bfft
        outfft[i] = afft[i]*bfft[i] + afft[i+1]*bfft[i+1];
        outfft[n-i] = afft[n-i]*bfft[i] - afft[i]*bfft[n-i];
    }    
}


// important that the globalWorkSize <= max workGroupSize
// so that final parallel reduction (2nd stage) results in
// a single value
__kernel void indexReduceAbsMax(__global float* buffer,
                             __const int length,
                             __local float* scratch,
                             __local int* scratchIndex,
                             __global float* result,
                             __global int* resultIndex,
                             __const int resultStorageIndex) {
    
    int global_index = get_global_id(0);
    float element = buffer[global_index];
    float accumulator = element;
    int accumulatorIndex = global_index;
    // Loop sequentially over chunks of input vector
    while (global_index < length) {
        element = buffer[global_index];
        if (fabs(accumulator) < fabs(element)) {
            accumulator = element;
            accumulatorIndex = global_index;
        }
        global_index += get_global_size(0);
    }
    
    // Perform parallel reduction
    int local_index = get_local_id(0);
    scratch[local_index] = accumulator;
    scratchIndex[local_index] = accumulatorIndex;
    barrier(CLK_LOCAL_MEM_FENCE);
    for(int offset = get_local_size(0) / 2; offset > 0; offset = offset / 2) {
        if (local_index < offset) {
            float other = scratch[local_index + offset];
            float mine = scratch[local_index];
            if ( fabs(mine) > fabs(other)) {
                scratch[local_index] = mine;
                scratchIndex[local_index] = scratchIndex[local_index];
            } else {
                scratch[local_index] = other;
                scratchIndex[local_index] = scratchIndex[local_index + offset];
            }
        }
        barrier(CLK_LOCAL_MEM_FENCE);
    }
    if (get_global_id(0) == 0) {
        result[resultStorageIndex] = scratch[0];
        resultIndex[resultStorageIndex] = scratchIndex[0];
    }
}



__kernel void phase_shift(__global float* xFFT,
                         int xRealSize,
                          __global float* shiftFFT,
                          float shift,
                          float dt) 
{
    float a, b, c, d, omega;
    int i = get_global_id(0);
    if (i > xRealSize)
        return;
    
        
    a = xFFT[2*i];
    b = xFFT[2*i+1];
    if (i == 0) {
        omega = M_PI/dt;
        shiftFFT[0] = a;
        shiftFFT[1] = cos(omega*shift);
    } else {
        omega = i*(2*M_PI*shift)/(dt*xRealSize);
        c = cos(omega);
        d = sin(omega);
        shiftFFT[2*i] = a*c-b*d;
        shiftFFT[2*i+1] = a*d+b*c;
    }
}

__kernel void buildSpikes(__global float* spike,
                          __global int* spikeIndex,
                          __global float* out,
                          int outSize,
                          int currentBump,
                          __local int* tmp) 
{
    float accumulator = 0;
    int gtid = get_global_id(0);
    int ltid = get_local_id(0);
    int spikeLoop = 0;
    for (int groupLoop=0; (groupLoop-1) * (int)get_global_size(0) +1 < outSize; groupLoop++) { 
        // this parallelizes the load by each thread in the workgroup loading one element
        spikeLoop = groupLoop*get_global_size(0)+ltid;
        if (spikeLoop < currentBump)
            tmp[ltid] = spikeIndex[spikeLoop];
        else
            tmp[ltid] = -1;
        barrier(CLK_LOCAL_MEM_FENCE); // make sure all loads are finished
        if (gtid < outSize) {
            for(int offset = 0; offset < (int)get_local_size(0); offset++) {
                if (tmp[offset] == gtid) {
                    accumulator += spike[tmp[offset]];
                }
            }
        }
    }
    out[gtid] = accumulator;
}

