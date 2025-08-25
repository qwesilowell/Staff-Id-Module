/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.util;

import com.margins.STIM.entity.Users;
import jakarta.mail.Authenticator;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 *
 * @author PhilipManteAsare
 */
public class EmailSender {

    public static boolean sendEmail(String pass, Users user) {
        // Sender’s email and app password (not your Gmail password, use App Password)
        final String fromEmail = "thestaffidmodule@gmail.com";
        final String password = "whak ohgw tgri aekl";  // New app password

        // Receiver’s email
        String toEmail = user.getEmail();

        // SMTP server properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");           // SSL instead of STARTTLS
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");                  // SSL port instead of 587
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        // Create session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            String template = EmailTemplateLoader.loadTemplate("templates/passwordTemplate.html");

            template = template.replace("{{username}}", user.getUsername())
                    .replace("{{email}}", user.getEmail())
                    .replace("{{userRole}}", user.getUserRole().getUserRolename())
                    .replace("{{tempPassword}}", pass);

            // Create email message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Password Reset");
            message.setContent(template, "text/html; charset=utf-8");

            // Send email
            Transport.send(message);

            System.out.println(" Email sent successfully!");
            return true;
        } catch (MessagingException e) {
            System.out.println("Email error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
