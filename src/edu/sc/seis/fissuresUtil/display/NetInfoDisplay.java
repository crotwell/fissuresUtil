
package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;              //for layout managers
import java.awt.event.*;        //for action and window events
import edu.iris.Fissures.display.*;

/**
 * NetInfoDisplay.java
 *
 *
 * Created: Thu Mar  1 22:00:08 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class NetInfoDisplay extends JPanel {
    
    public NetInfoDisplay() {
	setLayout(new BorderLayout());
	//Create a text pane.
        textPane = new JTextPane();
        paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //paneScrollPane.setPreferredSize(new Dimension(250, 155));
        paneScrollPane.setMinimumSize(new Dimension(10, 10));
	add(paneScrollPane, BorderLayout.CENTER);
	initStylesForTextPane(textPane);
    }
        
    public void displayNetwork(NetworkAttr attr) {
	Document doc = textPane.getDocument();
        try {
	    doc.remove(0, doc.getLength());
	    appendNetwork(attr, doc);
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    protected void appendNetwork(NetworkAttr attr, Document doc)
	throws BadLocationException 
    {
	appendHeader(doc, "Network");
	appendLabelValue(doc, "Code", attr.get_code());
	appendLabelValue(doc, "Name", attr.name);
	appendLabelValue(doc, "Description", attr.description);
	appendLabelValue(doc, "Owner", attr.owner);
	appendLabelValue(doc, "Begin", attr.effective_time.start_time.date_time);
	appendLabelValue(doc, "End", attr.effective_time.end_time.date_time);
	appendLine(doc, "");
    }

    public void displayStation(Station sta) {
	Document doc = textPane.getDocument();
        try {
	    doc.remove(0, doc.getLength());
	    appendNetwork(sta.my_network, doc);
	    appendStation(sta, doc);
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    protected void appendStation(Station sta, Document doc) 
	throws BadLocationException 
    {
	appendHeader(doc, "Station");
	appendLabelValue(doc, "Code", sta.get_id().station_code);
	appendLabelValue(doc, "Name", sta.name);
	appendLabelValue(doc, "Location", "("+sta.my_location.latitude+
			 ", "+sta.my_location.longitude+")");
	appendLabelValue(doc, "Begin", sta.effective_time.start_time.date_time);
	appendLabelValue(doc, "End", sta.effective_time.end_time.date_time);
	appendLabelValue(doc, "Operator", sta.operator);
	appendLabelValue(doc, "Description", sta.description);
	appendLabelValue(doc, "Comment", sta.comment);
	appendLine(doc, "");
    }

    public void displaySite(Site site) {
	Document doc = textPane.getDocument();
        try {
	    doc.remove(0, doc.getLength());
	    appendNetwork(site.my_station.my_network, doc);
	    appendStation(site.my_station, doc);
	    appendSite(site, doc);
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    protected void appendSite(Site site, Document doc) 
	throws BadLocationException 
    {
	appendHeader(doc, "Site");
	appendLabelValue(doc, "Location", "("+site.my_location.latitude+
			 ", "+site.my_location.longitude+")");
	appendLabelValue(doc, "Begin", 
			 site.effective_time.start_time.date_time);
	appendLabelValue(doc, "End", 
			 site.effective_time.end_time.date_time);
	appendLabelValue(doc, "Comment", site.comment);
	appendLine(doc, "");
    }

    public void displayChannel(Channel chan) {
	    Document doc = textPane.getDocument();
        try {
	    doc.remove(0, doc.getLength());
	    appendNetwork(chan.my_site.my_station.my_network, doc);
	    appendStation(chan.my_site.my_station, doc);
	    appendSite(chan.my_site, doc);
	    appendChannel(chan, doc);
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    protected void appendChannel(Channel chan, Document doc) 
	throws BadLocationException 
    {
	appendHeader(doc, "Channel");
	appendLabelValue(doc, "Chan Code", chan.get_code());
	appendLabelValue(doc, "Orientation", 
			 "("+chan.an_orientation.azimuth+
			 ", "+chan.an_orientation.dip+")");
	if (chan.sampling_info != null) {
	    appendLabelValue(doc, "Sampling", 
			     chan.sampling_info.numPoints+" points in "+
			     chan.sampling_info.interval.toString()); 
	} else {
	    appendLabelValue(doc, "Sampling", "null value");
	}
	appendLabelValue(doc, "Begin", 
			 chan.effective_time.start_time.date_time);
	appendLabelValue(doc, "End", 
			 chan.effective_time.end_time.date_time);
	appendLine(doc, "");
    }

    public void displayResponse(ChannelId chan, Instrumentation inst) {
	Document doc = textPane.getDocument();
        try {
	    doc.remove(0, doc.getLength());
	    doc.insertString(doc.getLength(), 
			     ResponsePrint.printResponse(chan, inst),
			     textPane.getStyle("response"));
            toTop();
	} catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        } 
    }

    public void displayProblem(String problem) {
	Document doc = textPane.getDocument();
        try {
	    doc.remove(0, doc.getLength());
	    doc.insertString(doc.getLength(), 
			     "Problem: ",
			     textPane.getStyle("label"));
	    doc.insertString(doc.getLength(), 
			     problem+"\n",
			     textPane.getStyle("problem"));
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }


    protected void appendLabelValue(Document doc, String label, String value)
	throws javax.swing.text.BadLocationException {
	    doc.insertString(doc.getLength(), 
			     label+": ",
			     textPane.getStyle("label"));
	    doc.insertString(doc.getLength(), 
			     value+"\n",
			     textPane.getStyle("value"));
    }

    protected void appendLine(Document doc, String value)
	throws javax.swing.text.BadLocationException {
	    doc.insertString(doc.getLength(), 
			     value+"\n",
			     textPane.getStyle("value"));
    }


    protected void appendHeader(Document doc, String value)
	throws javax.swing.text.BadLocationException {
	    doc.insertString(doc.getLength(), 
			     value+"\n",
			     textPane.getStyle("header"));
    }

    protected void toTop() {
        int min = paneScrollPane.getVerticalScrollBar().getMinimum();
        paneScrollPane.getVerticalScrollBar().setValue(min);
        repaint();
    }

    protected void initStylesForTextPane(JTextPane textPane) {
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                                        getStyle(StyleContext.DEFAULT_STYLE);

        Style header = textPane.addStyle("header", def);
        StyleConstants.setFontFamily(header, "SansSerif");
        StyleConstants.setFontSize(header, 18);
        StyleConstants.setForeground(header, Color.green);

        Style regular = textPane.addStyle("regular", def);
        StyleConstants.setFontFamily(regular, "Serif");

        Style s = textPane.addStyle("label", regular);

        s = textPane.addStyle("value", regular);
        StyleConstants.setBold(s, true);

        s = textPane.addStyle("problem", regular);
        StyleConstants.setBold(s, true);
        StyleConstants.setForeground(s, Color.red);

        Style mono = StyleContext.getDefaultStyleContext().
            getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(mono, "MonoSpaced");
        mono = textPane.addStyle("mono", mono);

    }

    JTextPane textPane;
    JScrollPane paneScrollPane;
    NetworkExplorer netExplorer;
    NetworkFinder netFinder;
} // NetInfoDisplay
