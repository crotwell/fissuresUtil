package edu.sc.seis.fissuresUtil.display;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 * SeismogramToolbar.java
 *
 *
 * Created: Wed May 29 14:14:48 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class ControlToolbar extends JComponent{
    protected JToolBar toolbar = new JToolBar("Seismogram Controls");

    public ControlToolbar(){
	final ControlToolbar test = this;
	JButton button = new JButton("Zoom In");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    test.updateControlBehaviorListener("Zoom In");
		}
	    });
	toolbar.add(button);
    
	button = new JButton("Zoom Out");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    test.updateControlBehaviorListener("Zoom Out");
		}
	    });
	toolbar.add(button);
    
	button = new JButton("Pan");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    test.updateControlBehaviorListener("Pan");
		}
	    });
	toolbar.add(button);
	
	button = new JButton("Select");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    test.updateControlBehaviorListener("Select");
		}
	    });
	toolbar.add(button);
    }

    public JToolBar getToolBar(){ return toolbar; }

    public void addControlBehaviorListener(ControlChangeListener e){
	mouseListeners.add(e);
    }
    
    public void removeControlBehaviorListener(ControlChangeListener e){
	mouseListeners.remove(e);
    }

    public void updateControlBehaviorListener(String type){
	Iterator e = mouseListeners.iterator();
	while(e.hasNext())
	    ((ControlChangeListener)e.next()).setControlBehavior(type);
    }
    
    protected LinkedList mouseListeners = new LinkedList();

    
}// ControlToolbar
