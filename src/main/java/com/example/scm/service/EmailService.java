package com.example.scm.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	public boolean sendEmail(String to,String subject,String message)
	{
		boolean flag = false;
		String host = "smtp.gmail.com";
		Properties properties = System.getProperties();
		
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		//properties.put("mail.smtp.socketFactory.port", "465");    
		//properties.put("mail.smtp.socketFactory.class",    
                  //"javax.net.ssl.SSLSocketFactory");    
		
		//Step 1 to get session object
		
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				
				return new PasswordAuthentication("ismile.ghani@gmail.com", "njdluxozahiuijhn");
			}
			
			
		});
		
		session.setDebug(true);
		
		//step 2 compose the message text/multimedia
		
		MimeMessage mime = new MimeMessage(session);
		
		try {
			
			mime.setFrom("ismile.ghani@gmail.com");
			mime.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			mime.setSubject(subject);
			//mime.setText(message);
			mime.setContent(message,"text/html");
			
			//step 3 for attachment
			
//			String path = "D:\\IsmileGhaniCV.pdf";
//			
//			MimeMultipart mimemultipart = new MimeMultipart();
//			
//			MimeBodyPart textmime = new MimeBodyPart();
//			MimeBodyPart filemime = new MimeBodyPart();
//			
//			textmime.setText("For Attachment");
//			File file = new File(path);
//			
//			try {
//				filemime.attachFile(file);
//			} catch (IOException e) {
//				
//				e.printStackTrace();
//			}
//			
//			mimemultipart.addBodyPart(textmime);
//			mimemultipart.addBodyPart(filemime);
//			mime.setContent(mimemultipart);
			
			//step 4 send the message with transport class
			
			Transport.send(mime);
			
			System.out.print("Email Sent Successfully");
			flag = true;
			
			
		} catch (MessagingException e) {
			
			e.printStackTrace();
			flag = false;
			
		}
		
		return flag;
		
		
	}

}
