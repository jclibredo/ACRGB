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
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.LogStatus;
import acrgb.structure.ManagingBoard;
import acrgb.structure.Tranch;
import acrgb.structure.User;
import acrgb.structure.UserActivity;
import acrgb.structure.UserLevel;
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
public class UpdateMethods {

    public UpdateMethods() {
    }
    private final Utility utility = new Utility();
    private final FetchMethods fm = new FetchMethods();
    private final InsertMethods im = new InsertMethods();

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult UPDATEASSETS(final DataSource datasource, Assets assets) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            UserActivity userLogs = utility.UserActivity();
//             String oldData = "";
//            if (fm.ge) {
//                Assets assets = utility.ObjectMapper().readValue(src, Assets.class);
//
//            }
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEASSETS(:Message,:Code,"
                    + ":p_assetsid,:p_hcfid,:p_receipt,:p_amount"
                    + ",:p_datereleased)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_assetsid", assets.getAssetid());
            getinsertresult.setString("p_hcfid", assets.getHcfid());
            getinsertresult.setString("p_receipt", assets.getReceipt());
            getinsertresult.setString("p_amount", assets.getAmount());
            getinsertresult.setDate("p_datereleased", (Date) new Date(utility.StringToDate(assets.getDatereleased()).getTime())); //assets.getDatereleased());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage("OK");
                userLogs.setActstatus("SUCCESS");
            } else {
                userLogs.setActstatus("FAILED");
                result.setMessage(getinsertresult.getString("Message"));
            }
