/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Accreditation;
import acrgb.structure.Assets;
import acrgb.structure.Book;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.DateSettings;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.LogStatus;
import acrgb.structure.ManagingBoard;
import acrgb.structure.NclaimsData;
import acrgb.structure.PaymentType;
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
import java.text.ParseException;
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
        try (Connection connection = datasource.getConnection()) {
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
                UserActivity userlogs = utility.UserActivity();
                ACRGBWSResult getSubject = fm.GETFACILITYID(datasource, assets.getHcfid());
                if (getSubject.isSuccess()) {
                    HealthCareFacility hcf = utility.ObjectMapper().readValue(getSubject.getResult(), HealthCareFacility.class);
                    String actdetails = "INSERT ASSETS TO " + hcf.getHcfname();
                    userlogs.setActby(assets.getCreatedby());
                    userlogs.setActdate(assets.getDatecreated());
                    userlogs.setActdetails(actdetails);
                    ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                    result.setMessage(getinsertresult.getString("Message") + " , " + insertActivitylogs.getMessage());
                } else {
                    ACRGBWSResult getSubjectA = methods.GETMBWITHID(datasource, assets.getHcfid());
                    userlogs.setActby(assets.getCreatedby());
                    userlogs.setActdate(assets.getDatecreated());
                    if (getSubjectA.isSuccess()) {
                        ManagingBoard mb = utility.ObjectMapper().readValue(getSubjectA.getResult(), ManagingBoard.class);
                        String actdetails = "INSERT ASSETS TO " + mb.getMbname();
                        userlogs.setActdetails(actdetails);
                    } else {
                        String actdetails = "INSERT ASSETS TO HCPN ";
                        userlogs.setActdetails(actdetails);
                    }
                    ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                    result.setMessage(getinsertresult.getString("Message") + " , " + insertActivitylogs.getMessage());
                }
                Tranch tranch = utility.ObjectMapper().readValue(fm.ACR_TRANCHWITHID(datasource, assets.getTranchid()).getResult(), Tranch.class);
                if (tranch.getTranchtype().trim().equals("1ST")) {
                    updatemethods.UPDATECONBALANCESTATS(datasource, assets.getHcfid());
                }
                result.setSuccess(true);
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }

        } catch (SQLException | IOException | ParseException ex) {
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
        try (Connection connection = datasource.getConnection()) {
            if (contract.getAmount().isEmpty()
                    || contract.getCreatedby().isEmpty()
                    || contract.getDatecreated().isEmpty()
                    || contract.getHcfid().isEmpty()) {
                result.setMessage("SOME REQUIRED FIELDS IS EMPTY");
                result.setSuccess(false);
            } else if (!utility.IsValidNumber(contract.getHcfid()) || !utility.IsValidNumber(contract.getCreatedby())) {
                result.setMessage("NUMBER FORMAT IS NOT VALID");
                result.setSuccess(false);
            } else {
                if (!utility.IsValidDate(contract.getDatecreated())) {
                    result.setSuccess(false);
                    result.setMessage("DATE FORMAT IS NOT VALID");
                } else {
                    CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTCONTRACT(:Message,:Code,:p_hcfid,:p_amount"
                            + ",:p_createdby,:p_datecreated,:p_contractdate,:p_transcode,"
                            + ":p_baseamount,:c_claimsvol,:t_claimsvol)");
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
                    getinsertresult.execute();
                    if (getinsertresult.getString("Message").equals("SUCC")) {
                        //INSERT TO ACTIVITY LOGS
                        UserActivity userlogs = utility.UserActivity();
                        ACRGBWSResult getSubject = fm.GETFACILITYID(datasource, contract.getHcfid());
                        if (getSubject.isSuccess()) {
                            HealthCareFacility hcf = utility.ObjectMapper().readValue(getSubject.getResult(), HealthCareFacility.class);
                            String actdetails = "INSERT CONTRACT TO " + hcf.getHcfname();
                            userlogs.setActby(contract.getCreatedby());
                            userlogs.setActdate(contract.getDatecreated());
                            userlogs.setActdetails(actdetails);
                            ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                            result.setMessage(getinsertresult.getString("Message") + " , " + insertActivitylogs.getMessage());
                        } else {
                            ACRGBWSResult getSubjectA = methods.GETMBWITHID(datasource, contract.getHcfid());
                            userlogs.setActby(contract.getCreatedby());
                            userlogs.setActdate(contract.getDatecreated());
                            if (getSubjectA.isSuccess()) {
                                ManagingBoard mb = utility.ObjectMapper().readValue(getSubjectA.getResult(), ManagingBoard.class);
                                String actdetails = "INSERT CONTRACT TO " + mb.getMbname();
                                userlogs.setActdetails(actdetails);
                            } else {
                                String actdetails = "INSERT BUDGET TO PRO ";
                                userlogs.setActdetails(actdetails);
                            }
                            ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                            result.setMessage(getinsertresult.getString("Message") + " , " + insertActivitylogs.getMessage());
                        }

                        ACRGBWSResult insertRoleIndex = um.UPDATEROLEINDEX(datasource,
                                contract.getHcfid(), contract.getContractdate(), "UPDATE");

                        result.setSuccess(true);
                    } else {
                        result.setMessage(getinsertresult.getString("Message"));
                    }

                }
            }
        } catch (SQLException | ParseException | IOException ex) {
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
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidDate(tranch.getDatecreated())) {
                result.setSuccess(false);
                result.setMessage("DATE FORMAT IS NOT VALID");
            } else if (tranch.getTranchtype().isEmpty() || tranch.getPercentage().isEmpty() || tranch.getCreatedby().isEmpty() || tranch.getDatecreated().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("SOME REQUIRED FIELD IS EMPTY");
            } else if (!utility.IsValidNumber(tranch.getPercentage())) {
                result.setSuccess(false);
                result.setMessage("PERCENTAGE VALUE IS NOT VALID");
            } else if (!utility.IsValidNumber(tranch.getCreatedby())) {
                result.setSuccess(false);
                result.setMessage("INVALID NUMBER FORMAT");
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTTRANCH(:Message,:Code,:p_tranchtype,:p_percentage,:p_createdby,:p_datecreated)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("p_tranchtype", tranch.getTranchtype().toUpperCase());
                getinsertresult.setString("p_percentage", tranch.getPercentage());
                getinsertresult.setString("p_createdby", tranch.getCreatedby());
                getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(tranch.getDatecreated()).getTime()));//tranch.getDatecreated());
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    UserActivity userlogs = utility.UserActivity();
                    String actdetails = "ADD TRANCH DATA ["
                            + tranch.getTranchtype() + "]";
                    userlogs.setActby(tranch.getCreatedby());
                    userlogs.setActdate(tranch.getDatecreated());
                    userlogs.setActdetails(actdetails);
                    ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                    result.setSuccess(true);
                    result.setMessage(getinsertresult.getString("Message") + " LOGS STATUS: " + insertActivitylogs.getMessage());
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
            }
        } catch (SQLException | ParseException ex) {
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
        try (Connection connection = datasource.getConnection()) {
            if (userinfo.getFirstname() != null && userinfo.getLastname() != null) {
                if (!utility.IsValidDate(userinfo.getDatecreated())) {
                    result.setSuccess(false);
                    result.setMessage("DATE FORMAT IS NOT VALID");
                } else {
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
                            UserActivity userlogs = utility.UserActivity();
                            String actdetails = "INSERT USER DETAILS " + userinfo.getContact() + " "
                                    + " " + userinfo.getLastname() + " , " + userinfo.getFirstname();
                            userlogs.setActby(userinfo.getCreatedby());
                            userlogs.setActdate(userinfo.getDatecreated());
                            userlogs.setActdetails(actdetails);
                            ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                            result.setMessage(getinsertresult.getString("Message") + " , " + insertActivitylogs.getMessage());
                            result.setSuccess(true);
                        } else {
                            result.setMessage(getinsertresult.getString("Message"));
                        }
                    } else {
                        result.setMessage(validateUsername.getMessage());
                        result.setResult(validateUsername.getResult());
                    }
                }
            } else {
                result.setMessage("SOME REQUIRED FIELD IS EMPTY");
            }
        } catch (SQLException | ParseException ex) {
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
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidDate(userlevel.getDatecreated())) {
                result.setSuccess(false);
                result.setMessage("DATE FORMAT IS NOT VALID");
            } else {
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
                    UserActivity userlogs = utility.UserActivity();
                    String actdetails = "INSERT USER LEVEL " + userlevel.getLevname();
                    userlogs.setActby(userlevel.getCreatedby());
                    userlogs.setActdate(userlevel.getDatecreated());
                    userlogs.setActdetails(actdetails);
                    ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                    result.setSuccess(true);
                    result.setMessage(getinsertresult.getString("Message") + " , " + insertActivitylogs.getMessage());
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }

            }
        } catch (SQLException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//---------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTUSER(final DataSource datasource, User user) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Forgetpassword emailsender = new Forgetpassword();
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
            ACRGBWSResult levelresult = fm.GETUSERLEVEL(datasource, user.getLeveid());
            int countresult = 0;
            if (levelresult.isSuccess()) {
                if (!levelresult.getResult().isEmpty()) {
                    countresult++;
                }
            }
            ACRGBWSResult infolistresult = fm.ACR_USER_DETAILS(datasource, "ACTIVE");
            int countinfo = 0;
            if (levelresult.isSuccess()) {
                if (!infolistresult.getResult().isEmpty()) {
                    List<UserInfo> infolist = Arrays.asList(utility.ObjectMapper().readValue(infolistresult.getResult(), UserInfo[].class));
                    for (int x = 0; x < infolist.size(); x++) {
                        if (infolist.get(x).getDid().equals(user.getDid())) {
                            countinfo++;
                        }
                    }
                }
            }
            if (!utility.IsValidDate(user.getDatecreated())) {
                result.setSuccess(false);
                result.setMessage("DATE FORMAT IS NOT VALID");
            } else if (countresult == 0) {
                result.setSuccess(false);
                result.setMessage("ROLE ID NOT FOUND");
            } else if (countinfo == 0) {
                result.setSuccess(false);
                result.setMessage("USER INFO ID NOT FOUND");
            } else {
                ACRGBWSResult validateUsername = methods.ACRUSERNAME(datasource, user.getUsername());
                if (validateUsername.isSuccess()) {
                    ACRGBWSResult validateRole = methods.ACRUSERLEVEL(datasource, user.getLeveid());
                    if (validateRole.isSuccess()) {
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
                            result.setSuccess(true);
                            //SEND PASSCODE TO EMAIL IF SUCCESS
                            ACRGBWSResult emailResult = emailsender.Forgetpassword(datasource, user.getUsername().trim(), user.getUserpassword().trim());
                            UserActivity userlogs = utility.UserActivity();
                            String level = "";
                            ACRGBWSResult levelname = fm.GETUSERLEVEL(datasource, user.getLeveid());
                            if (levelname.isSuccess()) {
                                level = levelname.getResult();
                            } else {
                                level = "Level not found";
                            }
                            String actdetails = "INSERT USER ACCOUNT " + user.getUsername() + " Level " + level;
                            userlogs.setActby(user.getCreatedby());
                            userlogs.setActdate(user.getDatecreated());
                            userlogs.setActdetails(actdetails);
                            ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                            result.setSuccess(true);
                            result.setMessage(getinsertresult.getString("Message") + ""
                                    + " AND" + emailResult.getMessage() + " \n " + insertActivitylogs.getMessage());

                        } else {
                            result.setMessage(getinsertresult.getString("Message"));
                        }
                    } else {
                        result.setMessage(validateRole.getMessage());
                        result.setResult(validateRole.getResult());
                    }
                } else {
                    result.setMessage(validateUsername.getMessage());
                    result.setResult(validateUsername.getResult());
                }
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSEROLEINDEX(final DataSource datasource, UserRoleIndex userroleindex) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidDate(userroleindex.getDatecreated())) {
                result.setMessage("DATE FORMAT IS NOT VALID");
            } else {
                ArrayList<String> errorList = new ArrayList<>();
                List<String> accesslist = Arrays.asList(userroleindex.getAccessid().split(","));
                int errCount = 0;
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
                        errCount++;
                        errorList.add(getinsertresult.getString("Message"));
                    }
                }
                if (errCount == 0) {
                    UserActivity userlogs = utility.UserActivity();
                    String actdetails = "ADD ACCESS " + userroleindex.getUserid() + " TO " + userroleindex.getAccessid();
                    userlogs.setActby(userroleindex.getCreatedby());
                    userlogs.setActdate(userroleindex.getDatecreated());
                    userlogs.setActdetails(actdetails);
                    ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                    result.setSuccess(true);
                    result.setMessage("ADD ACCESS OK , INSERT LOGS " + insertActivitylogs.getMessage());
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

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult REMOVEDACCESSLEVEL(final DataSource datasource, UserRoleIndex userroleindex) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
            ArrayList<String> errorList = new ArrayList<>();
            List<String> accesslist = Arrays.asList(userroleindex.getAccessid().split(","));
            int errCount = 0;
            for (int x = 0; x < accesslist.size(); x++) {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.REMOVEDACCESSLEVEL(:Message,:Code,"
                        + ":a_userid,:a_accessid)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("a_userid", userroleindex.getUserid());
                getinsertresult.setString("a_accessid", accesslist.get(x));
                getinsertresult.execute();
                if (!getinsertresult.getString("Message").equals("SUCC")) {
                    errCount++;
                    errorList.add(getinsertresult.getString("Message"));
                }
            }
            if (errCount == 0) {
                UserActivity userlogs = utility.UserActivity();
                String actdetails = "DELETE ACCESS " + userroleindex.getUserid() + " TO " + userroleindex.getAccessid();
                userlogs.setActby(userroleindex.getCreatedby());
                userlogs.setActdate(userroleindex.getDatecreated());
                userlogs.setActdetails(actdetails);
                ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                result.setSuccess(true);
                result.setMessage("REMOVE ACCESS OK , INSERT LOGS " + insertActivitylogs.getMessage());
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
            final String createdby,
            final String datecreated) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidNumber(dataid)) {
                result.setMessage(" " + dataid + " NUMBER FORMAT IS NOT VALID");
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INACTIVEDATA(:Message,:Code,"
                        + ":tags,:dataid)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("tags", tags);
                getinsertresult.setInt("dataid", Integer.parseInt(dataid));
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    UserActivity userlogs = utility.UserActivity();
                    String actdetails = "UPDATE STATUS TO INACTIVE" + tags + " TO 3 Data ID" + dataid;
                    userlogs.setActby(createdby);
                    userlogs.setActdate(datecreated);
                    userlogs.setActdetails(actdetails);
                    ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                    result.setSuccess(true);
                    result.setMessage(getinsertresult.getString("Message") + " Logs status :" + insertActivitylogs.getMessage());
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult ACTIVEDATA(final DataSource datasource,
            final String tags,
            final String dataid,
            final String createdby,
            final String datecreated) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidNumber(dataid)) {
                result.setMessage(" " + dataid + " NUMBER FORMAT IS NOT VALID");
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.ACTIVEDATA(:Message,:Code,"
                        + ":tags,:dataid)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("tags", tags);
                getinsertresult.setInt("dataid", Integer.parseInt(dataid));
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    UserActivity userlogs = utility.UserActivity();
                    String actdetails = "UPDATE STATUS TO ACTIVE" + tags + " TO 3 Data ID" + dataid;
                    userlogs.setActby(createdby);
                    userlogs.setActdate(datecreated);
                    userlogs.setActdetails(actdetails);
                    ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                    result.setSuccess(true);
                    result.setMessage(getinsertresult.getString("Message") + " Logs status :" + insertActivitylogs.getMessage());
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTDATESETTINGS(final DataSource datasource, final DateSettings datesettings) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidDate(datesettings.getDatefrom()) || !utility.IsValidDate(datesettings.getDateto())) {
                result.setMessage("DATE FORMAT IS NOT VALID");
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTDATESETTINGS(:Message,:Code,"
                        + ":udatefrom,:udateto)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setDate("udatefrom", (Date) new Date(utility.StringToDate(datesettings.getDatefrom()).getTime()));
                getinsertresult.setDate("udateto", (Date) new Date(utility.StringToDate(datesettings.getDateto()).getTime()));
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    UserActivity userlogs = utility.UserActivity();
                    String actdetails = "ADD DATE REFERENCE POSSIBLE BUDGET AMOUNT From :"
                            + datesettings.getDatefrom() + " TO " + datesettings.getDateto();
                    userlogs.setActby(datesettings.getCreatedby());
                    userlogs.setActdate(datesettings.getDatecreated());
                    userlogs.setActdetails(actdetails);
                    ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                    result.setMessage(getinsertresult.getString("Message") + " Logs status :" + insertActivitylogs.getMessage());
                    result.setSuccess(true);
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTSKIPYEAR(final DataSource datasource, final DateSettings datesettings) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidDate(datesettings.getDatefrom()) || !utility.IsValidDate(datesettings.getDateto())) {
                result.setMessage("DATE FORMAT IS NOT VALID");
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTSKIPYEAR(:Message,:Code,"
                        + ":udatefrom,:udateto)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setDate("udatefrom", (Date) new Date(utility.StringToDate(datesettings.getDatefrom()).getTime()));
                getinsertresult.setDate("udateto", (Date) new Date(utility.StringToDate(datesettings.getDateto()).getTime()));
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    UserActivity userlogs = utility.UserActivity();
                    String actdetails = "ADD DATE REFERENCE SKIP YEAR From :"
                            + datesettings.getDatefrom() + " TO " + datesettings.getDateto();
                    userlogs.setActby(datesettings.getCreatedby());
                    userlogs.setActdate(datesettings.getDatecreated());
                    userlogs.setActdetails(actdetails);
                    ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                    result.setMessage(getinsertresult.getString("Message") + " Logs status :" + insertActivitylogs.getMessage());
                    result.setSuccess(true);
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTHCPN(final DataSource datasource, final ManagingBoard mb) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidDate(mb.getDatecreated())) {
                result.setMessage("DATE FORMAT IS NOT VALID");
            } else {
                ACRGBWSResult restA = methods.GETROLE(datasource, mb.getCreatedby(), "ACTIVE");
                if (restA.isSuccess()) {
                    ACRGBWSResult getProCode = methods.GetProWithPROID(datasource, restA.getResult());
                    if (!getProCode.isSuccess()) {
                        result.setMessage(getProCode.getMessage());
                    } else {
                        Pro pro = utility.ObjectMapper().readValue(getProCode.getResult(), Pro.class);
                        UserRoleIndex indexrole = new UserRoleIndex();
                        indexrole.setUserid("2024" + pro.getProcode());
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
//                                result.setMessage(getinsertresult.getString("Message"));
//                                result.setSuccess(true);
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
                                logstats.setDatechange(mb.getDatecreated());
                                logstats.setStatus("2");
                                ACRGBWSResult logsResult = this.INSERTSTATSLOG(datasource, logstats);
                                if (logsResult.isSuccess() && accreResult.isSuccess()) {
                                    result.setMessage(logsResult.getMessage() + " , " + accreResult.getMessage());
                                    result.setSuccess(true);
                                } else {
                                    result.setMessage(logsResult.getMessage() + " , " + accreResult.getMessage());
                                }
                            } else {
                                result.setMessage(getinsertresult.getString("Message"));
                            }

                            UserActivity userlogs = utility.UserActivity();
                            String actdetails = "INSERT NEW HCPN PROFILE :"
                                    + mb.getMbname() + " , " + mb.getAddress() + " , " + mb.getControlnumber();
                            userlogs.setActby(mb.getCreatedby());
                            userlogs.setActdate(mb.getDatecreated());
                            userlogs.setActdetails(actdetails);
                            ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);

                        } else {
                            result.setMessage(insertRole.getMessage());
                        }
                    }
                } else {
                    result.setMessage(restA.getMessage());
                }
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //INSERT ACCREDITATION  
    public ACRGBWSResult INSERTACCREDITAION(final DataSource datasource, final Accreditation accre) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidDate(accre.getDatecreated())) {
                result.setMessage("DATE FORMAT IS NOT VALID");
            } else {
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
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //INSERT LOGS STATUS
    public ACRGBWSResult INSERTSTATSLOG(final DataSource datasource, final LogStatus logs) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidDate(logs.getDatechange())) {
                result.setMessage("DATE FORMAT IS NOT VALID");
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTSTATSLOG(:Message,:Code,"
                        + ":uaccount,:ustatus,:udatechange,:uactby)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("uaccount", logs.getAccount());
                getinsertresult.setString("ustatus", logs.getStatus());
                getinsertresult.setDate("udatechange", (Date) new Date(utility.StringToDate(logs.getDatechange()).getTime()));
                getinsertresult.setString("uactby", logs.getActby());
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(true);
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult INSERTAPPELLATE(final DataSource datasource, final String uaccesscode,
            final String ucontrolcode, final String createdby, final String datecreated) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
            ArrayList<String> errorList = new ArrayList<>();
            List<String> accesslist = Arrays.asList(ucontrolcode.split(","));
            int errCount = 0;
            for (int x = 0; x < accesslist.size(); x++) {
                //------------------------------------------------------------------------------------------------
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTAPPELLATE(:Message,:Code,"
                        + ":uaccesscode,:ucontrolcode)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("uaccesscode", uaccesscode);
                getinsertresult.setString("ucontrolcode", accesslist.get(x));
                getinsertresult.execute();
                //------------------------------------------------------------------------------------------------
                if (!getinsertresult.getString("Message").equals("SUCC")) {
                    errCount++;
                    errorList.add(getinsertresult.getString("Message"));
                }
            }
            if (errCount == 0) {
                UserActivity userlogs = utility.UserActivity();
                String actdetails = "ADD APPELATE APEX TO HCPN :"
                        + ucontrolcode + " TO :" + uaccesscode;
                userlogs.setActby(createdby);
                userlogs.setActdate(datecreated);
                userlogs.setActdetails(actdetails);
                ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                result.setSuccess(true);
                result.setMessage("OK ADD APPELATE, LOGS STATUS: " + insertActivitylogs.getMessage());
            } else {
                result.setMessage(errorList.toString());
            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //INSERT PAYMENT TYPE
    public ACRGBWSResult ACRPAYMENTTYPE(final DataSource datasource, final PaymentType paymentType) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            //------------------------------------------------------------------------------------------------
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.ACRPAYMENTTYPE(:Message,:Code,"
                    + ":uaccount,:udatecreted,:uconid,:udatefrom,:udateto,:ucreatedby,:upaymenttype,:ureference)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("uaccount", paymentType.getUaccount());
            getinsertresult.setString("udatecreted", paymentType.getUdatecreted());
            getinsertresult.setString("uconid", paymentType.getUconid());
            getinsertresult.setString("udatefrom", paymentType.getUdatefrom());
            getinsertresult.setString("udateto", paymentType.getUdateto());
            getinsertresult.setString("ucreatedby", paymentType.getUcreatedby());
            getinsertresult.setString("upaymenttype", paymentType.getUpaymenttype());
            getinsertresult.setString("ureference", paymentType.getUreference());
            getinsertresult.execute();
            //------------------------------------------------------------------------------------------------
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //INSERT BOOK
    public ACRGBWSResult ACRBOOKING(final DataSource datasource, final Book book) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
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
                UserActivity userlogs = utility.UserActivity();
                String actdetails = "ADD BOOKING REFERENCES DATA :"
                        + book.getBooknum() + " , for Contract " + book.getConid();
                userlogs.setActby(book.getCreatedby());
                userlogs.setActdate(book.getDatecreated());
                userlogs.setActdetails(actdetails);
                ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message") + " LOGS STATUS: " + insertActivitylogs.getMessage());

            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //INSERT BOOK DATA
    public ACRGBWSResult ACRBOOKINGDATA(final DataSource datasource,
            final NclaimsData nclaims, final String booknum, final String datecreated, final String createdby) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
            //------------------------------------------------------------------------------------------------
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.ACRBOOKINGDATA(:Message,:Code,"
                    + ":useries,:uaccreno,:upmccno,:udateadmission,:udatesubmitted,:uclaimamount,:ubooknum,"
                    + ":utags,:urvscode,:uicdcode,:utrn,:ubentype,:uclaimid,:uhcfname)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("useries", nclaims.getSeries());
            getinsertresult.setString("uaccreno", nclaims.getAccreno());
            if (nclaims.getPmccno().equals("N/A")) {
                getinsertresult.setString("upmccno", nclaims.getPmccno());
            } else {
                HealthCareFacility hci = utility.ObjectMapper().readValue(nclaims.getPmccno(), HealthCareFacility.class);
                getinsertresult.setString("upmccno", hci.getHcfcode());
            }
            if (nclaims.getDateadmission() == null) {
                getinsertresult.setString("udateadmission", nclaims.getDateadmission());
            } else {
                getinsertresult.setDate("udateadmission", (Date) new Date(utility.StringToDate(nclaims.getDateadmission()).getTime()));
            }
            if (nclaims.getDatesubmitted() == null) {
                getinsertresult.setString("udatesubmitted", nclaims.getDatesubmitted());
            } else {
                getinsertresult.setDate("udatesubmitted", (Date) new Date(utility.StringToDate(nclaims.getDatesubmitted()).getTime()));
            }
            getinsertresult.setString("uclaimamount", nclaims.getClaimamount());
            getinsertresult.setString("ubooknum", booknum);
            getinsertresult.setString("utags", nclaims.getTags());
            getinsertresult.setString("urvscode", nclaims.getRvscode());
            getinsertresult.setString("uicdcode", nclaims.getIcdcode());
            getinsertresult.setString("utrn", nclaims.getTrn());
            getinsertresult.setString("ubentype", nclaims.getBentype());
            getinsertresult.setString("uclaimid", nclaims.getClaimid());
            getinsertresult.setString("uhcfname", nclaims.getHcfname());
            getinsertresult.execute();
            //------------------------------------------------------------------------------------------------
            if (getinsertresult.getString("Message").equals("SUCC")) {
                //FOR REVIEW THIS LINE   
                UserActivity userlogs = utility.UserActivity();
                String actdetails = "ADD BOOK CLAIMS DATA: Series["
                        + nclaims.getSeries() + "] ,BookNum[" + booknum + "]";
                userlogs.setActby(createdby);
                userlogs.setActdate(datecreated);
                userlogs.setActdetails(actdetails);
                ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message") + " LOGS STATUS: " + insertActivitylogs.getMessage());
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //INSERT LOGS STATUS
    public ACRGBWSResult INSERTCONDATE(final DataSource datasource, final ContractDate contractdate) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
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
                UserActivity userlogs = utility.UserActivity();
                String actdetails = "ADD CONTRACT DATE FROM["
                        + contractdate.getDatefrom() + "] , TO[" + contractdate.getDateto() + "]";
                userlogs.setActby(contractdate.getCreatedby());
                userlogs.setActdate(contractdate.getDatecreated());
                userlogs.setActdetails(actdetails);
                ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message") + " LOGS STATUS: " + insertActivitylogs.getMessage());
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //INSERT USER ACCOUNT BATCH UPLOAD
    public ACRGBWSResult INSERTUSERACCOUNTBATCHUPLOAD(final DataSource datasource, final UserInfo userinfo) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
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
            if (getinsertresult.getString("Message").equals("SUCC")) {
                UserActivity userlogs = utility.UserActivity();
                String actdetails = "INSERT USER DETAILS " + userinfo.getContact() + " "
                        + " " + userinfo.getLastname() + " , " + userinfo.getFirstname();
                userlogs.setActby(userinfo.getCreatedby());
                userlogs.setActdate(userinfo.getDatecreated());
                userlogs.setActdetails(actdetails);
                ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
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
                            ACRGBWSResult insertRoleIndex = this.INSEROLEINDEX(datasource, userrole);
                            if (insertRoleIndex.isSuccess()) {
                            }
                        }
                    }
                }
                result.setMessage(getinsertresult.getString("Message") + ""
                        + " , " + insertActivitylogs.getMessage() + " , "
                        + getUserInfoUsingEmail.getMessage());
                result.setSuccess(true);
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
