package edu.sc.seis.fissuresUtil.display;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
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
                appendLabelValue(doc, "Name", chan.name);
                appendLabelValue(doc, "Orientation", chan.an_orientation.azimuth+"/"+chan.an_orientation.dip);
                appendLabelValue(doc, "Effective Time", chan.effective_time.start_time.date_time+" to "+chan.effective_time.end_time.date_time);
                appendLabelValue(doc, "Id", ChannelIdUtil.toString(chan.get_id()));
                appendLabelValue(doc, "Sampling", chan.sampling_info.numPoints+" in "+chan.sampling_info.interval);
                appendHeader(doc, "Site");
                Site site = chan.my_site;
                appendLabelValue(doc, "Id", SiteIdUtil.toString(site.get_id()));
                appendLabelValue(doc, "Effective Time", site.effective_time.start_time.date_time+" to "+site.effective_time.end_time.date_time);
                appendLabelValue(doc, "Comment", site.comment);
                appendLabelValue(doc, "Location", site.my_location.latitude+"/"+site.my_location.longitude+" elev="+site.my_location.elevation+" depth="+site.my_location.depth);
                appendHeader(doc, "Station");
                Station sta = site.my_station;
                appendLabelValue(doc, "Id", StationIdUtil.toString(sta.get_id()));
                appendLabelValue(doc, "Effective Time", sta.effective_time.start_time.date_time+" to "+sta.effective_time.end_time.date_time);
                appendLabelValue(doc, "Name", sta.name);
                appendLabelValue(doc, "Description", sta.description);
                appendLabelValue(doc, "Operator", sta.operator);
                appendLabelValue(doc, "Comment", sta.comment);
                appendLabelValue(doc, "Location", sta.my_location.latitude+"/"+sta.my_location.longitude+" elev="+sta.my_location.elevation+" depth="+sta.my_location.depth);
                appendHeader(doc, "Network");
                NetworkAttr net = sta.my_network;
                appendLabelValue(doc, "Id", NetworkIdUtil.toString(net.get_id()));
                appendLabelValue(doc, "Effective Time", net.effective_time.start_time.date_time+" to "+net.effective_time.end_time.date_time);
                appendLabelValue(doc, "Name", sta.name);
                appendLabelValue(doc, "Description", sta.description);
                appendLabelValue(doc, "Owner", sta.operator);
            }
            Object respObj = dss.getAuxillaryData(StdAuxillaryDataNames.RESPONSE);
            if (respObj != null && respObj instanceof Response) {
                Response resp = (Response)respObj;
                appendHeader(doc, "Response");
                appendLabelValue(doc, "Sensitivity", resp.the_sensitivity.sensitivity_factor+" at "+resp.the_sensitivity.frequency+" Hz");
                appendLabelValue(doc, "Number of Stages", ""+resp.stages.length);
                TimeRange effTime;
                if (chan != null) {
                    effTime = chan.effective_time;
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

    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.S z");

    protected LinkedList format;

    DecimalFormat twoDecimal = new DecimalFormat("0.00");
}// SeismogramInfoDisplay
