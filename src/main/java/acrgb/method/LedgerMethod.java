/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Assets;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.FacilityComputedAmount;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.Ledger;
import acrgb.structure.Tranch;
import acrgb.structure.UserInfo;
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
 * @author ACR_GB
 */
@RequestScoped
public class LedgerMethod {

    public LedgerMethod() {
    }
    private final Utility utility = new Utility();
    private final SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");
    //private final SimpleDateFormat datetimeformat = utility.SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");

    //GET ASSESTS USING CONTRACT ID
    public ACRGBWSResult GetAssetsUsingConID(final DataSource dataSource, final String conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETASSETSBYCONID(:pconid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pconid", conid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Assets> listassets = new ArrayList<>();
            while (resultset.next()) {
                Assets assets = new Assets();
                assets.setAssetid(resultset.getString("ASSETSID"));
                ACRGBWSResult tranchresult = new FetchMethods().ACR_TRANCHWITHID(dataSource, resultset.getString("TRANCHID"));
                if (tranchresult.isSuccess()) {
                    assets.setTranchid(tranchresult.getResult());
                } else {
                    assets.setTranchid(tranchresult.getMessage());
                }
                ACRGBWSResult facilityresult = new FetchMethods().GETFACILITYID(dataSource, resultset.getString("HCFID"));
                if (facilityresult.isSuccess()) {
                    assets.setHcfid(facilityresult.getResult());
                } else {
                    assets.setHcfid(facilityresult.getMessage());
                }
                assets.setDatereleased(dateformat.format(resultset.getTimestamp("DATERELEASED")));//resultset.getDate("DATERELEASED"));
                assets.setReceipt(resultset.getString("RECEIPT"));
                assets.setAmount(resultset.getString("AMOUNT"));
                ACRGBWSResult creator = new FetchMethods().GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    assets.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    assets.setCreatedby(creator.getMessage());
                }
                assets.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                ACRGBWSResult getcon = new FetchMethods().GETCONTRACTCONID(dataSource, resultset.getString("CONID").trim(), "ACTIVE");
                if (getcon.isSuccess()) {
                    assets.setConid(getcon.getResult());
                } else {
                    assets.setConid(getcon.getMessage());
                }
                assets.setStatus(resultset.getString("STATS"));
                assets.setPreviousbalance(resultset.getString("PREVIOUSBAL"));
                assets.setClaimscount(resultset.getString("CLAIMSCOUNT"));
                assets.setReleasedamount(resultset.getString("RELEASEDAMOUNT"));
                listassets.add(assets);
            }
            if (listassets.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(listassets));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETASSETSBYHCF(
            final DataSource dataSource,
            final String phcfid,
            final String pdatefrom,
            final String pdateto) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETASSETSBYHCF(:phcfid,:pdatefrom,:pdateto); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("phcfid", phcfid.trim());
            statement.setDate("pdatefrom", (Date) new Date(utility.StringToDate(pdatefrom).getTime()));
            statement.setDate("pdateto", (Date) new Date(utility.StringToDate(pdateto).getTime()));
            statement.execute();
            ArrayList<Assets> assetslist = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                Assets assets = new Assets();
                assets.setAssetid(resultset.getString("ASSETSID"));
                assets.setHcfid(resultset.getString("HCFID"));
                assets.setTranchid(resultset.getString("TRANCHID"));
                assets.setReceipt(resultset.getString("RECEIPT"));
                assets.setAmount(resultset.getString("AMOUNT"));
                assets.setPreviousbalance(resultset.getString("PREVIOUSBAL"));
                assets.setClaimscount(resultset.getString("CLAIMSCOUNT"));
                assets.setReleasedamount(resultset.getString("RELEASEDAMOUNT"));
                ACRGBWSResult creator = new FetchMethods().GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                        assets.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        assets.setCreatedby(creator.getMessage());
                    }
                } else {
                    assets.setCreatedby(creator.getMessage());
                }
                assets.setDatereleased(dateformat.format(resultset.getTimestamp("DATERELEASED")));//resultset.getString("DATERELEASED"));
                assets.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                assetslist.add(assets);
            }
            if (!assetslist.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(assetslist));
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETSUMAMOUNTCLAIMS(final DataSource dataSource,
            final String upmccno,
            final String udatefrom,
            final String udateto) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETSUMAMOUNTCLAIMS(:upmccno,:utags,:udatefrom,:udateto); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("upmccno", upmccno.trim());
            statement.setString("utags", "G".trim());
            statement.setDate("udatefrom", (Date) new Date(utility.StringToDate(udatefrom).getTime()));
            statement.setDate("udateto", (Date) new Date(utility.StringToDate(udateto).getTime()));
            statement.execute();
            ArrayList<FacilityComputedAmount> fcalist = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                FacilityComputedAmount fca = new FacilityComputedAmount();
                fca.setHospital(resultset.getString("PMCC_NO"));
                fca.setTotalamount(resultset.getString("CTOTAL"));
                fca.setYearfrom(udatefrom);
                fca.setYearto(udateto);
                fca.setTotalclaims(resultset.getString("COUNTVAL"));
                if (resultset.getTimestamp("DATESUB") != null) {
                    fca.setDatefiled(dateformat.format(resultset.getTimestamp("DATESUB")));
                } else {
                    fca.setDatefiled("");
                }
                if (resultset.getTimestamp("DATEREFILE") != null) {
                    fca.setDaterefiled(dateformat.format(resultset.getTimestamp("DATEREFILE")));
                } else {
                    fca.setDaterefiled("");
                }
                if (resultset.getTimestamp("DATEADM") != null) {
                    fca.setDateadmit(dateformat.format(resultset.getTimestamp("DATEADM")));
                } else {
                    fca.setDateadmit("");
                }
                fcalist.add(fca);
            }

            if (fcalist.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(fcalist));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
