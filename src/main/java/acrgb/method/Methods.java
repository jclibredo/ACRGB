/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.NclaimsData;
import acrgb.structure.Summary;
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
    private final Cryptor cryptor = new Cryptor();
    private final SimpleDateFormat datetimeformat = utility.SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");

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
                for (int x = 0; x < userlist.size(); x++) {
                    String dpassword = cryptor.decrypt(userlist.get(x).getUserpassword(), p_password, "ACRGB");
                    if (dpassword != null) {
                        if (userlist.get(x).getUsername().equals(p_username) && dpassword.equals(p_password)) {
                            User user = new User();
                            user.setUserid(userlist.get(x).getUserid());
                            user.setLeveid(userlist.get(x).getLeveid());
                            user.setUsername(userlist.get(x).getUsername());
                            user.setUserpassword(userlist.get(x).getUserpassword());
                            user.setDatecreated(userlist.get(x).getDatecreated());
                            user.setStatus(userlist.get(x).getStatus());
                            user.setDid(userlist.get(x).getDid());
                            user.setCreatedby(userlist.get(x).getCreatedby());
                            result.setSuccess(true);
                            result.setResult(utility.ObjectMapper().writeValueAsString(user));
                            result.setMessage("OK");
                        } else {
                            result.setSuccess(false);
                            result.setMessage("CREDENTIAL NOT FOUND");
                            result.setResult("Username:" + p_username + " Password:" + p_password);
                        }
                    } else {
                        result.setSuccess(false);
                        result.setResult("Username:" + p_username + " Password:" + p_password);
                        result.setMessage("CREDENTIALS NOT FOUND");
                    }
                }
            } else {
                result.setSuccess(false);
                result.setMessage("NO AVAILABLE DATA");
                result.setResult("Username:" + p_username + " Password:" + p_password);
            }
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

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
                result.setSuccess(false);
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
            } else {
                result.setMessage("USER ROLE IS NOT AVAILABLE");
            }
            result.setSuccess(true);
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
                    + ":p_firstname,:p_lastname,:p_middlename,:p_did,:p_hcfid)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_firstname", userinfo.getFirstname().toUpperCase());
            getinsertresult.setString("p_lastname", userinfo.getLastname().toUpperCase());
            getinsertresult.setString("p_middlename", userinfo.getMiddlename().toUpperCase());
            getinsertresult.setString("p_did", userinfo.getDid());
            getinsertresult.setString("p_hcfid", userinfo.getHcfid());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage("OK");
            } else {
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(false);
            }
            result.setResult(utility.ObjectMapper().writeValueAsString(userinfo));
        } catch (SQLException | IOException ex) {
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
            ACRGBWSResult usernameResult = this.ACRUSERNAME(dataSource, p_username);
            if (usernameResult.isSuccess()) {
                if (!utility.validatePassword(p_password)) {
                    result.setSuccess(false);
                    result.setMessage("PASSWORD IS NOT VALID");
                } else {
                    CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.UPDATEUSERCREDENTIALS(:Message,:Code,:userid,:p_username,:p_password,:p_stats)");
                    getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                    getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                    getinsertresult.setString("userid", userid);
                    getinsertresult.setString("p_username", p_username);
                    getinsertresult.setString("p_password", cryptor.encrypt(p_password, p_password, "ACRGB"));
                    getinsertresult.setString("p_stats", "2");
                    getinsertresult.execute();
                    if (getinsertresult.getString("Message").equals("SUCC")) {
                        result.setSuccess(true);
                        result.setMessage("OK");
                    } else {
                        result.setMessage(getinsertresult.getString("Message"));
                        result.setSuccess(false);
                    }
                }
            } else {
                result.setMessage(usernameResult.getMessage());
                result.setSuccess(usernameResult.isSuccess());
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //--------------------------------------------------------
    //ACR GB GET SUMMARY
    public ACRGBWSResult JOINCONHCFTBL(final DataSource dataSource) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.JOINCONHCFTBL(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ArrayList<Summary> summarylist = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                //SimpleDateFormat sf = utility.SimpleDateFormat("MM-DD-YYYY");
                Summary summary = new Summary();
                String u_accreno = resultset.getString("HCFCODE");
                Date u_date = resultset.getDate("DATECOVERED");
                String u_tags = "GOOD";
                Double assetsamount = Double.parseDouble(resultset.getString("CAMOUNT"));
                ACRGBWSResult sumresult = fm.GETNCLAIMS(dataSource, u_accreno, u_tags, u_date);
                if (sumresult.isSuccess()) {
                    NclaimsData nclaimsdata = utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData.class);
                    Double totalclaimsamount = Double.parseDouble(nclaimsdata.getClaimamount());
                    Double sums = totalclaimsamount / assetsamount * 100;
                    summary.setAccreno(u_accreno);
                    summary.setHcfid(resultset.getString("HCFID"));
                    if (sums > 100) {
                        Double negvalue = 100 - sums;
                        summary.setTotalpercentage(String.valueOf(Math.round(negvalue)));
                    } else {
                        summary.setTotalpercentage(String.valueOf(Math.round(sums)));
                    }
                    summary.setTranchid(resultset.getString("TRANCHID"));
                    summary.setRemarks("YES");
                    summarylist.add(summary);
                } else {
                    summary.setAccreno(u_accreno);
                    summary.setHcfid(resultset.getString("HCFID"));
                    summary.setTotalpercentage("");
                    summary.setTranchid(resultset.getString("TRANCHID"));
                    summary.setRemarks("NO");
                    summarylist.add(summary);
                }
            }
            if (summarylist.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(summarylist));
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
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
                result.setMessage("OK");
            } else {
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(false);
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
        java.util.Date d1 = new java.util.Date();
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
            } else {
                result.setMessage("NO DATA FOUND");
            }
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//
//    // ACR GB USER IMAGE
//    public ACRGBWSResult UPDATEUSERIMAGE(final DataSource dataSource, final String userid, final String p_image) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try (Connection connection = dataSource.getConnection()) {
//            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBINSERTPKG.UPDATEUSERIMAGE(:Message,:Code,:userid,:p_image)");
//            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
//            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
//            getinsertresult.setString("userid", userid);
//            getinsertresult.setString("p_image", p_image);
//            getinsertresult.execute();
//            if (getinsertresult.getString("Message").equals("SUCC")) {
//                result.setSuccess(true);
//                result.setMessage("SUCCESSFULLY INSERTED");
//            } else {
//                result.setMessage(getinsertresult.getString("Message"));
//                result.setSuccess(false);
//            }
//        } catch (SQLException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
//
//    // ACR GB ACCOUNTS PAYABLE
//    public ACRGBWSResult JOINPAYABLE(final DataSource dataSource, final String p_username) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try (Connection connection = dataSource.getConnection()) {
//            CallableStatement statement = connection.prepareCall("begin :joinresult := ACR_GB.ACRGBPKG.JOINPAYABLE(); end;");
//            statement.registerOutParameter("joinresult", OracleTypes.CURSOR);
//            statement.execute();
//            ResultSet resultset = (ResultSet) statement.getObject("joinresult");
//            ArrayList<AccountPayable> accountpayablelist = new ArrayList<>();
//            while (resultset.next()) {
//                AccountPayable accountpayable = new AccountPayable();
//                accountpayable.setPayableid(resultset.getString("PAYABLEID"));
//                accountpayable.setClaimseries(resultset.getString("CLAIMSERIES"));
//                accountpayable.setDatereleased(resultset.getString("DATERELEASED"));
//                accountpayable.setAmount(resultset.getString("AAMOUNT"));
//                accountpayable.setCreatedby(resultset.getString("CREATEDBY"));
//                accountpayable.setModeofpayment(resultset.getString("MODEOFPAYMENT"));
//                accountpayable.setPaidto(resultset.getString("PAIDTO"));
//                accountpayable.setAssetsid(resultset.getString("AASSETSID"));
//                accountpayable.setClaimid(resultset.getString("CLAIMID"));
//                accountpayablelist.add(accountpayable);
////------------------------------------------------------------------------------------------------------
//                Assets assests = new Assets();
//                assests.setAssetsid(resultset.getString("BASSETSID"));
//                assests.setAmount(resultset.getString("BAMOUNT"));
//                assests.setTransnumber(resultset.getString("TRANSNUMBER"));
//                assests.setTypeofassets(resultset.getString("TYPEOFASSETS"));
//                assests.setSourceoffunds(resultset.getString("SOURCEOFFUNDS"));
//                assests.setDaterecieved(resultset.getString("DATERECIEVED"));
//                assests.setEncodedby(resultset.getString("ENCODEDBY"));
//                assests.setAccreditation(resultset.getString("ACCREDITATION"));
//                assests.setHfid(resultset.getString("HFID"));
//                assests.setTransid(resultset.getString("BID"));
////------------------------------------------------------------------------------------------------------
//                Tranch trans = new Tranch();
//                trans.setTransid(resultset.getString("CID"));
//                trans.setDatereleased(resultset.getString("DATERELEASED"));
//                trans.setTranstype(resultset.getString("TRANSTYPEID"));
//                trans.setPercent(resultset.getString("PERCENT"));
//                trans.setHfid(resultset.getString("CHFID"));
//            }
//            
//            if (!accountpayablelist.isEmpty()) {
//                result.setSuccess(true);
//                result.setResult(utility.ObjectMapper().writeValueAsString(accountpayablelist));
//            } else {
//                result.setSuccess(false);
//                result.setMessage("NO DATA AVAILABLE");
//            }
//            
//        } catch (SQLException | IOException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(Methods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
//    
}
