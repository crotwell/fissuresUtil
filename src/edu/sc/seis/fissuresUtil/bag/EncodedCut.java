package edu.sc.seis.fissuresUtil.bag;

import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

public class EncodedCut extends Cut {

    public EncodedCut(MicroSecondDate begin, MicroSecondDate end) {
        super(begin, end);
    }

    public EncodedCut(RequestFilter rf) {
        super(rf);
    }

    /**
     * Makes a seismogram covering as little extra beyond begin and end times of
     * this cut without extracting the data from the encoded data array. This
     * means there may be a few extra points around the begin and end time as
     * the encoded data segments probably won't line up with the cut times. If
     * the cut and the seismogram have no time in common, null is returned. If
     * the data isn't encoded, a regular cut is performed on it
     * 
     * @return an encoded seismogram covering as little of cut time as possible
     *         or null if there's no overlap
     * @throws FissuresException
     * 
     */
    public LocalSeismogramImpl apply(LocalSeismogramImpl seis)
            throws FissuresException {
        if(!overlaps(seis)) {
            return null;
        }
        if(!seis.is_encoded()) {
            return super.apply(seis);
        }
        int beginIndex = getBeginIndex(seis);
        int endIndex = getEndIndex(seis);
        List outData = new ArrayList();
        EncodedData[] ed = seis.get_as_encoded();
        int currentPoint = 0;
        for(int i = 0; i < ed.length && currentPoint < endIndex; i++) {
            if(currentPoint + ed[i].num_points > beginIndex) {
                outData.add(ed[i]);
            }
            currentPoint += ed[i].num_points;
        }
        TimeSeriesDataSel ds = new TimeSeriesDataSel();
        ds.encoded_values((EncodedData[])outData.toArray(new EncodedData[outData.size()]));
        logger.debug(outData.size() + " encoded segments matched cut");
        return new LocalSeismogramImpl(seis, ds);
    }
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(EncodedCut.class);
}
