/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Assets;
import acrgb.structure.Contract;
import acrgb.structure.Ledger;
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
            ACRGBWSResult restA = this.GetConByDate(dataSource, datefrom, dateto, accessid);
            //START PROCESS LEDGER
            if (restA.isSuccess()) {
                List<Contract> conlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Contract[].class));
                Double remaining = 00.0;
                for (int x = 0; x < conlist.size(); x++) {
                    Ledger ledger = new Ledger();
                    ledger.setDatetime(conlist.get(x).getDatecreated());
                    ledger.setParticular("Beginning Balance");
                    ledger.setBalance(conlist.get(x).getAmount());
                    remaining += Double.parseDouble(conlist.get(x).getAmount());//SET BEGINNING BALANCE
                    ledger.setContractnumber(conlist.get(x).getTranscode());
                    ledgerlist.add(ledger);
                    //====================
                    ACRGBWSResult restB = fm.GETASSETSBYCONID(dataSource, conlist.get(x).getConid());
                    if (restB.isSuccess()) {
                        List<Assets> assetsList = Arrays.asList(utility.ObjectMapper().readValue(restB.getResult(), Assets[].class));
                        for (int a = 0; a < assetsList.size(); a++) {
                            Ledger Subledger = new Ledger();
                            Subledger.setDatetime(assetsList.get(a).getDatereleased());
                            Subledger.setParticular(assetsList.get(a).getTranchid());
                            Subledger.setFacility(assetsList.get(a).getHcfid());
                            Subledger.setFundreleased(assetsList.get(a).getAmount());
                            Double trans = Double.parseDouble(assetsList.get(a).getAmount());
                            Subledger.setBalance(String.valueOf(remaining - trans));
                            remaining -= trans;
                            Subledger.setContractnumber(assetsList.get(a).getConid());
                            ledgerlist.add(Subledger);
                        }
                    } else {
                        result.setMessage(restB.getMessage());
                    }
                }

            } else {
                result.setMessage(restA.getMessage());
            }
            //END OF PROCESS LEDGER
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    //LEDGER HCF BUDGET UTILIZATION 
    public ACRGBWSResult HFLedger(final DataSource dataSource, final String datefrom, final String dateto, final String accessid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        ArrayList<Ledger> ledgerlist = new ArrayList<>();
        try {
            ACRGBWSResult restA = this.GetConByDate(dataSource, datefrom, dateto, accessid);
            //START PROCESS LEDGER
            if (restA.isSuccess()) {
                List<Contract> conlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Contract[].class));
                Double remaining = 00.0;
                for (int x = 0; x < conlist.size(); x++) {
                    Ledger ledger = new Ledger();
                    ledger.setDatetime(conlist.get(x).getDatecreated());
                    ledger.setParticular("Beginning Balance");
                    ledger.setBalance(conlist.get(x).getAmount());
                    remaining += Double.parseDouble(conlist.get(x).getAmount());//SET BEGINNING BALANCE
                    ledger.setContractnumber(conlist.get(x).getTranscode());
                    ledgerlist.add(ledger);
                    //====================
                    ACRGBWSResult restB = fm.GETASSETSBYCONID(dataSource, conlist.get(x).getConid());
                    if (restB.isSuccess()) {
                        List<Assets> assetsList = Arrays.asList(utility.ObjectMapper().readValue(restB.getResult(), Assets[].class));
                        for (int a = 0; a < assetsList.size(); a++) {
                            Ledger Subledger = new Ledger();
                            Subledger.setDatetime(assetsList.get(a).getDatereleased());
                            Subledger.setParticular(assetsList.get(a).getTranchid());
                            Subledger.setFacility(assetsList.get(a).getHcfid());
                            Subledger.setFundreleased(assetsList.get(a).getAmount());
                            Double trans = Double.parseDouble(assetsList.get(a).getAmount());
                            Subledger.setBalance(String.valueOf(remaining - trans));
                            remaining -= trans;
                            Subledger.setContractnumber(assetsList.get(a).getConid());
                            ledgerlist.add(Subledger);
                        }
                    } else {
                        result.setMessage(restB.getMessage());
                    }
                }

            } else {
                result.setMessage(restA.getMessage());
            }
            //END OF PROCESS LEDGER
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT BY DATE AND PER CONTROL NUMBER
    public ACRGBWSResult GetConByDate(final DataSource dataSource, final String datefrom, final String dateto, final String pan) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");//CHANGE TO WHILE
        result.setSuccess(false);
        result.setResult("");
        ArrayList<Contract> contractList = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETCONBYDATE(:pan,:pdatefrom,:pdateto); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pan", pan);
            statement.setDate("pdatefrom", (Date) new Date(utility.StringToDate(datefrom).getTime()));
            statement.setDate("pdateto", (Date) new Date(utility.StringToDate(dateto).getTime()));
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Contract contract = new Contract();
                contract.setConid(resultset.getString("CONID"));
                ACRGBWSResult facility = m.GETMBWITHID(dataSource, resultset.getString("HCFID"));
                if (facility.isSuccess()) {
                    contract.setHcfid(facility.getResult());
                } else {
                    contract.setHcfid("NOT DATA FOUND");
                }
                //END OF GET NETWORK FULL DETAILS
                contract.setAmount(resultset.getString("AMOUNT"));
                contract.setStats(resultset.getString("STATS"));
                ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    if (!creator.getResult().isEmpty()) {
                        contract.setCreatedby(creator.getResult());
                    } else {
                        contract.setCreatedby(creator.getMessage());
                    }
                } else {
                    contract.setCreatedby("DATA NOT FOUND");
                }
                contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                contract.setDatefrom(dateformat.format(resultset.getDate("DATEFROM")));//resultset.getString("DATECOVERED"));
                contract.setDateto(dateformat.format(resultset.getDate("DATETO")));//resultset.getString("DATECOVERED"));
                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                contract.setTranscode(resultset.getString("TRANSCODE"));
                contract.setEnddate(resultset.getString("ENDDATE"));
                contractList.add(contract);

            }
            if (!contractList.isEmpty()) {
                result.setMessage("OK");
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
                result.setSuccess(true);
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (SQLException | ParseException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(LedgerMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

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
                    if (!creator.getResult().isEmpty()) {
                        UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                        assets.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                    } else {
                        assets.setCreatedby(creator.getMessage());
                    }
                } else {
                    assets.setCreatedby("DATA NOT FOUND");
                }
                assets.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                ACRGBWSResult getcon = fm.GETCONTRACTCONID(dataSource, resultset.getString("CONID").trim());
                if (getcon.isSuccess()) {
                    if (!getcon.getResult().isEmpty()) {
                        assets.setConid(getcon.getResult());
                    } else {
                        assets.setConid(getcon.getMessage());
                    }
                } else {
                    assets.setCreatedby("DATA NOT FOUND");
                }
                assets.setStatus(resultset.getString("STATS"));
                listassets.add(assets);
            }
            if (!listassets.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(listassets));
            } else {
                result.setMessage("NO DATA FOUND");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
