package edu.sc.seis.fissuresUtil.exceptionHandlerGUI;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


/**
 * MailGUI.java
 *
 *
 * Created: Tue May  7 14:35:22 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class MailGUI {
    public MailGUI (){
	this("");
    }
    public MailGUI(String content) {
	this.content = content;
    }
    public void show() {

	final JFrame mailDialog = new JFrame("Fissures Mail");
	JPanel panel = new JPanel();
	JPanel buttonPanel = new JPanel();
	JPanel topPanel = new JPanel();
	JButton sendButton = new JButton("send");
	JButton attachmentButton = new JButton("attach file");
	JButton cancelButton = new JButton("cancel");
	
	JPanel fromPanel = new JPanel();
	JLabel fromLabel = new JLabel("From:");
	final JTextField textField = new JTextField(200);
	buttonPanel.add(sendButton);
	buttonPanel.add(attachmentButton);
	buttonPanel.add(cancelButton);
	topPanel.add(fromLabel);
	topPanel.add(textField);

	/*GridBagLayout bagLayout = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.weightx = 1.0;
	constraints.weighty = 1.0;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = constraints.BOTH;*/
	panel.setLayout(new BorderLayout());
	final JTextArea textArea = new JTextArea("<-- Add your message here -->\n"+content);
	//	panel.add(topPanel, BorderLayout.NORTH);
	panel.add(textArea, BorderLayout.CENTER);
	panel.add(buttonPanel, BorderLayout.SOUTH);

	sendButton.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {

		    FissuresMailService fissuresMailService = new FissuresMailService();
		    String from = textField.getText();
		    if(from == null) from = "";
		    fissuresMailService.setContent(textArea.getText());
		    fissuresMailService.setSender(from);
		    fissuresMailService.addRecipient("telukutl@seis.sc.edu");
		    fissuresMailService.setSubject("Bug in VSNEXPLORER");
		    for(int counter = 0; counter < arrayList.size(); counter++) {
			fissuresMailService.addAttachment((String)arrayList.get(counter));
		    }
		    try {
			fissuresMailService.sendMail();
		    } catch(Exception ex) {
			//	ex.printStackTrace();
			ExceptionHandlerGUI.handleException(ex);
		    }
		    mailDialog.dispose();
		}
	    });
	
	attachmentButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		 
		    JFileChooser fileChooser = new JFileChooser();
		    int rtnVal = fileChooser.showDialog(null, "Run Application");
		    if(rtnVal == JFileChooser.APPROVE_OPTION) {

			arrayList.add(fileChooser.getSelectedFile().getAbsolutePath());
		    }
		}
	    });

	cancelButton.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {

		    mailDialog.dispose();
		}
	    });
	java.awt.Dimension dimension = new java.awt.Dimension(800, 300);
	panel.setPreferredSize(dimension);	
	panel.setMinimumSize(dimension);
	mailDialog.setContentPane(panel);
	mailDialog.setSize(dimension);
	mailDialog.show();
	mailDialog.pack();
	
    }
    public void setContent(String content) {
	this.content = content;
    }
    
    
        
    private String content = new String();
    private ArrayList arrayList = new ArrayList();

}// MailGUI
