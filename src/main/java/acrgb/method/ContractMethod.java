/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.ConBalance;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.HealthCareFacility;
import acrgb.structure.ManagingBoard;
import acrgb.structure.NclaimsData;
import acrgb.structure.Total;
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
 * @author MinoSun
 */
@RequestScoped
public class ContractMethod {

    public ContractMethod() {
    }

    private final Utility utility = new Utility();
    private final FetchMethods fm = new FetchMethods();
    private final SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");

    public ACRGBWSResult GetAllContract(final DataSource dataSource, final String phcfcode, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        try (Connection connection = dataSource.getConnection()) {
            switch (tags.toUpperCase()) {
                case "HCPNPRO":
                    ArrayList<Contract> contractList = new ArrayList<>();
                    CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
                    statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                    statement.setString("tags", "INACTIVE");
                    statement.setString("pfchid", phcfcode.trim());
                    statement.execute();
                    ResultSet resultset = (ResultSet) statement.getObject("v_result");
                    while (resultset.next()) {
                        Contract contract = new Contract();
                        contract.setConid(resultset.getString("CONID"));
                        //END OF GET NETWORK FULL DETAILS
                        contract.setAmount(resultset.getString("AMOUNT"));
                        contract.setStats(resultset.getString("STATS"));
                        contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                        ACRGBWSResult getcondateA = this.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE"));
                        if (getcondateA.isSuccess()) {
                            contract.setContractdate(getcondateA.getResult());
                        }
                        contract.setTranscode(resultset.getString("TRANSCODE"));
                        contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                        contract.setHcfid(resultset.getString("HCFID"));
                        contract.setComittedClaimsVol(resultset.getString("C_CLAIMSVOL"));
                        contract.setComputedClaimsVol(resultset.getString("T_CLAIMSVOL"));
                        contract.setSb(resultset.getString("SB"));
                        contract.setAddamount(resultset.getString("ADDAMOUNT"));
                        contract.setQuarter(resultset.getString("QUARTER"));
                        contractList.add(contract);
                    }
                    if (contractList.isEmpty()) {
                        result.setSuccess(true);
                        result.setMessage("OK");
                        result.setResult(utility.ObjectMapper().writeValueAsString(contractList));
                    } else {
                        result.setMessage("N/A");
                    }
                    break;
                case "HCPNPHIC":
                    ArrayList<Contract> contractLists = new ArrayList<>();
                    CallableStatement statements = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
                    statements.registerOutParameter("v_result", OracleTypes.CURSOR);
                    statements.setString("tags", "INACTIVE");
                    statements.setString("pfchid", phcfcode.trim());
                    statements.execute();
                    ResultSet resultsets = (ResultSet) statements.getObject("v_result");
                    while (resultsets.next()) {
                        Contract contracts = new Contract();
                        contracts.setConid(resultsets.getString("CONID"));
                        //END OF GET NETWORK FULL DETAILS
                        contracts.setAmount(resultsets.getString("AMOUNT"));
                        contracts.setStats(resultsets.getString("STATS"));
                        contracts.setDatecreated(dateformat.format(resultsets.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                        ACRGBWSResult getcondateA = this.GETCONDATEBYID(dataSource, resultsets.getString("CONTRACTDATE"));
                        if (getcondateA.isSuccess()) {
                            contracts.setContractdate(getcondateA.getResult());
                        }
                        contracts.setTranscode(resultsets.getString("TRANSCODE"));
                        contracts.setBaseamount(resultsets.getString("BASEAMOUNT"));
                        contracts.setHcfid(resultsets.getString("HCFID"));
                        contracts.setComittedClaimsVol(resultsets.getString("C_CLAIMSVOL"));
                        contracts.setComputedClaimsVol(resultsets.getString("T_CLAIMSVOL"));
                        contracts.setSb(resultsets.getString("SB"));
                        contracts.setAddamount(resultsets.getString("ADDAMOUNT"));
                        contracts.setQuarter(resultsets.getString("QUARTER"));
                        contractLists.add(contracts);
                    }

                    if (contractLists.isEmpty()) {
                        result.setSuccess(true);
                        result.setMessage("OK");
                        result.setResult(utility.ObjectMapper().writeValueAsString(contractLists));
                    } else {
                        result.setMessage("N/A");
                    }
                    break;
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    //GET APPELLATE CONTROL
    public ACRGBWSResult GETCONTRACT(final DataSource dataSource, final String tags, final String hcfid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
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
                ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    contract.setCreatedby(creator.getResult());
                } else {
                    contract.setCreatedby(creator.getMessage());
                }
                contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
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
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contract));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET APPELLATE CONTROL
    public ACRGBWSResult GETCONTRACTList(final DataSource dataSource, final String tags, final String hcfid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<Contract> contractList = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.ACR_CONTRACT(:tags,:pfchid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.toUpperCase().trim());
            statement.setString("pfchid", hcfid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                Contract contract = new Contract();
                contract.setConid(resultset.getString("CONID"));
                contract.setHcfid(resultset.getString("HCFID"));
                //END OF GET NETWORK FULL DETAILS
                contract.setAmount(resultset.getString("AMOUNT"));
                contract.setStats(resultset.getString("STATS"));
                ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    contract.setCreatedby(creator.getResult());
                } else {
                    contract.setCreatedby(creator.getMessage());
                }
                contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
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
            result.setMessage(ex.toString());
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET APPELLATE CONTROL
    public ACRGBWSResult GETCONDATE(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETCONDATE(:tags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.toUpperCase().trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (tags.toUpperCase().equals("ACTIVE")) {
                ArrayList<ContractDate> contractdatelist = new ArrayList<>();
                while (resultset.next()) {
                    ContractDate contractdate = new ContractDate();
                    contractdate.setCondateid(resultset.getString("CONDATEID"));
                    ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                    if (creator.isSuccess()) {
                        contractdate.setCreatedby(creator.getResult());
                    } else {
                        contractdate.setCreatedby(creator.getMessage());
                    }
                    contractdate.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                    contractdate.setDatefrom(dateformat.format(resultset.getDate("DATEFROM")));//resultset.getString("DATECOVERED"));
                    contractdate.setDateto(dateformat.format(resultset.getDate("DATETO")));//resultset.getString("DATECOVERED"));
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
                    ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                    if (creator.isSuccess()) {
                        contractdate.setCreatedby(creator.getResult());
                    } else {
                        contractdate.setCreatedby(creator.getMessage());
                    }
                    contractdate.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                    contractdate.setDatefrom(dateformat.format(resultset.getDate("DATEFROM")));//resultset.getString("DATECOVERED"));
                    contractdate.setDateto(dateformat.format(resultset.getDate("DATETO")));//resultset.getString("DATECOVERED"));
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
            result.setMessage(ex.toString());
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
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETCONDATEBYID(:ucondateid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ucondateid", ucondateid.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                ContractDate contractdate = new ContractDate();
                contractdate.setCondateid(resultset.getString("CONDATEID"));
                contractdate.setCreatedby(resultset.getString("CREATEDBY"));
                contractdate.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));//resultset.getString("DATECREATED"));
                contractdate.setDatefrom(dateformat.format(resultset.getDate("DATEFROM")));//resultset.getString("DATECOVERED"));
                contractdate.setDateto(dateformat.format(resultset.getDate("DATETO")));//resultset.getString("DATECOVERED"));
                contractdate.setStatus(resultset.getString("STATUS"));
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(contractdate));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
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
        Methods methods = new Methods();
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETCONTRACTBYCONDATEID(:ucondateid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ucondateid", ucondateid.trim());
            statement.execute();
            // ArrayList<Contract> contractList = new ArrayList<>();
            ArrayList<String> conidlist = new ArrayList<>();
            ArrayList<Contract> contractlist = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                conidlist.add(resultset.getString("CONID"));
                Contract contract = new Contract();
                contract.setAmount(resultset.getString("AMOUNT"));
                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                contract.setConid(resultset.getString("CONID"));
                ACRGBWSResult getcondateA = this.GETCONDATEBYID(dataSource, resultset.getString("CONTRACTDATE").trim());
                if (getcondateA.isSuccess()) {
                    contract.setContractdate(getcondateA.getResult());
                }
                contract.setCreatedby(resultset.getString("CREATEDBY"));
                contract.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                if (resultset.getString("ENDDATE") == null) {
                    contract.setEnddate(resultset.getString("ENDDATE"));
                } else {
                    contract.setEnddate(dateformat.format(resultset.getDate("ENDDATE")));
                }
                //GET MANAGINGBOARD NAME
                ACRGBWSResult GetMB = methods.GETMBWITHID(dataSource, resultset.getString("HCFID"));
                if (GetMB.isSuccess()) {
                    ManagingBoard mb = utility.ObjectMapper().readValue(GetMB.getResult(), ManagingBoard.class);
                    contract.setHcfid(mb.getMbname());
                } else {
                    ACRGBWSResult GetHCI = fm.GETFACILITYID(dataSource, resultset.getString("HCFID"));
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
            result.setMessage(ex.toString());
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT BY DATECOVERED
    public ACRGBWSResult GETPREVIOUSBALANCE(final DataSource dataSource,
            final String paccount,
            final String pconid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETPREVIOUSBALANCE(:paccount,:pconid); end;");
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
                conbal.setConamount(resultset.getString("CONAMOUNT")); //CONTRACT AMOUNT
                conbal.setConbalance(resultset.getString("CONBALANCE"));//CONTRACT REMAINING BALANCE
                //GETCONDATEBYID
                ACRGBWSResult getCondate = this.GETCONDATEBYID(dataSource, resultset.getString("CONID"));
                if (getCondate.isSuccess()) {
                    conbal.setCondateid(getCondate.getResult());
                }
                conbal.setConid(resultset.getString("CONID"));
                conbal.setConutilized(resultset.getString("CONUTILIZED"));
                if (resultset.getString("DATECREATED") == null) {
                    conbal.setDatecreated(resultset.getString("DATECREATED"));
                } else {
                    conbal.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
                }
                conbal.setStatus(resultset.getString("STATUS"));
                //------------------  END OF OBJECT MAPPING ----------------------
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(conbal));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT OF FACILITY USING ACCOUNT USERID
    public ACRGBWSResult GETCONTRACTOFFACILITY(final DataSource dataSource, final String tags, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try {
            ACRGBWSResult GetRole = methods.GETROLE(dataSource, userid, "ACTIVE");
            if (GetRole.isSuccess()) {
                //GETCONTRACT
                ACRGBWSResult GetFacilityContract = this.GETCONTRACT(dataSource, tags, GetRole.getResult().trim());
                if (GetFacilityContract.isSuccess()) {
                    Contract MapContract = utility.ObjectMapper().readValue(GetFacilityContract.getResult(), Contract.class);
                    if (MapContract.getContractdate() != null) {
                        Contract contract = new Contract();
                        //--------------------------------------
                        ACRGBWSResult GetFacility = fm.GETFACILITYID(dataSource, userid);
                        if (GetFacility.isSuccess()) {
                            contract.setHcfid(GetFacility.getResult());
                        }
                        //END OF GET NETWORK FULL DETAILS
                        contract.setAmount(MapContract.getAmount());
                        contract.setStats(MapContract.getStats());
                        contract.setCreatedby(MapContract.getCreatedby());
                        contract.setDatecreated(MapContract.getDatecreated());//resultset.getString("DATECREATED"));
                        contract.setContractdate(MapContract.getContractdate());
                        contract.setTranscode(MapContract.getTranscode());
                        contract.setBaseamount(MapContract.getBaseamount());
                        contract.setComittedClaimsVol(MapContract.getComittedClaimsVol());
                        contract.setComputedClaimsVol(MapContract.getComputedClaimsVol());
                        contract.setSb(MapContract.getSb());
                        contract.setAddamount(MapContract.getAddamount());
                        //=============================================
                        ContractDate condate = utility.ObjectMapper().readValue(MapContract.getContractdate(), ContractDate.class);
                        int numberofclaims = 0;
                        int tranches = 0;
                        double trancheamount = 0.00;
                        double percentageA = 0.00;
                        double percentageB = 0.00;
                        double claimsamount = 0.00;
                        ACRGBWSResult totalResult = methods.GETSUMMARY(dataSource, MapContract.getHcfid());
                        if (totalResult.isSuccess()) {
                            Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                            tranches += Integer.parseInt(getResult.getCcount());
                            trancheamount += Double.parseDouble(getResult.getCtotal());
                        }
                        //======================================
                        ACRGBWSResult sumresult = fm.GETNCLAIMS(dataSource, MapContract.getHcfid().trim(), "G", condate.getDatefrom(), condate.getDateto(), "CURRENTSTATUS");
                        if (sumresult.isSuccess()) {
                            List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                            for (int i = 0; i < nclaimsdata.size(); i++) {
                                numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                claimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
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
                        double sumA = (trancheamount / Double.parseDouble(MapContract.getAmount())) * 100;
                        if (sumA > 100) {
                            Double negvalue = 100 - sumA;
                            percentageA += negvalue;
                        } else {
                            percentageA += sumA;
                        }
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
                result.setMessage(methods.GETROLE(dataSource, userid, tags).getMessage());
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT OF HCPN USING ACCOUNT USERID
    public ACRGBWSResult GETCONTRACTOFHCPN(final DataSource dataSource, final String tags, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try {
            ACRGBWSResult GetRole = methods.GETROLE(dataSource, userid, "ACTIVE");
            if (GetRole.isSuccess()) {
                //GETCONTRACT
                ACRGBWSResult GetFacilityContract = this.GETCONTRACT(dataSource, tags, GetRole.getResult().trim());
                if (GetFacilityContract.isSuccess()) {
                    Contract MapContract = utility.ObjectMapper().readValue(GetFacilityContract.getResult(), Contract.class);
                    if (MapContract.getContractdate() != null) {
                        Contract contract = new Contract();
                        ACRGBWSResult GetHCPN = methods.GETMBWITHID(dataSource, GetRole.getResult());
                        if (GetHCPN.isSuccess()) {
                            contract.setHcfid(GetHCPN.getResult());
                        }
                        //END OF GET NETWORK FULL DETAILS
                        contract.setAmount(MapContract.getAmount());
                        contract.setStats(MapContract.getStats());
                        contract.setCreatedby(MapContract.getCreatedby());
                        contract.setDatecreated(MapContract.getDatecreated());//resultset.getString("DATECREATED"));
                        contract.setContractdate(MapContract.getContractdate());
                        contract.setTranscode(MapContract.getTranscode());
                        contract.setBaseamount(MapContract.getBaseamount());
                        contract.setComittedClaimsVol(MapContract.getComittedClaimsVol());
                        contract.setComputedClaimsVol(MapContract.getComputedClaimsVol());
                        contract.setSb(MapContract.getSb());
                        contract.setAddamount(MapContract.getAddamount());
                        //=============================================
                        ContractDate condate = utility.ObjectMapper().readValue(MapContract.getContractdate(), ContractDate.class);
                        int numberofclaims = 0;
                        double percentageA = 0.00;
                        double percentageB = 0.00;
                        int tranches = 0;
                        double trancheamount = 0.00;
                        double claimsamount = 0.00;
                        //GET ALL FACILITY UNDER HCPN
                        ACRGBWSResult GetAccessList = methods.GETROLEMULITPLE(dataSource, GetRole.getResult().trim(), tags.trim());
                        if (GetAccessList.isSuccess()) {
                            List<String> HCIList = Arrays.asList(GetAccessList.getResult().trim().split(","));
                            for (int x = 0; x < HCIList.size(); x++) {
                                ACRGBWSResult totalResult = methods.GETSUMMARY(dataSource, HCIList.get(x).trim());
                                if (totalResult.isSuccess()) {
                                    Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                                    tranches += Integer.parseInt(getResult.getCcount());
                                    trancheamount += Double.parseDouble(getResult.getCtotal());
                                }
                                //======================================
                                ACRGBWSResult sumresult = fm.GETNCLAIMS(dataSource, HCIList.get(x).trim(), "G", condate.getDatefrom(), condate.getDateto(), "CURRENTSTATUS");
                                if (sumresult.isSuccess()) {
                                    List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                                    for (int i = 0; i < nclaimsdata.size(); i++) {
                                        numberofclaims += Integer.parseInt(nclaimsdata.get(i).getTotalclaims());
                                        claimsamount += Double.parseDouble(nclaimsdata.get(i).getClaimamount());
                                    }
                                }
                            }
                        }
                        double recievedamount = Double.parseDouble(MapContract.getAmount());
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
                result.setMessage(methods.GETROLE(dataSource, userid, tags).getMessage());
            }
        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(FetchMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET CONTRACT OF PRO USING ACCOUNT USERID
    public ACRGBWSResult GETCONTRACTOFPRO(final DataSource dataSource, final String tags, final String userid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        try {
            ACRGBWSResult GetRole = methods.GETROLE(dataSource, userid, tags);
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
                ACRGBWSResult GetFacilityContract = this.GETCONTRACTList(dataSource, tags, GetRole.getResult().trim());
                if (GetFacilityContract.isSuccess()) {
                    List<Contract> MapContract = Arrays.asList(utility.ObjectMapper().readValue(GetFacilityContract.getResult(), Contract[].class));
                    //--------------------------------------------------------
                    for (int w = 0; w < MapContract.size(); w++) {
                        Contract contract = new Contract();
                        //--------------------------------------
                        ACRGBWSResult GetHCPN = methods.GetProWithPROID(dataSource, GetRole.getResult());
                        if (GetHCPN.isSuccess()) {
                            contract.setHcfid(GetHCPN.getResult());
                        }
                        //END OF GET NETWORK FULL DETAILS
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

                    }
                    ACRGBWSResult GetHCPNList = methods.GETROLEMULITPLE(dataSource, GetRole.getResult().trim(), tags.trim());
                    if (GetHCPNList.isSuccess()) {
                        List<String> HCPNList = Arrays.asList(GetHCPNList.getResult().split(","));
                        for (int y = 0; y < HCPNList.size(); y++) {
                            //GET ALL FACILITY UNDER HCPN
                            ACRGBWSResult totalResult = methods.GETSUMMARY(dataSource, HCPNList.get(y).trim());
                            if (totalResult.isSuccess()) {
                                Total getResult = utility.ObjectMapper().readValue(totalResult.getResult(), Total.class);
                                tranches += Integer.parseInt(getResult.getCcount());
                                tracnheamountreleased += Double.parseDouble(getResult.getCtotal());
                            }
                            //GET CONTRACT PER HCPN
                            ACRGBWSResult GetHCPNContract = this.GETCONTRACT(dataSource, tags, HCPNList.get(y).trim());
                            if (GetHCPNContract.isSuccess()) {
                                Contract MapHCPNContract = utility.ObjectMapper().readValue(GetHCPNContract.getResult(), Contract.class);
                                if (MapHCPNContract.getContractdate() != null) {
                                    ContractDate condate = utility.ObjectMapper().readValue(MapHCPNContract.getContractdate(), ContractDate.class);
                                    ACRGBWSResult GetHCIList = methods.GETROLEMULITPLE(dataSource, HCPNList.get(y).trim(), tags.trim());
                                    if (GetHCIList.isSuccess()) {
                                        List<String> HCIList = Arrays.asList(GetHCIList.getResult().split(","));
                                        for (int x = 0; x < HCIList.size(); x++) {
                                            ACRGBWSResult sumresult = fm.GETNCLAIMS(dataSource, HCIList.get(x).trim(), "G", condate.getDatefrom(), condate.getDateto(), "CURRENTSTATUS");
                                            if (sumresult.isSuccess()) {
                                                List<NclaimsData> nclaimsdata = Arrays.asList(utility.ObjectMapper().readValue(sumresult.getResult(), NclaimsData[].class));
                                                for (int i = 0; i < nclaimsdata.size(); i++) {
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
            result.setMessage(ex.toString());
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
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETCONBYCODE(:ucode); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ucode", ucode.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<Contract> conlist = new ArrayList<>();
            while (resultset.next()) {
                Contract con = new Contract();
                con.setConid(resultset.getString("CONID"));
                con.setHcfid(resultset.getString("HCFID"));
                con.setAmount(resultset.getString("AMOUNT"));
                con.setStats(resultset.getString("STATS"));
                ACRGBWSResult creator = fm.GETFULLDETAILS(dataSource, resultset.getString("CREATEDBY").trim());
                if (creator.isSuccess()) {
                    UserInfo userinfos = utility.ObjectMapper().readValue(creator.getResult(), UserInfo.class);
                    con.setCreatedby(userinfos.getLastname() + ", " + userinfos.getFirstname());
                } else {
                    con.setCreatedby(creator.getMessage());
                }
                con.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
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
                if (resultset.getDate("ENDDATE") == null) {
                    con.setEnddate("N/A");
                } else {
                    con.setEnddate(dateformat.format(resultset.getDate("ENDDATE")));
                }
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
            result.setMessage(ex.toString());
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
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETROLEINDEXCONDATE(:pcondate); end;");
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
                roleindex.setDatecreated(dateformat.format(resultset.getDate("DATECREATED")));
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
            result.setMessage(ex.toString());
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
