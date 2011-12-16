package edu.sc.seis.fissuresUtil.display;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TimeZone;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.ResponsePrint;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import edu.sc.seis.fissuresUtil.chooser.ThreadSafeSimpleDateFormat;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdAuxillaryDataNames;

/**
 * SeismogramInfoDisplay.java
 *
 *
 * Created: Wed Jul 31 09:56:59 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class SeismogramInfoDisplay extends TextInfoDisplay {
    public SeismogramInfoDisplay (LinkedList format){
        this.format = format;
    }

    public void addSeismogram(DataSetSeismogram dss){
        HashMap values = new HashMap();
        Document doc = getDocument();
        try{
            appendLine(doc, "");
            appendHeader(doc, dss.toString());
            RequestFilter rf = dss.getRequestFilter();
            MicroSecondTimeRange rfRange = new MicroSecondTimeRange(rf);
            appendLabelValue(doc,"Channel ID", ChannelIdUtil.toString(rf.channel_id));
            appendLabelValue(doc,"Begin Time", "" + rfRange.getBeginTime());
            appendLabelValue(doc,"End Time" , "" + rfRange.getEndTime());

            Channel chan = dss.getDataSet().getChannel(rf.channel_id);
            if (chan != null) {
                appendHeader(doc, "Channel");
                appendLabelValue(doc, "Name", chan.getName());
                appendLabelValue(doc, "Orientation", chan.getOrientation().azimuth+"/"+chan.getOrientation().dip);
                appendLabelValue(doc, "Effective Time", chan.getBeginTime().date_time+" to "+chan.getEndTime().date_time);
                appendLabelValue(doc, "Id", ChannelIdUtil.toString(chan.get_id()));
                appendLabelValue(doc, "Sampling", chan.getSamplingInfo().numPoints+" in "+chan.getSamplingInfo().interval);
                appendHeader(doc, "Site");
                Site site = chan.getSite();
                appendLabelValue(doc, "Id", SiteIdUtil.toString(site.get_id()));
                appendLabelValue(doc, "Effective Time", site.getBeginTime().date_time+" to "+site.getEndTime().date_time);
                appendLabelValue(doc, "Comment", site.getComment());
                appendLabelValue(doc, "Location", site.getLocation().latitude+"/"+site.getLocation().longitude+" elev="+site.getLocation().elevation+" depth="+site.getLocation().depth);
                appendHeader(doc, "Station");
                Station sta = site.getStation();
                appendLabelValue(doc, "Id", StationIdUtil.toString(sta.get_id()));
                appendLabelValue(doc, "Effective Time", sta.getBeginTime().date_time+" to "+sta.getEndTime().date_time);
                appendLabelValue(doc, "Name", sta.getName());
                appendLabelValue(doc, "Description", sta.getDescription());
                appendLabelValue(doc, "Operator", sta.getOperator());
                appendLabelValue(doc, "Comment", sta.getComment());
                appendLabelValue(doc, "Location", sta.getLocation().latitude+"/"+sta.getLocation().longitude+" elev="+sta.getLocation().elevation+" depth="+sta.getLocation().depth);
                appendHeader(doc, "Network");
                NetworkAttr net = sta.getNetworkAttr();
                appendLabelValue(doc, "Id", NetworkIdUtil.toString(net.get_id()));
                appendLabelValue(doc, "Effective Time", net.getEffectiveTime().start_time.date_time+" to "+net.getEffectiveTime().end_time.date_time);
                appendLabelValue(doc, "Name", sta.getName());
                appendLabelValue(doc, "Description", sta.getDescription());
                appendLabelValue(doc, "Owner", sta.getOperator());
            }
            Object respObj = dss.getAuxillaryData(StdAuxillaryDataNames.RESPONSE);
            if (respObj != null && respObj instanceof Response) {
                Response resp = (Response)respObj;
                appendHeader(doc, "Response");
                appendLabelValue(doc, "Sensitivity", resp.the_sensitivity.sensitivity_factor+" at "+resp.the_sensitivity.frequency+" Hz");
                appendLabelValue(doc, "Number of Stages", ""+resp.stages.length);
                TimeRange effTime;
                if (chan != null) {
                    effTime = chan.getEffectiveTime();
                } else {
                    effTime = new TimeRange(rf.start_time, rf.end_time);
                }
                appendLine(doc, ResponsePrint.printResponse(rf.channel_id, resp, effTime));
            }
//            Iterator e = format.iterator();
//            while(e.hasNext()){
//                String current = (String)e.next();
//                appendLabelValue(doc, current, (String)values.get(current));
//            }
        }
        catch(Exception e){ e.printStackTrace(); }
    }


    public void addSeismogram(LocalSeismogramImpl seis) throws FissuresException {
        try {
            Document doc = getDocument();
            appendLine(doc, "");
            appendHeader(doc, seis.toString());

            appendLabelValue(doc, "ID", seis.get_id());
            appendLabelValue(doc, "Name", seis.getName());
            appendLabelValue(doc, "Channel", ChannelIdUtil.toStringNoDates(seis.channel_id));
            appendLabelValue(doc, "Num Points", ""+seis.num_points);
            appendLabelValue(doc, "Begin Time", ""+seis.getBeginTime());
            appendLabelValue(doc, "End Time", ""+seis.getEndTime());
            appendLabelValue(doc, "Sampling", ""+seis.sampling_info);
            appendLabelValue(doc, "Unit", ""+seis.y_unit);
            Statistics stat = new Statistics(seis);
            appendLabelValue(doc, "Min", stat.min()+"");
            appendLabelValue(doc, "Max", stat.max()+"");
            appendLabelValue(doc, "Mean", stat.mean()+"");
        } catch (BadLocationException e) {
            GlobalExceptionHandler.handle("Can't append seismogram info to display", e);
        }
    }

    public void drop(DropTargetDropEvent e){}

    public void dragScroll(DropTargetDragEvent e) {}

    public void dropActionChanged(DropTargetDragEvent e) {}

    ThreadSafeSimpleDateFormat dateFormat = new ThreadSafeSimpleDateFormat("MMM dd, yyyy HH:mm:ss.S z", TimeZone.getTimeZone("GMT"));

    protected LinkedList format;

    DecimalFormat twoDecimal = new DecimalFormat("0.00");
}// SeismogramInfoDisplay
