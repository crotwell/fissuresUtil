package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.model.MicroSecondDate;  
import edu.iris.Fissures.model.ISOTime;  
import edu.iris.Fissures.network.ChannelIdUtil;
import javax.swing.text.Document;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import java.text.*;

import java.awt.*;              //for layout managers
import java.awt.event.*;        //for action and window events

//drag and drop
import java.awt.dnd.*;
import java.awt.datatransfer.*;

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
