/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Accreditation;
import acrgb.structure.Appellate;
import acrgb.structure.Assets;
import acrgb.structure.Book;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.Email;
import acrgb.structure.LogStatus;
import acrgb.structure.ManagingBoard;
import acrgb.structure.Pro;
import acrgb.structure.Tranch;
import acrgb.structure.User;
import acrgb.structure.UserActivity;
import acrgb.structure.UserInfo;
import acrgb.structure.UserLevel;
import acrgb.structure.UserRoleIndex;
import acrgb.utility.Cryptor;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
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
public class InsertMethods {

    public InsertMethods() {
    }
    private final Utility utility = new Utility();
    private final Cryptor cryptor = new Cryptor();
    private final FetchMethods fm = new FetchMethods();

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTASSETS(final DataSource datasource, Assets assets) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UpdateMethods updatemethods = new UpdateMethods();
        Methods methods = new Methods();
        UserActivityLogs logs = new UserActivityLogs();
        String logsTags = "";
        try (Connection connection = datasource.getConnection()) {
            UserActivity userlogs = utility.UserActivity();
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTASSETS(:Message,:Code,:p_hcfid,:p_tranchid ,:p_receipt,:p_amount"
                    + ",:p_createdby,:p_datereleased,:p_datecreated,:p_conid,:p_releasedamount,:p_previousbal)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_hcfid", assets.getHcfid());
            getinsertresult.setString("p_tranchid", assets.getTranchid());
            getinsertresult.setString("p_receipt", assets.getReceipt());
            getinsertresult.setString("p_amount", assets.getAmount());
            getinsertresult.setString("p_createdby", assets.getCreatedby());
            getinsertresult.setDate("p_datereleased", (Date) new Date(utility.StringToDate(assets.getDatereleased()).getTime())); //assets.getDatereleased());
            getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(assets.getDatecreated()).getTime()));//assets.getDatecreated());
            getinsertresult.setString("p_conid", assets.getConid());
            getinsertresult.setString("p_releasedamount", assets.getReleasedamount());
            getinsertresult.setString("p_previousbal", assets.getPreviousbalance());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                //INSERT TO ACTIVITY LOGS
                userlogs.setActstatus("SUCCESS");
                ACRGBWSResult getSubject = fm.GETFACILITYID(datasource, assets.getHcfid());
                if (getSubject.isSuccess()) {
                    logsTags = "ADD-TRANCHE-HCI";
                    result.setMessage(getinsertresult.getString("Message"));
                } else {
                    ACRGBWSResult getSubjectA = methods.GETMBWITHID(datasource, assets.getHcfid());
                    if (getSubjectA.isSuccess()) {
                        logsTags = "ADD-TRANCHE-HCPN";
                    }
                    result.setMessage(getinsertresult.getString("Message"));
                }
                updatemethods.UPDATECONBALANCESTATS(datasource, assets.getHcfid());
                result.setSuccess(true);
            } else {
                userlogs.setActstatus("FAILED");
                result.setMessage(getinsertresult.getString("Message"));
            }
            userlogs.setActdetails(getinsertresult.getString("Message"));
            userlogs.setActby(assets.getCreatedby());
            //ACTIVITY LOGS
            logs.UserLogsMethod(datasource, logsTags, userlogs, assets.getHcfid().trim(), assets.getTranchid().trim());
            //END ACTIVITY LOGS
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTCONTRACT(final DataSource datasource, final Contract contract) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        UpdateMethods um = new UpdateMethods();
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            String logsTags = "";
            UserActivity userlogs = utility.UserActivity();
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTCONTRACT(:Message,:Code,:p_hcfid,:p_amount"
                    + ",:p_createdby,:p_datecreated,:p_contractdate,:p_transcode,"
                    + ":p_baseamount,:c_claimsvol,:t_claimsvol,:p_sb,:p_addamount,:p_quarter)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_hcfid", contract.getHcfid());//PAN Number or MB Accreditaion Number
            getinsertresult.setString("p_amount", contract.getAmount());
            getinsertresult.setString("p_createdby", contract.getCreatedby());
            getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(contract.getDatecreated()).getTime()));//contract.getDatecreated());
            getinsertresult.setString("p_contractdate", contract.getContractdate());
            getinsertresult.setString("p_transcode", contract.getTranscode());
            getinsertresult.setString("p_baseamount", contract.getBaseamount());
            getinsertresult.setString("c_claimsvol", contract.getComittedClaimsVol());
            getinsertresult.setString("t_claimsvol", contract.getComputedClaimsVol());
            getinsertresult.setString("p_sb", contract.getSb());
            getinsertresult.setString("p_addamount", contract.getAddamount());
            getinsertresult.setString("p_quarter", contract.getQuarter());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                //INSERT TO ACTIVITY LOGS
                int countpro = 0;
                ACRGBWSResult getSubject = fm.GETFACILITYID(datasource, contract.getHcfid());
                if (getSubject.isSuccess()) {
                    logsTags = "ADD-CONTRACT-HCI";
                } else {
                    ACRGBWSResult getSubjectA = methods.GETMBWITHID(datasource, contract.getHcfid());
                    if (getSubjectA.isSuccess()) {
                        logsTags = "ADD-CONTRACT-HCPN";
                    } else {
                        logsTags = "ADD-CONTRACT-PRO";
                        countpro++;
                    }
                }
                if (countpro == 0) {
                    ACRGBWSResult uaccessid = methods.GETROLE(datasource, contract.getCreatedby(), "ACTIVE");
                    //INSERT CONTRACT ID TO ROLE INDEX TABLE
                    um.UPDATEROLEINDEX(datasource, uaccessid.getResult().trim(), contract.getHcfid(), contract.getContractdate(), "HCIUPDATE");
                    //END INSERT CONTRACT ID TO ROLE INDEX TABLE
                    //INSERT CONTRACT ID TO APPELLATE TABLE
                    Appellate appellate = new Appellate();
                    appellate.setAccesscode(contract.getHcfid());
                    appellate.setStatus("2");
                    appellate.setConid(contract.getContractdate());
                    um.UPDATEAPELLATE(datasource, "NONUPDATE", appellate);
                    //END INSERT CONTRACT ID TO APPELLATE TABLE
                }
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(true);
                userlogs.setActstatus("SUCCESS");
            } else {
                userlogs.setActstatus("FAILED");
                result.setMessage(getinsertresult.getString("Message"));
            }
            userlogs.setActby(contract.getCreatedby());
            userlogs.setActdetails("Amount :" + contract.getAmount() + "| SB :" + contract.getSb() + "| Comitted volume:" + contract.getComittedClaimsVol() + " " + contract.getQuarter());
            logs.UserLogsMethod(datasource, logsTags, userlogs, contract.getHcfid(), contract.getContractdate());

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTTRANCH(final DataSource datasource, Tranch tranch) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            UserActivity userlogs = utility.UserActivity();
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTTRANCH(:Message,:Code,:p_tranchtype,:p_percentage,:p_createdby,:p_datecreated)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_tranchtype", tranch.getTranchtype().toUpperCase());
            getinsertresult.setString("p_percentage", tranch.getPercentage());
            getinsertresult.setString("p_createdby", tranch.getCreatedby());
            getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(tranch.getDatecreated()).getTime()));//tranch.getDatecreated());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
                userlogs.setActstatus("SUCCESS");
            } else {
                userlogs.setActstatus("FAILED");
                result.setMessage(getinsertresult.getString("Message"));
            }
            // USER LOGS
            userlogs.setActby(tranch.getCreatedby());
            userlogs.setActdetails(" Tranche type :"
                    + tranch.getTranchtype() + " " + getinsertresult.getString("Message"));
            logs.UserLogsMethod(datasource, "ADD-TRANCHE", userlogs, "0", "0");
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTUSERDETAILS(final DataSource datasource, UserInfo userinfo) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            UserActivity userlogs = utility.UserActivity();
            ACRGBWSResult validateUsername = methods.ACRUSERNAME(datasource, userinfo.getEmail());
            if (validateUsername.isSuccess()) {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTUSERDETAILS(:Message,:Code,"
                        + ":p_firstname,:p_lastname,:p_middlename,:p_datecreated,:p_createdby,:p_email,:p_contact)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("p_firstname", userinfo.getFirstname().toUpperCase());
                getinsertresult.setString("p_lastname", userinfo.getLastname().toUpperCase());
                getinsertresult.setString("p_middlename", userinfo.getMiddlename().toUpperCase());
                getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(userinfo.getDatecreated()).getTime()));//userinfo.getDatecreated());
                getinsertresult.setString("p_createdby", userinfo.getCreatedby());
                getinsertresult.setString("p_email", userinfo.getEmail());
                getinsertresult.setString("p_contact", userinfo.getContact());
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    userlogs.setActstatus("SUCCESS");
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(true);
                } else {
                    userlogs.setActstatus("FAILED");
                    result.setMessage(getinsertresult.getString("Message"));
                }
                userlogs.setActstatus("SUCCESS");
            } else {
                userlogs.setActstatus("FAILED");
                result.setMessage(validateUsername.getMessage());
                result.setResult(validateUsername.getResult());
            }
            //USER LOGS
            userlogs.setActdetails("contact :" + userinfo.getContact() + " Email :" + userinfo.getEmail()
                    + " LastName :" + userinfo.getLastname() + " FirstName:" + userinfo.getFirstname() + " " + result.getMessage());
            userlogs.setActby(userinfo.getCreatedby());
            logs.UserLogsMethod(datasource, "ADD-USERINFO", userlogs, "0", "0");
            //USER LOGS
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTUSERLEVEL(final DataSource datasource, UserLevel userlevel) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            UserActivity userlogs = utility.UserActivity();
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTUSERLEVEL(:Message,:Code,"
                    + ":p_levdetails,:p_levname,:p_createdby,:p_datecreated)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_levdetails", userlevel.getLevdetails().toUpperCase());
            getinsertresult.setString("p_levname", userlevel.getLevname().toUpperCase());
            getinsertresult.setString("p_createdby", userlevel.getCreatedby());
            getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(userlevel.getDatecreated()).getTime())); //userlevel.getDatecreated());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
                userlogs.setActstatus("SUCCESS");
            } else {
                result.setMessage(getinsertresult.getString("Message"));
                userlogs.setActstatus("FAILED");
            }
            //USER LOGS
            userlogs.setActby(userlevel.getCreatedby());
            userlogs.setActdetails(" Level : " + userlevel.getLevname() + " " + getinsertresult.getString("Message"));
            logs.UserLogsMethod(datasource, "ADD-USER-LEVEL", userlogs, "0", "0");
            //END USER LOGS
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//---------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTUSER(final DataSource datasource, final User user) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        EmailSender emailsender = new EmailSender();
        Methods methods = new Methods();
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            UserActivity userlogs = utility.UserActivity();
            if (!fm.GETUSERDETAILSBYDID(datasource, user.getDid()).isSuccess()) {
                result.setMessage("USER INFO ID NOT FOUND");
            } else {
                // ACRGBWSResult validateUsername = methods.ACRUSERNAME(datasource, user.getUsername());
                if (methods.ACRUSERNAME(datasource, user.getUsername()).isSuccess()) {
                    if (fm.GETUSERLEVEL(datasource, user.getLeveid()).isSuccess()) {
                        String encryptpword = cryptor.encrypt(user.getUserpassword(), user.getUserpassword(), "ACRGB");
                        CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTUSER(:Message,:Code,:p_levelid,:p_username,:p_userpassword,:p_datecreated,:p_createdby,:p_stats,:p_did)");
                        getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                        getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                        getinsertresult.setString("p_levelid", user.getLeveid());
                        getinsertresult.setString("p_username", user.getUsername());
                        getinsertresult.setString("p_userpassword", encryptpword);
                        getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(user.getDatecreated()).getTime())); //userlevel.getDatecreated());//user.getDatecreated());
                        getinsertresult.setString("p_createdby", user.getCreatedby());
                        getinsertresult.setString("p_stats", "2");
                        getinsertresult.setString("p_did", user.getDid());
                        getinsertresult.execute();
                        if (getinsertresult.getString("Message").equals("SUCC")) {
                            userlogs.setActstatus("SUCCESS");
                            result.setSuccess(true);
                            //SEND PASSCODE TO EMAIL IF SUCCESS
                            Email email = new Email();
                            email.setRecipient(user.getUsername());
                            emailsender.EmailSender(datasource, email, user.getUserpassword().trim());
                            result.setMessage(getinsertresult.getString("Message"));
                        } else {
                            userlogs.setActstatus("FAILED");
                            result.setMessage(getinsertresult.getString("Message"));
                        }
                    } else {
                        userlogs.setActstatus("FAILED");
                        result.setMessage(fm.GETUSERLEVEL(datasource, user.getLeveid()).getMessage());
                    }
                } else {
                    userlogs.setActstatus("FAILED");
                    result.setMessage(methods.ACRUSERNAME(datasource, user.getUsername()).getMessage());
                }
            }
            //USER LOGS
            userlogs.setActby(user.getCreatedby());
            userlogs.setActdetails(result.getMessage());
            logs.UserLogsMethod(datasource, "ADD-USERACCOUNT", userlogs, user.getLeveid(), user.getDid());
            //USER LOGS
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSEROLEINDEX(final DataSource datasource, UserRoleIndex userroleindex) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            UserActivity userLogs = utility.UserActivity();
            ArrayList<String> errorList = new ArrayList<>();
            List<String> accesslist = Arrays.asList(userroleindex.getAccessid().split(","));
            for (int x = 0; x < accesslist.size(); x++) {
                //------------------------------------------------------------------------------------------------
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.USEROLEINDEX(:Message,:Code,"
                        + ":a_userid,:a_accessid,:a_createdby,:a_datecreated)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("a_userid", userroleindex.getUserid().trim());
                getinsertresult.setString("a_accessid", accesslist.get(x).trim());
                getinsertresult.setString("a_createdby", userroleindex.getCreatedby());
                getinsertresult.setDate("a_datecreated", (Date) new Date(utility.StringToDate(userroleindex.getDatecreated()).getTime()));
                getinsertresult.execute();
                //------------------------------------------------------------------------------------------------
                if (!getinsertresult.getString("Message").equals("SUCC")) {
                    userLogs.setActstatus("FAILED");
                    errorList.add(getinsertresult.getString("Message"));
                } else {
                    userLogs.setActstatus("SUCCESS");
                }
                userLogs.setActdetails(getinsertresult.getString("Message"));
                userLogs.setActby(userroleindex.getCreatedby());
                logs.UserLogsMethod(datasource, "REMOVED-ACCESS", userLogs, userroleindex.getUserid(), accesslist.get(x));
            }
            if (errorList.size() > 0) {
                result.setMessage(errorList.toString());
            } else {
                result.setSuccess(true);
                result.setMessage("OK");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult REMOVEDACCESSLEVEL(final DataSource datasource, UserRoleIndex userroleindex) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            UserActivity userLogs = utility.UserActivity();
            ArrayList<String> errorList = new ArrayList<>();
            List<String> accesslist = Arrays.asList(userroleindex.getAccessid().split(","));
            for (int x = 0; x < accesslist.size(); x++) {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.REMOVEDACCESSLEVEL(:Message,:Code,"
                        + ":a_userid,:a_accessid)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("a_userid", userroleindex.getUserid());
                getinsertresult.setString("a_accessid", accesslist.get(x));
                getinsertresult.execute();
                if (!getinsertresult.getString("Message").equals("SUCC")) {
                    errorList.add(getinsertresult.getString("Message"));
                    userLogs.setActstatus("SUCCESS");
                } else {
                    userLogs.setActstatus("SUCCESS");
                }
                userLogs.setActdetails(getinsertresult.getString("Message"));
                userLogs.setActby(userroleindex.getCreatedby());
                logs.UserLogsMethod(datasource, "ADD-ACCESS", userLogs, userroleindex.getUserid(), accesslist.get(x));
            }
            if (errorList.size() > 0) {
                result.setSuccess(true);
                result.setMessage("OK");
            } else {
                result.setMessage(errorList.toString());
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INACTIVEDATA(final DataSource datasource, final String tags,
            final String dataid,
            final String createdby) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            UserActivity userLogs = utility.UserActivity();
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INACTIVEDATA(:Message,:Code,"
                    + ":tags,:dataid)");
            if (tags.toUpperCase().trim().equals("USER")) {
                if (fm.GETUSERBYUSERID(datasource, dataid).isSuccess()) {
                    User user = utility.ObjectMapper().readValue(fm.GETUSERBYUSERID(datasource, dataid).getResult(), User.class);
                    getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                    getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                    getinsertresult.setString("p_tags", "USERDETAILS".trim().toUpperCase());
                    getinsertresult.setInt("p_dataid", Integer.parseInt(user.getDid()));
                    getinsertresult.execute();
                    if (getinsertresult.getString("Message").equals("SUCC")) {
                        userLogs.setActstatus("SUCCESS");
                    } else {
                        userLogs.setActstatus("FAILED");
                        result.setMessage(getinsertresult.getString("Message"));
                    }
                }
            } else {
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("tags", tags.trim().toUpperCase());
                getinsertresult.setInt("dataid", Integer.parseInt(dataid));
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    userLogs.setActstatus("SUCCESS");
                    result.setSuccess(true);
                    result.setMessage(getinsertresult.getString("Message"));
                } else {
                    userLogs.setActstatus("FAILED");
                    result.setMessage(getinsertresult.getString("Message"));
                }
            }
            userLogs.setActby(createdby);
            userLogs.setActdetails(tags);
            logs.UserLogsMethod(datasource, "INACTIVE-DATA", userLogs, dataid, "0");

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult ACTIVEDATA(final DataSource datasource,
            final String tags,
            final String dataid,
            final String createdby) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            UserActivity userLogs = utility.UserActivity();
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.ACTIVEDATA(:Message,:Code,"
                    + ":tags,:dataid)");
            if (tags.toUpperCase().trim().equals("USER")) {
                if (fm.GETUSERBYUSERID(datasource, dataid).isSuccess()) {
                    User user = utility.ObjectMapper().readValue(fm.GETUSERBYUSERID(datasource, dataid).getResult(), User.class);
                    getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                    getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                    getinsertresult.setString("p_tags", "USERDETAILS".trim().toUpperCase());
                    getinsertresult.setInt("p_dataid", Integer.parseInt(user.getDid()));
                    getinsertresult.execute();
                    if (getinsertresult.getString("Message").equals("SUCC")) {
                    } else {
                        result.setMessage(getinsertresult.getString("Message"));
                    }
                }
            }
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("tags", tags.trim().toUpperCase());
            getinsertresult.setInt("dataid", Integer.parseInt(dataid));
            getinsertresult.execute();
            UserActivity userlogs = utility.UserActivity();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                userlogs.setActstatus("SUCCESS");
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                userlogs.setActstatus("FAILED");
                result.setMessage(getinsertresult.getString("Message"));
            }
            //USER LOGS
            userLogs.setActby(createdby);
            userLogs.setActdetails(tags);
            logs.UserLogsMethod(datasource, "ACTIVE-DATA", userLogs, dataid, "0");

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTHCPN(final DataSource datasource, final ManagingBoard mb) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            UserActivity userlogs = utility.UserActivity();
            if (fm.GETMBCONTROL(datasource, mb.getControlnumber()).isSuccess()) {
                result.setMessage("DUPLICATEREGISTRATIONNUMBER");
            } else {
                if (methods.GETROLE(datasource, mb.getCreatedby(), "ACTIVE").isSuccess()) {
                    if (!methods.GetProWithPROID(datasource, methods.GETROLE(datasource, mb.getCreatedby(), "ACTIVE").getResult()).isSuccess()) {
                        result.setMessage(methods.GetProWithPROID(datasource, methods.GETROLE(datasource, mb.getCreatedby(), "ACTIVE").getResult()).getMessage());
                    } else {
                        Pro pro = utility.ObjectMapper().readValue(methods.GetProWithPROID(datasource, methods.GETROLE(datasource, mb.getCreatedby(), "ACTIVE").getResult()).getResult(), Pro.class);
                        UserRoleIndex indexrole = new UserRoleIndex();
                        indexrole.setUserid(pro.getProcode());
                        indexrole.setAccessid(mb.getControlnumber());
                        indexrole.setCreatedby(mb.getCreatedby());
                        indexrole.setDatecreated(mb.getDatecreated());
                        ACRGBWSResult insertRole = this.INSEROLEINDEX(datasource, indexrole);
                        if (insertRole.isSuccess()) {
                            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTHCPN(:Message,:Code,"
                                    + ":umbname,:udatecreated,:ucreatedby,:uaccreno,:uaddress,:ubankaccount,:ubankname)");
                            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                            getinsertresult.setString("umbname", mb.getMbname().toUpperCase());
                            getinsertresult.setDate("udatecreated", (Date) new Date(utility.StringToDate(mb.getDatecreated()).getTime()));
                            getinsertresult.setString("ucreatedby", mb.getCreatedby());
                            getinsertresult.setString("uaccreno", mb.getControlnumber());
                            getinsertresult.setString("uaddress", mb.getAddress());
                            getinsertresult.setString("ubankaccount", mb.getBankaccount());
                            getinsertresult.setString("ubankname", mb.getBankname());
                            getinsertresult.execute();
                            if (getinsertresult.getString("Message").equals("SUCC")) {
                                //MAPPING OF ACCREDITATION DATA
                                Accreditation acree = new Accreditation();
                                acree.setAccreno(mb.getControlnumber());
                                acree.setCreatedby(mb.getCreatedby());
                                acree.setDatecreated(mb.getDatecreated());
                                acree.setDatefrom(mb.getLicensedatefrom());
                                acree.setDateto(mb.getLicensedateto());
                                ACRGBWSResult accreResult = this.INSERTACCREDITAION(datasource, acree);
                                //MAPPING LOGSTATUS VALUE
                                LogStatus logstats = new LogStatus();
                                logstats.setAccount(mb.getControlnumber());
                                logstats.setActby(mb.getCreatedby());
                                logstats.setDatefrom(mb.getLicensedatefrom());
                                logstats.setDateto(mb.getLicensedateto());
                                logstats.setRemarks("New Registration");
                                logstats.setStatus("2");
                                ACRGBWSResult logsResult = this.INSERTSTATSLOG(datasource, logstats);
                                if (logsResult.isSuccess() && accreResult.isSuccess()) {
                                    result.setMessage(logsResult.getMessage() + " , " + accreResult.getMessage());
                                    result.setSuccess(true);
                                    userlogs.setActstatus("SUCCESS");
                                } else {
                                    userlogs.setActstatus("FAILED");
                                    result.setMessage(logsResult.getMessage() + " , " + accreResult.getMessage());
                                }
                            } else {
                                userlogs.setActstatus("FAILED");
                                result.setMessage(getinsertresult.getString("Message"));
                            }
                        } else {
                            userlogs.setActstatus("FAILED");
                            result.setMessage(insertRole.getMessage());
                        }
                    }
                } else {
                    userlogs.setActstatus("FAILED");
                    result.setMessage(methods.GETROLE(datasource, mb.getCreatedby(), "ACTIVE").getMessage());
                }
            }
            //USER LOGS
            userlogs.setActby(mb.getCreatedby());
            userlogs.setActdetails("Name :" + mb.getMbname().toUpperCase() + " Address :" + mb.getAddress() + " Control Number :" + mb.getControlnumber() + " Bank :" + mb.getBankname() + " Account :" + mb.getBankaccount() + " " + result.getMessage());
            logs.UserLogsMethod(datasource, "ADD-HCPN", userlogs, "0", "0");
            //USER LOGS
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //INSERT ACCREDITATION  
    public ACRGBWSResult INSERTACCREDITAION(final DataSource datasource, final Accreditation accre) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            UserActivity userlogs = utility.UserActivity();
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTACCREDITAION(:Message,:Code,"
                    + ":uaccreno,:udatefrom,:udateto,:udatecreated,:ucreatedby)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("uaccreno", accre.getAccreno());
            getinsertresult.setDate("udatefrom", (Date) new Date(utility.StringToDate(accre.getDatefrom()).getTime()));
            getinsertresult.setDate("udateto", (Date) new Date(utility.StringToDate(accre.getDateto()).getTime()));
            getinsertresult.setDate("udatecreated", (Date) new Date(utility.StringToDate(accre.getDatecreated()).getTime()));
            getinsertresult.setString("ucreatedby", accre.getCreatedby());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(true);
                userlogs.setActstatus("SUCCESS");
            } else {
                userlogs.setActstatus("FAILED");
                result.setMessage(getinsertresult.getString("Message"));
            }
            //==============ACTIVLITY LOGS AREA ===========================
            userlogs.setActby(accre.getCreatedby());
            userlogs.setActdetails("Accreditation period" + accre.getDatefrom() + " - " + accre.getDateto() + " " + result.getMessage());
            logs.UserLogsMethod(datasource, "ADD-ACCREDITATION-HCPN", userlogs, accre.getAccreno(), "0");
            //==============ACTIVLITY LOGS AREA ===========================
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //INSERT LOGS STATUS
    public ACRGBWSResult INSERTSTATSLOG(final DataSource datasource, final LogStatus logsS) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            UserActivity userlogs = utility.UserActivity();
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTSTATSLOG(:Message,:Code,"
                    + ":uaccount,:ustatus,:udatechange,:uactby,:uremarks,:udatefrom,:udateto)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("uaccount", logsS.getAccount());
            getinsertresult.setString("ustatus", logsS.getStatus());
            getinsertresult.setTimestamp("udatechange", new java.sql.Timestamp(new java.util.Date().getTime()));
            getinsertresult.setString("uactby", logsS.getActby());
            getinsertresult.setString("uremarks", logsS.getRemarks());
            getinsertresult.setDate("udatefrom", (Date) new Date(utility.StringToDate(logsS.getDatefrom()).getTime()));
            getinsertresult.setDate("udateto", (Date) new Date(utility.StringToDate(logsS.getDateto()).getTime()));
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                userlogs.setActstatus("SUCCESS");
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(true);
            } else {
                userlogs.setActstatus("FAILED");
                result.setMessage(getinsertresult.getString("Message"));
            }
            // USER LOGS
            userlogs.setActby(logsS.getActby());
            // userlogs.setActdetails("Accreditation period" + accre.getDatefrom() + " - " + accre.getDateto() + " " + result.getMessage());
            logs.UserLogsMethod(datasource, "ADD-STATSLOG", userlogs, "0", "0");
            //USER LOGS
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult INSERTAPPELLATE(final DataSource datasource,
            final String userid, //SINGLE
            final String accessid, //MULTIPLE
            final String createdby,
            final String datecreated) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            UserActivity userlogs = utility.UserActivity();
            ArrayList<String> errorCode = new ArrayList<>();
            ArrayList<String> errorList = new ArrayList<>();
            List<String> accesslist = Arrays.asList(accessid.split(","));
            for (int x = 0; x < accesslist.size(); x++) {
                //------------------------------------------------------------------------------------------------
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTAPPELLATE(:Message,:Code,"
                        + ":userid,:accessid)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("userid", userid);
                getinsertresult.setString("accessid", accesslist.get(x));
                getinsertresult.execute();
                //------------------------------------------------------------------------------------------------
                if (!getinsertresult.getString("Message").equals("SUCC")) {
                    errorList.add(getinsertresult.getString("Message"));
                    errorCode.add(accesslist.get(x));
                    userlogs.setActstatus("FAILED");
                } else {
                    userlogs.setActstatus("SUCCESS");
                }
                userlogs.setActdetails(getinsertresult.getString("Message"));
                userlogs.setActby(createdby); // 1,2,2,APEX,HCPN
                logs.UserLogsMethod(datasource, "ADD-APPELIATE", userlogs, userid, accesslist.get(x).trim());
            }
            if (errorCode.size() > 0) {
                result.setMessage(errorList.toString());
            } else {
                result.setSuccess(true);
                result.setMessage("OK");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //INSERT BOOK
    public ACRGBWSResult ACRBOOKING(final DataSource datasource, final Book book) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            UserActivity userlogs = utility.UserActivity();
            //------------------------------------------------------------------------------------------------
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.ACRBOOKING(:Message,:Code,"
                    + ":ubooknum,:uconid,:udatecreated,:ucreatedby)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("ubooknum", book.getBooknum());
            getinsertresult.setString("uconid", book.getConid());
            getinsertresult.setString("udatecreated", book.getDatecreated());
            getinsertresult.setString("ucreatedby", book.getCreatedby());
            getinsertresult.execute();
            //------------------------------------------------------------------------------------------------
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                userlogs.setActstatus("SUCCESS");
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                userlogs.setActstatus("FAILED");
                result.setMessage(getinsertresult.getString("Message"));
            }
            userlogs.setActby(book.getCreatedby()); // 1,2,2,APEX,HCPN
            userlogs.setActdetails(book.getBooknum());
            logs.UserLogsMethod(datasource, "INSERT-BOOK-REF", userlogs, book.getConid(), book.getHcpncode());

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //INSERT BOOK DATA
//    public ACRGBWSResult ACRBOOKINGDATA(final DataSource datasource,
//            final NclaimsData nclaims, final String booknum, final String datecreated, final String createdby)  {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        //Methods methods = new Methods();
//        UserActivityLogs logs = new UserActivityLogs();
//        try (Connection connection = datasource.getConnection()) {
//            UserActivity userlogs = utility.UserActivity();
//            //String logsTags = "INSERT-CLAIMS-BOOK-DATA";
//            //------------------------------------------------------------------------------------------------
//            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.ACRBOOKINGDATA(:Message,:Code,"
//                    + ":useries,:uaccreno,:upmccno,:udateadmission,:udatesubmitted,:uclaimamount,:ubooknum,"
//                    + ":utags,:utrn,:uclaimid,:uhcfname,:c1rvcode,:c2rvcode,:c1icdcode,:c2icdcode)");
//            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
//            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
//            getinsertresult.setString("useries", nclaims.getSeries());
//            getinsertresult.setString("uaccreno", nclaims.getAccreno());
//            if (nclaims.getPmccno().equals("N/A")) {
//                getinsertresult.setString("upmccno", nclaims.getPmccno());
//            } else {
//                HealthCareFacility hci = utility.ObjectMapper().readValue(nclaims.getPmccno(), HealthCareFacility.class);
//                getinsertresult.setString("upmccno", hci.getHcfcode());
//            }
//            if (nclaims.getDateadmission() == null) {
//                getinsertresult.setString("udateadmission", nclaims.getDateadmission().trim());
//            } else {
//                getinsertresult.setDate("udateadmission", (Date) new Date(utility.StringToDate(nclaims.getDateadmission().trim()).getTime()));
//            }
//            if (nclaims.getDatesubmitted() == null) {
//                getinsertresult.setString("udatesubmitted", nclaims.getDatesubmitted());
//            } else {
//                getinsertresult.setDate("udatesubmitted", (Date) new Date(utility.StringToDate(nclaims.getDatesubmitted()).getTime()));
//            }
//            getinsertresult.setString("uclaimamount", nclaims.getClaimamount());
//            getinsertresult.setString("ubooknum", booknum);
//            getinsertresult.setString("utags", nclaims.getTags());
//            getinsertresult.setString("utrn", nclaims.getTrn());
//            getinsertresult.setString("uclaimid", nclaims.getClaimid());
//            getinsertresult.setString("uhcfname", nclaims.getHcfname());
//            getinsertresult.setString("c1rvcode", nclaims.getC1rvcode());
//            getinsertresult.setString("c2rvcode", nclaims.getC2rvcode());
//            getinsertresult.setString("c1icdcode", nclaims.getC1icdcode());
//            getinsertresult.setString("c2icdcode", nclaims.getC2icdcode());
//            getinsertresult.execute();
//            //------------------------------------------------------------------------------------------------
//            if (getinsertresult.getString("Message").equals("SUCC")) {
//                //FOR REVIEW THIS LINE   
//                result.setSuccess(true);
//                result.setMessage(getinsertresult.getString("Message"));
//                userlogs.setActstatus("SUCCESS");
//            } else {
//                userlogs.setActstatus("FAILED");
//                result.setMessage(getinsertresult.getString("Message"));
//            }
//
//            userlogs.setActdetails(" book " + nclaims.getSeries() + " | " + getinsertresult.getString("Message"));
//            userlogs.setActby(createdby); // 1,2,2,APEX,HCPN
//            logs.UserLogsMethod(datasource, "INSERT-CLAIMS-BOOK-DATA", userlogs, nclaims.getPmccno(), "0");
//        } catch (SQLException | IOException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    //INSERT LOGS STATUS
    public ACRGBWSResult INSERTCONDATE(final DataSource datasource, final ContractDate contractdate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            UserActivity userLogs = utility.UserActivity();
            if (!methods.VALIDATECONTRACTDATE(datasource, contractdate.getDatefrom(), contractdate.getDateto()).isSuccess()) {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTCONDATE(:Message,:Code,"
                        + ":udatefrom,:udateto,:ucreatedby,:udatecreated)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setDate("udatefrom", (Date) new Date(utility.StringToDate(contractdate.getDatefrom()).getTime()));
                getinsertresult.setDate("udateto", (Date) new Date(utility.StringToDate(contractdate.getDateto()).getTime()));
                getinsertresult.setString("ucreatedby", contractdate.getCreatedby());
                getinsertresult.setDate("udatecreated", (Date) new Date(utility.StringToDate(contractdate.getDatecreated()).getTime()));
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setSuccess(true);
                    userLogs.setActstatus("SUCCESS");
                    result.setMessage(getinsertresult.getString("Message"));
                } else {
                    userLogs.setActstatus("FAILED");
                    result.setMessage(getinsertresult.getString("Message"));
                }
            } else {
                result.setMessage("DateFrom : " + contractdate.getDatefrom() + " DateTo" + contractdate.getDateto() + " is already exist :");
                userLogs.setActstatus("FAILED");
            }

            //==============ACTIVLITY LOGS AREA ===========================
            userLogs.setActby(contractdate.getCreatedby());
            userLogs.setActdetails(contractdate.getDatefrom() + " - " + contractdate.getDateto() + "|" + result.getMessage());
            logs.UserLogsMethod(datasource, "ADD-CONTRACT-DATE", userLogs, "0", "0");
            //==============ACTIVLITY LOGS AREA ===========================
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //INSERT USER ACCOUNT BATCH UPLOAD
    public ACRGBWSResult INSERTUSERACCOUNTBATCHUPLOAD(final DataSource datasource, final UserInfo userinfo) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTUSERDETAILS(:Message,:Code,"
                    + ":p_firstname,:p_lastname,:p_middlename,:p_datecreated,:p_createdby,:p_email,:p_contact)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_firstname", userinfo.getFirstname().toUpperCase());
            getinsertresult.setString("p_lastname", userinfo.getLastname().toUpperCase());
            getinsertresult.setString("p_middlename", userinfo.getMiddlename().toUpperCase());
            getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(userinfo.getDatecreated()).getTime()));//userinfo.getDatecreated());
            getinsertresult.setString("p_createdby", userinfo.getCreatedby());
            getinsertresult.setString("p_email", userinfo.getEmail());
            getinsertresult.setString("p_contact", userinfo.getContact());
            getinsertresult.execute();
            UserActivity infologs = utility.UserActivity();
            UserActivity userlogs = utility.UserActivity();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                infologs.setActstatus("SUCCESS");
                //CREATE ACCOUNT DIRECT
                ACRGBWSResult getUserInfoUsingEmail = fm.GETUSERINFOUSINGEMAIL(datasource, userinfo.getEmail());
                if (getUserInfoUsingEmail.isSuccess()) {
                    UserInfo userinfoResult = utility.ObjectMapper().readValue(getUserInfoUsingEmail.getResult(), UserInfo.class);
                    //MAP USER ACCOUNT
                    User user = new User();
                    user.setCreatedby(userinfo.getCreatedby());
                    user.setDid(userinfoResult.getDid());
                    user.setUsername(userinfo.getEmail());
                    //GENERATE PASS
                    GenerateRandomPassword createpass = new GenerateRandomPassword();
                    user.setUserpassword(createpass.GenerateRandomPassword(10));
                    //END GENERATE PASS
                    user.setDatecreated(userinfo.getDatecreated());
                    user.setLeveid(userinfo.getRole());
                    //-----------------------------
                    ACRGBWSResult insertNewAccount = this.INSERTUSER(datasource, user);
                    if (insertNewAccount.isSuccess()) {
                        //INSERT USER ROLE
                        ACRGBWSResult getUserUsingEmail = fm.GETACCOUNTUSINGEMAIL(datasource, userinfo.getEmail());
                        if (getUserUsingEmail.isSuccess()) {
                            User userResult = utility.ObjectMapper().readValue(getUserUsingEmail.getResult(), User.class);
                            UserRoleIndex userrole = new UserRoleIndex();
                            userrole.setUserid(userResult.getUserid());
                            userrole.setAccessid(userinfo.getDesignation());
                            userrole.setCreatedby(userinfo.getCreatedby());
                            userrole.setDatecreated(userinfo.getDatecreated());
                            //INSERT TO SELECTED DESIGNATION
                            this.INSEROLEINDEX(datasource, userrole);

                        }
                        userlogs.setActstatus("SUCCESS");
                    } else {
                        userlogs.setActstatus("FAILED");
                    }
                    userlogs.setActby(userinfo.getCreatedby());
                    userlogs.setActdetails(" Username :" + userinfo.getEmail());
                    logs.UserLogsMethod(datasource, "ADD-USERACCOUNT-BATCH", userlogs, userinfo.getRole(), "0");
                }

                result.setMessage(getinsertresult.getString("Message") + ""
                        + getUserInfoUsingEmail.getMessage());
                result.setSuccess(true);
            } else {
                infologs.setActstatus("FAILED");
                result.setMessage(getinsertresult.getString("Message"));
            }
            infologs.setActby(userinfo.getCreatedby());
            infologs.setActdetails(" Contact :" + userinfo.getContact() + " Email :" + userinfo.getEmail() + " Lastname :" + userinfo.getLastname() + " FirstName :" + userinfo.getFirstname() + " " + getinsertresult.getString("Message"));
            logs.UserLogsMethod(datasource, "ADD-USERINFO-BATCH", infologs, userinfo.getDesignation(), "0");
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
