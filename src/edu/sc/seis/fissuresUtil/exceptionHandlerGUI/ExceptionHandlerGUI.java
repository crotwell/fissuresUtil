package edu.sc.seis.fissuresUtil.exceptionHandlerGUI;

import java.awt.*;
import java.awt.event.*;
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
     * @param e an <code>Throwable</code> value
     */
    public ExceptionHandlerGUI (Throwable e){
	this.exception = e;
	this.message = "A problem Occured";
	createGUI();
    }

    public ExceptionHandlerGUI(String message, Throwable e) {
	this.exception = e;
	this.message = message;
	createGUI();
    }

    public static ExceptionHandlerGUI getExceptionHandlerGUI(String message, Throwable e) {
	ExceptionHandlerGUI exceptionHandlerGUI = new ExceptionHandlerGUI(message, e);
	return exceptionHandlerGUI;
    }
    
    public void addToButtonPanel(JButton button) {
	buttonPanel.add(button);
    }

    public JFrame display() {
	createFrame();
	return displayFrame;
    }

    public static JFrame handleException(String message, Throwable e) {

	ExceptionHandlerGUI gui = getExceptionHandlerGUI(message, e);
	return gui.display();

    }

    private void createGUI() {
	
	JTabbedPane tabbedPane = new JTabbedPane();
	tabbedPane.addTab("information", getMessagePanel());
	tabbedPane.addTab("stackTrace", getStackTracePanel());
	java.awt.Dimension dimension = new java.awt.Dimension(800, 300);
	tabbedPane.setPreferredSize(dimension);
	tabbedPane.setMinimumSize(dimension);
	mainPanel.setPreferredSize(dimension);
	mainPanel.setMinimumSize(dimension);
	mainPanel.add(tabbedPane);
    }
   

    private JPanel getMessagePanel() {
	JPanel messagePanel = new JPanel();	
	JTextArea exceptionMessageLabel = new JTextArea();
	exceptionMessageLabel.setLineWrap(true);
	exceptionMessageLabel.setFont(new Font("BookManOldSytle", Font.BOLD, 12));
	exceptionMessageLabel.setWrapStyleWord(true);
	exceptionMessageLabel.setEditable(false);
	exceptionMessageLabel.setText(message);
	messagePanel.setLayout(new BorderLayout());
	messagePanel.add(exceptionMessageLabel);
	return messagePanel;
    }
    
    
    private JScrollPane getStackTracePanel() {
	JTextArea messageArea = new JTextArea();
	messageArea.setLineWrap(true);
	messageArea.setFont(new Font("BookManOldSytle", Font.BOLD, 12));
	messageArea.setWrapStyleWord(true);
	messageArea.setEditable(false);
	
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
	return scrollPane;
    }
    


    public static String getSystemInformation() {

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

    public void createFrame() {
	

	JPanel displayPanel = new JPanel();

	JButton closeButton = new JButton("Close");

	JButton saveToFile = new JButton("save");
	buttonPanel.add(closeButton);
	buttonPanel.add(saveToFile);
	
	displayPanel.setLayout(new BorderLayout());
	displayPanel.add(mainPanel, 
			 BorderLayout.CENTER);
	displayPanel.add(buttonPanel, BorderLayout.SOUTH);
	
	java.awt.Dimension dimension = new java.awt.Dimension(800, 400);
	displayPanel.setPreferredSize(dimension);
	displayPanel.setMinimumSize(dimension);
	
	closeButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

		    displayFrame.dispose();

		}
	    });

	saveToFile.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ex) {
		    
		    JFileChooser fileChooser = new JFileChooser();
		    int rtnVal = fileChooser.showSaveDialog(null);
		    if(rtnVal == JFileChooser.APPROVE_OPTION) {
			try {
			    FileWriter fw = new FileWriter(fileChooser.getSelectedFile().getAbsolutePath());
			  
			    BufferedWriter bw = new BufferedWriter(fw);
			    String str = getSystemInformation();
			    bw.write(str, 0, str.length());
			    str = getStackTrace(exception);
			    bw.write(str, 0, str.length());
			    
			    // fw.close();
			    bw.close();
			    fw.close();
			} catch(Exception e) {}
		    }
		}
	    });
		
	displayFrame.getContentPane().add(displayPanel);
	displayFrame.setSize(dimension);
	displayFrame.pack();
	displayFrame.show();
  }


    /**
     * retuns the stackTrace of the exception as a string.
     *
     * @param e an <code>Throwable</code> value
     * @return a <code>String</code> value
     */
    public static String getStackTrace(Throwable e) {


	StringWriter  stringWriter = new StringWriter();
	PrintWriter printWriter = new PrintWriter(stringWriter);
	e.printStackTrace(printWriter);
	
	return  stringWriter.toString();

    }

    private Throwable exception;

    private String message;

    private JPanel buttonPanel = new JPanel();

    private JPanel mainPanel = new JPanel();

    private JFrame displayFrame = new JFrame();
}// ExceptionHandlerGUI
