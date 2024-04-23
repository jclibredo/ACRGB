/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Accreditation;
import acrgb.structure.Assets;
import acrgb.structure.Contract;
import acrgb.structure.DateSettings;
import acrgb.structure.HealthCareFacility;
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
    private final Methods methods = new Methods();
    private final FetchMethods fm = new FetchMethods();

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTASSETS(final DataSource datasource, Assets assets) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            //GET TRANCH PERCENTAGE
            if (assets.getCreatedby().isEmpty()
                    || assets.getDatecreated().isEmpty()
                    || assets.getDatereleased().isEmpty()
                    || assets.getHcfid().isEmpty()
                    || assets.getReceipt().isEmpty()
                    || assets.getTranchid().isEmpty()) {
                result.setMessage("SOME REQUIRED FIELDS IS EMPTY");
                result.setSuccess(false);
            } else if (utility.IsValidNumber(assets.getHcfid()) && utility.IsValidNumber(assets.getTranchid())) {
                ACRGBWSResult tranchresult = fm.GETTRANCHAMOUNT(datasource, assets.getTranchid());
                if (!tranchresult.isSuccess()) {
                    result.setMessage(tranchresult.getMessage());
                } else if (!utility.IsValidDate(assets.getDatecreated()) || !utility.IsValidDate(assets.getDatereleased())) {
                    result.setMessage("DATE FORMAT IS NOT VALID");
                } else {
                    ACRGBWSResult conresult = fm.GETCONTRACTAMOUNT(datasource, assets.getHcfid());
                    if (conresult.isSuccess()) {
                        ACRGBWSResult transresult = fm.ACR_ASSETS(datasource, "active", "0");
                        if (transresult.isSuccess()) {
                            ACRGBWSResult trans = fm.ACR_TRANCH(datasource, "active");
                            if (trans.isSuccess()) {
                                int transcount = 0;
                                List<Tranch> tranchlist = Arrays.asList(utility.ObjectMapper().readValue(trans.getResult(), Tranch[].class));
                                for (int x = 0; x < tranchlist.size(); x++) {
                                    if (tranchlist.get(x).getTranchid().equals(assets.getTranchid())) {
                                        transcount++;
                                    }
                                }

                                //---------------------------------------------------------------
                                int count = 0;
                                List<Assets> assetslist = Arrays.asList(utility.ObjectMapper().readValue(transresult.getResult(), Assets[].class));
                                for (int x = 0; x < assetslist.size(); x++) {
                                    if (assets.getHcfid().trim().equals(assetslist.get(x).getHcfid().trim())
                                            && assets.getTranchid().trim().equals(assetslist.get(x).getTranchid().trim())
                                            && assetslist.get(x).getStatus().equals("2")) {
                                        count++;
                                    }
                                }

                                if (transcount == 0) {
                                    result.setSuccess(false);
                                    result.setMessage("TRANCH VALUE NOT FOUND");
                                } else {
                                    if (count < 1) {
                                        Double percentage = Double.parseDouble(tranchresult.getResult());//60 percent
                                        Double totalpercent = percentage / 100;
                                        Double amount = Double.parseDouble(conresult.getResult());//contract amount
                                        Double p_amount = amount * totalpercent;
                                        CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTASSETS(:Message,:Code,:p_hcfid,:p_tranchid ,:p_receipt,:p_amount"
                                                + ",:p_createdby,:p_datereleased,:p_datecreated,:p_conid)");
                                        getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                                        getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                                        getinsertresult.setString("p_hcfid", assets.getHcfid());
                                        getinsertresult.setString("p_tranchid", assets.getTranchid());
                                        getinsertresult.setString("p_receipt", assets.getReceipt());
                                        getinsertresult.setString("p_amount", String.valueOf(p_amount));
                                        getinsertresult.setString("p_createdby", assets.getCreatedby());
                                        getinsertresult.setDate("p_datereleased", (Date) new Date(utility.StringToDate(assets.getDatecreated()).getTime())); //assets.getDatereleased());
                                        getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(assets.getDatecreated()).getTime()));//assets.getDatecreated());
                                        getinsertresult.setString("p_conid", assets.getConid());
                                        getinsertresult.execute();
                                        if (getinsertresult.getString("Message").equals("SUCC")) {
                                            result.setSuccess(true);
                                            result.setMessage(getinsertresult.getString("Message"));
                                        } else {
                                            result.setMessage(getinsertresult.getString("Message"));
                                        }
                                    } else {
                                        result.setMessage("TRANCH VALUE IS ALREADY ASSIGN TO FACILITY");
                                    }
                                }
                            } else {
                                result.setMessage(trans.getMessage());
                            }
                        } else {
                            result.setMessage(transresult.getMessage());
                        }
                    } else {
                        result.setMessage(conresult.getMessage());
                    }
                }
            } else {
                result.setMessage("NUMBER FORMAT IS NOT VALID");
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

        try (Connection connection = datasource.getConnection()) {
            if (contract.getAmount().isEmpty()
                    || contract.getCreatedby().isEmpty()
                    || contract.getDatecreated().isEmpty()
                    || contract.getDatefrom().isEmpty()
                    || contract.getDateto().isEmpty()
                    || contract.getHcfid().isEmpty()) {
                result.setMessage("SOME REQUIRED FIELDS IS EMPTY");
                result.setSuccess(false);
            } else if (!utility.IsValidNumber(contract.getHcfid()) || !utility.IsValidNumber(contract.getCreatedby())) {
                result.setMessage("NUMBER FORMAT IS NOT VALID");
                result.setSuccess(false);
            } else {
                if (!utility.IsValidDate(contract.getDatecreated()) || !utility.IsValidDate(contract.getDatefrom()) || !utility.IsValidDate(contract.getDateto())) {
                    result.setSuccess(false);
                    result.setMessage("DATE FORMAT IS NOT VALID");
                } else {
                    CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTCONTRACT(:Message,:Code,:p_hcfid,:p_amount"
                            + ",:p_createdby,:p_datecreated,:p_datefrom,:p_dateto,:p_transcode,:p_baseamount)");
                    getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                    getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                    getinsertresult.setString("p_hcfid", contract.getHcfid());//PAN Number or MB Accreditaion Number
                    getinsertresult.setString("p_amount", contract.getAmount());
                    getinsertresult.setString("p_createdby", contract.getCreatedby());
                    getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(contract.getDatecreated()).getTime()));//contract.getDatecreated());
                    getinsertresult.setDate("p_datefrom", (Date) new Date(utility.StringToDate(contract.getDatefrom()).getTime()));
                    getinsertresult.setDate("p_dateto", (Date) new Date(utility.StringToDate(contract.getDateto()).getTime()));
                    getinsertresult.setString("p_transcode", contract.getTranscode());
                    getinsertresult.setString("p_baseamount", contract.getBaseamount());
                    getinsertresult.execute();
                    if (getinsertresult.getString("Message").equals("SUCC")) {
                        result.setSuccess(true);
                        result.setMessage(getinsertresult.getString("Message"));
                    } else {
                        result.setMessage(getinsertresult.getString("Message"));
                    }
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
                    } else {
                        ACRGBWSResult getSubjectA = methods.GETMBWITHID(datasource, contract.getHcfid());
                        userlogs.setActby(contract.getCreatedby());
                        userlogs.setActdate(contract.getDatecreated());
                        if (getSubjectA.isSuccess()) {
                            ManagingBoard mb = utility.ObjectMapper().readValue(getSubjectA.getResult(), ManagingBoard.class);
                            String actdetails = "INSERT CONTRACT TO " + mb.getMbname();
                            userlogs.setActdetails(actdetails);
                        } else {
                            String actdetails = "INSERT CONTRACT TO HCPN ";
                            userlogs.setActdetails(actdetails);
                        }
                        ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);

                    }

                }
            }
        } catch (SQLException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
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
                    result.setSuccess(true);
                    result.setMessage(getinsertresult.getString("Message"));
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
        try (Connection connection = datasource.getConnection()) {
            if (userinfo.getFirstname() != null && userinfo.getLastname() != null) {
                if (!utility.IsValidDate(userinfo.getDatecreated())) {
                    result.setSuccess(false);
                    result.setMessage("DATE FORMAT IS NOT VALID");
                } else {
                    CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTUSERDETAILS(:Message,:Code,"
                            + ":p_firstname,:p_lastname,:p_middlename,:p_datecreated,:p_createdby)");
                    getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                    getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                    getinsertresult.setString("p_firstname", userinfo.getFirstname().toUpperCase());
                    getinsertresult.setString("p_lastname", userinfo.getLastname().toUpperCase());
                    getinsertresult.setString("p_middlename", userinfo.getMiddlename().toUpperCase());
                    getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(userinfo.getDatecreated()).getTime()));//userinfo.getDatecreated());
                    getinsertresult.setString("p_createdby", userinfo.getCreatedby());
                    getinsertresult.execute();
                    if (getinsertresult.getString("Message").equals("SUCC")) {
                        result.setSuccess(true);
                        result.setMessage(getinsertresult.getString("Message"));
                    } else {
                        result.setMessage(getinsertresult.getString("Message"));
                    }
                }
            } else {
                result.setMessage("SOME REQUIRED FIELD IS EMPTY");
            }
        } catch (SQLException | ParseException ex) {
            result.setMessage(ex.toString() + "YOUR ARE HERE");
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
                    result.setSuccess(true);
                    result.setMessage(getinsertresult.getString("Message"));
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
        try (Connection connection = datasource.getConnection()) {
            ACRGBWSResult levelresult = fm.GETUSERLEVEL(datasource, user.getLeveid());
            int countresult = 0;
            if (levelresult.isSuccess()) {
                if (!levelresult.getResult().isEmpty()) {
                    countresult++;
                }
            }
            ACRGBWSResult infolistresult = fm.ACR_USER_DETAILS(datasource, "active");
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
            if (!utility.validatePassword(user.getUserpassword())) {
                result.setSuccess(false);
                result.setMessage("PASSWORD IS NOT VALID NOT MEET WITH THE REQUIREMENTS");
            } else if (!utility.IsValidDate(user.getDatecreated())) {
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
                        getinsertresult.setString("p_username", user.getUsername().toUpperCase());
                        if (validateRole.getResult().equals("Admin")) {
                            getinsertresult.setString("p_userpassword", encryptpword);
                        } else {
                            getinsertresult.setString("p_userpassword", user.getUserpassword());
                        }
                        getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(user.getDatecreated()).getTime())); //userlevel.getDatecreated());//user.getDatecreated());
                        getinsertresult.setString("p_createdby", user.getCreatedby());
                        if (validateRole.getResult().equals("Admin")) {
                            getinsertresult.setString("p_stats", "2");

                        } else {
                            getinsertresult.setString("p_stats", "1");

                        }
                        getinsertresult.setString("p_did", user.getDid());
                        getinsertresult.execute();

                        if (getinsertresult.getString("Message").equals("SUCC")) {
                            result.setSuccess(true);
                            result.setMessage(getinsertresult.getString("Message"));
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
                    getinsertresult.setString("a_userid", userroleindex.getUserid());
                    getinsertresult.setString("a_accessid", accesslist.get(x));
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

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult REMOVEDACCESSLEVEL(final DataSource datasource, UserRoleIndex userroleindex) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
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
    public ACRGBWSResult INACTIVEDATA(final DataSource datasource, final String tags, final String dataid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
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
                    result.setSuccess(true);
                    result.setMessage(getinsertresult.getString("Message"));
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
    public ACRGBWSResult ACTIVEDATA(final DataSource datasource, final String tags, final String dataid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
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

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTDATESETTINGS(final DataSource datasource, final DateSettings datesettings) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
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

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTSKIPYEAR(final DataSource datasource, final DateSettings datesettings) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
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

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTHCPN(final DataSource datasource, final ManagingBoard mb) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidDate(mb.getDatecreated())) {
                result.setMessage("DATE FORMAT IS NOT VALID");
            } else {
                ACRGBWSResult restA = methods.GETROLE(datasource, mb.getCreatedby());
                System.out.println(restA);
                if (restA.isSuccess()) {
                    ACRGBWSResult getProCode = methods.GetProWithPROID(datasource, restA.getResult());
                    if (!getProCode.isSuccess()) {
                        result.setMessage(getProCode.getMessage());
                    } else {
                        Pro pro = utility.ObjectMapper().readValue(getProCode.getResult(), Pro.class);
                        UserRoleIndex indexrole = new UserRoleIndex();
                        indexrole.setUserid(pro.getProcode());
                        indexrole.setAccessid(mb.getControlnumber());
                        indexrole.setCreatedby(mb.getCreatedby());
                        indexrole.setDatecreated(mb.getDatecreated());
                        ACRGBWSResult insertRole = this.INSEROLEINDEX(datasource, indexrole);
                        if (insertRole.isSuccess()) {
                            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTHCPN(:Message,:Code,"
                                    + ":umbname,:udatecreated,:ucreatedby,:uaccreno)");
                            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                            getinsertresult.setString("umbname", mb.getMbname().toUpperCase());
                            getinsertresult.setDate("udatecreated", (Date) new Date(utility.StringToDate(mb.getDatecreated()).getTime()));
                            getinsertresult.setString("ucreatedby", mb.getCreatedby());
                            getinsertresult.setString("uaccreno", mb.getControlnumber());
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
            final String ucontrolcode) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
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

}
