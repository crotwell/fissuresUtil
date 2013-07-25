package edu.sc.seis.fissuresUtil.simple;

import java.text.DecimalFormat;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.chooser.ThreadSafeDecimalFormat;

public class SeismogramSpeedCheck extends SimpleSeismogramClient {

    public void exercise() {}

    public void query(ServerQuery toQuery) {
        try {
            seisDC = Initializer.getNS().getSeismogramDC(toQuery.dns,
                                                         toQuery.name);
        } catch(Exception e1) {
            throw new RuntimeException(e1);
        }
        TimeInterval oneDay = new TimeInterval(1, UnitImpl.DAY);
        TimeInterval oneHour = new TimeInterval(1, UnitImpl.HOUR);
        MicroSecondDate yesterday = toQuery.queryStart;
        TimeInterval[] requestSizes = {new TimeInterval(1, UnitImpl.MINUTE),
                                       oneHour,
                                       (TimeInterval)oneHour.multiplyBy(6)};// ,
        // oneDay};
        RequestFilter[][] reqs = new RequestFilter[requestSizes.length][];
        for(int i = 0; i < reqs.length; i++) {
            requestSizes[i].setFormat(new ThreadSafeDecimalFormat("0"));
            MicroSecondDate start = yesterday.subtract((TimeInterval)oneDay.multiplyBy(i));
            reqs[i] = new RequestFilter[] {new RequestFilter(toQuery.chan,
                                                             start.getFissuresTime(),
                                                             start.add(requestSizes[i])
                                                                     .getFissuresTime())};
        }
        retrieve_seismograms(false);// Warmup connection
        for(int i = 0; i < reqs.length; i++) {
            MicroSecondDate start = ClockUtil.now();
            LocalSeismogram[] seis;
            try {
                seis = seisDC.retrieve_seismograms(reqs[i]);
            } catch(FissuresException e) {
                throw new RuntimeException(e);
            }
            MicroSecondDate end = ClockUtil.now();
            TimeInterval totalRetrieved = new TimeInterval(0, UnitImpl.SECOND);
            for(int j = 0; j < seis.length; j++) {
                totalRetrieved = totalRetrieved.add(((LocalSeismogramImpl)seis[j]).getTimeInterval());
            }
            // printSeisResults(seis);
            TimeInterval callTime = (TimeInterval)end.subtract(start)
                    .convertTo(UnitImpl.SECOND);
            callTime.setFormat(new DecimalFormat("0.000"));
            System.out.println(requestSizes[i] + " request took " + callTime);
            System.out.println("Got " + totalRetrieved);
        }
    }

    private static class ServerQuery {

        public ServerQuery(String dns,
                           String name,
                           MicroSecondDate queryStart,
                           ChannelId chan) {
            this.dns = dns;
            this.name = name;
            this.queryStart = queryStart;
            this.chan = chan;
        }

        public String dns, name;

        public MicroSecondDate queryStart;

        public ChannelId chan;
    }

    private static final MicroSecondDate now = ClockUtil.now();

    private static final MicroSecondDate DAY_255 = new ISOTime(2005,
                                                               255,
                                                               0,
                                                               0,
                                                               0).getDate();

    private static final TimeInterval ONE_WEEK = new TimeInterval(1,
                                                                  UnitImpl.WEEK);

    private static final NetworkId SNEP = new NetworkId("XE",
                                                        DAY_255.getFissuresTime());

    private static final ChannelId SNP_15 = new ChannelId(SNEP,
                                                          "SNP15",
                                                          "00",
                                                          "BHZ",
                                                          DAY_255.getFissuresTime());

    private static ServerQuery[] queries = new ServerQuery[] {new ServerQuery("edu/iris/dmc",
                                                                              "IRIS_BudDataCenter",
                                                                              now.subtract(ONE_WEEK),
                                                                              Initializer.fakeChan),
                                                              new ServerQuery("edu/sc/seis/internal",
                                                                              "SNEP",
                                                                              DAY_255,
                                                                              SNP_15),
                                                              new ServerQuery("edu/iris/dmc",
                                                                              "IRIS_DataCenter",
                                                                              DAY_255,
                                                                              Initializer.fakeChan)};

    public static void main(String[] args) {
        /*
         * Initializes the corba orb, finds the naming service and other startup
         * tasks. See Initializer for the code in this method.
         */
        Initializer.init(args);
        SeismogramSpeedCheck ssc = new SeismogramSpeedCheck();
        for(int i = 1; i < 2; i++) {
            ssc.query(queries[i]);
        }
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SeismogramSpeedCheck.class);
}
