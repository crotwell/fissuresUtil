package edu.sc.seis.fissuresUtil.plottable.server;

import java.text.DecimalFormat;
import org.apache.log4j.Logger;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.UNKNOWN;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import edu.iris.Fissures.Dimension;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfPlottable.PlottableDCPOA;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.simple.Initializer;

public class PlottableImpl extends PlottableDCPOA {

    public PlottableImpl() throws NotFound, CannotProceed, InvalidName,
            org.omg.CORBA.ORBPackage.InvalidName {
        this(getDataCenter());
    }

    public PlottableImpl(DataCenterOperations dc) {
        this.dataCenter = dc;
    }

    public boolean custom_sizes() {
        return true;
    }

    public Plottable[] get_plottable(RequestFilter request, Dimension pixel_size) {
        try {
            
            RequestFilter[] filters = {request};
            MicroSecondTimeRange requestRange = new MicroSecondTimeRange(request);
            LocalSeismogram[] seis = dataCenter.retrieve_seismograms(filters);
            logger.debug("Got " + seis.length + " seismograms for request "
                    + requestRange);
            return makePlottables(seis, pixel_size.width, requestRange);
        } catch(Throwable e) {
            logger.error("Exception occured while retrieving plottable using get_plottable",
                         e);
            throw new UNKNOWN(e.toString());
        }
    }

    public static Plottable[] makePlottables(LocalSeismogram[] seis,
                                             int width,
                                             MicroSecondTimeRange time)
            throws CodecException {
        Plottable[] plottable = new Plottable[seis.length];
        for(int i = 0; i < seis.length; i++) {
            int[][] coordinates = SimplePlotUtil.compressXvalues(seis[i],
                                                                 time,
                                                                 width);
            plottable[i] = new Plottable(coordinates[0], coordinates[1]);
        }
        return plottable;
    }

    public synchronized Plottable[] get_for_day(ChannelId channel_id,
                                                int year,
                                                int jday,
                                                Dimension pixel_size) {
        try {
            logger.debug("get_for_day("
                    + ChannelIdUtil.toStringNoDates(channel_id) + ", " + year
                    + ", " + jday + ", " + pixel_size.width + ")");
            Time[] times = getTimes(jday, year);
            return get_plottable(new RequestFilter(channel_id,
                                                   times[0],
                                                   times[1]), pixel_size);
        } catch(Throwable e) {
            logger.error("Exception occured while retrieving plottable using get_for_day",
                         e);
            throw new org.omg.CORBA.UNKNOWN(e.toString());
        }
    }

    public Dimension[] get_whole_day_sizes() {
        return DAY_DIMS;
    }

    private static final Time[] getTimes(int jday, int year) {
        DecimalFormat numberFormat = new DecimalFormat("000");
        String baseTime = new Integer(year) + numberFormat.format(jday);
        Time startTime = new Time(baseTime + "J00:00:00.000Z", -1);
        Time endTime = new Time(baseTime + "J23:59:59.999Z", -1);
        return new Time[] {startTime, endTime};
    }

    public Dimension[] get_event_sizes() {
        throw new NO_IMPLEMENT();
    }

    public Plottable[] get_for_event(EventAccess event,
                                     ChannelId channel_id,
                                     Dimension pixel_size) {
        throw new NO_IMPLEMENT();
    }

    private static DataCenterOperations getDataCenter() throws NotFound,
            CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {
        return Initializer.getNS().getSeismogramDC("edu/iris/dmc",
                                                   "IRIS_BudDataCenter");
    }

    private DataCenterOperations dataCenter;

    private static final Dimension[] DAY_DIMS = new Dimension[] {new Dimension(8640,
                                                                               1)};

    private static final Logger logger = Logger.getLogger(PlottableImpl.class);
}