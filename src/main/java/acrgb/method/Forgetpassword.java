/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.utility.Utility;
import java.text.ParseException;
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

    private final Utility utility = new Utility();
    public ACRGBWSResult Forgetpassword(final DataSource dataSource, final String emailto, final String randpass) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        Methods methods = new Methods();
        UpdateMethods updatemethods = new UpdateMethods();
        GenerateRandomPassword generatepass = new GenerateRandomPassword();
        try {
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
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));
            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to.trim()));
            // Set Subject: header field
            message.setSubject("ACR-GB SYSTEM USER ACCOUNT CREDENTIALS");
            if (randpass.length() > 0) {
                // Now set the actual message
                //message.setText("This is actual message");
                message.setContent("<button class='btn btn-success'>This is button </button><h2>Note : </h2><em>Dont share your account credentials</em><br><h1> Username : " + emailto + ""
                        + "<br> Passcode :" + randpass + "</h1>",
                        "text/html");
                Transport.send(message);
                result.setResult(randpass);
                result.setSuccess(true);
                result.setMessage("Account credentials successfully sent to " + emailto);
            } else {
                ACRGBWSResult validateUsername = methods.ACRUSERNAME(dataSource, emailto);
                if (validateUsername.isSuccess()) {
                    result.setMessage(emailto + "User email not found");
                } else {
                    // Now set the actual message
                    //message.setText("This is actual message");
                    String newpass = generatepass.GenerateRandomPassword(10);
                    message.setContent("<h2>Note : This account password was reset </h2><em>Dont share your account credentials</em><br><h1> Username : " + emailto + ""
                            + "<br>New Passcode :" + newpass + "</h1>",
                            "text/html");
                    Transport.send(message);
                    ACRGBWSResult updatepassword = updatemethods.UPDATEPASSCODE(dataSource, emailto, newpass);
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
        } catch (ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Forgetpassword.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
