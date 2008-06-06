
package edu.sc.seis.fissuresUtil.display;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.ResponsePrint;

/**
 * NetInfoDisplay.java
 *
 *
 * Created: Thu Mar  1 22:00:08 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class NetInfoDisplay extends TextInfoDisplay {
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

    protected void appendNetwork(NetworkAttr attr)
        throws BadLocationException  {
        appendNetwork(attr, textPane.getDocument());
    }

    protected void appendNetwork(NetworkAttr attr, Document doc)
        throws BadLocationException  {
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
            appendNetwork(sta.getNetworkAttr(), doc);
            appendStation(sta, doc);
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    protected void appendStation(Station sta)
        throws BadLocationException  {
        appendStation(sta, textPane.getDocument());
    }

    protected void appendStation(Station sta, Document doc)
        throws BadLocationException  {
        appendHeader(doc, "Station");
        appendLabelValue(doc, "Code", sta.get_id().station_code);
        appendLabelValue(doc, "Name", sta.getName());
        appendLabelValue(doc, "Location", "("+sta.getLocation().latitude+
                             ", "+sta.getLocation().longitude+")");
        appendLabelValue(doc, "Begin", sta.getBeginTime().date_time);
        appendLabelValue(doc, "End", sta.getEndTime().date_time);
        appendLabelValue(doc, "Operator", sta.getOperator());
        appendLabelValue(doc, "Description", sta.getDescription());
        appendLabelValue(doc, "Comment", sta.getComment());
        appendLine(doc, "");
    }

    public void displaySite(Site site) {
        Document doc = textPane.getDocument();
        try {
            doc.remove(0, doc.getLength());
            appendNetwork(site.getStation().getNetworkAttr(), doc);
            appendStation(site.getStation(), doc);
            appendSite(site, doc);
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    protected void appendSite(Site site)
        throws BadLocationException  {
        appendSite(site, textPane.getDocument());
    }

    protected void appendSite(Site site, Document doc)
        throws BadLocationException  {
        appendHeader(doc, "Site");
        appendLabelValue(doc, "Location", "("+site.getLocation().latitude+
                             ", "+site.getLocation().longitude+")");
        appendLabelValue(doc, "Begin",
                         site.getEffectiveTime().start_time.date_time);
        appendLabelValue(doc, "End",
                         site.getEffectiveTime().end_time.date_time);
        appendLabelValue(doc, "Comment", site.getComment());
        appendLine(doc, "");
    }

    public void displayChannel(Channel chan) {
        Document doc = textPane.getDocument();
        try {
            doc.remove(0, doc.getLength());
            appendNetwork(chan.getSite().getStation().getNetworkAttr(), doc);
            appendStation(chan.getSite().getStation(), doc);
            appendSite(chan.getSite(), doc);
            appendChannel(chan, doc);
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    protected void appendChannel(Channel chan)
        throws BadLocationException  {
        appendChannel(chan, textPane.getDocument());
    }

    protected void appendChannel(Channel chan, Document doc)
        throws BadLocationException  {
        appendHeader(doc, "Channel");
        appendLabelValue(doc, "Chan Code", chan.get_code());
        appendLabelValue(doc, "Orientation",
                         "("+chan.getOrientation().azimuth+
                             ", "+chan.getOrientation().dip+")");
        if (chan.getSamplingInfo() != null) {
            appendLabelValue(doc, "Sampling",
                             chan.getSamplingInfo().numPoints+" points in "+
                                 chan.getSamplingInfo().interval.toString());
        } else {
            appendLabelValue(doc, "Sampling", "null value");
        }
        appendLabelValue(doc, "Begin",
                         chan.getBeginTime().date_time);
        appendLabelValue(doc, "End",
                         chan.getEndTime().date_time);
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
} // NetInfoDisplay
