/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.ForgetPassword;
import acrgb.utility.Utility;
import java.util.Date;
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
public class Forgetpassword {

    @Resource(lookup = "mail/acrgbmail")
    private Session acrgbmail;

    private final Utility utility = new Utility();

    public ACRGBWSResult SendEmail(
            final DataSource dataSource,
            final ForgetPassword forgetPassword,
            final String emailto,
            final String randpass) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        Methods methods = new Methods();
        UpdateMethods updatemethods = new UpdateMethods();
        GenerateRandomPassword generatepass = new GenerateRandomPassword();
        try {

            Message message = new MimeMessage(acrgbmail);
            message.setFrom(new InternetAddress("EMAILFROM", false));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailto, false));
            message.setSubject("PHILHEALTH ACR-GB SYSTEM");
            message.setSentDate(new Date());
            if (randpass.length() > 0) {
                //--------------
                message.setContent(utility.EmailTemplate(emailto.trim(), randpass), "text/html");
                Transport.send(message);
                //---------------
                result.setResult(randpass);
                result.setSuccess(true);
                result.setMessage("Account credentials successfully sent to " + emailto.trim());
            } else {
                ACRGBWSResult validateUsername = methods.ACRUSERNAME(dataSource, emailto.trim());
                if (validateUsername.isSuccess()) {
                    result.setMessage(emailto + "User email not found");
                } else {
                    String newpass = generatepass.GenerateRandomPassword(10);
                    //-----------------
                    message.setContent(utility.EmailTemplate(emailto, newpass), "text/html");
                    Transport.send(message);
                    //------------------
                    ACRGBWSResult updatepassword = updatemethods.UPDATEPASSCODE(dataSource, emailto.trim(), newpass);
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
            Logger.getLogger(Forgetpassword.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult Forgetpassword(final DataSource dataSource,
            final ForgetPassword forgetPassword,
            final String emailto,
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
            // Setup mail server
            properties.put("mail.smtp.host", forgetPassword.getMailhost());
            properties.put("mail.smtp.port", forgetPassword.getMailport());
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");
            // Get the Session object.// and pass username and password
            Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(forgetPassword.getAppuser(), forgetPassword.getApppass());
                }
            });
            // Used to debug SMTP issues
            session.setDebug(true);
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            // Set From: header field of the header.
            message.setFrom(new InternetAddress(forgetPassword.getMailfrom()));
            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailto.trim()));
            // Set Subject: header field
            message.setSubject("ACR-GB SYSTEM USER ACCOUNT CREDENTIALS");
            if (randpass.length() > 0) {
                // Now set the actual message
                //message.setText("This is actual message");
                message.setContent(utility.EmailTemplate(emailto.trim(), randpass), "text/html");
                Transport.send(message);
                result.setResult(randpass);
                result.setSuccess(true);
                result.setMessage("Account credentials successfully sent to " + emailto.trim());
            } else {
                ACRGBWSResult validateUsername = methods.ACRUSERNAME(dataSource, emailto.trim());
                if (validateUsername.isSuccess()) {
                    result.setMessage(emailto + "User email not found");
                } else {
                    // Now set the actual message
                    //message.setText("This is actual message");
                    String newpass = generatepass.GenerateRandomPassword(10);
                    message.setContent(utility.EmailTemplate(emailto, newpass), "text/html");
                    Transport.send(message);
                    ACRGBWSResult updatepassword = updatemethods.UPDATEPASSCODE(dataSource, emailto.trim(), newpass);
                    if (updatepassword.isSuccess()) {
                        result.setSuccess(true);
                        result.setMessage(updatepassword.getMessage());
                    } else {
                        result.setMessage(updatepassword.getMessage());
                    }
                }
            }
            result.setSuccess(true);
        } catch (MessagingException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Forgetpassword.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//  
    public ACRGBWSResult FA2CodeSender(final DataSource dataSource,
            final String emailto, final String code,
            final ForgetPassword forgetPassword) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        try {
            // Get system properties
            Properties properties = System.getProperties();
            // Setup mail server
            properties.put("mail.smtp.host", forgetPassword.getMailhost());
            properties.put("mail.smtp.port", forgetPassword.getMailport());
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");
            // Get the Session object.// and pass username and password
            Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            forgetPassword.getAppuser(),
                            forgetPassword.getApppass());
                }
            });
            // Used to debug SMTP issues
            session.setDebug(true);
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            // Set From: header field of the header.
            message.setFrom(new InternetAddress(forgetPassword.getMailfrom()));
            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailto.trim()));
            // Set Subject: header field
            message.setSubject("ACR-GB SYSTEM 2FA CODE");
            // Now set the actual message
            //message.setText("This is actual message");
            message.setContent(utility.FA2Template(emailto, code), "text/html");
            Transport.send(message);
            result.setSuccess(true);

        } catch (MessagingException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Forgetpassword.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
