/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
                    statement.setString("pfchid", phcfcode);
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
                        } else {
                            contract.setContractdate(getcondateA.getMessage());
                        }
                        contract.setTranscode(resultset.getString("TRANSCODE"));
                        contract.setBaseamount(resultset.getString("BASEAMOUNT"));
                        contract.setHcfid(resultset.getString("HCFID"));
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
                    statements.setString("pfchid", phcfcode);
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
                        } else {
                            contracts.setContractdate(getcondateA.getMessage());
                        }
                        contracts.setTranscode(resultsets.getString("TRANSCODE"));
                        contracts.setBaseamount(resultsets.getString("BASEAMOUNT"));
                        contracts.setHcfid(resultsets.getString("HCFID"));
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
            statement.setString("pfchid", hcfid);
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
                } else {
                    contract.setContractdate(getcondateA.getMessage());
                }
                contract.setTranscode(resultset.getString("TRANSCODE"));
                contract.setBaseamount(resultset.getString("BASEAMOUNT"));
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
    public ACRGBWSResult GETCONDATE(final DataSource dataSource, final String tags) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETCONDATE(:tags); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("tags", tags.toUpperCase());
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
            statement.setString("ucondateid", ucondateid);
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
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKGFUNCTION.GETCONTRACTBYCONDATEID(:ucondateid); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("ucondateid", ucondateid);
            statement.execute();
            // ArrayList<Contract> contractList = new ArrayList<>();
            ArrayList<String> conidlist = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                conidlist.add(resultset.getString("CONID"));
            }
            if (!conidlist.isEmpty()) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.ObjectMapper().writeValueAsString(conidlist));
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
