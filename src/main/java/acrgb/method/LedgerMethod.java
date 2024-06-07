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
import acrgb.structure.FacilityComputedAmount;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.Ledger;
import acrgb.structure.ManagingBoard;
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

    //LEDGER HCPN BUDGET UTILIZATION 
    public ACRGBWSResult HCPNLedger(final DataSource dataSource, final String datefrom, final String dateto, final String accessid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        ArrayList<Ledger> ledgerlist = new ArrayList<>();
        try {
            ACRGBWSResult restA = m.GETROLEMULITPLE(dataSource, accessid);
            List<String> restAList = Arrays.asList(restA.getResult().split(","));
            Double remaining = 00.0;
            Double subA = 00.0;
            Double suB = 00.0;
            for (int x = 0; x < restAList.size(); x++) {
                ACRGBWSResult restB = this.GETASSETSBYHCF(dataSource, restAList.get(x), datefrom, dateto);
                if (restB.isSuccess()) {
                    List<Assets> assetsList = Arrays.asList(utility.ObjectMapper().readValue(restB.getResult(), Assets[].class));
                    for (int a = 0; a < assetsList.size(); a++) {
                        Ledger Subledger = new Ledger();
                        Subledger.setVoucher(assetsList.get(a).getReceipt());
                        Subledger.setDatetime(assetsList.get(a).getDatereleased());
                        if (assetsList.get(a).getTranchid() != null) {
                            ACRGBWSResult gettranch = fm.ACR_TRANCHWITHID(dataSource, assetsList.get(a).getTranchid());
                            if (gettranch.isSuccess()) {
                                Tranch tranch = utility.ObjectMapper().readValue(gettranch.getResult(), Tranch.class);
                                Subledger.setParticular("PAYMENT OF " + tranch.getTranchtype() + " TRANCH");
                            } else {
                                Subledger.setParticular("N/A");
                            }
                        } else {
                            Subledger.setParticular("N/A");
                        }
                        //==========================================
                        if (assetsList.get(a).getHcfid() != null) {
                            ACRGBWSResult facility = fm.GETFACILITYID(dataSource, assetsList.get(a).getHcfid());
                            if (facility.isSuccess()) {
                                HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                                Subledger.setFacility(hcf.getHcfname());
                            } else {
                                Subledger.setFacility("N/A");
                            }
                        }
                        Subledger.setCredit(assetsList.get(a).getAmount());
                        Double trans = Double.parseDouble(assetsList.get(a).getAmount());
                        remaining += trans;
                        ACRGBWSResult getMB = m.GETMBWITHID(dataSource, accessid);
                        ManagingBoard mb = utility.ObjectMapper().readValue(getMB.getResult(), ManagingBoard.class);
                        if (remaining > 0) {
                            Subledger.setBalance(String.valueOf(remaining));
                            Subledger.setAccount("LIABILITY");
                        } else {
                            Subledger.setBalance(String.valueOf(remaining));
                        }
                        ledgerlist.add(Subledger);

                        //GET LIQUIDATION PART
                        if ((a + 1) < assetsList.size()) {
                            SimpleDateFormat sdf = utility.SimpleDateFormat("MM-dd-yyyy");
                            String convertedDateto = String.valueOf(sdf.format(sdf.parse(assetsList.get(a + 1).getDatereleased()).getTime() - 1));
                            ACRGBWSResult getAmountPayable = this.GETSUMAMOUNTCLAIMS(dataSource,
                                    assetsList.get(a).getHcfid(),
                                    assetsList.get(a).getDatereleased(),
                                    convertedDateto, "TWO");
                            if (getAmountPayable.isSuccess()) {
                                FacilityComputedAmount hcfA = utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount.class);
                                Ledger SubledgerA = new Ledger();
                                SubledgerA.setDatetime(assetsList.get(a).getDatereleased() + " TO " + assetsList.get(a + 1).getDatereleased());
                                ACRGBWSResult facility = fm.GETFACILITYID(dataSource, assetsList.get(a).getHcfid());
                                HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                                SubledgerA.setFacility(hcf.getHcfname());
                                SubledgerA.setParticular("LIQUIDATION OF " + hcf.getHcfname());
                                SubledgerA.setDebit(hcfA.getTotalamount());
                                SubledgerA.setTotalclaims(hcfA.getTotalclaims());
                                remaining -= Double.parseDouble(hcfA.getTotalamount());
                                SubledgerA.setBalance(String.valueOf(remaining));
                                if (remaining > 0) {
                                    SubledgerA.setBalance(String.valueOf(remaining));
                                    SubledgerA.setAccount("LIABILITY");
                                } else {
                                    SubledgerA.setBalance(String.valueOf(remaining));
                                    SubledgerA.setAccount("PAYABLES");
                                }
                                SubledgerA.setLiquidation(hcfA.getTotalamount());
                                ledgerlist.add(SubledgerA);
                            }

                        } else {
                            ACRGBWSResult getAmountPayable = this.GETSUMAMOUNTCLAIMS(dataSource,
                                    assetsList.get(a).getHcfid(),
                                    assetsList.get(a).getDatereleased(),
                                    assetsList.get(a).getDatereleased(), "ONE");
                            Ledger SubledgerB = new Ledger();
                            if (getAmountPayable.isSuccess()) {
                                FacilityComputedAmount hcfA = utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount.class);
                                SubledgerB.setDatetime(assetsList.get(a).getDatereleased());
                                ACRGBWSResult facility = fm.GETFACILITYID(dataSource, assetsList.get(a).getHcfid());
                                HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                                SubledgerB.setFacility(hcf.getHcfname());
                                SubledgerB.setParticular("LIQUIDATION OF " + hcf.getHcfname());
                                SubledgerB.setLiquidation(hcfA.getTotalamount());
                                SubledgerB.setDebit(hcfA.getTotalamount());
                                SubledgerB.setTotalclaims(hcfA.getTotalclaims());
                                remaining -= Double.parseDouble(hcfA.getTotalamount());
                                SubledgerB.setBalance(String.valueOf(remaining));
                                if (remaining > 0) {
                                    SubledgerB.setBalance(String.valueOf(remaining));
                                    SubledgerB.setAccount("LIABILITY");
                                } else {
                                    SubledgerB.setBalance(String.valueOf(remaining));
                                    SubledgerB.setAccount("PAYABLES");
                                }
                                ledgerlist.add(SubledgerB);
                            }

                        }
                    }
                }
            }

