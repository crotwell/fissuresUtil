package edu.sc.seis.fissuresUtil.display;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimeZone;
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
			values.put("Channel ID", ChannelIdUtil.toString(rf.channel_id));
			values.put("Begin Time", "" + rfRange.getBeginTime());
			values.put("End Time" , "" + rfRange.getEndTime());
			Iterator e = format.iterator();
			while(e.hasNext()){
				String current = (String)e.next();
				appendLabelValue(doc, current, (String)values.get(current));
			}
		}
		catch(Exception e){ e.printStackTrace(); }
    }
    
    public void drop(DropTargetDropEvent e){}
	
    public void dragScroll(DropTargetDragEvent e) {}
	
    public void dropActionChanged(DropTargetDragEvent e) {}
	
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.S z");
    
    protected LinkedList format;
	
    DecimalFormat twoDecimal = new DecimalFormat("0.00");
}// SeismogramInfoDisplay
