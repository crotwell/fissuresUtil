package edu.sc.seis.fissuresUtil.eventHandlerGUI;

import java.awt.*;
import javax.swing.*;
import java.lang.*;


/**
 * EventHandlerGUI.java
 *
 *
 * Created: Thu Jan 31 16:39:57 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */



public class EventHandlerGUI {
    public EventHandlerGUI (Exception e){
	
	this.exception = e;
	
    }

    public static  void handleException(Exception exception) {
	
	JTabbedPane tabbedPane = new JTabbedPane();
	JFrame displayFrame = new JFrame("Exception Handler");
	JPanel messagePanel = new JPanel();
	JLabel exceptionMessageLabel = new JLabel();
	exceptionMessageLabel.setText(exception.toString());
	messagePanel.setLayout(new BorderLayout());
	messagePanel.add(exceptionMessageLabel);
	tabbedPane.addTab("information", messagePanel);

	JPanel stackTracePanel = new JPanel();
	JLabel stackTraceLabel = new JLabel();
	stackTraceLabel.setText(exception.getMessage());
	stackTracePanel.setLayout(new BorderLayout());
	stackTracePanel.add(stackTraceLabel);
	tabbedPane.addTab("stackTrace", stackTracePanel);

	java.awt.Dimension dimension = new java.awt.Dimension(400, 400);
	tabbedPane.setPreferredSize(dimension);
	tabbedPane.setMinimumSize(dimension);

	displayFrame.setContentPane(tabbedPane);
	displayFrame.setSize(400,400);
	displayFrame.pack();
	displayFrame.show();

    }

    private static String getStackTrace(Exception e) {

	/*String returnValue = new String();
	java.lang.StackTraceElement[] stackTraceElements = e.getStackTrace();
	for(int counter = 0; counter < stactTraceElements.length; counter++) {

	    returnValue = returnValue + stackTraceElements[counter].toString();

	    }*/
	return null;

    }

    Exception exception;
}// EventHandlerGUI
