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

    public static  JPanel handleException(Exception exception) {
	
	JPanel mainPanel = new JPanel();
	JTabbedPane tabbedPane = new JTabbedPane();
	JPanel messagePanel = new JPanel();
	JLabel exceptionMessageLabel = new JLabel();
	JTextArea messageArea = new JTextArea();
	messageArea.setLineWrap(true);
	messageArea.setFont(new Font("BookManOldSytle", Font.BOLD, 12));
	messageArea.setWrapStyleWord(true);
	messageArea.setEditable(false);
	
	
	exceptionMessageLabel.setText(exception.toString());
	
	messagePanel.setLayout(new BorderLayout());
	messagePanel.add(exceptionMessageLabel);
	tabbedPane.addTab("information", messagePanel);

	JPanel stackTracePanel = new JPanel();
	JScrollPane scrollPane = new JScrollPane(stackTracePanel);
	String traceString = "";
	if (exception instanceof WrappedException) {
	    WrappedException we = (WrappedException)exception;
	    if (we.getCausalException() != null) {
		traceString += 
		    getStackTrace(we.getCausalException());
	    } // end of if (we.getCausalException() != null)
	}
	traceString += getStackTrace(exception);

	messageArea.setText(traceString);
	stackTracePanel.setLayout(new BorderLayout());
	stackTracePanel.add(messageArea);
	tabbedPane.addTab("stackTrace", scrollPane);

	java.awt.Dimension dimension = new java.awt.Dimension(800, 300);
	tabbedPane.setPreferredSize(dimension);
	tabbedPane.setMinimumSize(dimension);
	mainPanel.setPreferredSize(dimension);
	mainPanel.setMinimumSize(dimension);
	mainPanel.add(tabbedPane);
	return mainPanel;

    }

    private static String getStackTrace(Exception e) {


	StringWriter  stringWriter = new StringWriter();
	PrintWriter printWriter = new PrintWriter(stringWriter);
	e.printStackTrace(printWriter);
	
	return  stringWriter.toString();

    }

    Exception exception;
}// ExceptionHandlerGUI
