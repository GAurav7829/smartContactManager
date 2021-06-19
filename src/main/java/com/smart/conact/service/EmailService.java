package com.smart.conact.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	@Value("${email.username}")
	private String fromEmail;
	@Value("${email.password}")
	private String password;
	
	public boolean sendEmail(String toEmail, int OTP) {
		try {
			System.out.println("SSLEmail Start");
			Properties props = new Properties();
			props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP Host
			props.put("mail.smtp.socketFactory.port", "465"); // SSL Port
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // SSL Factory Class
			props.put("mail.smtp.auth", "true"); // Enabling SMTP Authentication
			props.put("mail.smtp.port", "465"); // SMTP Port
	
			Authenticator auth = new Authenticator() {
				// override the getPasswordAuthentication method
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(fromEmail, password);
				}
			};
	
	//		Session session = Session.getDefaultInstance(props, auth);
			Session session = Session.getInstance(props, auth);
			System.out.println("Session created");
			EmailUtil.sendEmail(session, toEmail, "Smart Contact Manager - OTP", OTP+"");
			return true;
		}catch(Exception ex) {
			ex.printStackTrace();
			return false;
		}

	}
}
