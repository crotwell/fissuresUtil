package edu.sc.seis.fissuresUtil.exceptionHandlerGUI;

import java.awt.*;
import javax.swing.*;
import java.lang.*;
import java.io.*;

/**
 * Description: This class can be used to display the GUI showing the exception along with useful information. 
 * It also shows the stackTrace. It also gives the option of saving the exception stack trace along with other
 * useful information added by the user.
 *
 *
 * Created: Thu Jan 31 16:39:57 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */



public class ExceptionHandlerGUI {
    /**
     * Creates a new <code>ExceptionHandlerGUI</code> instance.
     *
     * @param e an <code>Exception</code> value
     */
    public ExceptionHandlerGUI (Exception e){
	
	this.exception = e;
	
    }

    /**
     * displays a GUI showing the stackTrace in one tabbedPane and a brief description
     * of the exception in a separate tabbed pane.
     *
     * @return a <code>JPanel</code> value
     */
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

	traceString += getSystemInformation();
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

    private static String getSystemInformation() {

	String rtnValue = new String();
	rtnValue += "os.name : "+System.getProperty("os.name")+"\n";
	rtnValue += "os.version : "+System.getProperty("os.version")+"\n";
	rtnValue += "os.arch : "+System.getProperty("os.arch")+"\n";
	rtnValue += "java.runtime.version : "+System.getProperty("java.runtime.version")+"\n";
	rtnValue += "java.class.version : "+System.getProperty("java.class.version")+"\n";
	rtnValue += "java.class.path : "+System.getProperty("java.class.path")+"\n";
	rtnValue += "user.name : "+System.getProperty("user.name")+"\n";
	rtnValue += "user.timeZone : "+System.getProperty("user.timeZone")+"\n";
	rtnValue += "user.region : "+System.getProperty("user.region")+"\n";
	return rtnValue;
    }

    /**
     * retuns the stackTrace of the exception as a string.
     *
     * @param e an <code>Exception</code> value
     * @return a <code>String</code> value
     */
    public static String getStackTrace(Exception e) {


	StringWriter  stringWriter = new StringWriter();
	PrintWriter printWriter = new PrintWriter(stringWriter);
	e.printStackTrace(printWriter);
	
	return  stringWriter.toString();

    }

    private Exception exception;
}// ExceptionHandlerGUI
