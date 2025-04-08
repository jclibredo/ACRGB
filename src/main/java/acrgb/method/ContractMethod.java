/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Assets;
import acrgb.structure.CaseRate;
import acrgb.structure.ConBalance;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.ManagingBoard;
import acrgb.structure.NclaimsData;
import acrgb.structure.Tranch;
import acrgb.structure.UserInfo;
import acrgb.structure.UserRoleIndex;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
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
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class ContractMethod {

    public ContractMethod() {
    }
    private final Utility utility = new Utility();
    private final SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");

//    public ACRGBWSResult GetAllContract(final DataSource dataSource, final String phcfcode, final String tags) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setSuccess(false);
//        result.setResult("");
//        try (Connection connection = dataSource.getConnection()) {
//            ArrayList<Contract> contractList = new ArrayList<>();
//            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
//            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
//            statement.setString("tags", "INACTIVE");
//            statement.setString("pfchid", phcfcode.trim());
//            statement.execute();
//            ResultSet resultset = (ResultSet) statement.getObject("v_result");
//            switch (tags.toUpperCase()) {
//                case "HCPNPRO": {
//                    while (resultset.next()) {
//                        Contract contract = new Contract();
//                        contract.setConid(resultset.getString("CONID"));
//                        contract.setAmount(resultset.getString("AMOUNT"));
//                        contract.setStats(resultset.getString("STATS"));
//                        contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
//                        ACRGBWSResult getcondateA = this.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
//                        if (getcondateA.isSuccess()) {
//                            contract.setContractdate(getcondateA.getResult());
//                        }
//                        contract.setTranscode(resultset.getString("TRANSCODE"));
//                        contract.setBaseamount(resultset.getString("BASEAMOUNT"));
//                        contract.setHcfid(resultset.getString("HCFID"));
//                        contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
//                        contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
//                        contract.setSb(resultset.getString("SB"));
//                        contract.setAddamount(resultset.getString("ADDAMOUNT"));
//                        contract.setQuarter(resultset.getString("QUARTER"));
//                        if (resultset.getTimestamp("ENDDATE") != null) {
//                            contract.setEnddate(dateformat.format(resultset.getTimestamp("ENDDATE")));
//                        } else {
//                            contract.setEnddate("");
//                        }
//                        contractList.add(contract);
//                    }
//                    if (contractList.isEmpty()) {
//                        result.setSuccess(true);
//                        result.setMessage("OK");
//                        result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
//                    } else {
//                        result.setMessage("N/A");
//                    }
//                    break;
//                }
//                case "HCPNPHIC": {
//                    while (resultset.next()) {
//                        Contract contract = new Contract();
//                        contract.setConid(resultset.getString("CONID"));
//                        contract.setAmount(resultset.getString("AMOUNT"));
//                        contract.setStats(resultset.getString("STATS"));
//                        contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
//                        ACRGBWSResult getcondateA = this.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
//                        if (getcondateA.isSuccess()) {
//                            contract.setContractdate(getcondateA.getResult());
//                        }
//                        contract.setTranscode(resultset.getString("TRANSCODE"));
//                        contract.setBaseamount(resultset.getString("BASEAMOUNT"));
//                        contract.setHcfid(resultset.getString("HCFID"));
//                        contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
//                        contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
//                        contract.setSb(resultset.getString("SB"));
//                        contract.setAddamount(resultset.getString("ADDAMOUNT"));
//                        contract.setQuarter(resultset.getString("QUARTER"));
//                        if (resultset.getTimestamp("ENDDATE") != null) {
//                            contract.setEnddate(dateformat.format(resultset.getTimestamp("ENDDATE")));
//                        } else {
//                            contract.setEnddate("");
//                        }
//                        contractList.add(contract);
//                    }
//
//                    if (contractList.isEmpty()) {
//                        result.setSuccess(true);
//                        result.setMessage("OK");
//                        result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
//                    } else {
//                        result.setMessage("N/A");
//                    }
//                    break;
//                }
//            }
//
//        } catch (SQLException | IOException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//
//    }
    //GET APPELLATE CONTROL
    public ACRGBWSResult GETCONTRACT(
            final DataSource dataSource,
            final String tags,
            final String hcfid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.toUpperCase());
            statement.setString("pfchid", hcfid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Contract contract = new Contract();
                contract.setConid(resultset.getString("CONID"));
                contract.setHcfid(resultset.getString("HCFID"));
                //END OF GET NETWORK FULL DETAILS
                contract.setAmount(resultset.getString("AMOUNT"));
                contract.setStats(resultset.getString("STATS"));
                ACRGBWSResult creator = new FetchMethods().GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    contract.setCreatedby(creator.getResult());
                } else {
                    contract.setCreatedby(creator.getMessage());
                }
                contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                ACRGBWSResult getcondateA = this.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                if (getcondateA.isSuccess()) {
                    contract.setContractdate(getcondateA.getResult());
                }
                contract.setTranscode(resultset.getString("TRANSCODE"));
                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                contract.setSb(resultset.getString("SB"));
                contract.setAddamount(resultset.getString("ADDAMOUNT"));
                contract.setQuarter(resultset.getString("QUARTER"));
                contract.setEnddate(resultset.getString("ENDDATE") == null
                        || resultset.getString("ENDDATE").isEmpty()
                        || resultset.getString("ENDDATE").equals("") ? "" : dateformat.format(resultset.getTimestamp("ENDDATE")));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contract));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET APPELLATE CONTROL
