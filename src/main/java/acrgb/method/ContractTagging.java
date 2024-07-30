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
import java.sql.SQLException;
import java.text.ParseException;
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
public class ContractTagging {

    private final Utility utility = new Utility();
    private final FetchMethods fm = new FetchMethods();

    public ACRGBWSResult TAGGINGCONTRACT(final DataSource datasource, final String tags, final Contract con) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        UpdateMethods updateMethod = new UpdateMethods();
        ArrayList<String> error = new ArrayList<>();
        try {
            switch (tags.trim().toUpperCase()) {
                case "HCI": {
                    //END CONTRACT USING HCI CODE
                    ACRGBWSResult contractList = fm.GETCONTRACTCONID(datasource, con.getConid().trim(), "ACTIVE".trim());
                    if (contractList.isSuccess()) {
                        Contract contract = utility.ObjectMapper().readValue(contractList.getResult(), Contract.class);
                        if (!contract.getContractdate().isEmpty()) {
                            ContractDate conDate = utility.ObjectMapper().readValue(contract.getContractdate(), ContractDate.class);
                            //UPDATE CONTRACT AND ASSETS UNDER 
                            ACRGBWSResult updateConANDAssets = updateMethod.CONSTATSUPDATE(datasource, con.getConid(), con.getStats(), con.getRemarks(), con.getEnddate());
                            if (!updateConANDAssets.isSuccess()) {
                                error.add(updateConANDAssets.getMessage());
                            }
                            //UPDATE ROLE INDEX 
                            ACRGBWSResult updateAccessID = updateMethod.UPDATEMAPPEDROLEBASECONDATE(datasource, contract.getHcfid(), conDate.getCondateid(), "HCI");
                            if (!updateAccessID.isSuccess()) {
                                error.add(updateAccessID.getMessage());
                            }
                        }
                    } else {
                        error.add("No Contract Found");
                    }
                    result.setSuccess(true);
                    break;
                }
                case "HCPN": {
                    ACRGBWSResult contractList = fm.GETCONTRACTCONID(datasource, con.getConid().trim(), "ACTIVE".trim());
                    if (contractList.isSuccess()) {
                        Contract contract = utility.ObjectMapper().readValue(contractList.getResult(), Contract.class);
                        if (!contract.getContractdate().isEmpty()) {
                            ContractDate conDate = utility.ObjectMapper().readValue(contract.getContractdate(), ContractDate.class);
                            //UPDATE CONTRACT AND ASSETS UNDER 
                            ACRGBWSResult updateConANDAssets = updateMethod.CONSTATSUPDATE(datasource, con.getConid().trim(), con.getStats(), con.getRemarks(), con.getEnddate());
                            if (!updateConANDAssets.isSuccess()) {
                                error.add(updateConANDAssets.getMessage());
                            }
                            //UPDATE ROLE INDEX 
                            ACRGBWSResult updateAccessID = updateMethod.UPDATEMAPPEDROLEBASECONDATE(datasource, contract.getHcfid(), conDate.getCondateid(), "HCPN");
                            if (!updateAccessID.isSuccess()) {
                                error.add(updateAccessID.getMessage());
                            }
                        }
                        // GET FACILITY UNDER SELECTED HPCN
                        ACRGBWSResult getRole = methods.GETROLEMULITPLE(datasource, contract.getHcfid().trim(), "ACTIVE");
                        if (getRole.isSuccess()) {
                            List<String> listRole = Arrays.asList(getRole.getResult().split(","));
                            for (int i = 0; i < listRole.size(); i++) {
                                ACRGBWSResult contractHCIList = fm.GETCONBYCODE(datasource, listRole.get(i).trim());
                                if (contractHCIList.isSuccess()) {
                                    //MAPPED CONTRACT
                                    Contract hciContract = utility.ObjectMapper().readValue(contractHCIList.getResult(), Contract.class);
                                    if (hciContract.getContractdate() != null) {
                                        //MAPPED CONTRACT DATE
                                        ContractDate conDate = utility.ObjectMapper().readValue(hciContract.getContractdate(), ContractDate.class);
                                        //UPDATE CONTRACT AND ASSETS UNDER 
                                        ACRGBWSResult updateConANDAssets = updateMethod.CONSTATSUPDATE(datasource, hciContract.getConid().trim(), con.getStats(), hciContract.getRemarks(), con.getEnddate());
                                        if (!updateConANDAssets.isSuccess()) {
                                            error.add("Contract and Assets " + updateConANDAssets.getMessage());
                                        }
                                        //UPDATE ROLE INDEX 
                                        ACRGBWSResult updateAccessID = updateMethod.UPDATEMAPPEDROLEBASECONDATE(datasource, listRole.get(i).trim(), conDate.getCondateid(), "HCI");
                                        if (!updateAccessID.isSuccess()) {
                                            error.add(updateAccessID.getMessage());
                                        }
                                    }
                                }
                            }
                        } else {
                            error.add("No Facility Found Under HCPN " + contract.getHcfid().trim());
                        }
                    } else {
                        error.add("No Contract Found");
                    }
                    result.setSuccess(true);
                    break;
                }
                default:
                    error.add("Tags Not Found");
                    break;
            }

            result.setMessage(String.join(",", error));
            result.setResult("");

        } catch (ParseException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractTagging.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult EndContractUsingDateid(final DataSource datasource, final String dateid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        String error = "";
        ContractMethod methods = new ContractMethod();
        UpdateMethods updateMethod = new UpdateMethods();
        try (Connection connection = datasource.getConnection()) {
            //GET LIST OF CONTRACT USING CONTRACT DATE ID
            ACRGBWSResult getContractList = methods.GETCONTRACTBYCONDATEID(datasource, dateid.trim());
            if (getContractList.isSuccess()) {
                List<Contract> contractList = Arrays.asList(utility.ObjectMapper().readValue(getContractList.getResult(), Contract[].class));
                for (int i = 0; i < contractList.size(); i++) {
                    if (!contractList.get(i).getContractdate().isEmpty()) {
                        //MAPPED CONTRACT DATE
                        ContractDate conDate = utility.ObjectMapper().readValue(contractList.get(i).getContractdate(), ContractDate.class);
                        //UPDATE CONTRACT AND ASSETS UNDER 
                        ACRGBWSResult updateConANDAssets = updateMethod.CONSTATSUPDATE(datasource, contractList.get(i).getConid(), "3".trim(), "CONTRACT ENDED", conDate.getDateto().trim());
                        if (!updateConANDAssets.isSuccess()) {
                            error = updateConANDAssets.getMessage();
                        }
                    }
                }
            }
            //END MAPPED ROLE INDEX STATUS TO 3
            ACRGBWSResult updatecondate = updateMethod.UPDATEROLEINDEX(datasource, "00", dateid.trim(), "NONUPDATE");
            //------------------------------------------------------------------------------------------------------------------------
            //TAGGING OF CONTRACT PERIOD TO END CHANGE STATUS TO 3
            CallableStatement statement = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.TAGCONTRACTPERIOD(:Message,:Code,:pcondateid)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            statement.setString("pcondateid", dateid.trim());
            statement.execute();
            if (statement.getString("Message").equals("SUCC")) {
                result.setMessage(statement.getString("Message") + " " + error + " " + updatecondate.getMessage());
                result.setSuccess(true);
            } else {
                result.setMessage(statement.getString("Message") + " " + error + " " + updatecondate.getMessage());
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ContractTagging.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//    public ACRGBWSResult EndContractUsingContractPeriod(final DataSource datasource, final String dateid) throws ParseException {
//        ACRGBWSResult result = utility.ACRGBWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        String error = "";
//        ContractMethod methods = new ContractMethod();
//        UpdateMethods updateMethod = new UpdateMethods();
//        try (Connection connection = datasource.getConnection()) {
//            
//            //GET LIST OF CONTRACT USING CONTRACT DATE ID
//            ACRGBWSResult getContractList = methods.GETCONTRACTBYCONDATEID(datasource, dateid.trim());
//            if (getContractList.isSuccess()) {
//                List<Contract> contractList = Arrays.asList(utility.ObjectMapper().readValue(getContractList.getResult(), Contract[].class));
//                for (int i = 0; i < contractList.size(); i++) {
//                    if (!contractList.get(i).getContractdate().isEmpty()) {
//                        //MAPPED CONTRACT DATE
//                        ContractDate conDate = utility.ObjectMapper().readValue(contractList.get(i).getContractdate(), ContractDate.class);
//                        //UPDATE CONTRACT AND ASSETS UNDER 
//                        ACRGBWSResult updateConANDAssets = updateMethod.CONSTATSUPDATE(datasource, contractList.get(i).getConid(), "3".trim(), "CONTRACT ENDED", conDate.getDateto().trim());
//                        if (!updateConANDAssets.isSuccess()) {
//                            error = updateConANDAssets.getMessage();
//                        }
//                    }
//                }
//            }
//            //TAGGING OF CONTRACT PERIOD TO END CHANGE STATUS TO 3
//            CallableStatement statement = connection.prepareCall("call ACR_GB.ACRGBPKGUPDATEDETAILS.TAGCONTRACTPERIOD(:Message,:Code,:pcondateid)");
//            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
//            statement.registerOutParameter("Code", OracleTypes.INTEGER);
//            statement.setString("pcondateid", dateid.trim());
//            statement.execute();
//            if (statement.getString("Message").equals("SUCC")) {
//                result.setMessage(statement.getString("Message") + " " + error);
//                result.setSuccess(true);
//            } else {
//                result.setMessage(statement.getString("Message") + " " + error);
//            }
//        } catch (SQLException | IOException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(ContractTagging.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
}
