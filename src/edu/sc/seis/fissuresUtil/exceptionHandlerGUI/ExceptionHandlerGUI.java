package edu.sc.seis.fissuresUtil.exceptionHandlerGUI;

import java.awt.*;
import javax.swing.*;
import java.lang.*;
import java.io.*;

/**
 * ExceptionHandlerGUI.java
 *
 *
 * Created: Thu Jan 31 16:39:57 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */



public class ExceptionHandlerGUI {
    public ExceptionHandlerGUI (Exception e){
	
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
	JScrollPane scrollPane = new JScrollPane(stackTracePanel);
	JLabel stackTraceLabel = new JLabel();
	stackTraceLabel.setText(getStackTrace(exception));
	stackTracePanel.setLayout(new BorderLayout());
	stackTracePanel.add(stackTraceLabel);
	tabbedPane.addTab("stackTrace", scrollPane);

	java.awt.Dimension dimension = new java.awt.Dimension(400, 400);
	tabbedPane.setPreferredSize(dimension);
	tabbedPane.setMinimumSize(dimension);

	displayFrame.getContentPane().add(tabbedPane);
	displayFrame.setSize(400,400);
	displayFrame.pack();
	displayFrame.show();

    }

    private static String getStackTrace(Exception e) {


	StringWriter  stringWriter = new StringWriter();
	PrintWriter printWriter = new PrintWriter(stringWriter);
	e.printStackTrace(printWriter);
	
	return  stringWriter.toString();

    }

    Exception exception;
}// ExceptionHandlerGUI
