package edu.sc.seis.fissuresUtil.exceptionHandler;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
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



public class GUIReporter implements ExceptionReporter{
    
    public void report(String message, Throwable e, List sections) {
        this.message = message;
        this.e = e;
        this.sections = sections;
        if(displayPanel == null){
            createFrame();
        }
        displayPanel.add(createGUI(e, message, sections),
                         BorderLayout.CENTER);
        displayPanel.revalidate();
    }
    
    private JTabbedPane createGUI(Throwable e, String message, List sections) {
        JTabbedPane tabbedPane = new JTabbedPane();
        if(greeting != null){
            tabbedPane.addTab(greeting.getName(), createTextArea(greeting.getContents()));
        }
        tabbedPane.addTab("Details", createTextArea(message));
        tabbedPane.addTab("Stack Trace", createTextArea(ExceptionReporterUtils.getTrace(e)));
        Iterator it = sections.iterator();
        while(it.hasNext()){
            Section sec = (Section)it.next();
            tabbedPane.addTab(sec.getName(), createTextArea(sec.getContents()));
        }
        Dimension dimension = new Dimension(800, 300);
        tabbedPane.setPreferredSize(dimension);
        tabbedPane.setMinimumSize(dimension);
        return tabbedPane;
    }
    
    private static JScrollPane createTextArea(String message){
        JTextArea messageArea = new JTextArea();
        messageArea.setLineWrap(true);
        messageArea.setFont(new Font("Serif", Font.PLAIN, 14));
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        if(message != null){
            messageArea.setText(message);
        }
        return new JScrollPane(messageArea);
    }
    
    private void createFrame() {
        JFrame displayFrame = new JFrame();
        displayFrame.addWindowListener(new WindowAdapter(){
                    public void windowClosing(WindowEvent e) {
                        displayPanel = null;
                    }
                });
        displayPanel = new JPanel(new BorderLayout());
        displayPanel.add(createButtonPanel(displayFrame),
                         BorderLayout.SOUTH);
        Dimension dimension = new Dimension(800, 400);
        displayPanel.setPreferredSize(dimension);
        displayFrame.setContentPane(displayPanel);
        displayFrame.setSize(dimension);
        displayFrame.pack();
        displayFrame.show();
    }
    
    private JPanel createButtonPanel(final JFrame displayFrame){
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        displayFrame.dispose();
                        displayPanel = null;
                    }
                });
        JButton saveToFile = new JButton("Save");
        saveToFile.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ex) {
                        try{
                            writeFile();
                        }catch(IOException e){
                            int result = JOptionPane.showConfirmDialog(displayPanel,
                                                                       "We were unable to write to that file. Try again?",
                                                                       "Trouble writing exception file",
                                                                       JOptionPane.OK_CANCEL_OPTION,
                                                                       JOptionPane.WARNING_MESSAGE);
                            if(result == JOptionPane.OK_OPTION){
                                actionPerformed(null);
                            }
                        }
                    }
                    
                    public void writeFile()throws IOException{
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setSelectedFile(new File(ExceptionReporterUtils.getExceptionClassName(e) + ".txt"));
                        int rtnVal = fileChooser.showSaveDialog(displayPanel);
                        if(rtnVal == JFileChooser.APPROVE_OPTION) {
                            FileWriterReporter writer = new FileWriterReporter(fileChooser.getSelectedFile().getAbsoluteFile());
                            writer.report(message, e, sections);
                        }
                    }
                });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        buttonPanel.add(saveToFile);
        return buttonPanel;
    }
    
    public static void setGreeting(String title, String contents) {
        if(greeting != null){
            contents = greeting.getContents() + contents;
        }
        greeting = new Section(title, contents);
    }
    
    private static Section greeting;
    
    private String message;
    
    private Throwable e;
    
    private List sections;
    
    private JPanel displayPanel;
    
    private static Logger logger = Logger.getLogger(GUIReporter.class);
}// ExceptionHandlerGUI