//=======================================

    public ACRGBWSResult GETSUMAMOUNTCLAIMSBOOKDATA(
            final DataSource dataSource,
            final String upmmcno,
            final String udatefrom,
            final String udateto) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETSUMAMOUNTCLAIMSBOOKDATA(:upmmcno,:utags,:udatefrom,:udateto); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("upmmcno", upmmcno.trim());
            statement.setString("utags", "G".trim());
            statement.setDate("udatefrom", (Date) new Date(utility.StringToDate(udatefrom).getTime()));
            statement.setDate("udateto", (Date) new Date(utility.StringToDate(utility.AddMinusDaysDate(udateto, "60")).getTime()));
            statement.execute();
            ArrayList<FacilityComputedAmount> fcalist = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                FacilityComputedAmount fca = new FacilityComputedAmount();
                fca.setHospital(resultset.getString("PMCC_NO"));
                fca.setTotalamount(resultset.getString("CLAIMSTOTAL"));
                fca.setYearfrom(udatefrom);
                fca.setYearto(utility.AddMinusDaysDate(udateto, "60"));
                fca.setTotalclaims(resultset.getString("CLAIMSVOLUME"));
                if (resultset.getTimestamp("DATESUB") != null) {
                    fca.setDatefiled(dateformat.format(resultset.getTimestamp("DATESUB")));
                } else {
                    fca.setDatefiled("");
                }
                if (resultset.getTimestamp("DATEREFILE") != null) {
                    fca.setDaterefiled(dateformat.format(resultset.getTimestamp("DATEREFILE")));
                } else {
                    fca.setDaterefiled("");
                }
                if (resultset.getTimestamp("DATEADM") != null) {
                    fca.setDateadmit(dateformat.format(resultset.getTimestamp("DATEADM")));
                } else {
                    fca.setDateadmit("");
                }
                fcalist.add(fca);
            }

            if (fcalist.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(fcalist));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //===============================================================================================
    //PROCESS LEDGER PER CONTRACT UNDER HCPN
    public ACRGBWSResult GETLedgerPerContractHCPN(final DataSource dataSource,
            final String hcpncode,
            final String conid,
            final String utags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Ledger> ledgerlist = new ArrayList<>();
        try {
            int finalclaims = 0;
            double remaining = 0.00;
            double begin = 0.00;
            //  double finalblance = 0.00;
            ACRGBWSResult restA = new FetchMethods().GETASSETBYIDANDCONID(dataSource, hcpncode, conid, utags);
            if (restA.isSuccess()) {
                List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                for (int y = 0; y < assetlist.size(); y++) {
                    if (assetlist.get(y).getPreviousbalance() != null) {
                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(y).getTranchid(), Tranch.class);
                        switch (tranch.getTranchtype()) {
                            case "1ST": {
                                begin += Double.parseDouble(assetlist.get(y).getPreviousbalance());
                                break;
                            }
                            case "1STFINAL": {
                                if (!assetlist.get(0).getPreviousbalance().isEmpty()) {
                                    int from = Integer.parseInt(assetlist.get(0).getClaimscount());
                                    int to = Integer.parseInt(assetlist.get(y).getClaimscount());
                                    finalclaims = to - from;
                                }
                                break;
                            }
                        }
                    }
                }

                if (begin > 0.00) {
                    Ledger ledgersss = new Ledger();
                    ledgersss.setDatetime(assetlist.get(0).getDatecreated());
                    ledgersss.setParticular("Beginning Balance");
                    ledgersss.setCredit(String.valueOf(begin));
                    ledgersss.setBalance(String.valueOf(begin));
                    ledgerlist.add(ledgersss);
                }
                //------------------------------------------------------------
                for (int x = 0; x < assetlist.size(); x++) {
                    Ledger ledger = new Ledger();
                    ledger.setDatetime(assetlist.get(x).getDatereleased());
                    if (!assetlist.get(x).getTranchid().isEmpty()) {
                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(x).getTranchid(), Tranch.class);
                        switch (tranch.getTranchtype()) {
                            case "1ST": {
                                if (begin > 0.00) {
                                    ledger.setParticular("Payment of 1ST Tranche running balance from previous contract");
                                } else {
                                    ledger.setParticular("Payment of 1ST Tranche of new contract");
                                }
                                ledger.setCredit(String.valueOf(Double.parseDouble(assetlist.get(x).getReleasedamount())));
                                remaining += Double.parseDouble(assetlist.get(x).getReleasedamount());//INCREMENT ASSETS
                                break;
                            }
                            case "1STFINAL": {
                                ledger.setParticular("Payment of 1ST Tranche from fully recon contract balance");
                                ledger.setCredit(String.valueOf(Double.parseDouble(assetlist.get(x).getReleasedamount())));
                                remaining += Double.parseDouble(assetlist.get(x).getReleasedamount());//INCREMENT ASSETS
                                break;
                            }
                            default: {
                                ledger.setParticular("Front Loading Of " + tranch.getTranchtype() + " Tranche");
                                ledger.setCredit(assetlist.get(x).getReleasedamount());
                                remaining += Double.parseDouble(assetlist.get(x).getReleasedamount());//INCREMENT ASSETS
                                break;
                            }
                        }
                        switch (tranch.getTranchtype()) {
                            case "1STFINAL": {
                                Ledger ledgers = new Ledger();
                                ledgers.setParticular("Unutilization fund from revious fully recon contract");
                                ledgers.setDebit(String.valueOf(Double.parseDouble(assetlist.get(x).getReleasedamount())));
                                ledgers.setDatetime(assetlist.get(x).getDatereleased());
                                ledgers.setTotalclaims(String.valueOf(finalclaims));
                                ledgers.setBalance(String.valueOf(remaining));
                                ledgers.setVoucher("");
                                ledgerlist.add(ledgers);
                                break;
                            }
                        }

                    }
                    ledger.setTotalclaims(assetlist.get(x).getClaimscount());
                    ledger.setBalance(String.valueOf(remaining));
                    ledger.setVoucher(assetlist.get(x).getReceipt());
                    ledgerlist.add(ledger);
                }
            }
            //GET LIQUIDATION PART
            ACRGBWSResult hcflist = new Methods().GETROLEMULITPLE(dataSource, hcpncode, utags);
            ACRGBWSResult getContractDate = new FetchMethods().GETCONTRACTCONID(dataSource, conid, utags);
            if (getContractDate.isSuccess()) {
                Contract cons = utility.ObjectMapper().readValue(getContractDate.getResult(), Contract.class);
                if (cons.getContractdate() != null) {
                    ContractDate contractdate = utility.ObjectMapper().readValue(cons.getContractdate(), ContractDate.class);
                    if (hcflist.isSuccess()) {
                        List<String> hcfresult = Arrays.asList(hcflist.getResult().split(","));
                        for (int b = 0; b < hcfresult.size(); b++) {
                            //------------------------------------------------------------------- THIS GET ALL PMCC NO UNDER SEKECTED FACILITY
                            ACRGBWSResult getHcfByCode = new GetHCFMultiplePMCCNO().GETFACILITYBYCODE(dataSource, hcfresult.get(b).trim());
                            if (getHcfByCode.isSuccess()) {
                                HealthCareFacility healthCareFacility = utility.ObjectMapper().readValue(getHcfByCode.getResult(), HealthCareFacility.class);
                                //GET HCF DETAILS BY NAME
                                ACRGBWSResult getHcfByName = new GetHCFMultiplePMCCNO().GETFACILITYBYNAME(dataSource, healthCareFacility.getHcfname().trim(), healthCareFacility.getStreet().trim());
                                if (getHcfByName.isSuccess()) {
                                    List<HealthCareFacility> healthCareFacilityList = Arrays.asList(utility.ObjectMapper().readValue(getHcfByName.getResult(), HealthCareFacility[].class));
                                    for (int yu = 0; yu < healthCareFacilityList.size(); yu++) {
                                        //------------------------------------------------------------------------END THIS GET ALL PMCC NO UNDER SEKECTED FACILITY
                                        ACRGBWSResult getAmountPayable = this.GETSUMAMOUNTCLAIMS(dataSource,
                                                healthCareFacilityList.get(yu).getHcfcode().trim(),
                                                contractdate.getDatefrom(),
                                                utility.AddMinusDaysDate(contractdate.getDateto(), "60"));
                                        if (getAmountPayable.isSuccess()) {
                                            List<FacilityComputedAmount> hcfA = Arrays.asList(utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount[].class));
                                            for (int u = 0; u < hcfA.size(); u++) {
                                                if (hcfA.get(u).getDaterefiled().isEmpty()) {
                                                    if (dateformat.parse(hcfA.get(u).getDatefiled()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdate.getDateto(), "60"))) <= 0) {
                                                        Ledger SubledgerA = new Ledger();
                                                        SubledgerA.setDatetime(hcfA.get(u).getDatefiled());
                                                        ACRGBWSResult facility = new FetchMethods().GETFACILITYID(dataSource, healthCareFacilityList.get(yu).getHcfcode().trim());
                                                        HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                                                        SubledgerA.setFacility(hcf.getHcfname());
                                                        SubledgerA.setParticular("Liquidation Of " + hcf.getHcfname());
                                                        SubledgerA.setDebit(hcfA.get(u).getTotalamount());
                                                        SubledgerA.setTotalclaims(hcfA.get(u).getTotalclaims());
                                                        remaining -= Double.parseDouble(hcfA.get(u).getTotalamount());
                                                        SubledgerA.setBalance(String.valueOf(remaining));
                                                        if (remaining > 0) {
                                                            SubledgerA.setBalance(String.valueOf(remaining));
                                                            SubledgerA.setAccount("Liabilities");
                                                        } else {
                                                            SubledgerA.setBalance(String.valueOf(remaining));
                                                            SubledgerA.setAccount("Payables");
                                                        }
                                                        SubledgerA.setLiquidation(hcfA.get(u).getTotalamount());
                                                        ledgerlist.add(SubledgerA);
                                                    }

                                                } else {
                                                    if (dateformat.parse(hcfA.get(u).getDaterefiled()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdate.getDateto(), "60"))) <= 0) {
                                                        Ledger SubledgerA = new Ledger();
                                                        SubledgerA.setDatetime(hcfA.get(u).getDatefiled());
                                                        ACRGBWSResult facility = new FetchMethods().GETFACILITYID(dataSource, healthCareFacilityList.get(yu).getHcfcode().trim());
                                                        HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                                                        SubledgerA.setFacility(hcf.getHcfname());
                                                        SubledgerA.setParticular("Liquidation Of " + hcf.getHcfname());
                                                        SubledgerA.setDebit(hcfA.get(u).getTotalamount());
                                                        SubledgerA.setTotalclaims(hcfA.get(u).getTotalclaims());
                                                        remaining -= Double.parseDouble(hcfA.get(u).getTotalamount());
                                                        SubledgerA.setBalance(String.valueOf(remaining));
                                                        if (remaining > 0) {
                                                            SubledgerA.setBalance(String.valueOf(remaining));
                                                            SubledgerA.setAccount("Liabilities");
                                                        } else {
                                                            SubledgerA.setBalance(String.valueOf(remaining));
                                                            SubledgerA.setAccount("Payables");
                                                        }
                                                        SubledgerA.setLiquidation(hcfA.get(u).getTotalamount());
                                                        ledgerlist.add(SubledgerA);
                                                    }
                                                }

                                            }
                                        }

                                    }
                                }
                            }

                        }
                    }
                }
            }
            if (ledgerlist.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(ledgerlist));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //===============================================================================================
    //PROCESS LEDGER PER CONTRACT UNDER HCPN
    public ACRGBWSResult GETLedgerPerContractHCPNLedger(
            final DataSource dataSource,
            final String hcpncode,
            final String conid,
            final String utags,
            final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Ledger> ledgerlist = new ArrayList<>();
        try {
            double remaining = 0.00;
            double begin = 0.00;
            int finalclaims = 0;
            ACRGBWSResult restA = new FetchMethods().GETASSETBYIDANDCONID(dataSource, hcpncode, conid, utags);
            if (restA.isSuccess()) {
                List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                for (int y = 0; y < assetlist.size(); y++) {
                    if (assetlist.get(y).getPreviousbalance() != null) {
                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(y).getTranchid(), Tranch.class);
                        switch (tranch.getTranchtype()) {
                            case "1ST": {
                                begin += Double.parseDouble(assetlist.get(y).getPreviousbalance());
                                break;
                            }
                            case "1STFINAL": {
                                if (!assetlist.get(0).getPreviousbalance().isEmpty()) {
                                    int from = Integer.parseInt(assetlist.get(0).getClaimscount());
                                    int to = Integer.parseInt(assetlist.get(y).getClaimscount());
                                    finalclaims = to - from;
                                    // finalblance += Double.parseDouble(assetlist.get(y).getReleasedamount());
                                }
                                break;
                            }
                        }
                    }
                }
                if (begin > 0.00) {
                    Ledger ledgersss = new Ledger();
                    ledgersss.setDatetime(assetlist.get(0).getDatecreated());
                    ledgersss.setParticular("Beginning Balance");
                    ledgersss.setCredit(String.valueOf(begin));
                    ledgersss.setBalance(String.valueOf(begin));
                    ledgerlist.add(ledgersss);
                }

                //------------------------------------------------------------
                for (int x = 0; x < assetlist.size(); x++) {
                    Ledger ledger = new Ledger();
                    ledger.setDatetime(assetlist.get(x).getDatereleased());
                    if (!assetlist.get(x).getTranchid().isEmpty()) {
                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(x).getTranchid(), Tranch.class);
                        switch (tranch.getTranchtype()) {
                            case "1STFINAL": {
                                ledger.setParticular("Payment of 1ST Tranche from fully recon contract balance");
                                ledger.setCredit(String.valueOf(Double.parseDouble(assetlist.get(x).getReleasedamount())));
                                remaining += Double.parseDouble(assetlist.get(x).getReleasedamount());//INCREMENT ASSETS
                                break;
                            }
                            case "1ST": {
                                if (begin > 0.00) {
                                    ledger.setParticular("Payment of 1ST Tranche running balance from previous contract");
                                } else {
                                    ledger.setParticular("Payment of 1ST Tranche of new contract");
                                }
                                ledger.setCredit(String.valueOf(Double.parseDouble(assetlist.get(x).getReleasedamount()) + begin));
                                remaining += Double.parseDouble(assetlist.get(x).getReleasedamount());//INCREMENT ASSETS
                                break;
                            }
                            default: {
                                ledger.setParticular("Payment of " + tranch.getTranchtype() + " tranche");
                                ledger.setCredit(assetlist.get(x).getReleasedamount());
                                remaining += Double.parseDouble(assetlist.get(x).getReleasedamount());//INCREMENT ASSETS
                                break;
                            }
                        }
                        switch (tranch.getTranchtype()) {
                            case "1STFINAL": {
                                Ledger ledgers = new Ledger();
                                ledgers.setParticular("Unutilization fund from revious fully recon contract");
                                ledgers.setDebit(String.valueOf(Double.parseDouble(assetlist.get(x).getReleasedamount())));
                                ledgers.setDatetime(assetlist.get(x).getDatereleased());
                                ledgers.setTotalclaims(String.valueOf(finalclaims));
                                ledgers.setBalance(String.valueOf(remaining));
                                ledgers.setVoucher("");
                                ledgerlist.add(ledgers);
                                break;
                            }
                        }
                    }
                    ledger.setTotalclaims(assetlist.get(x).getClaimscount());
                    ledger.setBalance(String.valueOf(remaining));
                    ledger.setVoucher(assetlist.get(x).getReceipt());
                    ledgerlist.add(ledger);
                    //======================================================
                }
            }
            //GET LIQUIDATION PART
            ACRGBWSResult getContractDate = new FetchMethods().GETCONTRACTCONID(dataSource, conid, utags);
            if (getContractDate.isSuccess()) {
                Contract cons = utility.ObjectMapper().readValue(getContractDate.getResult(), Contract.class);
                if (cons.getContractdate() != null) {
                    ContractDate contractdate = utility.ObjectMapper().readValue(cons.getContractdate(), ContractDate.class);
                    ACRGBWSResult hcflist = new Methods().GETROLEMULITPLEFORENDROLE(
                            dataSource,
                            utags.trim().toUpperCase(),
                            hcpncode.trim(),
                            contractdate.getCondateid().trim());
                    if (hcflist.isSuccess()) {
                        List<String> hcfresult = Arrays.asList(hcflist.getResult().split(","));
                        for (int b = 0; b < hcfresult.size(); b++) {
                            ACRGBWSResult GetHCIContract = new ContractMethod().GETCONTRACTWITHOPENSTATE(dataSource, utags.trim().toUpperCase(), hcfresult.get(b).trim(), ustate);
                            if (GetHCIContract.isSuccess()) {
                                Contract conshci = utility.ObjectMapper().readValue(GetHCIContract.getResult(), Contract.class);
                                if (conshci.getContractdate() != null) {
                                    ContractDate contractdateHCI = utility.ObjectMapper().readValue(conshci.getContractdate(), ContractDate.class);
                                    //------------------------------------------------------------------- THIS AREA GET ALL PMCC NO UNDER SELECTED FACILITTY
                                    ACRGBWSResult getHcfByCode = new GetHCFMultiplePMCCNO().GETFACILITYBYCODE(dataSource, hcfresult.get(b).trim());
                                    if (getHcfByCode.isSuccess()) {
                                        HealthCareFacility healthCareFacility = utility.ObjectMapper().readValue(getHcfByCode.getResult(), HealthCareFacility.class);
                                        //GET HCF DETAILS BY NAME
                                        ACRGBWSResult getHcfByName = new GetHCFMultiplePMCCNO().GETFACILITYBYNAME(dataSource, healthCareFacility.getHcfname().trim(), healthCareFacility.getStreet().trim());
                                        if (getHcfByName.isSuccess()) {
                                            List<HealthCareFacility> healthCareFacilityList = Arrays.asList(utility.ObjectMapper().readValue(getHcfByName.getResult(), HealthCareFacility[].class));
                                            for (int yu = 0; yu < healthCareFacilityList.size(); yu++) {
                                                //------------------------------------------------------------------------ END OF THIS AREA GET ALL PMCC NO UNDER SELECTED FACILITTY
                                                ACRGBWSResult getAmountPayable = this.GETSUMAMOUNTCLAIMSBOOKDATA(dataSource,
                                                        healthCareFacilityList.get(yu).getHcfcode().trim(),
                                                        contractdateHCI.getDatefrom().trim(),
                                                        utility.AddMinusDaysDate(contractdateHCI.getDateto().trim(), "60").trim());
                                                if (getAmountPayable.isSuccess()) {
                                                    List<FacilityComputedAmount> hcfA = Arrays.asList(utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount[].class));
                                                    for (int u = 0; u < hcfA.size(); u++) {
                                                        int addledger = 0;
                                                        if (hcfA.get(u).getDaterefiled().isEmpty()) {
                                                            if (conshci.getEnddate().isEmpty()) {
                                                                if (dateformat.parse(hcfA.get(u).getDatefiled()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdateHCI.getDateto(), "60"))) <= 0) {
                                                                    addledger++;
                                                                }
                                                            } else {
                                                                if (dateformat.parse(hcfA.get(u).getDatefiled()).compareTo(dateformat.parse(utility.AddMinusDaysDate(conshci.getEnddate().trim(), "60"))) <= 0) {
                                                                    addledger++;
                                                                }
                                                            }
                                                        } else {
                                                            if (conshci.getEnddate().isEmpty()) {
                                                                if (dateformat.parse(hcfA.get(u).getDaterefiled()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdateHCI.getDateto(), "60"))) <= 0) {
                                                                    addledger++;
                                                                }
                                                            } else {
                                                                if (dateformat.parse(hcfA.get(u).getDaterefiled()).compareTo(dateformat.parse(utility.AddMinusDaysDate(conshci.getEnddate().trim(), "60"))) <= 0) {
                                                                    addledger++;
                                                                }
                                                            }
                                                        }
                                                        if (addledger > 0) {
                                                            Ledger SubledgerA = new Ledger();
                                                            SubledgerA.setDatetime(hcfA.get(u).getDatefiled());//utility.AddMinusDaysDate(conshci.getEnddate().trim(), "60")
                                                            ACRGBWSResult facility = new FetchMethods().GETFACILITYID(dataSource, healthCareFacilityList.get(yu).getHcfcode().trim());
                                                            HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                                                            SubledgerA.setFacility(hcf.getHcfname());
                                                            SubledgerA.setParticular("Liquidation Of " + hcf.getHcfname());
                                                            SubledgerA.setDebit(hcfA.get(u).getTotalamount());
                                                            SubledgerA.setTotalclaims(hcfA.get(u).getTotalclaims());
                                                            remaining -= Double.parseDouble(hcfA.get(u).getTotalamount());
                                                            SubledgerA.setBalance(String.valueOf(remaining));
                                                            if (remaining > 0) {
                                                                SubledgerA.setBalance(String.valueOf(remaining));
                                                                SubledgerA.setAccount("Liabilities");
                                                            } else {
                                                                SubledgerA.setBalance(String.valueOf(remaining));
                                                                SubledgerA.setAccount("Payables");
                                                            }
                                                            SubledgerA.setLiquidation(hcfA.get(u).getTotalamount());
                                                            ledgerlist.add(SubledgerA);
                                                        }
                                                    }
                                                }

                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
            if (ledgerlist.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(ledgerlist));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //PROCESS LEDGER PER CONTRACT OF SELECTED APEX FACILITY ACTIVE
    public ACRGBWSResult GETLedgerAllContractAPEXActive(
            final DataSource dataSource,
            final String upmmc_no,
            final String contractid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Ledger> ledgerlist = new ArrayList<>();
        try {
            double remaining = 0.00;
            double begin = 0.00;
            int finalclaims = 0;
            ACRGBWSResult restA = new FetchMethods().GETASSETBYIDANDCONID(dataSource, upmmc_no, contractid, "ACTIVE");
            if (restA.isSuccess()) {
                List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                //GETTING PREVIOUS BALANCE
                for (int y = 0; y < assetlist.size(); y++) {
                    if (assetlist.get(y).getPreviousbalance() != null) {
                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(y).getTranchid(), Tranch.class);
                        switch (tranch.getTranchtype()) {
                            case "1ST": {
                                begin += Double.parseDouble(assetlist.get(y).getPreviousbalance());
                                break;
                            }
                            case "1STFINAL": {
                                if (!assetlist.get(0).getPreviousbalance().isEmpty()) {
                                    int from = Integer.parseInt(assetlist.get(0).getClaimscount());
                                    int to = Integer.parseInt(assetlist.get(y).getClaimscount());
                                    finalclaims = to - from;
                                    // finalblance += Double.parseDouble(assetlist.get(y).getReleasedamount());
                                }
                                break;
                            }
                        }
                    }
                }
                if (begin > 0.00) {
                    Ledger ledgersss = new Ledger();
                    ledgersss.setDatetime(assetlist.get(0).getDatecreated());
                    ledgersss.setParticular("Beginning Balance");
                    ledgersss.setCredit(String.valueOf(begin));
                    ledgersss.setBalance(String.valueOf(begin));
                    ledgerlist.add(ledgersss);
                }
                //GETTING PREVIOUS BALANCE
                for (int x = 0; x < assetlist.size(); x++) {
                    Ledger ledger = new Ledger();
                    ledger.setDatetime(assetlist.get(x).getDatereleased());
                    if (!assetlist.get(x).getTranchid().isEmpty()) {
                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(x).getTranchid(), Tranch.class);
                        switch (tranch.getTranchtype()) {
                            case "1STFINAL": {
                                ledger.setParticular("Payment of 1ST Tranche from fully recon contract balance");
                                ledger.setCredit(String.valueOf(Double.parseDouble(assetlist.get(x).getReleasedamount())));
                                remaining += Double.parseDouble(assetlist.get(x).getReleasedamount());//INCREMENT ASSETS
                                break;
                            }
                            case "1ST": {
                                if (begin > 0.00) {
                                    ledger.setParticular("Payment of 1ST Tranche running balance from previous contract");
                                } else {
                                    ledger.setParticular("Payment of 1ST Tranche of new contract");
                                }
                                ledger.setCredit(String.valueOf(Double.parseDouble(assetlist.get(x).getReleasedamount())));
                                remaining += Double.parseDouble(assetlist.get(x).getReleasedamount());//INCREMENT ASSETS
                                break;
                            }
                            default: {
                                ledger.setParticular("Payment of " + tranch.getTranchtype() + " tranche");
                                ledger.setCredit(assetlist.get(x).getReleasedamount());
                                remaining += Double.parseDouble(assetlist.get(x).getReleasedamount());//INCREMENT ASSETS
                                break;
                            }
                        }

                        switch (tranch.getTranchtype()) {
                            case "1STFINAL": {
                                Ledger ledgers = new Ledger();
                                ledgers.setParticular("Unutilization fund from revious fully recon contract");
                                ledgers.setDebit(String.valueOf(Double.parseDouble(assetlist.get(x).getReleasedamount())));
                                ledgers.setDatetime(assetlist.get(x).getDatereleased());
                                ledgers.setTotalclaims(String.valueOf(finalclaims));
                                ledgers.setBalance(String.valueOf(remaining));
                                ledgers.setVoucher("");
                                ledgerlist.add(ledgers);
                                break;
                            }
                        }

                    }
                    ledger.setTotalclaims(assetlist.get(x).getClaimscount());
                    ledger.setBalance(String.valueOf(remaining));
                    ledger.setVoucher(assetlist.get(x).getReceipt());
                    ledgerlist.add(ledger);
                }
            }
            //GET LIQUIDATION PART
            ACRGBWSResult getContractDate = new FetchMethods().GETCONTRACTCONID(dataSource, contractid, "ACTIVE");
            if (getContractDate.isSuccess()) {
                Contract cons = utility.ObjectMapper().readValue(getContractDate.getResult(), Contract.class);
                if (cons.getContractdate() != null) {
                    ContractDate contractdate = utility.ObjectMapper().readValue(cons.getContractdate(), ContractDate.class);
                    //-------------------------------------------------------------------
                    ACRGBWSResult getHcfByCode = new GetHCFMultiplePMCCNO().GETFACILITYBYCODE(dataSource, upmmc_no.trim());
                    if (getHcfByCode.isSuccess()) {
                        HealthCareFacility healthCareFacility = utility.ObjectMapper().readValue(getHcfByCode.getResult(), HealthCareFacility.class);
                        //GET HCF DETAILS BY NAME
                        ACRGBWSResult getHcfByName = new GetHCFMultiplePMCCNO().GETFACILITYBYNAME(dataSource, healthCareFacility.getHcfname().trim(), healthCareFacility.getStreet().trim());
                        if (getHcfByName.isSuccess()) {
                            List<HealthCareFacility> healthCareFacilityList = Arrays.asList(utility.ObjectMapper().readValue(getHcfByName.getResult(), HealthCareFacility[].class));
                            for (int yu = 0; yu < healthCareFacilityList.size(); yu++) {
                                //------------------------------------------------------------------------
                                ACRGBWSResult getAmountPayable = this.GETSUMAMOUNTCLAIMS(dataSource,
                                        healthCareFacilityList.get(yu).getHcfcode(),
                                        contractdate.getDatefrom(),
                                        utility.AddMinusDaysDate(contractdate.getDateto(), "60"));
                                if (getAmountPayable.isSuccess()) {
                                    List<FacilityComputedAmount> hcfA = Arrays.asList(utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount[].class));
                                    for (int u = 0; u < hcfA.size(); u++) {
                                        int countLedger = 0;
                                        if (hcfA.get(u).getDaterefiled().isEmpty()) {
                                            if (cons.getEnddate().isEmpty()) {
                                                if (dateformat.parse(hcfA.get(u).getDatefiled()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdate.getDateto(), "60"))) <= 0) {
                                                    countLedger++;
                                                }
                                            } else {
                                                if (dateformat.parse(hcfA.get(u).getDatefiled()).compareTo(dateformat.parse(utility.AddMinusDaysDate(cons.getEnddate().trim(), "60"))) <= 0) {
                                                    countLedger++;
                                                }
                                            }
                                        } else {
                                            if (cons.getEnddate().isEmpty()) {//cons.getEnddate().trim()
                                                if (dateformat.parse(hcfA.get(u).getDaterefiled()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdate.getDateto(), "60"))) <= 0) {
                                                    countLedger++;
                                                }
                                            } else {
                                                if (dateformat.parse(hcfA.get(u).getDaterefiled()).compareTo(dateformat.parse(utility.AddMinusDaysDate(cons.getEnddate().trim(), "60"))) <= 0) {
                                                    countLedger++;
                                                }
                                            }
                                        }
                                        if (countLedger > 0) {
                                            Ledger SubledgerA = new Ledger();
                                            SubledgerA.setDatetime(hcfA.get(u).getDatefiled());
                                            ACRGBWSResult facility = new FetchMethods().GETFACILITYID(dataSource, healthCareFacilityList.get(yu).getHcfcode());
                                            HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                                            SubledgerA.setFacility(hcf.getHcfname());
                                            SubledgerA.setParticular("Liquidation Of " + hcf.getHcfname());
                                            SubledgerA.setDebit(hcfA.get(u).getTotalamount());
                                            SubledgerA.setTotalclaims(hcfA.get(u).getTotalclaims());
                                            remaining -= Double.parseDouble(hcfA.get(u).getTotalamount());
                                            SubledgerA.setBalance(String.valueOf(remaining));
                                            if (remaining > 0) {
                                                SubledgerA.setBalance(String.valueOf(remaining));
                                                SubledgerA.setAccount("Liabilities");
                                            } else {
                                                SubledgerA.setBalance(String.valueOf(remaining));
                                                SubledgerA.setAccount("Payables");
                                            }
                                            SubledgerA.setLiquidation(hcfA.get(u).getTotalamount());
                                            ledgerlist.add(SubledgerA);

                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
            if (ledgerlist.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(ledgerlist));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //PROCESS LEDGER PER CONTRACT OF SELECTED APEX FACILITY ACTIVE
    public ACRGBWSResult GETLedgerAllContractAPEXInactive(
            final DataSource dataSource,
            final String upmmc_no,
            final String contractid,
            final String utags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<String> errorList = new ArrayList<>();
        ArrayList<Ledger> ledgerlist = new ArrayList<>();
        try {
            double remaining = 0.00;
            double begin = 0.00;
            int finalclaims = 0;
            ACRGBWSResult restA = new FetchMethods().GETASSETBYIDANDCONID(dataSource, upmmc_no, contractid, utags);
            if (restA.isSuccess()) {
                List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                //GETTING PREVIOUS BALANCE
                for (int y = 0; y < assetlist.size(); y++) {
                    if (assetlist.get(y).getPreviousbalance() != null) {
                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(y).getTranchid(), Tranch.class);
                        switch (tranch.getTranchtype()) {
                            case "1ST": {
                                begin += Double.parseDouble(assetlist.get(y).getPreviousbalance());
                                break;
                            }
                            case "1STFINAL": {
                                if (!assetlist.get(0).getPreviousbalance().isEmpty()) {
                                    int from = Integer.parseInt(assetlist.get(0).getClaimscount());
                                    int to = Integer.parseInt(assetlist.get(y).getClaimscount());
                                    finalclaims = to - from;
                                    // finalblance += Double.parseDouble(assetlist.get(y).getReleasedamount());
                                }
                                break;
                            }
                        }
                    }
                }
                if (begin > 0.00) {
                    Ledger ledgersss = new Ledger();
                    ledgersss.setDatetime(assetlist.get(0).getDatecreated());
                    ledgersss.setParticular("Beginning Balance");
                    ledgersss.setCredit(String.valueOf(begin));
                    ledgersss.setBalance(String.valueOf(begin));
                    ledgerlist.add(ledgersss);
                }
                //GETTING PREVIOUS BALANCE
                for (int x = 0; x < assetlist.size(); x++) {
                    Ledger ledger = new Ledger();
                    ledger.setDatetime(assetlist.get(x).getDatereleased());
                    if (!assetlist.get(x).getTranchid().isEmpty()) {
                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(x).getTranchid(), Tranch.class);
                        switch (tranch.getTranchtype()) {
                            case "1STFINAL": {
                                ledger.setParticular("Payment of 1ST Tranche from fully recon contract balance");
                                ledger.setCredit(String.valueOf(Double.parseDouble(assetlist.get(x).getReleasedamount())));
                                remaining += Double.parseDouble(assetlist.get(x).getReleasedamount());//INCREMENT ASSETS
                                break;
                            }
                            case "1ST": {
                                if (begin > 0.00) {
                                    ledger.setParticular("Payment of 1ST Tranche running balance from previous contract");
                                } else {
                                    ledger.setParticular("Payment of 1ST Tranche of new contract");
                                }
                                ledger.setCredit(String.valueOf(Double.parseDouble(assetlist.get(x).getReleasedamount()) + begin));
                                remaining += Double.parseDouble(assetlist.get(x).getReleasedamount());//INCREMENT ASSETS
                                break;
                            }
                            default: {
                                ledger.setParticular("Payment of " + tranch.getTranchtype() + " tranche");
                                ledger.setCredit(assetlist.get(x).getReleasedamount());
                                remaining += Double.parseDouble(assetlist.get(x).getReleasedamount());//INCREMENT ASSETS
                                break;
                            }
                        }
                        switch (tranch.getTranchtype()) {
                            case "1STFINAL": {
                                Ledger ledgers = new Ledger();
                                ledgers.setParticular("Unutilization fund from revious fully recon contract");
                                ledgers.setDebit(String.valueOf(Double.parseDouble(assetlist.get(x).getReleasedamount())));
                                ledgers.setDatetime(assetlist.get(x).getDatereleased());
                                ledgers.setTotalclaims(String.valueOf(finalclaims));
                                ledgers.setBalance(String.valueOf(remaining));
                                ledgers.setVoucher("");
                                ledgerlist.add(ledgers);
                                break;
                            }
                        }
                    }
                    ledger.setTotalclaims(assetlist.get(x).getClaimscount());
                    ledger.setBalance(String.valueOf(remaining));
                    ledger.setVoucher(assetlist.get(x).getReceipt());
                    ledgerlist.add(ledger);
                    //====================================================
                }
            } else {
                errorList.add("No Assets Found Under Selected Contract");
            }
            //GET LIQUIDATION PART
            ACRGBWSResult getContractDate = new FetchMethods().GETCONTRACTCONID(dataSource, contractid, utags.trim());
            if (getContractDate.isSuccess()) {
                Contract cons = utility.ObjectMapper().readValue(getContractDate.getResult(), Contract.class);
                if (cons.getContractdate() != null) {
                    ContractDate contractdate = utility.ObjectMapper().readValue(cons.getContractdate(), ContractDate.class);
                    //-------------------------------------------------------------------
                    ACRGBWSResult getHcfByCode = new GetHCFMultiplePMCCNO().GETFACILITYBYCODE(dataSource, upmmc_no.trim());
                    if (getHcfByCode.isSuccess()) {
                        HealthCareFacility healthCareFacility = utility.ObjectMapper().readValue(getHcfByCode.getResult(), HealthCareFacility.class);
                        //GET HCF DETAILS BY NAME
                        ACRGBWSResult getHcfByName = new GetHCFMultiplePMCCNO().GETFACILITYBYNAME(dataSource, healthCareFacility.getHcfname().trim(), healthCareFacility.getStreet().trim());
                        if (getHcfByName.isSuccess()) {
                            List<HealthCareFacility> healthCareFacilityList = Arrays.asList(utility.ObjectMapper().readValue(getHcfByName.getResult(), HealthCareFacility[].class));
                            for (int yu = 0; yu < healthCareFacilityList.size(); yu++) {
                                //------------------------------------------------------------------------
                                ACRGBWSResult getAmountPayable = this.GETSUMAMOUNTCLAIMSBOOKDATA(dataSource,
                                        healthCareFacilityList.get(yu).getHcfcode().trim(),
                                        contractdate.getDatefrom(),
                                        utility.AddMinusDaysDate(contractdate.getDateto().trim(), "60"));
                                if (getAmountPayable.isSuccess()) {
                                    List<FacilityComputedAmount> hcfA = Arrays.asList(utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount[].class));
                                    for (int u = 0; u < hcfA.size(); u++) {
                                        int ledgerVar = 0;
                                        if (hcfA.get(u).getDaterefiled().isEmpty()) {
                                            if (cons.getEnddate().isEmpty()) {
                                                if (dateformat.parse(hcfA.get(u).getDatefiled()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdate.getDateto(), "60"))) <= 0) {
                                                    ledgerVar++;
                                                }
                                            } else {
                                                if (dateformat.parse(hcfA.get(u).getDatefiled()).compareTo(dateformat.parse(cons.getEnddate().trim())) <= 0) {
                                                    ledgerVar++;
                                                }
                                            }
                                        } else {
                                            if (cons.getEnddate().isEmpty()) {
                                                if (dateformat.parse(hcfA.get(u).getDaterefiled()).compareTo(dateformat.parse(utility.AddMinusDaysDate(contractdate.getDateto(), "60"))) <= 0) {
                                                    ledgerVar++;
                                                }
                                            } else {
                                                if (dateformat.parse(hcfA.get(u).getDaterefiled()).compareTo(dateformat.parse(cons.getEnddate().trim())) <= 0) {
                                                    ledgerVar++;
                                                }
                                            }
                                        }
                                        if (ledgerVar > 0) {
                                            Ledger SubledgerA = new Ledger();
                                            SubledgerA.setDatetime(hcfA.get(u).getDatefiled());
                                            ACRGBWSResult facility = new FetchMethods().GETFACILITYID(dataSource, healthCareFacilityList.get(yu).getHcfcode().trim());
                                            HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                                            SubledgerA.setFacility(hcf.getHcfname());
                                            SubledgerA.setParticular("Liquidation of " + hcf.getHcfname());
                                            SubledgerA.setDebit(hcfA.get(u).getTotalamount());
                                            SubledgerA.setTotalclaims(hcfA.get(u).getTotalclaims());
                                            remaining -= Double.parseDouble(hcfA.get(u).getTotalamount());
                                            SubledgerA.setBalance(String.valueOf(remaining));
                                            if (remaining > 0) {
                                                SubledgerA.setBalance(String.valueOf(remaining));
                                                SubledgerA.setAccount("Liabilities");
                                            } else {
                                                SubledgerA.setBalance(String.valueOf(remaining));
                                                SubledgerA.setAccount("Payables");
                                            }
                                            SubledgerA.setLiquidation(hcfA.get(u).getTotalamount());
                                            ledgerlist.add(SubledgerA);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                errorList.add("Contract Not Found");
            }
            if (ledgerlist.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(ledgerlist));
                result.setSuccess(true);
            } else {
                result.setMessage(utility.ObjectMapper().writeValueAsString(errorList));
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
