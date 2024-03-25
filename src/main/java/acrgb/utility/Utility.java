/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.utility;

import acrgb.structure.ACRGBWSResult;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author MinoSun
 */
@ApplicationScoped
@Singleton
public class Utility {

    String regex = "^(?=.*[0-9])"
            + "(?=.*[a-z])(?=.*[A-Z])"
            + "(?=.*[@#$%^&+=])"
            + "(?=\\S+$).{8,20}$";

//represents starting character of the string.
//represents a digit must occur at least once.
//represents a lower case alphabet must occur at least once.
//represents an upper case alphabet that must occur at least once.
//represents a special character that must occur at least once.
//white spaces don’t allowed in the entire string.
//.{8, 20} represents at least 8 characters and at most 20 characters.
//$ represents the end of the string.  
    public ACRGBWSResult ACRGBWSResult() {
        return new ACRGBWSResult();
    }

    public SimpleDateFormat SimpleDateFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    public ObjectMapper ObjectMapper() {
        return new ObjectMapper();
    }

    public boolean validatePassword(String password) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);
        return m.matches();
    }

    public String GetString(String name) {
        String result = "";
        try {
            Context context = new InitialContext();
            Context environment = (Context) context.lookup("java:comp/env");
            result = (String) environment.lookup(name);
        } catch (NamingException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
            result = ex.getMessage();
        }
        return result;
    }

    public boolean IsValidNumber(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean IsValidDate(String string) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        sdf.setLenient(false);
        try {
            sdf.parse(string);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public boolean IsValidDateDifference(String string) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(string);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public Date StringToDate(String stringdate) throws ParseException {
        java.util.Date sf = this.SimpleDateFormat("MM-dd-yyyy").parse(stringdate);
        // Date dateCovered = (Date) new Date(sf.getTime());
        return sf;
    }

    public Date StringToDateTime(String stringdatetime) throws ParseException {
        java.util.Date sf = this.SimpleDateFormat("MM-dd-yyyy hh:mm a").parse(stringdatetime);
        // Date dateCovered = (Date) new Date(sf.getTime());
        return sf;
    }

    public String ComputeDateBackward(String dates, int diff) throws ParseException {
        String dateResults = String.valueOf(LocalDate.parse(dates).minusYears(diff).format(DateTimeFormatter.ofPattern("MM-dd-yyyy")));
        return dateResults;
    }

}
