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
import acrgb.structure.HealthCareFacility;
import acrgb.structure.NclaimsData;
import acrgb.structure.Total;
import acrgb.structure.Tranch;
import acrgb.structure.UserRoleIndex;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class CurrentBalance {

    private final Utility utility = new Utility();
    private final SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");

    public ACRGBWSResult OpenEndedHCIContract( //09761235056
            final DataSource dataSource,
            final String utags,
            final String uhcfcode,
            final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETCONTRACTSTATE(:utags,:uhcfcode,:ustate); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.trim());
            statement.setString("uhcfcode", uhcfcode.trim());
            statement.setString("ustate", ustate.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                if (!this.ValidateConBalance(dataSource, resultset.getString("CONTRACTDATE"), uhcfcode.trim(), resultset.getString("CONID")).isSuccess()) {
                    Contract contract = new Contract();
                    contract.setConid(resultset.getString("CONID"));
                    ACRGBWSResult facility = new FetchMethods().GETFACILITYID(dataSource, resultset.getString("HCFID"));
                    if (facility.isSuccess()) {
                        contract.setHcfid(facility.getResult());
                    } else {
                        contract.setHcfid(facility.getMessage());
                    }
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
                    contract.setTranscode(resultset.getString("TRANSCODE"));
                    contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                    contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                    contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                    contract.setSb(resultset.getString("SB"));
                    contract.setAddamount(resultset.getString("ADDAMOUNT"));
                    contract.setQuarter(resultset.getString("QUARTER"));
                    int numberofclaims = 0;
                    double totalclaimsamount = 0.00;
                    double percentageA = 0.00;
                    double percentageB = 0.00;
                    double trancheamount = 0.00;
                    double totalrecievedamount = 0.00;
                    int tranches = 0;
                    ACRGBWSResult getcondateA = new ContractMethod().GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                    if (getcondateA.isSuccess()) {
                        ContractDate condate = utility.ObjectMapper().readValue(getcondateA.getResult(), ContractDate.class);
                        contract.setContractdate(getcondateA.getResult());
                        //GET TRANCHE SUMMARY 
                        ACRGBWSResult getIdType = new CurrentBalance().GETTRANCHBYTYPE(dataSource, "1STFINAL");
                        if (getIdType.isSuccess()) {
                            Tranch tranch = utility.ObjectMapper().readValue(getIdType.getResult(), Tranch.class);
                            ACRGBWSResult totalResult = new Methods().GETSUMMARY(dataSource, utags.trim(), resultset.getString("HCFID").trim(), tranch.getTranchid(), resultset.getString("CONID"));
                            if (totalResult.isSuccess()) {
                                Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                                tranches += Integer.parseInt(getResult.getCcount());
                                trancheamount += Double.parseDouble(getResult.getCtotal());
                            }
                            //GET TRANCHE AMOUNT
                            ACRGBWSResult getTranchid = new CurrentBalance().GET1STFINAL(dataSource, utags.trim().toUpperCase(), resultset.getString("HCFID").trim(), tranch.getTranchid(), resultset.getString("CONID").trim());
                            if (getTranchid.isSuccess()) {
                                Total getResult = utility.ObjectMapper().readValue(getTranchid.getResult(), Total.class);
                                tranches -= Integer.parseInt(getResult.getCcount());
                                trancheamount += Double.parseDouble(getResult.getCtotal());
                            }
                        }
                        ACRGBWSResult restAB = new FetchMethods().GETASSETBYIDANDCONID(dataSource, resultset.getString("HCFID").trim(), resultset.getString("CONID"), utags);
                        if (restAB.isSuccess()) {
                            if (getIdType.isSuccess()) {
                                Tranch tranch = utility.ObjectMapper().readValue(getIdType.getResult(), Tranch.class);
                                ACRGBWSResult totalResult = new Methods().GETSUMMARY(dataSource, utags.trim(), resultset.getString("HCFID").trim(), tranch.getTranchid(), resultset.getString("CONID").trim());
                                if (totalResult.isSuccess()) {
                                    Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                                    totalrecievedamount += Double.parseDouble(getResult.getCtotal());
                                }
                                //GET TRANCHE AMOUNT
                                ACRGBWSResult getTranchid = new CurrentBalance().GET1STFINAL(dataSource, utags.trim().toUpperCase(), resultset.getString("HCFID").trim(), tranch.getTranchid(), resultset.getString("CONID").trim());
                                if (getTranchid.isSuccess()) {
                                    Total getResult = utility.ObjectMapper().readValue(getTranchid.getResult(), Total.class);
                                    totalrecievedamount += Double.parseDouble(getResult.getCtotal());
                                }
                            }
                            List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restAB.getResult(), Assets[].class));
                            for (int g = 0; g < assetlist.size(); g++) {

                                if (assetlist.get(g).getPreviousbalance() != null) {
                                    Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                    switch (tranch.getTranchtype()) {
                                        case "1ST": {
                                            trancheamount += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                            totalrecievedamount += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                            break;
                                        }
                                        case "1STFINAL": {
                                            trancheamount -= Double.parseDouble(assetlist.get(g).getReleasedamount());
                                            totalrecievedamount -= Double.parseDouble(assetlist.get(g).getReleasedamount());
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        //GET CLAIMS SUMMARY OF FACILITY UNDER NETWORK
                        //-------------------------------------------------------------------
                        ACRGBWSResult getHcfByCode = new GetHCFMultiplePMCCNO().GETFACILITYBYCODE(dataSource, resultset.getString("HCFID").trim());
                        if (getHcfByCode.isSuccess()) {
                            HealthCareFacility healthCareFacility = utility.ObjectMapper().readValue(getHcfByCode.getResult(), HealthCareFacility.class);
                            //GET HCF DETAILS BY NAME
                            ACRGBWSResult getHcfByName = new GetHCFMultiplePMCCNO().GETFACILITYBYNAME(dataSource, healthCareFacility.getHcfname().trim(), healthCareFacility.getStreet().trim());
                            if (getHcfByName.isSuccess()) {
                                List<HealthCareFacility> healthCareFacilityList = Arrays.asList(utility.ObjectMapper().readValue(getHcfByName.getResult(), HealthCareFacility[].class));
                                for (int yu = 0; yu < healthCareFacilityList.size(); yu++) {
                                    //------------------------------------------------------------------------
                                    ACRGBWSResult sumresult = new FetchMethods().GETNCLAIMS(dataSource, healthCareFacilityList.get(yu).getHcfcode().trim(), "G",
                                            condate.getDatefrom(), utility.AddMinusDaysDate(condate.getDateto(), "60"), "CURRENTSTATUS");
                                    if (sumresult.isSuccess()) {
                                        List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                                        for (int i = 0; i < nclaimsdata.size(); i++) {
                                            if (nclaimsdata.get(i).getRefiledate().isEmpty()) {
                                                if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                    numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                    totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                }
                                            } else {
                                                if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                    numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                    totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        double sumsA = (trancheamount / Double.parseDouble(resultset.getString("AMOUNT"))) * 100;
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
                    contract.setRemainingbalance(String.valueOf(totalrecievedamount - totalclaimsamount));
                    contract.setTotalclaims(String.valueOf(numberofclaims));
                    contract.setTraches(String.valueOf(tranches));
                    contract.setTotaltrancheamount(String.valueOf(trancheamount));
                    contract.setTotalclaimsamount(String.valueOf(totalclaimsamount));
                    contract.setPercentage(String.valueOf(percentageA));
                    contract.setTotalclaimspercentage(String.valueOf(percentageB));
                    result.setResult(utility.ObjectMapper().writeValueAsString(contract));
                    result.setMessage("OK");
                    result.setSuccess(true);
                } else {
                    result.setMessage("No data found");
                }
            } else {
                result.setMessage("No data found");
            }
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(CurrentBalance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult OpenEndedHCPNContract(
            final DataSource dataSource,
            final String utags,
            final String uhcfcode,
            final String ustate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETCONTRACTSTATE(:utags,:uhcfcode,:ustate); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.trim());
            statement.setString("uhcfcode", uhcfcode.trim());
            statement.setString("ustate", ustate.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                if (!this.ValidateConBalance(dataSource, resultset.getString("CONTRACTDATE"), uhcfcode, resultset.getString("CONID")).isSuccess()) {
                    Contract contract = new Contract();
                    contract.setConid(resultset.getString("CONID"));
                    ACRGBWSResult getHCPN = new FetchMethods().GETMBCONTROL(dataSource, resultset.getString("HCFID"));
                    if (getHCPN.isSuccess()) {
                        contract.setHcfid(getHCPN.getResult());
                    } else {
                        contract.setHcfid(getHCPN.getMessage());
                    }
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
                    contract.setTranscode(resultset.getString("TRANSCODE"));
                    contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                    contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                    contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                    contract.setSb(resultset.getString("SB"));
                    contract.setAddamount(resultset.getString("ADDAMOUNT"));
                    contract.setQuarter(resultset.getString("QUARTER"));
                    int numberofclaims = 0;
                    double totalclaimsamount = 0.00;
                    double percentageA = 0.00;
                    double percentageB = 0.00;
                    double trancheamount = 0.00;
                    double totalrecievedamount = 0.00;
                    int tranches = 0;
                    ACRGBWSResult getcondateA = new ContractMethod().GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                    if (getcondateA.isSuccess()) {
                        ContractDate condate = utility.ObjectMapper().readValue(getcondateA.getResult(), ContractDate.class);
                        contract.setContractdate(getcondateA.getResult());
                        ACRGBWSResult getIdType = new CurrentBalance().GETTRANCHBYTYPE(dataSource, "1STFINAL");
                        if (getIdType.isSuccess()) {
                            Tranch tranch = utility.ObjectMapper().readValue(getIdType.getResult(), Tranch.class);
                            //GET TRANCHE SUMMARY 
                            ACRGBWSResult totalResult = new Methods().GETSUMMARY(dataSource, utags, uhcfcode, tranch.getTranchid(), resultset.getString("CONID"));
                            if (totalResult.isSuccess()) {
                                Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                                tranches += Integer.parseInt(getResult.getCcount());
                                trancheamount += Double.parseDouble(getResult.getCtotal());
                            }
                            //GET TRANCHE AMOUNT
                            ACRGBWSResult getTranchid = new CurrentBalance().GET1STFINAL(dataSource, utags.trim().toUpperCase(), uhcfcode, tranch.getTranchid(), resultset.getString("CONID"));
                            if (getTranchid.isSuccess()) {
                                Total getResult = utility.ObjectMapper().readValue(getTranchid.getResult(), Total.class);
                                tranches -= Integer.parseInt(getResult.getCcount());
                                trancheamount += Double.parseDouble(getResult.getCtotal());
                            }
                        }

                        ACRGBWSResult restAB = new FetchMethods().GETASSETBYIDANDCONID(dataSource, uhcfcode, resultset.getString("CONID"), utags);
                        if (restAB.isSuccess()) {
                            if (getIdType.isSuccess()) {
                                Tranch tranch = utility.ObjectMapper().readValue(getIdType.getResult(), Tranch.class);
                                ACRGBWSResult totalResult = new Methods().GETSUMMARY(dataSource, utags.trim(), resultset.getString("HCFID").trim(), tranch.getTranchid(), resultset.getString("CONID").trim());
                                if (totalResult.isSuccess()) {
                                    Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                                    totalrecievedamount += Double.parseDouble(getResult.getCtotal());
                                }
                                //GET TRANCHE AMOUNT
                                ACRGBWSResult getTranchid = new CurrentBalance().GET1STFINAL(dataSource, utags.trim().toUpperCase(), resultset.getString("HCFID").trim(), tranch.getTranchid(), resultset.getString("CONID").trim());
                                if (getTranchid.isSuccess()) {
                                    Total getResult = utility.ObjectMapper().readValue(getTranchid.getResult(), Total.class);
                                    totalrecievedamount += Double.parseDouble(getResult.getCtotal());
                                }
                            }

                            List<Assets> assetlist = Arrays.asList(utility.ObjectMapper().readValue(restAB.getResult(), Assets[].class));
                            for (int g = 0; g < assetlist.size(); g++) {
                                if (assetlist.get(g).getPreviousbalance() != null) {
                                    Tranch tranch = utility.ObjectMapper().readValue(assetlist.get(g).getTranchid(), Tranch.class);
                                    switch (tranch.getTranchtype()) {
                                        case "1ST": {
                                            trancheamount += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                            totalrecievedamount += Double.parseDouble(assetlist.get(g).getPreviousbalance());
                                            break;
                                        }
                                        case "1STFINAL": {
                                            trancheamount -= Double.parseDouble(assetlist.get(g).getReleasedamount());
                                            totalrecievedamount -= Double.parseDouble(assetlist.get(g).getReleasedamount());
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        //GET CLAIMS SUMMARY OF FACILITY UNDER NETWORK
                        ACRGBWSResult conList = this.GETPREVIOUSMAP(dataSource, uhcfcode.trim(), condate.getCondateid());
                        if (conList.isSuccess()) {
                            List<UserRoleIndex> userRoleList = Arrays.asList(utility.ObjectMapper().readValue(conList.getResult(), UserRoleIndex[].class));
                            for (int x = 0; x < userRoleList.size(); x++) {
                                //-------------------------------------------------------------------
                                ACRGBWSResult getHcfByCode = new GetHCFMultiplePMCCNO().GETFACILITYBYCODE(dataSource, userRoleList.get(x).getAccessid().trim());
                                if (getHcfByCode.isSuccess()) {
                                    HealthCareFacility healthCareFacility = utility.ObjectMapper().readValue(getHcfByCode.getResult(), HealthCareFacility.class);
                                    //GET HCF DETAILS BY NAME
                                    ACRGBWSResult getHcfByName = new GetHCFMultiplePMCCNO().GETFACILITYBYNAME(dataSource, healthCareFacility.getHcfname().trim(), healthCareFacility.getStreet().trim());
                                    if (getHcfByName.isSuccess()) {
                                        List<HealthCareFacility> healthCareFacilityList = Arrays.asList(utility.ObjectMapper().readValue(getHcfByName.getResult(), HealthCareFacility[].class));
                                        for (int yu = 0; yu < healthCareFacilityList.size(); yu++) {
                                            //------------------------------------------------------------------------
                                            ACRGBWSResult sumresult = new FetchMethods().GETNCLAIMS(dataSource,
                                                    healthCareFacilityList.get(yu).getHcfcode().trim(), "G",
                                                    condate.getDatefrom(),
                                                    utility.AddMinusDaysDate(condate.getDateto().trim(), "60"),
                                                    "CURRENTSTATUS");
                                            if (sumresult.isSuccess()) {
                                                List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                                                for (int i = 0; i < nclaimsdata.size(); i++) {
                                                    if (nclaimsdata.get(i).getRefiledate().isEmpty()) {
                                                        if (dateformat.parse(nclaimsdata.get(i).getDatesubmitted()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
                                                            numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                                            totalclaimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                                        }
                                                    } else {
                                                        if (dateformat.parse(nclaimsdata.get(i).getRefiledate()).compareTo(dateformat.parse(utility.AddMinusDaysDate(condate.getDateto(), "60"))) <= 0) {
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
                        double sumsA = (trancheamount / Double.parseDouble(resultset.getString("AMOUNT"))) * 100;
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
                    contract.setTotalclaims(String.valueOf(numberofclaims));
                    contract.setTraches(String.valueOf(tranches));
                    contract.setTotaltrancheamount(String.valueOf(trancheamount));
                    contract.setTotalclaimsamount(String.valueOf(totalclaimsamount));
                    contract.setPercentage(String.valueOf(percentageA));
                    contract.setTotalclaimspercentage(String.valueOf(percentageB));
                    contract.setRemainingbalance(String.valueOf(totalrecievedamount - totalclaimsamount));
                    result.setResult(utility.ObjectMapper().writeValueAsString(contract));
                    result.setMessage("OK");
                    result.setSuccess(true);
                } else {
                    result.setMessage("No data found");
                }
            } else {
                result.setMessage("No data found");
            }
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(CurrentBalance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETPREVIOUSMAP(final DataSource dataSource,
            final String puserid,
            final String pcondate) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETPREVIOUSMAP(:puserid,:pcondate); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("puserid", puserid.trim());
            statement.setString("pcondate", pcondate.trim());
            statement.execute();
            ArrayList<UserRoleIndex> userRoleList = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                UserRoleIndex userRole = new UserRoleIndex();
                userRole.setUserid(resultset.getString("USERID"));
                userRole.setAccessid(resultset.getString("ACCESSID"));
                userRole.setContractdate(resultset.getString("CONDATE"));
                userRoleList.add(userRole);
            }
            if (userRoleList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(userRoleList));
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(CurrentBalance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult ValidateConBalance(
            final DataSource dataSource,
            final String ucondateid,
            final String uaccount,
            final String uconid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.VALIDATECONBALANCE(:ucondateid,:uaccount,:uconid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ucondateid", ucondateid.trim());
            statement.setString("uaccount", uaccount.trim());
            statement.setString("uconid", uconid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                ConBalance conBalance = new ConBalance();
                conBalance.setConbalance(resultset.getString("CONBALANCE"));
                conBalance.setConamount(resultset.getString("CONAMOUNT"));
                conBalance.setCondateid(resultset.getString("CONDATEID"));
                conBalance.setAccount(resultset.getString("ACCOUNT"));
                conBalance.setConid(resultset.getString("CONID"));
                conBalance.setClaimscount(resultset.getString("CLAIMSCOUNT"));
                //--------------------------------------------------------------
                result.setResult(utility.ObjectMapper().writeValueAsString(conBalance));
                result.setMessage("OK");
                result.setSuccess(true);
            } else {
                result.setMessage("No data found");
            }
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(CurrentBalance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // CHANGEUSELEVEL
    public ACRGBWSResult GET1STFINAL(
            final DataSource dataSource,
            final String utags,
            final String phcfid,
            final String utrancheid,
            final String uconid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GET1STFINAL(:utags,:phcfid,:utrancheid,:uconid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.trim());
            statement.setString("phcfid", phcfid.trim());
            statement.setString("utrancheid", utrancheid.trim());
            statement.setString("uconid", uconid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Total tot = new Total();
                tot.setCtotal(resultset.getString("cTOTAL"));
                tot.setHcfid(resultset.getString("HCFID"));
                tot.setCcount(resultset.getString("cCOUNT"));
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(tot));
                result.setMessage("OK");
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(CurrentBalance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETTRANCHBYTYPE(
            final DataSource dataSource,
            final String ptype) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.TRANCHWITHNAME(:ptype); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ptype", ptype.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                Tranch tranch = new Tranch();
                tranch.setTranchid(resultset.getString("TRANCHID"));
                tranch.setTranchtype(resultset.getString("TRANCHTYPE"));
                tranch.setPercentage(resultset.getString("PERCENTAGE"));
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(tranch));
                result.setMessage("OK");
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(CurrentBalance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult GETFINALBALANCE(
            final DataSource dataSource,
            final String utags,
            final String paccount) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETFINALBALANCE(:utags,:paccount); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("utags", utags.trim());
            statement.setString("paccount", paccount.trim());
            statement.execute();
            ArrayList<ConBalance> conBalanceList = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                //------------------OBJECT MAPPING ----------------------
                ConBalance conbal = new ConBalance();
                conbal.setAccount(resultset.getString("ACCOUNT"));
                conbal.setBooknum(resultset.getString("BOOKNUM"));
                conbal.setConamount(resultset.getString("CONAMOUNT")); //CONTRACT AMOUNT
                conbal.setConbalance(resultset.getString("CONBALANCE"));//CONTRACT REMAINING BALANCE
                //GETCONDATEBYID
                ACRGBWSResult getCondate = new ContractMethod().GETCONDATEBYID(dataSource, resultset.getString("CONID"));
                if (getCondate.isSuccess()) {
                    conbal.setCondateid(getCondate.getResult());
                }
                conbal.setConid(resultset.getString("CONID"));
                conbal.setConutilized(resultset.getString("CONUTILIZED"));
                if (resultset.getTimestamp("DATECREATED") == null) {
                    conbal.setDatecreated(resultset.getString("DATECREATED"));
                } else {
                    conbal.setDatecreated(dateformat.format(resultset.getTimestamp("DATECREATED")));
                }
                conbal.setStatus(resultset.getString("STATUS"));
                conbal.setClaimscount(resultset.getString("CLAIMSCOUNT"));
                conBalanceList.add(conbal);
                //------------------  END OF OBJECT MAPPING ----------------------
            }
            if (conBalanceList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(conBalanceList));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(CurrentBalance.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
