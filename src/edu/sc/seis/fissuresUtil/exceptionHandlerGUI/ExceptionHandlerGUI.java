package edu.sc.seis.fissuresUtil.exceptionHandlerGUI;

import java.io.*;
import javax.swing.*;

import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

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
        this("A problem has occured:",e);
    }
    
    public ExceptionHandlerGUI(String message, Throwable e) {
        this.exception = e;
        this.message = message;
        logger.error(message, e);
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
        if (greeting != null) {
            tabbedPane.addTab("Information", new JScrollPane(getGreetingPanel()));
        }
        tabbedPane.addTab("Details", new JScrollPane(getMessagePanel()));
        tabbedPane.addTab("Stack Trace", new JScrollPane(getStackTracePanel()));
        tabbedPane.addTab("System Info", new JScrollPane(getSystemInfoPanel()));
        Iterator it = panelNameToContents.keySet().iterator();
        while(it.hasNext()){
            String panelName = (String)it.next();
            String message = createString((Object)panelNameToContents.get(panelName));
            tabbedPane.addTab(panelName,
                              new JScrollPane(createGenericTextArea(message)));
        }
        Dimension dimension = new java.awt.Dimension(800, 300);
        tabbedPane.setPreferredSize(dimension);
        tabbedPane.setMinimumSize(dimension);
        mainPanel.setPreferredSize(dimension);
        mainPanel.setMinimumSize(dimension);
        mainPanel.add(tabbedPane);
    }
    
    
    private JTextArea getMessagePanel() {
        String message = this.message+"\n\n"+exception.getMessage();
        if (exception.getCause() != null) {
            message += "\n  caused by\n"+exception.getCause().getMessage();
        }
        return createGenericTextArea(message);
    }
    
    
    private JTextArea getGreetingPanel() {
        return createGenericTextArea(greeting);
    }
    
    
    private JTextArea getSystemInfoPanel() {
        return createGenericTextArea(getSystemInformation());
    }
    
    private JTextArea getStackTracePanel() {
        return createGenericTextArea(getStackTraceString());
    }
    
    
    private String createString(Object item) {
        if(item instanceof List)
            return createString((List)item);
        else if(item instanceof File)
            return createString((File)item);
        else
            throw new IllegalArgumentException();
    }
    
    private String createString(File file){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuffer message = new StringBuffer();
            try {
                String line = reader.readLine();
                while(line != null){
                    message.append(line + "\n");
                    line = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return message.toString();
        } catch (FileNotFoundException e) {
            ExceptionHandlerGUI.handleException("File to be displayed in Exceptionhandler not found", e);
        }
        return "";
    }
    
    private String createString(List stringList) {
        StringBuffer message = new StringBuffer();
        Iterator it = stringList.iterator();
        while(it.hasNext()){
            message.append((String)it.next() + "\n");
        }
        return message.toString();
    }
    
    private static JTextArea createGenericTextArea(String message){
        JTextArea messageArea = new JTextArea();
        messageArea.setLineWrap(true);
        messageArea.setFont(new Font("Serif", Font.PLAIN, 14));
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        if(message != null){
            messageArea.setText(message);
        }
        return messageArea;
    }
    
    public String getStackTraceString() {
        String traceString = "";
        if (exception instanceof WrappedException) {
            WrappedException we = (WrappedException)exception;
            if (we.getCausalException() != null) {
                traceString +=
                    getStackTrace(we.getCausalException())+"\n";
            } // end of if (we.getCausalException() != null)
        }
        
        traceString += getStackTrace(exception);
        return traceString;
    }
    
    public String getMessage() {
        return message;
    }
    
    public static String getSystemInformation() {
        
        String rtnValue = "";
        rtnValue += "Date : "+new java.util.Date().toString()+"\n";
        try {
            rtnValue += "Server offset : "+ClockUtil.getServerTimeOffset()+"\n";
        } catch (IOException e) {
            rtnValue += "Server offset : "+e.toString()+"\n";
        }
        rtnValue += "os.name : "+System.getProperty("os.name")+"\n";
        rtnValue += "os.version : "+System.getProperty("os.version")+"\n";
        rtnValue += "os.arch : "+System.getProperty("os.arch")+"\n";
        rtnValue += "java.runtime.version : "+System.getProperty("java.runtime.version")+"\n";
        rtnValue += "java.class.version : "+System.getProperty("java.class.version")+"\n";
        rtnValue += "java.class.path : "+System.getProperty("java.class.path")+"\n";
        rtnValue += "edu.sc.seis.gee.configuration : "+System.getProperty("edu.sc.seis.gee.configuration")+"\n";
        rtnValue += "user.name : "+System.getProperty("user.name")+"\n";
        rtnValue += "user.timeZone : "+System.getProperty("user.timeZone")+"\n";
        rtnValue += "user.region : "+System.getProperty("user.region")+"\n";
        
        rtnValue += "\n\n\n Other Properties:\n";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        java.util.Properties props = System.getProperties();
        props.list(printWriter);
        printWriter.close();
        rtnValue += stringWriter.getBuffer();
        
        return rtnValue;
    }
    
    public void createFrame() {
        
        
        JPanel displayPanel = new JPanel();
        
        JButton closeButton = new JButton("Close");
        
        JButton saveToFile = new JButton("Save");
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
                                String str = message+"\n";
                                bw.write(str, 0, str.length());
                                str = getStackTrace(exception);
                                bw.write(str, 0, str.length());
                                str = getSystemInformation();
                                bw.write(str, 0, str.length());
                                
                                // fw.close();
                                bw.close();
                                fw.close();
                            } catch(Exception e) {}
                        }
                    }
                });
        
        
        if (System.getProperty("errorHandlerServlet") != null) {
            JButton submit = new JButton("Submit");
            addToButtonPanel(submit);
            submit.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            try {
                                URL url = new URL(System.getProperty("errorHandlerServlet"));
                                HttpURLConnection http = (HttpURLConnection)url.openConnection();
                                http.setRequestMethod("POST");
                                http.setDoOutput(true);
                                BufferedWriter out = new BufferedWriter( new OutputStreamWriter(http.getOutputStream()));
                                out.write("bugreport="+getMessage());
                                out.write(getStackTraceString());
                                out.write(getSystemInformation());
                                out.write("\r\n");
                                out.close();
                                http.connect();
                                BufferedReader read = new BufferedReader(new InputStreamReader(http.getInputStream()));
                                String s;
                                while ((s = read.readLine()) != null) {
                                    logger.debug(s);
                                }
                                read.close();
                            } catch (IOException ex) {
                                logger.error("Problem sending error to server", ex);
                            }
                        }
                    });
        }
        buttonPanel.add(closeButton);
        buttonPanel.add(saveToFile);
        
        
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
    
    /** Sets a message to appear in the first tab. This is a generic message
     that is used by all exceptions. If the initial message is null, then this
     tab does not appear. */
    public static void setGreeting(String message) {
        greeting = message;
    }
    
    public static void addMessageToPanel(String panelName, String message){
        List panelStringList = null;
        if(panelNameToContents.containsKey(panelName)){
            panelStringList = (List)panelNameToContents.get(panelName);
        }else{
            panelStringList = new LinkedList();
            panelNameToContents.put(panelName, panelStringList);
        }
        if(panelStringList.size() > 1000){
            panelStringList.remove(0);
        }
        panelStringList.add(message);
    }
    
    public static void addFile(String panelName, File fileName){
        panelNameToContents.put(panelName, fileName);
    }
    
    private Throwable exception;
    
    private String message;
    
    private static String greeting = null;
    
    private static Map panelNameToContents = new HashMap();
    
    private JPanel buttonPanel = new JPanel();
    
    private JPanel mainPanel = new JPanel();
    
    private JFrame displayFrame = new JFrame();
    
    static Logger logger = Logger.getLogger(ExceptionHandlerGUI.class);
    
}// ExceptionHandlerGUI
