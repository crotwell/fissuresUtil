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
	final JButton closeButton = new JButton("CLOSE");
	displayFrame = new JFrame("CHANNEL CHOOSER");
	this.setLayout(new BorderLayout());
	channelChooser  = new ChannelChooserGUI(netDC);
	this.add(channelChooser, BorderLayout.CENTER);
	this.add(closeButton, BorderLayout.SOUTH);
	displayFrame.setContentPane(this);
	
	displayFrame.pack();
	displayFrame.setResizable(false);
		
	
	closeButton.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		
		    displayFrame.hide();
		    
		}
	    });

    }

    public void displayChannelChooser() {

	displayFrame.show();

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

    ChannelChooserGUI channelChooser;

    NetworkDC netDC;
    
    JFrame displayFrame;
    

}// ChannelChooserDisplay
