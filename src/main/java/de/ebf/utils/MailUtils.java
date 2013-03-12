package de.ebf.utils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

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
	
	private static final Logger log = Logger.getLogger(MailUtils.class);
	private static final String SMTP_SERVER 	= Config.instance.getString("smtp.server");
	private static final String SMTP_PORT	 	= Config.instance.getString("smtp.port");
	private static final String SMTP_USER 		= Config.instance.getString("smtp.user");
	private static final String SMTP_PASS 		= Config.instance.getString("smtp.pass");
	private static final String SMTP_SENDERMAIL	= Config.instance.getString("smtp.user.mail");
	private static final String SMTP_SENDERNAME = Config.instance.getString("smtp.user.name");
	private static final String SMTP_BCC_MAIL = Config.instance.getString("smtp.bcc.mail");
	
	private static final String SMTP_USER_DEFAULT = "${smtp.user}";
	private static final String SMTP_PASS_DEFAULT = "${smtp.pass}";
	
	private static Session session;
	static{
		Properties props = System.getProperties();
		props.put("mail.smtp.host", SMTP_SERVER);
		props.put("mail.smtp.port", SMTP_PORT);
		props.put("mail.transport.protocol","smtp");
		if (
			!StringUtils.isEmpty(SMTP_USER) &&
			!StringUtils.isEmpty(SMTP_PASS) &&
			!SMTP_USER.equals(SMTP_USER_DEFAULT) &&
			!SMTP_PASS.equals(SMTP_PASS_DEFAULT)
		){
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.user", SMTP_USER);
			props.put("mail.password", SMTP_PASS);
			
			Authenticator auth = new javax.mail.Authenticator() {
			   @Override
			   public PasswordAuthentication getPasswordAuthentication() {
			      return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
			   }
			};
			session = Session.getDefaultInstance(props, auth);
		} else {
			session = Session.getDefaultInstance(props);
		}
	}
	
	public static boolean sendMail(String replyTo, List<String> recipients, String subject, String body){
		return MailUtils.sendMail(replyTo, recipients, subject, body, null);
	}
	
	public static boolean sendMail(String replyTo, List<String> recipients, String subject, String body, String htmlBody){
		log.info("Sending message ["+subject+"] to "+recipients+" via "+SMTP_SERVER);
		try{
			MimeMessage message = new MimeMessage(session);
			InternetAddress sender = new InternetAddress(SMTP_SENDERMAIL, SMTP_SENDERNAME, "UTF-8");
			message.setFrom(sender);
         
			for (String recipient : recipients) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
			}
			message.addRecipient(Message.RecipientType.BCC, new InternetAddress(SMTP_BCC_MAIL));
			message.setSubject(subject);
			if (!StringUtils.isEmpty(replyTo)){
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
				multiContentPart.addBodyPart(plainMessageBodyPart,0);
			}

			// Create html part
			if (!StringUtils.isEmpty(htmlBody)) {
				BodyPart htmlMessageBodyPart = new MimeBodyPart();
				htmlMessageBodyPart.setContent(htmlBody, "text/html");
				multiContentPart.addBodyPart(htmlMessageBodyPart,1);
			}

			// Build content
			BodyPart contentWrapper = new MimeBodyPart();
			contentWrapper.setContent(multiContentPart);
			rootBodyPart.addBodyPart(contentWrapper,0);
			message.setContent(rootBodyPart);

			Transport.send(message);
         	log.info("Sent message ["+subject+"] to "+recipients);
         	return true;
		} catch (MessagingException | UnsupportedEncodingException mex) {
			log.error("Exception while sending email: "+mex);
		}
		return false;
	}

}
