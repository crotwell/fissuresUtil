package edu.sc.seis.fissuresUtil.display;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimeZone;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

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
//            Iterator e = format.iterator();
//            while(e.hasNext()){
//                String current = (String)e.next();
//                appendLabelValue(doc, current, (String)values.get(current));
//            }
        }
        catch(Exception e){ e.printStackTrace(); }
    }
    
    
    public void addSeismogram(LocalSeismogramImpl seis){
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
        } catch (BadLocationException e) {
            GlobalExceptionHandler.handleStatic("Can't append seismogram info to display", e);
        }
    }
    
    public void drop(DropTargetDropEvent e){}
    
    public void dragScroll(DropTargetDragEvent e) {}
    
    public void dropActionChanged(DropTargetDragEvent e) {}
    
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.S z");
    
    protected LinkedList format;
    
    DecimalFormat twoDecimal = new DecimalFormat("0.00");
}// SeismogramInfoDisplay
