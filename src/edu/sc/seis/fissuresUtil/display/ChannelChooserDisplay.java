package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.chooser.*;
import edu.sc.seis.fissuresUtil.namingService.*;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;

/**
 * ChannelChooserDisplay.java
 *
 *
 * Created: Thu Feb 14 10:06:28 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ChannelChooserDisplay extends JPanel implements  ChannelChooserInterface{
    public ChannelChooserDisplay (){
	
	//this.netDC = netDC;

    }

    

  

    public void configure(NetworkDC netDC) {
	
	this.netDC = netDC;
	this.setLayout(new BorderLayout());
	channelChooser  = new ChannelChooserGUI(netDC);
	this.add(channelChooser, BorderLayout.CENTER);
  }

    public void displayChannelChooser() {

    }

    public ChannelId[] getChannelIds() {

	return channelChooser.getChannelIds();

    }

    public String[] getNetworks() {

	return channelChooser.getNetworks();

    }

    public String[] getStations() {
	
	return channelChooser.getStations();

    }

    public String[] getSites() {

	return channelChooser.getSites();
	
    }

    public String[] getChannels() {
	
	return channelChooser.getChannels();

    }

    public ChannelChooser getChannelChooser() {
	if (channelChooser == null) {
	    return null;
	} // end of if (channelChooser == null)
	return channelChooser.getChannelChooser();
    }

    ChannelChooserGUI channelChooser;

    NetworkDC netDC;
    
  
}// ChannelChooserDisplay
