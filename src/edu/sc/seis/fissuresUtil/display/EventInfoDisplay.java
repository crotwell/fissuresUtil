package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.model.*;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;              //for layout managers
import java.awt.event.*;        //for action and window events

/**
 * EventInfoDisplay.java
 *
 *
 * Created: Fri May 31 10:01:21 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version $Id: EventInfoDisplay.java 1761 2002-05-31 17:34:01Z crotwell $
 */

public class EventInfoDisplay extends TextInfoDisplay  {
    public EventInfoDisplay (){
	
    }
    
    public void displayEvent(EventAccessOperations event) {
	Document doc = textPane.getDocument();
        try {
	    doc.remove(0, doc.getLength());
	    appendEvent(event, doc);
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    protected void appendEvent(EventAccessOperations event, Document doc)
	throws BadLocationException 
    {
	    appendEventAttr(event.get_attributes(), doc);
	    try {
		appendOrigin(event.get_preferred_origin(), doc);
	    } catch (NoPreferredOrigin e) {
		
	    } // end of try-catch
	    
    }

    protected void appendEventAttr(EventAttr attr, Document doc)
	throws BadLocationException 
    {
	appendHeader(doc, "Event");
	appendLabelValue(doc, "Name", attr.name);
	appendLabelValue(doc, "Region", feRegions.getRegionName(attr.region));
	appendLine(doc, "");
    }

    protected void appendOrigin(Origin origin, Document doc)
	throws BadLocationException 
    {
	appendHeader(doc, "Origin");
	appendLabelValue(doc, "Location", "("+origin.my_location.latitude+
			 ", "+origin.my_location.longitude+")");
	appendLabelValue(doc, "Time", origin.origin_time.date_time);
	appendLabelValue(doc, "Depth", origin.my_location.depth.value+" "+
			 ((UnitImpl)origin.my_location.depth.the_units).toString());
	appendLine(doc, "");
    }

    protected void appendMagnitude(Magnitude mag, Document doc)
	throws BadLocationException 
    {
	appendLabelValue(doc, "Magnitude", mag.value+" "+mag.type);
    }

    static ParseRegions feRegions = new ParseRegions();

}// EventInfoDisplay
