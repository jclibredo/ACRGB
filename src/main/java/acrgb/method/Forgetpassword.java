/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.ForgetPassword;
import acrgb.structure.UserActivity;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
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
import oracle.jdbc.OracleTypes;
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
            ACRGBWSResult GetEmailCred = this.GetEmailSender(dataSource);
            if (GetEmailCred.isSuccess()) {
                ForgetPassword forget = utility.ObjectMapper().readValue(GetEmailCred.getResult(), ForgetPassword.class);
                final String appUser = forget.getAppuser();
                final String appPass = forget.getApppass();
                // Recipient's email ID needs to be mentioned.
                String to = emailto;
                // Sender's email ID needs to be mentioned
                String from = forget.getEmailto();
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
                    message.setContent(utility.EmailTemplate(emailto, randpass), "text/html");
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
                        message.setContent(utility.EmailTemplate(emailto, newpass), "text/html");
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

            } else {
                result.setMessage(GetEmailCred.getMessage());
            }

        } catch (MessagingException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Forgetpassword.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Forgetpassword.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET EMAIL
    public ACRGBWSResult GetEmailSender(final DataSource dataSource) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETEMAILCRED(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                ForgetPassword emailcred = new ForgetPassword();
                emailcred.setApppass(resultset.getString("APPPASS"));
                emailcred.setAppuser(resultset.getString("APPUSER"));
                emailcred.setEmailto(resultset.getString("SENDER"));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(emailcred));
            } else {
                result.setMessage("N/A");
            }
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Forgetpassword.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET EMAIL
    public ACRGBWSResult InserEmailCred(final DataSource dataSource, final ForgetPassword forgetpass) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        Methods methods = new Methods();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTEMAILCRED(:Message,:Code,"
                    + ":uappuser,:uapppass,:uappemail)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("uappuser", forgetpass.getAppuser());
            getinsertresult.setString("uapppass", forgetpass.getApppass());
            getinsertresult.setString("uappemail", forgetpass.getEmailto());
            getinsertresult.execute();
            //------------------------------------------------------------------------------------------------
            if (getinsertresult.getString("Message").equals("SUCC")) {
                UserActivity userlogs = utility.UserActivity();
                String actdetails = "ADD EMAIL CREDENTIALS: APPUSER["
                        + forgetpass.getAppuser() + "] , APPPASS [" + forgetpass.getApppass() + "]";
                userlogs.setActby(forgetpass.getCreatedby());
                userlogs.setActdate(forgetpass.getDatecreated());
                userlogs.setActdetails(actdetails);
                ACRGBWSResult insertActivitylogs = methods.ActivityLogs(dataSource, userlogs);
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message") + " LOGS STATUS: " + insertActivitylogs.getMessage());

            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }

        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Forgetpassword.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
