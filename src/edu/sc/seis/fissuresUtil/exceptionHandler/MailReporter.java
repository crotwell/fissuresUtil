/**
 * MailReporter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.exceptionHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * The four key strings' values must be set in the passed in properties
 */
public class MailReporter implements ExceptionReporter{
    public MailReporter(Properties props){
        this(props, true);
    }

    public MailReporter(Properties props, boolean displayExceptionClass){
        this.displayExceptionClass = displayExceptionClass;
        this.props = props;
        checkPropertiesSet();
    }

    private void checkPropertiesSet() {
        checkProperty(SMTP);
        checkProperty(SUBJECT);
        checkProperty(TO);
        checkProperty(FROM);
    }

    private void checkProperty(String property) {
        if(props.getProperty(property) == null){
            throw new IllegalStateException("A system properties required by this class isn't set! " + property + " must be set");
        }
    }

    public void report(String message, Throwable e, List sections) throws Exception{
        props.put("mail.smtp.host", props.getProperty(SMTP));
        Session session = Session.getDefaultInstance(props, null);
        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(props.getProperty(FROM));
        msg.setFrom(addressFrom);
        Address addressTo = new InternetAddress(props.getProperty(TO));
        msg.setRecipient(Message.RecipientType.TO, addressTo);
        String subject = props.getProperty(SUBJECT);
        if(displayExceptionClass){
            subject += " " + ExceptionReporterUtils.getExceptionClassName(e);
        }
        msg.setSubject(subject);
        Multipart multipart = new MimeMultipart();
        BodyPart bodyPart = new MimeBodyPart();
        bodyPart.setText(message + "\n" + ExceptionReporterUtils.getTrace(e));
        multipart.addBodyPart(bodyPart);
        Iterator it = sections.iterator();
        while(it.hasNext()){
            multipart.addBodyPart(createAttachement((Section)it.next()));
        }
        msg.setContent(multipart);
        Transport.send(msg);
    }

    private BodyPart createAttachement(Section section) throws IOException, MessagingException{
        String dir = System.getProperty("java.io.tmpdir");
        File file = new File(dir + section.getName() + ".txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(section.getContents());
        bw.close();
        DataSource source = new FileDataSource(file);
        BodyPart bp = new MimeBodyPart();
        bp.setDataHandler(new DataHandler(source));
        bp.setFileName(section.getName() + ".txt");
        return bp;
    }

    /**
     * The key is edu.sc.seis.fissuresUtil.exceptionHandler.MailReporter.SMTP
     * The value for this key in the passed in properties should be the smtp server
     * you want to use
     */
    public static final String SMTP = "edu.sc.seis.fissuresUtil.exceptionHandler.MailReporter.SMTP";

    /**
     * the Key is edu.sc.seis.fissuresUtil.exceptionHandler.MailReporter.to
     * The value for this key in the passed in properties should be the address of the
     * desired recipient of the exception email
     */
    public static final String TO = "edu.sc.seis.fissuresUtil.exceptionHandler.MailReporter.to";

    /**
     * The key is edu.sc.seis.fissuresUtil.exceptionHandler.MailReporter.from
     * The value for this key in the passed in properties should be the address of the
     * desired sender of the exception email
     */
    public static final String FROM = "edu.sc.seis.fissuresUtil.exceptionHandler.MailReporter.from";

    /**
     * The key is edu.sc.seis.fissuresUtil.exceptionHandler.MailReporter.title
     * The value for this key in the passed in properties should be the address of the
     * desired subject of the exception email
     */
    public static final String SUBJECT = "edu.sc.seis.fissuresUtil.exceptionHandler.MailReporter.subject";

    private boolean displayExceptionClass;

    private Properties props;

    /**
     *
     */
    public static void main(String[] args) {
        Properties props = new Properties();
        System.out.println("ADDING PROPS");
        props.put(SUBJECT, "MailReporterTest");
        props.put(SMTP, "mail.seis.sc.edu");
        props.put(FROM, "exception@seis.sc.edu");
        props.put(TO, "crotwell@seis.sc.edu");
        System.out.println("CREATING MAIL REPORTER");
        GlobalExceptionHandler.add(new MailReporter(props));
        System.out.println("SENDING MAIL");
        GlobalExceptionHandler.handle("This is a test of the emergency brodcast system",
                                      new Exception("This is only a test"));
    }
}

