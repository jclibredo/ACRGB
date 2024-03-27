/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Assets;
import acrgb.structure.Contract;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.ManagingBoard;
import acrgb.structure.Pro;
import acrgb.structure.Tranch;
import acrgb.structure.UserLevel;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
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
                ACRGBWSResult hcfresult = fm.ACR_HCF(datasource);
                int countresult = 0;
                if (hcfresult.isSuccess()) {
                    if (!hcfresult.getResult().isEmpty()) {
                        List<HealthCareFacility> hcflist = Arrays.asList(utility.ObjectMapper().readValue(hcfresult.getResult(), HealthCareFacility[].class));
                        for (int x = 0; x < hcflist.size(); x++) {
                            if (hcflist.get(x).getHcfid().equals(assets.getHcfid())) {
                                countresult++;
                            }
                        }
                    }
                }

                if (!tranchresult.isSuccess()) {
                    result.setMessage(tranchresult.getMessage());
                    result.setSuccess(false);
                } else if (countresult == 0) {
                    result.setSuccess(false);
                    result.setMessage("HEALTH CARE FACILITY NOT FOUND");
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
                                        result.setMessage("TRANCH VALUE IS ALREADY ASSIGN TO HCF");
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
                    || contract.getDatefrom().isEmpty()
                    || contract.getDateto().isEmpty()
                    || contract.getHcfid().isEmpty()) {
                result.setMessage("SOME REQUIRED FIELDS IS EMPTY");
                result.setSuccess(false);
            } else if (!utility.IsValidNumber(contract.getHcfid()) || !utility.IsValidNumber(contract.getConid())) {
                result.setMessage("NUMBER FORMAT IS NOT VALID");
                result.setSuccess(false);
            } else {
                if (!utility.IsValidDate(contract.getDatefrom()) || !utility.IsValidDate(contract.getDateto())) {
                    result.setSuccess(false);
                    result.setMessage("DATE FORMAT IS NOT VALID");
                } else {
                    CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.UPDATECONTRACT(:Message,:Code,:p_conid,:p_hcfid,:p_amount"
                            + ",:p_datefrom,:p_dateto,:p_transcode)");
                    getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                    getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                    getinsertresult.setString("p_conid", contract.getConid());
                    getinsertresult.setString("p_hcfid", contract.getHcfid());
                    getinsertresult.setString("p_amount", contract.getAmount());
                    getinsertresult.setDate("p_datefrom", (Date) new Date(utility.StringToDate(contract.getDatefrom()).getTime()));
                    getinsertresult.setDate("p_dateto", (Date) new Date(utility.StringToDate(contract.getDateto()).getTime()));
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

            }
        } catch (SQLException | IOException | ParseException ex) {
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
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.FACILITYTAGGING(:Message,:Code,:p_hcfidcode,"
                    + ":p_type,:p_gb)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_hcfidcode", hcf.getHcfcode());//GET HOSPITAL CODE
            getinsertresult.setString("p_type", hcf.getType()); //APEX OR HCPN CONTROL NUMBER
            getinsertresult.setString("p_gb", hcf.getGbtags());//GB OR NONGB
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

}