//    public ACRGBWSResult GETCONTRACTList(final DataSource dataSource, final String tags, final String hcfid) {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ArrayList<Contract> contractList = new ArrayList<>();
//        try (Connection connection = dataSource.getConnection()) {
//            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
//            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
//            statement.setString("tags", tags.toUpperCase().trim());
//            statement.setString("pfchid", hcfid.trim());
//            statement.execute();
//            ResultSet resultset = (ResultSet) statement.getObject("v_result");
//            while (resultset.next()) {
//                Contract contract = new Contract();
//                contract.setConid(resultset.getString("CONID"));
//                contract.setHcfid(resultset.getString("HCFID"));
//                //END OF GET NETWORK FULL DETAILS
//                contract.setAmount(resultset.getString("AMOUNT"));
//                contract.setStats(resultset.getString("STATS"));
//                ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
//                if (creator.isSuccess()) {
//                    contract.setCreatedby(creator.getResult());
//                } else {
//                    contract.setCreatedby(creator.getMessage());
//                }
//                contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
//                ACRGBWSResult getcondateA = this.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
//                if (getcondateA.isSuccess()) {
//                    contract.setContractdate(getcondateA.getResult());
//                }
//                contract.setTranscode(resultset.getString("TRANSCODE"));
//                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
//                contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
//                contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
//                contract.setSb(resultset.getString("SB"));
//                contract.setAddamount(resultset.getString("ADDAMOUNT"));
//                contract.setQuarter(resultset.getString("QUARTER"));
//                contract.setQuarter(resultset.getString("QUARTER"));
//                if (resultset.getTimestamp("ENDDATE") != null) {
//                    contract.setEnddate(dateformat.format(resultset.getTimestamp("ENDDATE")));
//                } else {
//                    contract.setEnddate("");
//                }
//                contractList.add(contract);
//            }
//            if (contractList.size() > 0) {
//                result.setMessage("OK");
//                result.setSuccess(true);
//                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
//            } else {
//                result.setMessage("N/A");
//            }
//        } catch (SQLException | IOException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    //GET APPELLATE CONTROL
    public ACRGBWSResult GETCONDATE(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETCONDATE(:tags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.toUpperCase().trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (tags.toUpperCase().equals("ACTIVE")) {
                ArrayList<ContractDate> contractdatelist = new ArrayList<>();
                while (resultset.next()) {
                    ContractDate contractdate = new ContractDate();
                    contractdate.setCondateid(resultset.getString("CONDATEID"));
                    ACRGBWSResult creator = new FetchMethods().GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                    if (creator.isSuccess()) {
                        contractdate.setCreatedby(creator.getResult());
                    } else {
                        contractdate.setCreatedby(creator.getMessage());
                    }
                    contractdate.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                    contractdate.setDatefrom(dateformat.format(resultset.getTimestamp("DATEFROM")));//resultset.getString("DATECOVERED"));
                    contractdate.setDateto(dateformat.format(resultset.getTimestamp("DATETO")));//resultset.getString("DATECOVERED"));
                    contractdate.setStatus(resultset.getString("STATUS"));
                    //GET CONTRACT USING CONID
                    ACRGBWSResult getAccountbyConid = this.GETCONTRACTBYCONDATEID(dataSource, resultset.getString("CONDATEID"));
                    if (getAccountbyConid.isSuccess()) {
                        List<Contract> contratList = Arrays.asList(utility.ObjectMapper().readValue(getAccountbyConid.getMessage(), Contract[].class));
                        contractdate.setAccountunder(utility.ObjectMapper().writeValueAsString(contratList));
                    } else {
                        contractdate.setAccountunder(getAccountbyConid.getMessage());
                    }
                    contractdatelist.add(contractdate);
                }
                if (!contractdatelist.isEmpty()) {
                    result.setMessage("OK");
                    result.setSuccess(true);
                    result.setResult(utility.ObjectMapper().writeValueAsString(contractdatelist));
                } else {
                    result.setMessage("N/A");
                }
            } else {
                ArrayList<ContractDate> contractdatelist = new ArrayList<>();
                while (resultset.next()) {
                    ContractDate contractdate = new ContractDate();
                    contractdate.setCondateid(resultset.getString("CONDATEID"));
                    ACRGBWSResult creator = new FetchMethods().GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                    if (creator.isSuccess()) {
                        contractdate.setCreatedby(creator.getResult());
                    } else {
                        contractdate.setCreatedby(creator.getMessage());
                    }
                    contractdate.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                    contractdate.setDatefrom(dateformat.format(resultset.getTimestamp("DATEFROM")));//resultset.getString("DATECOVERED"));
                    contractdate.setDateto(dateformat.format(resultset.getTimestamp("DATETO")));//resultset.getString("DATECOVERED"));
                    contractdate.setStatus(resultset.getString("STATUS"));
                    contractdatelist.add(contractdate);
                }
                if (!contractdatelist.isEmpty()) {
                    result.setMessage("OK");
                    result.setSuccess(true);
                    result.setResult(utility.ObjectMapper().writeValueAsString(contractdatelist));
                } else {
                    result.setMessage("N/A");
                }

            }

        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET APPELLATE CONTROL
    public ACRGBWSResult GETCONDATEBYID(final DataSource dataSource, final String ucondateid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETCONDATEBYID(:ucondateid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ucondateid", ucondateid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                ContractDate contractdate = new ContractDate();
                contractdate.setCondateid(resultset.getString("CONDATEID"));
                contractdate.setCreatedby(resultset.getString("CREATEDBY"));
                contractdate.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                contractdate.setDatefrom(dateformat.format(resultset.getTimestamp("DATEFROM")));//resultset.getString("DATECOVERED"));
                contractdate.setDateto(dateformat.format(resultset.getTimestamp("DATETO")));//resultset.getString("DATECOVERED"));
                contractdate.setStatus(resultset.getString("STATUS"));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractdate));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT BY DATECOVERED
    public ACRGBWSResult GETCONTRACTBYCONDATEID(final DataSource dataSource, final String ucondateid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETCONTRACTBYCONDATEID(:ucondateid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ucondateid", ucondateid.trim());
            statement.execute();
            ArrayList<String> conidlist = new ArrayList<>();
            ArrayList<Contract> contractlist = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                conidlist.add(resultset.getString("CONID"));
                Contract contract = new Contract();
                contract.setAmount(resultset.getString("AMOUNT") == null
                        || resultset.getString("AMOUNT").isEmpty()
                        || resultset.getString("AMOUNT").equals("") ? "0" : resultset.getString("AMOUNT"));
                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                contract.setConid(resultset.getString("CONID"));
                ACRGBWSResult getcondateA = this.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE").trim());
                if (getcondateA.isSuccess()) {
                    contract.setContractdate(getcondateA.getResult());
                }
                contract.setCreatedby(resultset.getString("CREATEDBY"));
                contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                contract.setEnddate(resultset.getString("ENDDATE") == null
                        || resultset.getString("ENDDATE").equals("")
                        || resultset.getString("ENDDATE").isEmpty() ? "" : dateformat.format(resultset.getTimestamp("ENDDATE")));
                //GET MANAGINGBOARD NAME
                ACRGBWSResult GetMB = new Methods().GETMBWITHID(dataSource, resultset.getString("HCFID"));
                if (GetMB.isSuccess()) {
                    ManagingBoard mb = utility.ObjectMapper().readValue(GetMB.getResult(), ManagingBoard.class);
                    contract.setHcfid(mb.getMbname());
                } else {
                    ACRGBWSResult GetHCI = new FetchMethods().GETFACILITYID(dataSource, resultset.getString("HCFID"));
                    if (GetHCI.isSuccess()) {
                        HealthCareFacility hciname = utility.ObjectMapper().readValue(GetHCI.getResult(), HealthCareFacility.class);
                        contract.setHcfid(hciname.getHcfname());
                    } else {
                        contract.setHcfid(resultset.getString("HCFID"));
                    }
                }
                contract.setStats(resultset.getString("STATS"));
                contract.setTranscode(resultset.getString("TRANSCODE"));
                contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                contract.setAddamount(resultset.getString("ADDAMOUNT"));
                contract.setSb(resultset.getString("SB"));
                contract.setQuarter(resultset.getString("QUARTER"));
                contractlist.add(contract);
            }
            if (!conidlist.isEmpty()) {
                result.setMessage(utility.ObjectMapper().writeValueAsString(contractlist));
                result.setSuccess(true);
                result.setResult(String.join(",", conidlist));
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//    //GET CONTRACT BY DATECOVERED
    public ACRGBWSResult GETPREVIOUSBALANCE(final DataSource dataSource,
            final String paccount,
            final String pconid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETPREVIOUSBALANCE(:paccount,:pconid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("paccount", paccount.trim());
            statement.setString("pconid", pconid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                //------------------OBJECT MAPPING ----------------------
                ConBalance conbal = new ConBalance();
                conbal.setAccount(resultset.getString("ACCOUNT"));
                conbal.setBooknum(resultset.getString("BOOKNUM"));
                conbal.setConamount(resultset.getString("CONAMOUNT") == null
                        || resultset.getString("CONAMOUNT").isEmpty()
                        || resultset.getString("CONAMOUNT").equals("") ? "" : resultset.getString("CONAMOUNT")); //CONTRACT AMOUNT
                conbal.setConbalance(resultset.getString("CONBALANCE"));//CONTRACT REMAINING BALANCE
                //GETCONDATEBYID
                ACRGBWSResult getCondate = this.GETCONDATEBYID(dataSource, resultset.getString("CONID"));
                if (getCondate.isSuccess()) {
                    conbal.setCondateid(getCondate.getResult());
                }
                conbal.setConid(resultset.getString("CONID"));
                conbal.setConutilized(resultset.getString("CONUTILIZED"));
                conbal.setDatecreated(resultset.getString("DATECREATED") == null
                        || resultset.getString("DATECREATED").isEmpty()
                        || resultset.getString("DATECREATED").equals("") ? "" : dateformat.format(resultset.getTimestamp("DATECREATED")));
                conbal.setStatus(resultset.getString("STATUS"));
                //------------------  END OF OBJECT MAPPING ----------------------
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(conbal));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT OF FACILITY USING ACCOUNT USERID
    public ACRGBWSResult GETCONTRACTOFFACILITY(final DataSource dataSource, final String tags, final String userid, final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            ACRGBWSResult GetRole = new Methods().GETROLE(dataSource, userid, "ACTIVE");
            if (GetRole.isSuccess()) {
                //GETCONTRACT
                ACRGBWSResult GetFacilityContract = this.GETCONTRACTWITHOPENSTATE(dataSource, tags.trim().toUpperCase(), GetRole.getResult().trim(), ustate.trim().toUpperCase());
                if (GetFacilityContract.isSuccess()) {
                    Contract con = utility.ObjectMapper().readValue(GetFacilityContract.getResult(), Contract.class);
                    if (con.getContractdate() != null) {
                        Contract contract = new Contract();
                        //--------------------------------------
                        ACRGBWSResult GetFacility = new FetchMethods().GETFACILITYID(dataSource, userid);
                        if (GetFacility.isSuccess()) {
                            contract.setHcfid(GetFacility.getResult());
                        }
                        //END OF GET NETWORK FULL DETAILS
                        contract.setAmount(con.getAmount());
                        contract.setStats(con.getStats());
                        contract.setCreatedby(con.getCreatedby());
                        contract.setDatecreated(con.getDatecreated());//resultset.getString("DATECREATED"));
                        contract.setContractdate(con.getContractdate());
                        contract.setTranscode(con.getTranscode());
                        contract.setBaseamount(con.getBaseamount());
                        contract.setComittedClaimsVol(con.getComittedClaimsVol());
                        contract.setComputedClaimsVol(con.getComputedClaimsVol());
                        contract.setSb(con.getSb());
                        contract.setAddamount(con.getAddamount());
                        //=============================================
                        ContractDate condate = utility.ObjectMapper().readValue(con.getContractdate(), ContractDate.class);
                        int numberofclaims = 0;
                        int tranches = 0;
                        double trancheamount = 0.00;
                        double percentageA = 0.00;
                        double percentageB = 0.00;
                        double claimsamount = 0.00;
                        ACRGBWSResult restA = new FetchMethods().GETASSETBYIDANDCONID(dataSource, con.getHcfid(), con.getConid().trim(), tags);
                        if (restA.isSuccess()) {
                            List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                            for (int g = 0; g < assetlist.size(); g++) {
                                if (assetlist.get(g).getPreviousbalance() != null) {
                                    Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                    switch (tranch.getTranchtype()) {
                                        case "1ST": {
                                            trancheamount += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                            trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                            tranches++;
                                            break;
                                        }
                                        case "1STFINAL": {
                                            trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
//                                            tranches--;
                                            break;
                                        }
                                        default: {
                                            trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                            tranches++;
                                            break;
                                        }
                                    }
                                } else {
                                    trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                    tranches++;
                                }
                            }
                        }
                        ArrayList<HealthCareFacility> testHCIlist = new ArrayList<>();
                        ACRGBWSResult getMainAccre = new GetHCFMultiplePMCCNO().GETFACILITYBYMAINACCRE(dataSource, con.getHcfid().trim());
                        if (getMainAccre.isSuccess()) {
                            testHCIlist.addAll(Arrays.asList(utility.ObjectMapper().readValue(getMainAccre.getResult(), HealthCareFacility[].class)));
                        } else if (new FetchMethods().GETFACILITYID(dataSource, con.getHcfid().trim()).isSuccess()) {
                            testHCIlist.add(utility.ObjectMapper().readValue(new FetchMethods().GETFACILITYID(dataSource, con.getHcfid().trim()).getResult(), HealthCareFacility.class));
                        }
                        if (testHCIlist.size() > 0) {
                            for (int yu = 0; yu < testHCIlist.size(); yu++) {
                                //------------------------------------------------------------------------
                                ACRGBWSResult sumresult = new FetchMethods().GETNCLAIMS(dataSource, testHCIlist.get(yu).getHcfcode().trim(), "G", condate.getDatefrom(), utility.AddMinusDaysDate(condate.getDateto(), "60"), "CURRENTSTATUS");
                                if (sumresult.isSuccess()) {
                                    List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                                    for (int i = 0; i < nclaimsdata.size(); i++) {
                                        int countLedger = 0;
                                        if (nclaimsdata.get(i).getRefiledate().isEmpty() || nclaimsdata.get(i).getRefiledate().equals("") || nclaimsdata.get(i).getRefiledate() == null) {
                                            if (con.getEnddate().isEmpty() || con.getEnddate() == null || con.getEnddate().equals("")) {
                                                if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                    countLedger++;
                                                }
                                            } else {
                                                if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(con.getEnddate().trim(), "60"))) <= 0) {
                                                    countLedger++;
                                                }
                                            }
                                        } else {
                                            if (con.getEnddate().isEmpty() || con.getEnddate() == null || con.getEnddate().equals("")) {
                                                if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                    countLedger++;
                                                }
                                            } else {
                                                if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(con.getEnddate().trim(), "60"))) <= 0) {
                                                    countLedger++;
                                                }
                                            }
                                        }
                                        if (countLedger > 0) {
                                            numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                            claimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                        }
                                    }
                                }
                            }
                        }

                        //TRACNHE AMOUNT TO TOTAL CLAIMS AMOUNT
                        double sumB = (claimsamount / trancheamount) * 100;
                        if (sumB > 100) {
                            Double negvalue = 100 - sumB;
                            percentageB += negvalue;
                        } else {
                            percentageB += sumB;
                        }
                        //CONTRACT AMOUNT TO TOTAL TRANCHE RELEASED
                        double sumA = (trancheamount / Double.parseDouble(con.getAmount())) * 100;
                        if (sumA > 100) {
                            Double negvalue = 100 - sumA;
                            percentageA += negvalue;
                        } else {
                            percentageA += sumA;
                        }
                        contract.setTotalamountrecieved(String.valueOf(trancheamount));
                        contract.setTotalclaims(String.valueOf(numberofclaims));
                        contract.setTraches(String.valueOf(tranches));
                        contract.setPercentage(String.valueOf(percentageA));
                        contract.setTotaltrancheamount(String.valueOf(trancheamount));
                        contract.setTotalclaimsamount(String.valueOf(claimsamount));
                        contract.setTotalclaimspercentage(String.valueOf(percentageB));
                        result.setMessage("OK");
                        result.setSuccess(true);
                        result.setResult(utility.ObjectMapper().writeValueAsString(contract));
                    }
                } else {
                    result.setMessage(GetFacilityContract.getMessage());
                }
            } else {
                result.setMessage(new Methods().GETROLE(dataSource, userid, tags).getMessage());
            }
        } catch (IOException | ParseException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT OF HCPN USING ACCOUNT USERID
    public ACRGBWSResult GETCONTRACTOFHCPN(final DataSource dataSource, final String tags, final String userid, final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            ACRGBWSResult GetRole = new Methods().GETROLE(dataSource, userid, "ACTIVE");
            if (GetRole.isSuccess()) {
                //GETCONTRACT
                ACRGBWSResult getHCPNContract = this.GETCONTRACTWITHOPENSTATE(dataSource, tags.trim().toUpperCase(), GetRole.getResult().trim(), ustate.trim().toUpperCase());
//                ACRGBWSResult getHCPNContract = this.GETCONTRACT(dataSource, tags.trim(), GetRole.getResult().trim());
                if (getHCPNContract.isSuccess()) {
                    Contract con = utility.ObjectMapper().readValue(getHCPNContract.getResult(), Contract.class);
                    if (con.getContractdate() != null) {
                        Contract contract = new Contract();
                        ACRGBWSResult GetHCPN = new Methods().GETMBWITHID(dataSource, GetRole.getResult());
                        if (GetHCPN.isSuccess()) {
                            contract.setHcfid(GetHCPN.getResult());
                        }
                        //END OF GET NETWORK FULL DETAILS
                        contract.setAmount(con.getAmount());
                        contract.setStats(con.getStats());
                        contract.setCreatedby(con.getCreatedby());
                        contract.setDatecreated(con.getDatecreated());//resultset.getString("DATECREATED"));
                        contract.setContractdate(con.getContractdate());
                        contract.setTranscode(con.getTranscode());
                        contract.setBaseamount(con.getBaseamount());
                        contract.setComittedClaimsVol(con.getComittedClaimsVol());
                        contract.setComputedClaimsVol(con.getComputedClaimsVol());
                        contract.setSb(con.getSb());
                        contract.setAddamount(con.getAddamount());
                        //=============================================
                        ContractDate condate = utility.ObjectMapper().readValue(con.getContractdate(), ContractDate.class);
                        int numberofclaims = 0;
                        double percentageA = 0.00;
                        double percentageB = 0.00;
                        int tranches = 0;
                        double trancheamount = 0.00;
                        double claimsamount = 0.00;
                        double totalrecievedamount = 0.00;
                        //GET ALL FACILITY UNDER HCPN
                        ACRGBWSResult GetAccessList = new Methods().GETROLEMULITPLE(dataSource, GetRole.getResult().trim(), tags.trim());
                        if (GetAccessList.isSuccess()) {
                            List<String> HciList = Arrays.asList(GetAccessList.getResult().trim().split(","));
                            for (int x = 0; x < HciList.size(); x++) {
                                ACRGBWSResult getHCIContract = this.GETCONTRACT(dataSource, tags, HciList.get(x).trim());
                                if (getHCIContract.isSuccess()) {
                                    Contract hcinCon = utility.ObjectMapper().readValue(getHCIContract.getResult(), Contract.class);
                                    ACRGBWSResult restA = new FetchMethods().GETASSETBYIDANDCONID(dataSource, HciList.get(x).trim(), hcinCon.getConid().trim(), tags);
                                    if (restA.isSuccess()) {
                                        List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                                        for (int g = 0; g < assetlist.size(); g++) {
                                            if (assetlist.get(g).getPreviousbalance() != null) {
                                                Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                                switch (tranch.getTranchtype()) {
                                                    case "1ST": {
                                                        trancheamount += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                                        trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                        tranches++;
                                                        break;
                                                    }
                                                    case "1STFINAL": {
                                                        trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
//                                                        tranches--;
                                                        break;
                                                    }
                                                    default: {
                                                        trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                        tranches++;
                                                        break;
                                                    }
                                                }
                                            } else {
                                                trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                tranches++;
                                            }
                                        }
                                    }
                                    ArrayList<HealthCareFacility> testHCIlist = new ArrayList<>();
                                    ACRGBWSResult getMainAccre = new GetHCFMultiplePMCCNO().GETFACILITYBYMAINACCRE(dataSource, HciList.get(x).trim());
                                    if (getMainAccre.isSuccess()) {
                                        testHCIlist.addAll(Arrays.asList(utility.ObjectMapper().readValue(getMainAccre.getResult(), HealthCareFacility[].class)));
                                    } else if (new FetchMethods().GETFACILITYID(dataSource, HciList.get(x).trim()).isSuccess()) {
                                        testHCIlist.add(utility.ObjectMapper().readValue(new FetchMethods().GETFACILITYID(dataSource, HciList.get(x).trim()).getResult(), HealthCareFacility.class));
                                    }
                                    if (testHCIlist.size() > 0) {
                                        for (int yu = 0; yu < testHCIlist.size(); yu++) {
                                            //------------------------------------------------------------------------
                                            ACRGBWSResult sumresult = new FetchMethods().GETNCLAIMS(dataSource, testHCIlist.get(yu).getHcfcode().trim(), "G", condate.getDatefrom(), utility.AddMinusDaysDate(condate.getDateto(), "60"), "CURRENTSTATUS");
                                            if (sumresult.isSuccess()) {
                                                List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                                                for (int i = 0; i < nclaimsdata.size(); i++) {
                                                    int countLedger = 0;
                                                    if (nclaimsdata.get(i).getRefiledate().isEmpty() || nclaimsdata.get(i).getRefiledate().equals("") || nclaimsdata.get(i).getRefiledate() == null) {
                                                        if (hcinCon.getEnddate().isEmpty() || hcinCon.getEnddate().equals("") || hcinCon.getEnddate() == null) {
                                                            if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                                countLedger++;
                                                            }
                                                        } else {
                                                            if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(con.getEnddate().trim(), "60"))) <= 0) {
                                                                countLedger++;
                                                            }
                                                        }
                                                    } else {
                                                        if (hcinCon.getEnddate().isEmpty() || hcinCon.getEnddate().equals("") || hcinCon.getEnddate() == null) {
                                                            if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                                countLedger++;
                                                            }
                                                        } else {
                                                            if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(con.getEnddate().trim(), "60"))) <= 0) {
                                                                countLedger++;
                                                            }
                                                        }
                                                    }
                                                    if (countLedger > 0) {
                                                        numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                        claimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ACRGBWSResult restA = new FetchMethods().GETASSETBYIDANDCONID(dataSource, con.getHcfid().trim(), con.getConid().trim(), tags);    //utility.AddMinusDaysDate(con.getEnddate().trim(), "60")
                        if (restA.isSuccess()) {
                            List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                            for (int g = 0; g < assetlist.size(); g++) {
                                if (assetlist.get(g).getPreviousbalance() != null) {
                                    Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                    switch (tranch.getTranchtype()) {
                                        case "1ST": {
                                            totalrecievedamount += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                            totalrecievedamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                            break;
                                        }
                                        case "1STFINAL": {
                                            totalrecievedamount -= Double.parseDouble(assetlist.get(g).getReleasedamount());
                                            break;
                                        }
                                        default: {
                                            totalrecievedamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                            break;
                                        }
                                    }
                                } else {
                                    totalrecievedamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                }
                            }
                        }

                        double recievedamount = Double.parseDouble(con.getAmount());
                        double sumB = (claimsamount / trancheamount) * 100;
                        if (sumB > 100) {
                            Double negvalue = 100 - sumB;
                            percentageB += negvalue;
                        } else {
                            percentageB += sumB;
                        }
                        double sumA = (trancheamount / recievedamount) * 100;
                        if (sumA > 100) {
                            Double negvalue = 100 - sumA;
                            percentageA += negvalue;
                        } else {
                            percentageA += sumA;
                        }
                        //-------------------------------------------------
                        contract.setTotalclaims(String.valueOf(numberofclaims));
                        contract.setTraches(String.valueOf(tranches));
                        contract.setTotalamountrecieved(String.valueOf(totalrecievedamount));
                        contract.setPercentage(String.valueOf(percentageA));
                        contract.setTotaltrancheamount(String.valueOf(trancheamount));
                        contract.setTotalclaimsamount(String.valueOf(claimsamount));
                        contract.setTotalclaimspercentage(String.valueOf(percentageB));
                        result.setMessage("OK");
                        result.setSuccess(true);
                        result.setResult(utility.ObjectMapper().writeValueAsString(contract));
                    }
                } else {
                    result.setMessage(getHCPNContract.getMessage());
                }
            } else {
                result.setMessage(new Methods().GETROLE(dataSource, userid, tags).getMessage());
            }
        } catch (IOException | ParseException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT OF PRO USING ACCOUNT USERID
    public ACRGBWSResult GETCONTRACTOFPRO(final DataSource dataSource,
            final String tags,
            final String userid,
            final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try {
            ACRGBWSResult GetRole = methods.GETROLE(dataSource, userid, "ACTIVE");
            if (GetRole.isSuccess()) {
                int numberofclaims = 0;
                int tranches = 0;
                double tracnheamountreleased = 0.00;
                double totalclaimsamount = 0.00;
                double percentageA = 0.00;
                double recievedamount = 0.00;
                double percentageB = 0.00;
                ArrayList<Contract> contractList = new ArrayList<>();
                //GETCONTRACT
//                ACRGBWSResult GetFacilityContract = this.GETCONTRACTList(dataSource, tags.trim().toUpperCase(), GetRole.getResult().trim());
                ACRGBWSResult GetFacilityContract = this.GETCONTRACTWITHOPENSTATEOFPRO(dataSource, tags.trim().toUpperCase(), GetRole.getResult().trim(), ustate.trim().toUpperCase());
                if (GetFacilityContract.isSuccess()) {
                    List<Contract> MapContract = Arrays.asList(utility.ObjectMapper().readValue(GetFacilityContract.getResult(), Contract[].class));
                    for (int w = 0; w < MapContract.size(); w++) {
                        Contract contract = new Contract();
                        ACRGBWSResult GetHCPN = methods.GetProWithPROID(dataSource, GetRole.getResult());
                        if (GetHCPN.isSuccess()) {
                            contract.setHcfid(GetHCPN.getResult());
                        }
                        contract.setAmount(MapContract.get(w).getAmount());
                        contract.setStats(MapContract.get(w).getStats());
                        contract.setCreatedby(MapContract.get(w).getCreatedby());
                        contract.setDatecreated(MapContract.get(w).getDatecreated());
                        contract.setContractdate(MapContract.get(w).getContractdate());
                        contract.setTranscode(MapContract.get(w).getTranscode());
                        contract.setBaseamount(MapContract.get(w).getBaseamount());
                        contract.setComittedClaimsVol(MapContract.get(w).getComittedClaimsVol());
                        contract.setComputedClaimsVol(MapContract.get(w).getComputedClaimsVol());
                        contract.setSb(MapContract.get(w).getSb());
                        contract.setAddamount(MapContract.get(w).getAddamount());
                        contract.setQuarter(MapContract.get(w).getQuarter());
                        // GET HCPN UNDER PRO USER
                        recievedamount += Double.parseDouble(MapContract.get(w).getAmount());
                        contractList.add(contract);
                        //-----------------------------------------------------------------------------
                    }
                    ACRGBWSResult GetHCPNList = methods.GETROLEMULITPLE(dataSource, GetRole.getResult().trim(), tags.trim());
                    if (GetHCPNList.isSuccess()) {
                        List<String> HCPNList = Arrays.asList(GetHCPNList.getResult().split(","));
                        for (int y = 0; y < HCPNList.size(); y++) {
                            //GET CONTRACT PER HCPN
//                            ACRGBWSResult GetHCPNContract = this.GETCONTRACT(dataSource, tags.trim().toUpperCase(), HCPNList.get(y).trim());
                            ACRGBWSResult GetHCPNContract = this.GETCONTRACTWITHOPENSTATE(dataSource, tags.trim().toUpperCase(), HCPNList.get(y).trim(), ustate);
                            if (GetHCPNContract.isSuccess()) {
                                Contract MapHCPNContract = utility.ObjectMapper().readValue(GetHCPNContract.getResult(), Contract.class);
                                //----------------------------------------------
                                ACRGBWSResult restA = new FetchMethods().GETASSETBYIDANDCONID(dataSource, HCPNList.get(y).trim(), MapHCPNContract.getConid().trim(), tags);
                                if (restA.isSuccess()) {
                                    List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restA.getResult(), Assets[].class));
                                    for (int g = 0; g < assetlist.size(); g++) {
                                        if (assetlist.get(g).getPreviousbalance() != null || !assetlist.get(g).getPreviousbalance().isEmpty() || !assetlist.get(g).getPreviousbalance().equals("")) {
                                            Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                            switch (tranch.getTranchtype()) {
                                                case "1ST": {
                                                    tracnheamountreleased += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                                    tracnheamountreleased += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                    tranches++;
                                                    break;
                                                }
                                                case "1STFINAL": {
                                                    tracnheamountreleased += Double.parseDouble(assetlist.get(g).getReleasedamount());
//                                                    tranches--;
                                                    break;
                                                }
                                                default: {
                                                    tracnheamountreleased += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                    tranches++;
                                                    break;
                                                }
                                            }
                                        } else {
                                            tracnheamountreleased += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                            tranches++;
                                            break;
                                        }
                                    }
                                }
                                //----------------------------------------------
                                if (MapHCPNContract.getContractdate() != null) {
                                    ContractDate condate = utility.ObjectMapper().readValue(MapHCPNContract.getContractdate(), ContractDate.class);
                                    ACRGBWSResult GetHCIList = methods.GETROLEMULITPLE(dataSource, HCPNList.get(y).trim(), tags.trim());
                                    if (GetHCIList.isSuccess()) {
                                        List<String> HCIList = Arrays.asList(GetHCIList.getResult().split(","));
                                        for (int x = 0; x < HCIList.size(); x++) {
                                            ACRGBWSResult GetHCIContract = this.GETCONTRACTWITHOPENSTATE(dataSource, tags.trim().toUpperCase(), HCIList.get(x).trim(), ustate);
                                            if (GetHCIContract.isSuccess()) {
                                                Contract hciCon = utility.ObjectMapper().readValue(GetHCIContract.getResult(), Contract.class);
                                                ArrayList<HealthCareFacility> testHCIlist = new ArrayList<>();
                                                ACRGBWSResult getMainAccre = new GetHCFMultiplePMCCNO().GETFACILITYBYMAINACCRE(dataSource, HCIList.get(x).trim());
                                                if (getMainAccre.isSuccess()) {
                                                    testHCIlist.addAll(Arrays.asList(utility.ObjectMapper().readValue(getMainAccre.getResult(), HealthCareFacility[].class)));
                                                } else if (new FetchMethods().GETFACILITYID(dataSource, HCIList.get(x).trim()).isSuccess()) {
                                                    testHCIlist.add(utility.ObjectMapper().readValue(new FetchMethods().GETFACILITYID(dataSource, HCIList.get(x).trim()).getResult(), HealthCareFacility.class));
                                                }
                                                if (testHCIlist.size() > 0) {
                                                    for (int yu = 0; yu < testHCIlist.size(); yu++) {
                                                        //------------------------------------------------------------------------
                                                        ACRGBWSResult sumresult = new FetchMethods().GETNCLAIMS(dataSource, testHCIlist.get(yu).getHcfcode().trim(), "G", condate.getDatefrom(), utility.AddMinusDaysDate(condate.getDateto(), "60"), "CURRENTSTATUS");
                                                        if (sumresult.isSuccess()) {
                                                            List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                                                            for (int i = 0; i < nclaimsdata.size(); i++) {
                                                                int countLedger = 0;
                                                                if (nclaimsdata.get(i).getRefiledate().isEmpty() || nclaimsdata.get(i).getRefiledate().equals("") || nclaimsdata.get(i).getRefiledate() == null) {
                                                                    if (hciCon.getEnddate().isEmpty() || hciCon.getEnddate().equals("") || hciCon.getEnddate() == null) {
                                                                        if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                                            countLedger++;
                                                                        }
                                                                    } else {
                                                                        if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(hciCon.getEnddate().trim(), "60"))) <= 0) {
                                                                            countLedger++;
                                                                        }
                                                                    }
                                                                } else {
                                                                    if (hciCon.getEnddate().isEmpty() || hciCon.getEnddate().equals("") || hciCon.getEnddate() == null) {
                                                                        if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                                            countLedger++;
                                                                        }
                                                                    } else {
                                                                        if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(hciCon.getEnddate().trim(), "60"))) <= 0) {
                                                                            countLedger++;
                                                                        }
                                                                    }
                                                                }
                                                                if (countLedger > 0) {
                                                                    numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                                    totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
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
                    Contract contractA = new Contract();
                    double sumA = (tracnheamountreleased / recievedamount) * 100;
                    if (sumA > 100) {
                        Double negvalue = 100 - sumA;
                        percentageA += negvalue;
                    } else {
                        percentageA += sumA;
                    }
                    double sumB = (totalclaimsamount / tracnheamountreleased) * 100;
                    if (sumB > 100) {
                        Double negvalue = 100 - sumB;
                        percentageB += negvalue;
                    } else {
                        percentageB += sumB;
                    }
                    //-------------------------------------------------
                    ACRGBWSResult GetHCPN = methods.GetProWithPROID(dataSource, GetRole.getResult());
                    if (GetHCPN.isSuccess()) {
                        contractA.setHcfid(GetHCPN.getResult());
                    } else {
                        contractA.setHcfid(GetHCPN.getResult());
                    }
                    contractA.setAmount(String.valueOf(recievedamount));
                    contractA.setTotalamountrecieved(String.valueOf(recievedamount));
                    contractA.setTotalclaims(String.valueOf(numberofclaims));
                    contractA.setTraches(String.valueOf(tranches));
                    contractA.setPercentage(String.valueOf(percentageA));
                    contractA.setTotaltrancheamount(String.valueOf(tracnheamountreleased));
                    contractA.setTotalclaimsamount(String.valueOf(totalclaimsamount));
                    contractA.setTotalclaimspercentage(String.valueOf(percentageB));
                    contractList.add(contractA);
                    //---------------------------------------------------------
                    if (contractList.size() > 0) {
                        result.setMessage("OK");
                        result.setSuccess(true);
                        result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
                    } else {
                        result.setMessage("N/A");
                    }
                } else {
                    result.setMessage(GetFacilityContract.getMessage());
                }
            } else {
                result.setMessage(methods.GETROLE(dataSource, userid, tags).getMessage());
            }
        } catch (IOException | ParseException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//GET ACCREDITATION NUMBER BY ACCOUNTCODE
    public ACRGBWSResult GETCONBYCODEOFPRO(final DataSource dataSource, final String ucode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ContractMethod contractmethod = new ContractMethod();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETCONBYCODE(:ucode); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ucode", ucode.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Contract> conlist = new ArrayList<>();
            while (resultset.next()) {
                Contract con = new Contract();
                con.setConid(resultset.getString("CONID"));
                con.setHcfid(resultset.getString("HCFID"));
                con.setAmount(resultset.getString("AMOUNT") == null
                        || resultset.getString("AMOUNT").isEmpty()
                        || resultset.getString("AMOUNT").equals("") ? "0" : resultset.getString("AMOUNT"));
                con.setStats(resultset.getString("STATS"));
                ACRGBWSResult creator = new FetchMethods().GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());

                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class
                    );
                    con.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    con.setCreatedby(creator.getMessage());
                }
                con.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                ACRGBWSResult getcondateA = contractmethod.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                if (getcondateA.isSuccess()) {
                    con.setContractdate(getcondateA.getResult());
                }
                con.setTranscode(resultset.getString("TRANSCODE"));
                con.setBaseamount(resultset.getString("BASEAMOUNT"));
                con.setRemarks(resultset.getString("REMARKS"));
                con.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                con.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                con.setAddamount(resultset.getString("ADDAMOUNT"));
                con.setQuarter(resultset.getString("QUARTER"));
                con.setSb(resultset.getString("SB"));
                //END DATE
                con.setEnddate(resultset.getString("ENDDATE") == null
                        || resultset.getString("ENDDATE").isEmpty()
                        || resultset.getString("ENDDATE").equals("") ? "" : dateformat.format(resultset.getTimestamp("ENDDATE")));
                conlist.add(con);
            }
            if (conlist.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(conlist));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ROLE INDEX BY CONDATE
    public ACRGBWSResult GETROLEINDEXCONDATE(final DataSource dataSource, final String pcondate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETROLEINDEXCONDATE(:pcondate); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("pcondate", pcondate.trim());
            statement.execute();
            ArrayList<UserRoleIndex> roleindexList = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                UserRoleIndex roleindex = new UserRoleIndex();
                roleindex.setRoleid(resultset.getString("ROLEID"));
                roleindex.setUserid(resultset.getString("USERID"));
                roleindex.setAccessid(resultset.getString("ACCESSID"));
                roleindex.setCreatedby(resultset.getString("CREATEDBY"));
                roleindex.setStatus(resultset.getString("STATUS"));
                roleindex.setContractdate(resultset.getString("CONDATE"));
                roleindex.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                roleindexList.add(roleindex);
            }
            if (roleindexList.size() > 0) {
                result.setResult(utility.ObjectMapper().writeValueAsString(roleindexList));
                result.setSuccess(true);
                result.setMessage("OK");
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ROLE INDEX BY CONDATE
    public ACRGBWSResult GETVALIDATECODE(
            final DataSource dataSource,
            final String utags,
            final String ucode) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETVALIDATECODE(:utags,:ucode); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.trim());
            statement.setString("ucode", ucode.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                CaseRate caseRate = new CaseRate();
                caseRate.setCode(resultset.getString("CODE"));
                caseRate.setAmount(resultset.getString("AMOUNT") == null
                        || resultset.getString("AMOUNT").equals("")
                        || resultset.getString("AMOUNT").isEmpty() ? "0" : resultset.getString("AMOUNT"));

                result.setMessage(resultset.getString("AMOUNT") == null
                        || resultset.getString("AMOUNT").equals("")
                        || resultset.getString("AMOUNT").isEmpty() ? "0" : resultset.getString("AMOUNT"));
                result.setResult(utility.ObjectMapper().writeValueAsString(caseRate));
                result.setSuccess(true);
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//TAGS
    public ACRGBWSResult GETCONTRACTWITHOPENSTATE(
            final DataSource dataSource,
            final String utags,
            final String uhcfcode,
            final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETENDCONOPENCONSTATE(:utags,:uhcfcode,:ustate); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.toUpperCase());
            statement.setString("uhcfcode", uhcfcode.trim());
            statement.setString("ustate", ustate.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Contract contract = new Contract();
                contract.setConid(resultset.getString("CONID"));
                contract.setHcfid(resultset.getString("HCFID"));
                //END OF GET NETWORK FULL DETAILS
                contract.setAmount(resultset.getString("AMOUNT") == null
                        || resultset.getString("AMOUNT").equals("")
                        || resultset.getString("AMOUNT").isEmpty() ? "0" : resultset.getString("AMOUNT"));
                contract.setStats(resultset.getString("STATS"));
                ACRGBWSResult creator = new FetchMethods().GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    contract.setCreatedby(creator.getResult());
                } else {
                    contract.setCreatedby(creator.getMessage());
                }
                contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                ACRGBWSResult getcondateA = this.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                if (getcondateA.isSuccess()) {
                    contract.setContractdate(getcondateA.getResult());
                }
                contract.setTranscode(resultset.getString("TRANSCODE"));
                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                contract.setSb(resultset.getString("SB"));
                contract.setAddamount(resultset.getString("ADDAMOUNT"));
                contract.setQuarter(resultset.getString("QUARTER"));
                //END DATE
                contract.setEnddate(resultset.getString("ENDDATE") == null
                        || resultset.getString("ENDDATE").isEmpty()
                        || resultset.getString("ENDDATE").equals("") ? "" : dateformat.format(resultset.getTimestamp("ENDDATE")));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contract));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETCONTRACTWITHOPENSTATEOFPRO(
            final DataSource dataSource,
            final String utags,
            final String uhcfcode,
            final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETENDCONOPENCONSTATE(:utags,:uhcfcode,:ustate); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.toUpperCase());
            statement.setString("uhcfcode", uhcfcode.trim());
            statement.setString("ustate", ustate.trim());
            statement.execute();
            ArrayList<Contract> contractList = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                Contract contract = new Contract();
                contract.setConid(resultset.getString("CONID"));
                contract.setHcfid(resultset.getString("HCFID"));
                //END OF GET NETWORK FULL DETAILS
                contract.setAmount(resultset.getString("AMOUNT") == null
                        || resultset.getString("AMOUNT").equals("")
                        || resultset.getString("AMOUNT").isEmpty() ? "0" : resultset.getString("AMOUNT"));
                contract.setStats(resultset.getString("STATS"));
                ACRGBWSResult creator = new FetchMethods().GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    contract.setCreatedby(creator.getResult());
                } else {
                    contract.setCreatedby(creator.getMessage());
                }
                contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
//                ACRGBWSResult getcondateA = this.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                if (this.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE")).isSuccess()) {
                    contract.setContractdate(this.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE")).getResult());
                }
                contract.setTranscode(resultset.getString("TRANSCODE"));
                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                contract.setSb(resultset.getString("SB"));
                contract.setAddamount(resultset.getString("ADDAMOUNT"));
                contract.setQuarter(resultset.getString("QUARTER"));
                //END DATE
                contract.setEnddate(resultset.getString("ENDDATE") == null
                        || resultset.getString("ENDDATE").isEmpty()
                        || resultset.getString("ENDDATE").equals("") ? "" : dateformat.format(resultset.getTimestamp("ENDDATE")));
                contractList.add(contract);
            }
            if (contractList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT OF APEX FACILITY
    public ACRGBWSResult APEXFACILITYCONTRACT(final DataSource dataSource,
            final String utags,
            final String uhcfcode,
            final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            ArrayList<Contract> contractlist = new ArrayList<>();
            ArrayList<String> meeslist = new ArrayList<>();
//            //-------------- GET APEX FACILITY
            ACRGBWSResult resultfm = new FacilityTagging().GETAPEXFACILITYS(dataSource);
            if (resultfm.isSuccess()) {
                List<HealthCareFacility> userlist = Arrays.asList(utility.ObjectMapper().readValue(resultfm.getResult(), HealthCareFacility[].class));
                for (int x = 0; x < userlist.size(); x++) {
                    CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.ACRGBPKGFUNCTION.GETENDCONOPENCONSTATE(:utags,:uhcfcode,:ustate); end;");
                    statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                    statement.setString("utags", utags);
                    statement.setString("uhcfcode", userlist.get(x).getHcfcode().trim());
                    statement.setString("ustate", ustate);
                    statement.execute();
                    ResultSet resultset = (ResultSet) statement.getObject("v_result");
                    if (resultset.next()) {
                        if (userlist.get(x).getType() != null) {
                            if (userlist.get(x).getType().equals("AH")) {
                                Contract contract = new Contract();
                                contract.setConid(resultset.getString("CONID"));
                                ACRGBWSResult facility = new FetchMethods().GETFACILITYID(dataSource, resultset.getString("HCFID"));
                                if (facility.isSuccess()) {
                                    contract.setHcfid(facility.getResult());
                                } else {
                                    contract.setHcfid(facility.getMessage());
                                }
                                //END OF GET NETWORK FULL DETAILS
                                contract.setAmount(resultset.getString("AMOUNT") == null
                                        || resultset.getString("AMOUNT").equals("")
                                        || resultset.getString("AMOUNT").isEmpty() ? "0" : resultset.getString("AMOUNT"));
                                contract.setStats(resultset.getString("STATS"));
                                ACRGBWSResult creator = new FetchMethods().GETFULLDETAILS(dataSource, (resultset.getString("CREATEDBY") == null
                                        || resultset.getString("CREATEDBY").isEmpty()
                                        || resultset.getString("CREATEDBY").equals("") ? "00" : resultset.getString("CREATEDBY")).trim());
                                if (creator.isSuccess()) {
                                    contract.setCreatedby(creator.getResult());
                                } else {
                                    contract.setCreatedby(creator.getMessage());
                                }
                                contract.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));//resultset.getString("DATECREATED"));
                                contract.setTranscode(resultset.getString("TRANSCODE"));
                                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                                contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                                contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                                contract.setSb(resultset.getString("SB"));
                                contract.setAddamount(resultset.getString("ADDAMOUNT"));
                                contract.setQuarter(resultset.getString("QUARTER"));
                                //END DATE
                                contract.setEnddate(resultset.getString("ENDDATE") == null
                                        || resultset.getString("ENDDATE").isEmpty()
                                        || resultset.getString("ENDDATE").equals("") ? "" : dateformat.format(resultset.getTimestamp("ENDDATE")));
                                //=============================================
                                int numberofclaims = 0;
                                double totalclaimsamount = 0.00;
                                double percentageA = 0.00;
                                double percentageB = 0.00;
                                double trancheamount = 0.00;
                                int tranches = 0;
                                ACRGBWSResult getcondateA = this.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                                if (getcondateA.isSuccess()) {
                                    contract.setContractdate(getcondateA.getResult());
                                    ContractDate condate = utility.ObjectMapper().readValue(getcondateA.getResult(), ContractDate.class);
                                    ACRGBWSResult restAB = new FetchMethods().GETASSETBYIDANDCONID(dataSource, resultset.getString("HCFID").trim(), resultset.getString("CONID").trim(), utags);
                                    if (restAB.isSuccess()) {
                                        List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restAB.getResult(), Assets[].class));
                                        for (int g = 0; g < assetlist.size(); g++) {
                                            if (assetlist.get(g).getPreviousbalance() == null || assetlist.get(g).getPreviousbalance().isEmpty() || assetlist.get(g).getPreviousbalance().equals("")) {
                                                trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                tranches++;
                                            } else {
                                                Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                                switch (tranch.getTranchtype()) {
                                                    case "1ST": {
                                                        trancheamount += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                                        trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                        tranches++;
                                                        break;
                                                    }
                                                    case "1STFINAL": {
                                                        trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
//                                                tranches--;
                                                        break;
                                                    }
                                                    default: {
                                                        trancheamount += Double.parseDouble(assetlist.get(g).getReleasedamount());
                                                        tranches++;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    ArrayList<HealthCareFacility> testHCIlist = new ArrayList<>();
                                    ACRGBWSResult getMainAccre = new GetHCFMultiplePMCCNO().GETFACILITYBYMAINACCRE(dataSource, userlist.get(x).getHcfcode().trim());
                                    if (getMainAccre.isSuccess()) {
                                        testHCIlist.addAll(Arrays.asList(utility.ObjectMapper().readValue(getMainAccre.getResult(), HealthCareFacility[].class)));
                                    } else if (new FetchMethods().GETFACILITYID(dataSource, userlist.get(x).getHcfcode().trim()).isSuccess()) {
                                        testHCIlist.add(utility.ObjectMapper().readValue(new FetchMethods().GETFACILITYID(dataSource, userlist.get(x).getHcfcode().trim()).getResult(), HealthCareFacility.class));
                                    }
                                    if (testHCIlist.size() > 0) {
                                        for (int yu = 0; yu < testHCIlist.size(); yu++) {
                                            //------------------------------------------------------------------------END GET ALL PMCC NO UNDER SELECTED FACILITY
                                            ACRGBWSResult sumresult = new FetchMethods().GETNCLAIMS(dataSource, testHCIlist.get(yu).getHcfcode().trim(), "G",
                                                    condate.getDatefrom(), utility.AddMinusDaysDate(condate.getDateto(), "60"), "CURRENTSTATUS");
                                            if (sumresult.isSuccess()) {
                                                List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                                                for (int i = 0; i < nclaimsdata.size(); i++) {
                                                    int countLedger = 0;
                                                    if (nclaimsdata.get(i).getRefiledate().isEmpty() || nclaimsdata.get(i).getRefiledate().equals("") || nclaimsdata.get(i).getRefiledate() == null) {
                                                        if (resultset.getString("ENDDATE") == null || resultset.getString("ENDDATE").equals("") || resultset.getString("ENDDATE").isEmpty()) {
                                                            if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                                countLedger++;
                                                            }
                                                        } else {
                                                            if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(dateformat.format(resultset.getTimestamp("ENDDATE")), "60"))) <= 0) {
                                                                countLedger++;
                                                            }
                                                        }
                                                    } else {
                                                        if (resultset.getString("ENDDATE") == null || resultset.getString("ENDDATE").equals("") || resultset.getString("ENDDATE").isEmpty()) {
                                                            if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                                countLedger++;
                                                            }
                                                        } else {
                                                            if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(dateformat.format(resultset.getTimestamp("ENDDATE")), "60"))) <= 0) {
                                                                countLedger++;
                                                            }
                                                        }
                                                    }
                                                    if (countLedger > 0) {
                                                        numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                        totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    double sumsA = (trancheamount / Double.parseDouble(resultset.getString("AMOUNT") == null
                                            || resultset.getString("AMOUNT").isEmpty()
                                            || resultset.getString("AMOUNT").equals("") ? "0" : resultset.getString("AMOUNT"))) * 100;
                                    if (sumsA > 100) {
                                        double negvalue = 100 - sumsA;
                                        percentageA += negvalue;
                                    } else {
                                        percentageA += sumsA;
                                    }
                                    //----------------
                                    double sumsB = (totalclaimsamount / trancheamount) * 100;
                                    if (sumsB > 100) {
                                        double negvalue = 100 - sumsB;
                                        percentageB += negvalue;
                                    } else {
                                        percentageB += sumsB;
                                    }
                                }

                                contract.setTotalamountrecieved(String.valueOf(trancheamount));
                                contract.setTotalclaims(String.valueOf(numberofclaims));
                                contract.setTraches(String.valueOf(tranches));
                                contract.setTotaltrancheamount(String.valueOf(trancheamount));
                                contract.setTotalclaimsamount(String.valueOf(totalclaimsamount));
                                contract.setPercentage(String.valueOf(percentageA));
                                contract.setTotalclaimspercentage(String.valueOf(percentageB));
                                contractlist.add(contract);
                            }
                        }
                    }
                }
            } else {
                result.setMessage(resultfm.getMessage());
            }
            if (contractlist.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractlist));
            } else {
                result.setMessage("N/A " + meeslist);
            }

        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
