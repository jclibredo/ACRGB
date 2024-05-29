/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.utility.Utility;
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
import javax.mail.PasswordAuthentication;
//import jakarta.mail.internet.InternetAddress;
//import jakarta.mail.internet.MimeMessage;

/**
 *
 * @author MinoSun
 */
public class Forgetpassword {

    private final Utility utility = new Utility();

    public ACRGBWSResult Forgetpassword(final String emailto, final String randpass) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        GenerateRandomPassword generatepass = new GenerateRandomPassword();
        final String appUser = "roland.aboga@gmail.com";
        final String appPass = "bvyf bnrj nire gbvb";
        // Recipient's email ID needs to be mentioned.
        String to = emailto;
        // Sender's email ID needs to be mentioned
        String from = "roland.aboga@gmail.com";
        // Assuming you are sending email from through gmails smtp
        String host = "smtp.gmail.com";
        // Get system properties
        Properties properties = System.getProperties();
        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(appUser, appPass);
            }
        });

        // Used to debug SMTP issues
        session.setDebug(true);
        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to.trim()));

            // Set Subject: header field
            message.setSubject("This is the Subject Line!");

            // Now set the actual message
            //message.setText("This is actual message");
            String newpass = generatepass.GenerateRandomPassword(10);
            if (randpass.length() > 0) {
                message.setContent("<h2>This message is use to reset your password</h2><br><em>This message is test</em><p> New password is</p><h1>" + randpass + "</h1>",
                        "text/html");
                result.setResult(randpass);
            } else {
                message.setContent("<h2>This message is use to reset your password</h2><br><em>This message is test</em><p> New password is</p><h1>" + newpass + "</h1>",
                        "text/html");
                result.setResult(newpass);
            }

            // Send message
            Transport.send(message);

            result.setSuccess(true);
            result.setMessage("Passcode successfully sent to " + emailto);
        } catch (MessagingException mex) {
            result.setMessage(mex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, mex);
        }
        return result;
    }

}
