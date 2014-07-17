package de.ebf.utils.mail;

import de.ebf.utils.Config;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class MailUtils {

    private static final Logger log = Logger.getLogger(MailUtils.class);
    private static MailConfig mailConfig;

    private static final String SMTP_USER_DEFAULT = "${smtp.user}";
    private static final String SMTP_PASS_DEFAULT = "${smtp.pass}";
    public static final String NOREPLY = "noreply@ebf.de";
    private static Session session = null;

    /**
     * Set system mail properties to given configuration
     */
    private static void setSystemMailProperties(final MailConfig newConfig) {
        if (newConfig != null) {
            mailConfig = newConfig;

            Properties props = System.getProperties();
            props.put("mail.smtp.host", mailConfig.getHost());
            props.put("mail.smtp.port", mailConfig.getPort());
            props.put("mail.transport.protocol", mailConfig.getProtocol());
            props.put("mail.smtp.timeout", 10000);
            props.put("mail.smtp.connectiontimeout", 10000);
            if (mailConfig.getRequireAuthentication()) {
                if (StringUtils.isEmpty(mailConfig.getUsername()) || StringUtils.isEmpty(mailConfig.getPassword())) {
                    throw new IllegalArgumentException("MailConfig requires authentication but username and/or password are empty");
                }

                if (mailConfig.getUsername().equals(SMTP_USER_DEFAULT) || mailConfig.getPassword().equals(SMTP_PASS_DEFAULT)) {
                    throw new IllegalArgumentException("MailConfig requires authentication but username and/or password are not set");
                }
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.user", mailConfig.getUsername());
                props.put("mail.password", mailConfig.getPassword());
                Authenticator auth = new javax.mail.Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailConfig.getUsername(), mailConfig.getPassword());
                    }
                };
                session = Session.getInstance(props, auth);
            } else {
                session = Session.getInstance(props);
            }
        }
    }

    /**
     * Set system mail properties to default configuration read from LocalSettings.properties
     */
    private static void setDefaultSystemMailProperties() {
        PropertiesConfiguration config = Config.getInstance();
        SMTPMailConfig defaultConfig = new SMTPMailConfig();
        defaultConfig.setBccEmail(config.getString("smtp.bcc.mail"));
        String username = config.getString("smtp.user");
        String password = config.getString("smtp.pass");
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password) && !username.equals(SMTP_USER_DEFAULT) && !password.equals(SMTP_PASS_DEFAULT)) {
            defaultConfig.setRequireAuthentication(true);
            defaultConfig.setUsername(username);
            defaultConfig.setPassword(password);
        } else {
            defaultConfig.setRequireAuthentication(false);
        }
        defaultConfig.setSenderEmail(config.getString("smtp.user.mail"));
        defaultConfig.setSenderName(config.getString("smtp.user.name"));
        defaultConfig.setHost(config.getString("smtp.server"));
        defaultConfig.setPort(Integer.parseInt(config.getString("smtp.port")));
        defaultConfig.setProtocol(config.getString("smtp.protocol"));
        setSystemMailProperties(defaultConfig);
    }

    public static void sendMail(MailConfig config, String replyTo, String recipient, String subject, String body) throws Exception {
        sendMail(config, replyTo, Arrays.asList(new String[]{recipient}), subject, body, null, null);
    }

    public static void sendMail(String replyTo, String recipient, String subject, String body) throws Exception {
        sendMail(null, replyTo, Arrays.asList(new String[]{recipient}), subject, body, null, null);
    }

    public static void sendMail(String replyTo, String recipient, String subject, String body, String htmlBody) throws Exception {
        sendMail(null, replyTo, Arrays.asList(new String[]{recipient}), subject, body, htmlBody, null);
    }

    public static void sendMail(MailConfig config, String replyTo, List<String> recipients, String subject, String body) throws Exception {
        MailUtils.sendMail(config, replyTo, recipients, subject, body, null, null);
    }

    public static void sendMail(String replyTo, List<String> recipients, String subject, String body) throws Exception {
        MailUtils.sendMail(null, replyTo, recipients, subject, body, null, null);
    }

    public static void sendMail(String replyTo, List<String> recipients, String subject, String body, String htmlBody) throws Exception {
        MailUtils.sendMail(null, replyTo, recipients, subject, body, htmlBody, null);
    }

    public static void sendMail(MailConfig config, String replyTo, List<String> recipients, String subject, String body, File attachement) throws Exception {
        MailUtils.sendMail(config, replyTo, recipients, subject, body, null, attachement);
    }

    public static void sendMail(String replyTo, List<String> recipients, String subject, String body, File attachement) throws Exception {
        MailUtils.sendMail(null, replyTo, recipients, subject, body, null, attachement);
    }

    public static void sendMail(MailConfig config, String replyTo, List<String> recipients, String subject, String body, String htmlBody, File attachment) throws Exception {
        /*Lazy-intialization. */
        if (config == null) {
            //if no mailConfig is specified we use the default system configuration
            setDefaultSystemMailProperties();
        } else {
            setSystemMailProperties(config);
        }

        log.info("Sending message [" + subject + "] to " + recipients + " via " + mailConfig.getHost());
        MimeMessage message = new MimeMessage(session);

        InternetAddress sender = new InternetAddress(mailConfig.getSenderEmail(), mailConfig.getSenderName(), "UTF-8");
        message.setFrom(sender);

        Address[] addresses = new Address[recipients.size()];
        for (int i = 0; i < recipients.size(); i++) {
            addresses[i] = new InternetAddress(recipients.get(i));
        }

        message.addRecipients(Message.RecipientType.TO, addresses);

        if (!StringUtils.isEmpty(mailConfig.getBccEmail())) {
            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(mailConfig.getBccEmail()));
        }

        message.setSubject(subject);

        if (!StringUtils.isEmpty(replyTo)) {
            InternetAddress replyToAddress = new InternetAddress(replyTo);
            message.setReplyTo(new Address[]{replyToAddress});
        }
        //message.setText(body);
        message.setSentDate(new java.util.Date());

        message.setDisposition("mixed");
        Multipart multiContentPart = new MimeMultipart("alternative");
        Multipart rootBodyPart = new MimeMultipart();

        // Create plain text part
        if (!StringUtils.isEmpty(body)) {
            BodyPart plainMessageBodyPart = new MimeBodyPart();
            plainMessageBodyPart.setContent(body, "text/plain");
            multiContentPart.addBodyPart(plainMessageBodyPart);
        }

        // Create html part
        if (!StringUtils.isEmpty(htmlBody)) {
            BodyPart htmlMessageBodyPart = new MimeBodyPart();
            htmlMessageBodyPart.setContent(htmlBody, "text/html");
            multiContentPart.addBodyPart(htmlMessageBodyPart);
        }

        if (attachment != null) {
            BodyPart attachmentBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachment);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(attachment.getName());
            multiContentPart.addBodyPart(attachmentBodyPart);
        }

        // Build content
        BodyPart contentWrapper = new MimeBodyPart();
        contentWrapper.setContent(multiContentPart);
        rootBodyPart.addBodyPart(contentWrapper, 0);
        message.setContent(rootBodyPart);

        if (mailConfig.getRequireAuthentication()) {
            Transport transport = session.getTransport();
            transport.connect(mailConfig.getHost(), mailConfig.getUsername(), mailConfig.getPassword());
            transport.sendMessage(message, addresses);
        } else {
            Transport.send(message, addresses);
        }
        log.info("Sent message [" + subject + "] to " + recipients);

    }

    public static MailConfig getMailConfig() {
        return mailConfig;
    }
}
