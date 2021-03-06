package edu.sc.seis.fissuresUtil.exceptionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
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

public class ResultMailer {

    /**
     * The four key strings' SMTP, SUBJECT, FROM and TO values must be set in
     * the passed in properties
     */
    public ResultMailer(Properties props) throws MissingPropertyException {
        this.props = props;
        checkProperties();
        logger.info("Exception mailer going to " + props.getProperty(TO)
                + " from " + props.getProperty(FROM) + " through "
                + props.getProperty(SMTP) + " created");
    }

    private void checkProperties() throws MissingPropertyException {
        checkProperty(SMTP);
        checkProperty(SUBJECT);
        checkProperty(TO);
        checkProperty(FROM);
    }

    private void checkProperty(String property) throws MissingPropertyException {
        if(props.getProperty(property) == null) {
            throw new MissingPropertyException("A system properties required by this class isn't set! "
                    + property + " must be set");
        }
    }

    public void mail(String message, String bodyText, List sections)
            throws Exception {
        Session session = Session.getDefaultInstance(props, null);
        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(props.getProperty(FROM));
        msg.setFrom(addressFrom);
        Address addressTo = new InternetAddress(props.getProperty(TO));
        msg.setRecipient(Message.RecipientType.TO, addressTo);
        String subject = props.getProperty(SUBJECT) + " " + message;
        msg.setSubject(subject);
        Multipart multipart = new MimeMultipart();
        BodyPart bodyPart = new MimeBodyPart();
        bodyPart.setText(message + "\n" + bodyText);
        multipart.addBodyPart(bodyPart);
        Iterator it = sections.iterator();
        while(it.hasNext()) {
            multipart.addBodyPart(createAttachement((Section)it.next()));
        }
        msg.setContent(multipart);
        Transport.send(msg);
    }

    private BodyPart createAttachement(Section section) throws IOException,
            MessagingException {
        DataSource source = new SectionDataSource(section);
        BodyPart bp = new MimeBodyPart();
        bp.setDataHandler(new DataHandler(source));
        bp.setFileName(section.getName() + ".txt");
        return bp;
    }

    private Properties props;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ResultMailer.class);

    /**
     * mail.smtp.host specifies the smtp server you want to use
     */
    public static final String SMTP = "mail.smtp.host";

    /**
     * mail.to specifies the recipient of the exception email
     */
    public static final String TO = "mail.to";

    /**
     * mail.from specifies the sender of the exception email
     */
    public static final String FROM = "mail.from";

    /**
     * mail.subject specifies the subject of the exception email
     */
    public static final String SUBJECT = "mail.subject";

    class SectionDataSource implements DataSource {

        SectionDataSource(Section s) {
            this.s = s;
        }

        public String getContentType() {
            return "text/plain";
        }

        public InputStream getInputStream() throws IOException {
            return new StringBufferInputStream(s.getContents());
        }

        public String getName() {
            return s.getName();
        }

        public OutputStream getOutputStream() throws IOException {
            throw new RuntimeException("getOutputStream() not impl");
        }

        Section s;
    }
}
