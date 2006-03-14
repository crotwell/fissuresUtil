package edu.sc.seis.fissuresUtil.simple;

import java.text.DecimalFormat;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;

public class SeismogramSpeedCheck extends SimpleSeismogramClient {

    public void exercise() {
        TimeInterval oneDay = new TimeInterval(1, UnitImpl.DAY);
        TimeInterval oneHour = new TimeInterval(1, UnitImpl.HOUR);
        MicroSecondDate now = ClockUtil.now();
        MicroSecondDate yesterday = now.subtract(oneDay);
        TimeInterval[] requestSizes = {new TimeInterval(1, UnitImpl.MINUTE),
                                       oneHour,
                                       (TimeInterval)oneHour.multiplyBy(6),
                                       oneDay};
        RequestFilter[][] reqs = new RequestFilter[requestSizes.length][];
        for(int i = 0; i < reqs.length; i++) {
            requestSizes[i].setFormat(new DecimalFormat("0"));
            MicroSecondDate start = yesterday.subtract((TimeInterval)oneDay.multiplyBy(i));
            reqs[i] = createRF(start, start.add(requestSizes[i]));
        }
        retrieve_seismograms(false);// Warmup connection
        for(int i = 0; i < reqs.length; i++) {
            MicroSecondDate start = ClockUtil.now();
            try {
                seisDC.retrieve_seismograms(reqs[i]);
            } catch(FissuresException e) {
                throw new RuntimeException(e);
            }
            MicroSecondDate end = ClockUtil.now();
            // printSeisResults(seis);
            TimeInterval callTime = (TimeInterval)end.subtract(start)
                    .convertTo(UnitImpl.SECOND);
            callTime.setFormat(new DecimalFormat("0.000"));
            System.out.println(requestSizes[i] + " request took " + callTime);
        }
    }

    public static void main(String[] args) {
        /*
         * Initializes the corba orb, finds the naming service and other startup
         * tasks. See Initializer for the code in this method.
         */
        Initializer.init(args);
        new SeismogramSpeedCheck().exercise();
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SeismogramSpeedCheck.class);
}
