package edu.sc.seis.fissuresUtil.display;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
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
	    LocalSeismogramImpl seis = dss.getSeismogram();
	    appendHeader(doc, dss.toString());
	    values.put("Number of Points", twoDecimal.format(seis.getNumPoints()));
	    values.put("ID", seis.get_id());
	    values.put("Sampling", "" + seis.getSampling());
	    values.put("Channel ID", ChannelIdUtil.toString(seis.getChannelID()));
	    values.put("Begin Time", "" + seis.getBeginTime());
	    values.put("End Time" , "" + seis.getEndTime());
	    values.put("Minimum Value", "" + seis.getMinValue());
	    values.put("Maximum Value", "" + seis.getMaxValue());
	    values.put("Mean Value", "" + seis.getMeanValue());
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
