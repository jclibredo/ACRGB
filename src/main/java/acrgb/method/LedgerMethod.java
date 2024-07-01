/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Assets;
import acrgb.structure.ConBalance;
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
 * @author MinoSun
 */
@RequestScoped
public class LedgerMethod {

    public LedgerMethod() {
    }
    private final Utility utility = new Utility();
    private final FetchMethods fm = new FetchMethods();
    private final Methods m = new Methods();
    private final ContractMethod contractmethod = new ContractMethod();
    private final SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");
    //private final SimpleDateFormat datetimeformat = utility.SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");

    //GET ASSESTS USING CONTRACT ID
    public ACRGBWSResult GetAssetsUsingConID(final DataSource dataSource, final String conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :assets_type := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETASSETSBYCONID(:pconid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pconid", conid);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Assets> listassets = new ArrayList<>();
            while (resultset.next()) {
                Assets assets = new Assets();
                assets.setAssetid(resultset.getString("ASSETSID"));
                ACRGBWSResult tranchresult = fm.ACR_TRANCHWITHID(dataSource, resultset.getString("TRANCHID"));
                if (tranchresult.isSuccess()) {
                    assets.setTranchid(tranchresult.getResult());
                } else {
                    assets.setTranchid(tranchresult.getMessage());
                }
                ACRGBWSResult facilityresult = fm.GETFACILITYID(dataSource, resultset.getString("HCFID"));
                if (facilityresult.isSuccess()) {
                    assets.setHcfid(facilityresult.getResult());
                } else {
                    assets.setHcfid(facilityresult.getMessage());
                }
                assets.setDatereleased(dateformat.format(resultset.getDate("DATERELEASED")));//resultset.getDate("DATERELEASED"));
                assets.setReceipt(resultset.getString("RECEIPT"));
                assets.setAmount(resultset.getString("AMOUNT"));
                ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    assets.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    assets.setCreatedby(creator.getMessage());
                }
                assets.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                ACRGBWSResult getcon = fm.GETCONTRACTCONID(dataSource, resultset.getString("CONID").trim(), "ACTIVE");
                if (getcon.isSuccess()) {
                    assets.setConid(getcon.getResult());
                } else {
                    assets.setConid(getcon.getMessage());
                }
                assets.setStatus(resultset.getString("STATS"));
                listassets.add(assets);
            }
            if (!listassets.isEmpty()) {
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

    public ACRGBWSResult GETASSETSBYHCF(final DataSource dataSource, final String phcfid, final String pdatefrom, final String pdateto) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETASSETSBYHCF(:phcfid,:pdatefrom,:pdateto); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("phcfid", phcfid);
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
                ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
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
                assets.setDatereleased(dateformat.format(resultset.getDate("DATERELEASED")));//resultset.getString("DATERELEASED"));
                assets.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                assetslist.add(assets);
            }
            if (!assetslist.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(assetslist));
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETSUMAMOUNTCLAIMS(final DataSource dataSource,
            final String uaccreno,
            final String udatefrom,
            final String udateto, final String ulevel) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKG.GETSUMAMOUNTCLAIMS(:ulevel,:uaccreno,:utags,:udatefrom,:udateto); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ulevel", ulevel.toUpperCase());
            statement.setString("uaccreno", uaccreno);
            statement.setString("utags", "G");
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
                fca.setDatefiled(dateformat.format(resultset.getDate("DATESUB")));
                fcalist.add(fca);

            }

            if (fcalist.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(fcalist));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
//=======================================