//            userLogs.setActby(assets.getCreatedby());
//            logs.UserLogsMethod(datasource, "EDIT-TRANCHE-HCPN", userLogs, userroleindex.getUserid(), accesslist.get(x));
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult UPDATECONTRACT(final DataSource datasource, final Contract contract) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
            String oldData = "";
            if (fm.GETCONTRACTCONID(datasource, contract.getConid(), "ACTIVE").isSuccess()) {
                Contract con = utility.ObjectMapper().readValue(fm.GETCONTRACTCONID(datasource, contract.getConid(), "ACTIVE").getResult(), Contract.class);
                oldData = " Amount: " + con.getAmount() + " Volume :" + con.getComittedClaimsVol() + " Reference :" + con.getTranscode();
            } else {
                oldData = fm.GETCONTRACTCONID(datasource, contract.getConid(), "ACTIVE").getMessage();
            }
            String logsTags = "";
            UserActivity userlogs = utility.UserActivity();
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATECONTRACT(:Message,:Code,:p_conid,:p_hcfid,:p_amount"
                    + ",:p_contractdate,:p_transcode,:c_claimsvol,:p_quarter)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_conid", contract.getConid());
            getinsertresult.setString("p_hcfid", contract.getHcfid());
            getinsertresult.setString("p_amount", contract.getAmount());
            getinsertresult.setString("p_contractdate", contract.getContractdate());
            getinsertresult.setString("p_transcode", contract.getTranscode());
            getinsertresult.setString("c_claimsvol", contract.getComittedClaimsVol());
            getinsertresult.setString("p_quarter", contract.getQuarter());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setResult(utility.ObjectMapper().writeValueAsString(contract));
                result.setSuccess(true);
                result.setMessage("OK");
                ACRGBWSResult getSubject = fm.GETFACILITYID(datasource, contract.getHcfid());
                if (getSubject.isSuccess()) {
                    logsTags = "EDIT-CONTRACT-HCI";
                } else {
                    ACRGBWSResult getSubjectA = methods.GETMBWITHID(datasource, contract.getHcfid());
                    if (getSubjectA.isSuccess()) {
                        logsTags = "EDIT-CONTRACT-HCPN";
                    } else {
                        logsTags = "EDIT-CONTRACT-PRO";
                    }
                }
                userlogs.setActstatus("SUCCESS");
            } else {
                userlogs.setActstatus("FAILED");
                ACRGBWSResult getSubject = fm.GETFACILITYID(datasource, contract.getHcfid());
                if (getSubject.isSuccess()) {
                    logsTags = "EDIT-CONTRACT-HCI";
                } else {
                    ACRGBWSResult getSubjectA = methods.GETMBWITHID(datasource, contract.getHcfid());
                    if (getSubjectA.isSuccess()) {
                        logsTags = "EDIT-CONTRACT-HCPN";
                    } else {
                        logsTags = "EDIT-CONTRACT-PRO";
                    }
                }
                result.setMessage(getinsertresult.getString("Message"));
            }
            userlogs.setActby(contract.getCreatedby());
            userlogs.setActdetails("Old " + oldData + " New Data Amount :" + contract.getAmount() + "| SB :"
                    + contract.getSb() + "| Comitted volume:" + contract.getComittedClaimsVol()
                    + " " + contract.getQuarter() + " " + getinsertresult.getString("Message").equals("SUCC"));
            logs.UserLogsMethod(datasource, logsTags, userlogs, contract.getHcfid(), contract.getContractdate());
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult UPDATETRANCH(final DataSource datasource, Tranch tranch) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            String oldData = "";
            UserActivity userlogs = utility.UserActivity();
            if (fm.ACR_TRANCHWITHID(datasource, tranch.getTranchid()).isSuccess()) {
                Tranch tranche = utility.ObjectMapper().readValue(fm.GETUSERLEVEL(datasource, tranch.getTranchid()).getResult(), Tranch.class);
                oldData = " Value:" + tranche.getPercentage() + " Type: " + tranche.getTranchtype();
            } else {
                oldData = fm.ACR_TRANCHWITHID(datasource, tranch.getTranchid()).getMessage();
            }
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATETRANCH(:Message,:Code,:p_tranchid,:p_tranchtype,:p_percentage)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_tranchid", tranch.getTranchid());
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
            userlogs.setActby(tranch.getCreatedby());
            userlogs.setActdetails(" Data before :" + oldData + " Data after Type: " + tranch.getTranchtype() + " Value: " + tranch.getPercentage() + " | " + getinsertresult.getString("Message"));
            logs.UserLogsMethod(datasource, "EDIT-TRANCHE", userlogs, "0", "0");
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult UPDATEUSERLEVEL(final DataSource datasource, UserLevel userlevel) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            String oldData = "";
            UserActivity userlogs = utility.UserActivity();
            if (fm.GETUSERLEVEL(datasource, userlevel.getLevelid()).isSuccess()) {
                UserLevel userlev = utility.ObjectMapper().readValue(fm.GETUSERLEVEL(datasource, userlevel.getLevelid()).getResult(), UserLevel.class);
                oldData = "Level name :" + userlev.getLevname() + " Details :" + userlev.getLevdetails();
            } else {
                oldData = fm.GETUSERLEVEL(datasource, userlevel.getLevelid()).getMessage();
            }
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEUSERLEVEL(:Message,:Code,:p_levelid,"
                    + ":p_levdetails)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_levelid", userlevel.getLevelid());
            getinsertresult.setString("p_levdetails", userlevel.getLevdetails().toUpperCase());
            getinsertresult.setString("p_levname", userlevel.getLevname().toUpperCase());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                userlogs.setActstatus("SUCCESS");
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                userlogs.setActstatus("FAILED");
                result.setMessage(getinsertresult.getString("Message"));
            }
            userlogs.setActby(userlevel.getCreatedby());
            userlogs.setActdetails(" Data before :" + oldData + " Data after Name: " + userlevel.getLevname() + " Details: " + userlevel.getLevdetails() + " | " + getinsertresult.getString("Message"));
            logs.UserLogsMethod(datasource, "EDIT-USER-LEVEL", userlogs, "0", "0");
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
//    public ACRGBWSResult FACILITYTAGGING(final DataSource datasource, HealthCareFacility hcf) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try (Connection connection = datasource.getConnection()) {
//            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKG.FACILITYTAGGING(:Message,:Code,:p_hcfidcode,"
//                    + ":p_type)");
//            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
//            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
//            getinsertresult.setString("p_hcfidcode", hcf.getHcfcode());//GET HOSPITAL CODE
//            getinsertresult.setString("p_type", hcf.getType()); //APEX OR HCPN CONTROL NUMBER
//            getinsertresult.execute();
//            if (getinsertresult.getString("Message").equals("SUCC")) {
//                result.setSuccess(true);
//                result.setMessage(getinsertresult.getString("Message"));
//            } else {
//                result.setMessage(getinsertresult.getString("Message"));
//            }
//        } catch (SQLException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
/*    public ACRGBWSResult TAGGINGCONTRACTHCPN(final DataSource datasource, Contract contract) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        UpdateMethods um = new UpdateMethods();
        UserActivityLogs logs = new UserActivityLogs();
        try (Connection connection = datasource.getConnection()) {
            //-------------------------------------------------------------------------
            UserActivity userlogs = utility.UserActivity();
            String conDatePerio = "";
            String contractHolder = "";
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.CONTRACTTAGGING(:Message,:Code,"
                    + ":pconid,:pstats,:pendate,:premarks)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("pconid", contract.getConid());//CONID
            getinsertresult.setString("pstats", contract.getStats()); //STATS
            getinsertresult.setDate("pendate", (Date) new Date(utility.StringToDate(contract.getEnddate()).getTime()));//END DATE
            getinsertresult.setString("premarks", contract.getRemarks());//REMARKS
            getinsertresult.execute();
            ArrayList<String> hcicontractaggingError = new ArrayList<>();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                //---------------------------------------------------------
                ACRGBWSResult rest = this.CONSTATSUPDATE(datasource,
                        Integer.parseInt(contract.getConid()),
                        Integer.parseInt(contract.getStats()),
                        contract.getRemarks(),
                        contract.getEnddate());
                if (rest.isSuccess()) {
                    ACRGBWSResult getCon = fm.GETCONTRACTCONID(datasource, contract.getConid(), "INACTIVE");
                    if (getCon.isSuccess()) {
                        Contract updatecontract = utility.ObjectMapper().readValue(getCon.getResult(), Contract.class);
                        contractHolder = updatecontract.getHcfid();
                        if (updatecontract.getContractdate() != null) {
                            ACRGBWSResult restA = methods.GETROLEMULITPLE(datasource, updatecontract.getHcfid(), "ACTIVE");
                            if (restA.isSuccess()) {
                                //--------------------------------------------------
                                List<String> hciList = Arrays.asList(restA.getResult().split(","));
                                for (int x = 0; x < hciList.size(); x++) {
                                    ACRGBWSResult getConA = fm.GETCONTRACTCONID(datasource, hciList.get(x), "ACTIVE");
                                    if (getConA.isSuccess()) {
                                        Contract updatecontracts = utility.ObjectMapper().readValue(getCon.getResult(), Contract.class);
                                        //-------------------------------------------
                                        ACRGBWSResult tagHCIunder = this.CONSTATSUPDATE(datasource,
                                                Integer.parseInt(updatecontracts.getConid()),
                                                Integer.parseInt(contract.getStats()),
                                                contract.getRemarks(),
                                                contract.getEnddate());
                                        //---------------------------------------------
                                        if (!tagHCIunder.isSuccess()) {
                                            hcicontractaggingError.add(tagHCIunder.getMessage());

                                        }
                                    }

                                }
                            }
                            ContractDate conDate = utility.ObjectMapper().readValue(updatecontract.getContractdate(), ContractDate.class);
                            conDatePerio = conDate.getDatefrom() + " | " + conDate.getDateto();
                            um.UPDATEROLEINDEX(datasource, updatecontract.getHcfid(), conDate.getCondateid(), "CONTAGGING");
                        }
                        result.setSuccess(true);
                        result.setMessage(rest.getMessage());
                        result.setResult("Error For HCI Contract tagging:" + hcicontractaggingError.toString());

                    }
                } else {
                    result.setMessage(rest.getMessage());
                }
                userlogs.setActstatus("SUCCESS");
            } else {
                userlogs.setActstatus("FAILED");
                result.setMessage(getinsertresult.getString("Message"));
            }
            switch (contract.getStats()) {
                case "3": {
                    //END CONTRACT
                    userlogs.setActdetails("NONRENEW with date covered : " + conDatePerio + " |" + getinsertresult.getString("Message"));
                    break;
                }
                case "4": {
                    //RENEW
                    userlogs.setActdetails("RENEW with date covered : " + conDatePerio + " |" + getinsertresult.getString("Message"));
                    break;
                }
                case "5": {
                    userlogs.setActdetails("TERMINATE with date covered : " + conDatePerio + " |" + getinsertresult.getString("Message"));
                    break;
                }
            }

            userlogs.setActby(contract.getCreatedby());
            logs.UserLogsMethod(datasource, "TAGGING-CONTRACT", userlogs, contractHolder, "0");
        } catch (SQLException | ParseException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
     */
    // REMOVED ACCESS LEVEL USING ROLE INDEX
    public ACRGBWSResult RemoveAppellate(final DataSource datasource, final String userid, final String accessid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            ArrayList<String> errorList = new ArrayList<>();
            List<String> accesslist = Arrays.asList(accessid.split(","));
            for (int x = 0; x < accesslist.size(); x++) {
                //------------------------------------------------------------------------------------------------
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.REMOVEAPPELLATE(:Message,:Code,"
                        + ":userid,:accessid)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("userid", userid.trim());
                getinsertresult.setString("accessid", accesslist.get(x).trim());
                getinsertresult.execute();
                //------------------------------------------------------------------------------------------------
                if (!getinsertresult.getString("Message").equals("SUCC")) {
                    errorList.add(getinsertresult.getString("Message"));
                }
            }
            if (errorList.isEmpty()) {
                result.setSuccess(true);
                result.setMessage("OK");
            } else {
                result.setMessage(errorList.toString());
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult CONSTATSUPDATE(final DataSource datasource, final String uconid, final String ustats, final String uremarks, final String uenddate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.CONSTATSUPDATE(:Message,:Code,"
                    + ":uconid,:ustats,:uremarks,:uenddate)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("uconid", uconid.trim());//CONID
            getinsertresult.setString("ustats", ustats.trim()); //STATS
            getinsertresult.setString("uremarks", uremarks); //REMAKRS
            getinsertresult.setDate("uenddate", (Date) new Date(utility.StringToDate(uenddate).getTime()));//END DATE
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult UPDATEHCPN(final DataSource datasource, final ManagingBoard mb) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        Methods methods = new Methods();
        try (Connection connection = datasource.getConnection()) {
            ACRGBWSResult getOldMBData = methods.GETMBUSINGMBID(datasource, mb.getMbid());
            if (getOldMBData.isSuccess()) {
                ManagingBoard mbOld = utility.ObjectMapper().readValue(getOldMBData.getResult(), ManagingBoard.class);
                String oldData = mbOld.getAddress() + "|" + mbOld.getMbname() + "|" + mbOld.getBankaccount() + "|" + mbOld.getBankname() + "|" + mbOld.getControlnumber();
                UserActivity userlogs = utility.UserActivity();
                String logsTags = "EDIT-HCPN";
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEHCPN(:Message,:Code,"
                        + ":umbname,:ucontrolnum,:uaddress,:ubankaccount,:ubankname,:umbid)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("umbname", mb.getMbname().toUpperCase());
                getinsertresult.setString("ucontrolnum", mb.getControlnumber());
                getinsertresult.setString("uaddress", mb.getAddress());
                getinsertresult.setString("ubankaccount", mb.getBankaccount());
                getinsertresult.setString("ubankname", mb.getBankname());
                getinsertresult.setString("umbid", mb.getMbid());
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(true);
                    userlogs.setActstatus("SUCCESS");
                } else {
                    userlogs.setActstatus("FAILED");
                    result.setMessage(getinsertresult.getString("Message"));
                }
                userlogs.setActdetails("NEW DATA BANK:" + mb.getBankname() + " ACCOUNT:" + mb.getBankaccount() + " NAME:" + mb.getMbname() + " ADDRESS:" + mb.getAddress() + " CONTROL NUMBER:" + mb.getControlnumber() + " - " + getinsertresult.getString("Message"));
                userlogs.setActby(mb.getCreatedby());
                logs.UserLogsMethod(datasource, logsTags, userlogs, mb.getMbid(), oldData);
            } else {
                result.setMessage(getOldMBData.getMessage());
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult UPDATEHCPNSTATS(final DataSource datasource, final LogStatus logstats) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEHCPNSTATS(:Message,:Code,"
                    + ":uaccount,:ustats)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("uaccount", logstats.getAccount());
            getinsertresult.setString("ustats", "3");
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                ACRGBWSResult insertLogsStatus = im.INSERTSTATSLOG(datasource, logstats);
                if (insertLogsStatus.isSuccess()) {
                    result.setMessage(insertLogsStatus.getMessage());
                    result.setSuccess(true);
                } else {
                    result.setMessage(insertLogsStatus.getMessage());
                }
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult APPROVEDHCPN(final DataSource datasource, final ManagingBoard mb) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.APPROVEDMB(:Message,:Code,"
                    + ":uremarks,:ustatus,:ucontrolnumber)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("uremarks", mb.getRemarks());
            getinsertresult.setString("ustatus", mb.getStatus());
            getinsertresult.setString("ucontrolnumber", mb.getControlnumber());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                if (!mb.getStatus().equals("2")) {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(true);
                } else {
                    //MAPPING OF ACCREDITATION DATA
                    Accreditation acree = new Accreditation();
                    acree.setAccreno(mb.getControlnumber());
                    acree.setCreatedby(mb.getCreatedby());
                    acree.setDatecreated(mb.getDatecreated());
                    acree.setDatefrom(mb.getLicensedatefrom());
                    acree.setDateto(mb.getLicensedateto());
                    ACRGBWSResult accreResult = im.INSERTACCREDITAION(datasource, acree);
                    //MAPPING LOGSTATUS VALUE
                    LogStatus logstats = new LogStatus();
                    logstats.setAccount(mb.getControlnumber());
                    logstats.setActby(mb.getCreatedby());
                    logstats.setDatechange(mb.getDatecreated());
                    logstats.setStatus("2");
                    ACRGBWSResult logsResult = im.INSERTSTATSLOG(datasource, logstats);
                    if (logsResult.isSuccess() && accreResult.isSuccess()) {
                        result.setMessage(logsResult.getMessage() + " , " + accreResult.getMessage());
                        result.setSuccess(true);
                    } else {
                        result.setMessage(logsResult.getMessage() + " , " + accreResult.getMessage());
                    }
                }
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult UPDATECONDATE(final DataSource datasource, final ContractDate contractdate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATECONDATE(:Message,:Code,"
                    + ":ucondateid,:udatefrom,:udateto)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("ucondateid", contractdate.getCondateid());
            getinsertresult.setDate("udatefrom", (Date) new Date(utility.StringToDate(contractdate.getDatefrom()).getTime()));
            getinsertresult.setDate("udateto", (Date) new Date(utility.StringToDate(contractdate.getDateto()).getTime()));
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(true);
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult UPDATEROLEINDEX(final DataSource datasource,
            final String uuserid,
            final String uaccessid,
            final String ucondate,
            final String tagsss) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            //====================================================================
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEROLEINDEX(:Message,:Code,"
                    + ":utags,:uuserid,:uaccessid,:ucondate)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            switch (tagsss.trim().toUpperCase()) {
                case "UPDATE": {
                    getinsertresult.setString("utags", "UPDATE".trim());
                    break;
                }
                case "HCIUPDATE": {
                    getinsertresult.setString("utags", "HCIUPDATE".trim());
                    break;
                }
                default: {
                    getinsertresult.setString("utags", "NONUPDATE".trim());
                    break;
                }
            }
            getinsertresult.setString("uuserid", uuserid.trim());
            getinsertresult.setString("uaccessid", uaccessid.trim());
            getinsertresult.setString("ucondate", ucondate.trim());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(true);
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------
    public ACRGBWSResult UPDATEASSETSTATUS(final DataSource datasource, final String uconid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEASSETSTATUS(:Message,:Code,"
                    + ":uconid)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("uconid", uconid);
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(true);
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------
    public ACRGBWSResult UPDATEPASSCODE(final DataSource datasource, final String pusername, final String ppasscode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Cryptor cryptor = new Cryptor();
        try (Connection connection = datasource.getConnection()) {
            String encryptpword = cryptor.encrypt(ppasscode, ppasscode, "ACRGB");
            CallableStatement statement = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEPASSCODES(:Message,:Code,:pusername,:ppasscode)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            statement.setString("pusername", pusername.trim());
            statement.setString("ppasscode", encryptpword.trim());
            statement.execute();
            if (statement.getString("Message").equals("SUCC")) {
                result.setMessage(statement.getString("Message"));
                result.setSuccess(true);
            } else {
                result.setMessage(statement.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------
    public ACRGBWSResult UPDATECONBALANCESTATS(final DataSource datasource, final String pconid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATECONBALANCESTATS(:Message,:Code,:pconid)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            statement.setString("pconid", pconid);
            statement.execute();
            if (statement.getString("Message").equals("SUCC")) {
                result.setMessage(statement.getString("Message"));
                result.setSuccess(true);
            } else {
                result.setMessage(statement.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------
    //INSERT CONTRACT DATE ID TO APPELLATE TABLE
    public ACRGBWSResult UPDATEAPELLATE(
            final DataSource datasource,
            final String tags,
            final Appellate appellate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivity userLogs = utility.UserActivity();
        try (Connection connection = datasource.getConnection()) {
            userLogs.setActby(appellate.getCreatedby());
            CallableStatement statement = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEAPELLATE(:Message,:Code,"
                    + ":utags,:utats,:uaccesscode,:ucondateid)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            statement.setString("utags", tags.trim().toUpperCase());
            statement.setString("utats", appellate.getStatus().trim());
            statement.setString("uaccesscode", appellate.getAccesscode().trim());
            statement.setString("ucondateid", appellate.getConid().trim());
            statement.execute();
            if (statement.getString("Message").equals("SUCC")) {
                result.setMessage(statement.getString("Message"));
                result.setSuccess(true);
                userLogs.setActstatus("SUCCESS");
            } else {
                result.setMessage(statement.getString("Message"));
                userLogs.setActstatus("FAILED");
            }

            userLogs.setActdetails("Update Appelliate TO " + appellate.getConid() + " TO " + appellate.getControlcode() + "" + statement.getString("Message"));
            userLogs.setActby(appellate.getId());
            if (statement.getString("Message").equals("SUCC")) {
                userLogs.setActstatus("SUCCESS");
            } else {
                userLogs.setActstatus("FAILED");
            }
            //methods.ActivityLogs(datasource, userLogs);

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    // UPDATE USER INFO USING DID
    public ACRGBWSResult UPDATEUSERINFOBYDID(final DataSource datasource,
            final String uemail,
            final String udid,
            final String createdby) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        UserActivityLogs logs = new UserActivityLogs();
        UserActivity userLogs = utility.UserActivity();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEUSERINFOBYDID(:Message,:Code,"
                    + ":udid,:uemail)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("udid", udid.trim());
            getinsertresult.setString("uemail", uemail.trim());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(true);
                userLogs.setActstatus("SUCCESS");
            } else {
                userLogs.setActstatus("FAILED");
                result.setMessage(getinsertresult.getString("Message"));
            }
            userLogs.setActdetails(uemail + " Change Email " + getinsertresult.getString("Message"));
            //USER LOGS
            userLogs.setActby(createdby);
            logs.UserLogsMethod(datasource, "EDIT-USERINFO-EMAIL", userLogs, udid, "0");
            //USER LOGS
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // UPDATE USER FOR 2FA CREDENTIALS
//    public ACRGBWSResult UPDATEUSERFOR2FA(final DataSource datasource,
//            final ForgetPassword forgetPassword,
//            final String email,
//            final String userid) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        EmailSender mets = new EmailSender();
//        java.util.Date d1 = new java.util.Date();
//        try (Connection connection = datasource.getConnection()) {
//            String code2fa = utility.Create2FACode().trim();
//            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEUSERFOR2FA(:Message,:Code,"
//                    + ":puserid,:p2facode,:p2faexpirydate)");
//            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
//            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
//            getinsertresult.setString("puserid", userid.trim());
//            getinsertresult.setString("p2facode", code2fa);
//            getinsertresult.setTimestamp("p2faexpirydate", new java.sql.Timestamp(d1.getTime()));
//            getinsertresult.execute();
//            if (getinsertresult.getString("Message").equals("SUCC")) {
//                ACRGBWSResult facodeSender = mets.FA2CodeSender(datasource, email, code2fa, forgetPassword);
//                if (facodeSender.isSuccess()) {
//                    result.setSuccess(true);
//                } else {
//                    result.setMessage(getinsertresult.getString("Message"));
//                }
//            } else {
//                result.setMessage(getinsertresult.getString("Message"));
//            }
//            UserActivity userLogs = utility.UserActivity();
//            userLogs.setActdetails("Update 2FA Code from user with ID " + userid + " |" + getinsertresult.getString("Message"));
//            userLogs.setActby(forgetPassword.getCreatedby());
//            if (getinsertresult.getString("Message").equals("SUCC")) {
//                userLogs.setActstatus("SUCCESS");
//            } else {
//                userLogs.setActstatus("FAILED");
//            }
//            // methods.ActivityLogs(datasource, userLogs);
//        } catch (SQLException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    // UPDATE USER FOR 2FA CREDENTIALS
    public ACRGBWSResult UPDATEUSEROLE(final DataSource datasource,
            final String createdby,
            final String datecreated,
            final String plevelid,
            final String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEUSEROLE(:Message,:Code,"
                    + ":puserid,:plevelid)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("puserid", puserid.trim());
            getinsertresult.setString("plevelid", plevelid.trim());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                //UPDATE ROLE INDEX IF PRO OR HCI OR HCPN
                ACRGBWSResult updateAccountControl = this.UPDATEACCOUNTCONTROL(datasource, createdby, plevelid, puserid);
                result.setSuccess(true);
                result.setMessage("OK " + updateAccountControl.getMessage());
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
            UserActivity userLogs = utility.UserActivity();
            userLogs.setActdate(datecreated);
            userLogs.setActdetails("Change Account role of user with ID " + puserid + " |" + getinsertresult.getString("Message"));
            userLogs.setActby(createdby);
            if (getinsertresult.getString("Message").equals("SUCC")) {
                userLogs.setActstatus("SUCCESS");
            } else {
                userLogs.setActstatus("FAILED");
            }
            /// methods.ActivityLogs(datasource, userLogs);

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
//    public ACRGBWSResult DELETEDATA(final DataSource datasource,
//            final String tags,
//            final String dataid,
//            final String createdby) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try (Connection connection = datasource.getConnection()) {
//            CallableStatement statement = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.DELETEDATA(:Message,:Code,"
//                    + ":p_tags,:p_dataid)");
//            if (tags.toUpperCase().trim().equals("USER")) {
//                if (fm.GETUSERBYUSERID(datasource, dataid).isSuccess()) {
//                    User user = utility.ObjectMapper().readValue(fm.GETUSERBYUSERID(datasource, dataid,"").getResult(), User.class);
//                    statement.registerOutParameter("Message", OracleTypes.VARCHAR);
//                    statement.registerOutParameter("Code", OracleTypes.INTEGER);
//                    statement.setString("p_tags", "USERDETAILS".trim().toUpperCase());
//                    statement.setInt("p_dataid", Integer.parseInt(user.getDid()));
//                    statement.execute();
//                    if (statement.getString("Message").equals("SUCC")) {
//                        result.setSuccess(true);
//                        result.setMessage(statement.getString("Message"));
//                    } else {
//                        result.setMessage(statement.getString("Message"));
//                    }
//                }
//            }
//            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
//            statement.registerOutParameter("Code", OracleTypes.INTEGER);
//            statement.setString("p_tags", tags);
//            statement.setInt("p_dataid", Integer.parseInt(dataid));
//            statement.execute();
//            if (statement.getString("Message").equals("SUCC")) {
////                    UserActivity userlogs = utility.UserActivity();  
////                    String actdetails = "UPDATE STATUS TO ACTIVE" + tags + " TO 3 Data ID" + dataid;
////                    userlogs.setActby(createdby);
////                    userlogs.setActdate(datecreated);
////                    userlogs.setActdetails(actdetails);
////                    ACRGBWSResult insertActivitylogs = methods.ActivityLogs(datasource, userlogs);
//                result.setSuccess(true);
//                result.setMessage(statement.getString("Message"));
//            } else {
//                result.setMessage(statement.getString("Message"));
//            }
//
//            //==============ACTIVLITY LOGS AREA ===========================
////                UserActivity userlogs = utility.UserActivity();
////                String actdetails = "Add new  HCPN "
////                        + "" + mb.getMbname().toUpperCase() + " Address"
////                        + "" + mb.getAddress() + " Control Number " + mb.getControlnumber();
////                userlogs.setActby(mb.getCreatedby());
////                if (getinsertresult.getString("Message").equals("SUCC")) {
////                    userlogs.setActstatus("SUCCESS");
////                } else {
////                    userlogs.setActstatus("FAILED");
////                }
////                userlogs.setActdetails(actdetails);
////                methods.ActivityLogs(datasource, userlogs);
//            //==============ACTIVLITY LOGS AREA ===========================
//        } catch (SQLException | IOException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }

    // UPDATE USER FOR 2FA CREDENTIALS
    public ACRGBWSResult UPDATEMAPEDCONTAGGING(final DataSource datasource,
            final String createdby,
            final String datecreated,
            final String plevelid,
            final String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEUSERLEVEL(:Message,:Code,"
                    + ":puserid,:plevelid)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("puserid", puserid.trim());
            getinsertresult.setString("plevelid", plevelid.trim());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage("OK");
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
            UserActivity userLogs = utility.UserActivity();
            userLogs.setActdate(datecreated);
            userLogs.setActdetails("Change Account role of user with ID " + puserid + " |" + getinsertresult.getString("Message"));
            userLogs.setActby(createdby);
            if (getinsertresult.getString("Message").equals("SUCC")) {
                userLogs.setActstatus("SUCCESS");
            } else {
                userLogs.setActstatus("FAILED");
            }
            /// methods.ActivityLogs(datasource, userLogs);

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
//    public ACRGBWSResult UPDATEMAPPEDROLEBASECONDATE(
//            final DataSource datasource,
//            final String accessid,
//            final String pcondate) throws ParseException {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try (Connection connection = datasource.getConnection()) {
//            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEMAPPEDROLEBASECONDATE(:Message,:Code,:accessid,:pcondate)");
//            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
//            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
//            getinsertresult.setString("accessid", accessid);
//            getinsertresult.setString("pcondate", pcondate);
//            getinsertresult.execute();
//            if (getinsertresult.getString("Message").equals("SUCC")) {
//                result.setMessage(getinsertresult.getString("Message"));
//                result.setSuccess(true);
//            } else {
//                result.setMessage(getinsertresult.getString("Message"));
//            }
//        } catch (SQLException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult UPDATEROLEINDEXBYACCESSID(
            final DataSource datasource,
            final String accessid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
//        UserActivityLogs logs = new UserActivityLogs();
//        UserActivity userLogs = utility.UserActivity();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEROLEINDEXBYACCESSID(:Message,:Code,:accessid)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("accessid", accessid);
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(true);
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }

//            userLogs.setActby(createdby);
//            logs.UserLogsMethod(datasource, "EDIT-USERINFO", userLogs, udid, "0");
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//---------------------------------------------------------------------------
    public ACRGBWSResult UPDATEACCOUNTCONTROL(
            final DataSource datasource,
            final String createdby,
            final String plevelid,
            final String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEACCOUNTCONTROL(:Message,:Code,"
                    + ":puserid)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("puserid", puserid.trim());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
//                ACRGBWSResult facodeSender = mets.FA2CodeSender(datasource, email, code2fa, forgetPassword);
//                if (facodeSender.isSuccess()) {
                result.setSuccess(true);
                result.setMessage("OK");
//                } else {
//                    result.setMessage(getinsertresult.getString("Message"));
//                }
                //UPDATE ROLE INDEX IF PRO OR HCI OR HCPN

            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
//            UserActivity userLogs = utility.UserActivity();
//            userLogs.setActdate(datecreated);
//            userLogs.setActdetails("Change Account role of user with ID " + puserid + " |" + getinsertresult.getString("Message"));
//            userLogs.setActby(createdby);
//            if (getinsertresult.getString("Message").equals("SUCC")) {
//                userLogs.setActstatus("SUCCESS");
//            } else {
//                userLogs.setActstatus("FAILED");
//            }
            /// methods.ActivityLogs(datasource, userLogs);

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
