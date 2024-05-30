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
import acrgb.structure.ContractDate;
import acrgb.structure.DateSettings;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.LogStatus;
import acrgb.structure.ManagingBoard;
import acrgb.structure.Tranch;
import acrgb.structure.UserLevel;
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
public class UpdateMethods {

    public UpdateMethods() {
    }

    private final Utility utility = new Utility();
    private final FetchMethods fm = new FetchMethods();
    private final InsertMethods im = new InsertMethods();
    private final Methods methods = new Methods();
    private final ContractMethod cm = new ContractMethod();

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult UPDATEASSETS(final DataSource datasource, Assets assets) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            //GET TRANCH PERCENTAGE
            if (assets.getAmount().isEmpty()
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
                    result.setSuccess(false);
                } else if (!utility.IsValidDate(assets.getDatereleased())) {
                    result.setSuccess(false);
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
                                            && assets.getTranchid().trim().equals(assetslist.get(x).getTranchid().trim())) {
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
                                        CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEASSETS(:Message,:Code,:p_assetsid,:p_hcfid,:p_tranchid ,:p_receipt,:p_amount"
                                                + ",:p_datereleased)");
                                        getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                                        getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                                        getinsertresult.setString("p_assetsid", assets.getAssetid());
                                        getinsertresult.setString("p_hcfid", assets.getHcfid());
                                        getinsertresult.setString("p_tranchid", assets.getTranchid());
                                        getinsertresult.setString("p_receipt", assets.getReceipt());
                                        getinsertresult.setString("p_amount", String.valueOf(p_amount));
                                        getinsertresult.setDate("p_datereleased", (Date) new Date(utility.StringToDate(assets.getDatecreated()).getTime())); //assets.getDatereleased());
                                        getinsertresult.execute();
                                        if (getinsertresult.getString("Message").equals("SUCC")) {
                                            result.setSuccess(true);
                                            result.setMessage("OK");
                                        } else {
                                            result.setMessage(getinsertresult.getString("Message"));
                                        }
                                        result.setResult(utility.ObjectMapper().writeValueAsString(assets));
                                    } else {
                                        result.setMessage("TRANCH VALUE IS ALREADY ASSIGN");
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
        try (Connection connection = datasource.getConnection()) {
            if (contract.getAmount().isEmpty()
                    || contract.getHcfid().isEmpty()) {
                result.setMessage("SOME REQUIRED FIELDS IS EMPTY");
                result.setSuccess(false);
            } else if (!utility.IsValidNumber(contract.getHcfid()) || !utility.IsValidNumber(contract.getConid())) {
                result.setMessage("NUMBER FORMAT IS NOT VALID");
                result.setSuccess(false);
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATECONTRACT(:Message,:Code,:p_conid,:p_hcfid,:p_amount"
                        + ",:p_contractdate,:p_transcode)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("p_conid", contract.getConid());
                getinsertresult.setString("p_hcfid", contract.getHcfid());
                getinsertresult.setString("p_amount", contract.getAmount());
                getinsertresult.setString("p_contractdate", contract.getContractdate());
                getinsertresult.setString("p_transcode", contract.getTranscode());
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setSuccess(true);
                    result.setMessage("OK");
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
                result.setResult(utility.ObjectMapper().writeValueAsString(contract));

            }
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
        try (Connection connection = datasource.getConnection()) {
            if (tranch.getTranchtype().isEmpty() || tranch.getPercentage().isEmpty() || tranch.getTranchid().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("SOME REQUIRED FIELD IS EMPTY");
            } else if (!utility.IsValidNumber(tranch.getPercentage())) {
                result.setSuccess(false);
                result.setMessage("PERCENTAGE VALUE IS NOT VALID");
            } else if (!utility.IsValidNumber(tranch.getTranchid())) {
                result.setSuccess(false);
                result.setMessage("INVALID NUMBER FORMAT");
            } else {
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
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
            }
        } catch (SQLException | ParseException ex) {
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
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidNumber(userlevel.getLevelid())) {
                result.setSuccess(false);
                result.setMessage("NUMBER FORMAT IS NOT VALID");
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEUSERLEVEL(:Message,:Code,:p_levelid,"
                        + ":p_levdetails)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("p_levelid", userlevel.getLevelid());
                getinsertresult.setString("p_levdetails", userlevel.getLevdetails().toUpperCase());
                getinsertresult.setString("p_levname", userlevel.getLevname().toUpperCase());
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
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult FACILITYTAGGING(final DataSource datasource, HealthCareFacility hcf) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKG.FACILITYTAGGING(:Message,:Code,:p_hcfidcode,"
                    + ":p_type)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_hcfidcode", hcf.getHcfcode());//GET HOSPITAL CODE
            getinsertresult.setString("p_type", hcf.getType()); //APEX OR HCPN CONTROL NUMBER
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
    public ACRGBWSResult UPDATESETTINGS(final DataSource datasource, DateSettings datesettings) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATESETTINGS(:Message,:Code,"
                    + ":pdatefrom,"
                    + ":pdateto,:ptags)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setDate("pdatefrom", (Date) new Date(utility.StringToDate(datesettings.getDatefrom()).getTime()));
            getinsertresult.setDate("pdatefrom", (Date) new Date(utility.StringToDate(datesettings.getDateto()).getTime()));
            getinsertresult.setString("ptags", datesettings.getTags().toUpperCase());//GET HOSPITAL CODE
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult TAGGINGCONTRACT(final DataSource datasource, Contract contract) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidNumber(contract.getConid())) {
                result.setMessage("CONTRACT ID IS INVALID NUMBER FORMAT");
            } else {
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
                    //---------------------------------------------------------
                    if (rest.isSuccess()) {
                        ACRGBWSResult getCon = fm.GETCONTRACTCONID(datasource, contract.getConid());
                        if (getCon.isSuccess()) {
                            Contract updatecontract = utility.ObjectMapper().readValue(getCon.getResult(), Contract.class);
                            ACRGBWSResult restA = methods.GETROLEMULITPLE(datasource, updatecontract.getHcfid());
                            if (restA.isSuccess()) {
                                //--------------------------------------------------
                                List<String> hciList = Arrays.asList(restA.getResult().split(","));
                                for (int x = 0; x < hciList.size(); x++) {
                                    ACRGBWSResult getConA = cm.GETCONTRACT(datasource, "ACTIVE", hciList.get(x));
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
                                //-----------------------------------------------------
                                result.setSuccess(true);
                                result.setMessage(rest.getMessage());
                                result.setResult("Error For HCI Contract tagging:" + hcicontractaggingError.toString());
                            }
                        }

                    } else {
                        result.setMessage(rest.getMessage());
                    }
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
            }
        } catch (SQLException | ParseException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // REMOVED ACCESS LEVEL USING ROLE INDEX
    public ACRGBWSResult RemoveAppellate(final DataSource datasource, final String accesscode, final String controlcode) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidNumber(accesscode)) {
                result.setMessage("NUMBER FORMAT IS NOT VALID");
            } else {
                ArrayList<String> errorList = new ArrayList<>();
                List<String> accesslist = Arrays.asList(controlcode.split(","));
                int errCount = 0;
                for (int x = 0; x < accesslist.size(); x++) {
                    //------------------------------------------------------------------------------------------------
                    CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.REMOVEAPPELLATE(:Message,:Code,"
                            + ":uaccesscode,:ucontrolcode)");
                    getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                    getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                    getinsertresult.setString("uaccesscode", accesscode);
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
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult CONSTATSUPDATE(final DataSource datasource, final int uconid, final int ustats, final String uremarks, final String uenddate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.CONSTATSUPDATE(:Message,:Code,"
                    + ":uconid,:ustats,:uremarks,:uenddate)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setInt("uconid", uconid);//CONID
            getinsertresult.setInt("ustats", ustats); //STATS
            getinsertresult.setString("uremarks", uremarks); //REMAKRS
            getinsertresult.setDate("uenddate", (Date) new Date(utility.StringToDate(uenddate).getTime()));//END DATE
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage(getinsertresult.getString("Message"));
            }
        } catch (SQLException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult UPDATEHCPN(final DataSource datasource, final ManagingBoard mb) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEHCPN(:Message,:Code,"
                    + ":umbname,:uaccreno,:uaddress,:ubankaccount,:ubankname)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("umbname", mb.getMbname().toUpperCase());
            getinsertresult.setString("uaccreno", mb.getControlnumber());
            getinsertresult.setString("uaddress", mb.getAddress());
            getinsertresult.setString("ubankaccount", mb.getBankaccount());
            getinsertresult.setString("ubankname", mb.getBankname());
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
    public ACRGBWSResult UPDATEHCPNSTATS(final DataSource datasource, final LogStatus logstats) throws ParseException {
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
    public ACRGBWSResult APPROVEDHCPN(final DataSource datasource, final ManagingBoard mb) throws ParseException {
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
    public ACRGBWSResult UPDATECONDATE(final DataSource datasource, final ContractDate contractdate) throws ParseException {
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
    public ACRGBWSResult UPDATEROLEINDEX(final DataSource datasource, final String uuserid, final String ucondate, final String tagsss) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            //====================================================================
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEROLEINDEX(:Message,:Code,"
                    + ":utags,:uuserid,:ucondate)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            //=====================================================================
            if (!tagsss.toUpperCase().equals("ENDCONDATE")) {
                getinsertresult.setString("utags", "UPDATE");
                getinsertresult.setString("uuserid", uuserid);
                getinsertresult.setString("ucondate", ucondate);
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(true);
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
            } else {
                //PROCESS AUTO END CONTRACT
                getinsertresult.setString("utags", "NONUPDATE");
                getinsertresult.setString("uuserid", uuserid);
                getinsertresult.setString("ucondate", ucondate);
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    ACRGBWSResult getConDateByid = cm.GETCONDATEBYID(datasource, ucondate);
                    if (getConDateByid.isSuccess()) {
                        ContractDate contractdate = utility.ObjectMapper().readValue(getConDateByid.getResult(), ContractDate.class);
                        ACRGBWSResult getResult = cm.GETCONTRACTBYCONDATEID(datasource, ucondate);
                        if (getResult.isSuccess()) {
                            List<String> RestA = Arrays.asList(getResult.getResult().split(","));
                            for (int x = 0; x < RestA.size(); x++) {
                                Contract con = new Contract();
                                con.setConid(RestA.get(x));
                                con.setEnddate(contractdate.getDateto());
                                con.setRemarks("CONTRACT ENDED");
                                con.setStats("3");
                                //UPDATE CONTRACT UNDER CONTRACTDATE PERIOD
                                ACRGBWSResult tagContract = this.TAGGINGCONTRACT(datasource, con);
                                //UPDATE ASSETS UNDER CONTRACT
                                ACRGBWSResult upDateAssets = this.UPDATEASSETSTATUS(datasource, RestA.get(x));

                            }
                            result.setMessage(getinsertresult.getString("Message"));
                            result.setSuccess(true);
                        } else {
                            result.setMessage(getResult.getMessage());
                        }
                    } else {
                        result.setMessage(getConDateByid.getMessage());
                    }
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                }
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(UpdateMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------
    public ACRGBWSResult UPDATEASSETSTATUS(final DataSource datasource, final String uconid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEASSETSTATUS(:Message,:Code,"
                    + ":uconid");
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
    public ACRGBWSResult UPDATEPASSCODE(final DataSource datasource, final String pusername, final String ppasscode) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Cryptor cryptor = new Cryptor();
        try (Connection connection = datasource.getConnection()) {
            String encryptpword = cryptor.encrypt(ppasscode, ppasscode, "ACRGB");
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATEPASSCODE(:Message,:Code,"
                    + ":pusername,:ppasscode");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("pusername", pusername);
            getinsertresult.setString("ppasscode", encryptpword);
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

}