    public ACRGBWSResult GETSUMAMOUNTCLAIMSBOOKDATA(final DataSource dataSource, final String uaccreno, final String udatefrom, final String udateto, final String ulevel) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETSUMAMOUNTCLAIMSBOOKDATA(:ulevel,:uaccreno,:utags,:udatefrom,:udateto); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ulevel", ulevel.toUpperCase());
            statement.setString("uaccreno", uaccreno);
            statement.setString("utags", "G");
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
                fca.setDatefiled(dateformat.format(resultset.getDate("DATESUB")));
                fcalist.add(fca);

            }

            if (fcalist.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(fcalist));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException | ParseException ex) {
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
            final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        // ContractMethod cm = new ContractMethod();
        ArrayList<Ledger> ledgerlist = new ArrayList<>();
        try {
            Double remaining = 0.00;
            Double begin = 0.00;
//            ACRGBWSResult beginning = cm.GETPREVIOUSBALANCE(dataSource, hcpncode, conid);
//            if (beginning.isSuccess()) {
//                ConBalance conbal = utility.ObjectMapper().readValue(beginning.getResult(), ConBalance.class);
//                Ledger ledgersss = new Ledger();
//                ledgersss.setDatetime(conbal.getCondateid());
//                ledgersss.setParticular("Beginning Balance");
//                ledgersss.setCredit(conbal.getConbalance());
//                begin = Double.parseDouble(conbal.getConbalance());
//                ledgersss.setBalance(String.valueOf(begin));
//                ledgerlist.add(ledgersss);
//            }
            ACRGBWSResult restA = fm.GETASSETBYIDANDCONID(dataSource, hcpncode, conid, tags);
            if (restA.isSuccess()) {
                List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                for (int y = 0; y < assetlist.size(); y++) {
                    if (assetlist.get(y).getPreviousbalance() != null) {
                        begin += Double.parseDouble(assetlist.get(y).getPreviousbalance());
                    }
                }
                //  if (begin > 0.00) {
                Ledger ledgersss = new Ledger();
                ledgersss.setDatetime(assetlist.get(0).getDatecreated());
                ledgersss.setParticular("Beginning Balance");
                ledgersss.setCredit(String.valueOf(begin));
                ledgersss.setBalance(String.valueOf(begin));
                ledgerlist.add(ledgersss);
                //  }
                //------------------------------------------------------------
                for (int x = 0; x < assetlist.size(); x++) {
                    Ledger ledger = new Ledger();
                    ledger.setDatetime(assetlist.get(x).getDatereleased());
                    if (!assetlist.get(x).getTranchid().isEmpty()) {
                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(x).getTranchid(), Tranch.class);
                        ledger.setParticular("Front Loading Of " + tranch.getTranchtype() + " Tranche");
                        if (tranch.getTranchtype().equals("1ST")) {
                            double amount = Double.parseDouble(assetlist.get(x).getAmount());
                            ledger.setCredit(String.valueOf(amount - begin));
                            remaining += Double.parseDouble(assetlist.get(x).getAmount());//INCREMENT ASSETS
                        } else {
                            ledger.setCredit(assetlist.get(x).getAmount());
                            remaining += Double.parseDouble(assetlist.get(x).getAmount());//INCREMENT ASSETS
                        }
                    }
                    ledger.setBalance(String.valueOf(remaining));
                    ledger.setVoucher(assetlist.get(x).getReceipt());
                    ledgerlist.add(ledger);
                }
            }
            //GET LIQUIDATION PART
            ACRGBWSResult hcflist = m.GETROLEMULITPLE(dataSource, hcpncode, tags);
            ACRGBWSResult getContractDate = fm.GETCONTRACTCONID(dataSource, conid, tags);
            if (getContractDate.isSuccess()) {
                Contract cons = utility.ObjectMapper().readValue(getContractDate.getResult(), Contract.class);
                ContractDate contractdate = utility.ObjectMapper().readValue(cons.getContractdate(), ContractDate.class);
                if (hcflist.isSuccess()) {
                    List<String> hcfresult = Arrays.asList(hcflist.getResult().split(","));
                    for (int b = 0; b < hcfresult.size(); b++) {
                        ACRGBWSResult getAmountPayable = this.GETSUMAMOUNTCLAIMS(dataSource,
                                hcfresult.get(b),
                                contractdate.getDatefrom(),
                                contractdate.getDateto(), "TWO");
                        if (getAmountPayable.isSuccess()) {
                            List<FacilityComputedAmount> hcfA = Arrays.asList(utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount[].class));
                            for (int u = 0; u < hcfA.size(); u++) {
                                Ledger SubledgerA = new Ledger();
                                SubledgerA.setDatetime(hcfA.get(u).getDatefiled());
                                ACRGBWSResult facility = fm.GETFACILITYID(dataSource, hcfresult.get(b));
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
                                    SubledgerA.setAccount("Payabales");
                                }
                                SubledgerA.setLiquidation(hcfA.get(u).getTotalamount());
                                ledgerlist.add(SubledgerA);
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
    public ACRGBWSResult GETLedgerPerContractHCPNLedger(final DataSource dataSource,
            final String hcpncode,
            final String conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Ledger> ledgerlist = new ArrayList<>();
        try {
            Double remaining = 0.00;
            Double begin = 0.00;
            // ACRGBWSResult beginning = cm.GETPREVIOUSBALANCE(dataSource, hcpncode, conid);
//            if (beginning.isSuccess()) {
//                ConBalance conbal = utility.ObjectMapper().readValue(beginning.getResult(), ConBalance.class);
//                Ledger ledgersss = new Ledger();
//                ledgersss.setDatetime(conbal.getCondateid());
//                ledgersss.setParticular("Beginning Balance");
//                ledgersss.setCredit(conbal.getConbalance());
//                begin = Double.parseDouble(conbal.getConbalance());
//                ledgersss.setBalance(String.valueOf(begin));
//                ledgerlist.add(ledgersss);
//            }
            ACRGBWSResult restA = fm.GETASSETBYIDANDCONID(dataSource, hcpncode, conid, "INACTIVE");
            if (restA.isSuccess()) {
                List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                for (int y = 0; y < assetlist.size(); y++) {
                    if (assetlist.get(y).getPreviousbalance() != null) {
                        begin += Double.parseDouble(assetlist.get(y).getPreviousbalance());
                    }
                }
                //  if (begin > 0.00) {
                Ledger ledgersss = new Ledger();
                ledgersss.setDatetime(assetlist.get(0).getDatecreated());
                ledgersss.setParticular("Beginning Balance");
                ledgersss.setCredit(String.valueOf(begin));
                ledgersss.setBalance(String.valueOf(begin));
                ledgerlist.add(ledgersss);
                //  }
                //------------------------------------------------------------
                for (int x = 0; x < assetlist.size(); x++) {
                    Ledger ledger = new Ledger();
                    ledger.setDatetime(assetlist.get(x).getDatereleased());
                    if (!assetlist.get(x).getTranchid().isEmpty()) {
                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(x).getTranchid(), Tranch.class);
                        ledger.setParticular("Payment of " + tranch.getTranchtype() + " tranche");
                        if (tranch.getTranchtype().equals("1ST")) {
                            double amount = Double.parseDouble(assetlist.get(x).getAmount());
                            ledger.setCredit(String.valueOf(amount - begin));
                            remaining += Double.parseDouble(assetlist.get(x).getAmount());//INCREMENT ASSETS
                        } else {
                            ledger.setCredit(assetlist.get(x).getAmount());
                            remaining += Double.parseDouble(assetlist.get(x).getAmount());//INCREMENT ASSETS
                        }
                    }
                    ledger.setBalance(String.valueOf(remaining));
                    ledger.setVoucher(assetlist.get(x).getReceipt());
                    ledgerlist.add(ledger);
                    //====================================================

                    //======================================================
                }
            }
            //GET LIQUIDATION PART
            ACRGBWSResult getContractDate = fm.GETCONTRACTCONID(dataSource, conid, "INACTIVE");
            if (getContractDate.isSuccess()) {
                Contract cons = utility.ObjectMapper().readValue(getContractDate.getResult(), Contract.class);
                ContractDate contractdate = utility.ObjectMapper().readValue(cons.getContractdate(), ContractDate.class);
                ACRGBWSResult hcflist = m.GETROLEMULITPLEFORENDROLE(dataSource, hcpncode, "INACTIVE", contractdate.getCondateid());
                if (hcflist.isSuccess()) {
                    List<String> hcfresult = Arrays.asList(hcflist.getResult().split(","));
                    for (int b = 0; b < hcfresult.size(); b++) {
                        ACRGBWSResult getAmountPayable = this.GETSUMAMOUNTCLAIMSBOOKDATA(dataSource,
                                hcfresult.get(b),
                                contractdate.getDatefrom(),
                                contractdate.getDateto(), "TWO");
                        if (getAmountPayable.isSuccess()) {
                            List<FacilityComputedAmount> hcfA = Arrays.asList(utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount[].class));
                            for (int u = 0; u < hcfA.size(); u++) {
                                Ledger SubledgerA = new Ledger();
                                SubledgerA.setDatetime(hcfA.get(u).getDatefiled());
                                ACRGBWSResult facility = fm.GETFACILITYID(dataSource, hcfresult.get(b));
                                HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                                SubledgerA.setFacility(hcf.getHcfname());
                                SubledgerA.setParticular("Liquidation Of " + hcf.getHcfname());
                                SubledgerA.setDebit(hcfA.get(u).getTotalamount());
                                SubledgerA.setTotalclaims(hcfA.get(u).getTotalclaims());
                                remaining -= Double.parseDouble(hcfA.get(u).getTotalamount());
                                SubledgerA.setBalance(String.valueOf(remaining));
                                if (remaining > 0) {
                                    SubledgerA.setBalance(String.valueOf(remaining));
                                    SubledgerA.setAccount("Liability");
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
    public ACRGBWSResult GETLedgerAllContractAPEXActive(final DataSource dataSource,
            final String upmmc_no,
            final String contractid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ContractMethod cm = new ContractMethod();
        ArrayList<Ledger> ledgerlist = new ArrayList<>();
        try {
            Double remaining = 0.00;
            Double begin = 0.00;
            ACRGBWSResult beginning = cm.GETPREVIOUSBALANCE(dataSource, upmmc_no, contractid);
            if (beginning.isSuccess()) {
                ConBalance conbal = utility.ObjectMapper().readValue(beginning.getResult(), ConBalance.class);
                Ledger ledgersss = new Ledger();
                ledgersss.setDatetime(conbal.getCondateid());
                ledgersss.setParticular("Beginning Balance");
                ledgersss.setCredit(conbal.getConbalance());
                begin = Double.parseDouble(conbal.getConbalance());
                ledgersss.setBalance(String.valueOf(begin));
                ledgerlist.add(ledgersss);
            }
            ACRGBWSResult restA = fm.GETASSETBYIDANDCONID(dataSource, upmmc_no, contractid, "ACTIVE");
            if (restA.isSuccess()) {
                List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                for (int x = 0; x < assetlist.size(); x++) {
                    Ledger ledger = new Ledger();
                    ledger.setDatetime(assetlist.get(x).getDatereleased());
                    if (!assetlist.get(x).getTranchid().isEmpty()) {
                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(x).getTranchid(), Tranch.class);
                        ledger.setParticular("PAYMENT OF " + tranch.getTranchtype() + " TRANCHE");
                        if (tranch.getTranchtype().equals("1ST")) {
                            double amount = Double.parseDouble(assetlist.get(x).getAmount());
                            ledger.setCredit(String.valueOf(amount - begin));
                            remaining += Double.parseDouble(assetlist.get(x).getAmount());//INCREMENT ASSETS
                        } else {
                            ledger.setCredit(assetlist.get(x).getAmount());
                            remaining += Double.parseDouble(assetlist.get(x).getAmount());//INCREMENT ASSETS
                        }
                    }
                    ledger.setBalance(String.valueOf(remaining));
                    ledger.setVoucher(assetlist.get(x).getReceipt());
                    ledgerlist.add(ledger);
                    //====================================================

                }
            }
            //GET LIQUIDATION PART
            ACRGBWSResult getContractDate = fm.GETCONTRACTCONID(dataSource, contractid, "ACTIVE");
            if (getContractDate.isSuccess()) {
                Contract cons = utility.ObjectMapper().readValue(getContractDate.getResult(), Contract.class);
                ContractDate contractdate = utility.ObjectMapper().readValue(cons.getContractdate(), ContractDate.class);
                ACRGBWSResult getAmountPayable = this.GETSUMAMOUNTCLAIMS(dataSource,
                        upmmc_no,
                        contractdate.getDatefrom(),
                        contractdate.getDateto(), "TWO");
                if (getAmountPayable.isSuccess()) {
                    List<FacilityComputedAmount> hcfA = Arrays.asList(utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount[].class));
                    for (int u = 0; u < hcfA.size(); u++) {
                        Ledger SubledgerA = new Ledger();
                        SubledgerA.setDatetime(hcfA.get(u).getDatefiled());
                        ACRGBWSResult facility = fm.GETFACILITYID(dataSource, upmmc_no);
                        HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                        SubledgerA.setFacility(hcf.getHcfname());
                        SubledgerA.setParticular("LIQUIDATION OF " + hcf.getHcfname());
                        SubledgerA.setDebit(hcfA.get(u).getTotalamount());
                        SubledgerA.setTotalclaims(hcfA.get(u).getTotalclaims());
                        remaining -= Double.parseDouble(hcfA.get(u).getTotalamount());
                        SubledgerA.setBalance(String.valueOf(remaining));
                        if (remaining > 0) {
                            SubledgerA.setBalance(String.valueOf(remaining));
                            SubledgerA.setAccount("LIABILITY");
                        } else {
                            SubledgerA.setBalance(String.valueOf(remaining));
                            SubledgerA.setAccount("PAYABLES");
                        }
                        SubledgerA.setLiquidation(hcfA.get(u).getTotalamount());
                        ledgerlist.add(SubledgerA);
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

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //PROCESS LEDGER PER CONTRACT OF SELECTED APEX FACILITY ACTIVE
    public ACRGBWSResult GETLedgerAllContractAPEXInactive(final DataSource dataSource,
            final String upmmc_no,
            final String contractid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ContractMethod cm = new ContractMethod();
        ArrayList<Ledger> ledgerlist = new ArrayList<>();
        try {
            Double remaining = 0.00;
            Double begin = 0.00;
            ACRGBWSResult beginning = cm.GETPREVIOUSBALANCE(dataSource, upmmc_no, contractid);
            if (beginning.isSuccess()) {
                ConBalance conbal = utility.ObjectMapper().readValue(beginning.getResult(), ConBalance.class);
                Ledger ledgersss = new Ledger();
                ledgersss.setDatetime(conbal.getCondateid());
                ledgersss.setParticular("Beginning Balance");
                ledgersss.setCredit(conbal.getConbalance());
                begin = Double.parseDouble(conbal.getConbalance());
                ledgersss.setBalance(String.valueOf(begin));
                ledgerlist.add(ledgersss);
            }
            ACRGBWSResult restA = fm.GETASSETBYIDANDCONID(dataSource, upmmc_no, contractid, "INACTIVE");
            if (restA.isSuccess()) {
                List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                for (int x = 0; x < assetlist.size(); x++) {
                    Ledger ledger = new Ledger();
                    ledger.setDatetime(assetlist.get(x).getDatereleased());
                    if (!assetlist.get(x).getTranchid().isEmpty()) {
                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(x).getTranchid(), Tranch.class);
                        ledger.setParticular("PAYMENT OF " + tranch.getTranchtype() + " TRANCHE");
                        if (tranch.getTranchtype().equals("1ST")) {
                            double amount = Double.parseDouble(assetlist.get(x).getAmount());
                            ledger.setCredit(String.valueOf(amount - begin));
                            remaining += Double.parseDouble(assetlist.get(x).getAmount());//INCREMENT ASSETS
                        } else {
                            ledger.setCredit(assetlist.get(x).getAmount());
                            remaining += Double.parseDouble(assetlist.get(x).getAmount());//INCREMENT ASSETS
                        }
                    }
                    ledger.setBalance(String.valueOf(remaining));
                    ledger.setVoucher(assetlist.get(x).getReceipt());
                    ledgerlist.add(ledger);
                    //====================================================
                }
            }
            //GET LIQUIDATION PART
            ACRGBWSResult getContractDate = fm.GETCONTRACTCONID(dataSource, contractid, "INACTIVE");
            if (getContractDate.isSuccess()) {
                Contract cons = utility.ObjectMapper().readValue(getContractDate.getResult(), Contract.class);
                ContractDate contractdate = utility.ObjectMapper().readValue(cons.getContractdate(), ContractDate.class);
                ACRGBWSResult getAmountPayable = this.GETSUMAMOUNTCLAIMSBOOKDATA(dataSource,
                        upmmc_no,
                        contractdate.getDatefrom(),
                        contractdate.getDateto(), "TWO");
                if (getAmountPayable.isSuccess()) {
                    List<FacilityComputedAmount> hcfA = Arrays.asList(utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount[].class));
                    for (int u = 0; u < hcfA.size(); u++) {
                        Ledger SubledgerA = new Ledger();
                        SubledgerA.setDatetime(hcfA.get(u).getDatefiled());
                        ACRGBWSResult facility = fm.GETFACILITYID(dataSource, upmmc_no);
                        HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                        SubledgerA.setFacility(hcf.getHcfname());
                        SubledgerA.setParticular("LIQUIDATION OF " + hcf.getHcfname());
                        SubledgerA.setDebit(hcfA.get(u).getTotalamount());
                        SubledgerA.setTotalclaims(hcfA.get(u).getTotalclaims());
                        remaining -= Double.parseDouble(hcfA.get(u).getTotalamount());
                        SubledgerA.setBalance(String.valueOf(remaining));
                        if (remaining > 0) {
                            SubledgerA.setBalance(String.valueOf(remaining));
                            SubledgerA.setAccount("LIABILITY");
                        } else {
                            SubledgerA.setBalance(String.valueOf(remaining));
                            SubledgerA.setAccount("PAYABLES");
                        }
                        SubledgerA.setLiquidation(hcfA.get(u).getTotalamount());
                        ledgerlist.add(SubledgerA);
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
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
