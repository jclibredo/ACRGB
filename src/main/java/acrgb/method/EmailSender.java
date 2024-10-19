/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.utility.Utility;
import java.util.Date;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.mail.Message;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class EmailSender {

    public EmailSender() {
    }

    private final Utility utility = new Utility();

    public ACRGBWSResult EmailSender(
            final DataSource dataSource,
            final String uemail,
            final String randpass,
            final Session mailSession) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress("noreply@philhealth.gov.ph", false));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(uemail.trim(), false));
            message.setSubject("ACRGB SYSTEM");
            message.setSentDate(new Date());
            if (randpass.length() > 0) {
                message.setContent(utility.EmailSenderContent(uemail.trim(), randpass), "text/html");
                Transport.send(message);
                result.setSuccess(true);
            } else {
                ACRGBWSResult validateUsername = new Methods().ACRUSERNAME(dataSource, uemail.trim());
                if (validateUsername.isSuccess()) {
                    result.setMessage(uemail + " User email not found");
                } else {
                    String newPass = utility.GenerateRandomPassword(10);
                    //message.setContent(utility.EmailSenderContent(uemail.trim(), newPass), "text/html");
                    Transport.send(message);
                    ACRGBWSResult updatepassword = new UpdateMethods().UPDATEPASSCODE(dataSource, uemail.trim(), newPass);
                    if (updatepassword.isSuccess()) {
                        result.setMessage(updatepassword.getMessage());
                        result.setSuccess(true);
                    } else {
                        result.setMessage(updatepassword.getMessage());
                    }
                }
            }
        } catch (MessagingException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//    public ACRGBWSResult EmailSenderTest(
//            final DataSource dataSource,
//            final String uemail,
//            final String randpass,
//            final Session mailSession) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setSuccess(false);
//        result.setResult("");
////        try {
////            Message message = new MimeMessage(mailSession);
////            message.setFrom(new InternetAddress("noreply@philhealth.gov.ph", false));
////            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(uemail.trim(), false));
////            message.setSubject("ACRGB SYSTEM");
////            message.setSentDate(new Date());
////            if (randpass.length() > 0) {
////                message.setContent(utility.EmailSenderContent(uemail.trim(), randpass), "text/html");
////                Transport.send(message);
////                result.setSuccess(true);
////            } else {
////                ACRGBWSResult validateUsername = new Methods().ACRUSERNAME(dataSource, uemail.trim());
////                if (validateUsername.isSuccess()) {
////                    result.setMessage(uemail + " User email not found");
////                } else {
////                    String newPass = utility.GenerateRandomPassword(10);
////                    message.setContent(utility.EmailSenderContent(uemail.trim(), newPass), "text/html");
////                    Transport.send(message);
////                    ACRGBWSResult updatepassword = new UpdateMethods().UPDATEPASSCODE(dataSource, uemail.trim(), newPass);
////                    if (updatepassword.isSuccess()) {
////                        result.setMessage(updatepassword.getMessage());
////                        result.setSuccess(true);
////                    } else {
////                        result.setMessage(updatepassword.getMessage());
////                    }
////                }
////            }
////        } catch (MessagingException ex) {
////            result.setMessage(ex.toString());
////            Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, null, ex);
////        }
//        return result;
//    }

//    //  Email Sender Using Gmail API 
//    public ACRGBWSResult EmailSender(final DataSource dataSource,
//            final Email email,
//            final String randpass) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setSuccess(false);
//        result.setResult("");
//        Methods methods = new Methods();
//        UpdateMethods updatemethods = new UpdateMethods();
//        GenerateRandomPassword generatepass = new GenerateRandomPassword();
//        try {
//            Properties properties = System.getProperties();
//            // final Properties properties = acrgbmail.getProperties();
//            // Setup mail server
//            properties.put("mail.smtp.host", email.getHost());
//            properties.put("mail.smtp.port", email.getPort());
//            properties.put("mail.smtp.ssl.enable", "true");
//            properties.put("mail.smtp.auth", "true");
//            // Get the Session object.// and pass username and password
//            Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
//                @Override
//                protected PasswordAuthentication getPasswordAuthentication() {
//                    // return new PasswordAuthentication(properties.getProperty("mail.smtp.user"), properties.getProperty("mail.smtp.pass"));
//                    return new PasswordAuthentication(email.getAppuser(), email.getApppass());
//                }
//            });
//            // Used to debug SMTP issues
//            session.setDebug(true);
//            // Create a default MimeMessage object.
//            MimeMessage message = new MimeMessage(session);
//            message.setFrom(new InternetAddress("no_reply@example.com", "no_reply"));
//            message.setReplyTo(InternetAddress.parse("no_reply@example.com", false));
//            // Set To: header field of the header.
//            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email.getEmailto()));
//            // Set Subject: header field
//            message.setSubject("ACR-GB");
//            if (randpass.length() > 0) {
//                // Now set the actual message
//                //message.setText("This is actual message");
//                message.setContent(utility.EmailSenderContent(email.getEmailto().trim(), randpass), "text/html");
//                Transport.send(message);
//                result.setResult(randpass);
//                result.setSuccess(true);
//                result.setMessage("Account credentials successfully sent to " + email.getEmailto().trim());
//            } else {
//                ACRGBWSResult validateUsername = methods.ACRUSERNAME(dataSource, email.getEmailto().trim());
//                if (validateUsername.isSuccess()) {
//                    result.setMessage(email.getEmailto() + "User email not found");
//                } else {
//                    // Now set the actual message
//                    //message.setText("This is actual message");
//                    String newPass = generatepass.GenerateRandomPassword(10);
//                    message.setContent(utility.EmailSenderContent(email.getEmailto(), newPass), "text/html");
//                    Transport.send(message);
//                    ACRGBWSResult updatepassword = updatemethods.UPDATEPASSCODE(dataSource, email.getEmailto().trim(), newPass);
//                    if (updatepassword.isSuccess()) {
//                        result.setSuccess(true);
//                        result.setMessage(updatepassword.getMessage());
//                    } else {
//                        result.setMessage(updatepassword.getMessage());
//                    }
//                }
//            }
//        } catch (MessagingException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (UnsupportedEncodingException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
}
