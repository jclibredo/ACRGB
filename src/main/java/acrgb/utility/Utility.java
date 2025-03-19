/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.utility;

import acrgb.method.Methods;
import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Contract;
import acrgb.structure.DateSettings;
import acrgb.structure.ACRGBPayload;
import acrgb.structure.UserActivity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import java.io.IOException;
import java.security.Key;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.spec.SecretKeySpec;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.DatatypeConverter;
import org.codehaus.jackson.map.ObjectMapper;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import okhttp3.OkHttpClient;

/**
 *
 * @author DRG_SHADOWBILLING
 */
@ApplicationScoped
@Singleton
public class Utility {

    private static SecretKeySpec secretkey;
    private byte[] key;
    String regex = "^(?=.*[0-9])"
            + "(?=.*[a-z])(?=.*[A-Z])"
            + "(?=.*[@#$%^&+=])"
            + "(?=\\S+$).{8,20}$";
    private static final String CIPHERKEY = "A263B7980A15ADE7";
    private final String email_pattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = UPPER.toLowerCase();
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$";
    private static final String ALL_CHARS = UPPER + LOWER + DIGITS + SPECIAL_CHARS;
    private static final SecureRandom RANDOM = new SecureRandom();

//represents starting character of the string.
//represents a digit must occur at least once.
//represents a lower case alphabet must occur at least once.
//represents an upper case alphabet that must occur at least once.
//represents a special character that must occur at least once.
//white spaces don’t allowed in the entire string.
//.{8, 20} represents at least 8 characters and at most 20 characters.
//$ represents the end of the string.  
    public String GenerateRandomPassword(int length) {
        StringBuilder password = new StringBuilder(length);
        // At least one uppercase letter
        password.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        // At least one lowercase letter
        password.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        // At least one digit
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        // At least one special character
        password.append(SPECIAL_CHARS.charAt(RANDOM.nextInt(SPECIAL_CHARS.length())));

        // Remaining characters randomly selected from all characters
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }
        return password.toString();
    }

    public ACRGBWSResult ACRGBWSResult() {
        return new ACRGBWSResult();
    }

    public Date GetCurrentDate() {
        return new java.util.Date();
    }

    public OkHttpClient OkHttpClient() {

        return new OkHttpClient();
    }

    public ACRGBPayload ACRGBPayload() {
        return new ACRGBPayload();
    }

    public UserActivity UserActivity() {
        return new UserActivity();
    }

    public Contract Contract() {
        return new Contract();
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

//    public String GetStrings(String name) {
//        String result = "";
//        try {
//            Context context = new InitialContext();
//            Context environment = (Context) context.lookup("java:comp/env");
//            result = (String) environment.lookup(name);
//        } catch (NamingException ex) {
//            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
//            result = ex.getMessage();
//        }
//        return result;
//    }
//    public static boolean isWindows() {
//        return (System.getProperty("os.name").toLowerCase().contains("win"));
//    }
//
//    public static boolean isMac() {
//        return (System.getProperty("os.name").toLowerCase().contains("mac"));
//    }
//
//    public static boolean isUnix() {
//        return (System.getProperty("os.name").toLowerCase().contains("nix")
//                || System.getProperty("os.name").toLowerCase().contains("nux")
//                || System.getProperty("os.name").toLowerCase().contains("aix"));
//    }
//
//    public static boolean isSolaris() {
//        return (System.getProperty("os.name").toLowerCase().contains("sunos"));
//    }
    public boolean IsValidNumber(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isValidPhoneNumber(String phone_number) {
        boolean isValid = phone_number.matches("\\d{11}");
        return isValid;
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

    public Date StringToDate(String stringdate) {
        java.util.Date sf = null;
        try {
            sf = this.SimpleDateFormat("MM-dd-yyyy").parse(stringdate);
        } catch (ParseException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sf;
    }

    public Date StringToDateTime(String stringdatetime) {
        java.util.Date sf = null;
        try {
            sf = this.SimpleDateFormat("MM-dd-yyyy hh:mm a").parse(stringdatetime);
        } catch (ParseException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sf;
    }

    public String ComputeDateBackward(String dates, int diff) {
        String dateResults = String.valueOf(LocalDate.parse(dates).minusYears(diff).format(DateTimeFormatter.ofPattern("MM-dd-yyyy")));
        return dateResults;
    }

    public String AddMinusDaysDate(String date, String val) {
        String dateresult = "";
        try {
            SimpleDateFormat sdsf = this.SimpleDateFormat("MM-dd-yyyy");
            Calendar c = Calendar.getInstance();
            c.setTime(sdsf.parse(date.replaceAll("\\s", "")));
            c.add(Calendar.DAY_OF_MONTH, Integer.parseInt(val.replaceAll("\\s", "")));
            dateresult = sdsf.format(c.getTime());
        } catch (ParseException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dateresult;
    }

    public ACRGBWSResult ProcessDateAmountComputation(String datefrom, String dateto) {
        ACRGBWSResult result = this.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            Date ConvertDateTo = new SimpleDateFormat("MM-dd-yyyy").parse(dateto);
            Date ConvertDateFrom = new SimpleDateFormat("MM-dd-yyyy").parse(datefrom);
            SimpleDateFormat GetFromMonth = new SimpleDateFormat("MM");
            SimpleDateFormat GetFromDay = new SimpleDateFormat("dd");
            SimpleDateFormat GetFromYear = new SimpleDateFormat("yyyy");
            LinkedList<String> dateCollection = new LinkedList<>();
            //-------------- OBJECT DATE SETTING ORM  ----------------------------
            ArrayList<DateSettings> dateList = new ArrayList<>();
            for (int y = 1; y < 7; y++) {
                dateCollection.add(String.valueOf(Integer.parseInt(GetFromYear.format(ConvertDateFrom)) - y));
            }
            dateCollection.remove(0);
            //REMOVED PADEMIC YEAR  CLEANING
            dateCollection.remove("2020");
            dateCollection.remove("2021");
            switch (dateCollection.size()) {
                case 5: {
                    dateCollection.remove(4);
                    dateCollection.remove(3);
                    for (int i = 0; i < dateCollection.size(); i++) {
                        DateSettings datesettings = new DateSettings();
                        datesettings.setDatefrom(GetFromMonth.format(ConvertDateFrom) + "-" + GetFromDay.format(ConvertDateFrom) + "-" + dateCollection.get(i));
                        datesettings.setDateto(GetFromMonth.format(ConvertDateTo) + "-" + GetFromDay.format(ConvertDateTo) + "-" + dateCollection.get(i));
                        dateList.add(datesettings);
                    }
                    break;
                }
                case 4: {
                    dateCollection.remove(3);
                    for (int i = 0; i < dateCollection.size(); i++) {
                        DateSettings datesettings = new DateSettings();
                        datesettings.setDatefrom(GetFromMonth.format(ConvertDateFrom) + "-" + GetFromDay.format(ConvertDateFrom) + "-" + dateCollection.get(i));
                        datesettings.setDateto(GetFromMonth.format(ConvertDateTo) + "-" + GetFromDay.format(ConvertDateTo) + "-" + dateCollection.get(i));
                        dateList.add(datesettings);
                    }
                    break;
                }
                default: {
                    for (int i = 0; i < dateCollection.size(); i++) {
                        DateSettings datesettings = new DateSettings();
                        datesettings.setDatefrom(GetFromMonth.format(ConvertDateFrom) + "-" + GetFromDay.format(ConvertDateFrom) + "-" + dateCollection.get(i));
                        datesettings.setDateto(GetFromMonth.format(ConvertDateTo) + "-" + GetFromDay.format(ConvertDateTo) + "-" + dateCollection.get(i));
                        dateList.add(datesettings);
                    }
                    break;
                }
            }
            if (dateList.size() > 0) {
                result.setMessage("OK");
                result.setResult(this.ObjectMapper().writeValueAsString(dateList));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }

        } catch (NumberFormatException | ParseException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public boolean isValidEmail(String email) {
        boolean isValid = email.matches(email_pattern);
        return isValid;
    }

    //HTML TEMPLATE TO SENT EMAIL CONTENT
//    public String EmailTemplate(String useremail, String passcode) {
//        String EmailSentTemplate = "<body style='background-color:grey'>\n"
//                + "    <table align='center' border='0' cellpadding='0' cellspacing='0'\n"
//                + "           width='550' bgcolor='white' style='border:2px solid black'>\n"
//                + "        <tbody>\n"
//                + "            <tr>\n"
//                + "                <td align='center'>\n"
//                + "                    <table align='center' border='0' cellpadding='0' \n"
//                + "                           cellspacing='0' class='col-550' width='550'>\n"
//                + "                        <tbody>\n"
//                + "                            <tr>\n"
//                + "                                <td align='center' style='background-color: #4cb96b;\n"
//                + "                                           height: 50px;'>\n"
//                + "                                    <a href='#' style='text-decoration: none;'>\n"
//                + "                                        <p style='color:white;'"
//                + "                                                  font-weight:bold;'>\n"
//                + "                                            ACR-GB SYSTEM\n"
//                + "                                        </p>\n"
//                + "                                    </a>\n"
//                + "                                </td>\n"
//                + "                            </tr>\n"
//                + "                        </tbody>\n"
//                + "                    </table>\n"
//                + "                </td>\n"
//                + "            </tr>\n"
//                + "            <tr style='height: 300px;'>\n"
//                + "                <td align='center' style='border: none;\n"
//                + "                           border-bottom: 2px solid #4cb96b; \n"
//                + "                           padding-right: 20px;padding-left:20px'>\n"
//                + "\n"
//                + "                    <p style='font-weight: bolder;font-size: 42px;\n"
//                + "                              letter-spacing: 0.025em;\n"
//                + "                              color:black;'>\n"
//                + "                        Hello!\n"
//                + "                        <br> Check your user credentials for ACR-GB Login account \n"
//                + "                    </p>\n"
//                + "                </td>\n"
//                + "            </tr>\n"
//                + "\n"
//                + "            <tr style='display: inline-block;'>\n"
//                + "                <td style='height: 150px;\n"
//                + "                           padding: 20px;\n"
//                + "                           border: none; \n"
//                + "                           border-bottom: 2px solid #361B0E;\n"
//                + "                           background-color: white;'>\n"
//                + "                  \n"
//                + "                    <h2 style='text-align: left;\n"
//                + "                               align-items: center;'>\n"
//                + "                        Username : " + useremail + "\n"
//                + "                      Passcode : " + passcode + "\n"
//                + "                   </h2>\n"
//                + "                    <p class='data' \n"
//                + "                       style='text-align: justify-all;\n"
//                + "                              align-items: center; \n"
//                + "                              font-size: 15px;\n"
//                + "                              padding-bottom: 12px;'>\n"
//                + "                       !Note: Don't share your account credentials</p>\n"
//                + "                    <p> <a href='#'\n"
//                + "                           style='text-decoration: none; \n"
//                + "                                  color:black; \n"
//                + "                                  border: 2px solid #4cb96b; \n"
//                + "                                  padding: 10px 30px;\n"
//                + "                                  font-weight: bold;'> \n"
//                + "                           Read More \n"
//                + "                      </a>\n"
//                + "                    </p>\n"
//                + "                </td>\n"
//                + "            </tr>\n"
//                + "        </tbody>\n"
//                + "    </table>\n"
//                + "</body>";//, "text/html");
//
//        return EmailSentTemplate;
//    }
    //GENERATE TOKEN METHODS
    public String GenerateToken(String username, String password) {
        SignatureAlgorithm algorithm = SignatureAlgorithm.HS256;
        byte[] userkeybytes = DatatypeConverter.parseBase64Binary(CIPHERKEY);
        Key signingkey = new SecretKeySpec(userkeybytes, algorithm.getJcaName());
        JwtBuilder builder = Jwts.builder()
                .claim("Code1", EncryptString(username))
                .claim("Code2", EncryptString(password))
                .setExpiration(new Date(System.currentTimeMillis() + 30 * 480000))//ADD EXPIRE TIME 8HOURS

                .signWith(algorithm, signingkey);
        return builder.compact();

    }

    public String EncryptString(String string) {
        String result = null;
        try {
            SetKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretkey);
            result = Base64.getEncoder().encodeToString(cipher.doFinal(string.getBytes("UTF-8"))).replaceAll("=", "");
        } catch (UnsupportedEncodingException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private void SetKey() {
        MessageDigest sha = null;
        try {
            String userkey = CIPHERKEY;
            key = userkey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretkey = new SecretKeySpec(key, "AES");
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean ValidateToken(final String token) {
        boolean result = false;
        try {
            Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(CIPHERKEY)).parseClaimsJws(token).getBody();
            result = true;
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException ex) {
        }
        return result;
    }

    public String DecryptString(String string) {
        String result = null;
        try {
            SetKey();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretkey);
            result = new String(cipher.doFinal(Base64.getDecoder().decode(string)));
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
            result = ex.toString();
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GetPayload(
            final DataSource dataSource,
            final String token) {
        ACRGBWSResult result = this.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            if (token.equals("")) {
                result.setMessage("Token is required");
            } else {
                if (this.ValidateToken(token) == true) {
                    Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(CIPHERKEY)).parseClaimsJws(token).getBody();
                    if (!this.isJWTExpired(claims)) {
                        ACRGBPayload payload = this.ACRGBPayload();
                        payload.setCode1(this.DecryptString((String) claims.get("Code1")));
                        payload.setCode2(this.DecryptString((String) claims.get("Code2")));
                        payload.setExp(claims.getExpiration());
                        if (new Methods().ACRUSERLOGIN(dataSource, this.DecryptString((String) claims.get("Code1")).trim(), this.DecryptString((String) claims.get("Code2")).trim()).isSuccess()) {
                            result.setSuccess(true);
                        } else {
                            result.setMessage("Unrecognized User");
                        }
                    } else {
                        result.setMessage("Token is expired");
                    }
                } else {
                    result.setMessage("Invalid Token");
                }
            }
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException ex) {
            result.setMessage("Invalid Token : " + ex.getLocalizedMessage());
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);

        }
        return result;
    }

    public boolean isJWTExpired(Claims claims) {
        if (claims.getExpiration() == null) {
            return true;
        } else {
            Date expiresAt = claims.getExpiration();
            return expiresAt.before(new Date());
        }
    }
//CREATE 2FA CODE

    public String Create2FACode() {
        int randnums = 0;
        for (int i = 0; i < 1; i++) {
            randnums += (int) ((Math.random() * 88881) + 22220);
        }
        return String.valueOf(randnums);
    }

    //GET STRING NAME
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

    public String EmailSenderContent(String useremail, String passcode) {
        String result = "<body style=\"padding-top: 30px; font-family: sans-serif; border: groove 10px; max-width: 80%; margin-top: 10px; display: grid; align-items:center ;\">\n"
                + "    <div style=\"display:grid; place-items: center; padding:5px; width: 80%; margin:auto\">\n"
                + "       <div style=\"font-family:impact; color:#7bb241; font-size:3rem; margin-top: -3rem;\">\n"
                + "        <center>\n"
                + "            <h3>ACR-GB</h3>\n"
                + "        </center>\n"
                + "        </div>\n"
                + "        <h4 style=\"margin-top: -1rem;\">\n"
                + "            ACR-GB Login Credentials\n"
                + "        </h4>\n"
                + "    </div>\n"
                + "    <div class=\"content\" style=\"display:grid; place-items: left; padding:5px; width: auto; margin:auto; font-size: 13px;\">\n"
                + "        <span style=\"padding-bottom: 10px;\">Username:&nbsp; <b>" + useremail + "</b></span>\n"
                + "        <span>Password:&nbsp; <b> " + passcode + "</b></span>\n"
                + "          <br>\n"
                + "        <span class=\"note\" style=\"font-size: 10px; color: rgb(209, 71, 71); display:grid; place-items: left; padding:5px; width: auto; margin: auto;font-weight: 700;padding-bottom: 30px; align-items:flex-start; display: flex;\">\n"
                + "            Note: Don't share your account credentials\n"
                + "        </span>\n"
                + "    </div>\n"
                + "    <footer style=\"font-size: 10px; background-color:rgb(223, 220, 220); text-align: center; color:seagreen ; font-weight: 500; padding: 3px 3px;\">\n"
                + "        ACR-GB v1.0 © Copyright 2024 <br>\n"
                + "        Philippine Health Insurance Corporation | Citystate Centre, 709 Shaw Boulevard 1603 Pasig City \n"
                + "        <br>Call Center (+632) 441-7442\n"
                + "    </footer>\n"
                + "</body>\n"
                + "</html>";
        return result;
    }

}
