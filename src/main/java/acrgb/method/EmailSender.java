/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Email;
import acrgb.utility.Utility;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
//-----------------------------------------------
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.mail.PasswordAuthentication;
import javax.sql.DataSource;
//import jakarta.mail.internet.InternetAddress;
//import jakarta.mail.internet.MimeMessage;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class EmailSender {

//    @Resource(lookup = "mail/acrgbmail")
//    private Session acrgbmail;
    private final Utility utility = new Utility();

//    public ACRGBWSResult EmailSender(
//            final DataSource dataSource,
//            final Email email,
//            final String randpass) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        Methods methods = new Methods();
//        UpdateMethods updatemethods = new UpdateMethods();
//        result.setMessage("");
//        result.setSuccess(false);
//        result.setResult("");
//        try {
//
////            Message message = new MimeMessage(acrgbmail);
////            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.getRecipient(), false));
////            message.setSubject(email.getSubject());
////            message.setText("TEST MESSAGE");
////            Transport.send(message);
//            Message message = new MimeMessage(acrgbmail);
//            message.setFrom(new InternetAddress("roland.aboga@gmail.com", false));
//            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email.getRecipient(), false));
//            message.setSubject(email.getSubject());
//            message.setSentDate(new Date());
//            if (randpass.length() > 0) {
//                message.setContent(utility.EmailTemplate(email.getRecipient(), randpass), "text/html");
////                message.setText("TEST EMAIL SENDER ");
//                Transport.send(message);
//                result.setSuccess(true);
//            } else {
//                ACRGBWSResult validateUsername = methods.ACRUSERNAME(dataSource, email.getRecipient().trim());
//                if (validateUsername.isSuccess()) {
//                    result.setMessage(email.getRecipient() + " User email not found");
//                } else {
//                    String newPass = utility.GenerateRandomPassword(10);
//                    message.setContent(utility.EmailTemplate(email.getRecipient(), newPass), "text/html");
////                    message.setText("TEST EMAIL SENDER ");
//                    Transport.send(message);
//                    ACRGBWSResult updatepassword = updatemethods.UPDATEPASSCODE(dataSource, email.getRecipient().trim(), newPass);
//                    if (updatepassword.isSuccess()) {
//                        result.setMessage(updatepassword.getMessage());
//                        result.setSuccess(true);
//                    } else {
//                        result.setMessage(updatepassword.getMessage());
//                    }
//                }
//            }
//        } catch (MessagingException ex) {
//            result.setMessage(ex.getLocalizedMessage());
//            Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
//    //  Email Sender Using Gmail API 
    public ACRGBWSResult EmailSender(final DataSource dataSource,
            final Email email,
            final String randpass) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        Methods methods = new Methods();
        UpdateMethods updatemethods = new UpdateMethods();
        GenerateRandomPassword generatepass = new GenerateRandomPassword();
        try {
            Properties properties = System.getProperties();
            // final Properties properties = acrgbmail.getProperties();
            // Setup mail server
            properties.put("mail.smtp.host", email.getHost());
            properties.put("mail.smtp.port", email.getPort());
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");
            // Get the Session object.// and pass username and password
            Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    // return new PasswordAuthentication(properties.getProperty("mail.smtp.user"), properties.getProperty("mail.smtp.pass"));
                    return new PasswordAuthentication(email.getAppuser(), email.getApppass());
                }
            });
            // Used to debug SMTP issues
            session.setDebug(true);
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("no_reply@example.com", "no_reply"));
            message.setReplyTo(InternetAddress.parse("no_reply@example.com", false));
            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email.getRecipient()));
            // Set Subject: header field
            message.setSubject("ACR-GB");
            if (randpass.length() > 0) {
                // Now set the actual message
                //message.setText("This is actual message");
                message.setContent(utility.EmailSenderContent(email.getRecipient().trim(), randpass), "text/html");
                Transport.send(message);
                result.setResult(randpass);
                result.setSuccess(true);
                result.setMessage("Account credentials successfully sent to " + email.getRecipient().trim());
            } else {
                ACRGBWSResult validateUsername = methods.ACRUSERNAME(dataSource, email.getRecipient().trim());
                if (validateUsername.isSuccess()) {
                    result.setMessage(email.getRecipient() + "User email not found");
                } else {
                    // Now set the actual message
                    //message.setText("This is actual message");
                    String newPass = generatepass.GenerateRandomPassword(10);
                    message.setContent(utility.EmailSenderContent(email.getRecipient(), newPass), "text/html");
                    Transport.send(message);
                    ACRGBWSResult updatepassword = updatemethods.UPDATEPASSCODE(dataSource, email.getRecipient().trim(), newPass);
                    if (updatepassword.isSuccess()) {
                        result.setSuccess(true);
                        result.setMessage(updatepassword.getMessage());
                    } else {
                        result.setMessage(updatepassword.getMessage());
                    }
                }
            }
        } catch (MessagingException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            result.setMessage(ex.toString());
            result.setMessage(ex.toString());
            Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
