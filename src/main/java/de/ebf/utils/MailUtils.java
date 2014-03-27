package de.ebf.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
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
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class MailUtils {

   private static  Logger log = Logger.getLogger(MailUtils.class);
   private static  String SMTP_SERVER ;//= Config.instance.getString("smtp.server");
   private static  String SMTP_PORT ;//= Config.instance.getString("smtp.port");
   private static  String SMTP_USER ;//= Config.instance.getString("smtp.user");
   private static  String SMTP_PASS ;//= Config.instance.getString("smtp.pass");
   private static  String SMTP_SENDERMAIL ;//= Config.instance.getString("smtp.user.mail");
   private static  String SMTP_SENDERNAME ;//= Config.instance.getString("smtp.user.name");
   private static  String SMTP_BCC_MAIL ;//= Config.instance.getString("smtp.bcc.mail");
   private static  String SMTP_USER_DEFAULT = "${smtp.user}";
   private static  String SMTP_PASS_DEFAULT = "${smtp.pass}";
   public static  String NOREPLY = "noreply@ebf.de";
   private static Session session = null;

   /**
    * 1) This method can be called at the time of Onpremise-SetupWizard with the user freshly entered
    * properties. At this time, the properties in configuration file are still not available.
    * 
    * 2) This method can also be lazily and indirectly called by sendEmail() method with the properties from
    * the configuration file, whose properties are not available until the onpremise-setupWizard is finished.
    */
   public static void setSystemMailProperties(final String server, final String port, final String user, final String pass,
                                                String senderEmail, String senderName, String bccMail){
       SMTP_SERVER = server;
       SMTP_PORT = port;
       SMTP_USER = user;
       SMTP_PASS = pass;
       SMTP_SENDERMAIL = senderEmail;
       SMTP_SENDERNAME = senderName;
       SMTP_BCC_MAIL = bccMail;
               
      Properties props = System.getProperties();
      props.put("mail.smtp.host", server);
      props.put("mail.smtp.port", port);
      props.put("mail.transport.protocol", "smtp");
      if (!StringUtils.isEmpty(user)
              && !StringUtils.isEmpty(pass)
              && !user.equals(SMTP_USER_DEFAULT)
              && !pass.equals(SMTP_PASS_DEFAULT)) {
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.user", user);
         props.put("mail.password", pass);

         Authenticator auth = new javax.mail.Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
               return new PasswordAuthentication(user, pass);
            }
         };
         session = Session.getDefaultInstance(props, auth);
      } else {
         session = Session.getDefaultInstance(props);
      }
   }
   
   /**
    * This can not be called at the time of setupWizard, because the properties are not finished with setup.
    */
    public static void setSystemMailProperties(){
        setSystemMailProperties(Config.instance.getString("smtp.server"), 
                                Config.instance.getString("smtp.port"), 
                                Config.instance.getString("smtp.user"), 
                                Config.instance.getString("smtp.pass"),
                                Config.instance.getString("smtp.user.mail"), 
                                Config.instance.getString("smtp.user.name"), 
                                Config.instance.getString("smtp.bcc.mail"));
    }

   public static boolean sendMail(String replyTo, String recipient, String subject, String body) {
      return sendMail(replyTo, Arrays.asList(new String[]{recipient}), subject, body, null, null);
   }

   public static boolean sendMail(String replyTo, String recipient, String subject, String body, String htmlBody) {
      return sendMail(replyTo, Arrays.asList(new String[]{recipient}), subject, body, htmlBody, null);
   }

   public static boolean sendMail(String replyTo, List<String> recipients, String subject, String body) {
      return MailUtils.sendMail(replyTo, recipients, subject, body, null, null);
   }

   public static boolean sendMail(String replyTo, List<String> recipients, String subject, String body, String htmlBody) {
      return MailUtils.sendMail(replyTo, recipients, subject, body, htmlBody, null);
   }

   public static boolean sendMail(String replyTo, List<String> recipients, String subject, String body, File attachement) {
      return MailUtils.sendMail(replyTo, recipients, subject, body, null, attachement);
   }

   public static boolean sendMail(String replyTo, List<String> recipients, String subject, String body, String htmlBody, File attachment) {
       /*Lazy-intialization. If the session is not created, we initialize the static properties and the session with
        the properties from LocalSetting.properties. */
       if(session==null) {
           setSystemMailProperties();
       }
       
      log.info("Sending message [" + subject + "] to " + recipients + " via " + SMTP_SERVER);
      try {
         MimeMessage message = new MimeMessage(session);

         InternetAddress sender = new InternetAddress(SMTP_SENDERMAIL, SMTP_SENDERNAME, "UTF-8");
         message.setFrom(sender);

         for (String recipient : recipients) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
         }

         if (!StringUtils.isEmpty(SMTP_BCC_MAIL)){
            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(SMTP_BCC_MAIL));
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

         Transport.send(message);
         log.info("Sent message [" + subject + "] to " + recipients);
         return true;
      } catch (MessagingException | UnsupportedEncodingException mex) {
         log.error("Exception while sending email: " + mex);
      }
      return false;
   }
}
