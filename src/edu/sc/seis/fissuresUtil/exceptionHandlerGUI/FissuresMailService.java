package edu.sc.seis.fissuresUtil.exceptionHandlerGUI;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

/**
 * FisssuresMailService.java
 *
 *
 * Created: Tue May  7 13:11:00 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class FissuresMailService {
    public FissuresMailService (){

    }

    public FissuresMailService(String[] recipients, String sender, String content, String subject) {
	
	for(int counter = 0; counter < recipients.length; counter++) {

	    addRecipient(recipients[counter]);
	}
	setSender(sender);
	setContent(content);
	setSubject(subject);
	
    }

    public FissuresMailService(String[] recipients, String sender, String content) {

	this(recipients, sender, content,"");
	
    }
    
    public FissuresMailService(String[] recipients, String content) {
	this(recipients, "", content, "");
    }

    public void setSender(String sender) {

	this.sender = sender;
    }
    
    public void setContent(String content) {

	this.content = content;
    }
    
    public void setSubject(String subject) {
	this.subject = subject;
    }

    public void addRecipient(String recipient) {

	recipients.add(recipient);
    }
    
    public void removeRecipient(String recipient) {
	
	recipients.remove(recipient);
    }

    public void addAttachment(String filename) {

	attachments.add(filename);
		
    }
    
    public void sendMail() throws Exception{


	Session session = Session.getDefaultInstance(null, null);
	MimeMessage message = new MimeMessage(session);
	BodyPart bodyPart = new MimeBodyPart();
	bodyPart.setContent(content, "text/plain");
	
	Multipart multiPart = new MimeMultipart();
	multiPart.addBodyPart(bodyPart);
	for(int counter = 0; counter < attachments.size(); counter++) {

	    String fileName = (String)attachments.get(counter);
	    bodyPart = new MimeBodyPart();
	    DataSource dataSource = new FileDataSource(fileName);
	    bodyPart.setDataHandler(new DataHandler(dataSource));
	    bodyPart.setFileName(fileName);
	    bodyPart.setDisposition(Part.ATTACHMENT);
	    multiPart.addBodyPart(bodyPart);
	}
	Address[] addresses = new Address[recipients.size()];
	for(int counter = 0; counter < recipients.size(); counter++) {
	    addresses[counter] = new InternetAddress((String)recipients.get(counter));
	}
	message.addRecipients(Message.RecipientType.TO, addresses);
	message.setFrom(new InternetAddress(sender));
	message.setSubject(subject);
	message.setContent(multiPart);
	Transport.send(message);
	
    }

    private ArrayList recipients;
    private String sender;
    private String content;
    private String subject;
    private ArrayList attachments = new ArrayList();
    
    
}// FisssuresMailService
