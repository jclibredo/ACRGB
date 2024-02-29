/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Area;
import acrgb.structure.AreaType;
import acrgb.structure.Assets;
import acrgb.structure.Contract;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.NclaimsData;
import acrgb.structure.Pro;
import acrgb.structure.Tranch;
import acrgb.structure.User;
import acrgb.structure.UserInfo;
import acrgb.structure.UserLevel;
import acrgb.utility.Cryptor;
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
public class InsertMethods {

    public InsertMethods() {
    }

    private final Utility utility = new Utility();
    private final Cryptor cryptor = new Cryptor();
    private final Methods methods = new Methods();
    private final FetchMethods fm = new FetchMethods();

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTAREA(final DataSource datasource, final Area area) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            ACRGBWSResult arearesult = fm.ACR_AREA(datasource, "active");
            if (!arearesult.isSuccess()) {
                result.setMessage(arearesult.getMessage());
                result.setSuccess(false);
            } else if (area.getAreaname().isEmpty() || area.getTypeid().isEmpty() || area.getCreatedby().isEmpty() || area.getDatecreated().isEmpty()) {
                result.setMessage("SOME REQUIRED FIELDS IS EMPTY");
                result.setSuccess(false);
            } else if (!utility.IsValidNumber(area.getCreatedby()) || !utility.IsValidNumber(area.getTypeid())) {
                result.setMessage("NUMBER FORMAT IS NOT VALID");
                result.setSuccess(false);
            } else if (!utility.IsValidDate(area.getDatecreated())) {
                result.setMessage("DATE FORMAT IS NOT VALID");
                result.setSuccess(false);
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTAREA(:Message,:Code,:p_areaname,:p_typeid,:p_createdby,:p_datecreated)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("p_areaname", area.getAreaname().toUpperCase());
                getinsertresult.setString("p_typeid", area.getTypeid());
                getinsertresult.setString("p_createdby", area.getCreatedby());
                getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(area.getDatecreated()).getTime()));//area.getDatecreated());
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setSuccess(true);
                    result.setMessage("OK");
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(false);
                }
                result.setResult(utility.ObjectMapper().writeValueAsString(area));
            }
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTAREATYPE(final DataSource datasource, final AreaType areatype) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidDate(areatype.getDatecreated())) {
                result.setSuccess(false);
                result.setMessage("DATE FORMAT IS NOT VALID");
            } else if (areatype.getTypename().isEmpty() || areatype.getCreatedby().isEmpty() || areatype.getDatecreated().isEmpty()) {
                result.setMessage("SOME REQUIRED FIELDS IS EMPTY");
                result.setSuccess(false);
            } else if (!utility.IsValidNumber(areatype.getCreatedby())) {
                result.setMessage("NUMBER FORMAT IS NOT VALID");
                result.setSuccess(false);
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTAREATYPE(:Message,:Code,:p_typename,"
                        + ":p_createdby,:p_datecreated)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("p_typename", areatype.getTypename().toUpperCase());
                getinsertresult.setString("p_createdby", areatype.getCreatedby());
                getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(areatype.getDatecreated()).getTime())); //areatype.getDatecreated());
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setSuccess(true);
                    result.setMessage("OK");
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(false);
                }
                result.setResult(utility.ObjectMapper().writeValueAsString(areatype));
            }
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

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
                ACRGBWSResult hcfresult = fm.ACR_HCF(datasource, "active");
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
                } else if (!utility.IsValidDate(assets.getDatecreated()) || !utility.IsValidDate(assets.getDatereleased())) {
                    result.setSuccess(false);
                    result.setMessage("DATE FORMAT IS NOT VALID");
                } else {
                    ACRGBWSResult conresult = fm.GETCONTRACTAMOUNT(datasource, assets.getHcfid());
                    if (conresult.isSuccess()) {
                        ACRGBWSResult transresult = fm.ACR_ASSETS(datasource, "active");
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
                                        CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTASSETS(:Message,:Code,:p_hcfid,:p_tranchid ,:p_receipt,:p_amount"
                                                + ",:p_createdby,:p_datereleased,:p_datecreated)");
                                        getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                                        getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                                        getinsertresult.setString("p_hcfid", assets.getHcfid());
                                        getinsertresult.setString("p_tranchid", assets.getTranchid());
                                        getinsertresult.setString("p_receipt", assets.getReceipt());
                                        getinsertresult.setString("p_amount", String.valueOf(p_amount));
                                        getinsertresult.setString("p_createdby", assets.getCreatedby());
                                        getinsertresult.setDate("p_datereleased", (Date) new Date(utility.StringToDate(assets.getDatecreated()).getTime())); //assets.getDatereleased());
                                        getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(assets.getDatecreated()).getTime()));//assets.getDatecreated());
                                        getinsertresult.execute();
                                        if (getinsertresult.getString("Message").equals("SUCC")) {
                                            result.setSuccess(true);
                                            result.setMessage("OK");
                                        } else {
                                            result.setMessage(getinsertresult.getString("Message"));
                                            result.setSuccess(false);
                                        }
                                        result.setResult(utility.ObjectMapper().writeValueAsString(assets));
                                    } else {
                                        result.setSuccess(false);
                                        result.setMessage("TRANCH VALUE IS ALREADY ASSIGN TO HCF");
                                    }
                                }
                            } else {
                                result.setSuccess(false);
                                result.setMessage(trans.getMessage());
                            }
                        } else {
                            result.setSuccess(false);
                            result.setMessage(transresult.getMessage());
                        }
                    } else {
                        result.setMessage(conresult.getMessage());
                        result.setSuccess(false);
                    }
                }
            } else {
                result.setSuccess(false);
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
                ACRGBWSResult hcfresult = fm.ACR_HCF(datasource, "active");
                int countresult = 0;
                if (hcfresult.isSuccess()) {
                    if (!hcfresult.getResult().isEmpty()) {
                        List<HealthCareFacility> hcflist = Arrays.asList(utility.ObjectMapper().readValue(hcfresult.getResult(), HealthCareFacility[].class));
                        for (int x = 0; x < hcflist.size(); x++) {
                            if (hcflist.get(x).getHcfid().equals(contract.getHcfid())) {
                                countresult++;
                            }
                        }
                    }
                }
                if (countresult < 1) {
                    result.setMessage("FACILITY DATA NOT FOUND");
                    result.setSuccess(false);
                } else {
                    if (!utility.IsValidDate(contract.getDatecreated()) || !utility.IsValidDate(contract.getDatefrom()) || !utility.IsValidDate(contract.getDateto())) {
                        result.setSuccess(false);
                        result.setMessage("DATE FORMAT IS NOT VALID");
                    } else {
                        CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTCONTRACT(:Message,:Code,:p_hcfid,:p_amount"
                                + ",:p_createdby,:p_datecreated,:p_datefrom,:p_dateto)");
                        getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                        getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                        getinsertresult.setString("p_hcfid", contract.getHcfid());
                        getinsertresult.setString("p_amount", contract.getAmount());
                        getinsertresult.setString("p_createdby", contract.getCreatedby());
                        getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(contract.getDatecreated()).getTime()));//contract.getDatecreated());
                        getinsertresult.setDate("p_datefrom", (Date) new Date(utility.StringToDate(contract.getDatefrom()).getTime()));
                        getinsertresult.setDate("p_dateto", (Date) new Date(utility.StringToDate(contract.getDateto()).getTime()));
                        getinsertresult.execute();
                        if (getinsertresult.getString("Message").equals("SUCC")) {
                            result.setSuccess(true);
                            result.setMessage("OK");
                        } else {
                            result.setMessage(getinsertresult.getString("Message"));
                            result.setSuccess(false);
                        }
                        result.setResult(utility.ObjectMapper().writeValueAsString(contract));
                    }
                }
            }
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTHCF(final DataSource datasource, final HealthCareFacility hcf) throws SQLException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {

            ACRGBWSResult hcfresult = fm.ACR_HCF(datasource, "active");
            int counthcf = 0;
            if (hcfresult.isSuccess()) {
                if (!hcfresult.getResult().isEmpty()) {
                    List<HealthCareFacility> hcflist = Arrays.asList(utility.ObjectMapper().readValue(hcfresult.getResult(), HealthCareFacility[].class));
                    for (int x = 0; x < hcflist.size(); x++) {
                        if (hcflist.get(x).getHcfname().toUpperCase().equals(hcf.getHcfname().toUpperCase())) {
                            counthcf++;
                        }
                    }
                }
            }

            ACRGBWSResult arearesult = fm.ACR_AREA(datasource, "active");
            int countresult = 0;
            if (arearesult.isSuccess()) {
                if (!arearesult.getResult().isEmpty()) {
                    List<Area> arealist = Arrays.asList(utility.ObjectMapper().readValue(arearesult.getResult(), Area[].class));
                    for (int x = 0; x < arealist.size(); x++) {
                        if (arealist.get(x).getAreaid().equals(hcf.getAreaid())) {
                            countresult++;
                        }
                    }
                }
            }

            if (!utility.IsValidDate(hcf.getDatecreated())) {
                result.setSuccess(false);
                result.setMessage("DATE FORMAT IS NOT VALID");
            } else if (countresult == 0) {
                result.setSuccess(false);
                result.setMessage("AREA ID NOT FOUND");
            } else if (hcf.getHcfname().isEmpty() || hcf.getHcfaddress().isEmpty() || hcf.getHcfcode().isEmpty() || hcf.getDatecreated().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("SOME REQUIRED FIELD IS EMPTY");
            } else if (counthcf > 0) {
                result.setSuccess(false);
                result.setMessage("HCF NAME IS ALREADY EXIST");
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTHCF(:Message,:Code,:p_hcfname,:p_hcfaddress,:p_hcfcode,:p_createdby,:"
                        + "p_areaid,:p_datecreated,:p_proid)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("p_hcfname", hcf.getHcfname().toUpperCase());
                getinsertresult.setString("p_hcfaddress", hcf.getHcfaddress().toUpperCase());
                getinsertresult.setString("p_hcfcode", hcf.getHcfcode());
                getinsertresult.setString("p_createdby", hcf.getCreatedby());
                getinsertresult.setString("p_areaid", hcf.getAreaid());
                getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(hcf.getDatecreated()).getTime())); //hcf.getDatecreated());
                getinsertresult.setString("p_proid", hcf.getProid());
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setSuccess(true);
                    result.setMessage("OK");
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(false);
                }
                result.setResult(utility.ObjectMapper().writeValueAsString(hcf));
            }

        } catch (SQLException | IOException | ParseException ex) {
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
                    result.setMessage("OK");
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(false);
                }
                result.setResult(utility.ObjectMapper().writeValueAsString(tranch));
            }
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    public ACRGBWSResult INSERTPRO(final DataSource datasource, Pro pro) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            if (!utility.IsValidDate(pro.getDatecreated())) {
                result.setSuccess(false);
                result.setMessage("DATE FORMAT IS NOT VALID");
            } else {
                CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTPRO(:Message,:Code,:p_proname,:p_createdby,:p_datecreated)");
                getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                getinsertresult.setString("p_proname", pro.getProname().toUpperCase());
                getinsertresult.setString("p_createdby", pro.getCreatedby());
                getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(pro.getDatecreated()).getTime()));//tranch.getDatecreated());
                getinsertresult.execute();
                if (getinsertresult.getString("Message").equals("SUCC")) {
                    result.setSuccess(true);
                    result.setMessage("OK");
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(false);
                }
                result.setResult(utility.ObjectMapper().writeValueAsString(pro));
            }
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------------------
    //INSERT USER DETAILS
    public ACRGBWSResult INSERTUSERDETAILS(final DataSource datasource, UserInfo userinfo) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {

            if (userinfo.getFirstname() != null || userinfo.getLastname() != null || userinfo.getAreaid() != null) {
                ACRGBWSResult realist = fm.ACR_AREA(datasource, "active");
                int countresult = 0;
                ACRGBWSResult hcflist = fm.ACR_HCF(datasource, "active");
                int hcfresult = 0;

                if (hcflist.isSuccess()) {
                    if (!hcflist.getResult().isEmpty()) {
                        List<HealthCareFacility> hcflistresult = Arrays.asList(utility.ObjectMapper().readValue(hcflist.getResult(), HealthCareFacility[].class));
                        for (int x = 0; x < hcflistresult.size(); x++) {
                            if (hcflistresult.get(x).getHcfid().equals(userinfo.getHcfid())) {
                                hcfresult++;
                            }
                        }
                    }
                }
                if (realist.isSuccess()) {
                    if (!realist.getResult().isEmpty()) {
                        List<Area> arealist = Arrays.asList(utility.ObjectMapper().readValue(realist.getResult(), Area[].class));
                        for (int x = 0; x < arealist.size(); x++) {
                            if (arealist.get(x).getAreaid().equals(userinfo.getAreaid())) {
                                countresult++;
                            }
                        }
                    }
                }
                if (!utility.IsValidDate(userinfo.getDatecreated())) {
                    result.setSuccess(false);
                    result.setMessage("DATE FORMAT IS NOT VALID");
                } else if (countresult == 0) {
                    result.setSuccess(false);
                    result.setMessage("AREA ID NOT FOUND");
                } else {
                    if (userinfo.getHcfid().isEmpty()) {
                        CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTUSERDETAILS(:Message,:Code,"
                                + ":p_firstname,:p_lastname,:p_middlename,:p_datecreated,:p_areaid,:p_createdby,:p_hcfid)");
                        getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                        getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                        getinsertresult.setString("p_firstname", userinfo.getFirstname().toUpperCase());
                        getinsertresult.setString("p_lastname", userinfo.getLastname().toUpperCase());
                        getinsertresult.setString("p_middlename", userinfo.getMiddlename().toUpperCase());
                        getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(userinfo.getDatecreated()).getTime()));//userinfo.getDatecreated());
                        getinsertresult.setString("p_areaid", userinfo.getAreaid());
                        getinsertresult.setString("p_createdby", userinfo.getCreatedby());
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
                    } else {
                        if (hcfresult == 0) {
                            result.setSuccess(false);
                            result.setMessage("HCF ID IS NOT VALID");
                        } else {
                            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.ACRGBPKGPROCEDURE.INSERTUSERDETAILS(:Message,:Code,"
                                    + ":p_firstname,:p_lastname,:p_middlename,:p_datecreated,:p_areaid,:p_createdby,:p_hcfid)");
                            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
                            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
                            getinsertresult.setString("p_firstname", userinfo.getFirstname().toUpperCase());
                            getinsertresult.setString("p_lastname", userinfo.getLastname().toUpperCase());
                            getinsertresult.setString("p_middlename", userinfo.getMiddlename().toUpperCase());
                            getinsertresult.setDate("p_datecreated", (Date) new Date(utility.StringToDate(userinfo.getDatecreated()).getTime()));//userinfo.getDatecreated());
                            getinsertresult.setString("p_areaid", userinfo.getAreaid());
                            getinsertresult.setString("p_createdby", userinfo.getCreatedby());
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
                        }
                    }
                }
            }

        } catch (SQLException | IOException | ParseException ex) {
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
                    result.setMessage("OK");
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(false);
                }
                result.setResult(utility.ObjectMapper().writeValueAsString(userlevel));
            }
        } catch (SQLException | IOException | ParseException ex) {
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
                        getinsertresult.setString("p_username", user.getUsername());
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
                            result.setMessage("OK");
                        } else {
                            result.setMessage(getinsertresult.getString("Message"));
                            result.setSuccess(false);
                        }
                        result.setResult(utility.ObjectMapper().writeValueAsString(user));
                    } else {
                        result.setSuccess(false);
                        result.setMessage(validateRole.getMessage());
                        result.setResult(validateRole.getResult());
                    }
                } else {
                    result.setSuccess(validateUsername.isSuccess());
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
    public ACRGBWSResult INSERTNCLAIMS(final DataSource datasource, NclaimsData nclaimsdata) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call ACR_GB.INSERTNCLAIMS(:Message,:Code,"
                    + ":p_accreno,:p_datesubmitted,:p_amount,:p_series,:p_claimid,:p_tags)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("p_accreno", nclaimsdata.getAccreno());
            getinsertresult.setDate("p_datesubmitted", (Date) new Date(utility.StringToDate(nclaimsdata.getDatesubmitted()).getTime()));
            getinsertresult.setString("p_amount", nclaimsdata.getClaimamount());
            getinsertresult.setString("p_series", nclaimsdata.getSeries());
            getinsertresult.setString("p_claimid", nclaimsdata.getClaimid());
            getinsertresult.setString("p_tags", nclaimsdata.getTags());
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage("OK");
            } else {
                result.setMessage(getinsertresult.getString("Message"));
                result.setSuccess(false);
            }
            result.setResult(utility.ObjectMapper().writeValueAsString(nclaimsdata));
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

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
                    result.setMessage("SUCCESSFULLY SET DATA AS INACTIVE");
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(false);
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

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
                    result.setSuccess(true);
                    result.setMessage("SUCCESSFULLY SET DATA AS ACTIVE");
                } else {
                    result.setMessage(getinsertresult.getString("Message"));
                    result.setSuccess(false);
                }
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(InsertMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
