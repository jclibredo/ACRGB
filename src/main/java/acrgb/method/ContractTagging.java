/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.UserRoleIndex;
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

    public ACRGBWSResult TAGGINGCONTRACT(final DataSource dataSource, final String tags, final Contract con) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        Methods methods = new Methods();
        UpdateMethods updateMethod = new UpdateMethods();
        InsertMethods im = new InsertMethods();
        ArrayList<String> error = new ArrayList<>();
        try {
            switch (tags.trim().toUpperCase()) {
                case "HCI": {
                    //END CONTRACT USING HCI CODE
                    ACRGBWSResult contractList = fm.GETCONTRACTCONID(dataSource, con.getConid().trim(), "ACTIVE".trim());
                    if (contractList.isSuccess()) {
                        Contract contract = utility.ObjectMapper().readValue(contractList.getResult(), Contract.class);
                        ACRGBWSResult getRoleList = fm.GETROLEINDEXUSERIDHCI(dataSource, contract.getHcfid());
                        if (getRoleList.isSuccess()) {
                            UserRoleIndex role = utility.ObjectMapper().readValue(getRoleList.getResult(), UserRoleIndex.class);
                            //PROCESS REMAP FACILITY TO HCPN THIS AREA WITH ACTIVE CONTRACT
                            UserRoleIndex userRole = new UserRoleIndex();
                            userRole.setDatecreated(con.getEnddate());
                            userRole.setCreatedby(con.getCreatedby());
                            userRole.setUserid(role.getUserid().trim());
                            userRole.setAccessid(role.getAccessid().trim());
                            ACRGBWSResult insertRoleIndex = im.INSEROLEINDEX(dataSource, userRole);
                            if (!insertRoleIndex.isSuccess()) {
                                error.add(insertRoleIndex.getMessage());
                            }
                        }
                        if (!contract.getContractdate().isEmpty()) {
                            ContractDate conDate = utility.ObjectMapper().readValue(contract.getContractdate(), ContractDate.class);
                            //UPDATE CONTRACT AND ASSETS UNDER 
                            ACRGBWSResult updateConANDAssets = updateMethod.CONSTATSUPDATE(dataSource, con.getConid(), con.getStats(), con.getRemarks(), con.getEnddate());
                            if (!updateConANDAssets.isSuccess()) {
                                error.add(updateConANDAssets.getMessage());
                            }
                            //UPDATE ROLE INDEX 
                            ACRGBWSResult updateAccessID = updateMethod.UPDATEMAPPEDROLEBASECONDATE(dataSource, contract.getHcfid(), conDate.getCondateid());
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
                    ACRGBWSResult contractList = fm.GETCONTRACTCONID(dataSource, con.getConid().trim(), "ACTIVE".trim());
                    if (contractList.isSuccess()) {
                        Contract contract = utility.ObjectMapper().readValue(contractList.getResult(), Contract.class);
                        ACRGBWSResult getRoleList = fm.GETROLEINDEXUSERID(dataSource, contract.getHcfid());
                        if (getRoleList.isSuccess()) {
                            List<UserRoleIndex> roleList = Arrays.asList(utility.ObjectMapper().readValue(getRoleList.getResult(), UserRoleIndex[].class));
                            for (int h = 0; h < roleList.size(); h++) {
                                //PROCESS REMAP FACILITY TO HCPN THIS AREA WITH ACTIVE CONTRACT
                                UserRoleIndex userRole = new UserRoleIndex();
                                userRole.setDatecreated(con.getEnddate());
                                userRole.setCreatedby(con.getCreatedby());
                                userRole.setUserid(roleList.get(h).getUserid().trim());
                                userRole.setAccessid(roleList.get(h).getAccessid().trim());
                                ACRGBWSResult insertRoleIndex = im.INSEROLEINDEX(dataSource, userRole);
                                if (!insertRoleIndex.isSuccess()) {
                                    error.add(insertRoleIndex.getMessage());
                                }
                            }
                        }
                        if (!contract.getContractdate().isEmpty()) {
                            ContractDate conDate = utility.ObjectMapper().readValue(contract.getContractdate(), ContractDate.class);
                            //UPDATE CONTRACT AND ASSETS UNDER 
                            ACRGBWSResult updateConANDAssets = updateMethod.CONSTATSUPDATE(dataSource, con.getConid().trim(), con.getStats(), con.getRemarks(), con.getEnddate());
                            if (!updateConANDAssets.isSuccess()) {
                                error.add(updateConANDAssets.getMessage());
                            }
                            // GET FACILITY UNDER SELECTED HPCN
                            ACRGBWSResult getRole = methods.GETROLEMULITPLE(dataSource, contract.getHcfid().trim(), "ACTIVE");
                            if (getRole.isSuccess()) {
                                List<String> listRole = Arrays.asList(getRole.getResult().split(","));
                                for (int i = 0; i < listRole.size(); i++) {
                                    ACRGBWSResult contractHCIList = fm.GETCONBYCODE(dataSource, listRole.get(i).trim());
                                    if (contractHCIList.isSuccess()) {
                                        //MAPPED CONTRACT
                                        Contract hciContract = utility.ObjectMapper().readValue(contractHCIList.getResult(), Contract.class);
                                        if (hciContract.getContractdate() != null) {
                                            //MAPPED CONTRACT DATE
                                            ContractDate conDates = utility.ObjectMapper().readValue(hciContract.getContractdate(), ContractDate.class);
                                            //UPDATE CONTRACT AND ASSETS UNDER 
                                            ACRGBWSResult updateConANDAssetss = updateMethod.CONSTATSUPDATE(dataSource, hciContract.getConid().trim(), con.getStats(), hciContract.getRemarks(), con.getEnddate());
                                            if (!updateConANDAssetss.isSuccess()) {
                                                error.add("Contract and Assets " + updateConANDAssetss.getMessage());
                                            }
                                            //UPDATE ROLE INDEX 
                                            ACRGBWSResult updateAccessIDs = updateMethod.UPDATEMAPPEDROLEBASECONDATE(dataSource, listRole.get(i).trim(), conDates.getCondateid());
                                            if (!updateAccessIDs.isSuccess()) {
                                                error.add(updateAccessIDs.getMessage());
                                            }
                                        }
                                    }
                                }
                            }
                            //UPDATE ROLE INDEX 
                            ACRGBWSResult updateAccessID = updateMethod.UPDATEMAPPEDROLEBASECONDATE(dataSource, contract.getHcfid().trim(), conDate.getCondateid().trim());
                            if (!updateAccessID.isSuccess()) {
                                error.add(updateAccessID.getMessage());
                            }
                            //GET MAPPED ACTIVE FACILITY WITH CONTRACT 
                            //System.out.println("|" + contract.getHcfid() + "|");

                        } else {
                            error.add("No Facility Found Under HCPN " + contract.getHcfid().trim());
                        }
                        //REMAP SELECTED HCPN TO PRO
                        UserRoleIndex userRole = new UserRoleIndex();
                        userRole.setDatecreated(con.getEnddate());
                        userRole.setCreatedby(con.getCreatedby());
                        userRole.setUserid(methods.GETROLE(dataSource, con.getCreatedby(), "ACTIVE").getResult().trim());
                        userRole.setAccessid(contract.getHcfid().trim());
                        ACRGBWSResult insertRoleIndex = im.INSEROLEINDEX(dataSource, userRole);
                        if (!insertRoleIndex.isSuccess()) {
                            error.add(insertRoleIndex.getMessage());
                        }
                        //END REMAP SELECTED HCPN TO PRO
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

    public ACRGBWSResult EndContractUsingDateid(final DataSource dataSource, final String dateid) throws ParseException {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<String> error = new ArrayList<>();
        ContractMethod methods = new ContractMethod();
        UpdateMethods updateMethod = new UpdateMethods();
        InsertMethods insertMethods = new InsertMethods();
        try (Connection connection = dataSource.getConnection()) {
            //GET ACTIVE ROLE INDEX MAPPED ACCOUNT
            ACRGBWSResult remapRoleIndex = methods.GETROLEINDEXCONDATE(dataSource, dateid.trim());
            if (remapRoleIndex.isSuccess()) {
                //GET RESULT AND REMAP VALUE
                List<UserRoleIndex> roleList = Arrays.asList(utility.ObjectMapper().readValue(remapRoleIndex.getResult(), UserRoleIndex[].class));
                for (int x = 0; x < roleList.size(); x++) {    
                    UserRoleIndex newRole = new UserRoleIndex();
                    newRole.setAccessid(roleList.get(x).getAccessid());
                    newRole.setUserid(roleList.get(x).getUserid());
                    newRole.setCreatedby(roleList.get(x).getCreatedby());
                    newRole.setDatecreated(roleList.get(x).getDatecreated());
                    ACRGBWSResult insertRole = insertMethods.INSEROLEINDEX(dataSource, newRole);
                    if (!insertRole.isSuccess()) {
                        error.add(insertRole.getMessage());
                    }
                }
            }
            //GET LIST OF CONTRACT USING CONTRACT DATE ID
            ACRGBWSResult getContractList = methods.GETCONTRACTBYCONDATEID(dataSource, dateid.trim());
            if (getContractList.isSuccess()) {
                List<Contract> contractList = Arrays.asList(utility.ObjectMapper().readValue(getContractList.getMessage(), Contract[].class));
                for (int i = 0; i < contractList.size(); i++) {
                    if (!contractList.get(i).getContractdate().isEmpty()) {
                        //MAPPED CONTRACT DATE
                        ContractDate conDate = utility.ObjectMapper().readValue(contractList.get(i).getContractdate(), ContractDate.class);
                        //UPDATE CONTRACT AND ASSETS UNDER 
                        ACRGBWSResult updateConANDAssets = updateMethod.CONSTATSUPDATE(dataSource, contractList.get(i).getConid(), "3".trim(), "CONTRACT ENDED", conDate.getDateto().trim());
                        if (!updateConANDAssets.isSuccess()) {
                            error.add(updateConANDAssets.getMessage());
                        }
                    }
                }
            }
            //END MAPPED ROLE INDEX STATUS TO 3
            ACRGBWSResult updatecondate = updateMethod.UPDATEROLEINDEX(dataSource, "00", "00", dateid.trim(), "NONUPDATE");
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
