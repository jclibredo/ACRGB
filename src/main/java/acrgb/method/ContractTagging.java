/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method;

import acrgb.structure.ACRGBWSResult;
import acrgb.structure.Appellate;
import acrgb.structure.Contract;
import acrgb.structure.ContractDate;
import acrgb.structure.UserRoleIndex;
import acrgb.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
public class ContractTagging {

    private final Utility utility = new Utility();

    public ACRGBWSResult TAGGINGCONTRACT(final DataSource dataSource, final String tags, final Contract con) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<String> error = new ArrayList<>();
        try {
            switch (tags.trim().toUpperCase()) {
                case "HCI": {
                    //END CONTRACT USING HCI CODE
                    ACRGBWSResult contractList = new FetchMethods().GETCONTRACTCONID(dataSource, con.getConid().trim(), "ACTIVE".trim());
                    if (contractList.isSuccess()) {
                        Contract contract = utility.ObjectMapper().readValue(contractList.getResult(), Contract.class);
                        ACRGBWSResult getRoleList = new FetchMethods().GETROLEINDEXUSERIDHCI(dataSource, contract.getHcfid());
                        if (getRoleList.isSuccess()) {
                            UserRoleIndex role = utility.ObjectMapper().readValue(getRoleList.getResult(), UserRoleIndex.class);
                            //PROCESS REMAP FACILITY TO HCPN THIS AREA WITH ACTIVE CONTRACT
                            UserRoleIndex userRole = new UserRoleIndex();
                            userRole.setDatecreated(con.getEnddate());
                            userRole.setCreatedby(con.getCreatedby());
                            userRole.setUserid(role.getUserid().trim());
                            userRole.setAccessid(role.getAccessid().trim());
                            ACRGBWSResult insertRoleIndex = new InsertMethods().INSEROLEINDEX(dataSource, userRole);
                            if (!insertRoleIndex.isSuccess()) {
                                error.add(insertRoleIndex.getMessage());
                            }
                        }
                        //UPDATE CONTRACT AND ASSETS UNDER 
                        ACRGBWSResult updateConANDAssets = new UpdateMethods().CONSTATSUPDATE(dataSource, con.getConid(), con.getStats(), con.getRemarks(), con.getEnddate());
                        if (!updateConANDAssets.isSuccess()) {
                            error.add(updateConANDAssets.getMessage());
                        }
                        //UPDATE ROLE INDEX 
                        ACRGBWSResult updateAccessID = new UpdateMethods().UPDATEROLEINDEXBYACCESSID(dataSource, contract.getHcfid());
                        if (!updateAccessID.isSuccess()) {
                            error.add(updateAccessID.getMessage());
                        }
                        result.setSuccess(true);
                        //AUTO BOOK CLAIMS UNDER CONTRACT OF SELECTED APEX FACILITY
//                        Book book = new Book();
//                        book.setBooknum("ACRGB" + utility.SimpleDateFormat("MMddyyyyHHmmss").format(new java.util.Date()));
//                        book.setConid(con.getConid());
//                        book.setHcpncode(contract.getHcfid());
//                        book.setCreatedby(con.getCreatedby());
//                        book.setDatecreated(utility.SimpleDateFormat("MM-dd-yyyy").format(new java.util.Date()));
//                        book.setTags("FACILITY");
//                        ACRGBWSResult bookingResult = bm.PROCESSENDEDCONTRACT(dataSource, book, "INACTIVE");
//                        if (!bookingResult.isSuccess()) {
//                            error.add(bookingResult.getMessage());
//                        }
                    } else {
                        error.add("No Contract Found");
                    }
                    break;
                }
                case "HCPN": {
                    ACRGBWSResult contractList = new FetchMethods().GETCONTRACTCONID(dataSource, con.getConid().trim(), "ACTIVE".trim());
                    if (contractList.isSuccess()) {
                        Contract contract = utility.ObjectMapper().readValue(contractList.getResult(), Contract.class);
                        ACRGBWSResult getRoleList = new FetchMethods().GETROLEINDEXUSERID(dataSource, contract.getHcfid());
                        if (getRoleList.isSuccess()) {
                            List<UserRoleIndex> roleList = Arrays.asList(utility.ObjectMapper().readValue(getRoleList.getResult(), UserRoleIndex[].class));
                            for (int h = 0; h < roleList.size(); h++) {
                                //PROCESS REMAP FACILITY TO HCPN THIS AREA WITH ACTIVE CONTRACT
                                UserRoleIndex userRole = new UserRoleIndex();
                                userRole.setDatecreated(con.getEnddate());
                                userRole.setCreatedby(con.getCreatedby());
                                userRole.setUserid(roleList.get(h).getUserid().trim());
                                userRole.setAccessid(roleList.get(h).getAccessid().trim());
                                ACRGBWSResult insertRoleIndex = new InsertMethods().INSEROLEINDEX(dataSource, userRole);
                                if (!insertRoleIndex.isSuccess()) {
                                    error.add(insertRoleIndex.getMessage());
                                }
                            }
                        }
                        if (!contract.getContractdate().isEmpty()) {
                            //UPDATE CONTRACT AND ASSETS UNDER 
                            ACRGBWSResult updateConANDAssets = new UpdateMethods().CONSTATSUPDATE(dataSource, con.getConid().trim(), con.getStats(), con.getRemarks(), con.getEnddate());
                            if (!updateConANDAssets.isSuccess()) {
                                error.add(updateConANDAssets.getMessage());
                            }
                            // GET FACILITY UNDER SELECTED HPCN
                            ACRGBWSResult getRole = new Methods().GETROLEMULITPLE(dataSource, contract.getHcfid().trim(), "ACTIVE");
                            if (getRole.isSuccess()) {
                                List<String> listRole = Arrays.asList(getRole.getResult().split(","));
                                for (int i = 0; i < listRole.size(); i++) {
                                    ACRGBWSResult contractHCIList = new FetchMethods().GETCONBYCODE(dataSource, listRole.get(i).trim());
                                    if (contractHCIList.isSuccess()) {
                                        //MAPPED CONTRACT
                                        Contract hciContract = utility.ObjectMapper().readValue(contractHCIList.getResult(), Contract.class);
                                        //UPDATE CONTRACT AND ASSETS UNDER 
                                        ACRGBWSResult updateConANDAssetss = new UpdateMethods().CONSTATSUPDATE(dataSource, hciContract.getConid().trim(), con.getStats(), hciContract.getRemarks(), con.getEnddate());
                                        if (!updateConANDAssetss.isSuccess()) {
                                            error.add("Contract and Assets " + updateConANDAssetss.getMessage());
                                        }
                                        //UPDATE ROLE INDEX 
                                        ACRGBWSResult updateAccessIDs = new UpdateMethods().UPDATEROLEINDEXBYACCESSID(dataSource, listRole.get(i).trim());
                                        if (!updateAccessIDs.isSuccess()) {
                                            error.add(updateAccessIDs.getMessage());
                                        }
                                    }

                                }
                            }
                            //UPDATE ROLE INDEX 
                            ACRGBWSResult updateAccessID = new UpdateMethods().UPDATEROLEINDEXBYACCESSID(dataSource, contract.getHcfid().trim());
                            if (!updateAccessID.isSuccess()) {
                                error.add(updateAccessID.getMessage());
                            }
                        } else {
                            error.add("No Facility Found Under HCPN " + contract.getHcfid().trim());
                        }
                        //REMAP SELECTED HCPN TO PRO
                        UserRoleIndex userRole = new UserRoleIndex();
                        userRole.setDatecreated(con.getEnddate());
                        userRole.setCreatedby(con.getCreatedby());
                        userRole.setUserid(new Methods().GETROLE(dataSource, con.getCreatedby(), "ACTIVE").getResult().trim());
                        userRole.setAccessid(contract.getHcfid().trim());
                        ACRGBWSResult insertRoleIndex = new InsertMethods().INSEROLEINDEX(dataSource, userRole);
                        if (!insertRoleIndex.isSuccess()) {
                            error.add(insertRoleIndex.getMessage());
                        }
                        //END REMAP SELECTED HCPN TO PRO
                        result.setSuccess(true);
                        //AUTO BOOK CLAIMS DATA UNDER SELECTED HCPN
//                        Book book = new Book();
//                        book.setBooknum("ACRGB" + utility.SimpleDateFormat("MMddyyyyHHmmss").format(new java.util.Date()));
//                        book.setConid(con.getConid());
//                        book.setHcpncode(contract.getHcfid());
//                        book.setCreatedby(con.getCreatedby());
//                        book.setDatecreated(utility.SimpleDateFormat("MM-dd-yyyy").format(new java.util.Date()));
//                        book.setTags("HCPN");
//                        ACRGBWSResult bookingResult = bm.PROCESSENDEDCONTRACT(dataSource, book, "INACTIVE");
//                        if (!bookingResult.isSuccess()) {
//                            error.add(bookingResult.getMessage());
//                        }
                        ACRGBWSResult pfResult = new ProcessAffiliate().GETAFFILIATE(dataSource, "0", contract.getHcfid(), "0");
                        if (pfResult.isSuccess()) {
                            List<Appellate> pfList = Arrays.asList(utility.ObjectMapper().readValue(pfResult.getResult(), Appellate[].class));
                            for (int i = 0; i < pfList.size(); i++) {
                                new InsertMethods().INSERTAPPELLATE(dataSource, pfList.get(i).getAccesscode(), pfList.get(i).getControlcode(), con.getCreatedby(), con.getDatecreated());
                            }
                            //END OF AUTOBBOK
                            Appellate appellate = new Appellate();
                            appellate.setAccesscode(contract.getHcfid());
                            appellate.setStatus("3");
                            appellate.setConid(contract.getContractdate());
                            //update affiliated facility under hcpn
                            ACRGBWSResult updateAffiliates = new UpdateMethods().UPDATEAPELLATE(dataSource, "HCPN", appellate);
                            if (!updateAffiliates.isSuccess()) {
                                error.add(updateAffiliates.getMessage());
                            }
                        }
                    } else {
                        error.add("No Contract Found");
                    }
                    break;
                }
                default:
                    error.add("Tags Not Found");
                    break;
            }
            result.setMessage(String.join(",", error));
            result.setResult("");
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ContractTagging.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ACRGBWSResult EndContractUsingDateid(final DataSource dataSource, final String dateid) {
        ACRGBWSResult result = utility.ACRGBWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<String> error = new ArrayList<>();
        String createdwho = "";
        try (Connection connection = dataSource.getConnection()) {
            //GET ACTIVE ROLE INDEX MAPPED ACCOUNT
            ACRGBWSResult remapRoleIndex = new ContractMethod().GETROLEINDEXCONDATE(dataSource, dateid.trim());
            if (remapRoleIndex.isSuccess()) {
                //GET RESULT AND REMAP VALUE
                List<UserRoleIndex> roleList = Arrays.asList(utility.ObjectMapper().readValue(remapRoleIndex.getResult(), UserRoleIndex[].class));
                for (int x = 0; x < roleList.size(); x++) {
                    UserRoleIndex newRole = new UserRoleIndex();
                    newRole.setAccessid(roleList.get(x).getAccessid());
                    newRole.setUserid(roleList.get(x).getUserid());
                    newRole.setCreatedby(roleList.get(x).getCreatedby());
                    newRole.setDatecreated(roleList.get(x).getDatecreated());
                    ACRGBWSResult insertRole = new InsertMethods().INSEROLEINDEX(dataSource, newRole);
                    if (!insertRole.isSuccess()) {
                        error.add(insertRole.getMessage());
                    }
                }
            }
            //GET LIST OF CONTRACT USING CONTRACT DATE ID
            ACRGBWSResult getContractList = new ContractMethod().GETCONTRACTBYCONDATEID(dataSource, dateid.trim());
            if (getContractList.isSuccess()) {
                List<Contract> contractList = Arrays.asList(utility.ObjectMapper().readValue(getContractList.getMessage(), Contract[].class));
                for (int i = 0; i < contractList.size(); i++) {
                    if (!contractList.get(i).getContractdate().isEmpty()) {
                        //MAPPED CONTRACT DATE
                        ContractDate conDate = utility.ObjectMapper().readValue(contractList.get(i).getContractdate(), ContractDate.class);
                        //UPDATE CONTRACT AND ASSETS UNDER 
                        ACRGBWSResult updateConANDAssets = new UpdateMethods().CONSTATSUPDATE(dataSource, contractList.get(i).getConid(), "3".trim(), "CONTRACT ENDED", conDate.getDateto().trim());
                        if (!updateConANDAssets.isSuccess()) {
                            error.add(updateConANDAssets.getMessage());
                        }
                    }
                }
                createdwho = contractList.get(0).getCreatedby();
            }
            //END MAPPED ROLE INDEX STATUS TO 3
            ACRGBWSResult updatecondate = new UpdateMethods().UPDATEROLEINDEX(dataSource, "00", "00", dateid.trim(), "NONUPDATE");
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
            //process end affiliates and remap 
            ACRGBWSResult pfResult = new ProcessAffiliate().GETAFFILIATE(dataSource, "0", "0", dateid.trim());
            if (pfResult.isSuccess()) {
                List<Appellate> pfList = Arrays.asList(utility.ObjectMapper().readValue(pfResult.getResult(), Appellate[].class));
                for (int i = 0; i < pfList.size(); i++) {
                    new InsertMethods().INSERTAPPELLATE(dataSource, pfList.get(i).getAccesscode(), pfList.get(i).getControlcode(), createdwho, utility.SimpleDateFormat("MM-dd-yyyy").format(new Date()));
                }
                Appellate appellate = new Appellate();
                appellate.setAccesscode("0");
                appellate.setStatus("3");
                appellate.setConid(dateid.trim());
                //update affiliated facility under hcpn
                new UpdateMethods().UPDATEAPELLATE(dataSource, "UOTHERS", appellate);
            }
            //end process end affiliates and remap 
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
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