//        
            if (ledgerlist.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(ledgerlist));
            } else {
                result.setMessage("N/A");
            }
            //END OF PROCESS LEDGER
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //LEDGER HCF BUDGET UTILIZATION 
//    public ACRGBWSResult HFLedger(final DataSource dataSource, final String datefrom, final String dateto, final String accessid) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setSuccess(false);
//        result.setResult("");
//        ArrayList<Ledger> ledgerlist = new ArrayList<>();
//        try {
//            ACRGBWSResult restA = this.GetConByDate(dataSource, datefrom, dateto, accessid);
//            //START PROCESS LEDGER
//            if (restA.isSuccess()) {
//                List<Contract> conlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Contract[].class));
//                Double remaining = 00.0;
//                for (int x = 0; x < conlist.size(); x++) {
//                    Ledger ledger = new Ledger();
//                    ledger.setDatetime(conlist.get(x).getDatecreated());
//                    ledger.setParticular("Beginning Balance");
//                    ledger.setBalance(conlist.get(x).getAmount());
//                    ledger.setCredit(conlist.get(x).getAmount());
//                    remaining += Double.parseDouble(conlist.get(x).getAmount());//SET BEGINNING BALANCE
//                    ledger.setContractnumber(conlist.get(x).getTranscode());
//                    ledgerlist.add(ledger);
//                    //====================
//                    ACRGBWSResult restB = fm.GETASSETSBYCONID(dataSource, conlist.get(x).getConid());
//                    if (restB.isSuccess()) {
//                        List<Assets> assetsList = Arrays.asList(utility.ObjectMapper().readValue(restB.getResult(), Assets[].class));
//                        for (int a = 0; a < assetsList.size(); a++) {
//                            Ledger Subledger = new Ledger();
//                            Subledger.setDatetime(assetsList.get(a).getDatereleased());
//                            Subledger.setParticular(assetsList.get(a).getTranchid());
//                            Subledger.setFacility(assetsList.get(a).getHcfid());
//                            Subledger.setDebit(assetsList.get(a).getAmount());
//                            Double trans = Double.parseDouble(assetsList.get(a).getAmount());
//                            Subledger.setBalance(String.valueOf(remaining - trans));
//                            remaining -= trans;
//                            Subledger.setContractnumber(assetsList.get(a).getConid());
//                            ledgerlist.add(Subledger);
//                        }
//                    } else {
//                        result.setMessage(restB.getMessage());
//                    }
//                }
//            } else {
//                result.setMessage(restA.getMessage());
//            }
//            //END OF PROCESS LEDGER
//        } catch (IOException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    //GET ASSESTS USING CONTRACT ID
    public ACRGBWSResult GetAssetsUsingConID(final DataSource dataSource, final String conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :assets_type := ACR_GB.ACRGBPKGFUNCTION.GETASSETSBYCONID(:pconid); end;");
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
                ACRGBWSResult getcon = fm.GETCONTRACTCONID(dataSource, resultset.getString("CONID").trim());
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

    //LEDGER HCPN BUDGET UTILIZATION 
//    public ACRGBWSResult HCFLedger(final DataSource dataSource, final String datefrom, final String dateto, final String accessid) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setSuccess(false);
//        result.setResult("");
//        ArrayList<Ledger> ledgerlist = new ArrayList<>();
//        try {
//            ACRGBWSResult restA = this.GetConByDate(dataSource, datefrom, dateto, accessid);
//
//            //START PROCESS LEDGER
//            if (restA.isSuccess()) {
//                List<Contract> conlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Contract[].class));
//                Double remaining = 00.0;
//                for (int x = 0; x < conlist.size(); x++) {
//                    Ledger ledger = new Ledger();
//                    ledger.setDatetime(conlist.get(x).getDatecreated());
//                    ledger.setParticular("Beginning Balance");
//                    remaining += Double.parseDouble(conlist.get(x).getAmount());//SET BEGINNING BALANCE
//                    ledger.setBalance(String.valueOf(remaining));
//                    ledger.setContractnumber(conlist.get(x).getTranscode());
//                    ledger.setCredit(String.valueOf(remaining));
//                    ledgerlist.add(ledger);
//                    //====================
//                    ACRGBWSResult restB = fm.GETASSETSBYCONID(dataSource, conlist.get(x).getConid());
//                    if (restB.isSuccess()) {
//                        List<Assets> assetsList = Arrays.asList(utility.ObjectMapper().readValue(restB.getResult(), Assets[].class));
//                        for (int a = 0; a < assetsList.size(); a++) {
//                            Ledger Subledger = new Ledger();
//                            Subledger.setDatetime(assetsList.get(a).getDatereleased());
//                            if (assetsList.get(a).getTranchid() != null) {
//                                Tranch tranch = utility.ObjectMapper().readValue(assetsList.get(a).getTranchid(), Tranch.class);
//                                Subledger.setParticular(tranch.getTranchtype());
//                            } else {
//                                Subledger.setParticular("N/A");
//                            }
//                            //==========================================
//                            if (assetsList.get(a).getHcfid() != null) {
//                                HealthCareFacility hcf = utility.ObjectMapper().readValue(assetsList.get(a).getHcfid(), HealthCareFacility.class);
//                                Subledger.setFacility(hcf.getHcfname());
//                            } else {
//                                Subledger.setFacility("N/A");
//                            }
//                            Subledger.setDebit(assetsList.get(a).getAmount());
//                            Double trans = Double.parseDouble(assetsList.get(a).getAmount());
//                            Subledger.setBalance(String.valueOf(remaining - trans));
//                            remaining -= trans;
//                            //  Subledger.setContractnumber(assetsList.get(a).getConid());
//                            ledgerlist.add(Subledger);
//                        }
//                    } else {
//                        result.setMessage(restB.getMessage());
//                    }
//                }
//            } else {
//                result.setMessage(restA.getMessage());
//            }
//
//            if (ledgerlist.size() > 0) {
//                result.setMessage("OK");
//                result.setSuccess(true);
//                result.setResult(utility.ObjectMapper().writeValueAsString(ledgerlist));
//            } else {
//                result.setMessage("N/A");
//            }
//            //END OF PROCESS LEDGER
//        } catch (IOException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    public ACRGBWSResult GETASSETSBYHCF(final DataSource dataSource, final String phcfid, final String pdatefrom, final String pdateto) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETASSETSBYHCF(:phcfid,:pdatefrom,:pdateto); end;");
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

    public ACRGBWSResult GETSUMAMOUNTCLAIMS(final DataSource dataSource, final String uaccreno, final String udatefrom, final String udateto, final String ulevel) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        String utags = "GOOD";
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.GETSUMAMOUNTCLAIMS(:ulevel,:uaccreno,:utags,:udatefrom,:udateto); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ulevel", ulevel.toUpperCase());
            statement.setString("uaccreno", uaccreno);
            statement.setString("utags", utags);
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
            final String conid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ContractMethod cm = new ContractMethod();
        ArrayList<Ledger> ledgerlist = new ArrayList<>();
        try {
            Double remaining = 0.00;
            Double begin = 0.00;
            ACRGBWSResult beginning = cm.GETPREVIOUSBALANCE(dataSource, hcpncode);
            if (beginning.isSuccess()) {
                ConBalance conbal = utility.ObjectMapper().readValue(beginning.getResult(), ConBalance.class);

                Ledger ledgersss = new Ledger();
                ledgersss.setDatetime(conbal.getDatecreated());
                ledgersss.setParticular("Beginning Balance");
                ledgersss.setCredit(conbal.getConbalance());
                begin = Double.parseDouble(conbal.getConbalance());
                // ledgersss.setVoucher(assetlist.get(x).getReceipt());
                ledgersss.setBalance(String.valueOf(begin));
                ledgerlist.add(ledgersss);
            }

            ACRGBWSResult restA = fm.GETASSETBYIDANDCONID(dataSource, hcpncode, conid);
            if (restA.isSuccess()) {
                List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                for (int x = 0; x < assetlist.size(); x++) {
                    Ledger ledger = new Ledger();
                    ledger.setDatetime(assetlist.get(x).getDatereleased());
                    if (!assetlist.get(x).getTranchid().isEmpty()) {
                        Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(x).getTranchid(), Tranch.class);
                        ledger.setParticular("PAYMENT OF " + tranch.getTranchtype() + " TRANCH");
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
                    //GET LIQUIDATION PART
                    ACRGBWSResult hcflist = m.GETROLEMULITPLE(dataSource, hcpncode);

                    if (hcflist.isSuccess()) {
                        List<String> hcfresult = Arrays.asList(hcflist.getResult().split(","));
                        //System.out.println(hcfresult);
                        for (int b = 0; b < hcfresult.size(); b++) {
                            if ((x + 1) < assetlist.size()) {
                                SimpleDateFormat sdf = utility.SimpleDateFormat("MM-dd-yyyy");
                                String convertedDateto = String.valueOf(sdf.format(sdf.parse(assetlist.get(x + 1).getDatereleased()).getTime() - 1));
                                ACRGBWSResult getAmountPayable = this.GETSUMAMOUNTCLAIMS(dataSource,
                                        hcfresult.get(b),
                                        assetlist.get(x).getDatereleased(),
                                        convertedDateto, "TWO");
                                if (getAmountPayable.isSuccess()) {
                                    List<FacilityComputedAmount> hcfA = Arrays.asList(utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount[].class));

                                    for (int u = 0; u < hcfA.size(); u++) {
                                        Ledger SubledgerA = new Ledger();
                                        SubledgerA.setDatetime(hcfA.get(u).getDatefiled());
                                        ACRGBWSResult facility = fm.GETFACILITYID(dataSource, hcfresult.get(b));
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

                            } else {
                                ACRGBWSResult getAmountPayable = this.GETSUMAMOUNTCLAIMS(dataSource,
                                        hcfresult.get(b),
                                        assetlist.get(x).getDatereleased(),
                                        assetlist.get(x).getDatereleased(), "ONE");

                                if (getAmountPayable.isSuccess()) {
                                    List<FacilityComputedAmount> hcfA = Arrays.asList(utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount[].class));
                                    for (int u = 0; u < hcfA.size(); u++) {
                                        Ledger SubledgerB = new Ledger();
                                        SubledgerB.setDatetime(hcfA.get(u).getDatefiled());
                                        ACRGBWSResult facility = fm.GETFACILITYID(dataSource, hcfresult.get(b));
                                        HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                                        SubledgerB.setFacility(hcf.getHcfname());
                                        SubledgerB.setParticular("LIQUIDATION OF " + hcf.getHcfname());
                                        SubledgerB.setLiquidation(hcfA.get(u).getTotalamount());
                                        SubledgerB.setDebit(hcfA.get(u).getTotalamount());
                                        SubledgerB.setTotalclaims(hcfA.get(u).getTotalclaims());
                                        remaining -= Double.parseDouble(hcfA.get(u).getTotalamount());
                                        SubledgerB.setBalance(String.valueOf(remaining));
                                        if (remaining > 0) {
                                            SubledgerB.setBalance(String.valueOf(remaining));
                                            SubledgerB.setAccount("LIABILITY");
                                        } else {
                                            SubledgerB.setBalance(String.valueOf(remaining));
                                            SubledgerB.setAccount("PAYABLES");
                                        }
                                        ledgerlist.add(SubledgerB);
                                    }
                                    // FacilityComputedAmount hcfA = utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount.class);

                                }
                            }
                        }
                    }
                    //======================================================
                }
            } else {
                result.setMessage(restA.getMessage());
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

    //PROCESS LEDGER PER CONTRACT UNDER HCPN
    public ACRGBWSResult GETLedgerAllContractHCPN(final DataSource dataSource, final String puserid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Ledger> ledgerlist = new ArrayList<>();
        try {
            ACRGBWSResult restA = m.GETROLE(dataSource, puserid);
            if (restA.isSuccess()) {
                ACRGBWSResult restB = m.GETROLEMULITPLE(dataSource, restA.getResult());
                if (restB.isSuccess()) {
                    Double remaining = 0.00;
                    List<String> restBList = Arrays.asList(restB.getResult().split(","));
                    for (int x = 0; x < restBList.size(); x++) {
                        ACRGBWSResult getassetA = fm.GETASSETSHCFID(dataSource, restBList.get(x));
                        if (getassetA.isSuccess()) {
                            List<Assets> listAssest = Arrays.asList(utility.ObjectMapper().readValue(getassetA.getResult(), Assets[].class));
                            for (int y = 0; y < listAssest.size(); y++) {
                                ManagingBoard newMB = utility.ObjectMapper().readValue(listAssest.get(y).getHcfid(), ManagingBoard.class);
                                Contract newCON = utility.ObjectMapper().readValue(listAssest.get(y).getConid(), Contract.class);
                                ACRGBWSResult AssetsList = fm.GETASSETBYIDANDCONID(dataSource, newMB.getControlnumber(), newCON.getConid());
                                if (AssetsList.isSuccess()) {
                                    List<Assets> listAssestA = Arrays.asList(utility.ObjectMapper().readValue(getassetA.getResult(), Assets[].class));
                                    for (int u = 0; u < listAssestA.size(); u++) {
                                        Ledger ledger = new Ledger();
                                        ledger.setDatetime(listAssest.get(u).getDatereleased());
                                        if (!listAssest.get(u).getTranchid().isEmpty()) {
                                            Tranch tranch = utility.ObjectMapper().readValue(listAssest.get(u).getTranchid(), Tranch.class);
                                            ledger.setParticular("PAYMENT OF " + tranch.getTranchtype() + " TRANCH");
                                        }
                                        ledger.setCredit(listAssest.get(u).getAmount());
                                        remaining += Double.parseDouble(listAssest.get(u).getAmount());//INCREMENT ASSETS
                                        ledger.setVoucher(listAssest.get(u).getReceipt());
                                        ledger.setBalance(String.valueOf(remaining));
                                        //GET HCPN NAME
                                        ACRGBWSResult getMB = m.GETMBWITHID(dataSource, restBList.get(x));
                                        if (getMB.isSuccess()) {
                                            ManagingBoard mb = utility.ObjectMapper().readValue(getMB.getResult(), ManagingBoard.class);
                                            ledger.setAccount(mb.getMbname());
                                        } else {
                                            ledger.setAccount("HCPN");
                                        }
                                        //END OF GET HCPN NAME
                                        ledgerlist.add(ledger);
                                        //====================================================
                                        //GET LIQUIDATION PART
                                        ACRGBWSResult hcflist = m.GETROLEMULITPLE(dataSource, restBList.get(x));
                                        if (hcflist.isSuccess()) {
                                            List<String> hcfresult = Arrays.asList(hcflist.getResult().split(","));
                                            for (int b = 0; b < hcfresult.size(); b++) {
                                                if ((u + 1) < listAssestA.size()) {
                                                    SimpleDateFormat sdf = utility.SimpleDateFormat("MM-dd-yyyy");
                                                    String convertedDateto = String.valueOf(sdf.format(sdf.parse(listAssestA.get(u + 1).getDatereleased()).getTime() - 1));
                                                    ACRGBWSResult getAmountPayable = this.GETSUMAMOUNTCLAIMS(dataSource,
                                                            hcfresult.get(b),
                                                            listAssestA.get(u).getDatereleased(),
                                                            convertedDateto, "TWO");
                                                    if (getAmountPayable.isSuccess()) {
                                                        FacilityComputedAmount hcfA = utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount.class);
                                                        Ledger SubledgerA = new Ledger();
                                                        SubledgerA.setDatetime(listAssestA.get(u).getDatereleased() + " TO " + listAssestA.get(u + 1).getDatereleased());
                                                        ACRGBWSResult facility = fm.GETFACILITYID(dataSource, hcfresult.get(b));
                                                        HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                                                        SubledgerA.setFacility(hcf.getHcfname());
                                                        SubledgerA.setParticular("LIQUIDATION OF " + hcf.getHcfname());
                                                        SubledgerA.setDebit(hcfA.getTotalamount());
                                                        SubledgerA.setTotalclaims(hcfA.getTotalclaims());
                                                        remaining -= Double.parseDouble(hcfA.getTotalamount());
                                                        SubledgerA.setBalance(String.valueOf(remaining));
                                                        if (remaining > 0) {
                                                            SubledgerA.setBalance(String.valueOf(remaining));
                                                            SubledgerA.setAccount("LIABILITY");
                                                        } else {
                                                            SubledgerA.setBalance(String.valueOf(remaining));
                                                            SubledgerA.setAccount("PAYABLES");
                                                        }
                                                        SubledgerA.setLiquidation(hcfA.getTotalamount());
                                                        ledgerlist.add(SubledgerA);
                                                    }
                                                } else {
                                                    ACRGBWSResult getAmountPayable = this.GETSUMAMOUNTCLAIMS(dataSource,
                                                            hcfresult.get(b),
                                                            listAssestA.get(u).getDatereleased(),
                                                            listAssestA.get(u).getDatereleased(), "ONE");
                                                    Ledger SubledgerB = new Ledger();
                                                    if (getAmountPayable.isSuccess()) {
                                                        FacilityComputedAmount hcfA = utility.ObjectMapper().readValue(getAmountPayable.getResult(), FacilityComputedAmount.class);
                                                        SubledgerB.setDatetime(listAssestA.get(u).getDatereleased());
                                                        ACRGBWSResult facility = fm.GETFACILITYID(dataSource, hcfresult.get(b));
                                                        HealthCareFacility hcf = utility.ObjectMapper().readValue(facility.getResult(), HealthCareFacility.class);
                                                        SubledgerB.setFacility(hcf.getHcfname());
                                                        SubledgerB.setParticular("LIQUIDATION OF " + hcf.getHcfname());
                                                        SubledgerB.setLiquidation(hcfA.getTotalamount());
                                                        SubledgerB.setDebit(hcfA.getTotalamount());
                                                        SubledgerB.setTotalclaims(hcfA.getTotalclaims());
                                                        remaining -= Double.parseDouble(hcfA.getTotalamount());
                                                        SubledgerB.setBalance(String.valueOf(remaining));
                                                        if (remaining > 0) {
                                                            SubledgerB.setBalance(String.valueOf(remaining));
                                                            SubledgerB.setAccount("LIABILITY");
                                                        } else {
                                                            SubledgerB.setBalance(String.valueOf(remaining));
                                                            SubledgerB.setAccount("PAYABLES");
                                                        }
                                                        ledgerlist.add(SubledgerB);
                                                    }
                                                }
                                            }
                                        }
                                        //======================================================
                                    }
                                }
                            }
                        }
                    }
                } else {
                    result.setMessage(restB.getMessage());
                }
            } else {
                result.setMessage(restA.getMessage());
            }

            if (ledgerlist.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(ledgerlist));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }

        } catch (ParseException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
