/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Accreditation;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.DateSettings;
import acrgb.structure.FacilityComputedAmount;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.MBRequestSummary;
import acrgb.structure.ManagingBoard;
import acrgb.structure.Pro;
import acrgb.structure.ReportsHCPNSummary;
import acrgb.structure.Total;
import acrgb.structure.User;
import acrgb.structure.UserActivity;
import acrgb.structure.UserInfo;
import acrgb.utility.Cryptor;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class Methods {

    public Methods() {
    }

    private final Utility utility = new Utility();
    private final FetchMethods fm = new FetchMethods();
    private final ContractMethod contractdate = new ContractMethod();
    private final Cryptor cryptor = new Cryptor();
    private final SimpleDateFormat datetimeformat = utility.SimpleDateFormat("MM-dd-yyyy hh:mm a");
    private final SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");

    //--------------------------------------------------------
// ACR GB USER ACCOUNT LOGIN
    public ACRGBWSResult ACRUSERLOGIN(final DataSource datasource, final String p_username, final String p_password) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try {
            ACRGBWSResult resultfm = fm.ACR_USER(datasource, "active");
            if (resultfm.isSuccess()) {
                List<User> userlist = Arrays.asList(utility.ObjectMapper().readValue(resultfm.getResult(), User[].class));
                int resultcounter = 0;
                for (int x = 0; x < userlist.size(); x++) {
                    UserPassword userPassword = new UserPassword();
                    if (userlist.get(x).getStatus().equals("2")) {
                        userPassword.setDbpass(cryptor.decrypt(userlist.get(x).getUserpassword(), p_password, "ACRGB"));
                    } else {
                        userPassword.setDbpass(userlist.get(x).getUserpassword());
                    }
                    if (userPassword.getDbpass() != null) {
                        if (userlist.get(x).getUsername().equals(p_username) && userPassword.getDbpass().equals(p_password)) {
                            User user = new User();
                            user.setUserid(userlist.get(x).getUserid());
                            user.setLeveid(userlist.get(x).getLeveid().toUpperCase());
                            user.setUsername(userlist.get(x).getUsername());
                            user.setUserpassword(userlist.get(x).getUserpassword());
                            user.setDatecreated(userlist.get(x).getDatecreated());
                            user.setStatus(userlist.get(x).getStatus());
                            ACRGBWSResult detailsresult = fm.GETFULLDETAILS(datasource, userlist.get(x).getUserid());
                            if (detailsresult.isSuccess()) {
                                user.setDid(detailsresult.getResult());
                            } else {
                                user.setDid(detailsresult.getMessage());
                            }
                            user.setCreatedby(userlist.get(x).getCreatedby());
                            result.setSuccess(true);
                            result.setResult(utility.ObjectMapper().writeValueAsString(user));
                            result.setMessage("OK");
                            resultcounter++;
                            break;
                        }
                    } else {
                        result.setResult("INVALID USERNAME AND PASSWORD");
                        result.setMessage("CREDENTIAL NOT FOUND");
                    }
                }

                if (resultcounter == 0) {
                    result.setMessage("CREDENTIAL NOT FOUND");
                    result.setResult("INVALID USERNAME AND PASSWORD");
                }
            } else {
                //result.setSuccess(false);
                result.setMessage("NO AVAILABLE DATA");
            }
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    //-------------------------- NEW OBJECT -----------------
    public class UserPassword {

        private String dbpass;

        public String getDbpass() {
            return dbpass;
        }

        public void setDbpass(String dbpass) {
            this.dbpass = dbpass;
        }
    }

    //-------------------------- NEW OBJECT -----------------
    //--------------------------------------------------------
    // ACR GB USERNAME CHECKING
    public ACRGBWSResult ACRUSERNAME(final DataSource dataSource, final String p_username) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :p_user := ACR_GB.ACRGBPKGFUNCTION.ACRUSERNAME(:p_username); end;");
            statement.registerOutParameter("p_user", OracleTypes.CURSOR);
            statement.setString("p_username", p_username);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("p_user");
            if (resultset.next()) {
                result.setMessage("USERNAME IS ALREADY EXIST");
                result.setResult(p_username);
            } else {
                result.setMessage("OK");
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //--------------------------------------------------------
    // ACR GB USER LEVEL
    public ACRGBWSResult ACRUSERLEVEL(final DataSource dataSource, final String p_levelid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_level := ACR_GB.ACRGBPKGFUNCTION.ACRUSERLEVEL(:p_levelid); end;");
            statement.registerOutParameter("v_level", OracleTypes.CURSOR);
            statement.setString("p_levelid", p_levelid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_level");
            if (resultset.next()) {
                result.setResult(resultset.getString("LEVNAME"));
                result.setSuccess(true);
            } else {
                result.setMessage("USER ROLE IS NOT AVAILABLE");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //--------------------------------------------------------
    // UPDATE USER INFORMATION
    public ACRGBWSResult UPDATEUSERDETAILS(final DataSource dataSource, final UserInfo userinfo) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.UPDATEUSERDETAILS(:Message,:Code,"
                    + ":p_firstname,:p_lastname,:p_middlename,:p_did,:p_email,:p_contact)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_firstname", userinfo.getFirstname().toUpperCase());
            getinsertresult.setString("p_lastname", userinfo.getLastname().toUpperCase());
            getinsertresult.setString("p_middlename", userinfo.getMiddlename().toUpperCase());
            getinsertresult.setString("p_email", userinfo.getEmail());
            getinsertresult.setString("p_contact", userinfo.getContact());
            getinsertresult.setString("p_did", userinfo.getDid());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //--------------------------------------------------------
    // ACR GB USER CREDENTIALS
    public ACRGBWSResult UPDATEUSERCREDENTIALS(final DataSource dataSource, final String userid, final String p_username, final String p_password) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
//            ACRGBWSResult usernameResult = this.ACRUSERNAME(dataSource, p_username);
//            if (usernameResult.isSuccess()) {
            if (!utility.validatePassword(p_password)) {
                result.setSuccess(false);
                result.setMessage("PASSWORD IS NOT VALID");
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.UPDATEUSERCREDENTIALS(:Message,:Code,"
                        + ":userid,:p_username,:p_password,:p_stats)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("userid", userid);
                getinsertresult.setString("p_username", p_username);
                getinsertresult.setString("p_password", cryptor.encrypt(p_password, p_password, "ACRGB"));
                getinsertresult.setString("p_stats", "2");
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setSuccess(true);
                    result.setMessage(getinsertresult.getString("Message"));
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // CHANGEUSERNAME
    public ACRGBWSResult CHANGEUSERNAME(final DataSource dataSource, final String userid, final String p_username) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEUSERNAME(:Message,:Code,:p_userid,:p_username,:p_stats)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_userid", userid);
            getinsertresult.setString("p_username", p_username);
            getinsertresult.setString("p_stats", "2");
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult RESETPASSWORD(final DataSource dataSource, final String userid, final String p_password) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            if (!utility.validatePassword(p_password)) {
                result.setSuccess(false);
                result.setMessage("PASSWORD IS NOT VALID");
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEPASSWORD(:Message,:Code,:p_userid,:p_password,:p_stats)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("p_userid", userid);
                getinsertresult.setString("p_password", p_password);
                getinsertresult.setString("p_stats", "1");
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setSuccess(true);
                    result.setMessage(getinsertresult.getString("Message"));
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //CHANGE PASSWORD
    public ACRGBWSResult CHANGEPASSWORD(final DataSource dataSource, final String userid, final String p_password) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            if (!utility.validatePassword(p_password)) {
                result.setSuccess(false);
                result.setMessage("PASSWORD IS NOT VALID");
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEPASSWORD(:Message,:Code,:p_userid,:p_password,:p_stats)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("p_userid", userid);
                getinsertresult.setString("p_password", cryptor.encrypt(p_password, p_password, "ACRGB"));
                getinsertresult.setString("p_stats", "2");
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setSuccess(true);
                    result.setMessage(getinsertresult.getString("Message"));
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // CHANGEUSELEVEL
    public ACRGBWSResult CHANGEUSELEVELID(final DataSource dataSource, final String userid, final String levelid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATELEVEL(:Message,:Code,:p_userid,:p_levelid,:p_stats)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_userid", userid);
            getinsertresult.setString("p_levelid", levelid);
            getinsertresult.setString("p_stats", "2");
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // CHANGEUSELEVEL
    public ACRGBWSResult GETSUMMARY(final DataSource dataSource, final String phcfid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETSUMMARY(:phcfid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("phcfid", phcfid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Total tot = new Total();
                tot.setCtotal(resultset.getString("cTOTAL"));
                tot.setHcfid(resultset.getString("HCFID"));
                tot.setCcount(resultset.getString("cCOUNT"));
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(tot));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //--------------------------------------------------------
    //ACR GB GET SUMMARY
    public ACRGBWSResult GetBaseAmount(final DataSource dataSource, final String tags, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            switch (tags.toUpperCase()) {
                case "USERPRO"://USERID IS PRO ACCOUNT USERID
                    ACRGBWSResult getPRO = this.GETROLE(dataSource, userid);
                    if (getPRO.isSuccess()) {
                        ACRGBWSResult getHCPNUnderUsingProCode = this.GETROLEMULITPLE(dataSource, getPRO.getResult());
                        if (getHCPNUnderUsingProCode.isSuccess()) {
                            ArrayList<String> HCIList = new ArrayList<>();
                            List<String> ListOFHCPN = Arrays.asList(getHCPNUnderUsingProCode.getResult().split(","));
                            for (int hcpn = 0; hcpn < ListOFHCPN.size(); hcpn++) {
                                ACRGBWSResult getHCIUisngHCPNCode = this.GETROLEMULITPLE(dataSource, ListOFHCPN.get(hcpn));
                                if (getHCIUisngHCPNCode.isSuccess()) {
                                    List<String> ListOHCI = Arrays.asList(getHCIUisngHCPNCode.getResult().split(","));
                                    for (int hci = 0; hci < ListOHCI.size(); hci++) {
                                        HCIList.add(ListOHCI.get(hci));
                                    }
                                }
                            }
                            Double totalSkipYearClaimAmount = 0.00;
                            Double totalDateSettingYearClaimAmount = 0.00;
                            int totalclaimcountskipyear = 0;
                            int totalclaimcountdatesetting = 0;
                            FacilityComputedAmount totalcompute = new FacilityComputedAmount();
                            for (int x = 0; x < HCIList.size(); x++) {
                                ACRGBWSResult restA = this.GetAmountPerFacility(dataSource, HCIList.get(x));
                                if (restA.isSuccess()) {
                                    FacilityComputedAmount fcaA = utility.ObjectMapper().readValue(restA.getResult(), FacilityComputedAmount.class);
                                    totalcompute.setYearfrom(fcaA.getYearfrom());
                                    totalcompute.setYearto(fcaA.getYearto());
                                    //GET SKIP YEAR AMOUNT
                                    ACRGBWSResult restB = this.GetAmountPerFacilitySkipYear(dataSource, HCIList.get(x));
                                    if (restB.isSuccess()) {
                                        FacilityComputedAmount fcaB = utility.ObjectMapper().readValue(restB.getResult(), FacilityComputedAmount.class);
                                        //SKIP YEAR VALUE
                                        totalSkipYearClaimAmount += Double.parseDouble(fcaB.getTotalamount());
                                        totalclaimcountskipyear += Integer.parseInt(fcaB.getTotalclaims());
                                        //DATE SETTINGS
                                        totalDateSettingYearClaimAmount += Double.parseDouble(fcaA.getTotalamount());
                                        totalclaimcountdatesetting += Integer.parseInt(fcaA.getTotalclaims());
                                        //===========================================================
                                    } else {
                                        totalDateSettingYearClaimAmount += Double.parseDouble(fcaA.getTotalamount());
                                        totalclaimcountdatesetting += Integer.parseInt(fcaA.getTotalclaims());
                                    }
                                }
                            }
                            totalcompute.setTotalamount(String.valueOf(totalDateSettingYearClaimAmount - totalSkipYearClaimAmount));
                            totalcompute.setTotalclaims(String.valueOf(totalclaimcountdatesetting - totalclaimcountskipyear));
                            ACRGBWSResult getPROCode = this.GetProWithPROID(dataSource, getPRO.getResult());
                            if (getPROCode.isSuccess()) {
                                totalcompute.setHospital(getPROCode.getResult());
                            } else {
                                totalcompute.setHospital(getPROCode.getMessage());
                            }
                            if (!HCIList.isEmpty()) {
                                result.setMessage("OK");
                                result.setResult(utility.ObjectMapper().writeValueAsString(totalcompute));
                                result.setSuccess(true);
                            } else {
                                result.setMessage("N/A");
                            }

                        } else {
                            result.setMessage(getHCPNUnderUsingProCode.getMessage());
                        }
                    } else {
                        result.setMessage(getPRO.getMessage());
                    }
                    break;
                case "PRO"://USERID IS PROCODE
                    ACRGBWSResult getHCPNUnder = this.GETROLEMULITPLE(dataSource, userid);
                    if (getHCPNUnder.isSuccess()) {
                        ArrayList<String> finalListofHCPN = new ArrayList<>();
                        List<String> HCPNList = Arrays.asList(getHCPNUnder.getResult().split(","));
                        for (int pro = 0; pro < HCPNList.size(); pro++) {
                            ACRGBWSResult getHCFUnder = this.GETROLEMULITPLE(dataSource, HCPNList.get(pro));
                            if (getHCFUnder.isSuccess()) {
                                List<String> HCFList = Arrays.asList(getHCFUnder.getResult().split(","));
                                for (int hcf = 0; hcf < HCFList.size(); hcf++) {
                                    finalListofHCPN.add(HCFList.get(hcf));
                                }
                            }
                        }

                        Double totalSkipYearClaimAmount = 0.00;
                        Double totalDateSettingYearClaimAmount = 0.00;
                        int totalclaimcountskipyear = 0;
                        int totalclaimcountdatesetting = 0;
                        FacilityComputedAmount totalcompute = new FacilityComputedAmount();
                        for (int listhci = 0; listhci < finalListofHCPN.size(); listhci++) {
                            ACRGBWSResult restA = this.GetAmountPerFacility(dataSource, finalListofHCPN.get(listhci));
                            if (restA.isSuccess()) {
                                FacilityComputedAmount fcaA = utility.ObjectMapper().readValue(restA.getResult(), FacilityComputedAmount.class);
                                totalcompute.setYearfrom(fcaA.getYearfrom());
                                totalcompute.setYearto(fcaA.getYearto());
                                //GET SKIP YEAR AMOUNT
                                ACRGBWSResult restB = this.GetAmountPerFacilitySkipYear(dataSource, finalListofHCPN.get(listhci));
                                if (restB.isSuccess()) {
                                    FacilityComputedAmount fcaB = utility.ObjectMapper().readValue(restB.getResult(), FacilityComputedAmount.class);
                                    //SKIP YEAR VALUE
                                    totalSkipYearClaimAmount += Double.parseDouble(fcaB.getTotalamount());
                                    totalclaimcountskipyear += Integer.parseInt(fcaB.getTotalclaims());
                                    //DATE SETTINGS
                                    totalDateSettingYearClaimAmount += Double.parseDouble(fcaA.getTotalamount());
                                    totalclaimcountdatesetting += Integer.parseInt(fcaA.getTotalclaims());
                                    //===========================================================
                                } else {
                                    totalDateSettingYearClaimAmount += Double.parseDouble(fcaA.getTotalamount());
                                    totalclaimcountdatesetting += Integer.parseInt(fcaA.getTotalclaims());
                                }
                            }
                        }
                        totalcompute.setTotalamount(String.valueOf(totalDateSettingYearClaimAmount - totalSkipYearClaimAmount));
                        totalcompute.setTotalclaims(String.valueOf(totalclaimcountdatesetting - totalclaimcountskipyear));
                        //----------------------------------------------------------
                        ACRGBWSResult getPROCode = this.GetProWithPROID(dataSource, userid);
                        if (getPROCode.isSuccess()) {
                            totalcompute.setHospital(getPROCode.getResult());
                        } else {
                            totalcompute.setHospital(getPROCode.getMessage());
                        }
                        //----------------------------------------------------------
                        if (!finalListofHCPN.isEmpty()) {
                            result.setMessage("OK");
                            result.setResult(utility.ObjectMapper().writeValueAsString(totalcompute));
                            result.setSuccess(true);
                        } else {
                            result.setMessage("N/A");
                        }
                    } else {
                        result.setMessage(getHCPNUnder.getMessage());
                    }
                    break;
                case "FACILITY"://USERID IS HCFCODE/ACCRENO
                    FacilityComputedAmount totalcompute = new FacilityComputedAmount();
                    ACRGBWSResult restA = this.GetAmountPerFacility(dataSource, userid);
                    if (restA.isSuccess()) {
                        FacilityComputedAmount fcaA = utility.ObjectMapper().readValue(restA.getResult(), FacilityComputedAmount.class);
                        //GET FACILITY
                        ACRGBWSResult getFacility = fm.GETFACILITYID(dataSource, userid);
                        if (getFacility.isSuccess()) {
                            totalcompute.setHospital(getFacility.getResult());
                        } else {
                            totalcompute.setHospital(getFacility.getMessage());
                        }
                        //GET SKIP YEAR AMOUNT
                        ACRGBWSResult restB = this.GetAmountPerFacilitySkipYear(dataSource, userid);
                        if (restB.isSuccess()) {
                            FacilityComputedAmount fcaB = utility.ObjectMapper().readValue(restB.getResult(), FacilityComputedAmount.class);
                            //SKIP YEAR VALUE
                            Double skipclaimsValue = Double.parseDouble(fcaB.getTotalamount());
                            int skipclaimcount = Integer.parseInt(fcaB.getTotalclaims());
                            //DATE SETTINGS
                            Double datesettingsclaimsValue = Double.parseDouble(fcaA.getTotalamount());
                            int dateclaimcount = Integer.parseInt(fcaA.getTotalclaims());
                            //===========================================================
                            totalcompute.setTotalamount(String.valueOf(datesettingsclaimsValue - skipclaimsValue));
                            totalcompute.setTotalclaims(String.valueOf(dateclaimcount - skipclaimcount));
                            totalcompute.setYearfrom(fcaA.getYearfrom());
                            totalcompute.setYearto(fcaA.getYearto());
                        } else {
                            Double datesettingsclaimsValue = Double.parseDouble(fcaA.getTotalamount());
                            int dateclaimcount = Integer.parseInt(fcaA.getTotalclaims());
                            totalcompute.setTotalamount(String.valueOf(datesettingsclaimsValue));
                            totalcompute.setTotalclaims(String.valueOf(dateclaimcount));
                            totalcompute.setYearfrom(fcaA.getYearfrom());
                            totalcompute.setYearto(fcaA.getYearto());
                        }
                        result.setResult(utility.ObjectMapper().writeValueAsString(totalcompute));
                        result.setMessage("OK");
                        result.setSuccess(true);
                    } else {
                        result.setMessage(restA.getMessage());
                    }
                    break;
                case "HCPN"://USERID IS HCPNCODE/ACCRENO
                    //GET ALL FACILITY UNDER OF HCPN
                    ACRGBWSResult getFacilityUnder = this.GETROLEMULITPLE(dataSource, userid);
                    FacilityComputedAmount totalcomputeHCPN = new FacilityComputedAmount();
                    if (getFacilityUnder.isSuccess()) {
                        List<String> hcflist = Arrays.asList(getFacilityUnder.getResult().split(","));
                        Double totalSkipYearClaimAmount = 0.00;
                        Double totalDateSettingYearClaimAmount = 0.00;
                        int totalclaimcountskipyear = 0;
                        int totalclaimcountdatesetting = 0;
                        for (int y = 0; y < hcflist.size(); y++) {
                            ACRGBWSResult restC = this.GetAmountPerFacility(dataSource, hcflist.get(y));
                            if (restC.isSuccess()) {
                                FacilityComputedAmount fcaA = utility.ObjectMapper().readValue(restC.getResult(), FacilityComputedAmount.class);
                                totalcomputeHCPN.setYearfrom(fcaA.getYearfrom());
                                totalcomputeHCPN.setYearto(fcaA.getYearto());
                                //GET SKIP YEAR AMOUNT
                                ACRGBWSResult restB = this.GetAmountPerFacilitySkipYear(dataSource, hcflist.get(y));
                                if (restB.isSuccess()) {
                                    FacilityComputedAmount fcaB = utility.ObjectMapper().readValue(restB.getResult(), FacilityComputedAmount.class);
                                    //SKIP YEAR VALUE
                                    Double skipclaimsValue = Double.parseDouble(fcaB.getTotalamount());
                                    totalSkipYearClaimAmount += skipclaimsValue;
                                    int skipclaimcount = Integer.parseInt(fcaB.getTotalclaims());
                                    totalclaimcountskipyear += skipclaimcount;
                                    //DATE SETTINGS
                                    Double datesettingsclaimsValue = Double.parseDouble(fcaA.getTotalamount());
                                    totalDateSettingYearClaimAmount += datesettingsclaimsValue;
                                    int dateclaimcount = Integer.parseInt(fcaA.getTotalclaims());
                                    totalclaimcountdatesetting += dateclaimcount;
                                } else {
                                    Double datesettingsclaimsValue = Double.parseDouble(fcaA.getTotalamount());
                                    totalDateSettingYearClaimAmount += datesettingsclaimsValue;
                                    int dateclaimcount = Integer.parseInt(fcaA.getTotalclaims());
                                    totalclaimcountdatesetting += dateclaimcount;
                                }
                            }
                        }
                        totalcomputeHCPN.setTotalamount(String.valueOf(totalDateSettingYearClaimAmount - totalSkipYearClaimAmount));
                        totalcomputeHCPN.setTotalclaims(String.valueOf(totalclaimcountdatesetting - totalclaimcountskipyear));
                        //GET HCPN
                        ACRGBWSResult getHCPN = this.GETMBWITHID(dataSource, userid);
                        if (getHCPN.isSuccess()) {
                            totalcomputeHCPN.setHospital(getHCPN.getResult());
                        } else {
                            totalcomputeHCPN.setHospital(getHCPN.getMessage());
                        }
                        //END OF GETTING HCPN
                        if (!hcflist.isEmpty()) {
                            result.setMessage("OK");
                            result.setResult(utility.ObjectMapper().writeValueAsString(totalcomputeHCPN));
                            result.setSuccess(true);
                        } else {
                            result.setMessage("N/A");
                        }
                    } else {
                        result.setMessage(getFacilityUnder.getMessage());
                    }
                    break;
            }

        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // ACR GB USER ACTIVITY LOGS
    public ACRGBWSResult ActivityLogs(final DataSource dataSource, final UserActivity useractivity) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        java.util.Date d1 = new java.util.Date();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.ACTIVITYLOGS(:Message,:Code,:a_date,:a_details,:a_by)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setTimestamp("a_date", new java.sql.Timestamp(d1.getTime()));
            getinsertresult.setString("a_details", useractivity.getActdetails().toUpperCase());
            getinsertresult.setInt("a_by", Integer.parseInt(useractivity.getActby()));
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage(getinsertresult.getString("Message"));

            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // ACR GB USER ACTIVITY LOGS WITH PARAMETER
    public ACRGBWSResult GetLogsWithID(final DataSource dataSource, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETLOGSWITHID(:userid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("userid", userid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                UserActivity logs = new UserActivity();
                logs.setActby(resultset.getString("ACTBY"));
                logs.setActdate(datetimeformat.format(resultset.getDate("ACTDATE")));
                logs.setActdetails(resultset.getString("ACTDETAILS"));
                logs.setActid(resultset.getString("ACTID"));
                result.setResult(utility.ObjectMapper().writeValueAsString(logs));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("NO DATA FOUND");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET AMOUNT PER FACILITY
    public ACRGBWSResult GetAmountPerFacility(final DataSource dataSource, final String uaccreno) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            ACRGBWSResult getdatesettings = fm.GETDATESETTINGS(dataSource);
            if (getdatesettings.isSuccess()) {
                DateSettings ds = utility.ObjectMapper().readValue(getdatesettings.getResult(), DateSettings.class);
                CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETSUMAMOUNTCLAIMS(:ulevel,:uaccreno,:utags,:udatefrom,:udateto); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("ulevel", "TWO");
                statement.setString("uaccreno", uaccreno);
                statement.setString("utags", ds.getTags());
                statement.setDate("udatefrom", (Date) new Date(utility.StringToDate(ds.getDatefrom()).getTime()));
                statement.setDate("udateto", (Date) new Date(utility.StringToDate(ds.getDateto()).getTime()));
                statement.execute();
                ResultSet resultset = (ResultSet) statement.getObject("v_result");
                if (resultset.next()) {
                    FacilityComputedAmount fca = new FacilityComputedAmount();
                    fca.setHospital(resultset.getString("PMCC_NO"));
                    fca.setTotalamount(resultset.getString("CTOTAL"));
                    fca.setYearfrom(ds.getDatefrom());
                    fca.setYearto(ds.getDateto());
                    fca.setTotalclaims(resultset.getString("COUNTVAL"));
                    result.setResult(utility.ObjectMapper().writeValueAsString(fca));
                    result.setMessage("OK");
                    result.setSuccess(true);
                } else {
                    result.setMessage("N/A");
                }
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET AMOUNT PER FACILITY FROM SKIP YEAR
    public ACRGBWSResult GetAmountPerFacilitySkipYear(DataSource dataSource, final String uaccreno) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            ACRGBWSResult skipyear = fm.GETSKIPYEAR(dataSource);
            if (skipyear.isSuccess()) {
                DateSettings ds = utility.ObjectMapper().readValue(skipyear.getResult(), DateSettings.class);
                CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETSKIPYEARAMOUNT(:uaccreno,:utags,:udatefrom,:udateto); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("uaccreno", uaccreno);
                statement.setString("utags", ds.getTags());
                statement.setDate("udatefrom", (Date) new Date(utility.StringToDate(ds.getDatefrom()).getTime()));
                statement.setDate("udateto", (Date) new Date(utility.StringToDate(ds.getDateto()).getTime()));
                statement.execute();
                ResultSet resultset = (ResultSet) statement.getObject("v_result");
                if (resultset.next()) {
                    FacilityComputedAmount fca = new FacilityComputedAmount();
                    fca.setHospital(resultset.getString("PMCC_NO"));
                    fca.setTotalamount(resultset.getString("CTOTAL"));
                    fca.setYearfrom(ds.getDatefrom());
                    fca.setYearto(ds.getDateto());
                    result.setResult(utility.ObjectMapper().writeValueAsString(fca));
                    result.setMessage("OK");
                    result.setSuccess(true);
                } else {
                    result.setMessage("N/A");
                }

            } else {
                result.setMessage(skipyear.getMessage());
            }

        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //INSERT MB REQUEST
    public ACRGBWSResult InsertMBRequest(DataSource dataSource, final MBRequestSummary mbrequestsummry) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        java.util.Date d1 = new java.util.Date();
        try (Connection connection = dataSource.getConnection()) {
            if (!utility.IsValidDate(mbrequestsummry.getDaterequest()) || !utility.IsValidDate(mbrequestsummry.getYearfrom()) || !utility.IsValidDate(mbrequestsummry.getYearto())) {
                result.setMessage("DATE FORMAT IS NOT VALID MM-dd-yyyy");
                result.setSuccess(false);
            } else if (!utility.IsValidNumber(mbrequestsummry.getTotalamount())) {
                result.setMessage("AMOUNT FORMAT IS NOT VALID");
                result.setSuccess(false);
            } else {
                ACRGBWSResult restA = this.GETROLE(dataSource, mbrequestsummry.getRequestor());
                if (restA.isSuccess()) {
                    CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.MBREQUEST(:Message,:Code,:udaterequest,:udatefrom,"
                            + ":udateto,:urequestor,:utranscode,:uremarks,:uamount,:udatecreated)");
                    getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                    getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                    getinsertresult.setDate("udaterequest", (Date) new Date(utility.StringToDate(mbrequestsummry.getDaterequest()).getTime()));
                    getinsertresult.setDate("udatefrom", (Date) new Date(utility.StringToDate(mbrequestsummry.getYearfrom()).getTime()));
                    getinsertresult.setDate("udateto", (Date) new Date(utility.StringToDate(mbrequestsummry.getYearto()).getTime()));
                    getinsertresult.setInt("urequestor", Integer.parseInt(restA.getResult()));
                    getinsertresult.setString("utranscode", mbrequestsummry.getTranscode());
                    getinsertresult.setString("uremarks", mbrequestsummry.getRemarks());
                    getinsertresult.setString("uamount", mbrequestsummry.getTotalamount());
                    getinsertresult.setTimestamp("udatecreated", new java.sql.Timestamp(d1.getTime()));
                    getinsertresult.execute();
                    if (getinsertresult.getString("Message").equals("SUCC")) {
                        ArrayList<String> errorList = new ArrayList<>();
                        List<String> facilitylist = Arrays.asList(mbrequestsummry.getFacility().split(","));
                        int errCounter = 0;
                        for (int x = 0; x < facilitylist.size(); x = x + 2) {
                            CallableStatement statement = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.MBREQUESTFCHUNDER(:Message,:Code,:utranscode,:udatecreated,:uamount,:ufacility)");
                            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
                            statement.registerOutParameter("Code", OracleTypes.INTEGER);
                            statement.setString("utranscode", mbrequestsummry.getTranscode());
                            statement.setTimestamp("udatecreated", new java.sql.Timestamp(d1.getTime()));
                            statement.setString("uamount", facilitylist.get(x + 1));
                            statement.setString("ufacility", facilitylist.get(x));
                            statement.execute();
                            if (!statement.getString("Message").equals("SUCC")) {
                                errCounter++;
                                errorList.add(getinsertresult.getString("Message"));
                            }
                        }

                        if (errCounter == 0) {
                            result.setMessage("OK");
                            result.setSuccess(true);
                        } else {
                            result.setMessage(errorList.toString());
                        }
                    } else {
                        result.setMessage(getinsertresult.getString("Message"));
                    }

                }

            }
        } catch (SQLException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET FHCI WITH BADGET USING MANAGING BOARD USERID
    public ACRGBWSResult MethodGetHealthFacilityBadget(final DataSource dataSource, final String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<String> accessidlist = new ArrayList<>();
            ACRGBWSResult restA = this.GETROLE(dataSource, puserid);
            if (restA.isSuccess()) {
                ACRGBWSResult restB = this.GETROLEMULITPLE(dataSource, restA.getResult());
                List<String> restist = Arrays.asList(restB.getResult().split(","));
                for (int y = 0; y < restist.size(); y++) {
                    accessidlist.add(restist.get(y));
                }
            }
            ArrayList<HealthCareFacility> listHCF = new ArrayList<>();
            for (int t = 0; t < accessidlist.size(); t++) {
                CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETFACILITY(:hcfrid); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("hcfrid", accessidlist.get(t));
                statement.execute();
                ResultSet resultset = (ResultSet) statement.getObject("v_result");
                while (resultset.next()) {
                    HealthCareFacility hcf = new HealthCareFacility();
                    hcf.setHcfid(resultset.getString("HCFID"));
                    // GET MANAGING BOARD USING FACILITY ID
                    ACRGBWSResult restB = this.GETROLEREVERESE(dataSource, resultset.getString("HCFCODE"));
                    if (restB.isSuccess()) {
                        ACRGBWSResult mgresult = this.GETMBWITHID(dataSource, restB.getResult());
                        if (mgresult.isSuccess()) {
                            ManagingBoard mb = utility.ObjectMapper().readValue(mgresult.getResult(), ManagingBoard.class);
                            hcf.setMb(mb.getMbname());
                            //GET PRO
                            ACRGBWSResult restC = this.GETROLEREVERESE(dataSource, mb.getControlnumber());
                            if (restC.isSuccess()) {
                                //GET PRO USING PROID
                                ACRGBWSResult getproid = this.GetProWithPROID(dataSource, restC.getResult());
                                if (getproid.isSuccess()) {
                                    Pro pro = utility.ObjectMapper().readValue(getproid.getResult(), Pro.class);
                                    hcf.setProid(pro.getProname());
                                } else {
                                    hcf.setProid(getproid.getMessage());
                                }
                            } else {
                                hcf.setProid(restC.getMessage());
                            }
                            //GET PRO
                        } else {
                            hcf.setMb(mgresult.getMessage());
                        }
                    }
                    // GET MANAGING BOARD USING FACILITY ID
                    hcf.setHcfname(resultset.getString("HCFNAME"));
                    hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                    hcf.setHcfcode(resultset.getString("HCFCODE"));
                    //GET DATE CREATOR
                    ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                    if (creator.isSuccess()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                        hcf.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        hcf.setCreatedby(creator.getMessage());
                    }
                    //END OF GET DATE CREATOR
                    hcf.setType(resultset.getString("HCFTYPE"));
                    hcf.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                    // GET BADGET 
                    //FacilityComputedAmount
                    ACRGBWSResult getBadgetResult = this.GetAmountPerFacility(dataSource, resultset.getString("HCFCODE"));//GET TOTAL CLAIMS AMOUNT FOR GOOD TAGS
                    if (getBadgetResult.isSuccess()) {
                        FacilityComputedAmount getBadgetFirst = utility.ObjectMapper().readValue(getBadgetResult.getResult(), FacilityComputedAmount.class);
                        ACRGBWSResult getBadgetFirstSecond = this.GetAmountPerFacilitySkipYear(dataSource, getBadgetFirst.getHospital());//GET TOTAL BADGET FROM SKIP YEAR                         
                        if (getBadgetFirstSecond.isSuccess()) {
                            FacilityComputedAmount combadget = utility.ObjectMapper().readValue(getBadgetFirstSecond.getResult(), FacilityComputedAmount.class);
                            Double skipamount = Double.parseDouble(combadget.getTotalamount());
                            Double totalamount = Double.parseDouble(getBadgetFirst.getTotalamount());
                            String diff = String.valueOf(totalamount - skipamount);
                            hcf.setAmount(diff);
                        } else {
                            hcf.setAmount(getBadgetFirst.getTotalamount());
                        }
                        hcf.setTotalclaims(getBadgetFirst.getTotalclaims());

                    } else {
                        hcf.setAmount(getBadgetResult.getMessage());
                    }
                    hcf.setGbtags(resultset.getString("GB"));
                    listHCF.add(hcf);
                }
            }
            if (listHCF.size() < 1) {
                result.setMessage("N/A");
            } else {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(listHCF));
                result.setSuccess(true);
            }

        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET FHCI WITH BADGET USING MANAGING BOARD MBID
    public ACRGBWSResult MethodGetHealthFacilityBadgetUisngMBID(final DataSource dataSource, final String mbid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            ACRGBWSResult restA = this.GETROLEMULITPLE(dataSource, mbid);
            List<String> accessidlist = Arrays.asList(restA.getResult().split(","));
            ArrayList<HealthCareFacility> listHCF = new ArrayList<>();
            for (int t = 0; t < accessidlist.size(); t++) {
                CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETFACILITY(:hcfrid); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("hcfrid", accessidlist.get(t));
                statement.execute();
                ResultSet resultset = (ResultSet) statement.getObject("v_result");
                while (resultset.next()) {
                    HealthCareFacility hcf = new HealthCareFacility();
                    hcf.setHcfid(resultset.getString("HCFID"));
                    //GET MANAGING BOARD USING FACILITY ID
                    ACRGBWSResult restB = this.GETROLEREVERESE(dataSource, resultset.getString("HCFID"));
                    if (restB.isSuccess()) {
                        ACRGBWSResult mgresult = this.GETMBWITHID(dataSource, restB.getResult());
                        if (mgresult.isSuccess()) {
                            ManagingBoard mb = utility.ObjectMapper().readValue(mgresult.getResult(), ManagingBoard.class);
                            hcf.setMb(mb.getMbname());
                            //GET PRO
                            ACRGBWSResult restC = this.GETROLEREVERESE(dataSource, mb.getMbid());
                            if (restC.isSuccess()) {
                                //GET PRO USING PROID
                                ACRGBWSResult getproid = this.GetProWithPROID(dataSource, restC.getResult());
                                if (getproid.isSuccess()) {
                                    Pro pro = utility.ObjectMapper().readValue(getproid.getResult(), Pro.class);
                                    hcf.setProid(pro.getProname());
                                } else {
                                    hcf.setProid(getproid.getMessage());
                                }
                            } else {
                                hcf.setProid(restC.getMessage());
                            }
//                            //GET PRO
                        } else {
                            hcf.setMb(mgresult.getMessage());
                        }
                    }
                    //GET MANAGING BOARD USING FACILITY ID
                    hcf.setHcfname(resultset.getString("HCFNAME"));
                    hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                    hcf.setHcfcode(resultset.getString("HCFCODE"));
                    //GET DATE CREATOR
                    ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                    if (creator.isSuccess()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                        hcf.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        hcf.setCreatedby(creator.getMessage());
                    }
                    //END OF GET DATE CREATOR
                    hcf.setType(resultset.getString("HCFTYPE"));
                    hcf.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                    // GET BADGET 
                    //FacilityComputedAmount
                    ACRGBWSResult getBadgetResult = this.GetAmountPerFacility(dataSource, resultset.getString("HCFCODE"));//GET TOTAL CLAIMS AMOUNT FOR GOOD TAGS
                    if (getBadgetResult.isSuccess()) {
                        if (!getBadgetResult.getResult().isEmpty()) {
                            FacilityComputedAmount getBadgetFirst = utility.ObjectMapper().readValue(getBadgetResult.getResult(), FacilityComputedAmount.class);
                            ACRGBWSResult getBadgetFirstSecond = this.GetAmountPerFacilitySkipYear(dataSource, getBadgetFirst.getHospital());//GET TOTAL BADGET FROM SKIP YEAR
                            if (getBadgetFirstSecond.isSuccess()) {
                                FacilityComputedAmount combadget = utility.ObjectMapper().readValue(getBadgetFirstSecond.getResult(), FacilityComputedAmount.class);
                                Double skipamount = Double.parseDouble(combadget.getTotalamount());
                                Double totalamount = Double.parseDouble(getBadgetFirst.getTotalamount());
                                String diff = String.valueOf(totalamount - skipamount);
                                hcf.setAmount(diff);
                            } else {
                                hcf.setAmount(getBadgetFirst.getTotalamount());
                            }
                            hcf.setTotalclaims(getBadgetFirst.getTotalclaims());
                        } else {
                            hcf.setAmount("N/A");
                        }
                    } else {
                        hcf.setAmount(getBadgetResult.getMessage());
                    }
                    listHCF.add(hcf);
                }
            }

            if (listHCF.size() < 1) {
                result.setMessage("N/A");
            } else {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(listHCF));
            }

        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // GET ALL REQUEST USING MB USERID ACCOUNT
    public ACRGBWSResult FetchMBRequest(final DataSource dataSource, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETMBREQUEST(:userid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("userid", userid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<MBRequestSummary> mbrequestlist = new ArrayList<>();
            while (resultset.next()) {
                MBRequestSummary mbrequest = new MBRequestSummary();
                mbrequest.setMbrid(resultset.getString("MBRID"));
                mbrequest.setTotalamount(resultset.getString("AMOUNT"));
                mbrequest.setDaterequest(dateformat.format(resultset.getDate("DATEREQUEST")));
                mbrequest.setYearfrom(dateformat.format(resultset.getDate("DATEFROM")));
                mbrequest.setYearto(dateformat.format(resultset.getDate("DATETO")));
                mbrequest.setRequestor(resultset.getString("REQUESTOR"));
                mbrequest.setTranscode(resultset.getString("TRANSCODE"));
                mbrequest.setReqstatus(resultset.getString("STATUS"));
                mbrequest.setRemarks(resultset.getString("REMARKS"));
                mbrequest.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                mbrequestlist.add(mbrequest);
            }

            if (!mbrequestlist.isEmpty()) {
                result.setResult(utility.ObjectMapper().writeValueAsString(mbrequestlist));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ACCESS LEVEL USING USERID
    public ACRGBWSResult GETROLEWITHID(final DataSource dataSource, final String pid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            if (!utility.IsValidNumber(pid)) {
                result.setMessage("INVALID NUMBER FORMAT");
            } else {
                ACRGBWSResult restA = this.GETROLE(dataSource, pid);
                if (restA.isSuccess()) {
                    ACRGBWSResult restB = this.GETROLEMULITPLE(dataSource, restA.getResult());
                    List<String> accessidlist = Arrays.asList(restB.getResult().split(","));
                    ArrayList<ManagingBoard> mblist = new ArrayList<>();
                    ArrayList<HealthCareFacility> facilitylist = new ArrayList<>();
                    if (accessidlist.size() > 0) {
                        for (int x = 0; x < accessidlist.size(); x++) {
                            ACRGBWSResult getmbresult = this.GETMBWITHID(dataSource, accessidlist.get(x));
                            if (getmbresult.isSuccess()) {
                                ManagingBoard managingboard = utility.ObjectMapper().readValue(getmbresult.getResult(), ManagingBoard.class);
                                //GET FALCITY UNDER EVERY MB
                                ACRGBWSResult restC = this.GETROLEMULITPLE(dataSource, managingboard.getControlnumber());
                                List<String> facilityidlist = Arrays.asList(restC.getResult().split(","));
                                for (int y = 0; y < facilityidlist.size(); y++) {
                                    ACRGBWSResult getfacility = fm.GETFACILITYID(dataSource, facilityidlist.get(y));
                                    if (getfacility.isSuccess()) {
                                        HealthCareFacility facility = utility.ObjectMapper().readValue(getfacility.getResult(), HealthCareFacility.class);
                                        HealthCareFacility newfacility = new HealthCareFacility();
                                        newfacility.setAmount(facility.getAmount());
                                        newfacility.setType(facility.getType());
                                        newfacility.setCreatedby(facility.getCreatedby());
                                        newfacility.setDatecreated(facility.getDatecreated());
                                        newfacility.setHcfaddress(facility.getHcfaddress());
                                        newfacility.setHcfcode(facility.getHcfcode());
                                        newfacility.setHcfid(facility.getHcfid());
                                        newfacility.setHcfname(facility.getHcfname());
                                        newfacility.setMb(utility.ObjectMapper().writeValueAsString(managingboard));
                                        facilitylist.add(newfacility);
                                    }
                                }
                                mblist.add(managingboard);
                            }
                        }
                    } else {
                        result.setMessage("NO DATA FOUND");
                    }
                    if (mblist.size() > 0) {
                        result.setResult(utility.ObjectMapper().writeValueAsString(facilitylist));
                        result.setMessage("OK");
                        result.setSuccess(true);
                    } else {
                        result.setMessage("NO DATA FOUND");
                    }
                } else {
                    result.setMessage("NO DATA FOUND");
                }
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET FACILITY UNDER MB USING USERID
    public ACRGBWSResult GETFACILITYUNDERMBUSER(final DataSource dataSource, final String pid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            if (!utility.IsValidNumber(pid)) {
                result.setMessage("INVALID NUMBER FORMAT");
            } else {
                ACRGBWSResult restA = this.GETROLE(dataSource, pid);
                if (restA.isSuccess()) {
                    ACRGBWSResult restB = this.GETROLEMULITPLE(dataSource, restA.getResult());
                    List<String> fchlist = Arrays.asList(restB.getResult().split(","));
                    ArrayList<HealthCareFacility> healthcarefacilitylist = new ArrayList<>();
                    if (fchlist.size() > 0) {
                        for (int y = 0; y < fchlist.size(); y++) {
                            ACRGBWSResult getfacility = fm.GETFACILITYID(dataSource, fchlist.get(y));
                            if (getfacility.isSuccess()) {
                                HealthCareFacility facility = utility.ObjectMapper().readValue(getfacility.getResult(), HealthCareFacility.class);
                                healthcarefacilitylist.add(facility);
                            }
                        }
                    } else {
                        result.setMessage("N/A");
                    }
                    if (healthcarefacilitylist.size() > 0) {
                        result.setResult(utility.ObjectMapper().writeValueAsString(healthcarefacilitylist));
                        result.setMessage("OK");
                        result.setSuccess(true);
                    } else {
                        result.setMessage("N/A");
                    }
                } else {
                    result.setMessage(restA.getMessage());
                }
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET MB WITH ID
    public ACRGBWSResult GETMBWITHID(final DataSource dataSource, final String pid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            //  System.out.println(resultset.getString("MBNAME"));
            if (!utility.IsValidNumber(pid)) {
                result.setMessage("INVALID NUMBER FORMAT");
            } else {
                //---------------------------------------------------- 
                CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETMBWITHID(:pid); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("pid", pid);
                statement.execute();
                ResultSet resultset = (ResultSet) statement.getObject("v_result");
                if (resultset.next()) {
                    //--------------------------------------------------------
                    ManagingBoard mb = new ManagingBoard();
                    mb.setMbid(resultset.getString("MBID"));
                    mb.setMbname(resultset.getString("MBNAME"));
                    mb.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                    ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                    if (creator.isSuccess()) {
                        if (!creator.getResult().isEmpty()) {
                            UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                            mb.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                        } else {
                            mb.setCreatedby(creator.getMessage());
                        }
                    } else {
                        mb.setCreatedby("DATA NOT FOUND");
                    }
                    mb.setStatus(resultset.getString("STATUS"));
                    mb.setControlnumber(resultset.getString("CONNUMBER"));
                    result.setMessage("OK");
                    result.setSuccess(true);
                    result.setResult(utility.ObjectMapper().writeValueAsString(mb));
                    //------------------------------------------------------
                } else {
                    result.setMessage("NO DATA FOUND");
                }
                //----------------------------------------------------------
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // REMOVED ACCESS LEVEL USING ROLE INDEX
    public ACRGBWSResult REMOVEDROLEINDEX(final DataSource datasource, final String userid, final String accessid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidNumber(userid)) {
                result.setMessage("NUMBER FORMAT IS NOT VALID");
            } else {
                ArrayList<String> errorList = new ArrayList<>();
                List<String> accesslist = Arrays.asList(accessid.split(","));
                int errCount = 0;
                for (int x = 0; x < accesslist.size(); x++) {
                    //------------------------------------------------------------------------------------------------
                    CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.REMOVEDROLEINDEX(:Message,:Code,"
                            + ":puserid,:paccessid)");
                    getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                    getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                    getinsertresult.setString("puserid", userid);
                    getinsertresult.setString("paccessid", accesslist.get(x));
                    getinsertresult.execute();
                    //------------------------------------------------------------------------------------------------
                    if (!getinsertresult.getString("Message").equals("SUCC")) {
                        errCount++;
                        errorList.add(getinsertresult.getString("Message"));
                    }
                }
                if (errCount == 0) {
                    result.setSuccess(true);
                    result.setMessage("OK");
                } else {
                    result.setMessage(errorList.toString());
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET MB WITH ID
    public ACRGBWSResult GETALLMBWITHPROID(final DataSource dataSource, final String proid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            if (!utility.IsValidNumber(proid)) {
                result.setMessage("INVALID NUMBER FORMAT");
            } else {
                ACRGBWSResult restA = this.GETROLE(dataSource, proid);
                ArrayList<ManagingBoard> mblist = new ArrayList<>();
                if (restA.isSuccess()) {
                    ACRGBWSResult restB = this.GETROLEMULITPLE(dataSource, restA.getResult());
                    List<String> fchlist = Arrays.asList(restB.getResult().split(","));
                    for (int x = 0; x < fchlist.size(); x++) {
                        //---------------------------------------------------- 
                        CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETMBWITHID(:pid); end;");
                        statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                        statement.setString("pid", fchlist.get(x));
                        statement.execute();
                        ResultSet resultset = (ResultSet) statement.getObject("v_result");
                        while (resultset.next()) {
                            //--------------------------------------------------------
                            ManagingBoard mb = new ManagingBoard();
                            mb.setMbid(resultset.getString("MBID"));
                            mb.setMbname(resultset.getString("MBNAME"));
                            mb.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                            ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                            if (creator.isSuccess()) {
                                if (!creator.getResult().isEmpty()) {
                                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                                    mb.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                                } else {
                                    mb.setCreatedby(creator.getMessage());
                                }
                            } else {
                                mb.setCreatedby("DATA NOT FOUND");
                            }
                            mb.setStatus(resultset.getString("STATUS"));
                            mb.setControlnumber(resultset.getString("CONNUMBER"));
                            mb.setBankaccount(resultset.getString("BANKACCOUNT"));
                            mb.setBankname(resultset.getString("BANKNAME"));
                            mb.setAddress(resultset.getString("ADDRESS"));
                            //-----------------------------------------------------
                            ACRGBWSResult reastC = this.GETROLEMULITPLE(dataSource, resultset.getString("CONNUMBER"));
                            List<String> hcfcodeList = Arrays.asList(reastC.getResult().split(","));
                            Double totaldiff = 0.00;
                            for (int f = 0; f < hcfcodeList.size(); f++) {
                                //FacilityComputedAmount
                                ACRGBWSResult getBadgetResult = this.GetAmountPerFacility(dataSource, hcfcodeList.get(f));//GET TOTAL CLAIMS AMOUNT FOR GOOD TAGS
                                if (getBadgetResult.isSuccess()) {
                                    if (!getBadgetResult.getResult().isEmpty()) {
                                        FacilityComputedAmount getBadgetFirst = utility.ObjectMapper().readValue(getBadgetResult.getResult(), FacilityComputedAmount.class);
                                        ACRGBWSResult getBadgetFirstSecond = this.GetAmountPerFacilitySkipYear(dataSource, getBadgetFirst.getHospital());//GET TOTAL BADGET FROM SKIP YEAR
                                        if (getBadgetFirstSecond.isSuccess()) {
                                            FacilityComputedAmount combadget = utility.ObjectMapper().readValue(getBadgetFirstSecond.getResult(), FacilityComputedAmount.class);
                                            Double skipamount = Double.parseDouble(combadget.getTotalamount());
                                            Double totalamount = Double.parseDouble(getBadgetFirst.getTotalamount());
//                                            String diff = String.valueOf(totalamount - skipamount);
                                            totaldiff += totalamount - skipamount;
                                        } else {
                                            totaldiff += Double.parseDouble(getBadgetFirst.getTotalamount());
                                        }
                                    } else {
                                        totaldiff += 0.00;
                                    }
                                } else {
                                    totaldiff += 0.00;
                                }
                            }
                            mb.setBaseamount(String.valueOf(totaldiff));
                            //GET ACCREDITATION USING CODE
                            ACRGBWSResult accreResult = fm.GETACCREDITATION(dataSource, resultset.getString("CONNUMBER"));
                            if (accreResult.isSuccess()) {
                                Accreditation accree = utility.ObjectMapper().readValue(accreResult.getResult(), Accreditation.class);
                                mb.setLicensedatefrom(accree.getDatefrom());
                                mb.setLicensedateto(accree.getDateto());
                            } else {
                                mb.setLicensedatefrom(accreResult.getMessage());
                                mb.setLicensedateto(accreResult.getMessage());
                            }
                            mblist.add(mb);
                            //------------------------------------------------------
                        }
                    }
                }
                if (mblist.size() > 0) {
                    result.setMessage("OK");
                    result.setSuccess(true);
                    result.setResult(utility.ObjectMapper().writeValueAsString(mblist));
                } else {
                    result.setMessage("NO DATA FOUND");
                }
            }

        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET MB USING PROCODE
    public ACRGBWSResult GETALLMBWITHPROCODE(final DataSource dataSource, final String proid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            if (!utility.IsValidNumber(proid)) {
                result.setMessage("INVALID NUMBER FORMAT");
            } else {
                ArrayList<ManagingBoard> mblist = new ArrayList<>();
                ACRGBWSResult restB = this.GETROLEMULITPLE(dataSource, proid);
                List<String> fchlist = Arrays.asList(restB.getResult().split(","));
                for (int x = 0; x < fchlist.size(); x++) {
                    //---------------------------------------------------- 
                    CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETMBWITHID(:pid); end;");
                    statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                    statement.setString("pid", fchlist.get(x));
                    statement.execute();
                    ResultSet resultset = (ResultSet) statement.getObject("v_result");
                    while (resultset.next()) {
                        //--------------------------------------------------------
                        ManagingBoard mb = new ManagingBoard();
                        mb.setMbid(resultset.getString("MBID"));
                        mb.setMbname(resultset.getString("MBNAME"));
                        mb.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                        ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                        if (creator.isSuccess()) {
                            if (!creator.getResult().isEmpty()) {
                                UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                                mb.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                            } else {
                                mb.setCreatedby(creator.getMessage());
                            }
                        } else {
                            mb.setCreatedby("DATA NOT FOUND");
                        }
                        mb.setStatus(resultset.getString("STATUS"));
                        mb.setControlnumber(resultset.getString("CONNUMBER"));
                        mb.setBankaccount(resultset.getString("BANKACCOUNT"));
                        mb.setBankname(resultset.getString("BANKNAME"));
                        mb.setAddress(resultset.getString("ADDRESS"));
                        //-----------------------------------------------------
                        ACRGBWSResult reastC = this.GETROLEMULITPLE(dataSource, resultset.getString("CONNUMBER"));
                        List<String> hcfcodeList = Arrays.asList(reastC.getResult().split(","));
                        Double totaldiff = 0.00;
                        for (int f = 0; f < hcfcodeList.size(); f++) {
                            //FacilityComputedAmount
                            ACRGBWSResult getBadgetResult = this.GetAmountPerFacility(dataSource, hcfcodeList.get(f));//GET TOTAL CLAIMS AMOUNT FOR GOOD TAGS
                            if (getBadgetResult.isSuccess()) {
                                if (!getBadgetResult.getResult().isEmpty()) {
                                    FacilityComputedAmount getBadgetFirst = utility.ObjectMapper().readValue(getBadgetResult.getResult(), FacilityComputedAmount.class);
                                    ACRGBWSResult getBadgetFirstSecond = this.GetAmountPerFacilitySkipYear(dataSource, getBadgetFirst.getHospital());//GET TOTAL BADGET FROM SKIP YEAR
                                    if (getBadgetFirstSecond.isSuccess()) {
                                        FacilityComputedAmount combadget = utility.ObjectMapper().readValue(getBadgetFirstSecond.getResult(), FacilityComputedAmount.class);
                                        Double skipamount = Double.parseDouble(combadget.getTotalamount());
                                        Double totalamount = Double.parseDouble(getBadgetFirst.getTotalamount());
//                                            String diff = String.valueOf(totalamount - skipamount);
                                        totaldiff += totalamount - skipamount;
                                    } else {
                                        totaldiff += Double.parseDouble(getBadgetFirst.getTotalamount());
                                    }

                                } else {
                                    totaldiff += 0.00;
                                }
                            } else {
                                totaldiff += 0.00;
                            }
                        }
                        mb.setBaseamount(String.valueOf(totaldiff));
                        ACRGBWSResult accreResult = fm.GETACCREDITATION(dataSource, resultset.getString("CONNUMBER"));
                        if (accreResult.isSuccess()) {
                            Accreditation accree = utility.ObjectMapper().readValue(accreResult.getResult(), Accreditation.class);
                            mb.setLicensedatefrom(accree.getDatefrom());
                            mb.setLicensedateto(accree.getDateto());
                        } else {
                            mb.setLicensedatefrom(accreResult.getMessage());
                            mb.setLicensedateto(accreResult.getMessage());
                        }
                        mblist.add(mb);
                        //------------------------------------------------------
                    }
                }
                if (mblist.size() > 0) {
                    result.setMessage("OK");
                    result.setSuccess(true);
                    result.setResult(utility.ObjectMapper().writeValueAsString(mblist));
                } else {
                    result.setMessage("NO DATA FOUND");
                }
            }

        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET FACILITY WITH MBID
    public ACRGBWSResult GETALLFACILITYWITHMBID(final DataSource dataSource, final String proid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            //  System.out.println(resultset.getString("MBNAME"));
            if (!utility.IsValidNumber(proid)) {
                result.setMessage("INVALID NUMBER FORMAT");
            } else {
                ACRGBWSResult restA = this.GETROLEMULITPLE(dataSource, proid);
                List<String> resultlist = Arrays.asList(restA.getResult().split(","));
                ArrayList<HealthCareFacility> fchlist = new ArrayList<>();
                for (int x = 0; x < resultlist.size(); x++) {
                    //---------------------------------------------------- 
                    CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETFACILITY(:pid); end;");
                    statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                    statement.setString("pid", resultlist.get(x));
                    statement.execute();
                    ResultSet resultset = (ResultSet) statement.getObject("v_result");
                    while (resultset.next()) {
                        //--------------------------------------------------------

                        HealthCareFacility hcf = new HealthCareFacility();
                        hcf.setHcfid(resultset.getString("HCFID"));
                        hcf.setHcfname(resultset.getString("HCFNAME"));
                        hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                        hcf.setHcfcode(resultset.getString("HCFCODE"));
                        ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                        if (creator.isSuccess()) {
                            if (!creator.getResult().isEmpty()) {
                                UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                                hcf.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                            } else {
                                hcf.setCreatedby(creator.getMessage());
                            }
                        } else {
                            hcf.setCreatedby("DATA NOT FOUND");
                        }
                        hcf.setType(resultset.getString("HCFTYPE"));
                        hcf.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                        hcf.setProid(resultset.getString("PROID"));
                        fchlist.add(hcf);
                        //------------------------------------------------------
                    }
                }
                if (fchlist.size() > 0) {
                    result.setMessage("OK");
                    result.setSuccess(true);
                    result.setResult(utility.ObjectMapper().writeValueAsString(fchlist));
                } else {
                    result.setMessage("NO DATA FOUND");
                }

                //----------------------------------------------------------
            }
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GetProWithPROID(final DataSource dataSource, final String pproid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            String procode = pproid.substring(pproid.length() - 2);
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETPROWITHID(:pproid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pproid", procode);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Pro pro = new Pro();
                pro.setProname(resultset.getString("PRONAME"));
                pro.setProcode(resultset.getString("PROCODE"));
                result.setResult(utility.ObjectMapper().writeValueAsString(pro));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETROLE(final DataSource dataSource, final String puserid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLEWITHID(:pid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pid", puserid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setResult(resultset.getString("ACCESSID"));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETROLEMULITPLE(final DataSource dataSource, final String puserid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLEWITHID(:pid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pid", puserid);
            statement.execute();
            ArrayList<String> listresult = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                listresult.add(resultset.getString("ACCESSID"));
            }

            if (listresult.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(String.join(",", listresult));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETROLEREVERESE(final DataSource dataSource, final String puserid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLEWITHIDREVERSE(:pid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pid", puserid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(resultset.getString("USERID"));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETROLEREVERESEMULTIPLE(final DataSource dataSource, final String puserid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLEWITHIDREVERSE(:pid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pid", puserid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<String> resultlist = new ArrayList<>();
            while (resultset.next()) {
                resultlist.add(resultset.getString("USERID"));
            }
            if (resultlist.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(String.join(",", resultlist));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET APEX FACILITY
    public ACRGBWSResult GETAPEXFACILITY(final DataSource dataSource) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETAPEXFACILITY(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ArrayList<HealthCareFacility> hcflist = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                HealthCareFacility hcf = new HealthCareFacility();
                hcf.setHcfid(resultset.getString("HCFID"));
                hcf.setHcfname(resultset.getString("HCFNAME"));
                hcf.setHcfaddress(resultset.getString("HCFADDRESS"));
                hcf.setHcfcode(resultset.getString("HCFCODE"));
                ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                        hcf.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        hcf.setCreatedby(creator.getMessage());
                    }
                } else {
                    hcf.setCreatedby("N/A");
                }
                hcf.setType(resultset.getString("HCFTYPE"));
                hcf.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                hcflist.add(hcf);
            }
            if (hcflist.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(hcflist));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET REPORTS FOR LIST OF SELECTED NETWORK
    public ACRGBWSResult GetReportsOfSelectedHCPN(final DataSource dataSource, final String tags, final String puserid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<ReportsHCPNSummary> rmblist = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            ACRGBWSResult resultreports = fm.ACR_CONTRACTPROID(dataSource, tags, puserid);//GET CONTRACT USING USERID OF PRO USER ACCOUNT
            if (resultreports.isSuccess()) {
                if (!resultreports.getResult().isEmpty()) {
                    List<Contract> conlist = Arrays.asList(utility.ObjectMapper().readValue(resultreports.getResult(), Contract[].class));
                    for (int x = 0; x < conlist.size(); x++) {
                        ReportsHCPNSummary rmb = new ReportsHCPNSummary();
                        rmb.setHcpnname(conlist.get(x).getHcfid());
                        if (!conlist.get(x).getContractdate().isEmpty()) {
                            ContractDate condate = utility.ObjectMapper().readValue(conlist.get(x).getContractdate(), ContractDate.class);
                            rmb.setContractadateto(condate.getDateto());
                            rmb.setContractadatefrom(condate.getDatefrom());
                        } else {
                            rmb.setContractadateto(conlist.get(x).getContractdate());
                            rmb.setContractadatefrom(conlist.get(x).getContractdate());
                        }
                        rmb.setConctractamount(conlist.get(x).getAmount());
                        rmb.setContractnumber(conlist.get(x).getTranscode());
                        CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETTOTALRELEASEUNDERMB(:tags,:pconid); end;");
                        statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                        statement.setString("tags", tags);
                        statement.setString("pconid", conlist.get(x).getConid());
                        statement.execute();
                        ResultSet resultset = (ResultSet) statement.getObject("v_result");
                        if (resultset.next()) {
                            rmb.setAmountrelease(resultset.getString("CAMOUNT"));
                            rmb.setTotalnumberofreleased(resultset.getString("TCONID"));
                            Double totalclaimsamount = Double.parseDouble(resultset.getString("CAMOUNT"));
                            Double contractamount = Double.parseDouble(conlist.get(x).getAmount());
                            Double bal = contractamount - totalclaimsamount;
                            rmb.setRemainingbal(String.valueOf(bal));
                        } else {
                            rmb.setAmountrelease("NO AMOUNT");
                            rmb.setTotalnumberofreleased("NO RELEASED");
                            rmb.setRemainingbal(conlist.get(x).getAmount());
                        }
                        rmblist.add(rmb);
                    }
                    result.setMessage("OK");
                } else {
                    result.setMessage("N/A");
                }
            } else {
                result.setMessage(resultreports.getMessage());
            }

            if (rmblist.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(rmblist));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (IOException | SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET REPORTS FOR LIST OF SELECTED NETWORK
    public ACRGBWSResult GetReportsOfSelectedAPEXFacility(final DataSource dataSource, final String tags, final String puserid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<ReportsHCPNSummary> rmblist = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            ACRGBWSResult resultreports = fm.ACR_CONTRACTPROID(dataSource, tags, puserid);//GET CONTRACT USING USERID OF PRO USER ACCOUNT
            if (resultreports.isSuccess()) {
                if (!resultreports.getResult().isEmpty()) {
                    List<Contract> conlist = Arrays.asList(utility.ObjectMapper().readValue(resultreports.getResult(), Contract[].class));
                    for (int x = 0; x < conlist.size(); x++) {
                        ManagingBoard mb = utility.ObjectMapper().readValue(conlist.get(x).getHcfid(), ManagingBoard.class);
                        ReportsHCPNSummary rmb = new ReportsHCPNSummary();
                        rmb.setHcpnname(mb.getMbname());
                        if (!conlist.get(x).getContractdate().isEmpty()) {
                            ContractDate condate = utility.ObjectMapper().readValue(conlist.get(x).getContractdate(), ContractDate.class);
                            rmb.setContractadateto(condate.getDateto());
                            rmb.setContractadatefrom(condate.getDatefrom());
                        } else {
                            rmb.setContractadateto(conlist.get(x).getContractdate());
                            rmb.setContractadatefrom(conlist.get(x).getContractdate());
                        }
                        rmb.setConctractamount(conlist.get(x).getAmount());
                        rmb.setContractnumber(conlist.get(x).getTranscode());
                        CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETTOTALRELEASEUNDERMB(:tags,:pconid); end;");
                        statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                        statement.setString("tags", tags);
                        statement.setString("pconid", conlist.get(x).getConid());
                        statement.execute();
                        ResultSet resultset = (ResultSet) statement.getObject("v_result");
                        if (resultset.next()) {
                            rmb.setAmountrelease(resultset.getString("CAMOUNT"));
                            rmb.setTotalnumberofreleased(resultset.getString("TCONID"));
                            Double totalclaimsamount = Double.parseDouble(resultset.getString("CAMOUNT"));
                            Double contractamount = Double.parseDouble(conlist.get(x).getAmount());
                            Double bal = contractamount - totalclaimsamount;
                            rmb.setRemainingbal(String.valueOf(bal));
                        } else {
                            rmb.setAmountrelease("NO AMOUNT");
                            rmb.setTotalnumberofreleased("NO RELEASED");
                            rmb.setRemainingbal(conlist.get(x).getAmount());
                        }
                        rmblist.add(rmb);
                    }
                } else {
                    result.setMessage("N/A");
                }
            } else {
                result.setMessage(resultreports.getMessage());
            }

            if (rmblist.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(rmblist));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (IOException | SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET REPORTS EVERY FACILITY USING MB ID
    public ACRGBWSResult GetReportsOfSelectedFacilityUnderHCPN(final DataSource dataSource, final String tags, final String puserid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<ReportsHCPNSummary> rmblist = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            ACRGBWSResult resultreports = fm.ACR_CONTRACTPROID(dataSource, tags, puserid);//GET CONTRACT USING USERID OF PRO USER ACCOUNT
            if (resultreports.isSuccess()) {
                if (!resultreports.getResult().isEmpty()) {
                    List<Contract> conlist = Arrays.asList(utility.ObjectMapper().readValue(resultreports.getResult(), Contract[].class));
                    for (int x = 0; x < conlist.size(); x++) {
                        ManagingBoard mb = utility.ObjectMapper().readValue(conlist.get(x).getHcfid(), ManagingBoard.class);
                        ReportsHCPNSummary rmb = new ReportsHCPNSummary();
                        rmb.setHcpnname(mb.getMbname());

                        if (!conlist.get(x).getContractdate().isEmpty()) {
                            ContractDate condate = utility.ObjectMapper().readValue(conlist.get(x).getContractdate(), ContractDate.class);
                            rmb.setContractadateto(condate.getDateto());
                            rmb.setContractadatefrom(condate.getDatefrom());
                        } else {
                            rmb.setContractadateto(conlist.get(x).getContractdate());
                            rmb.setContractadatefrom(conlist.get(x).getContractdate());
                        }
                        rmb.setConctractamount(conlist.get(x).getAmount());
                        rmb.setContractnumber(conlist.get(x).getTranscode());
                        CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETTOTALRELEASEUNDERMB(:tags,:pconid); end;");
                        statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                        statement.setString("tags", tags);
                        statement.setString("pconid", conlist.get(x).getConid());
                        statement.execute();
                        ResultSet resultset = (ResultSet) statement.getObject("v_result");
                        if (resultset.next()) {
                            rmb.setAmountrelease(resultset.getString("CAMOUNT"));
                            rmb.setTotalnumberofreleased(resultset.getString("TCONID"));
                            Double totalclaimsamount = Double.parseDouble(resultset.getString("CAMOUNT"));
                            Double contractamount = Double.parseDouble(conlist.get(x).getAmount());
                            Double bal = contractamount - totalclaimsamount;
                            rmb.setRemainingbal(String.valueOf(bal));
                        } else {
                            rmb.setAmountrelease("NO AMOUNT");
                            rmb.setTotalnumberofreleased("NO RELEASED");
                            rmb.setRemainingbal(conlist.get(x).getAmount());
                        }
                        rmblist.add(rmb);
                    }
//
                } else {
                    result.setMessage("N/A");
                }
            } else {
                result.setMessage(resultreports.getMessage());
            }

            if (rmblist.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(rmblist));
                result.setSuccess(true);
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (IOException | SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET COMPUTED REMAINING BALANCE FOR TERMINATED CONTRACT PER FACILITY
    public ACRGBWSResult GetRemainingBalanceForTerminatedContract(final DataSource dataSource, final String userid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Contract> contractlist = new ArrayList<>();
        try {
            //GET FACILITY UNDER PRO LEVEL USING USERID ACCOUNT
            ACRGBWSResult restA = this.GETROLE(dataSource, userid);//GET PRO ID USING USER ID
            if (restA.isSuccess()) {
                ACRGBWSResult restB = this.GETROLEMULITPLE(dataSource, restA.getResult());//GET MB UNDER PRO USING PRO ID
                if (restB.isSuccess()) {
                    List<String> restBList = Arrays.asList(restB.getResult().split(","));
                    for (int x = 0; x < restBList.size(); x++) {
                        ACRGBWSResult restC = this.GETROLEMULITPLE(dataSource, restBList.get(x));//GET MB UNDER PRO USING PRO ID
                        if (restC.isSuccess()) {
                            List<String> restCList = Arrays.asList(restC.getResult().split(","));
                            for (int y = 0; y < restCList.size(); y++) {
                                ACRGBWSResult conResult = fm.GetTerminateContract(dataSource, restCList.get(y));
                                if (conResult.isSuccess()) {
                                    Contract restD = utility.ObjectMapper().readValue(conResult.getResult(), Contract.class);
                                    contractlist.add(restD);
                                }
                            }
                            if (contractlist.isEmpty()) {
                                result.setMessage("N/A ");
                            } else {
                                result.setMessage("OK");
                                result.setSuccess(true);
                                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
                            }
                        } else {
                            result.setMessage("N/A ");
                        }
                    }
                } else {
                    result.setMessage("N/A ");
                }
            } else {
                result.setMessage("N/A ");
            }

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET TERMINATED CONTRACT OF APEX FACILITY
    public ACRGBWSResult GetRemainingBalanceForTerminatedContractApex(final DataSource dataSource) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Contract> contractlist = new ArrayList<>();
        try {
            //GET FACILITY UNDER PRO LEVEL USING USERID ACCOUNT
            ACRGBWSResult apexResult = this.GETAPEXFACILITY(dataSource);
            if (apexResult.isSuccess()) {
                List<HealthCareFacility> hcfList = Arrays.asList(utility.ObjectMapper().readValue(apexResult.getResult(), HealthCareFacility[].class));
                for (int v = 0; v < hcfList.size(); v++) {
                    ACRGBWSResult conResult = fm.GetTerminateContract(dataSource, hcfList.get(v).getHcfcode());
                    if (conResult.isSuccess()) {
                        Contract restA = utility.ObjectMapper().readValue(conResult.getResult(), Contract.class);
                        contractlist.add(restA);
                    }
                }
            }
            if (contractlist.isEmpty()) {
                result.setMessage("N/A ");
            } else {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
            }

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET END CONTRACT OF APEX FACILITY
    public ACRGBWSResult GetRemainingBalanceForEndContractApex(final DataSource dataSource) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Contract> contractlist = new ArrayList<>();
        try {
            //GET FACILITY UNDER PRO LEVEL USING USERID ACCOUNT
            ACRGBWSResult apexResult = this.GETAPEXFACILITY(dataSource);
            if (apexResult.isSuccess()) {
                List<HealthCareFacility> hcfList = Arrays.asList(utility.ObjectMapper().readValue(apexResult.getResult(), HealthCareFacility[].class));
                for (int v = 0; v < hcfList.size(); v++) {
                    ACRGBWSResult conResult = fm.GetEndContract(dataSource, hcfList.get(v).getHcfcode());
                    if (conResult.isSuccess()) {
                        Contract restA = utility.ObjectMapper().readValue(conResult.getResult(), Contract.class);
                        contractlist.add(restA);
                    }
                }
            }
            if (contractlist.isEmpty()) {
                result.setMessage("N/A");
            } else {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
            }

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GetAmount(final DataSource dataSource, final String pan, final String datestart, final String dateend) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        String utags = "GOOD";
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETSUMAMOUNTCLAIMS(:ulevel,:uaccreno,:utags,:udatefrom,:udateto); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ulevel", "TWO");
            statement.setString("uaccreno", pan);
            statement.setString("utags", utags);
            statement.setDate("udatefrom", (Date) new Date(utility.StringToDate(datestart).getTime()));
            statement.setDate("udateto", (Date) new Date(utility.StringToDate(dateend).getTime()));
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                FacilityComputedAmount fca = new FacilityComputedAmount();
                fca.setHospital(resultset.getString("PMCC_NO"));
                fca.setTotalamount(resultset.getString("CTOTAL"));
                fca.setYearfrom(datestart);
                fca.setYearto(dateend);
                fca.setTotalclaims(resultset.getString("COUNTVAL"));
                result.setResult(utility.ObjectMapper().writeValueAsString(fca));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET COMPUTED REMAINING BALANCE FOR TERMINATED CONTRACT PER FACILITY
    public ACRGBWSResult GetRemainingBalanceForEndContract(final DataSource dataSource, final String userid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Contract> contractlist = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            //GET FACILITY UNDER PRO LEVEL USING USERID ACCOUNT
            ACRGBWSResult restA = this.GETROLE(dataSource, userid);//GET PRO ID USING USER ID
            if (restA.isSuccess()) {
                ACRGBWSResult restB = this.GETROLEMULITPLE(dataSource, restA.getResult());//GET MB UNDER PRO USING PRO ID
                if (restB.isSuccess()) {
                    List<String> restBList = Arrays.asList(restB.getResult().split(","));
                    for (int x = 0; x < restBList.size(); x++) {
                        ACRGBWSResult restC = this.GETROLEMULITPLE(dataSource, restBList.get(x));//GET MB UNDER PRO USING PRO ID
                        if (restC.isSuccess()) {
                            List<String> restCList = Arrays.asList(restC.getResult().split(","));
                            for (int y = 0; y < restCList.size(); y++) {
                                ACRGBWSResult conResult = fm.GetEndContract(dataSource, restCList.get(y));
                                if (conResult.isSuccess()) {
                                    Contract restD = utility.ObjectMapper().readValue(conResult.getResult(), Contract.class);
                                    contractlist.add(restD);
                                }
                            }
                            if (contractlist.isEmpty()) {
                                result.setMessage("N/A");
                            } else {
                                result.setMessage("OK");
                                result.setSuccess(true);
                                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
                            }
                        } else {
                            result.setMessage("N/A");
                        }
                    }
                } else {
                    result.setMessage("N/A");
                }
            } else {
                result.setMessage("N/A");
            }

        } catch (IOException | SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
